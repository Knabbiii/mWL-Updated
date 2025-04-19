package me.truec0der.mwhitelist.util;

import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.BiConsumer;

@UtilityClass
public class ReflectionHelper {
    @SafeVarargs
    public void deepFieldHandle(
            Object target,
            BiConsumer<Object, Field> fieldHandler,
            Class<? extends Annotation>... requiredAnnotations
    ) {
        if (target == null) return;

        Class<?> targetClass = target.getClass();
        for (Field field : targetClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) continue;

            field.setAccessible(true);

            try {
                Object fieldValue = field.get(target);
                if (fieldValue == null) continue;

                fieldHandler.accept(target, field);

                Class<?> fieldType = field.getType();
                boolean isJavaType = fieldType.getPackageName().startsWith("java");
                boolean isAnnotated = hasAnyAnnotation(fieldType, requiredAnnotations);

                if (!fieldType.isPrimitive() && !isJavaType && isAnnotated) {
                    deepFieldHandle(fieldValue, fieldHandler, requiredAnnotations);
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to access field: " + field.getName(), e);
            }
        }
    }

    @SafeVarargs
    private boolean hasAnyAnnotation(Class<?> type, Class<? extends Annotation>... annotations) {
        if (annotations == null || annotations.length == 0) return true;
        return Arrays.stream(annotations).anyMatch(type::isAnnotationPresent);
    }
}
