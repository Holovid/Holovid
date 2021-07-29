package me.mattstudios.holovid.nms;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityTypes;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NMS_1_17_R1 implements NmsCommon {
    @Override
    public String getVersion() {
        return "1.17.1";
    }

    @Override
    public Object createSpawnPacket(final int entityId, final Location location) {
        return new PacketPlayOutSpawnEntity(
                entityId,
                UUID.randomUUID(),
                location.getX(),
                location.getY(),
                location.getZ(),
                0,
                0,
                EntityTypes.c,
                1,
                null
        );
    }

    @Override
    public Object createMetadataPacket(int entityId) {
        final PacketDataSerializer serializer = new PacketDataSerializer(UnpooledByteBufAllocator.DEFAULT.buffer());
        final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(serializer.a(new int[]{entityId}));
    }

    @Override
    public Object createRemovePacket(int entityId) {
        return new PacketPlayOutEntityDestroy(entityId);
    }

    @Override
    public void sendPacket(Player player, Object packet) {
        final CraftPlayer craftPlayer = ((CraftPlayer) player);
        final PlayerConnection playerConnection = craftPlayer.getHandle().b;
        final NetworkManager networkManager = playerConnection.a;
        final Channel playerChannel = networkManager.k;

        int entityId = 0;
        final Location location = null;

        final PacketPlayOutSpawnEntity spawnEntity =
                new PacketPlayOutSpawnEntity(entityId, UUID.randomUUID(), location.getX(),
                        location.getY(), location.getZ(), 0, 0, EntityTypes.c, 1, null);

        playerChannel.write(packet);
    }
}
