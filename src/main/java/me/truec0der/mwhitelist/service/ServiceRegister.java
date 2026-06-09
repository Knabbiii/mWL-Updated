package me.truec0der.mwhitelist.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.config.ConfigRegister;
import me.truec0der.mwhitelist.impl.repository.RepositoryRegister;
import me.truec0der.mwhitelist.misc.ThreadExecutor;
import me.truec0der.mwhitelist.service.plugin.PluginReloadService;
import me.truec0der.mwhitelist.service.whitelist.WhitelistActionService;
import me.truec0der.mwhitelist.service.whitelist.WhitelistInfoService;
import me.truec0der.mwhitelist.service.whitelist.WhitelistScheduleService;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceRegister {
    final RepositoryRegister repositoryRegister;
    final ConfigRegister configRegister;
    final ThreadExecutor threadExecutor;
    @Getter
    PluginReloadService pluginReloadService;
    @Getter
    WhitelistActionService whitelistActionService;
    @Getter
    WhitelistInfoService whitelistInfoService;
    @Getter
    WhitelistScheduleService whitelistScheduleService;

    public ServiceRegister(RepositoryRegister repositoryRegister, ConfigRegister configRegister, ThreadExecutor threadExecutor) {
        this.repositoryRegister = repositoryRegister;
        this.configRegister = configRegister;
        this.threadExecutor = threadExecutor;
        init();
    }

    public void init() {
        pluginReloadService = new PluginReloadService(this, repositoryRegister, configRegister);
        whitelistActionService = new WhitelistActionService(this, repositoryRegister, configRegister, threadExecutor);
        whitelistInfoService = new WhitelistInfoService(this, repositoryRegister, configRegister, threadExecutor);
        whitelistScheduleService = new WhitelistScheduleService(this, repositoryRegister, configRegister, threadExecutor);
        whitelistScheduleService.initExecutor();
    }
}
