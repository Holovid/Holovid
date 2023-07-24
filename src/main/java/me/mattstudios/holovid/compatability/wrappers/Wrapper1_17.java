package me.mattstudios.holovid.compatability.wrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

public class Wrapper1_17 extends Wrapper1_16{
    @Override
    public PacketContainer createRemovePacket(int entityId) {
        final PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packetContainer.getIntegers().write(0, entityId);
        return packetContainer;
    }

    private AtomicInteger ENTITY_ID;

    @Override
    public int getUniqueEntityId() {
        if (ENTITY_ID == null){
            try {
                final Field entityCount = Class.forName("net.minecraft.world.entity.Entity").getDeclaredField("b");
                entityCount.setAccessible(true);
                ENTITY_ID = (AtomicInteger) entityCount.get(null);
            } catch (final ReflectiveOperationException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return ENTITY_ID.incrementAndGet();
    }
}
