package io.gohoon.waffle.handlers

import io.gohoon.waffle.Data
import io.gohoon.waffle.Entry
import io.gohoon.waffle.G
import io.gohoon.waffle.structure.WaffleState
import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.floor

class PlayerPositionDetector(private val parent: Entry) : BukkitRunnable() {

    override fun run() {
        if (parent.data.state != WaffleState.INGAME) return
        
        parent.server.onlinePlayers.filter {
            it.health > 0 && 
                    it.world.environment == World.Environment.NORMAL &&
                    !parent.data.isInBorder(it, Data.AbsolutePoint(floor(it.location.x).toInt(), floor(it.location.z).toInt())) &&
                    it.gameMode == GameMode.SURVIVAL
        }.forEach {
            val damageAmount = if (it.world.difficulty == Difficulty.PEACEFUL) 5.0 else 2.0
            if (it.health <= damageAmount && G.DEATH_BY_WAFFLE[it.uniqueId] != true) {
                G.DEATH_BY_WAFFLE[it.uniqueId] = true
            }
            it.damage(damageAmount)
        }

    }

}