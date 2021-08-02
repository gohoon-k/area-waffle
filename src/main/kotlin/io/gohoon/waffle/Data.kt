package io.gohoon.waffle

import io.gohoon.waffle.structure.WaffleState
import org.bukkit.entity.Player
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class Data(
    var state: WaffleState,
    var players: MutableList<UUID>,
    var advancements: MutableList<MutableMap<String, Boolean>>,
    var areas: MutableMap<String, AbsolutePoint>,
    var unlocked: MutableMap<String, MutableList<AreaFragment>>
) : Serializable {

    companion object {

        const val serialVersionUID = -192748393820983L

        fun load(path: String): Data? {
            return try {
                val boo = BukkitObjectInputStream(GZIPInputStream(FileInputStream(path)))
                val result = boo.readObject() as Data
                boo.close()
                result.chestPoints = mutableMapOf()
                result.areaOutlines = mutableMapOf()
                result.playerInArea = mutableMapOf()
                result.areaOutlinesByPlayers = mutableMapOf()
                result.chestPointsByPlayers = mutableMapOf()
                result.areaOutlinesCoordinates = mutableMapOf()
                result.chestPointsCoordinates = mutableMapOf()
                result
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
        
        fun generate(): Data {
            return Data(
                WaffleState.INVALIDATED, mutableListOf(), mutableListOf(), mutableMapOf(), mutableMapOf()
            )
        }
        
    }

    fun save(path: String): Boolean {
        return try {
            val boo = BukkitObjectOutputStream(GZIPOutputStream(FileOutputStream(path)))
            boo.writeObject(this)
            boo.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    
    @Transient lateinit var chestPoints: MutableMap<String, Array<ChestPoint>>
    @Transient lateinit var areaOutlines: MutableMap<String, Array<AreaOutline>>

    @Transient lateinit var playerInArea: MutableMap<UUID, String>

    @Transient lateinit var chestPointsByPlayers: MutableMap<UUID, MutableList<Array<Double>>>
    @Transient lateinit var areaOutlinesByPlayers: MutableMap<UUID, MutableList<Array<Double>>>
    
    @Transient lateinit var chestPointsCoordinates: MutableMap<String, Array<AbsolutePoint>>
    @Transient lateinit var areaOutlinesCoordinates: MutableMap<String, Array<AbsolutePoint>>
    
    fun updateOutlinesAndPoints() {
        areas.keys.forEach { areaID ->
            val area = unlocked[areaID] ?: return@forEach
            
            val points = mutableListOf<ChestPoint>()
            val pointCoordinates = mutableListOf<AbsolutePoint>()
            area.forEach { areaFragment ->
                val checkingAreas = arrayOf(
                    AreaFragment(areaID, areaFragment.x, areaFragment.z + 1),
                    AreaFragment(areaID, areaFragment.x + 1, areaFragment.z),
                    AreaFragment(areaID, areaFragment.x, areaFragment.z - 1),
                    AreaFragment(areaID, areaFragment.x - 1, areaFragment.z)
                )
                checkingAreas.forEachIndexed { index, checkTarget ->
                    if (!area.any { each -> each.x == checkTarget.x && each.z == checkTarget.z }) {
                        points.add(areaFragment.getChestPoints(areas[areaID]!!)[index])
                        pointCoordinates.add(areaFragment.getChestPoints(areas[areaID]!!)[index].position)
                    }
                }
            }
            chestPoints[areaID] = points.toTypedArray()
            chestPointsCoordinates[areaID] = pointCoordinates.toTypedArray()


            val outlines = mutableListOf<AreaOutline>()
            val outlineCoordinates = mutableListOf<AbsolutePoint>()
            
            val outlineCheckTargets = arrayOf(
                RelativePoint(0, 1), RelativePoint(-1, 0), RelativePoint(0, -1), RelativePoint(1, 0)
            )
            val cornerCheckTargets = arrayOf(
                arrayOf(RelativePoint(1, 0), RelativePoint(0, -1), RelativePoint(1, -1)),
                arrayOf(RelativePoint(0, -1), RelativePoint(-1, 0), RelativePoint(-1, -1)),
                arrayOf(RelativePoint(0, 1), RelativePoint(-1, 0), RelativePoint(-1, 1)),
                arrayOf(RelativePoint(0, 1), RelativePoint(1, 0), RelativePoint(1, 1)),
            )

            area.forEach { areaFragment ->
                val directions = mutableListOf<Int>()
                val corners = mutableListOf<Int>()

                outlineCheckTargets.forEachIndexed { index, checkTarget ->
                    if (!area.any { each -> each == AreaFragment(areaID, areaFragment.x + checkTarget.x, areaFragment.z + checkTarget.z) }) {
                        directions.add(index)
                    }
                }
                cornerCheckTargets.forEachIndexed { index, checkTarget ->
                    val check0 = area.any { each -> each == AreaFragment(areaID, areaFragment.x + checkTarget[0].x, areaFragment.z + checkTarget[0].z) }
                    val check1 = area.any { each -> each == AreaFragment(areaID, areaFragment.x + checkTarget[1].x, areaFragment.z + checkTarget[1].z) }
                    val check2 = area.any { each -> each == AreaFragment(areaID, areaFragment.x + checkTarget[2].x, areaFragment.z + checkTarget[2].z) }

                    if (check0 && check1 && !check2) {
                        corners.add(index)
                    }
                }

                val outline = AreaOutline(areaFragment, directions.toTypedArray(), corners.toTypedArray())
                outlines.add(outline)
                outline.coords(areas[areaID]!!).forEach { 
                    outlineCoordinates.add(it)
                }
            }
            areaOutlines[areaID] = outlines.toTypedArray()
            areaOutlinesCoordinates[areaID] = outlineCoordinates.toTypedArray()
        }
        
    }
    
    fun isInBorder(player: Player, loc: AbsolutePoint): Boolean {
        areas.keys.forEach { areaID ->
            val area = unlocked[areaID] ?: return@forEach
            val areaPos = areas[areaID] ?: return@forEach

            area.forEach { areaFragment ->
                if (areaFragment.coord(areaPos).x - G.FIELD_SIZE / 2 <= loc.x && areaFragment.coord(areaPos).x + G.FIELD_SIZE / 2 >= loc.x &&
                    areaFragment.coord(areaPos).z - G.FIELD_SIZE / 2 <= loc.z && areaFragment.coord(areaPos).z + G.FIELD_SIZE / 2 >= loc.z
                ) {
                    playerInArea[player.uniqueId] = areaID
                    return true
                }
            }
        }
        return false
    }
    
    class AbsolutePoint(val x: Int, val z: Int) : Serializable {
        override fun equals(other: Any?): Boolean {
            if (other !is AbsolutePoint) return false
            return other.x == this.x && other.z == this.z
        }

        override fun hashCode(): Int {
            var result = x
            result = 31 * result + z
            return result
        }
    }
    class RelativePoint(val x: Int, val z: Int) : Serializable

    class AreaFragment(val parentID: String, val x: Int, val z: Int) : Serializable {
        fun getChestPoints(initPosition: AbsolutePoint): Array<ChestPoint> {
            val center = coord(initPosition)
            return arrayOf(
                ChestPoint(AbsolutePoint(center.x, center.z + G.FIELD_SIZE / 2), RelativePoint(x, z + 1)),
                ChestPoint(AbsolutePoint(center.x + G.FIELD_SIZE / 2, center.z), RelativePoint(x + 1, z)),
                ChestPoint(AbsolutePoint(center.x, center.z - G.FIELD_SIZE / 2), RelativePoint(x, z - 1)),
                ChestPoint(AbsolutePoint(center.x - G.FIELD_SIZE / 2, center.z), RelativePoint(x - 1, z))
            )
        }

        fun coord(initPosition: AbsolutePoint): AbsolutePoint {
            return AbsolutePoint(initPosition.x + x * G.FIELD_SIZE, initPosition.z + z * G.FIELD_SIZE)
        }

        override fun equals(other: Any?): Boolean {
            if (other !is AreaFragment) return false
            return other.parentID == this.parentID && other.x == this.x && other.z == this.z
        }

        override fun hashCode(): Int {
            var result = x
            result = 31 * result + z
            return result
        }
    }

    class AreaOutline(val areaFragment: AreaFragment, val directions: Array<Int>, val corners: Array<Int>) : Serializable {
        fun coords(initPosition: AbsolutePoint): Array<AbsolutePoint> {
            val result = mutableListOf<AbsolutePoint>()
            val directionRelatives = arrayOf(RelativePoint(0, 1), RelativePoint(-1, 0), RelativePoint(0, -1), RelativePoint(1, 0))
            val areaFragmentCenter = areaFragment.coord(initPosition)
            directions.forEach {
                if (it == 0 || it == 2) {
                    for (i in -2 .. 2) {
                        result.add(AbsolutePoint(
                            areaFragmentCenter.x + directionRelatives[it].x * (G.FIELD_SIZE / 2) + i,
                            areaFragmentCenter.z + directionRelatives[it].z * (G.FIELD_SIZE / 2)
                        ))
                    }
                } else if (it == 1 || it == 3) {
                    for (i in -2 .. 2) {
                        result.add(AbsolutePoint(
                            areaFragmentCenter.x + directionRelatives[it].x * (G.FIELD_SIZE / 2),
                            areaFragmentCenter.z + directionRelatives[it].z * (G.FIELD_SIZE / 2) + i
                        ))
                    }
                }
            }
            
            val cornerRelatives = arrayOf(RelativePoint(1, -1), RelativePoint(-1, -1), RelativePoint(-1, 1), RelativePoint(1, 1))
            corners.forEach { 
                result.add(
                    AbsolutePoint(
                    areaFragmentCenter.x + cornerRelatives[it].x * (G.FIELD_SIZE / 2),
                    areaFragmentCenter.z + cornerRelatives[it].z * (G.FIELD_SIZE / 2)
                )
                )
            }
            return result.toTypedArray()
        }
    }

    class ChestPoint(val position: AbsolutePoint, val unlock: RelativePoint) : Serializable

}