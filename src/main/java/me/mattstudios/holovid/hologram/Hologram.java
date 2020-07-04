package me.mattstudios.holovid.hologram;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Hologram parent to manage individual hologram lines.
 */
public final class Hologram {

    private static final int TRACKING_DISTANCE_SQUARED = 50 * 50;
    private final Set<Player> viewers = Collections.newSetFromMap(new WeakHashMap<>());
    private final List<HologramLine> lines;
    private Location baseLocation;
    private boolean spawned;

    public Hologram(final int expectedSize) {
        this.lines = new ArrayList<>(expectedSize);
    }

    public Hologram() {
        this.lines = new ArrayList<>();
    }

    /**
     * Spawns the hologram and sends spawn packets to players in range.
     *
     * @param location base location
     */
    public void spawn(final Location location) {
        Preconditions.checkArgument(!spawned);
        this.baseLocation = location.clone();
        this.spawned = true;

        // Check for players in range
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (!baseLocation.getWorld().equals(player.getWorld())) continue;
            if (player.getLocation().distanceSquared(baseLocation) < TRACKING_DISTANCE_SQUARED) {
                viewers.add(player);
            }
        }

        if (viewers.isEmpty()) return;

        // Send spawn packets
        for (final HologramLine line : lines) {
            final PacketContainer spawnPacket = line.createSpawnPackets(location);
            final PacketContainer metadataPacket = line.createMetadataPacket();
            for (final Player player : viewers) {
                distributePacket(player, spawnPacket);
                distributePacket(player, metadataPacket);
            }
            location.add(0, 0.225D, 0);
        }
    }

    /**
     * Clears all lines, removes tracked players, and despawns the hologram.
     */
    public void despawn() {
        Preconditions.checkArgument(spawned);
        for (final HologramLine line : lines) {
            final PacketContainer despawnPacket = line.createDespawnPacket();
            for (final Player player : viewers) {
                distributePacket(player, despawnPacket);
            }
        }
        viewers.clear();
        spawned = false;
    }

    /**
     * Sends a spawn packet for the hologram to the given player if they weren't already in the tracking set.
     *
     * @param player player
     */
    public void addToTracking(final Player player) {
        Preconditions.checkArgument(spawned);
        if (!viewers.add(player)) return;

        final Location location = baseLocation.clone();
        for (final HologramLine line : lines) {
            final PacketContainer spawnPacket = line.createSpawnPackets(location);
            final PacketContainer metadataPacket = line.createMetadataPacket();
            distributePacket(player, spawnPacket);
            distributePacket(player, metadataPacket);
            location.add(0, 0.225D, 0);
        }
    }

    /**
     * Sends a despawn packet for the hologram to the given player if they were in the tracking set.
     *
     * @param player player
     */
    public void removeFromTracking(final Player player) {
        Preconditions.checkArgument(spawned);
        if (!viewers.remove(player)) return;
        for (final HologramLine line : lines) {
            final PacketContainer despawnPacket = line.createDespawnPacket();
            distributePacket(player, despawnPacket);
        }
    }

    /**
     * Checks if the given player is inside or outside of the hologram's tracking range
     * and sends its packets accordingly.
     *
     * @param player   player
     * @param location (new) location of the player
     */
    public void handleTracking(final Player player, final Location location) {
        if (baseLocation == null) return;
        if (!baseLocation.getWorld().equals(location.getWorld())) return;

        final double distance = baseLocation.distanceSquared(location);
        if (distance > TRACKING_DISTANCE_SQUARED) {
            removeFromTracking(player);
        } else {
            addToTracking(player);
        }
    }

    /**
     * Appends a new line to the top (!) of the hologram.
     *
     * @return created line
     */
    public HologramLine addLine() {
        final HologramLine line = new HologramLine(this);
        if (spawned) {
            final PacketContainer spawnPacket = line.createSpawnPackets(baseLocation.clone().add(0, 0.225D * lines.size(), 0));
            final PacketContainer metadataPacket = line.createMetadataPacket();
            for (final Player player : viewers) {
                distributePacket(player, spawnPacket);
                distributePacket(player, metadataPacket);
            }
        }
        lines.add(line);
        return line;
    }

    /**
     * Properly removes a line and despawns it for tracked players.
     *
     * @param index index
     * @throws IndexOutOfBoundsException if the index is lower than 0 or bigger than the size of the lines array
     */
    public void removeLine(final int index) {
        final HologramLine line = lines.remove(index);
        if (spawned) {
            final PacketContainer despawnPacket = line.createDespawnPacket();
            distributePacket(despawnPacket);
        }
    }

    public List<HologramLine> getLines() {
        return lines;
    }

    /**
     * Returns a mutable set of players in the hologram's tracking range.
     * Empty if not spawned.
     *
     * @return mutable set of players in the hologram's tracking range
     */
    public Set<Player> getViewers() {
        return viewers;
    }

    /**
     * @return bottom base position of the hologram
     */
    @Nullable
    public Location getBaseLocation() {
        return baseLocation.clone();
    }

    void distributePacket(final PacketContainer packetContainer) {
        for (final Player player : viewers) {
            distributePacket(player, packetContainer);
        }
    }

    void distributePacket(final Player player, final PacketContainer packetContainer) {
        if (!player.isOnline()) return;
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
