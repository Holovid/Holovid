package me.mattstudios.holovid.listener;

import me.mattstudios.holovid.Holovid;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public final class ResourcePackStatusListener implements Listener {

    private final Holovid plugin;

    public ResourcePackStatusListener(final Holovid plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void resourcepackStatus(final PlayerResourcePackStatusEvent event) {
        final Player player = event.getPlayer();
        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED:
            case DECLINED:
            case FAILED_DOWNLOAD:
                // Contains check is done there - remove awaiting status of the player
                plugin.getAudioProcessor().removeAwaiting(player);
                break;
        }
    }

    @EventHandler
    public void playerQuit(final PlayerQuitEvent event) {
        plugin.getAudioProcessor().removeAwaiting(event.getPlayer());
    }
}
