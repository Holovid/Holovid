package me.mattstudios.holovid.nms;

import io.netty.channel.Channel;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NMS_1_16_R3 implements NmsCommon {
    @Override
    public String getVersion() {
        return "1.16.5";
    }

    @Override
    public void sendPacket(Player player, Object packet) {
        final CraftPlayer craftPlayer = ((CraftPlayer) player);
        final PlayerConnection playerConnection = craftPlayer.getHandle().playerConnection;
        final NetworkManager networkManager = playerConnection.networkManager;
        final Channel playerChannel = networkManager.channel;
        playerChannel.write(packet);
    }
}
