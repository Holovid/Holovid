package me.mattstudios.holovid.download;

import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.display.BufferedDisplayTask;
import me.mattstudios.holovid.utils.ImageUtils;
import me.mattstudios.holovid.utils.Task;
import org.bukkit.entity.Player;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class VideoProcessor {

    private final Lock threadLock = new ReentrantLock();
    private final Holovid plugin;
    private ArrayBlockingQueue<Picture> pictures;
    private Thread grabbingThread;
    private Thread frameProcessingThread;

    public VideoProcessor(final Holovid plugin) {
        this.plugin = plugin;
    }

    /**
     * Plays the video from the file.
     * Should ALWAYS be called async!
     */
    public void play(final Player player, final File videoFile, final URL videoUrl,
                     final int height, final int width, final int frames, final int fps, final boolean disableInterlacing) {
        //TODO use videourl to request a resourcepack with sound
        try {
            player.sendMessage("Processing and displaying video...");

            // Starts the frame grabber
            final FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile));

            threadLock.lock();
            frameProcessingThread = Thread.currentThread();
            // Limit to a few seconds of video if buffered
            pictures = new ArrayBlockingQueue<>(Holovid.PRE_RENDER_SECONDS * fps);
            threadLock.unlock();

            Task.async(() -> {
                threadLock.lock();
                grabbingThread = Thread.currentThread();
                threadLock.unlock();

                Picture last = null;
                for (int i = 0; i < frames; i++) {
                    final Picture picture;
                    try {
                        picture = grab.getNativeFrame();
                    } catch (final IOException e) {
                        e.printStackTrace();
                        // Interrupt threads on error
                        stopCurrentTask();
                        return;
                    }

                    // Write the last non-null picture again in case of failure
                    if (picture != null) {
                        last = picture;
                    }

                    if (Thread.interrupted()) return;

                    // Block if there already are a lot of pre-buffered frames
                    try {
                        pictures.put(last);
                    } catch (final InterruptedException e) {
                        return;
                    }
                }

                threadLock.lock();
                grabbingThread = null;
                threadLock.unlock();
            });

            // Start instant replay slightly delayed
            plugin.startBufferedTask(2000, frames, height, fps, !disableInterlacing);

            // Resize and save images in parallel to the frame grabbing
            for (int frameCount = 0; frameCount < frames; frameCount++) {
                if (Thread.interrupted()) return;

                // Wait for frame to be loaded
                final Picture picture = pictures.take();
                addToBufferedDisplay(picture, height, width);
            }

            threadLock.lock();
            pictures = null;
            frameProcessingThread = null;
            threadLock.unlock();
        } catch (final IOException | JCodecException e) {
            player.sendMessage("Error processing the video!");
            e.printStackTrace();
        } catch (final InterruptedException e) {
            // Exit
        }
    }

    public void stopCurrentTask() {
        threadLock.lock();
        if (grabbingThread != null) {
            grabbingThread.interrupt();
        }
        if (frameProcessingThread != null) {
            frameProcessingThread.interrupt();
        }
        if (pictures != null) {
            pictures.clear();
            pictures = null;
        }
        threadLock.unlock();
    }

    private void addToBufferedDisplay(final Picture picture, final int height, final int width) throws IOException, InterruptedException {
        final BufferedImage resized = ImageUtils.resize(AWTUtil.toBufferedImage(picture), width, height);
        final int[] rgbArray = resized.getRGB(0, 0, width, height, null, 0, width);

        final int[][] frame = new int[height][width];
        for (int i = 0; i < height; i++) {
            final int[] row = frame[height - i - 1];
            System.arraycopy(rgbArray, i * width, row, 0, width);
        }

        final BufferedDisplayTask task = (BufferedDisplayTask) plugin.getTask();
        if (task == null) return;

        // Block until the frame can be placed in the queue
        task.getFrameQueue().put(frame);
    }
}
