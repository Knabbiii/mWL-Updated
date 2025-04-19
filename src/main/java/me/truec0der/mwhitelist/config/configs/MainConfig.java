package me.truec0der.mwhitelist.config.configs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.config.base.ConfigHolder;
import me.truec0der.mwhitelist.config.base.ConfigSettings;
import me.truec0der.mwhitelist.config.base.annotation.SerializableEntry;
import me.truec0der.mwhitelist.config.base.serializer.custom.ComponentSerializer;
import me.truec0der.mwhitelist.model.enums.database.DatabaseType;
import me.truec0der.mwhitelist.model.enums.database.ModeType;
import net.kyori.adventure.text.Component;

import java.text.SimpleDateFormat;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SerializableEntry
public class MainConfig extends ConfigHolder<MainConfig> {
    String locale;
    String timeFormat;
    Main main = new Main();
    Whitelist whitelist = new Whitelist();
    Database database = new Database();

    public MainConfig(ConfigSettings configSettings) {
        super(configSettings);
        loadAndSave();
        init();
    }

    @Override
    protected void registerSerializers() {
        getSerializerRegistry()
                .register(Component.class, new ComponentSerializer());
    }

    public SimpleDateFormat getTimeFormat() {
        return new SimpleDateFormat(timeFormat);
    }

    public void setStatus(boolean isEnabled) {
        setValueAndSave("whitelist.status", isEnabled);
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SerializableEntry
    public static class Main {
        Update update = new Update();

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class Update {
            boolean check;
            boolean auto;
        }
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SerializableEntry
    public static class Whitelist {
        boolean status;
        ModeType mode;
        boolean removeOnExpired;
        boolean kickOnRemove;
        Bypass bypass = new Bypass();
        PlayerCheck playerCheck = new PlayerCheck();
        ExpiredNotify expiredNotify = new ExpiredNotify();

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class Bypass {
            Permission permission = new Permission();

            @Getter
            @FieldDefaults(level = AccessLevel.PRIVATE)
            @SerializableEntry
            public static class Permission {
                boolean enabled;
                String permission;
            }
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class PlayerCheck {
            boolean enabled;
            int initialDelay;
            int delay;
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class ExpiredNotify {
            boolean enabled;
            long time;
        }
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SerializableEntry
    public static class Database {
        DatabaseType type;
        MongoDB mongodb = new MongoDB();

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class MongoDB {
            String url;
            String name;
            Collections collections = new Collections();

            @Getter
            @FieldDefaults(level = AccessLevel.PRIVATE)
            @SerializableEntry
            public static class Collections {
                String users;
            }
        }
    }
}
