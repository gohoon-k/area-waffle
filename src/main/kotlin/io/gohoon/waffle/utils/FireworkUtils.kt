package io.gohoon.waffle.utils

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player

class FireworkUtils {
    
    companion object {
        fun create(player: Player, x: Int, y: Int, z: Int): Boolean{
            if (player.world.environment != World.Environment.NORMAL) return false
            //{LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Flight:100,Explosions:[{Type:4,Colors:[I; 16777045]},{Type:4,Colors:[I; 16755200]}]}}}}
            val firework =
                player.world.spawnEntity(
                    Location(player.world, x + 0.5, y + 0.5, z + 0.5),
                    EntityType.FIREWORK
                ) as Firework
            val fireworkMeta = firework.fireworkMeta
            fireworkMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.YELLOW).build())
            fireworkMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.ORANGE).build())
            fireworkMeta.power = 0

            firework.fireworkMeta = fireworkMeta
            return true
        }
    }
    
}