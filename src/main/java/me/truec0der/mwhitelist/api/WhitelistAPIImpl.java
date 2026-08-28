package me.truec0der.mwhitelist.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.api.event.PlayerWhitelistAddEvent;
import me.truec0der.mwhitelist.api.event.PlayerWhitelistRemoveEvent;
import me.truec0der.mwhitelist.api.event.WhitelistStatusChangeEvent;
import me.truec0der.mwhitelist.config.ConfigRegister;
import me.truec0der.mwhitelist.impl.repository.RepositoryRegister;
import me.truec0der.mwhitelist.interfaces.repository.PlayerRepository;
import me.truec0der.mwhitelist.model.entity.database.PlayerEntity;
import me.truec0der.mwhitelist.util.UUIDUtil;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WhitelistAPIImpl implements WhitelistAPI {
    RepositoryRegister repositoryRegister;
    ConfigRegister configRegister;

    private PlayerRepository repository() {
        return repositoryRegister.getPlayerRepository();
    }

    @Override
    public boolean addPlayer(UUID uuid, String name) {
        PlayerRepository playerRepository = repository();

        if (playerRepository.isExists(uuid)) return false;

        PlayerWhitelistAddEvent addEvent = new PlayerWhitelistAddEvent(uuid, name, false);
        Bukkit.getPluginManager().callEvent(addEvent);
        if (addEvent.isCancelled()) return false;

        playerRepository.create(name, uuid);
        return true;
    }

    @Override
    public boolean removePlayer(UUID uuid) {
        PlayerRepository playerRepository = repository();

        Optional<PlayerEntity> optionalEntity = playerRepository.find(uuid);
        if (optionalEntity.isEmpty()) return false;

        PlayerWhitelistRemoveEvent removeEvent = new PlayerWhitelistRemoveEvent(uuid, latestNickname(optionalEntity.get()));
        Bukkit.getPluginManager().callEvent(removeEvent);
        if (removeEvent.isCancelled()) return false;

        playerRepository.remove(uuid);
        return true;
    }

    @Override
    public boolean isWhitelisted(UUID uuid) {
        Optional<PlayerEntity> optionalEntity = repository().find(uuid);
        return optionalEntity.isPresent() && !optionalEntity.get().isTimeExpired();
    }

    @Override
    public boolean addPlayerTemp(UUID uuid, String name, long durationMs) {
        PlayerRepository playerRepository = repository();

        if (playerRepository.isExists(uuid)) return false;

        PlayerWhitelistAddEvent addEvent = new PlayerWhitelistAddEvent(uuid, name, true);
        Bukkit.getPluginManager().callEvent(addEvent);
        if (addEvent.isCancelled()) return false;

        playerRepository.create(name, uuid);
        playerRepository.setTime(uuid, System.currentTimeMillis() + durationMs);
        return true;
    }

    @Override
    public boolean setPlayerTemp(UUID uuid, long durationMs) {
        PlayerRepository playerRepository = repository();

        if (!playerRepository.isExists(uuid)) return false;

        playerRepository.setTime(uuid, System.currentTimeMillis() + durationMs);
        return true;
    }

    @Override
    public boolean extendPlayerTemp(UUID uuid, long durationMs) {
        PlayerRepository playerRepository = repository();

        Optional<PlayerEntity> optionalEntity = playerRepository.find(uuid);
        if (optionalEntity.isEmpty()) return false;

        long currentTime = System.currentTimeMillis();
        long newExpirationTime = Math.max(optionalEntity.get().getTime(), currentTime) + durationMs;

        playerRepository.setTime(uuid, newExpirationTime);
        return true;
    }

    @Override
    public Optional<WhitelistEntry> getEntry(UUID uuid) {
        return repository().find(uuid).map(this::toEntry);
    }

    @Override
    public List<WhitelistEntry> getAll() {
        return repository().find().stream().map(this::toEntry).collect(Collectors.toList());
    }

    @Override
    public int getWhitelistSize() {
        return repository().find().size();
    }

    @Override
    public boolean isWhitelistEnabled() {
        return configRegister.getMainConfig().getWhitelist().isStatus();
    }

    @Override
    public boolean setWhitelistEnabled(boolean enabled) {
        WhitelistStatusChangeEvent statusChangeEvent = new WhitelistStatusChangeEvent(enabled);
        Bukkit.getPluginManager().callEvent(statusChangeEvent);
        if (statusChangeEvent.isCancelled()) return false;

        configRegister.getMainConfig().setStatus(enabled);
        return true;
    }

    @Override
    public CompletableFuture<Boolean> addPlayer(String name) {
        return CompletableFuture.supplyAsync(() -> UUIDUtil.getOnlineUuid(name))
                .thenApply(uuid -> uuid != null && addPlayer(uuid, name));
    }

    @Override
    public CompletableFuture<Boolean> removePlayer(String name) {
        return CompletableFuture.supplyAsync(() -> UUIDUtil.getOnlineUuid(name))
                .thenApply(uuid -> uuid != null && removePlayer(uuid));
    }

    @Override
    public CompletableFuture<Boolean> isWhitelisted(String name) {
        return CompletableFuture.supplyAsync(() -> UUIDUtil.getOnlineUuid(name))
                .thenApply(uuid -> uuid != null && isWhitelisted(uuid));
    }

    private WhitelistEntry toEntry(PlayerEntity entity) {
        return new WhitelistEntry(
                entity.getUuid(),
                latestNickname(entity),
                entity.getInfo().getLastUpdate(),
                entity.getTime(),
                entity.isTimeInfinity()
        );
    }

    private String latestNickname(PlayerEntity entity) {
        List<String> nicknameHistory = entity.getInfo().getNicknameHistory();
        return nicknameHistory.isEmpty() ? "" : nicknameHistory.get(nicknameHistory.size() - 1);
    }
}
