package me.mattstudios.holovid.command;

import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.hologram.HologramLine;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Command("holovid")
public final class PlayCommand extends CommandBase {

    private final Holovid plugin;

    public PlayCommand(final Holovid plugin) {
        this.plugin = plugin;
    }

    @SubCommand("play")
    @Completion("#videos")
    public void play(final Player player, final String folder) {
        // Repeating here for now, yes, won't be final
        final File[] folderFiles = new File(plugin.getDataFolder(), "saves/" + folder).listFiles();
        if (folderFiles == null) return;

        final List<File> files = Arrays.asList(folderFiles)
                .parallelStream()
                .filter(file -> FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("jpg"))
                .sorted()
                .collect(Collectors.toList());

        new BukkitRunnable() {

            private int frameCounter;

            @Override
            public void run() {
                final List<String> frame = getFrame(files.get(frameCounter));
                final List<HologramLine> lines = plugin.getHologram().getLines();

                for (int i = 0; i < frame.size(); i++) {
                    // Uses spread operator cuz spigot's varargs
                    final String line = frame.get(frame.size() - i - 1);

                    final HologramLine hologramLine = lines.get(i);
                    hologramLine.updateText(line);
                }

                if (++frameCounter == files.size() - 1) {
                    frameCounter = 0;
                }

            }

        }.runTaskTimerAsynchronously(plugin, 0L, 1L);

    }

    private List<String> getFrame(final File file) {

        try {
            final BufferedImage image = ImageIO.read(file);
            final List<String> frame = new ArrayList<>(image.getHeight());

            for (int i = 0; i < image.getHeight(); i++) {

                final StringBuilder builder = new StringBuilder("{\"text\":\"\",\"extra\":[");

                for (int j = 0; j < image.getWidth(); j++) {
                    final int color = image.getRGB(j, i);
                    builder.append("{\"color\":\"#").append(String.format("%06x", color & 0x00FFFFFF)).append("\",\"text\":\"█\"},");
                }

                builder.deleteCharAt(builder.length() - 1).append("]}");
                frame.add(builder.toString());
            }

            return frame;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

}
