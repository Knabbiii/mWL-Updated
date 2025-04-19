package me.truec0der.mwhitelist.config.base;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import me.truec0der.mwhitelist.config.base.adapter.ConfigurationAdapter;
import me.truec0der.mwhitelist.config.base.loader.ConfigurationLoader;
import me.truec0der.mwhitelist.config.base.node.EntryNode;

import java.io.File;

@SuppressWarnings("unchecked")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class ConfigHolder<T extends ConfigHolder<T>> extends ConfigSerializable {
    File configFile;
    File directory;
    ConfigurationLoader<?> loader;
    ConfigurationAdapter<?> adapter;
    String file;
    String defaultFile;
    @Getter
    @NonFinal
    Object config;

    protected ConfigHolder(ConfigSettings configSettings) {
        this.directory = configSettings.getDirectory();
        this.file = configSettings.getFile();
        this.defaultFile = configSettings.getDefaultFile() == null ? file : configSettings.getDefaultFile();
        this.loader = configSettings.getLoader();
        this.adapter = configSettings.getAdapter();
        this.configFile = new File(directory, file);
    }

    public void load() {
        if (defaultFile == null) {
            config = ((ConfigurationLoader<Object>) loader).load(directory, file);
        } else {
            config = ((ConfigurationLoader<Object>) loader).load(directory, file, defaultFile);
        }
    }

    public void save() {
        ((ConfigurationLoader<Object>) loader).save(config, configFile);
    }

    public void loadAndSave() {
        load();
        save();
    }

    public void reload() {
        loadAndSave();
        init();
    }

    protected void init() {
        EntryNode node = ((ConfigurationAdapter<Object>) adapter).createEntryNode(config);
        getSerializerRegistry().deserialize(this, node);
    }

    protected abstract void registerSerializers();

    public void setValue(String path, Object value) {
        ((ConfigurationAdapter<Object>) adapter).setValue(config, path, value);
    }

    public void setValueAndSave(String path, Object value) {
        setValue(path, value);
        save();
        reload();
    }

    public Object getValue(String path) {
        return ((ConfigurationAdapter<Object>) adapter).getValue(config, path);
    }

    public boolean contains(String path) {
        return ((ConfigurationAdapter<Object>) adapter).contains(config, path);
    }
}