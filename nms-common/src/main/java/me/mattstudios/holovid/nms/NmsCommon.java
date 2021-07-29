package me.mattstudios.holovid.nms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface NmsCommon {
    String getVersion();

    Object createSpawnPacket(final int entityId, final Location location);

    Object createMetadataPacket(final int entityId);

    Object createRemovePacket(final int entityId);

    void sendPacket(final Player player, final Object packet);
}
