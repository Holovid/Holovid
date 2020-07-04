package me.mattstudios.holovid;

import me.mattstudios.holovid.hologram.HologramLine;
import net.minecraft.server.v1_16_R1.*;

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
            final IChatBaseComponent[] frame = getFrame(files.get(frameCounter));
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
                final IChatBaseComponent line = frame[i];
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

    private IChatBaseComponent[] getFrame(final File file) {

        try {
            final BufferedImage image = ImageIO.read(file);
            final IChatBaseComponent[] frame = new IChatBaseComponent[image.getHeight()];
            final int width = image.getWidth();
            final int height = image.getHeight();

            final int[] rgbArray = image.getRGB(0, 0, width, height, null, 0, width);

            for (int y = 0; y < height; y++) {
                ChatBaseComponent component = new ChatComponentText("");
                for (int x = 0; x < width; x++) {
                    ChatComponentText text = new ChatComponentText("█");
                    text.setChatModifier(ChatModifier.b.setColor(ChatHexColor.a(rgbArray[y * width + x] & 0x00FFFFFF)));
                    component.addSibling(text);
                }
                frame[image.getHeight() - y - 1] = component;
            }

            return frame;
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return new IChatBaseComponent[0];
    }
}
