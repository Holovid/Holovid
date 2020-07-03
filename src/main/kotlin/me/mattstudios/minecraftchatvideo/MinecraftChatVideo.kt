package me.mattstudios.minecraftchatvideo

import me.mattstudios.mf.base.CommandManager
import me.mattstudios.mf.base.components.TypeResult
import me.mattstudios.minecraftchatvideo.command.DownloadCommand
import me.mattstudios.minecraftchatvideo.command.PlayCommand
import me.mattstudios.minecraftchatvideo.func.Tasks
import org.apache.commons.validator.routines.UrlValidator
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.net.URL

class MinecraftChatVideo : JavaPlugin(), Listener {

    private lateinit var commandManager: CommandManager

    // TODO this two need to get their own place
    val armorStands = mutableListOf<ArmorStand>()
    val temporaryFrames = mutableListOf<List<String>>()

    override fun onEnable() {
        saveDefaultConfig()

        // Loads the tasks util
        Tasks.load(this)

        commandManager = CommandManager(this)

        server.pluginManager.registerEvents(this, this)
        registerCommands()
    }

    /**
     * Registers anything related to commands
     * TODO should probably move this away
     */
    private fun registerCommands() {
        // Registers URL parameter fro commands
        commandManager.parameterHandler.register(URL::class.java) { argument ->
            if (argument == null) return@register TypeResult(argument)
            if (!UrlValidator.getInstance().isValid(argument.toString())) return@register TypeResult(argument)
            return@register TypeResult(URL(argument.toString()), argument)
        }

        // Registers the videos completion
        commandManager.completionHandler.register("#videos") {
            val files = File(dataFolder, "saves").listFiles() ?: return@register emptyList()
            return@register files.filter { it.isDirectory }.map { it.name }
        }

        // Registers all the commands
        listOf(
                DownloadCommand(this),
                PlayCommand(this)
        ).forEach(commandManager::register)

    }

    // TODO make a better way to start it
    @EventHandler
    fun BlockPlaceEvent.onBlockPlace() {
        if (block.type != Material.REDSTONE_BLOCK) return

        var counter = 0.0

        repeat(72) {
            val armorStand = block.world.spawn(block.location.clone().add(0.0, counter, 0.0), ArmorStand::class.java) {
                it.customName = "█"
                it.isCustomNameVisible = true
                it.setGravity(false)
                it.isSmall = true
                it.isMarker = true
                it.isVisible = false
            }

            counter += 0.225

            armorStands.add(armorStand)
        }
    }

}