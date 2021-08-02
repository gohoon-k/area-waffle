package io.gohoon.waffle

import io.gohoon.waffle.structure.Requirements
import io.gohoon.waffle.structure.WaffleAdvancement
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class G {
    
    companion object {
        var DATA_PATH = "waffle.dat"
        
        const val MAIN_AREA = "main"
        
        // MUST be odd number
        const val FIELD_SIZE = 5
        
        var DEATH_BY_WAFFLE = mutableMapOf<UUID, Boolean>()
    }
    
    class Chatting {
        companion object {
            fun withWaffle(content: String, contentColor: ChatColor): String {
                return "" + ChatColor.GRAY + "[waffle] " + contentColor + content
            }
            
            fun withDefault(content: String, contentColor: ChatColor): String {
                return "" + contentColor + content
            }
        }
    }
    
    class DeathMessages {
        companion object {
            val DEFAULT = arrayOf(
                "이(가) 자유를 찾아 떠났습니다.",
                "이(가) 선을 넘었습니다.",
                "이(가) player.damage(Double)에 당했습니다.",
                "이(가) PlayerPositionDetector의 32번째 줄에 당했습니다.",
                "이(가) 선을 넘다가 죽었습니다... 선넘지 마라고 (ㅋㅋㅋ)"
            )
        }
    }
    
    class Advancements {
        
        companion object {
            
            val ROOT = WaffleAdvancement("root", requirements = arrayOf())

            val DEFAULT: Array<WaffleAdvancement> = arrayOf(
                WaffleAdvancement("basalt", requirements = arrayOf(Requirements(Material.BASALT, 32))),
                WaffleAdvancement("black_trash", requirements = arrayOf(Requirements(Material.COAL, 64))),
                WaffleAdvancement("blackstone", requirements = arrayOf(Requirements(Material.BLACKSTONE, 64))),
                WaffleAdvancement("blaze_rod", requirements = arrayOf(Requirements(Material.BLAZE_ROD, 16))),
                WaffleAdvancement("blue_something", requirements = arrayOf(Requirements(Material.LAPIS_LAZULI, 32))),
                WaffleAdvancement("carrot", requirements = arrayOf(Requirements(Material.CARROT, 64))),
                WaffleAdvancement("create_crafting_table", requirements = arrayOf(Requirements(Material.CRAFTING_TABLE, 1))),
                WaffleAdvancement("crimson_fungus", requirements = arrayOf(Requirements(Material.CRIMSON_FUNGUS, 1))),
                WaffleAdvancement("crimson_stem", requirements = arrayOf(Requirements(Material.CRIMSON_STEM, 32))),
                WaffleAdvancement("crying_obsidian", requirements = arrayOf(Requirements(Material.CRYING_OBSIDIAN, 1))),
                WaffleAdvancement("current_final_item", isChallenge = true, requirements = arrayOf(Requirements(Material.NETHERITE_SWORD, 1))),
                WaffleAdvancement("end_crystal", requirements = arrayOf(Requirements(Material.END_CRYSTAL, 1))),
                WaffleAdvancement("ender_chest", canRepeat = true, requirements = arrayOf(Requirements(Material.ENDER_CHEST, 1), Requirements(Material.NETHERITE_SCRAP, 1))),
                WaffleAdvancement("ender_eye", isChallenge = true, requirements = arrayOf(Requirements(Material.ENDER_EYE, 12))),
                WaffleAdvancement("ender_pearl", requirements = arrayOf(Requirements(Material.ENDER_PEARL, 12))),
                WaffleAdvancement("endless_mining", requirements = arrayOf(Requirements(Material.STONE_PICKAXE, 1))),
                WaffleAdvancement("endless_trip", requirements = arrayOf(Requirements(Material.OBSIDIAN, 12))),
                WaffleAdvancement("ghast_tear", isChallenge = true, requirements = arrayOf(Requirements(Material.GHAST_TEAR, 1))),
                WaffleAdvancement("give_me_web", requirements = arrayOf(Requirements(Material.STRING, 32))),
                WaffleAdvancement("harder_than_expected", requirements = arrayOf(Requirements(Material.BONE, 16))),
                WaffleAdvancement("hay_block", canRepeat = true, requirements = arrayOf(Requirements(Material.HAY_BLOCK, 16))),
                WaffleAdvancement("hidden_bedrock", isChallenge = true, requirements = arrayOf(Requirements(Material.BEDROCK, 1))),
                WaffleAdvancement("hoe", requirements = arrayOf(Requirements(Material.STONE_HOE, 1))),
                WaffleAdvancement("honey_block", requirements = arrayOf(Requirements(Material.HONEY_BLOCK, 1))),
                WaffleAdvancement("honeycomb", requirements = arrayOf(Requirements(Material.HONEYCOMB, 8))),
                WaffleAdvancement("how_much_is_it", requirements = arrayOf(
                    Requirements(Material.DIAMOND_HELMET, 1),
                    Requirements(Material.DIAMOND_CHESTPLATE, 1),
                    Requirements(Material.DIAMOND_LEGGINGS, 1),
                    Requirements(Material.DIAMOND_BOOTS, 1)
                )
                ),
                WaffleAdvancement("i_protect_myself", requirements = arrayOf(Requirements(Material.IRON_SWORD, 1))),
                WaffleAdvancement("iron_iron", requirements = arrayOf(Requirements(Material.IRON_INGOT, 32))),
                WaffleAdvancement("iron_pickaxe", requirements = arrayOf(Requirements(Material.IRON_PICKAXE, 1))),
                WaffleAdvancement("iron_waste", requirements = arrayOf(
                    Requirements(Material.IRON_HELMET, 1),
                    Requirements(Material.IRON_CHESTPLATE, 1),
                    Requirements(Material.IRON_LEGGINGS, 1),
                    Requirements(Material.IRON_BOOTS, 1)
                )
                ),
                WaffleAdvancement("is_this_possible", isChallenge = true, requirements = arrayOf(Requirements(Material.NETHER_STAR, 1))),
                WaffleAdvancement("lectern", requirements = arrayOf(Requirements(Material.LECTERN, 1))),
                WaffleAdvancement("magma_block", requirements = arrayOf(Requirements(Material.MAGMA_BLOCK, 32))),
                WaffleAdvancement("magma_cream", requirements = arrayOf(Requirements(Material.MAGMA_CREAM, 16))),
                WaffleAdvancement("melon", requirements = arrayOf(Requirements(Material.MELON_SLICE, 64))),
                WaffleAdvancement("more_important_than", requirements = arrayOf(Requirements(Material.TORCH, 64))),
                WaffleAdvancement("more_useless_than", requirements = arrayOf(Requirements(Material.BOW, 1), Requirements(Material.ARROW, 4))),
                WaffleAdvancement("nether_wart_block", requirements = arrayOf(Requirements(Material.NETHER_WART_BLOCK, 32))),
                WaffleAdvancement("netherrack", requirements = arrayOf(Requirements(Material.NETHERRACK, 64))),
                WaffleAdvancement("noteblock", requirements = arrayOf(Requirements(Material.NOTE_BLOCK, 1))),
                WaffleAdvancement("old_final_item", requirements = arrayOf(Requirements(Material.DIAMOND_SWORD, 1))),
                WaffleAdvancement("paper", requirements = arrayOf(Requirements(Material.PAPER, 15))),
                WaffleAdvancement("potato", requirements = arrayOf(Requirements(Material.POTATO, 64))),
                WaffleAdvancement("pumpkin", requirements = arrayOf(Requirements(Material.PUMPKIN, 16))),
                WaffleAdvancement("red_trash", requirements = arrayOf(Requirements(Material.REDSTONE, 64))),
                WaffleAdvancement("repeating_mining", requirements = arrayOf(Requirements(Material.DIAMOND_PICKAXE, 1))),
                WaffleAdvancement("seriously", requirements = arrayOf(Requirements(Material.DIAMOND_HOE, 1))),
                WaffleAdvancement("soooo_nice", isChallenge = true, requirements = arrayOf(
                    Requirements(Material.NETHERITE_HELMET, 1),
                    Requirements(Material.NETHERITE_CHESTPLATE, 1),
                    Requirements(Material.NETHERITE_LEGGINGS, 1),
                    Requirements(Material.NETHERITE_BOOTS, 1)
                )
                ),
                WaffleAdvancement("soul_sand", requirements = arrayOf(Requirements(Material.SOUL_SAND, 32))),
                WaffleAdvancement("soul_soil", requirements = arrayOf(Requirements(Material.SOUL_SOIL, 32))),
                WaffleAdvancement("steak", requirements = arrayOf(Requirements(Material.COOKED_BEEF, 32))),
                WaffleAdvancement("tanoshii", requirements = arrayOf(Requirements(Material.MINECART, 1))),
                WaffleAdvancement("this_is_most_expensive", isChallenge = true, requirements = arrayOf(Requirements(Material.NETHERITE_INGOT, 4))),
                WaffleAdvancement("this_was_most_expensive", requirements = arrayOf(Requirements(Material.DIAMOND, 16))),
                WaffleAdvancement("this_was_trash", requirements = arrayOf(Requirements(Material.GOLD_INGOT, 32))),
                WaffleAdvancement("uaaaaaaa", requirements = arrayOf(Requirements(Material.GUNPOWDER, 8))),
                WaffleAdvancement("warped_fungus", requirements = arrayOf(Requirements(Material.WARPED_FUNGUS, 1))),
                WaffleAdvancement("warped_stem", requirements = arrayOf(Requirements(Material.WARPED_STEM, 32))),
                WaffleAdvancement("warped_wart_block", requirements = arrayOf(Requirements(Material.WARPED_WART_BLOCK, 32))),
                WaffleAdvancement("what_a_waste", isChallenge = true, requirements = arrayOf(Requirements(Material.NETHERITE_HOE, 1))),
                WaffleAdvancement("wheat", requirements = arrayOf(Requirements(Material.WHEAT, 64))),
                WaffleAdvancement("why_did_you_made_this", isChallenge = true, requirements = arrayOf(Requirements(Material.NETHERITE_PICKAXE, 1))),
                WaffleAdvancement("why_magnet_is_this_expensive", isChallenge = true, requirements = arrayOf(Requirements(Material.LODESTONE, 1))),
                WaffleAdvancement("wooden_pickaxe", requirements = arrayOf(Requirements(Material.WOODEN_PICKAXE, 1))),
                WaffleAdvancement("you_are_the_easiest", requirements = arrayOf(Requirements(Material.ROTTEN_FLESH, 32)))
            )

            val TERMINALS = arrayOf(
                WaffleAdvancement("terminal_0", requirements = arrayOf()),
                WaffleAdvancement("terminal_1", requirements = arrayOf()),
                WaffleAdvancement("terminal_2", requirements = arrayOf()),
                WaffleAdvancement("terminal_3", requirements = arrayOf()),
                WaffleAdvancement("terminal_4", requirements = arrayOf()),
                WaffleAdvancement("terminal_5", requirements = arrayOf()),
                WaffleAdvancement("terminal_6", requirements = arrayOf()),
                WaffleAdvancement("terminal_7", requirements = arrayOf()),
                WaffleAdvancement("terminal_8", requirements = arrayOf()),
                WaffleAdvancement("terminal_9", requirements = arrayOf()),
                WaffleAdvancement("terminal_10", requirements = arrayOf()),
                WaffleAdvancement("terminal_11", requirements = arrayOf()),
                WaffleAdvancement("terminal_12", requirements = arrayOf()),
                WaffleAdvancement("terminal_13", requirements = arrayOf()),
                WaffleAdvancement("terminal_14", requirements = arrayOf())
            )


            fun grant(parent: JavaPlugin, player: Player, advancement: WaffleAdvancement): NamespacedKey? {
                val namespacedKey = NamespacedKey(parent, "area/${advancement.name}")
                val adv = parent.server.getAdvancement(namespacedKey) ?: return null
                val progress = player.getAdvancementProgress(adv)
                progress.remainingCriteria.forEach { criteria ->
                    progress.awardCriteria(criteria)
                }
                return namespacedKey
            }

            fun revoke(parent: JavaPlugin, player: Player, advancement: WaffleAdvancement) {
                val namespacedKey = NamespacedKey(parent, "area/${advancement.name}")
                val adv = parent.server.getAdvancement(namespacedKey) ?: return
                val progress = player.getAdvancementProgress(adv)
                progress.awardedCriteria.forEach { criteria ->
                    progress.revokeCriteria(criteria)
                }
            }
            
        }
        
    }
    
}