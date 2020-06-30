package me.mattstudios.minecraftchatvideo

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

/**
 * @author Matt
 */

object Tasks {
    private lateinit var plugin: Plugin

    fun load(plugin: Plugin) {
        this.plugin = plugin
    }

    fun async(task: () -> Unit) = Bukkit.getScheduler().runTaskAsynchronously(plugin, task)


}