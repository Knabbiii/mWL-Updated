package me.truec0der.mwhitelist.config.base.node;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntryNode {
    @Setter
    @Getter
    private Object value;
    private Map<String, EntryNode> children = new LinkedHashMap<>();

    public EntryNode() {
    }

    public static EntryNode fromMap(Map<?, ?> map) {
        EntryNode node = new EntryNode();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key)) continue;

            Object value = entry.getValue();
            EntryNode child = new EntryNode();

            if (value instanceof Map<?, ?> nestedMap) {
                child = fromMap(nestedMap);
            } else if (value instanceof List<?> list) {
                child.setValue(list);
            } else {
                child.setValue(value);
            }

            node.setNode(key, child);
        }

        return node;
    }

    public static EntryNode fromYamlConfiguration(YamlConfiguration yaml) {
        EntryNode root = new EntryNode();

        for (String key : yaml.getKeys(true)) {
            Object value = yaml.get(key);
            if (value instanceof ConfigurationSection) continue;

            String[] path = key.split("\\.");
            EntryNode current = root;

            for (int i = 0; i < path.length - 1; i++) {
                current = current.getNodeOrCreate(path[i]);
            }

            current.set(path[path.length - 1], value);
        }

        return root;
    }

    public EntryNode getNode(String key) {
        return children.get(key);
    }

    public void setNode(String key, EntryNode node) {
        children.put(key, node);
    }

    public Object get(String key) {
        EntryNode child = children.get(key);
        return child != null ? child.getValue() : null;
    }

    public void set(String key, Object value) {
        EntryNode node = new EntryNode();
        node.setValue(value);
        children.put(key, node);
    }

    public Set<String> keys() {
        return children.keySet();
    }

    public boolean has(String key) {
        return children.containsKey(key);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();

        for (Map.Entry<String, EntryNode> entry : children.entrySet()) {
            String key = entry.getKey();
            EntryNode child = entry.getValue();

            if (!child.children.isEmpty()) {
                map.put(key, child.toMap());
            } else {
                map.put(key, child.getValue());
            }
        }

        return map;
    }

    public EntryNode getNodeOrCreate(String key) {
        if (children == null) {
            children = new LinkedHashMap<>();
        }

        return children.computeIfAbsent(key, k -> new EntryNode());
    }
}
