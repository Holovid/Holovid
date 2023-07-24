package me.mattstudios.holovid.download;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;
import com.github.kiulian.downloader.model.videos.formats.VideoWithAudioFormat;
import me.mattstudios.holovid.Holovid;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

public final class YouTubeDownloader extends VideoDownloader {

    private final YoutubeDownloader downloader = new YoutubeDownloader();

    public YouTubeDownloader(final Holovid plugin) {
        super(plugin);
        downloader.getConfig().setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
    }

    public void download0(final Player player, final URL videoUrl, final boolean interlace) {
        // Gets the video ID
        final String id = videoUrl.getQuery().substring(2);
        try {
            final RequestVideoInfo reqest = new RequestVideoInfo(id);
            final Response<VideoInfo> response = downloader.getVideoInfo(reqest);
            final VideoInfo videoInfo = response.data();

            final File outputDir = getOutputDirForTitle(videoInfo.details().title());
            final File videoFile = new File(outputDir, "video.mp4");
            if (videoFile.exists()) {
                // Play video from saved file
                final File dataFile = new File(outputDir, "data.yml");
                if (!dataFile.exists()) {
                    player.sendMessage("Could not find data.yml file in " + outputDir.getName() + " directory!");
                    return;
                }

                plugin.playVideoFromSave(player, videoFile, dataFile, interlace);
                return;
            }

            // Gets the format to use on the download (this one has been the only one to work so far)
            final List<VideoWithAudioFormat> videoWithAudioFormats = videoInfo.videoWithAudioFormats();
            //This is set to 1 because YouTube's lowest quality uses some weird codec which Jcodec does not understand
            final VideoWithAudioFormat format = videoWithAudioFormats.get(1);

            // Downloads the video into the videos dir
            final RequestVideoFileDownload download = new RequestVideoFileDownload(format)
                    .saveTo(outputDir);

            final Response<File> downloadedVideo = downloader.downloadVideoFile(download);


            // Rename for easier access
            Files.move(downloadedVideo.data().toPath(), videoFile.toPath());

            // Calculates how many frames the video has
            final List<VideoFormat> videoQuality = videoInfo.videoFormats();
            final int fps = videoQuality.get(1).fps();
            final int frames = fps * videoInfo.details().lengthSeconds();
            final boolean prepareAudio = frames / fps < Holovid.MAX_SECONDS_FOR_AUDIO;
            saveDataAndPlay(player, videoFile, videoUrl, outputDir, prepareAudio, frames, fps, interlace);
        } catch (final IOException e) {
            player.sendMessage("Error downloading the video!");
            e.printStackTrace();
        }
    }
}
