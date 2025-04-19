package me.truec0der.mwhitelist.config.base.serializer.custom;

import me.truec0der.mwhitelist.config.base.node.EntryNode;
import me.truec0der.mwhitelist.config.base.serializer.SerializerRegistry;
import me.truec0der.mwhitelist.config.base.serializer.TypeSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ComponentSerializer implements TypeSerializer<Component> {
    private final static MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    public Component deserialize(EntryNode node, String path, SerializerRegistry serializerRegistry, Class<?> fieldType) {
        Object raw = node.get(path);
        return raw != null ? MINI_MESSAGE.deserialize(raw.toString()) : null;
    }
}
