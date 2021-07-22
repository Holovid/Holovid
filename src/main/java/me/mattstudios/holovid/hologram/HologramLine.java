package me.mattstudios.holovid.hologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.mattstudios.holovid.utils.NMSUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Individual hologram line of its {@link Hologram} parent.
 */
public final class HologramLine {

    private static final WrappedDataWatcher.Serializer chatSerializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true);

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
    public void updateText(final Component component) {
        final PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, entityId);

        WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component));

        final List<WrappedWatchableObject> object = new ArrayList<>();
        object.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, chatSerializer), Optional.of(wrappedChatComponent.getHandle())));
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
        WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(Component.text("Waiting for video...")));

        final List<WrappedWatchableObject> content = new ArrayList<>();
        content.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20));
        content.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, chatSerializer), Optional.of(wrappedChatComponent.getHandle())));
        content.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true));
        content.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true));

        return content;
    }

    public Hologram getParent() {
        return parent;
    }

    public int getEntityId() {
        return entityId;
    }

}
