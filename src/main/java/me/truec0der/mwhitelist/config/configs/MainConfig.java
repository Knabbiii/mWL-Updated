package me.truec0der.mwhitelist.config.configs;

import lombok.Getter;
import me.truec0der.mwhitelist.config.ConfigHolder;
import me.truec0der.mwhitelist.model.enums.database.DatabaseType;
import me.truec0der.mwhitelist.model.enums.database.ModeType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.text.SimpleDateFormat;

@Getter
public class MainConfig extends ConfigHolder {
    private String locale;
    private SimpleDateFormat timeFormat;
    private Boolean updateCheck;
    private Boolean updateAuto;
    private Boolean status;
    private ModeType mode;
    private Boolean removeOnExpired;
    private Boolean kickOnRemove;
    private Boolean bypassPermissionEnabled;
    private String bypassPermission;
    private Boolean playersCheckEnabled;
    private Boolean expiredNotifyEnabled;
    private Long expiredNotifyTime;
    private Long playersCheckInitialDelay;
    private Long playersCheckDelay;
    private DatabaseType databaseType;
    private String mongoUrl;
    private String mongoName;
    private String mongoCollectionUser;

    public MainConfig(Plugin plugin, File filePath, String file) {
        super(plugin, filePath, file);
        loadAndSave();
        init();
    }

    @Override
    public void init() {
        YamlConfiguration config = this.getConfig();

        locale = config.getString("locale");
        timeFormat = new SimpleDateFormat(config.getString("time-format"));

        updateCheck = config.getBoolean("main.update.check");
        updateAuto = config.getBoolean("main.update.auto");

        status = config.getBoolean("whitelist.status");
        mode = ModeType.valueOf(config.getString("whitelist.mode"));
        removeOnExpired = config.getBoolean("whitelist.remove-on-expired");
        kickOnRemove = config.getBoolean("whitelist.kick-on-remove");

        bypassPermissionEnabled = config.getBoolean("whitelist.bypass.permission.enabled");
        bypassPermission = config.getString("whitelist.bypass.permission.permission");

        playersCheckEnabled = config.getBoolean("whitelist.player-check.enabled");
        playersCheckInitialDelay = config.getLong("whitelist.player-check.initial-delay");
        playersCheckDelay = config.getLong("whitelist.player-check.delay");

        expiredNotifyEnabled = config.getBoolean("whitelist.expired-notify.enabled");
        expiredNotifyTime = config.getLong("whitelist.expired-notify.time");

        databaseType = DatabaseType.valueOf(config.getString("database.type"));

        mongoUrl = config.getString("database.mongodb.url");
        mongoName = config.getString("database.mongodb.name");
        mongoCollectionUser = config.getString("database.mongodb.collections.users");
    }

    public void setStatus(boolean status) {
        getConfig().set("whitelist.status", status);
        save();
        reload();
    }
}
