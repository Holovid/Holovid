package me.mattstudios.holovid.nms;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface NmsCommon {
    String getVersion();

    Object getUpdatePacket(int entityId, final Component component);

    Object createSpawnPacket(final int entityId, final Location location);

    Object createMetadataPacket(final int entityId);

    Object createRemovePacket(final int entityId);

    void sendPacket(final Player player, final Object packet);

    void flush(final Player player);

    int getUniqueEntityID();
}
