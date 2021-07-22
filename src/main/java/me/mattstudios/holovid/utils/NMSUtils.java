package me.mattstudios.holovid.utils;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

public final class NMSUtils {

    private static final AtomicInteger ENTITY_ID;
    private static final String NMS;
    private static final String SERVER_VERSION;

    static {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        SERVER_VERSION = packageName.substring(packageName.lastIndexOf('.') + 1);
        NMS = "net.minecraft.server." + SERVER_VERSION + ".";
        /*
        try {
            final Field entityCount = getNMSClass("Entity").getDeclaredField("entityCount");
            entityCount.setAccessible(true);
            ENTITY_ID = (AtomicInteger) entityCount.get(null);
        } catch (final ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }

         */
        ENTITY_ID = new AtomicInteger(100000000);
    }

    public static Class<?> getNMSClass(final String className) throws ClassNotFoundException {
        return Class.forName(NMS + className);
    }

    public static String getServerVersion() {
        return SERVER_VERSION;
    }

    public static int getNewEntityId() {
        return ENTITY_ID.incrementAndGet();
    }

}
