package me.mattstudios.holovid.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public final class HologramListener implements Listener {

    private final Holovid plugin;

    public HologramListener(final Holovid plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        handleTracking(player, player.getLocation());
    }

    @EventHandler
    public void playerQuit(final PlayerQuitEvent event) {
        final Hologram hologram = plugin.getHologram();
        if (hologram != null) {
            hologram.getViewers().remove(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerMove(final PlayerMoveEvent event) {
        handleTracking(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerTeleport(final PlayerTeleportEvent event) {
        handleTracking(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerWorldChange(final PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        final Hologram hologram = plugin.getHologram();
        if (hologram != null) {
            hologram.getViewers().remove(player);
        }

        handleTracking(player, player.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerRespawn(final PlayerPostRespawnEvent event) {
        final Player player = event.getPlayer();
        final Hologram hologram = plugin.getHologram();
        if (hologram != null) {
            hologram.getViewers().remove(player);
        }

        handleTracking(player, event.getRespawnedLocation());
    }

    private void handleTracking(final Player player, final Location location) {
        if (plugin.getHologram() != null) {
            plugin.getHologram().handleTracking(player, location);
        }
    }

}
