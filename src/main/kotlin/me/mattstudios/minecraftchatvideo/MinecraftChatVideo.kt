package me.mattstudios.minecraftchatvideo

import me.mattstudios.mf.base.CommandManager
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.plugin.java.JavaPlugin

class MinecraftChatVideo : JavaPlugin(), Listener {

    val armorStands = mutableListOf<ArmorStand>()

    override fun onEnable() {
        saveDefaultConfig()

        server.pluginManager.registerEvents(this, this)

        val commandManager = CommandManager(this)
        commandManager.register(PlayCommand(this))
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