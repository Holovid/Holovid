package me.mattstudios.holovid.compatability.wrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import java.util.Collections;

public class Wrapper1_17_1 extends Wrapper1_17{
    @Override
    public PacketContainer createRemovePacket(int entityId) {
        final PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

        packetContainer.getIntLists()
                .write(0, Collections.singletonList(entityId));

        return packetContainer;
    }
}
