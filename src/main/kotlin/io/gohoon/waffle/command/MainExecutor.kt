package io.gohoon.waffle.command

import io.gohoon.waffle.Data
import io.gohoon.waffle.Entry
import io.gohoon.waffle.G
import io.gohoon.waffle.structure.WaffleState
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class MainExecutor(
    private val parent: Entry
) {
    
    companion object {
        const val INVALIDATE = "invalidate"
        const val START = "start"
        const val PAUSE = "pause"
        const val RESUME = "resume"
        const val MANAGE = "manage"
        const val QUERY = "query"
        const val SAVE = "save"
        const val UPDATE_TERMINALS = "update_terminals"
    }
    
    private val manageExecutor = ManageExecutor(parent)
    private val queryExecutor = QueryExecutor(parent)
    
    fun execute(sender: Player, args: Array<out String>): Boolean {
        return when(args[0]) {
            START -> {
                start(sender)
                parent.server.broadcastMessage(G.Chatting.withWaffle("game started!", ChatColor.GOLD))
                parent.server.broadcastMessage(G.Chatting.withDefault("main position is: (${parent.data.areas[G.MAIN_AREA]!!.x}, ${parent.data.areas[G.MAIN_AREA]!!.z})", ChatColor.GRAY))
                parent.server.broadcastMessage(G.Chatting.withDefault("press 'l' key to check advancements!", ChatColor.GRAY))
                true
            }
            INVALIDATE -> {
                invalidate()
                sender.sendMessage(G.Chatting.withWaffle("invalidated.", ChatColor.GRAY))
                true
            }
            PAUSE -> {
                parent.data.state = WaffleState.PAUSED
                sender.sendMessage(G.Chatting.withWaffle("paused.", ChatColor.GRAY))
                true
            }
            RESUME -> {
                if (parent.data.state != WaffleState.PAUSED) {
                    sender.sendMessage(G.Chatting.withWaffle("game is already running, or not started.", ChatColor.GRAY))
                } else {
                    parent.data.state = WaffleState.INGAME
                    sender.sendMessage(G.Chatting.withWaffle("resumed.", ChatColor.GRAY))
                }
                true
            }
            MANAGE -> {
                manageExecutor.execute(sender, args.sliceArray(1 until args.size))
            }
            QUERY -> {
                queryExecutor.execute(sender, args.sliceArray(1 until args.size))
            }
            SAVE -> {
                parent.data.save(G.DATA_PATH)
                sender.sendMessage(G.Chatting.withWaffle("all data has been saved.", ChatColor.GRAY))
                true
            }
            UPDATE_TERMINALS -> {
                parent.server.onlinePlayers.forEach { player ->
                    G.Advancements.TERMINALS.forEach { advancement ->
                        G.Advancements.grant(parent, player, advancement)
                    }
                }
                true
            }
            else -> false
        }
    }
    
    fun getTabComplete(args: Array<out String>): MutableList<String> {
        return when {
            args.size == 1 -> mutableListOf(START, INVALIDATE, PAUSE, RESUME, MANAGE, QUERY, SAVE, UPDATE_TERMINALS)
            args.size >= 2 && args[0] == MANAGE -> manageExecutor.getTabComplete(args.sliceArray(1 until args.size))
            args.size >= 2 && args[0] == QUERY -> queryExecutor.getTabComplete(args.sliceArray(1 until args.size))
            else -> mutableListOf()
        }
    }
    
    private fun start(sender: Player) {
        parent.reset()
        parent.data.state = WaffleState.INGAME
        parent.data.areas[G.MAIN_AREA] = Data.AbsolutePoint(sender.location.blockX, sender.location.blockZ)
        parent.data.unlocked[G.MAIN_AREA] = mutableListOf(Data.AreaFragment(G.MAIN_AREA, 0, 0))
        parent.data.updateOutlinesAndPoints()
        parent.server.onlinePlayers.forEach { player -> 
            parent.data.players.add(player.uniqueId)
            parent.data.advancements.add(mutableMapOf())
            
            G.Advancements.grant(parent, player, G.Advancements.ROOT)
            G.Advancements.TERMINALS.forEach { advancement ->
                G.Advancements.grant(parent, player, advancement)
            }
        }
    }
    
    private fun invalidate() {
        parent.reset()
        
        parent.server.onlinePlayers.forEach { player ->
            G.Advancements.revoke(parent, player, G.Advancements.ROOT)
            
            G.Advancements.DEFAULT.forEach { advancement ->
                G.Advancements.revoke(parent, player, advancement)
            }

            G.Advancements.TERMINALS.forEach { advancement ->
                G.Advancements.revoke(parent, player, advancement)
            }
        }
    }
    
}