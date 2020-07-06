package me.mattstudios.holovid.download;

import org.bukkit.entity.Player;

import java.net.URL;

public interface VideoDownloader {

    /**
     * Should ALWAYS be called async!
     *
     * @param player   player to request the download
     * @param videoUrl video url
     */
    void download(Player player, URL videoUrl, final boolean disableinterlacing);

}
