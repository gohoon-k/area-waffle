package io.gohoon.waffle.handlers

import io.gohoon.waffle.Data
import io.gohoon.waffle.Entry
import io.gohoon.waffle.G
import io.gohoon.waffle.utils.MaterialUtils
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.logging.Level
import kotlin.math.floor

class DataUpdater(private val parent: Entry): BukkitRunnable() {
    
    private val playerYCoords = mutableMapOf<UUID, Int>()

    var mustUpdateFlag: MutableMap<UUID, Boolean> = mutableMapOf()

    private val pointDeltas = arrayOf(
        Data.AbsolutePoint(0, G.FIELD_SIZE / 2),
        Data.AbsolutePoint(-G.FIELD_SIZE / 2, 0),
        Data.AbsolutePoint(0, -G.FIELD_SIZE / 2),
        Data.AbsolutePoint(G.FIELD_SIZE / 2, 0)
    )
    private val pointDeltasFrom = arrayOf(
        Data.AbsolutePoint(G.FIELD_SIZE / 2, 0),
        Data.AbsolutePoint(0, G.FIELD_SIZE / 2),
        Data.AbsolutePoint(-G.FIELD_SIZE / 2, 0),
        Data.AbsolutePoint(0, -G.FIELD_SIZE / 2)
    )

    private val cornerDeltas = arrayOf(
        Data.AbsolutePoint(G.FIELD_SIZE / 2, -G.FIELD_SIZE / 2),
        Data.AbsolutePoint(-G.FIELD_SIZE / 2, -G.FIELD_SIZE / 2),
        Data.AbsolutePoint(-G.FIELD_SIZE / 2, G.FIELD_SIZE / 2),
        Data.AbsolutePoint(G.FIELD_SIZE / 2, G.FIELD_SIZE / 2)
    )

    private val cornerPositions = arrayOf(
        arrayOf(arrayOf(0.875, 0.25), arrayOf(0.75, 0.125)),
        arrayOf(arrayOf(0.125, 0.25), arrayOf(0.25, 0.125)),
        arrayOf(arrayOf(0.125, 0.75), arrayOf(0.25, 0.875)),
        arrayOf(arrayOf(0.875, 0.75), arrayOf(0.75, 0.875))
    )
    
    override fun run() {
        parent.server.onlinePlayers.forEach { player ->
            if (playerYCoords[player.uniqueId] == player.location.blockY && mustUpdateFlag[player.uniqueId] == false) return@forEach
            if (player.world.environment != World.Environment.NORMAL) return@forEach

            val areaID = parent.data.playerInArea[player.uniqueId]
            val areaOutlines = parent.data.areaOutlines[areaID] ?: return@forEach
            val chestPoints = parent.data.chestPoints[areaID] ?: return@forEach

            playerYCoords[player.uniqueId] = player.location.blockY
            
            parent.data.areaOutlinesByPlayers[player.uniqueId] = mutableListOf()
            parent.data.chestPointsByPlayers[player.uniqueId] = mutableListOf()

            mustUpdateFlag[player.uniqueId] = false
            
            update(areaOutlines, areaID, player, chestPoints)
        }
    }

    private fun update(
        areaOutlines: Array<Data.AreaOutline>,
        areaID: String?,
        player: Player,
        chestPoints: Array<Data.ChestPoint>
    ) {
        areaOutlines.forEach { outline ->
            outline.directions.forEach { direction ->
                val fromPoint = Data.AbsolutePoint(
                    outline.areaFragment.coord(parent.data.areas[areaID]!!).x + pointDeltas[direction].x + pointDeltasFrom[direction].x,
                    outline.areaFragment.coord(parent.data.areas[areaID]!!).z + pointDeltas[direction].z + pointDeltasFrom[direction].z
                )

                val horizontal = direction == 1 || direction == 3
                val reversed = direction == 0 || direction == 1

                val from = if (horizontal) fromPoint.z else fromPoint.x

                for (i in 0 until G.FIELD_SIZE) {
                    val position = from + (if (reversed) -1.0 else 1.0) * i

                    val x = if (horizontal) fromPoint.x.toDouble() else position
                    val z = if (horizontal) position else fromPoint.z.toDouble()

                    val targetX =
                        x + (if (direction != 1) 0.5 else 0.0) + (if (direction == 1 || direction == 3) 0.25 else 0.0)
                    var targetY = MaterialUtils.getTargetY(player, x, z)
                    val targetZ =
                        z + (if (direction != 2) 0.5 else 0.0) + (if (direction == 0 || direction == 2) 0.25 else 0.0)

                    val underBlock = player.world.getBlockAt(floor(x).toInt(), targetY - 1, floor(z).toInt())
                    if (underBlock.type == Material.CHEST) {
                        targetY--
                    }

                    val yOffset = MaterialUtils.getYOffset(
                        player.world.getBlockAt(floor(x).toInt(), targetY, floor(z).toInt())
                    )

                    val delta = if (i != 0 && i != 4) 0.3 else 0.1

                    parent.data.areaOutlinesByPlayers[player.uniqueId]?.add(
                        arrayOf(
                            targetX,
                            targetY + yOffset + 0.02,
                            targetZ,
                            if (horizontal) 0.0 else delta,
                            if (horizontal) delta else 0.0,
                            3.0
                        )
                    )
                }
            }
            outline.corners.forEach { cornerIndex ->
                val areaFrag = outline.areaFragment

                for (index in 0 until 2) {
                    val x =
                        areaFrag.coord(parent.data.areas[areaID]!!).x + cornerDeltas[cornerIndex].x + cornerPositions[cornerIndex][index][0]
                    val z =
                        areaFrag.coord(parent.data.areas[areaID]!!).z + cornerDeltas[cornerIndex].z + cornerPositions[cornerIndex][index][1]

                    val horizontal = index == 1

                    var targetY = MaterialUtils.getTargetY(player, x, z)

                    val yOffset = MaterialUtils.getYOffset(
                        player.world.getBlockAt(
                            floor(x).toInt(),
                            if (player.world.getBlockAt(
                                    floor(x).toInt(),
                                    targetY - 1,
                                    floor(z).toInt()
                                ).type == Material.CHEST
                            ) --targetY else targetY,
                            floor(z).toInt()
                        )
                    )

                    parent.data.areaOutlinesByPlayers[player.uniqueId]?.add(
                        arrayOf(
                            x,
                            targetY + yOffset + 0.02,
                            z,
                            if (horizontal) 0.0 else 0.0625,
                            if (horizontal) 0.0625 else 0.0,
                            1.0
                        )
                    )
                }
            }
        }
        chestPoints.forEach { point ->
            val x = point.position.x
            val z = point.position.z

            val targetY = MaterialUtils.getTargetY(player, x.toDouble(), z.toDouble())

            val yOffset = MaterialUtils.getYOffset(
                player.world.getBlockAt(x, targetY, z)
            )

            parent.data.chestPointsByPlayers[player.uniqueId]?.add(
                arrayOf(
                    x.toDouble(),
                    targetY + yOffset + 0.02,
                    z.toDouble()
                )
            )
        }
    }

}