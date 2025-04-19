package me.truec0der.mwhitelist.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.intellij.lang.annotations.RegExp;

import java.lang.annotation.Annotation;

@UtilityClass
public class ComponentHelper {
    @SafeVarargs
    public void replacePlaceholderInClass(Object target, @RegExp String placeholder, Component replacement, Class<? extends Annotation>... requiredAnnotations) {
        ReflectionHelper.deepFieldHandle(target, (obj, field) -> {
            try {
                Object value = field.get(obj);

                if (value instanceof Component component) {
                    Component replaced = component.replaceText(builder ->
                            builder.match(placeholder).replacement(replacement)
                    );
                    field.set(obj, replaced);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to access field " + field.getName(), e);
            }
        }, requiredAnnotations);
    }
}

