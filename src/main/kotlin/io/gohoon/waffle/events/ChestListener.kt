package io.gohoon.waffle.events

import io.gohoon.waffle.Data
import io.gohoon.waffle.Entry
import io.gohoon.waffle.G
import io.gohoon.waffle.handlers.DataUpdater
import io.gohoon.waffle.structure.WaffleAdvancement
import io.gohoon.waffle.structure.WaffleState
import io.gohoon.waffle.utils.FireworkUtils
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import java.util.logging.Level
import kotlin.math.floor

class ChestListener(
    private val parent: Entry,
    private val dataUpdater: DataUpdater
) : Listener {

    @EventHandler
    fun onChestClose(event: InventoryCloseEvent) {
        if (parent.data.state != WaffleState.INGAME) return
        if (event.inventory.type != InventoryType.CHEST) return

        val playerID = parent.data.players.indexOf(event.player.uniqueId)

        val location = event.inventory.location ?: return
        val blockPosition = Data.AbsolutePoint(location.blockX, location.blockZ)

        var areaID = ""
        var inPoint = false
        var unlock = Data.RelativePoint(0, 0)
        run loop@{
            parent.data.chestPoints.forEach { area ->
                area.value.forEach { point ->
                    if (blockPosition == point.position) {
                        areaID = area.key
                        inPoint = true
                        unlock = point.unlock
                        return@loop
                    }
                }
            }
        }

        if (areaID == "") return
        if (!inPoint) return

        var advancementMade = false
        var targetAdvancement: WaffleAdvancement? = null
        G.Advancements.DEFAULT.forEach { advancement ->
            val key = NamespacedKey(parent, "area/${advancement.name}")
            val keyString = "${key.namespace}:${key.key}"

            if (!(parent.data.advancements[playerID].containsKey(keyString) &&
                        parent.data.advancements[playerID][keyString] == true) || advancement.canRepeat
            ) {

                var requirementOK = false
                advancement.requirements.forEachIndexed { index, requirement ->
                    val current = event.inventory.contents[index]
                    if (current != null && current.type == requirement.material && current.amount == requirement.count) {
                        requirementOK = true
                        return@forEachIndexed
                    }
                }
                if (requirementOK) {
                    advancementMade = true
                    targetAdvancement = advancement
                    return@forEach
                }
            }
        }

        if (!advancementMade || targetAdvancement == null) return

        val grantKey = G.Advancements.grant(parent, event.player as Player, targetAdvancement!!) ?: return
        val grantKeyString = "${grantKey.namespace}:${grantKey.key}"
        parent.data.advancements[playerID][grantKeyString] = true

        if (targetAdvancement!!.isChallenge) {
            val newAreas = arrayOf(
                Data.AreaFragment(areaID, unlock.x, unlock.z),
                Data.AreaFragment(areaID, unlock.x + 1, unlock.z),
                Data.AreaFragment(areaID, unlock.x + 1, unlock.z + 1),
                Data.AreaFragment(areaID, unlock.x, unlock.z + 1),
                Data.AreaFragment(areaID, unlock.x - 1, unlock.z + 1),
                Data.AreaFragment(areaID, unlock.x - 1, unlock.z),
                Data.AreaFragment(areaID, unlock.x - 1, unlock.z - 1),
                Data.AreaFragment(areaID, unlock.x, unlock.z - 1),
                Data.AreaFragment(areaID, unlock.x + 1, unlock.z - 1),
            )

            newAreas.forEach { area ->
                if (!parent.data.unlocked[areaID]!!.any { addedArea -> addedArea.x == area.x && addedArea.z == area.z }) {
                    parent.server.logger.log(Level.INFO, "Unlocking Area: ${area.x}, ${area.z}")
                    parent.data.unlocked[areaID]!!.add(area)
                    parent.data.updateOutlinesAndPoints()
                }
            }

            parent.server.broadcastMessage("" + ChatColor.GOLD + event.player.name + ChatColor.BLUE + " unlocked new area: " + ChatColor.WHITE + "(${unlock.x}, ${unlock.z})" + ChatColor.GRAY + " and it's surrounding area!!")
        } else {
            val newArea = Data.AreaFragment(areaID, unlock.x, unlock.z)

            parent.server.logger.log(Level.INFO, "Unlocking Area: ${unlock.x}, ${unlock.z}")
            parent.data.unlocked[areaID]!!.add(newArea)
            parent.data.updateOutlinesAndPoints()

            parent.server.broadcastMessage("" + ChatColor.GOLD + event.player.name + ChatColor.BLUE + " unlocked new area: " + ChatColor.WHITE + "(${unlock.x}, ${unlock.z})")
        }

        if (targetAdvancement!!.canRepeat) {
            event.inventory.clear()
            event.player.world.getBlockAt(location).breakNaturally()
        } else {
            event.player.world.getBlockAt(location).breakNaturally()
        }

        FireworkUtils.create(
            event.player as Player,
            floor(location.x).toInt(),
            floor(location.y).toInt() + 1,
            floor(location.z).toInt()
        )

        parent.server.onlinePlayers.forEach { player -> dataUpdater.mustUpdateFlag[player.uniqueId] = true }
        parent.data.save(G.DATA_PATH)
    }

}