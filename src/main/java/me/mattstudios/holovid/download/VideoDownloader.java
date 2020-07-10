package me.mattstudios.holovid.download;

import com.google.common.base.Preconditions;
import me.mattstudios.holovid.Holovid;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public abstract class VideoDownloader {

    protected final Holovid plugin;
    protected boolean cancelBeforeDisplay;

    protected VideoDownloader(final Holovid plugin) {
        this.plugin = plugin;
    }

    /**
     * Should ALWAYS be called async!
     *
     * @param player   player to request the download
     * @param videoUrl video url
     */
    public final void download(final Player player, final URL videoUrl, final boolean interlace) {
        Preconditions.checkArgument(!Bukkit.isPrimaryThread());
        player.sendMessage("Downloading video...");
        cancelBeforeDisplay = false;
        try {
            download0(player, videoUrl, interlace);
        } finally {
            cancelBeforeDisplay = false;
            plugin.resetCurrentDownloader();
        }
    }

    protected abstract void download0(Player player, URL videoUrl, boolean disableinterlacing);

    protected void saveDataAndPlay(final Player player, final File videoFile, final URL videoUrl, final File outputDir, final boolean prepareAudio,
                                   final int frames, final int fps, final boolean interlace) throws IOException {
        // Save data about the video format
        final YamlConfiguration dataConfig = new YamlConfiguration();
        dataConfig.set("fps", fps);
        dataConfig.set("height", plugin.getDisplayHeight());
        dataConfig.set("width", plugin.getDisplayWidth());
        dataConfig.set("frames", frames);
        dataConfig.set("video-url", videoUrl.toString());
        dataConfig.save(new File(outputDir, "data.yml"));

        if (cancelBeforeDisplay) {
            player.sendMessage("The download/display has been cancelled!");
            cancelBeforeDisplay = false;
            return;
        }

        plugin.resetCurrentDownloader();

        // Play the video!
        plugin.getVideoProcessor().play(player, videoFile, videoUrl, prepareAudio, plugin.getDisplayHeight(), plugin.getDisplayWidth(), frames, fps, interlace);
    }

    protected File getOutputDirForTitle(final String title) {
        return new File(plugin.getDataFolder(), "saves/" + title.replaceAll("[^A-Za-z0-9]", ""));
    }

    public void cancelBeforeDisplay() {
        cancelBeforeDisplay = true;
    }
}
