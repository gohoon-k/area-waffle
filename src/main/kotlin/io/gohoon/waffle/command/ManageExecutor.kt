package io.gohoon.waffle.command

import io.gohoon.waffle.Data
import io.gohoon.waffle.Entry
import io.gohoon.waffle.G
import io.gohoon.waffle.structure.WaffleState
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*

class ManageExecutor(
    private val parent: Entry
) {

    companion object {
        const val ADD = "add"
        const val REMOVE = "remove"
        const val UNLOCK = "unlock"
        const val LOCK = "lock"
        const val ADVANCEMENTS = "advancements"
    }

    fun execute(sender: Player, args: Array<out String>): Boolean {
        if (parent.data.state != WaffleState.INGAME) {
            sender.sendMessage(G.Chatting.withWaffle("manage command can only run in 'ingame' mode!!", ChatColor.RED))
            sender.sendMessage(
                G.Chatting.withDefault(
                    "try execute: 'waffle start' or 'waffle resume' first",
                    ChatColor.GRAY
                )
            )
            return true
        }

        when (args[0]) {
            ADD -> {
                val next = args.slice(1 until args.size)
                if (next.size != 3) {
                    sender.sendMessage(G.Chatting.withWaffle("parameter count mismatch!!", ChatColor.RED))
                    sender.sendMessage(G.Chatting.withDefault("usage: ... add <area-name> <x> <z>", ChatColor.GRAY))
                } else {
                    try {
                        val x = next[1].toInt()
                        val z = next[2].toInt()
                        parent.data.areas[next[0]] = Data.AbsolutePoint(x, z)
                        parent.data.unlocked[next[0]] = mutableListOf(Data.AreaFragment(next[0], 0, 0))
                        parent.data.updateOutlinesAndPoints()
                    } catch (e: NumberFormatException) {
                        sender.sendMessage(G.Chatting.withWaffle("invalid number passed!!", ChatColor.RED))
                    }
                }
            }
            REMOVE -> {
                val name = args[1]
                if (!parent.data.areas.keys.contains(name)) {
                    sender.sendMessage(G.Chatting.withWaffle("unknown area name!!", ChatColor.RED))
                } else {
                    parent.data.areas.remove(name)
                    parent.data.unlocked.remove(name)
                    parent.data.updateOutlinesAndPoints()
                }
            }
            UNLOCK -> {
                val next = args.slice(1 until args.size)
                if (next.size != 3) {
                    sender.sendMessage(G.Chatting.withWaffle("parameter count mismatch!!", ChatColor.RED))
                    sender.sendMessage(
                        G.Chatting.withDefault(
                            "usage: ... unlock <area-name> <relative-x> <relative-z>",
                            ChatColor.GRAY
                        )
                    )
                } else if (!parent.data.areas.keys.contains(next[0])) {
                    sender.sendMessage(G.Chatting.withWaffle("unknown area name!!", ChatColor.RED))
                } else {
                    try {
                        val x = next[1].toInt()
                        val z = next[2].toInt()

                        val unlocked = parent.data.unlocked[next[0]]!!
                        val target = Data.AreaFragment(next[0], x, z)
                        if (!unlocked.contains(target)) {
                            unlocked.add(target)
                            sender.sendMessage(
                                G.Chatting.withWaffle(
                                    "area: (${target.x}, ${target.z}) has been unlocked by command.",
                                    ChatColor.GRAY
                                )
                            )
                        } else {
                            sender.sendMessage(
                                G.Chatting.withWaffle(
                                    "this area fragment is already unlocked!!",
                                    ChatColor.RED
                                )
                            )
                        }
                    } catch (e: NumberFormatException) {
                        sender.sendMessage(
                            G.Chatting.withWaffle(
                                "invalid number passed!! only Integer number is allowed!!",
                                ChatColor.RED
                            )
                        )
                    }
                }
            }
            LOCK -> {
                val next = args.slice(1 until args.size)
                if (next.size != 3) {
                    sender.sendMessage("" + ChatColor.RED + "[waffle] parameter count mismatch!!")
                    sender.sendMessage("" + ChatColor.GRAY + "usage: ... lock <area-name> <relative-x> <relative-z>")
                } else if (!parent.data.areas.keys.contains(next[0])) {
                    sender.sendMessage(G.Chatting.withWaffle("unknown area name!!", ChatColor.RED))
                } else {
                    try {
                        val x = next[1].toInt()
                        val z = next[2].toInt()

                        if (next[0] == "main" && x == 0 && z == 0) {
                            sender.sendMessage(
                                G.Chatting.withWaffle(
                                    "main position of 'main' area cannot be locked!!",
                                    ChatColor.RED
                                )
                            )
                            return true
                        } else if (x == 0 && z == 0) {
                            sender.sendMessage(G.Chatting.withWaffle("main position cannot be locked!!", ChatColor.RED))
                            sender.sendMessage(
                                G.Chatting.withDefault(
                                    "try 'waffle manage remove ${next[0]}' if you want to remove all areas from ${next[0]}",
                                    ChatColor.GRAY
                                )
                            )
                        }

                        val unlocked = parent.data.unlocked[next[0]]!!
                        val target = Data.AreaFragment(next[0], x, z)
                        if (unlocked.contains(target)) {
                            unlocked.remove(target)
                            sender.sendMessage(
                                G.Chatting.withWaffle(
                                    "area: (${target.x}, ${target.z}) has been locked.",
                                    ChatColor.GRAY
                                )
                            )
                        } else {
                            sender.sendMessage(
                                G.Chatting.withWaffle(
                                    "this area fragment is already locked!!",
                                    ChatColor.RED
                                )
                            )
                        }
                    } catch (e: NumberFormatException) {
                        sender.sendMessage(
                            G.Chatting.withWaffle(
                                "invalid number passed!! only Integer number is allowed!!",
                                ChatColor.RED
                            )
                        )
                    }
                }
            }
            ADVANCEMENTS -> {
                val next = args.slice(1 until args.size)
                if (next.size != 3) {
                    sender.sendMessage(G.Chatting.withWaffle("manages advancements.", ChatColor.GRAY))
                    sender.sendMessage(
                        G.Chatting.withDefault(
                            "usage: / ... advancements revoke <player> <advancement-namespace-key>",
                            ChatColor.GRAY
                        )
                    )
                    return true
                }
                if (next[0] != "revoke") {
                    sender.sendMessage(G.Chatting.withWaffle("unknown argument: ${next[0]}", ChatColor.RED))
                    return true
                }
                
                val player = parent.server.getPlayer(next[1])
                if (!parent.data.players.contains(player?.uniqueId)) {
                    sender.sendMessage(G.Chatting.withWaffle("unknown player!!", ChatColor.RED))
                    return true
                }
                val playerIndex = parent.data.players.indexOf(player?.uniqueId)
                if (playerIndex == -1) {
                    sender.sendMessage(G.Chatting.withWaffle("unexpected error!!", ChatColor.RED))
                    return true
                }
                
                if (!parent.data.advancements[playerIndex].containsKey(next[2])){
                    sender.sendMessage(G.Chatting.withWaffle("that advancement is not achieved yet!!", ChatColor.RED))
                    return true
                }
                if (parent.data.advancements[playerIndex][next[2]] == false) {
                    sender.sendMessage(G.Chatting.withWaffle("that advancement is already revoked!!", ChatColor.RED))
                    return true
                }
                
                parent.data.advancements[playerIndex].remove(next[2])
            }
        }
        return true
    }

    fun getTabComplete(args: Array<out String>): MutableList<String> {
        if (parent.data.state != WaffleState.INGAME) return mutableListOf("<illegal-state>")

        return when (args.size) {
            1 -> mutableListOf(ADD, REMOVE, UNLOCK, LOCK, ADVANCEMENTS)
            2 -> when (args[0]) {
                ADD -> mutableListOf("<name>")
                REMOVE -> {
                    val keys = parent.data.areas.keys.toSet().minus(G.MAIN_AREA)
                    if (keys.isEmpty())
                        mutableListOf("<no-removable-area>")
                    else
                        keys.toMutableList()
                }
                UNLOCK -> {
                    val keys = parent.data.areas.keys.toMutableList()
                    if (keys.isEmpty()) {
                        mutableListOf("<no-unlockable-area>")
                    } else {
                        keys
                    }
                }
                LOCK -> {
                    val keys = parent.data.areas.keys.toMutableList()
                    if (keys.isEmpty()) {
                        mutableListOf("<no-lockable-area>")
                    } else {
                        keys
                    }
                }
                ADVANCEMENTS -> {
                    mutableListOf("revoke")
                }
                else -> mutableListOf()
            }
            3 -> when (args[0]) {
                ADD -> mutableListOf("<x-absolute>")
                UNLOCK -> mutableListOf("<x-relative>")
                LOCK -> mutableListOf("<x-relative>")
                ADVANCEMENTS -> parent.data.players.map { uuid -> parent.server.getPlayer(uuid)?.displayName ?: parent.server.getOfflinePlayer(uuid).player?.displayName ?: "" }.toMutableList()
                else -> mutableListOf()
            }
            4 -> when (args[0]) {
                ADD -> mutableListOf("<z-absolute>")
                UNLOCK -> mutableListOf("<z-relative>")
                LOCK -> mutableListOf("<z-relative>")
                ADVANCEMENTS -> parent.data.advancements[parent.data.players.indexOf(parent.server.getPlayer(args[2])?.uniqueId)].keys.toMutableList()
                else -> mutableListOf()
            }
            else -> mutableListOf()
        }
    }

}