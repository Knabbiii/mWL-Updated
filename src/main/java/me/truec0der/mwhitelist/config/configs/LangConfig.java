package me.truec0der.mwhitelist.config.configs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.config.base.ConfigHolder;
import me.truec0der.mwhitelist.config.base.ConfigSettings;
import me.truec0der.mwhitelist.config.base.annotation.SerializableEntry;
import me.truec0der.mwhitelist.config.base.serializer.custom.ComponentSerializer;
import me.truec0der.mwhitelist.util.ComponentHelper;
import net.kyori.adventure.text.Component;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SerializableEntry
public class LangConfig extends ConfigHolder<LangConfig> {
    Component prefix;
    Component needCorrectArgs;
    Component notInWhitelist;
    Component whitelistTimeExpired;
    Component expiredNotify;
    Component notPerms;
    Main main = new Main();
    Command command = new Command();

    public LangConfig(ConfigSettings configSettings) {
        super(configSettings);
        loadAndSave();
        init();
    }

    @Override
    protected void registerSerializers() {
        getSerializerRegistry()
                .register(Component.class, new ComponentSerializer());
    }

    @Override
    public void init() {
        super.init();

        ComponentHelper.replacePlaceholderInClass(
                this,
                "%prefix%",
                prefix,
                SerializableEntry.class
        );
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
            Component notify;
            Component notifyFailed;
            Component versionInfo;
            Component action;
            Component actionFailed;
        }
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SerializableEntry
    public static class Command {
        Help help = new Help();
        Info info = new Info();
        Toggle toggle = new Toggle();
        Add add = new Add();
        AddTemp addTemp = new AddTemp();
        SetTemp setTemp = new SetTemp();
        ExtendTemp extendTemp = new ExtendTemp();
        Remove remove = new Remove();
        List list = new List();
        Check check = new Check();
        Reload reload = new Reload();

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class Help {
            Component info;
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class Info {
            Status status = new Status();
            Component info;

            @Getter
            @FieldDefaults(level = AccessLevel.PRIVATE)
            @SerializableEntry
            public static class Status {
                Component enabled;
                Component disabled;
            }
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class Toggle {
            Component invalidAction;
            Component enabled;
            Component disabled;
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class Add {
            Component alreadyAdded;
            Component added;
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class AddTemp {
            Component invalidTime;
            Component alreadyAdded;
            Component added;
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class SetTemp {
            Component invalidTime;
            Component setted;
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class ExtendTemp {
            Component invalidTime;
            Component extended;
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class Remove {
            Component notIn;
            Component removed;
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class List {
            Component info;
            Component empty;
            Component player;
            Component separator;
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class Check {
            NicknameHistory nicknameHistory = new NicknameHistory();
            Time time = new Time();

            Component notIn;
            Component info;

            @Getter
            @FieldDefaults(level = AccessLevel.PRIVATE)
            @SerializableEntry
            public static class NicknameHistory {
                Component nickname;
                Component separator;
            }

            @Getter
            @FieldDefaults(level = AccessLevel.PRIVATE)
            @SerializableEntry
            public static class Time {
                Infinity infinity = new Infinity();
                Expired expired = new Expired();
                Active active = new Active();

                @Getter
                @FieldDefaults(level = AccessLevel.PRIVATE)
                @SerializableEntry
                public static class Infinity {
                    Component info;
                    Component about;
                }

                @Getter
                @FieldDefaults(level = AccessLevel.PRIVATE)
                @SerializableEntry
                public static class Expired {
                    Component about;
                }

                @Getter
                @FieldDefaults(level = AccessLevel.PRIVATE)
                @SerializableEntry
                public static class Active {
                    Component about;
                }
            }
        }

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SerializableEntry
        public static class Reload {
            Component info;
        }
    }
}
