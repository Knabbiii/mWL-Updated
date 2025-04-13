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
import me.truec0der.mwhitelist.model.enums.database.ModeType;
import me.truec0der.mwhitelist.service.Service;
import me.truec0der.mwhitelist.service.ServiceRegister;
import me.truec0der.mwhitelist.util.TimeUtil;
import me.truec0der.mwhitelist.util.UUIDUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WhitelistActionService extends Service {
    ThreadExecutor threadExecutor;

    public WhitelistActionService(ServiceRegister serviceRegister, RepositoryRegister repositoryRegister, ConfigRegister configRegister, ThreadExecutor threadExecutor) {
        super(serviceRegister, repositoryRegister, configRegister);
        this.threadExecutor = threadExecutor;
    }

    public void addPlayer(CommandSender sender, String nickname) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        ModeType mode = mainConfig.getMode();

        CompletableFuture.runAsync(() -> {
            UUID playerOfflineUuid = UUIDUtil.getOfflineUuid(nickname);
            UUID playerOnlineUuid = UUIDUtil.getOnlineUuid(nickname);

            UUID playerUuid = UUIDUtil.getUuidByMode(playerOfflineUuid, playerOnlineUuid, mode);

            LangConfig.CommandAdd addCommand = langConfig.getCommand().getAdd();

            Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid, mode.isOnline());
            optionalFindPlayer.ifPresentOrElse(findPlayer -> {
                Component alreadyAdded = addCommand.getAlreadyAdded()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nickname));

                sender.sendMessage(alreadyAdded);
            }, () -> {
                playerRepository.create(nickname, playerOfflineUuid, playerOnlineUuid);

                Component added = addCommand.getAdded()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nickname));

                sender.sendMessage(added);
            });
        });
    }

    public void addPlayerTemp(CommandSender sender, String nickname, String[] time) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        ModeType mode = mainConfig.getMode();

        CompletableFuture.runAsync(() -> {
            UUID playerOfflineUuid = UUIDUtil.getOfflineUuid(nickname);
            UUID playerOnlineUuid = UUIDUtil.getOnlineUuid(nickname);

            UUID playerUuid = UUIDUtil.getUuidByMode(playerOfflineUuid, playerOnlineUuid, mode);

            LangConfig.CommandAddTemp addTempCommand = langConfig.getCommand().getAddTemp();

            Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid, mode.isOnline());
            optionalFindPlayer.ifPresentOrElse(findPlayer -> {
                Component alreadyAdded = addTempCommand.getAlreadyAdded()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nickname));

                sender.sendMessage(alreadyAdded);
            }, () -> {
                long addedTime = TimeUtil.parseUnit(time, 1);

                if (addedTime == 0) {
                    try {
                        addedTime = Long.parseLong(String.join("", Arrays.copyOfRange(time, 1, time.length)));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(addTempCommand.getInvalidTime());
                        return;
                    }
                }

                long currentTime = new Date().getTime();
                long finalTime = currentTime + addedTime;

                if (finalTime <= currentTime) {
                    sender.sendMessage(addTempCommand.getInvalidTime());
                    return;
                }

                Date finalDate = new Date(finalTime);

                playerRepository.create(nickname, playerOfflineUuid, playerOnlineUuid);
                playerRepository.setTime(playerUuid, mode.isOnline(), finalTime);

                Component added = addTempCommand.getAdded()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nickname))
                        .replaceText(text -> text.match("%player_time%").replacement(mainConfig.getTimeFormat().format(finalDate)));

                sender.sendMessage(added);
            });
        });
    }

    public void removePlayer(CommandSender sender, String nickname) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        ModeType mode = mainConfig.getMode();

        CompletableFuture.runAsync(() -> {
            UUID playerUuid = UUIDUtil.getUuidByMode(nickname, mode);

            LangConfig.CommandRemove removeCommand = langConfig.getCommand().getRemove();

            Player player = Bukkit.getPlayer(nickname);

            Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid, mode.isOnline());
            optionalFindPlayer.ifPresentOrElse(findPlayer -> {
                playerRepository.remove(playerUuid, mode.isOnline());

                if (player != null && player.isOnline() && mainConfig.getKickOnRemove()) {
                    if (mainConfig.getBypassPermissionEnabled() && player.hasPermission(mainConfig.getBypassPermission()))
                        return;
                    threadExecutor.runInMainThread(() -> player.kick(langConfig.getNotInWhitelist()));
                }

                Component removed = removeCommand.getRemoved()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nickname));

                sender.sendMessage(removed);
            }, () -> {
                Component notIn = removeCommand.getNotIn()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nickname));

                sender.sendMessage(notIn);
            });
        });
    }

    private void setWhitelistStatus(CommandSender sender, boolean status) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        LangConfig.CommandToggle toggleCommand = langConfig.getCommand().getToggle();
        mainConfig.setStatus(status);

        sender.sendMessage(status ? toggleCommand.getEnabled() : toggleCommand.getDisabled());
    }

    public void switchWhitelist(CommandSender sender, String action) {
        LangConfig langConfig = getConfigRegister().getLangConfig();

        switch (action) {
            case "enable":
                setWhitelistStatus(sender, true);
                break;
            case "disable":
                setWhitelistStatus(sender, false);
                break;
            default:
                sender.sendMessage(langConfig.getCommand().getToggle().getInvalidAction());
        }
    }

    public void handleJoin(PlayerLoginEvent event) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        if (!mainConfig.getStatus()) return;

        Player player = event.getPlayer();

        if (mainConfig.getBypassPermissionEnabled() && player.hasPermission(mainConfig.getBypassPermission())) return;

        UUID playerUuid = UUIDUtil.getUuidByMode(player.getName(), mainConfig.getMode());

        Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid, mainConfig.getMode().isOnline());
        optionalFindPlayer.ifPresentOrElse(findPlayer -> {
            if (findPlayer.isTimeInfinity()) return;
            if (findPlayer.isTimeExpired()) {
                Component timeExpired = langConfig.getWhitelistTimeExpired()
                        .replaceText(text -> text.match("%player_time%").replacement(findPlayer.formatTime(mainConfig.getTimeFormat())));
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, timeExpired);

                if (mainConfig.getRemoveOnExpired())
                    playerRepository.remove(playerUuid, mainConfig.getMode().isOnline());
            }
        }, () -> {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, langConfig.getNotInWhitelist());
        });
    }
}
