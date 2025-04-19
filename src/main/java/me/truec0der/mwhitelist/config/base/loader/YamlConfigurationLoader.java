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

        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        applyDefaults(config, fileName, configFile);

        return config;
    }

    @Override
    public YamlConfiguration load(File directory, String fileName, String defaultFileName) {
        File configFile = new File(directory, fileName);
        InputStream resourceStream = plugin.getResource(defaultFileName);

        String resourceToUse = (resourceStream != null) ? fileName : defaultFileName;
        File resourceFile = new File(directory, resourceToUse);

        if (!configFile.exists()) {
            configFile = (resourceStream != null) ? configFile : new File(directory, defaultFileName);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (!resourceFile.exists()) {
            plugin.saveResource(resourceToUse, false);
        }

        applyDefaults(config, fileName, configFile);

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

    private void applyDefaults(YamlConfiguration config, String resourceName, File configFile) {
        InputStream inputStream = plugin.getResource(resourceName);
        if (inputStream == null) return;

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {

            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(reader);

            for (String key : defaults.getKeys(false)) {
                if (!config.contains(key)) {
                    config.set(key, defaults.get(key));
                }
            }

            save(config, configFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}