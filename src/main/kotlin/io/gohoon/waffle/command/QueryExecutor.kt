package io.gohoon.waffle.command

import io.gohoon.waffle.Entry
import io.gohoon.waffle.G
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.floor

class QueryExecutor(
    private val parent: Entry
) {
    
    companion object {
        
        const val POSITION = "location"
        
        const val AREAS = "areas"
        
    }
    
    fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        when (args[0]) {
            AREAS -> {
                val areas = mutableListOf<String>()
                parent.data.areas.forEach { (k, v) -> areas.add("$k(${v.x}, ${v.z})") }
                sender.sendMessage(G.Chatting.withWaffle("There are ${parent.data.areas.size} area(s):", ChatColor.GOLD))
                sender.sendMessage(G.Chatting.withDefault(areas.joinToString(", "), ChatColor.GRAY))
            }
            POSITION -> {
                if (args.size < 2) {
                    sender.sendMessage(G.Chatting.withWaffle("parameter count mismatch!!", ChatColor.RED))
                    sender.sendMessage(G.Chatting.withDefault("usage: ... query position <target-area-name>", ChatColor.GRAY))
                    return true
                }
                val area = parent.data.areas[args[1]]
                if (area == null) {
                    sender.sendMessage(G.Chatting.withWaffle("unknown area name!!", ChatColor.RED))
                    sender.sendMessage(G.Chatting.withDefault("tip: the initial area's name is 'main'.", ChatColor.GRAY))
                    return true
                } 
                val player = sender as Player
                val dx = floor(player.location.x).toInt() - (area.x - G.FIELD_SIZE / 2)
                val dz = floor(player.location.z).toInt() - (area.z - G.FIELD_SIZE / 2)
                val cx = if (dx < 0) -1 else 0
                val cz = if (dz < 0) -1 else 0
                sender.sendMessage(G.Chatting.withWaffle("you are in (${dx / G.FIELD_SIZE + cx}, ${dz / G.FIELD_SIZE + cz}), '${args[0]}' area.", ChatColor.GOLD))
            }
        }
        return true
    }
    
    fun getTabComplete(args: Array<out String>): MutableList<String> {
        return when (args.size) {
            1 -> mutableListOf(AREAS, POSITION)
            2 -> {
                when (args[0]) {
                    POSITION -> parent.data.areas.keys.toMutableList()
                    else -> mutableListOf()
                }
            }
            else -> mutableListOf()
        }
    }
    
}