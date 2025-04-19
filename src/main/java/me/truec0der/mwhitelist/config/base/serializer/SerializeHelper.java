package me.truec0der.mwhitelist.config.base.serializer;

import lombok.experimental.UtilityClass;
import me.truec0der.mwhitelist.config.base.annotation.SerializableEntry;
import me.truec0der.mwhitelist.config.base.node.EntryNode;

@UtilityClass
public class SerializeHelper {
    public Object tryDeserializeNested(EntryNode node, SerializerRegistry registry) {
        for (Class<?> clazz : registry.getAll().keySet()) {
            if (!clazz.isAnnotationPresent(SerializableEntry.class)) continue;

            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                registry.deserialize(instance, node);
                return instance;
            } catch (Exception ignored) {
            }
        }

        return null;
    }
}
