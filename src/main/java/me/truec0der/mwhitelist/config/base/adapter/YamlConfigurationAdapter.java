package me.truec0der.mwhitelist.config.base.adapter;

import me.truec0der.mwhitelist.config.base.node.EntryNode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;

public class YamlConfigurationAdapter implements ConfigurationAdapter<YamlConfiguration> {
    @Override
    public EntryNode createEntryNode(YamlConfiguration config) {
        EntryNode root = new EntryNode();
        processSection(root, config);
        return root;
    }

    @Override
    public void setValue(YamlConfiguration config, String path, Object value) {
        config.set(path, value);
    }

    @Override
    public Object getValue(YamlConfiguration config, String path) {
        return config.get(path);
    }

    @Override
    public boolean contains(YamlConfiguration config, String path) {
        return config.contains(path);
    }

    private void processSection(EntryNode node, ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                EntryNode childNode = new EntryNode();
                processSection(childNode, section.getConfigurationSection(key));
                node.setNode(key, childNode);
            } else {
                node.set(key, section.get(key));
            }
        }
    }

    private void processNodeMap(YamlConfiguration config, String path, EntryNode node) {
        Map<?, ?> map = (Map<?, ?>) node.getValue();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = path + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                EntryNode childNode = new EntryNode();
                childNode.setValue(entry.getValue());
                processNodeMap(config, key, childNode);
            } else {
                config.set(key, entry.getValue());
            }
        }
    }
}
