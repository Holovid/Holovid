package me.mattstudios.holovid.command;

import me.mattstudios.holovid.Holovid;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.entity.Player;

import java.net.URL;

@Command("holovid")
public final class DownloadCommand extends CommandBase {

    private final Holovid plugin;

    public DownloadCommand(final Holovid plugin) {
        this.plugin = plugin;
    }

    @SubCommand("download")
    public void download(final Player player, final URL videoUrl, final boolean instantPlay) {
        if (instantPlay) {
            plugin.stopTask();
        }

        plugin.getVideoDownloader().download(player, videoUrl, instantPlay);
    }

}
