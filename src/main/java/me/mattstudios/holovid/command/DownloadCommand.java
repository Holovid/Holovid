package me.mattstudios.holovid.command;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.AudioVideoFormat;
import com.github.kiulian.downloader.model.formats.VideoFormat;
import com.github.kiulian.downloader.model.quality.VideoQuality;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.utils.ImageUtils;
import me.mattstudios.holovid.utils.Task;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.bukkit.entity.Player;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
                final int max = videoQuality.get(0).fps() * video.details().lengthSeconds();

                // Starts the frame grabber
                final FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile));

                for (int i = 0; i < max; i++) {
                    final Picture frame = grab.getNativeFrame();

                    if (frame == null) continue;

                    ImageIO.write(ImageUtils.resize(AWTUtil.toBufferedImage(frame), 128, 72), "jpg", new File(outputDir.getPath() + "/frame-" + i + ".jpg"));

                    // Debug percent checker
                    if (i % 100 == 0) player.sendMessage("Complete - " + i * 100 / max);
                }

                videoFile.delete();

                player.sendMessage("Load complete!");
            } catch (YoutubeException | IOException | JCodecException e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * TODO needs work, this is temporary
     */
    private void loadFrames(final String folder) throws IOException {
        // Gets all the files in the images folder
        final File[] folderFiles = new File(plugin.getDataFolder(), folder).listFiles();
        if (folderFiles == null) return;

        final List<File> files = Arrays.asList(folderFiles)
                                       .parallelStream()
                                       .filter(file -> FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("jpg"))
                                       .sorted()
                                       .collect(Collectors.toList());

        // Cycles through the files
        for (final File file : files) {
            // Loads the image and ignores non image files
            final BufferedImage image = ImageIO.read(file);
            if (image == null) continue;

            final List<String> frame = new ArrayList<>();

            // Cycles through the image pixels
            for (int i = 0; i < image.getHeight(); i++) {

                final StringBuilder builder = new StringBuilder();

                for (int j = 0; j < image.getWidth(); j++) {
                    final int color = image.getRGB(j, i);
                    builder.append(ChatColor.of("#" + Integer.toHexString(color).substring(2)));
                    builder.append("█");
                }

                frame.add(builder.toString());
            }

            // Would load here, but will comment it out for now
            //plugin.temporaryFrames.add(frame);
        }
    }

}
