package me.mattstudios.holovid.compatability.wrappers;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

public class Wrapper1_19_4 extends Wrapper1_19_3 {

    private AtomicInteger ENTITY_ID;

    @Override
    public int getUniqueEntityId() {
        if (ENTITY_ID == null) {
            try {
                final Field entityCount = Class.forName("net.minecraft.world.entity.Entity").getDeclaredField("d");
                entityCount.setAccessible(true);
                ENTITY_ID = (AtomicInteger) entityCount.get(null);
            } catch (final ReflectiveOperationException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return ENTITY_ID.incrementAndGet();
    }
}
