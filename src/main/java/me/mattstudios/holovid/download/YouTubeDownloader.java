package me.mattstudios.holovid.download;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.AudioVideoFormat;
import com.github.kiulian.downloader.model.formats.VideoFormat;
import com.github.kiulian.downloader.model.quality.VideoQuality;
import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.display.BufferedDisplayTask;
import me.mattstudios.holovid.utils.ImageUtils;
import me.mattstudios.holovid.utils.Task;
import me.mattstudios.holovid.utils.VideoTitleUtils;
import org.bukkit.configuration.file.YamlConfiguration;
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
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public final class YouTubeDownloader implements VideoDownloader {

    private final YoutubeDownloader downloader = new YoutubeDownloader();
    private final Holovid plugin;
    private Queue<Picture> pictures;
    private boolean grabbingImages;

    public YouTubeDownloader(final Holovid plugin) {
        this.plugin = plugin;
        downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
    }

    public void download(final Player player, final URL videoUrl, final boolean instantPlay) {
        // TODO more website support, abstract parallel tasks out
        // Gets the video ID
        final String id = videoUrl.getQuery().substring(2);

        Task.async(() -> {
            try {
                player.sendMessage("Downloading video...");

                final YoutubeVideo video = downloader.getVideo(id);

                // Gets the video format and audio format
                final List<AudioVideoFormat> videoWithAudioFormats = video.videoWithAudioFormats();
                final List<VideoFormat> videoQuality = video.findVideoWithQuality(VideoQuality.tiny);

                final File outputDir = new File(plugin.getDataFolder(), "saves/" + video.details().title().replaceAll("[^A-Za-z0-9]", ""));

                // Gets the format to use on the download (this one has been the only one to work so far)
                final AudioVideoFormat format = videoWithAudioFormats.get(0);

                // Downloads the video into the videos dir
                File videoFile = VideoTitleUtils.titleToFile(outputDir, video.details(), format);
                if (!videoFile.exists() || !videoFile.isFile()) {
                    // Download if it is not already in the videos folder
                    videoFile = video.download(format, outputDir);
                }

                // Calculates how many frames the video has
                final int fps = videoQuality.get(0).fps();
                final int max = fps * video.details().lengthSeconds();

                // Save data about the video format
                final YamlConfiguration dataConfig = new YamlConfiguration();
                dataConfig.set("fps", fps);
                dataConfig.set("height", plugin.getDisplayHeight());
                dataConfig.set("width", plugin.getDisplayWidth());
                dataConfig.save(new File(outputDir, "data.yml"));

                player.sendMessage(instantPlay ? "Processing and displaying video..." : "Resizing and saving video...");

                // Starts the frame grabber
                final FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile));


                grabbingImages = true;
                pictures = new ArrayBlockingQueue<>(max);
                Task.async(() -> {
                    Picture last = null;
                    for (int i = 0; i < max; i++) {
                        final Picture picture;
                        try {
                            picture = grab.getNativeFrame();
                        } catch (final IOException e) {
                            e.printStackTrace();
                            continue;
                        }

                        if (pictures == null) break;

                        // Write the last non-null picture again in case of failure
                        if (picture != null) {
                            last = picture;
                        }

                        if (instantPlay) {
                            waitForQueueReduction();
                        }

                        pictures.add(last);
                    }

                    grabbingImages = false;
                });

                if (instantPlay) {
                    // Start instant replay slightly delayed
                    plugin.startBufferedTask(2000, max, plugin.getDisplayHeight(), fps);
                }

                // Resize and save images in parallel to the frame grabbing
                for (int frameCount = 0; frameCount < max; frameCount++) {
                    // Wait for frame to be loaded
                    Picture picture = null;
                    do {
                        picture = pictures.poll();
                    } while (picture == null && grabbingImages && pictures != null);

                    if (picture == null) break; // In case a frame errors and grabbing is done

                    if (instantPlay) {
                        addToBufferedDisplay(picture);
                    } else {
                        ImageIO.write(ImageUtils.resize(AWTUtil.toBufferedImage(picture), plugin.getDisplayWidth(), plugin.getDisplayHeight()),
                                "jpg", new File(outputDir.getPath() + "/frame-" + frameCount + ".jpg"));

                        // Debug percent checker
                        if (frameCount % 100 == 0) {
                            player.sendMessage("Complete - " + (frameCount * 100 / max) + "%");
                        }
                    }
                }

                pictures = null;

                if (!instantPlay) {
                    videoFile.delete();
                    player.sendMessage("Load complete!");
                }
            } catch (final YoutubeException | IOException | JCodecException e) {
                player.sendMessage("Error downloading the video!");
                e.printStackTrace();
            }
        });

    }

    @Override
    public void stopCurrentDownloading() {
        pictures = null;
    }

    private void waitForQueueReduction() {
        if (pictures == null) return;

        // The cache shouldn't accumulate too much data
        final BufferedDisplayTask task = (BufferedDisplayTask) plugin.getTask();
        while (task.isQueueFull()) {
            try {
                Thread.sleep(5);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void addToBufferedDisplay(final Picture picture) throws IOException {
        final int width = plugin.getDisplayWidth();
        final int height = plugin.getDisplayHeight();

        final BufferedImage resized = ImageUtils.resize(AWTUtil.toBufferedImage(picture), width, height);
        final int[] rgbArray = resized.getRGB(0, 0, width, height, null, 0, width);

        final int[][] frame = new int[height][width];
        for (int i = 0; i < height; i++) {
            final int[] row = frame[height - i - 1];
            System.arraycopy(rgbArray, i * width, row, 0, width);
        }

        waitForQueueReduction();

        final BufferedDisplayTask task = (BufferedDisplayTask) plugin.getTask();
        // Block until the frame can be placed in the queue
        try {
            task.getFrameQueue().put(frame);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
