package me.truec0der.mwhitelist.config.base.loader;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class YamlConfigurationLoader implements ConfigurationLoader<YamlConfiguration> {
    private final Plugin plugin;

    public YamlConfigurationLoader(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public YamlConfiguration load(File directory, String fileName) {
        File configFile = new File(directory, fileName);

        ensureResourceExists(configFile, fileName);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        applyDefaults(config, fileName, configFile);

        return config;
    }

    @Override
    public YamlConfiguration load(File directory, String fileName, String defaultFileName) {
        File configFile = new File(directory, fileName);
        boolean hasDefault = plugin.getResource(defaultFileName) != null;

        String resourceToSave = hasDefault ? defaultFileName : fileName;

        ensureResourceExists(configFile, resourceToSave);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        applyDefaults(config, resourceToSave, configFile);

        return config;
    }

    @Override
    public void save(YamlConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public YamlConfiguration createNewConfig() {
        return new YamlConfiguration();
    }

    private void ensureResourceExists(File configFile, String resourceName) {
        if (!configFile.exists()) {
            plugin.saveResource(resourceName, false);
        }
    }

    private void applyDefaults(YamlConfiguration config, String resourceName, File configFile) {
        try (InputStream defaultStream = plugin.getResource(resourceName)) {
            if (defaultStream == null) return;
            try (InputStreamReader reader = new InputStreamReader(defaultStream)) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(reader);
                defaults.getKeys(false).stream()
                        .filter(key -> !config.contains(key))
                        .forEach(key -> config.set(key, defaults.get(key)));
                save(config, configFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}