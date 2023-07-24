package me.mattstudios.holovid.compatability.wrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Wrapper1_19_3 extends Wrapper1_19_2 {
    private static final WrappedDataWatcher.Serializer chatSerializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true);

    public PacketContainer createMetadataPacket(int entityId){
        final PacketContainer metaDataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        metaDataPacket.getIntegers()
                .write(0, entityId);

        final WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(Component.text("Waiting for video...")));

        final List<WrappedDataValue> content = new ArrayList<>();
        content.add(new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 40));
        content.add(new WrappedDataValue(2, chatSerializer, Optional.of(wrappedChatComponent.getHandle())));
        content.add(new WrappedDataValue(3, WrappedDataWatcher.Registry.get(Boolean.class), true));
        content.add(new WrappedDataValue(5, WrappedDataWatcher.Registry.get(Boolean.class), true));

        metaDataPacket.getDataValueCollectionModifier()
                .write(0, content);

        return metaDataPacket;
    }

    public PacketContainer getUpdatePacket(int entityId, final Component component) {
        final PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        packetContainer.getIntegers()
                .write(0, entityId);

        final WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component));
        final List<WrappedDataValue> object = new ArrayList<>();
        object.add(new WrappedDataValue(2, chatSerializer, Optional.of(wrappedChatComponent.getHandle())));

        packetContainer.getDataValueCollectionModifier()
                .write(0, object);

        return packetContainer;
    }
}
