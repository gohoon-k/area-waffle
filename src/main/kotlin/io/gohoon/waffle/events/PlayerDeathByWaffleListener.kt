package io.gohoon.waffle.events

import io.gohoon.waffle.G
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathByWaffleListener: Listener {
    
    @EventHandler
    fun onPlayerDeathByWaffle(event: PlayerDeathEvent) {
        if (G.DEATH_BY_WAFFLE[event.entity.uniqueId] == true) {
            event.deathMessage = "${event.entity.displayName}${G.DeathMessages.DEFAULT.random()}"
            G.DEATH_BY_WAFFLE[event.entity.uniqueId] = false
        }
    }
    
}