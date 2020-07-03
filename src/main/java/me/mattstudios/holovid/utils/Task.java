package me.mattstudios.holovid.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class Task {

    private Task() {}

    private static Plugin plugin;

    /**
     * Used to assign the plugin value
     *
     * @param plugin The plugin
     */
    public static void init(final Plugin plugin) {
        Task.plugin = plugin;
    }

    /**
     * Better syntax for running task async
     *
     * @param task The task
     * @return The bukkit task created
     */
    public static BukkitTask async(final Runnable task) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    /**
     * Better syntax for running task later
     *
     * @param delay The delay
     * @param task  The task
     * @return The bukkit task created
     */
    public static BukkitTask later(final long delay, final Runnable task) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    /**
     * Better syntax for running task later async
     *
     * @param delay The delay
     * @param task  The task
     * @return The bukkit task created
     */
    public static BukkitTask laterAsync(final long delay, final Runnable task) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
    }

    /**
     * Better syntax for running task timers
     *
     * @param period The period
     * @param delay  The delay
     * @param task   The task
     * @return The bukkit task created
     */
    public static BukkitTask timer(final long period, final Long delay, final Runnable task) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    /**
     * Better syntax for running task timers
     *
     * @param period The period
     * @param task   The task
     * @return The bukkit task created
     */
    public static BukkitTask timer(final long period, final Runnable task) {
        return timer(period, 0L, task);
    }

    /**
     * Better syntax for running task timers async
     *
     * @param period The period
     * @param delay  The delay
     * @param task   The task
     * @return The bukkit task created
     */
    public static BukkitTask asyncTimer(final long period, final Long delay, final Runnable task) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
    }

    /**
     * Better syntax for running task timers async
     *
     * @param period The period
     * @param task   The task
     * @return The bukkit task created
     */
    public static BukkitTask asyncTimer(final long period, final Runnable task) {
        return asyncTimer(period, 0L, task);
    }

}
