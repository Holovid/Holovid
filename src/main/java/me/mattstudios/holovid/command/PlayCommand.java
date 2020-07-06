package me.mattstudios.holovid.command;

import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.utils.Task;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Optional;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

@Command("holovid")
public final class PlayCommand extends CommandBase {

    private final Holovid plugin;

    public PlayCommand(final Holovid plugin) {
        this.plugin = plugin;
    }

    @SubCommand("play")
    @Completion("#videos")
    public void play(final Player player, final String folder, @Optional final boolean disableInterlace) {
        if (plugin.getHologram() == null) {
            player.sendMessage("Use /holovid spawnscreen to spawn the armor stands first.");
            return;
        }

        final File saveFolder = new File(plugin.getDataFolder(), "saves/" + folder);
        final File[] folderFiles = saveFolder.listFiles();
        if (folderFiles == null) return;

        final File videoFile = new File(saveFolder, "video.mp4");
        if (!videoFile.exists() || !videoFile.isFile()) {
            player.sendMessage("Could not find video.mp4 file!");
            return;
        }

        final File dataFile = new File(saveFolder, "data.yml");
        if (!dataFile.exists()) {
            player.sendMessage("Could not find data.yml file!");
            return;
        }

        final YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        final URL url;
        try {
            url = new URL(dataConfig.getString("video-url"));
        } catch (final MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        plugin.stopTask();

        final int fps = dataConfig.getInt("fps");
        final int frames = dataConfig.getInt("frames");
        final int height = dataConfig.getInt("height");
        final int width = dataConfig.getInt("width");
        Task.async(() -> plugin.getVideoProcessor().play(player, videoFile, url, height, width, frames, fps, disableInterlace));
    }

}
