package me.truec0der.mwhitelist.config.base.serializer.standard;

import me.truec0der.mwhitelist.config.base.node.EntryNode;
import me.truec0der.mwhitelist.config.base.serializer.SerializeHelper;
import me.truec0der.mwhitelist.config.base.serializer.SerializerRegistry;
import me.truec0der.mwhitelist.config.base.serializer.TypeSerializer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapSerializer implements TypeSerializer<Map<?, ?>> {

    @Override
    public Map<String, Object> deserialize(EntryNode node, String path, SerializerRegistry registry, Class<?> fieldType) {
        EntryNode subNode = node.getNode(path);
        if (subNode == null || subNode.keys().isEmpty()) return Collections.emptyMap();

        return deserializeMap(subNode, registry);
    }

    private Map<String, Object> deserializeMap(EntryNode node, SerializerRegistry registry) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (String key : node.keys()) {
            EntryNode childNode = node.getNode(key);
            if (childNode == null) continue;

            result.put(key, deserializeValue(childNode, registry));
        }

        return result;
    }

    private Object deserializeValue(EntryNode childNode, SerializerRegistry registry) {
        if (!childNode.keys().isEmpty()) {
            Object nested = SerializeHelper.tryDeserializeNested(childNode, registry);
            return nested != null ? nested : deserializeMap(childNode, registry);
        }

        return childNode.getValue();
    }
}
