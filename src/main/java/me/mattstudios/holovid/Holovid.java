package me.mattstudios.holovid;

import me.mattstudios.mf.base.CommandManager;
import me.mattstudios.mf.base.components.TypeResult;
import me.mattstudios.holovid.command.DownloadCommand;
import me.mattstudios.holovid.command.PlayCommand;
import me.mattstudios.holovid.utils.Task;
import org.apache.commons.validator.routines.UrlValidator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class Holovid extends JavaPlugin implements Listener {

    private CommandManager commandManager;

    // TODO this two need to get their own place
    private final List<ArmorStand> armorStands = new ArrayList<>();
    private final List<List<String>> temporaryFrames = new ArrayList<>();

    @Override
    public void onEnable() {
        //saveDefaultConfig();

        // Loads the tasks util
        Task.init(this);

        commandManager = new CommandManager(this);

        getServer().getPluginManager().registerEvents(this, this);
        registerCommands();
    }

    /**
     * Registers anything related to commands
     * TODO should probably move this away
     */
    private void registerCommands() {
        // Registers URL parameter fro commands
        commandManager.getParameterHandler().register(URL.class, argument -> {
            if (argument == null) return new TypeResult(null);
            if (!UrlValidator.getInstance().isValid(argument.toString())) return new TypeResult(argument);
            try {
                return new TypeResult(new URL(argument.toString()), argument);
            } catch (MalformedURLException e) {
                return new TypeResult(null);
            }
        });

        // Registers the videos completion
        commandManager.getCompletionHandler().register("#videos", input -> {
            final File[] files = new File(getDataFolder(), "saves").listFiles();
            if (files == null) return Collections.emptyList();
            return Arrays.asList(files).parallelStream().filter(File::isDirectory).map(File::getName).collect(Collectors.toList());
        });

        // Registers all the commands
        Arrays.asList(
                new DownloadCommand(this),
                new PlayCommand(this)
        ).forEach(commandManager::register);

    }

    // TODO remake this, this is temporary
    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Block block = event.getBlock();
        if (block.getType() != Material.REDSTONE_BLOCK) return;

        double counter = 0.0;

        for (int i = 0; i < 72; i++) {
            final ArmorStand armorStand = block.getWorld().spawn(block.getLocation().clone().add(0.0, counter, 0.0), ArmorStand.class, it -> {
                it.setCustomName("█");
                it.setCustomNameVisible(true);
                it.setGravity(false);
                it.setSmall(true);
                it.setMarker(true);
                it.setVisible(false);
            });

            counter += 0.225;

            armorStands.add(armorStand);
        }
    }

    public List<ArmorStand> getArmorStands() {
        return armorStands;
    }

}
