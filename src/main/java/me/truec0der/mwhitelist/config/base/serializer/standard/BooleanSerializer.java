package me.truec0der.mwhitelist.config.base.serializer.standard;

import me.truec0der.mwhitelist.config.base.node.EntryNode;
import me.truec0der.mwhitelist.config.base.serializer.SerializerRegistry;
import me.truec0der.mwhitelist.config.base.serializer.TypeSerializer;

public class BooleanSerializer implements TypeSerializer<Boolean> {
    @Override
    public Boolean deserialize(EntryNode node, String path, SerializerRegistry registry, Class<?> fieldType) {
        Object raw = node.get(path);
        return raw instanceof Boolean b ? b :
                raw instanceof String s ? Boolean.parseBoolean(s) : null;
    }
}
