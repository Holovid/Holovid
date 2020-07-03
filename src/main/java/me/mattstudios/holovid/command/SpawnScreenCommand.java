package me.mattstudios.holovid.command;

import me.mattstudios.holovid.Holovid;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.entity.Player;

@Command("holovid")
public final class SpawnScreenCommand extends CommandBase {

    private final Holovid plugin;

    public SpawnScreenCommand(final Holovid plugin) {
        this.plugin = plugin;
    }

    @SubCommand("spawnscreen")
    public void spawnScreen(final Player player) {
        plugin.spawnHologram(player.getLocation());
        player.sendMessage("Spawned!");
    }

}
