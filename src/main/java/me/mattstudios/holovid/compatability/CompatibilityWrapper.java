package me.mattstudios.holovid.compatability;

import com.comphenix.protocol.events.PacketContainer;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

public interface CompatibilityWrapper {
    PacketContainer createSpawnPacket(int entityId, Location location);

    PacketContainer createMetadataPacket(int entityId);

    PacketContainer getUpdatePacket(int entityId, final Component component);

    PacketContainer createRemovePacket(int entityId);

    int getUniqueEntityId();
}
