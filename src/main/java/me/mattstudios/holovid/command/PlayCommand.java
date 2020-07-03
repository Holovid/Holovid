package me.mattstudios.holovid.command;

import me.mattstudios.holovid.Holovid;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
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
        if (plugin.getHologram() == null) {
            player.sendMessage("Use /holovid spawnscreen to spawn the armor stands first.");
            return;
        }

        // Repeating here for now, yes, won't be final
        final File saveFolder = new File(plugin.getDataFolder(), "saves/" + folder);
        final File[] folderFiles = saveFolder.listFiles();
        if (folderFiles == null) return;

        final File dataFile = new File(saveFolder, "data.yml");
        if (!dataFile.exists()) {
            player.sendMessage("No data.yml file found - redownload the video to generate it!");
            return;
        }

        final YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        final List<File> files = Arrays.asList(folderFiles)
                .parallelStream()
                .filter(file -> FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("jpg"))
                .sorted(Comparator.comparingInt(value -> Integer.parseInt(value.getName().substring(6).split("\\.")[0])))
                .collect(Collectors.toList());
        plugin.startTask(files, dataConfig.getInt("height"), dataConfig.getInt("fps", 30));
        player.sendMessage("Video task started!");
    }

}
