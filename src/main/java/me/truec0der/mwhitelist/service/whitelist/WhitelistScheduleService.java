package me.truec0der.mwhitelist.service.whitelist;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.config.ConfigRegister;
import me.truec0der.mwhitelist.config.configs.LangConfig;
import me.truec0der.mwhitelist.config.configs.MainConfig;
import me.truec0der.mwhitelist.impl.repository.RepositoryRegister;
import me.truec0der.mwhitelist.interfaces.repository.PlayerRepository;
import me.truec0der.mwhitelist.misc.ThreadExecutor;
import me.truec0der.mwhitelist.model.entity.database.PlayerEntity;
import me.truec0der.mwhitelist.service.Service;
import me.truec0der.mwhitelist.service.ServiceRegister;
import me.truec0der.mwhitelist.util.UUIDUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WhitelistScheduleService extends Service {
    ThreadExecutor threadExecutor;
    ScheduledExecutorService executorService;

    public WhitelistScheduleService(ServiceRegister serviceRegister, RepositoryRegister repositoryRegister, ConfigRegister configRegister, ThreadExecutor threadExecutor) {
        super(serviceRegister, repositoryRegister, configRegister);
        this.threadExecutor = threadExecutor;
    }

    public void initExecutor() {
        destroyExecutor();

        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        if (!mainConfig.getStatus() || !mainConfig.getPlayersCheckEnabled()) return;

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            try {
                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                    if (mainConfig.getBypassPermissionEnabled() && onlinePlayer.hasPermission(mainConfig.getBypassPermission()))
                        return;

                    UUID playerUuid = UUIDUtil.getUuidByMode(onlinePlayer.getName(), mainConfig.getMode());

                    Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid, mainConfig.getMode().isOnline());
                    optionalFindPlayer.ifPresentOrElse(findPlayer -> {
                        if (findPlayer.isTimeInfinity() || !findPlayer.isTimeExpired()) return;
                        Component timeExpired = langConfig.getWhitelistTimeExpired()
                                .replaceText(text -> text.match("%player_time%").replacement(findPlayer.formatTime(mainConfig.getTimeFormat())));
                        threadExecutor.runInMainThread(() -> onlinePlayer.kick(timeExpired, PlayerKickEvent.Cause.WHITELIST));
                    }, () -> {
                        threadExecutor.runInMainThread(() -> onlinePlayer.kick(langConfig.getNotInWhitelist(), PlayerKickEvent.Cause.WHITELIST));
                    });
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }, mainConfig.getPlayersCheckInitialDelay(), mainConfig.getPlayersCheckDelay(), TimeUnit.MILLISECONDS);
    }

    public void destroyExecutor() {
        if (executorService == null) return;
        executorService.shutdown();
        executorService = null;
    }
}
