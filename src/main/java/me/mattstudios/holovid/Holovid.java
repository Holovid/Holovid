package me.mattstudios.holovid;

import me.mattstudios.holovid.command.DownloadCommand;
import me.mattstudios.holovid.command.PlayCommand;
import me.mattstudios.holovid.command.SpawnScreenCommand;
import me.mattstudios.holovid.hologram.Hologram;
import me.mattstudios.holovid.listener.HologramListener;
import me.mattstudios.holovid.utils.Task;
import me.mattstudios.mf.base.CommandManager;
import me.mattstudios.mf.base.components.TypeResult;
import org.apache.commons.validator.routines.UrlValidator;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public final class Holovid extends JavaPlugin {

    private CommandManager commandManager;
    private Hologram hologram;

    @Override
    public void onEnable() {
        //saveDefaultConfig();

        // Loads the tasks util
        Task.init(this);

        commandManager = new CommandManager(this);

        getServer().getPluginManager().registerEvents(new HologramListener(this), this);
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
                new PlayCommand(this),
                new SpawnScreenCommand(this)
        ).forEach(commandManager::register);

    }

    public void spawnHologram(final Location location) {
        // Despawn old holograms if present
        despawnHologram();

        hologram = new Hologram(72);
        for (int i = 0; i < 72; i++) {
            hologram.addLine();
        }

        hologram.spawn(location);
    }

    public void despawnHologram() {
        if (hologram != null) {
            hologram.despawn();
            hologram = null;
        }
    }

    @Nullable
    public Hologram getHologram() {
        return hologram;
    }

}
