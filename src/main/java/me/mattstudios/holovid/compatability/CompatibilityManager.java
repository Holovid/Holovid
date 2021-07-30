package me.mattstudios.holovid.compatability;

import me.mattstudios.holovid.Holovid;
import me.mattstudios.holovid.nms.NMS_1_17_R1;
import me.mattstudios.holovid.nms.NmsCommon;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * Inspired by AnvilGUI
 * https://github.com/WesJD/AnvilGUI/blob/master/api/src/main/java/net/wesjd/anvilgui/version/VersionMatcher.java
 */
public class CompatibilityManager {

    private final NmsCommon wrapper;

    private final List<Class<? extends NmsCommon>> versions = Arrays.asList(
            NMS_1_17_R1.class
    );

    public CompatibilityManager(Holovid holovid){
        final String forcedVersion = holovid.getUseVersionInstead();
        if (forcedVersion != null && !forcedVersion.equals("")){
            wrapper = match(forcedVersion);
        } else {
            String serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
            wrapper = match(serverVersion);
        }
    }

    private NmsCommon match(String serverVersion) {
        try {
            return versions.stream()
                    .filter(version -> version.getSimpleName().substring(4).equals(serverVersion))
                    .findFirst().orElseThrow(() -> new RuntimeException("Your server version [" + serverVersion + "] isn't supported in Holovid! Setting use-this-version-instead to a version string, like 1_17_R1, will skip this check and might work. Proceed with caution."))
                    .getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public NmsCommon getWrapper() {
        return wrapper;
    }
}
