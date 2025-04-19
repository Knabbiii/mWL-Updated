package me.truec0der.mwhitelist.config.base;

import lombok.Getter;
import me.truec0der.mwhitelist.config.base.serializer.SerializerRegistry;

@Getter
public abstract class ConfigSerializable {
    private final SerializerRegistry serializerRegistry;

    public ConfigSerializable() {
        this.serializerRegistry = new SerializerRegistry();
        registerSerializers();
    }

    protected abstract void registerSerializers();
}
