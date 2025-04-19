package me.truec0der.mwhitelist.config.base.serializer.standard;

import me.truec0der.mwhitelist.config.base.node.EntryNode;
import me.truec0der.mwhitelist.config.base.serializer.SerializeHelper;
import me.truec0der.mwhitelist.config.base.serializer.SerializerRegistry;
import me.truec0der.mwhitelist.config.base.serializer.TypeSerializer;

import java.util.*;

public class ListSerializer implements TypeSerializer<List<?>> {
    @Override
    public List<Object> deserialize(EntryNode node, String path, SerializerRegistry registry, Class<?> fieldType) {
        Object rawValue = node.get(path);
        if (!(rawValue instanceof List<?> list)) return Collections.emptyList();

        return deserializeList(list, registry);
    }

    private List<Object> deserializeList(List<?> list, SerializerRegistry registry) {
        List<Object> deserializedList = new ArrayList<>();
        for (Object element : list) {
            deserializedList.add(deserializeElement(element, registry));
        }

        return deserializedList;
    }

    private Object deserializeElement(Object element, SerializerRegistry registry) {
        if (isPrimitiveOrWrapper(element)) return element;

        if (element instanceof Map<?, ?> map) {
            EntryNode nestedNode = EntryNode.fromMap(map);
            Object nestedObject = SerializeHelper.tryDeserializeNested(nestedNode, registry);
            return Objects.requireNonNullElse(nestedObject, map);
        }

        return element;
    }

    private boolean isPrimitiveOrWrapper(Object element) {
        return element instanceof String || element instanceof Number || element instanceof Boolean;
    }
}