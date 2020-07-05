package me.mattstudios.holovid.display;

import me.mattstudios.holovid.Holovid;
import net.minecraft.server.v1_16_R1.ChatBaseComponent;
import net.minecraft.server.v1_16_R1.ChatComponentText;
import net.minecraft.server.v1_16_R1.ChatHexColor;
import net.minecraft.server.v1_16_R1.ChatModifier;
import net.minecraft.server.v1_16_R1.IChatBaseComponent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public final class FileDisplayTask extends DisplayTask {

    private final List<File> files;

    public FileDisplayTask(final Holovid plugin, final boolean repeat, final List<File> files, final int fps) {
        super(plugin, repeat, fps);
        this.files = files;
    }

    @Override
    public int getMaxFrames() {
        return files.size();
    }

    @Override
    protected IChatBaseComponent[] getCurrentFrame() {
        if (frameCounter >= files.size()) return null;

        final File file = files.get(frameCounter);
        return getFrame(file);
    }

    private IChatBaseComponent[] getFrame(final File file) {
        try {
            final BufferedImage image = ImageIO.read(file);
            final IChatBaseComponent[] frame = new IChatBaseComponent[image.getHeight()];
            final int width = image.getWidth();
            final int height = image.getHeight();

            final int[] rgbArray = image.getRGB(0, 0, width, height, null, 0, width);

            for (int y = 0; y < height; y++) {
                final ChatBaseComponent component = new ChatComponentText("");
                ChatComponentText lastComponent = null;
                int lastRgb = 0xFFFFFF;
                for (int x = 0; x < width; x++) {
                    final int rgb = rgbArray[y * width + x] & 0x00FFFFFF;
                    lastComponent = appendComponent(component, rgb, lastRgb, lastComponent);
                    lastRgb = rgb;
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
