package io.gohoon.waffle.handlers

import io.gohoon.waffle.Entry
import io.gohoon.waffle.structure.WaffleState
import org.bukkit.*
import org.bukkit.scheduler.BukkitRunnable

class ParticleCreator(private val parent: Entry) : BukkitRunnable() {

    private val borderColors = arrayOf(
        Color.fromRGB(255, 0, 0), Color.fromRGB(200, 0, 0),
        Color.fromRGB(150, 0, 0), Color.fromRGB(225, 0, 0),
        Color.fromRGB(175, 0, 0)
    )

    private val pointColors = arrayOf(
        Color.fromRGB(168, 119, 44), Color.fromRGB(159, 105, 34),
        Color.fromRGB(147, 106, 45), Color.fromRGB(130, 96, 46)
    )
    
    override fun run() {
        if (parent.data.state != WaffleState.INGAME && parent.data.state != WaffleState.PAUSED) return

        parent.server.onlinePlayers.forEach { player ->
            val world = player.world
            if (world.environment != World.Environment.NORMAL) return@forEach
            
            parent.data.areaOutlinesByPlayers[player.uniqueId]?.forEach {
                player.spawnParticle(
                    Particle.REDSTONE,
                    Location(world, it[0], it[1], it[2]), 
                    it[5].toInt(), 
                    it[3], 0.0, it[4],
                    1.0,
                    Particle.DustOptions(borderColors.random(), 1.0f)
                )
            }

            parent.data.chestPointsByPlayers[player.uniqueId]?.forEach {
                player.spawnParticle(
                    Particle.REDSTONE,
                    Location(world, it[0] + 0.5, it[1], it[2] + 0.5),
                    2,
                    0.125, 0.0, 0.125,
                    1.0,
                    Particle.DustOptions(pointColors.random(), 1.5f)
                )
            }
        }
    }
}