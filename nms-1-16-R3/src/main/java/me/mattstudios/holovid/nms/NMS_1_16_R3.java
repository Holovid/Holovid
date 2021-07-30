package me.mattstudios.holovid.nms;

import io.netty.channel.Channel;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NMS_1_16_R3 implements NmsCommon {
    private final Entity entity;

    public NMS_1_16_R3() {
        World world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();

        entity = new EntityArmorStand(EntityTypes.ARMOR_STAND, world);
        entity.setInvisible(true);
        entity.setInvulnerable(true);
        entity.setCustomNameVisible(true);
    }

    @Override
    public String getVersion() {
        return "1.16.5";
    }

    @Override
    public Object getUpdatePacket(int entityId, Component component) {
        entity.setCustomName(PaperAdventure.asVanilla(component));
        return new PacketPlayOutEntityMetadata(entityId, entity.getDataWatcher(), false);
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
                EntityTypes.ARMOR_STAND,
                1,
                new Vec3D(0, 0, 0)
        );
    }


    @Override
    public Object createMetadataPacket(int entityId) {
        return new PacketPlayOutEntityMetadata(entityId, entity.getDataWatcher(), true);
    }


    @Override
    public Object createRemovePacket(int entityId) {
        return new PacketPlayOutEntityDestroy(entityId);
    }

    @Override
    public void sendPacket(Player player, Object packet) {
        final CraftPlayer craftPlayer = ((CraftPlayer) player);
        final PlayerConnection playerConnection = craftPlayer.getHandle().playerConnection;
        final NetworkManager networkManager = playerConnection.networkManager;
        final Channel playerChannel = networkManager.channel;

        playerChannel.write(packet);
    }

    @Override
    public void flush(Player player) {
        final CraftPlayer craftPlayer = ((CraftPlayer) player);
        final PlayerConnection playerConnection = craftPlayer.getHandle().playerConnection;
        final NetworkManager networkManager = playerConnection.networkManager;
        final Channel playerChannel = networkManager.channel;

        playerChannel.flush();
    }

    @Override
    public int getUniqueEntityID() {
        return Entity.nextEntityId();
    }
}
