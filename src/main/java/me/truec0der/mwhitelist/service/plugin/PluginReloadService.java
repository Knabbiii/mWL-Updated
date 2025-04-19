package me.truec0der.mwhitelist.service.plugin;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.config.ConfigRegister;
import me.truec0der.mwhitelist.config.configs.LangConfig;
import me.truec0der.mwhitelist.config.configs.MainConfig;
import me.truec0der.mwhitelist.impl.repository.RepositoryRegister;
import me.truec0der.mwhitelist.service.Service;
import me.truec0der.mwhitelist.service.ServiceRegister;
import org.bukkit.command.CommandSender;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PluginReloadService extends Service {
    public PluginReloadService(ServiceRegister serviceRegister, RepositoryRegister repositoryRegister, ConfigRegister configRegister) {
        super(serviceRegister, repositoryRegister, configRegister);
    }

    public void reload() {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        mainConfig.reload();
        langConfig.reload();

        getConfigRegister().init();
        getRepositoryRegister().init(mainConfig.getDatabase().getType());
        getServiceRegister().init();
    }

    public void reload(CommandSender sender) {
        reload();

        LangConfig langConfig = getConfigRegister().getLangConfig();
        sender.sendMessage(langConfig.getCommand().getReload().getInfo());
    }
}
