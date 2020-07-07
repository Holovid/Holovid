package me.mattstudios.holovid.download;

import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.display.TaskInfo;
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

    private final Set<UUID> awaitingResourcepack = new HashSet<>();
    private final Holovid plugin;
    private TaskInfo taskInfo;
    private int awaitingTask = -1;

    public AudioProcessor(final Holovid plugin) {
        this.plugin = plugin;
    }

    public void process(final Player player, final URL videoUrl, final TaskInfo taskInfo) throws IOException {
        final String stringUrl = videoUrl.toExternalForm();
        final HttpURLConnection c = (HttpURLConnection) new URL("https://holov.id/resourcepack/download/resource?videoUrl=" + stringUrl).openConnection();
        final String downloadLink;
        final InputStream in = c.getInputStream();
        if (c.getResponseCode() != 200) {
            throw new RuntimeException("Error requestion audio data for " + videoUrl);
        }

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            downloadLink = reader.readLine();
        }

        player.sendMessage("The video display will start once all players in tracking range have accepted and downloaded the resourcepack!");

        final String hash = stringUrl.length() > 40 ? stringUrl.substring(0, 40) : stringUrl;
        for (final Player p : plugin.getHologram().getViewers()) {
            p.setResourcePack(downloadLink, hash);
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

    public void removeAwaiting(final Player player) {
        if (awaitingResourcepack.remove(player.getUniqueId()) && awaitingResourcepack.isEmpty()) {
            plugin.getServer().getScheduler().cancelTask(awaitingTask);
            startTask();
        }
    }

    private void startTask() {
        plugin.startBufferedTask(0, taskInfo.getFrames(), taskInfo.getHeight(), taskInfo.getFps(), taskInfo.interlacing());
        taskInfo = null;
        awaitingTask = -1;
    }

    public void stopCurrentTask() {
        if (awaitingTask != -1) {
            plugin.getServer().getScheduler().cancelTask(awaitingTask);
            taskInfo = null;
            awaitingResourcepack.clear();
        }
    }
}
