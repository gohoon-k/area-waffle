package io.gohoon.waffle.utils

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.*
import org.bukkit.entity.Player
import kotlin.math.floor

class MaterialUtils {
    
    companion object {

        private fun isValidBlock(block: Block): Boolean {
            return (block.type.isBlock && block.type.isSolid &&
                    !block.type.isLeaves() && !block.type.isDecorationBlocks() &&
                    !block.type.isExcluded() && !block.type.isSlab() && block.type != Material.BARRIER) || block.type.isSourceFluid()
        }

        private fun isInvalidBlock(block: Block): Boolean {
            return !isValidBlock(block)
        }

        private fun Material.isLeaves(): Boolean {
            return this.createBlockData() is Leaves
        }

        private fun Material.isDecorationBlocks(): Boolean {
            val excluded = arrayOf(
                Material.SPAWNER, Material.BEACON, Material.ENDER_CHEST
            )
            excluded.forEach {
                if (this == it)
                    return true
            }
            return false
        }

        private fun Material.isExcluded(): Boolean {
            val blockData = this.createBlockData()
            return blockData is Wall || blockData is Fence || blockData is Gate
        }

        private fun Material.isSlab(): Boolean {
            val blockData = this.createBlockData()
            return blockData is Slab && blockData.type != Slab.Type.DOUBLE
        }
        
        private fun Material.isSourceFluid(): Boolean {
            return this == Material.WATER || this == Material.LAVA
        }
        
        fun getYOffset(block: Block): Double {
            var yOffset = 0.0
            if (block.type.isSlab()) {
                val slabType = (block.blockData as Slab).type
                yOffset = when (slabType) {
                    Slab.Type.BOTTOM -> 0.5
                    Slab.Type.TOP, Slab.Type.DOUBLE -> 1.0
                }
            } else if (block.type == Material.SNOW) {
                val snowHeight = (block.blockData as Snow).layers + 1
                yOffset = snowHeight * 0.1
            }
            return yOffset
        }
        
        private fun getRawTargetY(player: Player, x: Double, z: Double): Array<Int> {
            val world = player.world
            val playerY = player.location.y
            var overY = playerY.toInt()
            var belowY = playerY.toInt()

            val xInt = floor(x).toInt()
            val zInt = floor(z).toInt()

            var block = world.getBlockAt(xInt, playerY.toInt(), zInt)

            if (isValidBlock(block)) {
                while (isValidBlock(block) && overY < 255) {
                    overY++
                    block = world.getBlockAt(xInt, overY, zInt)
                }
                block = world.getBlockAt(xInt, playerY.toInt(), zInt)
                while (isValidBlock(block) && belowY > 0) {
                    belowY--
                    block = world.getBlockAt(xInt, belowY, zInt)
                }
            }

            block = world.getBlockAt(xInt, belowY, zInt)
            while (isInvalidBlock(block) && belowY > 0) {
                belowY--
                block = world.getBlockAt(xInt, belowY, zInt)
            }
            belowY++
            
            return arrayOf(overY, belowY)
        }

        fun getTargetY(player: Player, x: Double, z: Double): Int {
            val playerY = player.location.y
            
            val result = getRawTargetY(player, x, z)
            
            val overY = result[0]
            val belowY = result[1]
            
            return if (overY != playerY.toInt() && overY - playerY <= 1.55) overY else belowY
        }
        
        fun getOverTargetY(player: Player, x: Double, z: Double): Int {
            val result = getRawTargetY(player, x, z)
            
            return result[0]
        }
        
    }
    
}