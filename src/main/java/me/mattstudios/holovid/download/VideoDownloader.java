package me.mattstudios.holovid.download;

import me.mattstudios.holovid.Holovid;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public abstract class VideoDownloader {

    protected final Holovid plugin;

    protected VideoDownloader(final Holovid plugin) {
        this.plugin = plugin;
    }

    /**
     * Should ALWAYS be called async!
     *
     * @param player   player to request the download
     * @param videoUrl video url
     */
    public abstract void download(Player player, URL videoUrl, final boolean disableinterlacing);


    protected void saveDataAndPlay(final Player player, final File videoFile, final URL videoUrl, final File outputDir, final boolean prepareAudio,
                                   final int frames, final int fps, final boolean disableInterlacing) throws IOException {
        // Save data about the video format
        final YamlConfiguration dataConfig = new YamlConfiguration();
        dataConfig.set("fps", fps);
        dataConfig.set("height", plugin.getDisplayHeight());
        dataConfig.set("width", plugin.getDisplayWidth());
        dataConfig.set("frames", frames);
        dataConfig.set("video-url", videoUrl.toString());
        dataConfig.save(new File(outputDir, "data.yml"));

        // Play the video!
        plugin.getVideoProcessor().play(player, videoFile, videoUrl, prepareAudio, plugin.getDisplayHeight(), plugin.getDisplayWidth(), frames, fps, disableInterlacing);
    }

}
