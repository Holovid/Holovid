package me.mattstudios.holovid.utils;

import com.github.kiulian.downloader.model.VideoDetails;
import com.github.kiulian.downloader.model.formats.Format;

import java.io.File;

/**
 * See package private Utils class in com.github.kiulian.downloader.model
 */
public final class VideoTitleUtils {

    private static final char[] ILLEGAL_FILENAME_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};

    public static File titleToFile(final File parentDirectory, final VideoDetails details, final Format format) {
        final String fileName = details.title() + "." + format.extension().value();
        return new File(parentDirectory, removeIllegalChars(fileName));
    }

    private static String removeIllegalChars(String filename) {
        for (final char c : ILLEGAL_FILENAME_CHARACTERS) {
            filename = filename.replace(c, '_');
        }
        return filename;
    }

}
