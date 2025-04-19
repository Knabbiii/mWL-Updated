package me.truec0der.mwhitelist.config.base.adapter;

import me.truec0der.mwhitelist.config.base.node.EntryNode;

public interface ConfigurationAdapter<T> {
    EntryNode createEntryNode(T config);

    void setValue(T config, String path, Object value);

    Object getValue(T config, String path);

    boolean contains(T config, String path);
}
