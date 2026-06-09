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
import org.bukkit.event.player.PlayerJoinEvent;
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

    public void addPlayer(CommandSender sender, String nicknameOrUuid) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        ModeType mode = mainConfig.getWhitelist().getMode();

        boolean isUuid = UUIDUtil.isUuid(nicknameOrUuid);
        UUID uuid = isUuid ? UUID.fromString(nicknameOrUuid) : null;

        CompletableFuture.runAsync(() -> {
            UUID playerOfflineUuid = isUuid ? uuid : UUIDUtil.getOfflineUuid(nicknameOrUuid);
            UUID playerOnlineUuid = isUuid ? uuid : UUIDUtil.getOnlineUuid(nicknameOrUuid);

            UUID playerUuid = UUIDUtil.getUuidByMode(playerOfflineUuid, playerOnlineUuid, mode);

            LangConfig.Command.Add addCommand = langConfig.getCommand().getAdd();

            Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid, mode.isOnline());
            optionalFindPlayer.ifPresentOrElse(findPlayer -> {
                Component alreadyAdded = addCommand.getAlreadyAdded()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid));

                threadExecutor.runInMainThread(() -> sender.sendMessage(alreadyAdded));
            }, () -> {
                if (isUuid) {
                    playerRepository.create(uuid);
                } else {
                    playerRepository.create(nicknameOrUuid, playerOfflineUuid, playerOnlineUuid);
                }

                Component added = addCommand.getAdded()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid));

                threadExecutor.runInMainThread(() -> sender.sendMessage(added));
            });
        });
    }

    public void addPlayerTemp(CommandSender sender, String nicknameOrUuid, String[] timeArgs) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();
        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();
        ModeType modeType = mainConfig.getWhitelist().getMode();

        boolean isUuid = UUIDUtil.isUuid(nicknameOrUuid);
        UUID uuid = isUuid ? UUID.fromString(nicknameOrUuid) : null;

        CompletableFuture.runAsync(() -> {
            UUID offlineUuid = isUuid ? uuid : UUIDUtil.getOfflineUuid(nicknameOrUuid);
            UUID onlineUuid = isUuid ? uuid : UUIDUtil.getOnlineUuid(nicknameOrUuid);

            UUID playerUuid = UUIDUtil.getUuidByMode(offlineUuid, onlineUuid, modeType);

            LangConfig.Command.AddTemp addTemp = langConfig.getCommand().getAddTemp();

            Optional<PlayerEntity> optionalPlayer = playerRepository.find(playerUuid, modeType.isOnline());
            if (optionalPlayer.isPresent()) {
                Component alreadyAddedMessage = addTemp.getAlreadyAdded()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid));
                threadExecutor.runInMainThread(() -> sender.sendMessage(alreadyAddedMessage));
                return;
            }

            long additionalTime = parseTime(timeArgs, addTemp.getInvalidTime(), sender);
            if (additionalTime <= 0) return;

            long currentTime = System.currentTimeMillis();
            long expirationTime = currentTime + additionalTime;

            if (isUuid) {
                playerRepository.create(uuid);
            } else {
                playerRepository.create(nicknameOrUuid, offlineUuid, onlineUuid);
            }

            playerRepository.setTime(playerUuid, modeType.isOnline(), expirationTime);

            Component addedMessage = addTemp.getAdded()
                    .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid))
                    .replaceText(text -> text.match("%player_time%")
                            .replacement(mainConfig.getTimeFormat().format(new Date(expirationTime))));

            threadExecutor.runInMainThread(() -> sender.sendMessage(addedMessage));
        });
    }

    public void setPlayerTemp(CommandSender sender, String nicknameOrUuid, String[] timeArgs) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();
        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();
        ModeType modeType = mainConfig.getWhitelist().getMode();

        boolean isUuid = UUIDUtil.isUuid(nicknameOrUuid);
        UUID uuid = isUuid ? UUID.fromString(nicknameOrUuid) : null;

        CompletableFuture.runAsync(() -> {
            UUID offlineUuid = isUuid ? uuid : UUIDUtil.getOfflineUuid(nicknameOrUuid);
            UUID onlineUuid = isUuid ? uuid : UUIDUtil.getOnlineUuid(nicknameOrUuid);

            UUID playerUuid = UUIDUtil.getUuidByMode(offlineUuid, onlineUuid, modeType);

            LangConfig.Command.SetTemp setTemp = langConfig.getCommand().getSetTemp();

            long newTime = parseTime(timeArgs, setTemp.getInvalidTime(), sender);
            if (newTime <= 0) return;

            long currentTime = System.currentTimeMillis();

            Optional<PlayerEntity> optionalPlayer = playerRepository.find(playerUuid, modeType.isOnline());
            long newExpirationTime = currentTime + newTime;

            if (optionalPlayer.isEmpty()) {
                if (isUuid) {
                    playerRepository.create(uuid);
                } else {
                    playerRepository.create(nicknameOrUuid, offlineUuid, onlineUuid);
                }
            }

            playerRepository.setTime(playerUuid, modeType.isOnline(), newExpirationTime);

            Component extendedMessage = setTemp.getSetted()
                    .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid))
                    .replaceText(text -> text.match("%player_time%")
                            .replacement(mainConfig.getTimeFormat().format(new Date(newExpirationTime))));

            threadExecutor.runInMainThread(() -> sender.sendMessage(extendedMessage));
        });
    }

    public void extendPlayerTemp(CommandSender sender, String nicknameOrUuid, String[] timeArgs) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();
        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();
        ModeType modeType = mainConfig.getWhitelist().getMode();

        boolean isUuid = UUIDUtil.isUuid(nicknameOrUuid);
        UUID uuid = isUuid ? UUID.fromString(nicknameOrUuid) : null;

        CompletableFuture.runAsync(() -> {
            UUID offlineUuid = isUuid ? uuid : UUIDUtil.getOfflineUuid(nicknameOrUuid);
            UUID onlineUuid = isUuid ? uuid : UUIDUtil.getOnlineUuid(nicknameOrUuid);

            UUID playerUuid = UUIDUtil.getUuidByMode(offlineUuid, onlineUuid, modeType);

            LangConfig.Command.ExtendTemp extendTemp = langConfig.getCommand().getExtendTemp();

            long additionalTime = parseTime(timeArgs, extendTemp.getInvalidTime(), sender);
            if (additionalTime <= 0) return;

            long currentTime = System.currentTimeMillis();

            Optional<PlayerEntity> optionalPlayer = playerRepository.find(playerUuid, modeType.isOnline());
            long newExpirationTime;

            if (optionalPlayer.isPresent()) {
                long existingTime = optionalPlayer.get().getTime();
                newExpirationTime = Math.max(existingTime, currentTime) + additionalTime;
            } else {
                newExpirationTime = currentTime + additionalTime;

                if (isUuid) {
                    playerRepository.create(uuid);
                } else {
                    playerRepository.create(nicknameOrUuid, offlineUuid, onlineUuid);
                }
            }

            playerRepository.setTime(playerUuid, modeType.isOnline(), newExpirationTime);

            Component extendedMessage = extendTemp.getExtended()
                    .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid))
                    .replaceText(text -> text.match("%player_time%")
                            .replacement(mainConfig.getTimeFormat().format(new Date(newExpirationTime))));

            threadExecutor.runInMainThread(() -> sender.sendMessage(extendedMessage));
        });
    }

    public void removePlayer(CommandSender sender, String nicknameOrUuid) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        ModeType mode = mainConfig.getWhitelist().getMode();

        boolean isUuid = UUIDUtil.isUuid(nicknameOrUuid);
        UUID uuid = isUuid ? UUID.fromString(nicknameOrUuid) : null;

        CompletableFuture.runAsync(() -> {
            UUID playerUuid;

            if (isUuid) {
                playerUuid = uuid;
            } else {
                playerUuid = UUIDUtil.getUuidByMode(nicknameOrUuid, mode);
            }

            LangConfig.Command.Remove removeCommand = langConfig.getCommand().getRemove();

            Player player = Bukkit.getPlayer(nicknameOrUuid);

            Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid, mode.isOnline());
            optionalFindPlayer.ifPresentOrElse(findPlayer -> {
                playerRepository.remove(playerUuid, mode.isOnline());

                if (player != null && player.isOnline() && mainConfig.getWhitelist().isKickOnRemove()) {
                    if (mainConfig.getWhitelist().getBypass().getPermission().isEnabled() && player.hasPermission(mainConfig.getWhitelist().getBypass().getPermission().getPermission()))
                        return;
                    threadExecutor.runInMainThread(() -> player.kick(langConfig.getNotInWhitelist()));
                }

                Component removed = removeCommand.getRemoved()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid));

                threadExecutor.runInMainThread(() -> sender.sendMessage(removed));
            }, () -> {
                Component notIn = removeCommand.getNotIn()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid));

                threadExecutor.runInMainThread(() -> sender.sendMessage(notIn));
            });
        });
    }

    private void setWhitelistStatus(CommandSender sender, boolean status) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        LangConfig.Command.Toggle toggleCommand = langConfig.getCommand().getToggle();
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

        if (!mainConfig.getWhitelist().isStatus()) return;

        Player player = event.getPlayer();

        if (mainConfig.getWhitelist().getBypass().getPermission().isEnabled() && player.hasPermission(mainConfig.getWhitelist().getBypass().getPermission().getPermission()))
            return;

        UUID playerUuid = UUIDUtil.getUuidByMode(player.getName(), mainConfig.getWhitelist().getMode());

        Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid, mainConfig.getWhitelist().getMode().isOnline());
        optionalFindPlayer.ifPresentOrElse(findPlayer -> {
            if (findPlayer.isTimeInfinity()) return;
            if (findPlayer.isTimeExpired()) {
                Component timeExpired = langConfig.getWhitelistTimeExpired()
                        .replaceText(text -> text.match("%player_time%").replacement(findPlayer.formatTime(mainConfig.getTimeFormat())));
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, timeExpired);

                if (mainConfig.getWhitelist().isRemoveOnExpired())
                    playerRepository.remove(playerUuid, mainConfig.getWhitelist().getMode().isOnline());
            }
        }, () -> {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, langConfig.getNotInWhitelist());
        });
    }

    public void handleExpiredNotify(PlayerJoinEvent event) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        Player player = event.getPlayer();

        CompletableFuture.runAsync(() -> {
            UUID playerUuid = UUIDUtil.getUuidByMode(player.getName(), mainConfig.getWhitelist().getMode());

            Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid, mainConfig.getWhitelist().getMode().isOnline());
            optionalFindPlayer.ifPresent(findPlayer -> {
                Long estimatedTime = findPlayer.getEstimatedTime();
                Long timeToNotify = mainConfig.getWhitelist().getExpiredNotify().getTime();

                boolean isTimeExists = findPlayer.isTimeExists();
                boolean shouldSendNotify = mainConfig.getWhitelist().getExpiredNotify().isEnabled() && isTimeExists && estimatedTime <= timeToNotify;

                if (!shouldSendNotify) return;

                Component expiredNotify = langConfig.getExpiredNotify()
                        .replaceText(text -> text.match("%player_time%").replacement(findPlayer.formatTime(mainConfig.getTimeFormat())));

                threadExecutor.runInMainThread(() -> player.sendMessage(expiredNotify));
            });
        });
    }

    private long parseTime(String[] timeArgs, Component invalidTimeMessage, CommandSender sender) {
        long time = TimeUtil.parseUnit(timeArgs, 1);
        if (time == 0) {
            try {
                time = Long.parseLong(String.join("", Arrays.copyOfRange(timeArgs, 1, timeArgs.length)));
            } catch (NumberFormatException e) {
                threadExecutor.runInMainThread(() -> sender.sendMessage(invalidTimeMessage));
                return 0;
            }
        }

        if (time <= 0) {
            threadExecutor.runInMainThread(() -> sender.sendMessage(invalidTimeMessage));
            return 0;
        }

        return time;
    }
}
