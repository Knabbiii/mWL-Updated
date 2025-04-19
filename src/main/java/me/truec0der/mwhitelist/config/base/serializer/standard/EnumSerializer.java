package me.truec0der.mwhitelist.config.base.serializer.standard;

import me.truec0der.mwhitelist.config.base.exception.EntryDeserializationException;
import me.truec0der.mwhitelist.config.base.node.EntryNode;
import me.truec0der.mwhitelist.config.base.serializer.SerializerRegistry;
import me.truec0der.mwhitelist.config.base.serializer.TypeSerializer;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EnumSerializer implements TypeSerializer<Enum<?>> {
    @Override
    public Enum<?> deserialize(EntryNode node, String path, SerializerRegistry registry, Class<?> fieldType) {
        Object raw = node.get(path);
        if (raw == null) {
            return null;
        }

        String enumValue = raw.toString();

        if (fieldType != null && fieldType.isEnum()) {
            try {
                return Enum.valueOf((Class<Enum>) fieldType, enumValue);
            } catch (IllegalArgumentException e) {
                throw new EntryDeserializationException("Invalid value for enum: " + enumValue + " at path: " + path, e);
            }
        }
        return null;
    }
}
