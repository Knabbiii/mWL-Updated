package me.truec0der.mwhitelist.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.config.base.ConfigSettings;
import me.truec0der.mwhitelist.config.base.adapter.YamlConfigurationAdapter;
import me.truec0der.mwhitelist.config.base.loader.YamlConfigurationLoader;
import me.truec0der.mwhitelist.config.configs.LangConfig;
import me.truec0der.mwhitelist.config.configs.MainConfig;
import org.bukkit.plugin.Plugin;

import java.io.File;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigRegister {
    final Plugin plugin;
    @Getter
    MainConfig mainConfig;
    @Getter
    LangConfig langConfig;

    public ConfigRegister(Plugin plugin) {
        this.plugin = plugin;
        init();
    }

    public void init() {
        File directory = new File(plugin.getDataFolder().getPath());

        mainConfig = new MainConfig(
                ConfigSettings.builder()
                        .directory(directory)
                        .file("config.yml")
                        .loader(new YamlConfigurationLoader(plugin))
                        .adapter(new YamlConfigurationAdapter())
                        .build()
        );

        langConfig = new LangConfig(
                ConfigSettings.builder()
                        .directory(directory)
                        .file(String.format("messages/lang_%s.yml", mainConfig.getLocale()))
                        .defaultFile("messages/lang_en.yml")
                        .loader(new YamlConfigurationLoader(plugin))
                        .adapter(new YamlConfigurationAdapter())
                        .build()
        );
    }
}
