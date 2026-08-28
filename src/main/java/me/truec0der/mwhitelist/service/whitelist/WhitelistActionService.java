package me.truec0der.mwhitelist.service.whitelist;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.api.event.PlayerWhitelistAddEvent;
import me.truec0der.mwhitelist.api.event.PlayerWhitelistExpiredEvent;
import me.truec0der.mwhitelist.api.event.PlayerWhitelistRemoveEvent;
import me.truec0der.mwhitelist.api.event.WhitelistStatusChangeEvent;
import me.truec0der.mwhitelist.config.ConfigRegister;
import me.truec0der.mwhitelist.config.configs.LangConfig;
import me.truec0der.mwhitelist.config.configs.MainConfig;
import me.truec0der.mwhitelist.impl.repository.RepositoryRegister;
import me.truec0der.mwhitelist.interfaces.repository.PlayerRepository;
import me.truec0der.mwhitelist.misc.ThreadExecutor;
import me.truec0der.mwhitelist.model.entity.database.PlayerEntity;
import me.truec0der.mwhitelist.service.Service;
import me.truec0der.mwhitelist.service.ServiceRegister;
import me.truec0der.mwhitelist.util.MessageSerializer;
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
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        boolean isUuid = UUIDUtil.isUuid(nicknameOrUuid);
        UUID uuid = isUuid ? UUID.fromString(nicknameOrUuid) : null;

        CompletableFuture.runAsync(() -> {
            UUID playerUuid = isUuid ? uuid : UUIDUtil.getOnlineUuid(nicknameOrUuid);

            if (playerUuid == null) {
                threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, langConfig.getPlayerNotFound()));
                return;
            }

            LangConfig.Command.Add addCommand = langConfig.getCommand().getAdd();

            Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid);
            optionalFindPlayer.ifPresentOrElse(findPlayer -> {
                Component alreadyAdded = addCommand.getAlreadyAdded()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid));

                threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, alreadyAdded));
            }, () -> {
                PlayerWhitelistAddEvent addEvent = new PlayerWhitelistAddEvent(playerUuid, nicknameOrUuid, false);
                Bukkit.getPluginManager().callEvent(addEvent);
                if (addEvent.isCancelled()) return;

                if (isUuid) {
                    playerRepository.create(uuid);
                } else {
                    playerRepository.create(nicknameOrUuid, playerUuid);
                }

                Component added = addCommand.getAdded()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid));

                threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, added));
            });
        });
    }

    public void addPlayerTemp(CommandSender sender, String nicknameOrUuid, String[] timeArgs) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();
        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        boolean isUuid = UUIDUtil.isUuid(nicknameOrUuid);
        UUID uuid = isUuid ? UUID.fromString(nicknameOrUuid) : null;

        CompletableFuture.runAsync(() -> {
            UUID playerUuid = isUuid ? uuid : UUIDUtil.getOnlineUuid(nicknameOrUuid);

            if (playerUuid == null) {
                threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, langConfig.getPlayerNotFound()));
                return;
            }

            LangConfig.Command.AddTemp addTemp = langConfig.getCommand().getAddTemp();

            Optional<PlayerEntity> optionalPlayer = playerRepository.find(playerUuid);
            if (optionalPlayer.isPresent()) {
                Component alreadyAddedMessage = addTemp.getAlreadyAdded()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid));
                threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, alreadyAddedMessage));
                return;
            }

            long additionalTime = parseTime(timeArgs, addTemp.getInvalidTime(), sender);
            if (additionalTime <= 0) return;

            long currentTime = System.currentTimeMillis();
            long expirationTime = currentTime + additionalTime;

            PlayerWhitelistAddEvent addEvent = new PlayerWhitelistAddEvent(playerUuid, nicknameOrUuid, true);
            Bukkit.getPluginManager().callEvent(addEvent);
            if (addEvent.isCancelled()) return;

            if (isUuid) {
                playerRepository.create(uuid);
            } else {
                playerRepository.create(nicknameOrUuid, playerUuid);
            }

            playerRepository.setTime(playerUuid, expirationTime);

            Component addedMessage = addTemp.getAdded()
                    .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid))
                    .replaceText(text -> text.match("%player_time%")
                            .replacement(mainConfig.getTimeFormat().format(new Date(expirationTime))));

            threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, addedMessage));
        });
    }

    public void setPlayerTemp(CommandSender sender, String nicknameOrUuid, String[] timeArgs) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();
        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        boolean isUuid = UUIDUtil.isUuid(nicknameOrUuid);
        UUID uuid = isUuid ? UUID.fromString(nicknameOrUuid) : null;

        CompletableFuture.runAsync(() -> {
            UUID playerUuid = isUuid ? uuid : UUIDUtil.getOnlineUuid(nicknameOrUuid);

            if (playerUuid == null) {
                threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, langConfig.getPlayerNotFound()));
                return;
            }

            LangConfig.Command.SetTemp setTemp = langConfig.getCommand().getSetTemp();

            long newTime = parseTime(timeArgs, setTemp.getInvalidTime(), sender);
            if (newTime <= 0) return;

            long currentTime = System.currentTimeMillis();

            Optional<PlayerEntity> optionalPlayer = playerRepository.find(playerUuid);
            long newExpirationTime = currentTime + newTime;

            if (optionalPlayer.isEmpty()) {
                if (isUuid) {
                    playerRepository.create(uuid);
                } else {
                    playerRepository.create(nicknameOrUuid, playerUuid);
                }
            }

            playerRepository.setTime(playerUuid, newExpirationTime);

            Component extendedMessage = setTemp.getSetted()
                    .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid))
                    .replaceText(text -> text.match("%player_time%")
                            .replacement(mainConfig.getTimeFormat().format(new Date(newExpirationTime))));

            threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, extendedMessage));
        });
    }

    public void extendPlayerTemp(CommandSender sender, String nicknameOrUuid, String[] timeArgs) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();
        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        boolean isUuid = UUIDUtil.isUuid(nicknameOrUuid);
        UUID uuid = isUuid ? UUID.fromString(nicknameOrUuid) : null;

        CompletableFuture.runAsync(() -> {
            UUID playerUuid = isUuid ? uuid : UUIDUtil.getOnlineUuid(nicknameOrUuid);

            if (playerUuid == null) {
                threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, langConfig.getPlayerNotFound()));
                return;
            }

            LangConfig.Command.ExtendTemp extendTemp = langConfig.getCommand().getExtendTemp();

            long additionalTime = parseTime(timeArgs, extendTemp.getInvalidTime(), sender);
            if (additionalTime <= 0) return;

            long currentTime = System.currentTimeMillis();

            Optional<PlayerEntity> optionalPlayer = playerRepository.find(playerUuid);
            long newExpirationTime;

            if (optionalPlayer.isPresent()) {
                long existingTime = optionalPlayer.get().getTime();
                newExpirationTime = Math.max(existingTime, currentTime) + additionalTime;
            } else {
                newExpirationTime = currentTime + additionalTime;

                if (isUuid) {
                    playerRepository.create(uuid);
                } else {
                    playerRepository.create(nicknameOrUuid, playerUuid);
                }
            }

            playerRepository.setTime(playerUuid, newExpirationTime);

            Component extendedMessage = extendTemp.getExtended()
                    .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid))
                    .replaceText(text -> text.match("%player_time%")
                            .replacement(mainConfig.getTimeFormat().format(new Date(newExpirationTime))));

            threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, extendedMessage));
        });
    }

    public void removePlayer(CommandSender sender, String nicknameOrUuid) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        boolean isUuid = UUIDUtil.isUuid(nicknameOrUuid);
        UUID uuid = isUuid ? UUID.fromString(nicknameOrUuid) : null;

        CompletableFuture.runAsync(() -> {
            UUID playerUuid = isUuid ? uuid : UUIDUtil.getOnlineUuid(nicknameOrUuid);

            if (playerUuid == null) {
                threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, langConfig.getPlayerNotFound()));
                return;
            }

            LangConfig.Command.Remove removeCommand = langConfig.getCommand().getRemove();

            Player player = Bukkit.getPlayer(nicknameOrUuid);

            Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid);
            optionalFindPlayer.ifPresentOrElse(findPlayer -> {
                PlayerWhitelistRemoveEvent removeEvent = new PlayerWhitelistRemoveEvent(playerUuid, nicknameOrUuid);
                Bukkit.getPluginManager().callEvent(removeEvent);
                if (removeEvent.isCancelled()) return;

                playerRepository.remove(playerUuid);

                if (player != null && player.isOnline() && mainConfig.getWhitelist().isKickOnRemove()) {
                    if (mainConfig.getWhitelist().getBypass().getPermission().isEnabled() && player.hasPermission(mainConfig.getWhitelist().getBypass().getPermission().getPermission()))
                        return;
                    threadExecutor.runInMainThread(() -> player.kickPlayer(MessageSerializer.serialize(langConfig.getNotInWhitelist())));
                }

                Component removed = removeCommand.getRemoved()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid));

                threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, removed));
            }, () -> {
                Component notIn = removeCommand.getNotIn()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nicknameOrUuid));

                threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, notIn));
            });
        });
    }

    private void setWhitelistStatus(CommandSender sender, boolean status) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        LangConfig.Command.Toggle toggleCommand = langConfig.getCommand().getToggle();

        WhitelistStatusChangeEvent statusChangeEvent = new WhitelistStatusChangeEvent(status);
        Bukkit.getPluginManager().callEvent(statusChangeEvent);
        if (statusChangeEvent.isCancelled()) return;

        mainConfig.setStatus(status);

        MessageSerializer.send(sender, status ? toggleCommand.getEnabled() : toggleCommand.getDisabled());
    }

    public void switchWhitelist(CommandSender sender, String action) {
        LangConfig langConfig = getConfigRegister().getLangConfig();

        if (action == null) {
            boolean current = getConfigRegister().getMainConfig().getWhitelist().isStatus();
            setWhitelistStatus(sender, !current);
            return;
        }

        switch (action) {
            case "enable":
                setWhitelistStatus(sender, true);
                break;
            case "disable":
                setWhitelistStatus(sender, false);
                break;
            default:
                MessageSerializer.send(sender, langConfig.getCommand().getToggle().getInvalidAction());
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

        UUID playerUuid = player.getUniqueId();

        Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid);
        optionalFindPlayer.ifPresentOrElse(findPlayer -> {
            playerRepository.updateNickname(playerUuid, player.getName());

            if (findPlayer.isTimeInfinity()) return;
            if (findPlayer.isTimeExpired()) {
                Bukkit.getPluginManager().callEvent(new PlayerWhitelistExpiredEvent(playerUuid, player.getName()));

                Component timeExpired = langConfig.getWhitelistTimeExpired()
                        .replaceText(text -> text.match("%player_time%").replacement(findPlayer.formatTime(mainConfig.getTimeFormat())));
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, MessageSerializer.serialize(timeExpired));

                if (mainConfig.getWhitelist().isRemoveOnExpired())
                    playerRepository.remove(playerUuid);
            }
        }, () -> {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, MessageSerializer.serialize(langConfig.getNotInWhitelist()));
        });
    }

    public void handleExpiredNotify(PlayerJoinEvent event) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        Player player = event.getPlayer();

        CompletableFuture.runAsync(() -> {
            UUID playerUuid = player.getUniqueId();

            Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(playerUuid);
            optionalFindPlayer.ifPresent(findPlayer -> {
                Long estimatedTime = findPlayer.getEstimatedTime();
                Long timeToNotify = mainConfig.getWhitelist().getExpiredNotify().getTime();

                boolean isTimeExists = findPlayer.isTimeExists();
                boolean shouldSendNotify = mainConfig.getWhitelist().getExpiredNotify().isEnabled() && isTimeExists && estimatedTime <= timeToNotify;

                if (!shouldSendNotify) return;

                Component expiredNotify = langConfig.getExpiredNotify()
                        .replaceText(text -> text.match("%player_time%").replacement(findPlayer.formatTime(mainConfig.getTimeFormat())));

                threadExecutor.runInMainThread(() -> MessageSerializer.send(player, expiredNotify));
            });
        });
    }

    private long parseTime(String[] timeArgs, Component invalidTimeMessage, CommandSender sender) {
        long time = TimeUtil.parseUnit(timeArgs, 1);
        if (time == 0) {
            try {
                time = Long.parseLong(String.join("", Arrays.copyOfRange(timeArgs, 1, timeArgs.length)));
            } catch (NumberFormatException e) {
                threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, invalidTimeMessage));
                return 0;
            }
        }

        if (time <= 0) {
            threadExecutor.runInMainThread(() -> MessageSerializer.send(sender, invalidTimeMessage));
            return 0;
        }

        return time;
    }
}
