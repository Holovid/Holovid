package me.mattstudios.holovid.hologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.mattstudios.holovid.utils.NMSUtils;
import net.minecraft.server.v1_16_R1.IChatBaseComponent;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        this.entityId = NMSUtils.getNewEntityId();
    }

    /**
     * Sends a metadata update with the given text.
     *
     * @param component text component
     */
    public void updateText(final IChatBaseComponent component) {
        final PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, entityId);
        final List<WrappedWatchableObject> object = Collections.singletonList(
                new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(2,
                        WrappedDataWatcher.Registry.getChatComponentSerializer(true)), Optional.ofNullable(component)));
        packetContainer.getWatchableCollectionModifier().write(0, object);
        parent.distributePacket(packetContainer);
    }

    public PacketContainer createSpawnPackets(final Location location) {
        final PacketContainer spawnEntityLiving = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        spawnEntityLiving.getIntegers().write(0, entityId);
        spawnEntityLiving.getUUIDs().write(0, UUID.randomUUID());
        spawnEntityLiving.getIntegers().write(1, 1);
        spawnEntityLiving.getDoubles().write(0, location.getX());
        spawnEntityLiving.getDoubles().write(1, location.getY());
        spawnEntityLiving.getDoubles().write(2, location.getZ());
        return spawnEntityLiving;
    }

    public PacketContainer createDespawnPacket() {
        final PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packetContainer.getIntegerArrays().write(0, new int[]{entityId});
        return packetContainer;
    }

    public PacketContainer createMetadataPacket() {
        final PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, entityId);
        packetContainer.getWatchableCollectionModifier().write(0, buildMetadata());
        return packetContainer;
    }

    private List<WrappedWatchableObject> buildMetadata() {
        final IChatBaseComponent component = IChatBaseComponent.ChatSerializer.jsonToComponent("{\"text\":\"-\"}");
        return Arrays.asList(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20),
                new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), Optional.ofNullable(component)),
                new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true),
                new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true));
    }

    public Hologram getParent() {
        return parent;
    }

    public int getEntityId() {
        return entityId;
    }

}
