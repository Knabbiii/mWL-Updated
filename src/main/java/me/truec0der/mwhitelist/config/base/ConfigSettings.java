package me.truec0der.mwhitelist.config.base;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.config.base.adapter.ConfigurationAdapter;
import me.truec0der.mwhitelist.config.base.loader.ConfigurationLoader;

import java.io.File;

@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigSettings {
    File directory;
    String file;
    String defaultFile;
    ConfigurationLoader<?> loader;
    ConfigurationAdapter<?> adapter;
}