package io.gohoon.waffle.events

import io.gohoon.waffle.Entry
import io.gohoon.waffle.G
import io.gohoon.waffle.structure.WaffleState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val parent: Entry) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (parent.data.state == WaffleState.INGAME &&
            !parent.data.players.contains(event.player.uniqueId)) {
                
            parent.data.players.add(event.player.uniqueId)
            parent.data.advancements.add(mutableMapOf())
            G.Advancements.TERMINALS.forEach { advancement ->
                G.Advancements.grant(parent, event.player, G.Advancements.ROOT)
                G.Advancements.grant(parent, event.player, advancement)
            }
        }
    }

}