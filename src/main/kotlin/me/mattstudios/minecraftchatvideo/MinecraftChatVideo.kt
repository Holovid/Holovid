package me.mattstudios.minecraftchatvideo

import me.mattstudios.mf.base.CommandManager
import me.mattstudios.mf.base.components.TypeResult
import org.apache.commons.validator.routines.UrlValidator
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.plugin.java.JavaPlugin
import java.net.URL

class MinecraftChatVideo : JavaPlugin(), Listener {

    val armorStands = mutableListOf<ArmorStand>()

    override fun onEnable() {
        saveDefaultConfig()

        Tasks.load(this)

        server.pluginManager.registerEvents(this, this)

        val commandManager = CommandManager(this)

        commandManager.parameterHandler.register(URL::class.java) { argument ->
            if (argument == null) TypeResult(argument)
            else if (!UrlValidator.getInstance().isValid(argument.toString())) TypeResult(argument)
            else TypeResult(URL(argument.toString()), argument)
        }

        commandManager.register(PlayCommand(this))
        commandManager.register(LoadCommand(this))
    }

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