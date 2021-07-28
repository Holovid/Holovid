package me.mattstudios.holovid.compatability.utils;

import com.comphenix.protocol.wrappers.AbstractWrapper;
import com.comphenix.protocol.wrappers.ClonableWrapper;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Represents a chat component added in Minecraft 1.7.2
 * @author Kristian
 */
public class HolovidWrappedChatComponent extends AbstractWrapper implements ClonableWrapper {
    private static final Class<?> SERIALIZER = MinecraftReflection.getChatSerializerClass();
    private static final Class<?> COMPONENT = MinecraftReflection.getIChatBaseComponentClass();

    private static MethodAccessor SERIALIZE_COMPONENT = null;

    static {
        FuzzyReflection fuzzy = FuzzyReflection.fromClass(SERIALIZER, true);

        // Retrieve the correct methods
        SERIALIZE_COMPONENT = Accessors.getMethodAccessor(fuzzy.getMethodByParameters("serialize", /* a */
                String.class, new Class<?>[] { COMPONENT }));
    }

    /**
     * Construct a new chat component wrapper around the given NMS object.
     * @param component - Adventure Text Component.
     * @return The wrapper.
     */
    public static HolovidWrappedChatComponent fromComponent(Component component) {
        return new HolovidWrappedChatComponent(PaperAdventure.asVanilla(component), null);
    }

    private transient String cache;

    private HolovidWrappedChatComponent(Object handle, String cache) {
        super(MinecraftReflection.getIChatBaseComponentClass());
        setHandle(handle);
        this.cache = cache;
    }

    /**
     * Retrieve a copy of this component as a JSON string.
     * <p>
     * Note that any modifications to this JSON string will not update the current component.
     * @return The JSON representation of this object.
     */
    public String getJson() {
        if (cache == null) {
            cache = (String) SERIALIZE_COMPONENT.invoke(null, handle);
        }
        return cache;
    }

    /**
     * Retrieve a deep copy of the current chat component.
     * @return A copy of the current component.
     */
    public HolovidWrappedChatComponent deepClone() {
        return this; // TODO check if this is needed, shouldnt be
    }

    @Override
    public String toString() {
        return "HolovidWrappedChatComponent[json=" + getJson() + "]";
    }
}