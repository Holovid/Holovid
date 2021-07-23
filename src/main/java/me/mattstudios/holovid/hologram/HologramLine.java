package me.mattstudios.holovid.hologram;

import com.comphenix.protocol.events.PacketContainer;
import me.mattstudios.holovid.Holovid;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

/**
 * Individual hologram line of its {@link Hologram} parent.
 */
public final class HologramLine {
    private final Hologram parent;
    private final int entityId;

    /**
     * @param parent parent hologram
     * @see #updateText(IChatBaseComponent) to set text; not stored since they are supposed to be quickly discarded and changed
     */
    public HologramLine(final Hologram parent) {
        this.parent = parent;
        this.entityId = Holovid.getCompatibilityWrapper().getUniqueEntityId();
    }

    /**
     * Sends a metadata update with the given text.
     *
     * @param component text component
     */
    public void updateText(final Component component) {
        parent.distributePacket(Holovid.getCompatibilityWrapper().getUpdatePacket(entityId, component));
    }

    public PacketContainer createSpawnPackets(final Location location) {
        return Holovid.getCompatibilityWrapper().createSpawnPacket(entityId, location);
    }

    public PacketContainer createDespawnPacket() {
        return Holovid.getCompatibilityWrapper().createRemovePacket(entityId);
    }

    public PacketContainer createMetadataPacket() {
        return Holovid.getCompatibilityWrapper().createMetadataPacket(entityId);
    }

    public Hologram getParent() {
        return parent;
    }

    public int getEntityId() {
        return entityId;
    }
}
