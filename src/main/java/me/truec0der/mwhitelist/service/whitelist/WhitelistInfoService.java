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
import org.bukkit.command.CommandSender;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WhitelistInfoService extends Service {
    ThreadExecutor threadExecutor;

    public WhitelistInfoService(ServiceRegister serviceRegister, RepositoryRegister repositoryRegister, ConfigRegister configRegister, ThreadExecutor threadExecutor) {
        super(serviceRegister, repositoryRegister, configRegister);
        this.threadExecutor = threadExecutor;
    }

    public void sendPlayerList(CommandSender sender) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        CompletableFuture.runAsync(() -> {
            LangConfig.Command.List listCommand = langConfig.getCommand().getList();

            List<PlayerEntity> findPlayers = playerRepository.find();
            AtomicReference<Component> playerList = new AtomicReference<>(Component.empty());

            findPlayers.forEach(findPlayer -> {
                List<String> nicknameHistory = findPlayer.getInfo().getNicknameHistory();
                String displayName;

                if (nicknameHistory.isEmpty()) {
                    UUID uuid = UUIDUtil.getUuidByMode(
                            findPlayer.getUuid().getOffline(),
                            findPlayer.getUuid().getOnline(),
                            mainConfig.getWhitelist().getMode()
                    );
                    String uuidStr = uuid.toString();
                    displayName = uuidStr.substring(0, 4) + "..." + uuidStr.substring(uuidStr.length() - 4);
                } else {
                    displayName = nicknameHistory.get(nicknameHistory.size() - 1);
                }

                UUID uuid = UUIDUtil.getUuidByMode(
                        findPlayer.getUuid().getOffline(),
                        findPlayer.getUuid().getOnline(),
                        mainConfig.getWhitelist().getMode()
                );

                Component formattedNickname = listCommand.getPlayer()
                        .replaceText(text -> text.match("%player_nickname%").replacement(displayName))
                        .replaceText(text -> text.match("%player_uuid%").replacement(String.valueOf(uuid)));

                playerList.set(playerList.get().append(formattedNickname));

                int findPlayerIndex = findPlayers.indexOf(findPlayer);
                if (findPlayerIndex != findPlayers.size() - 1) {
                    playerList.set(playerList.get().append(listCommand.getSeparator()));
                }
            });

            Component info = listCommand.getInfo()
                    .replaceText(text -> text.match("%list_size%").replacement(String.valueOf(findPlayers.size())))
                    .replaceText(text -> text.match("%player_list%").replacement(playerList.get()));

            Component result = playerList.get().equals(Component.empty()) ? listCommand.getEmpty() : info;
            threadExecutor.runInMainThread(() -> sender.sendMessage(result));
        });
    }

    public void sendPlayerInfo(CommandSender sender, String nickname) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        CompletableFuture.runAsync(() -> {
            LangConfig.Command.Check checkCommand = langConfig.getCommand().getCheck();

            UUID uuid = UUIDUtil.getUuidByMode(nickname, mainConfig.getWhitelist().getMode());

            Optional<PlayerEntity> optionalFindPlayer = playerRepository.find(uuid, mainConfig.getWhitelist().getMode().isOnline());

            optionalFindPlayer.ifPresentOrElse(findPlayer -> {
                AtomicReference<Component> nicknameHistoryJoined = new AtomicReference<>(Component.empty());
                List<String> nicknameHistory = findPlayer.getInfo().getNicknameHistory();

                nicknameHistory.forEach(nicknameLine -> {
                    Component formattedNickname = checkCommand.getNicknameHistory().getNickname()
                            .replaceText(text -> text.match("%recorded_nickname%").replacement(nickname));

                    nicknameHistoryJoined.set(nicknameHistoryJoined.get().append(formattedNickname));

                    int findNicknameIndex = nicknameHistory.indexOf(nickname);
                    if (findNicknameIndex != nicknameHistory.size() - 1) {
                        nicknameHistoryJoined.set(nicknameHistoryJoined.get().append(checkCommand.getNicknameHistory().getSeparator()));
                    }
                });

                Date playerLastUpdate = new Date(findPlayer.getInfo().getLastUpdate());
                String formattedPlayerLastUpdate = mainConfig.getTimeFormat().format(playerLastUpdate);

                Component formattedPlayerDate = Component.text(findPlayer.formatTime(mainConfig.getTimeFormat()));

                Component playerTimeAbout = findPlayer.isTimeExpired() ? checkCommand.getTime().getExpired().getAbout() : findPlayer.isTimeInfinity() ? checkCommand.getTime().getInfinity().getAbout() : checkCommand.getTime().getActive().getAbout();

                Component info = checkCommand.getInfo()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nickname))
                        .replaceText(text -> text.match("%player_offline_uuid%").replacement(String.valueOf(findPlayer.getUuid().getOffline())))
                        .replaceText(text -> text.match("%player_online_uuid%").replacement(String.valueOf(findPlayer.getUuid().getOnline())))
                        .replaceText(text -> text.match("%player_nickname_list%").replacement(nicknameHistoryJoined.get()))
                        .replaceText(text -> text.match("%player_last_update%").replacement(formattedPlayerLastUpdate))
                        .replaceText(text -> text.match("%player_time%").replacement(findPlayer.isTimeInfinity() ? checkCommand.getTime().getInfinity().getInfo() : formattedPlayerDate))
                        .replaceText(text -> text.match("%player_time_about%").replacement(playerTimeAbout));

                threadExecutor.runInMainThread(() -> sender.sendMessage(info));
            }, () -> {
                Component notIn = checkCommand.getNotIn()
                        .replaceText(text -> text.match("%player_nickname%").replacement(nickname));

                threadExecutor.runInMainThread(() -> sender.sendMessage(notIn));
            });
        });
    }

    public void sendWhitelistInfo(CommandSender sender) {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        LangConfig langConfig = getConfigRegister().getLangConfig();

        PlayerRepository playerRepository = getRepositoryRegister().getPlayerRepository();

        CompletableFuture.runAsync(() -> {
            LangConfig.Command.Info infoCommand = langConfig.getCommand().getInfo();

            boolean status = mainConfig.getWhitelist().isStatus();
            List<PlayerEntity> findPlayers = playerRepository.find();

            Component info = infoCommand.getInfo()
                    .replaceText(text -> text.match("%whitelist_database%").replacement(mainConfig.getDatabase().getType().toString()))
                    .replaceText(text -> text.match("%whitelist_mode%").replacement(mainConfig.getWhitelist().getMode().toString()))
                    .replaceText(text -> text.match("%whitelist_status%").replacement(status ? infoCommand.getStatus().getEnabled() : infoCommand.getStatus().getDisabled()))
                    .replaceText(text -> text.match("%whitelist_size%").replacement(String.valueOf(findPlayers.size())));

            threadExecutor.runInMainThread(() -> sender.sendMessage(info));
        });
    }

    public void sendHelp(CommandSender sender) {
        LangConfig langConfig = getConfigRegister().getLangConfig();
        sender.sendMessage(langConfig.getCommand().getHelp().getInfo());
    }

    public List<String> getWhitelistNicknames() {
        return getRepositoryRegister().getPlayerRepository().find().stream().map(player -> {
            List<String> nicknameHistory = player.getInfo().getNicknameHistory();
            if (nicknameHistory.isEmpty()) return "";
            return nicknameHistory.get(nicknameHistory.size() - 1);
        }).collect(Collectors.toList());
    }
}
