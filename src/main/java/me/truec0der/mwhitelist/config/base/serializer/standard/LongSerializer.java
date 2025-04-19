package me.truec0der.mwhitelist.config.base.serializer.standard;

import me.truec0der.mwhitelist.config.base.node.EntryNode;
import me.truec0der.mwhitelist.config.base.serializer.SerializerRegistry;
import me.truec0der.mwhitelist.config.base.serializer.TypeSerializer;

public class LongSerializer implements TypeSerializer<Long> {
    @Override
    public Long deserialize(EntryNode node, String path, SerializerRegistry registry, Class<?> fieldType) {
        Object raw = node.get(path);
        return raw instanceof Number ? ((Number) raw).longValue() : null;
    }
}