package me.mattstudios.holovid.compatability.wrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.mattstudios.holovid.compatability.CompatibilityWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Wrapper1_16 implements CompatibilityWrapper {
    private static final WrappedDataWatcher.Serializer chatSerializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true);

    public PacketContainer createSpawnPacket(int entityId, Location location) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

        packet.getIntegers()
                .write(0, entityId)
                .write(1, 1); // Armor Stand
        packet.getUUIDs()
                .write(0, UUID.randomUUID());
        packet.getDoubles() //Cords
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        return packet;
    }

    public PacketContainer createMetadataPacket(int entityId){
        final PacketContainer metaDataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        metaDataPacket.getIntegers()
                .write(0, entityId);

        final WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(Component.text("Waiting for video...")));
        final List<WrappedWatchableObject> watchers = Arrays.asList(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20),
                new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, chatSerializer), Optional.of(wrappedChatComponent.getHandle())),
                new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true),
                new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true));

        metaDataPacket.getWatchableCollectionModifier()
                .write(0, watchers);

        return metaDataPacket;
    }

    public PacketContainer getUpdatePacket(int entityId, final Component component) {
        final PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        packetContainer.getIntegers()
                .write(0, entityId);

        WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component));
        final List<WrappedWatchableObject> object = Collections.singletonList(
                new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, chatSerializer), Optional.of(wrappedChatComponent.getHandle())));

        packetContainer.getWatchableCollectionModifier()
                .write(0, object);

        return packetContainer;
    }

    public PacketContainer createRemovePacket(int entityId) {
        final PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packetContainer.getIntegerArrays().write(0,
                new int[]{entityId});
        return packetContainer;
    }

    private AtomicInteger ENTITY_ID;
    private String NMS;

    public Class<?> getNMSClass(final String className) throws ClassNotFoundException {
        return Class.forName(NMS + className);
    }

    @Override
    public int getUniqueEntityId() {
        if (ENTITY_ID == null){
            final String packageName = Bukkit.getServer().getClass().getPackage().getName();
            final String SERVER_VERSION = packageName.substring(packageName.lastIndexOf('.') + 1);
            NMS = "net.minecraft.server." + SERVER_VERSION + ".";

            try {
                final Field entityCount = getNMSClass("Entity").getDeclaredField("entityCount");
                entityCount.setAccessible(true);
                ENTITY_ID = (AtomicInteger) entityCount.get(null);
            } catch (final ReflectiveOperationException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return ENTITY_ID.incrementAndGet();
    }
}
