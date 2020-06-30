package me.mattstudios.minecraftchatvideo

import me.mattstudios.mf.base.CommandManager
import org.bukkit.plugin.java.JavaPlugin

class MinecraftChatVideo : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()

        val commandManager = CommandManager(this)
        commandManager.register(PlayCommand(this))
    }

}