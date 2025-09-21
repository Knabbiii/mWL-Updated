package me.truec0der.mwhitelist.config.base.serializer;

import me.truec0der.mwhitelist.config.base.annotation.SerializableEntry;
import me.truec0der.mwhitelist.config.base.exception.EntryDeserializationException;
import me.truec0der.mwhitelist.config.base.node.EntryNode;
import me.truec0der.mwhitelist.config.base.serializer.standard.*;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    @SuppressWarnings("unchecked")
    public <T> TypeSerializer<T> get(Class<T> type) {
        return (TypeSerializer<T>) serializers.get(type);
    }

    @Unmodifiable
    public Map<Class<?>, TypeSerializer<?>> getAll() {
        return Map.copyOf(serializers);
    }

    public boolean has(Class<?> type) {
        return serializers.containsKey(type);
    }

    public void deserialize(Object target, EntryNode node) {
        Class<?> targetClass = target.getClass();
        if (!targetClass.isAnnotationPresent(SerializableEntry.class)) {
            return;
        }

        for (Field field : targetClass.getDeclaredFields()) {
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
            } catch (EntryDeserializationException e) {
                throw e;
            } catch (Exception e) {
                throw new EntryDeserializationException(
                        "Failed to deserialize field '%s' (type: %s) in class '%s' at path '%s'. Node value: %s"
                                        .formatted(
                                                field.getName(),
                                                fieldType.getSimpleName(),
                                                targetClass.getName(),
                                                path,
                                                safeNodeValue(node, path)
                                        ),
                        e
                );
            }
        }
    }

    private void deserializeNestedField(Object target, Field field,
                                        Class<?> fieldType, String path, EntryNode node) {
        EntryNode nestedNode = node.getNode(path);
        if (nestedNode == null) {
            throw new EntryDeserializationException(
                    "Missing nested node for field '%s' (type: %s) in class '%s' at path '%s'"
                            .formatted(
                                    field.getName(),
                                    fieldType.getSimpleName(),
                                    target.getClass().getName(),
                                    path
                            )
            );
        }
        try {
            Object instance = createNewInstance(fieldType);
            deserialize(instance, nestedNode);
            field.set(target, instance);
        } catch (EntryDeserializationException e) {
            throw new EntryDeserializationException(
                    "Error while deserializing nested object for field '%s' (type: %s) in class '%s'"
                            .formatted(
                                    field.getName(),
                                    fieldType.getSimpleName(),
                                    target.getClass().getName()
                            ),
                    e
            );
        } catch (Exception e) {
            throw new EntryDeserializationException(
                    "Could not create or populate instance of '%s' for field '%s' in class '%s'"
                            .formatted(
                                    fieldType.getName(),
                                    field.getName(),
                                    target.getClass().getName()
                            ),
                    e
            );
        }
    }

    private Object createNewInstance(Class<?> fieldType) {
        try {
            return fieldType.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException |
                 InstantiationException | InvocationTargetException e) {
            throw new EntryDeserializationException(
                    "Failed to create an instance of '%s'. Make sure it has a public no-arg constructor."
                            .formatted(fieldType.getName()),
                    e
            );
        }
    }

    private String toPath(String fieldName) {
        return fieldName.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase();
    }

    private static String safeNodeValue(EntryNode node, String path) {
        try {
            return String.valueOf(node.getNode(path));
        } catch (Exception ignored) {
            return "<unavailable>";
        }
    }
}

