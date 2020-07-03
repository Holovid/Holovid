package me.mattstudios.minecraftchatvideo.func

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author Matt
 */

object Tasks {
    private lateinit var plugin: Plugin

    /**
     * Loads the plugin instance
     */
    fun load(plugin: Plugin) {
        Tasks.plugin = plugin
    }

    /**
     * Runs the task async
     */
    fun async(task: () -> Unit) = Bukkit.getScheduler().runTaskAsynchronously(plugin, task)

}