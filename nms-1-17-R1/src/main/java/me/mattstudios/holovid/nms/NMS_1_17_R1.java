package me.mattstudios.holovid.nms;

import io.netty.channel.Channel;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NMS_1_17_R1 implements NmsCommon {
    private final Entity entity;

    public NMS_1_17_R1() {
        World world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();

        entity = new EntityArmorStand(EntityTypes.c, world);
        entity.setInvisible(true);
        entity.setInvulnerable(true);
        entity.setCustomNameVisible(true);
    }

    @Override
    public String getVersion() {
        return "1.17.1";
    }

    @Override
    public Object getUpdatePacket(int entityId, final Component component) {
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
                EntityTypes.c,
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
        final PlayerConnection playerConnection = craftPlayer.getHandle().b;
        final NetworkManager networkManager = playerConnection.a;
        final Channel playerChannel = networkManager.k;

        playerChannel.write(packet);
    }

    @Override
    public void flush(Player player) {
        final CraftPlayer craftPlayer = ((CraftPlayer) player);
        final PlayerConnection playerConnection = craftPlayer.getHandle().b;
        final NetworkManager networkManager = playerConnection.a;
        final Channel playerChannel = networkManager.k;

        playerChannel.flush();
    }

    @Override
    public int getUniqueEntityID() {
        return Entity.nextEntityId();
    }
}
