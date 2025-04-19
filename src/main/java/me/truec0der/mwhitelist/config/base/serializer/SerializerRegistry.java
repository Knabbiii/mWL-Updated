package me.truec0der.mwhitelist.config.base.serializer;

import me.truec0der.mwhitelist.config.base.annotation.SerializableEntry;
import me.truec0der.mwhitelist.config.base.exception.EntryDeserializationException;
import me.truec0der.mwhitelist.config.base.node.EntryNode;
import me.truec0der.mwhitelist.config.base.serializer.standard.*;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SerializerRegistry {
    private final Map<Class<?>, TypeSerializer<?>> serializers = new HashMap<>();

    public SerializerRegistry() {
        registerDefaults();
    }

    private void registerDefaults() {
        register(String.class, new StringSerializer());
        register(int.class, new IntegerSerializer());
        register(Integer.class, new IntegerSerializer());
        register(boolean.class, new BooleanSerializer());
        register(Boolean.class, new BooleanSerializer());
        register(double.class, new DoubleSerializer());
        register(Double.class, new DoubleSerializer());
        register(long.class, new LongSerializer());
        register(Long.class, new LongSerializer());
        register(Enum.class, (TypeSerializer) new EnumSerializer());
        register(List.class, (TypeSerializer) new ListSerializer());
        register(Map.class, (TypeSerializer) new MapSerializer());
    }

    public <T> void register(Class<T> type, TypeSerializer<T> serializer) {
        serializers.put(type, serializer);
    }

    public <T> TypeSerializer<T> get(Class<T> type) {
        return (TypeSerializer<T>) serializers.get(type);
    }

    @Unmodifiable
    public Map<Class<?>, TypeSerializer<?>> getAll() {
        return serializers;
    }

    public boolean has(Class<?> type) {
        return serializers.containsKey(type);
    }

    public void deserialize(Object target, EntryNode node) {
        if (!target.getClass().isAnnotationPresent(SerializableEntry.class)) {
            return;
        }

        for (Field field : target.getClass().getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers())) continue;

            String path = toPath(field.getName());
            Class<?> fieldType = field.getType();

            try {
                field.setAccessible(true);

                if (has(fieldType)) {
                    Object value = get(fieldType).deserialize(node, path, this, fieldType);
                    field.set(target, value);
                } else if (fieldType.isEnum()) {
                    Object value = get(Enum.class).deserialize(node, path, this, fieldType);
                    field.set(target, value);
                } else {
                    deserializeNestedField(target, field, fieldType, path, node);
                }
            } catch (Exception e) {
                throw new EntryDeserializationException("Failed to deserialize field: " + field.getName(), e);
            }
        }
    }

    private void deserializeNestedField(Object target, Field field, Class<?> fieldType, String path, EntryNode node) throws Exception {
        EntryNode nestedNode = node.getNode(path);
        if (nestedNode != null) {
            Object instance = createNewInstance(fieldType);
            deserialize(instance, nestedNode);
            field.set(target, instance);
        } else {
            throw new EntryDeserializationException("Missing nested node for field: " + field.getName() + " at path: " + path);
        }
    }

    private Object createNewInstance(Class<?> fieldType) throws Exception {
        try {
            return fieldType.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new EntryDeserializationException("Failed to create instance of class: " + fieldType.getName(), e);
        }
    }

    private String toPath(String fieldName) {
        return fieldName.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase();
    }
}

