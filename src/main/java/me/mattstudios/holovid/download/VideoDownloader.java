package me.mattstudios.holovid.download;

import org.bukkit.entity.Player;

import java.net.URL;

public interface VideoDownloader {

    /**
     * @param player      player to request the download
     * @param videoUrl    video url
     * @param instantPlay whether the video should be instantly buffered or saved to disk first
     */
    void download(Player player, URL videoUrl, boolean instantPlay, final boolean disableinterlacing);

    void stopCurrentDownloading();
}
