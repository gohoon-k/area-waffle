package io.gohoon.waffle.events

import io.gohoon.waffle.Data
import io.gohoon.waffle.Entry
import io.gohoon.waffle.structure.WaffleState
import io.gohoon.waffle.utils.FireworkUtils
import io.gohoon.waffle.utils.MaterialUtils
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

class AdvancementListener(private val parent: Entry): Listener {
    
    @EventHandler
    fun advancementMadeEvent(event: PlayerAdvancementDoneEvent) {
        if (parent.data.state != WaffleState.INGAME) return
        
        val playerAdvancementData = parent.data.advancements[parent.data.players.indexOf(event.player.uniqueId)]
        val advancementKey = event.advancement.key
        val keyString = "${advancementKey.namespace}:${advancementKey.key}"
        if(advancementKey.namespace != "waffle" &&
            !advancementKey.key.contains("recipes") && 
            !advancementKey.key.contains(Regex("/root$")) &&
            playerAdvancementData[keyString] != true) {
                
            playerAdvancementData[keyString] = true
            val area = parent.data.areas.keys.random()
            val point = parent.data.chestPoints[area]!!.random()
            
            parent.data.unlocked[area]!!.add(Data.AreaFragment(area, point.unlock.x, point.unlock.z))
            parent.data.updateOutlinesAndPoints()
            
            parent.server.broadcastMessage("" + ChatColor.GOLD + event.player.displayName + ChatColor.BLUE + " has made advancement!!")
            parent.server.broadcastMessage("" + ChatColor.GRAY + "unlocking random area: " + ChatColor.WHITE + "(${point.unlock.x}, ${point.unlock.z})")
            
            val newArea = Data.AreaFragment(area, point.unlock.x, point.unlock.z).coord(parent.data.areas[area]!!)
            val y = MaterialUtils.getOverTargetY(event.player, newArea.x.toDouble(), newArea.z.toDouble())
            
            var fireworkSpawned = false
            parent.server.onlinePlayers.forEach { 
                if (fireworkSpawned) return@forEach
                fireworkSpawned = FireworkUtils.create(it, newArea.x, y, newArea.z)
            }
        }
    }
    
}