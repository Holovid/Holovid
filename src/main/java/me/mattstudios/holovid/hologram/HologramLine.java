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
        this.entityId = Holovid.getCompatibilityWrapper().getUniqueEntityID();
    }

    /**
     * Sends a metadata update with the given text.
     *
     * @param component text component
     */
    public void updateText(final Component component) {
        parent.distributePacketNoFlush(Holovid.getCompatibilityWrapper().getUpdatePacket(entityId, component));
    }

    public Object createSpawnPackets(final Location location) {
        return Holovid.getCompatibilityWrapper().createSpawnPacket(entityId, location);
    }

    public Object createDespawnPacket() {
        return Holovid.getCompatibilityWrapper().createRemovePacket(entityId);
    }

    public Object createMetadataPacket() {
        return Holovid.getCompatibilityWrapper().createMetadataPacket(entityId);
    }

    public Hologram getParent() {
        return parent;
    }

    public int getEntityId() {
        return entityId;
    }
}
