package me.mattstudios.holovid;

import me.mattstudios.holovid.hologram.HologramLine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public final class DisplayTask implements Runnable {

    private final Holovid plugin;
    private final List<File> files;
    private final long frameDelay;
    private long lastDisplayed;
    private boolean running = true;
    private int frameCounter;

    public DisplayTask(final Holovid plugin, final List<File> files, final int fps) {
        this.plugin = plugin;
        this.files = files;
        this.frameDelay = 1000 / fps;
    }

    @Override
    public void run() {
        while (running) {
            final String[] frame = getFrame(files.get(frameCounter));
            final List<HologramLine> lines = plugin.getHologram().getLines();

            // Frame delay
            final long timeSinceLast = System.currentTimeMillis() - lastDisplayed;
            if (timeSinceLast < frameDelay) {
                try {
                    Thread.sleep(frameDelay - timeSinceLast);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Set hologram lines
            for (int i = 0; i < frame.length; i++) {
                final String line = frame[i];
                final HologramLine hologramLine = lines.get(i);
                hologramLine.updateText(line);
            }

            if (++frameCounter == files.size() - 1) {
                frameCounter = 0;
            }

            lastDisplayed = System.currentTimeMillis();
        }
    }

    public void setRunning(final boolean running) {
        this.running = running;
    }

    private String[] getFrame(final File file) {

        try {
            final BufferedImage image = ImageIO.read(file);
            final String[] frame = new String[image.getHeight()];

            for (int i = 0; i < image.getHeight(); i++) {

                final StringBuilder builder = new StringBuilder("{\"text\":\"\",\"extra\":[");

                for (int j = 0; j < image.getWidth(); j++) {
                    final int color = image.getRGB(j, i);
                    builder.append("{\"color\":\"#").append(String.format("%06x", color & 0x00FFFFFF)).append("\",\"text\":\"█\"},");
                }

                builder.deleteCharAt(builder.length() - 1).append("]}");
                // Holograms are from bottom to top
                frame[image.getHeight() - i - 1] = builder.toString();
            }

            return frame;

        } catch (final IOException e) {
            e.printStackTrace();
        }

        return new String[0];
    }
}
