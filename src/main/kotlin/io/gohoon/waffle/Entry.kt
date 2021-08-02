package io.gohoon.waffle

import io.gohoon.waffle.command.MainExecutor
import io.gohoon.waffle.events.*
import io.gohoon.waffle.handlers.DataUpdater
import io.gohoon.waffle.handlers.ParticleCreator
import io.gohoon.waffle.handlers.PlayerPositionDetector
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Level

class Entry : JavaPlugin() {

    private var _data: Data? = null
    val data get() = _data!!
    
    private val executor = MainExecutor(this)
    
    private val dataUpdater = DataUpdater(this)

    override fun onEnable() {
        super.onEnable()

        server.logger.log(Level.INFO, "waffle plugin is activated.")

        val mainCommand = getCommand("waffle")
        mainCommand?.setExecutor(this)
        
        G.DATA_PATH = dataFolder.absolutePath + File.separator + G.DATA_PATH
        
        if (File(G.DATA_PATH).exists()) {
            _data = Data.load(G.DATA_PATH)
            data.updateOutlinesAndPoints()
        } else {
            if (!dataFolder.exists())
                dataFolder.mkdirs()
            _data = Data.generate()
            server.logger.log(Level.WARNING, "can not find data file, generating...")
        }

        server.pluginManager.registerEvents(PlayerJoinListener(this), this)
        server.pluginManager.registerEvents(PlayerDeathByWaffleListener(), this)
        server.pluginManager.registerEvents(ChestListener(this, dataUpdater), this)
        server.pluginManager.registerEvents(BlockListener(this, dataUpdater), this)
        server.pluginManager.registerEvents(DimensionChangeListener(this, dataUpdater), this)
        server.pluginManager.registerEvents(AdvancementListener(this), this)

        PlayerPositionDetector(this).runTaskTimer(this, 0, 20)
        
        dataUpdater.runTaskTimer(this, 0, 10)
        
        ParticleCreator(this).runTaskTimer(this, 0, 3)
    }

    override fun onDisable() {
        super.onDisable()

        data.save(G.DATA_PATH)

        server.logger.log(Level.INFO, "area-waffle plugin is de-activated.")
    }

    override fun onCommand(
        sender: CommandSender, 
        command: Command, 
        label: String, 
        args: Array<out String>
    ): Boolean {
        if (command.name != "waffle") return false

        return if (sender !is Player) {
            sender.sendMessage("Only player can execute 'waffle' command.")
            true
        } else if (args.isEmpty()) {
            sender.sendMessage("" + ChatColor.GRAY + "main command of 'waffle' plugin.")
            sender.sendMessage("" + ChatColor.GRAY + "usage: waffle [ start | pause | manage | query | invalidate ]")
            true
        } else {
            executor.execute(sender, args)
        }
    }
    
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        if (command.name != "waffle") return mutableListOf()
        
        return executor.getTabComplete(args)
    }

    fun reset() {
        _data = Data.generate()
    }
    
}