package io.gohoon.waffle.events

import io.gohoon.waffle.Data
import io.gohoon.waffle.Entry
import io.gohoon.waffle.handlers.DataUpdater
import io.gohoon.waffle.structure.WaffleState
import org.bukkit.World
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockEvent
import org.bukkit.event.block.BlockExpEvent
import org.bukkit.event.block.BlockPlaceEvent

class BlockListener(
    private val parent: Entry,
    private val dataUpdater: DataUpdater
) : Listener {

    @EventHandler
    fun onBlockDestroyed(event: BlockBreakEvent) {
        if (parent.data.state != WaffleState.INGAME) return
        if (event.player.world.environment != World.Environment.NORMAL) return
        
        update(event)
    }
    
    @EventHandler
    fun onBlockPlaced(event: BlockPlaceEvent) {
        if (parent.data.state != WaffleState.INGAME) return
        if (event.player.world.environment != World.Environment.NORMAL) return

        update(event)
    }
    
    private fun update(event: Event) {
        if (event is BlockBreakEvent) {
            if (event.block.y <= event.player.location.blockY + 2 &&
                parent.data.areaOutlinesCoordinates[parent.data.playerInArea[event.player.uniqueId]]
                    ?.contains(Data.AbsolutePoint(event.block.x, event.block.z)) == true) {
                parent.server.onlinePlayers.forEach { player -> dataUpdater.mustUpdateFlag[player.uniqueId] = true }
            }
        } else if (event is BlockPlaceEvent) {
            if (event.block.y <= event.player.location.blockY + 2 &&
                parent.data.areaOutlinesCoordinates[parent.data.playerInArea[event.player.uniqueId]]
                    ?.contains(Data.AbsolutePoint(event.block.x, event.block.z)) == true) {
                parent.server.onlinePlayers.forEach { player -> dataUpdater.mustUpdateFlag[player.uniqueId] = true }
            }
        }
    }

}