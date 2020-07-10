package me.mattstudios.holovid.command;

import me.mattstudios.holovid.Holovid;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.entity.Player;

@Command("holovid")
public final class StopCommand extends CommandBase {

    private final Holovid plugin;

    public StopCommand(final Holovid plugin) {
        this.plugin = plugin;
    }

    @SubCommand("stop")
    public void stop(final Player player) {
        if (plugin.stopDownload()) {
            player.sendMessage("The current display will be cancelled as soon as possible - you will receive another message when that happens.");
            return;
        }

        if (!plugin.stopDisplayTask()) {
            player.sendMessage("There is no video playing at the moment.");
            return;
        }

        player.sendMessage("Stopped the display.");
    }

}
