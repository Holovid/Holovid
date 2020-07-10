package me.mattstudios.holovid.download;

import com.google.gson.Gson;
import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.display.TaskInfo;
import me.mattstudios.holovid.hologram.Hologram;
import me.mattstudios.holovid.model.ResourcePackResult;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AudioProcessor {

    private static final Gson GSON = new Gson();
    private final Set<UUID> awaitingResourcepack = new HashSet<>();
    private final Holovid plugin;
    private TaskInfo taskInfo;
    private int awaitingTask = -1;

    public AudioProcessor(final Holovid plugin) {
        this.plugin = plugin;
    }

    public void process(final Player player, final URL videoUrl, final TaskInfo taskInfo) throws IOException {
        final String stringUrl = videoUrl.toExternalForm();
        final HttpURLConnection c = (HttpURLConnection) new URL("https://holov.id/resourcepack/download/resource").openConnection();
        c.setRequestProperty("videoUrl", stringUrl);
        final InputStream in = c.getInputStream();
        if (c.getResponseCode() != 200) {
            throw new RuntimeException("Error requestion audio data for " + videoUrl);
        }

        final ResourcePackResult data;
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            data = GSON.fromJson(reader, ResourcePackResult.class);
        }

        player.sendMessage("The video display will start once all players in tracking range have accepted and downloaded the resourcepack!");
        for (final Player p : plugin.getHologram().getViewers()) {
            p.setResourcePack(data.getUrl(), data.getHash());
            awaitingResourcepack.add(p.getUniqueId());
        }

        this.taskInfo = taskInfo;

        awaitingTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Force start after an amount of time if not all players have accepted/downloaded the rp
            if (!awaitingResourcepack.isEmpty()) {
                awaitingResourcepack.clear();
                startTask();
            }
        }, 20 * 15).getTaskId();
    }

    private void startTask() {
        final Hologram hologram = plugin.getHologram();
        final Location location = hologram.getBaseLocation().add(0, 0.225D * (hologram.getLines().size() / 2D), 0);

        // Make the client load in the audio first with volume 0
        location.getWorld().playSound(location, "holovid.video", SoundCategory.RECORDS, 0.001F, 1);

        // Run later because of the audio loading
        final TaskInfo taskInfo = this.taskInfo;
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            // Stop the loading sound
            for (final Player player : plugin.getServer().getOnlinePlayers()) {
                player.stopSound("holovid.video", SoundCategory.RECORDS);
            }

            plugin.startBufferedTask(0, taskInfo.getFrames(), taskInfo.getHeight(), taskInfo.getFps(), taskInfo.interlacing());
            // Now actually start the sound (set a high volume to workaround attenuation)
            location.getWorld().playSound(location, "holovid.video", SoundCategory.RECORDS, 20, 1);
        }, 30);

        this.taskInfo = null;
        awaitingTask = -1;
    }

    public void removeAwaiting(final Player player) {
        if (awaitingResourcepack.remove(player.getUniqueId()) && awaitingResourcepack.isEmpty()) {
            plugin.getServer().getScheduler().cancelTask(awaitingTask);
            startTask();
        }
    }

    public void stopCurrentTask() {
        if (awaitingTask != -1) {
            plugin.getServer().getScheduler().cancelTask(awaitingTask);
            taskInfo = null;
            awaitingResourcepack.clear();
        }
    }
}
