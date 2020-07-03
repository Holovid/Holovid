package me.mattstudios.holovid.utils;

import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.IOException;

public final class ImageUtils {

    private ImageUtils() {}

    public static BufferedImage resize(final BufferedImage image, final int width, final int height) throws IOException {
        return Thumbnails.of(image).size(width, height).asBufferedImage();
    }

}
