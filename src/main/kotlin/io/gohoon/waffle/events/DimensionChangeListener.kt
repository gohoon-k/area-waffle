package io.gohoon.waffle.events

import io.gohoon.waffle.Entry
import io.gohoon.waffle.handlers.DataUpdater
import io.gohoon.waffle.structure.WaffleState
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent

class DimensionChangeListener(
    private val parent: Entry,
    private val dataUpdater: DataUpdater
): Listener {
    
    @EventHandler
    fun onDimensionChange(event: PlayerChangedWorldEvent) {
        if (parent.data.state != WaffleState.INGAME) return
        
        if (event.player.world.environment == World.Environment.NORMAL) {
            dataUpdater.mustUpdateFlag[event.player.uniqueId] = true
        }
    }
    
}