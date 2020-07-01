package me.mattstudios.minecraftchatvideo

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

/**
 * @author Matt
 */

object Tasks {
    private lateinit var plugin: Plugin

    /**
     * Loads the plugin instance
     */
    fun load(plugin: Plugin) {
        this.plugin = plugin
    }

    /**
     * Runs the task async
     */
    fun async(task: () -> Unit) = Bukkit.getScheduler().runTaskAsynchronously(plugin, task)


}