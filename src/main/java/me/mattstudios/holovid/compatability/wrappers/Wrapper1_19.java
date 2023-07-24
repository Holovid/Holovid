package me.mattstudios.holovid.compatability.wrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class Wrapper1_19 extends Wrapper1_18_2 {

    public PacketContainer createSpawnPacket(int entityId, Location location) {
        final PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        packet.getIntegers()
                .write(0, entityId);
        packet.getEntityTypeModifier()
                .write(0, EntityType.ARMOR_STAND);
        packet.getUUIDs()
                .write(0, UUID.randomUUID());
        packet.getDoubles() //Cords
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        return packet;
    }

}
