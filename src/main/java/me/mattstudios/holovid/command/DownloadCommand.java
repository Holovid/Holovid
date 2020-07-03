package me.mattstudios.holovid.command;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.AudioVideoFormat;
import com.github.kiulian.downloader.model.formats.VideoFormat;
import com.github.kiulian.downloader.model.quality.VideoQuality;
import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.utils.ImageUtils;
import me.mattstudios.holovid.utils.Task;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Command("holovid")
public final class DownloadCommand extends CommandBase {

    private final Holovid plugin;
    private final YoutubeDownloader downloader = new YoutubeDownloader();

    public DownloadCommand(final Holovid plugin) {
        this.plugin = plugin;

        downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
    }

    @SubCommand("download")
    public void download(final Player player, final URL videoUrl) {

        // TODO more website support
        // Gets the video ID
        final String id = videoUrl.getQuery().substring(2);

        Task.async(() -> {
            try {

                player.sendMessage("Downloading video...");

                final YoutubeVideo video = downloader.getVideo(id);

                // Gets the video format and audio format
                final List<AudioVideoFormat> videoWithAudioFormats = video.videoWithAudioFormats();
                final List<VideoFormat> videoQuality = video.findVideoWithQuality(VideoQuality.tiny);

                final File outputDir = new File(plugin.getDataFolder(), "saves/" + video.details().title().replace(" ", ""));

                // Gets the format to use on the download (this one has been the only one to work so far)
                final AudioVideoFormat format = videoWithAudioFormats.get(0);

                // Downloads the video into the videos dir
                final File videoFile = video.download(format, outputDir);

                player.sendMessage("Resizing and saving video...");

                // Calculates how many frames the video has
                final int fps = videoQuality.get(0).fps();
                final int max = fps * video.details().lengthSeconds();

                // Starts the frame grabber
                final FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile));

                for (int i = 0; i < max; i++) {
                    final Picture frame = grab.getNativeFrame();

                    if (frame == null) continue;

                    //TODO parallelize grabbing the image vs. resizing/saving
                    ImageIO.write(ImageUtils.resize(AWTUtil.toBufferedImage(frame), plugin.getDisplayWidth(), plugin.getDisplayHeight()),
                            "jpg", new File(outputDir.getPath() + "/frame-" + i + ".jpg"));

                    // Debug percent checker
                    if (i % 100 == 0) player.sendMessage("Complete - " + (i * 100 / max) + "%");
                }

                final YamlConfiguration dataConfig = new YamlConfiguration();
                dataConfig.set("fps", fps);
                dataConfig.set("height", plugin.getDisplayHeight());
                dataConfig.set("width", plugin.getDisplayWidth());
                dataConfig.save(new File(outputDir, "data.yml"));

                videoFile.delete();

                player.sendMessage("Load complete!");
            } catch (final YoutubeException | IOException | JCodecException e) {
                player.sendMessage("Error downloading the video!");
                e.printStackTrace();
            }
        });

    }

}
