package me.truec0der.mwhitelist.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface WhitelistAPI {
    boolean addPlayer(UUID uuid, String name);

    boolean removePlayer(UUID uuid);

    boolean isWhitelisted(UUID uuid);

    boolean addPlayerTemp(UUID uuid, String name, long durationMs);

    /**
     * Overwrites the expiration of an existing entry to now + durationMs.
     * Returns false if the player is not whitelisted.
     */
    boolean setPlayerTemp(UUID uuid, long durationMs);

    /**
     * Extends the expiration of an existing entry by durationMs, starting from
     * the later of "now" and its current expiration. Returns false if the
     * player is not whitelisted.
     */
    boolean extendPlayerTemp(UUID uuid, long durationMs);

    Optional<WhitelistEntry> getEntry(UUID uuid);

    List<WhitelistEntry> getAll();

    int getWhitelistSize();

    boolean isWhitelistEnabled();

    boolean setWhitelistEnabled(boolean enabled);

    /**
     * Resolves the UUID for the given name according to the configured
     * whitelist mode (this may perform a Mojang API lookup), then adds it.
     * Runs off the calling thread.
     */
    CompletableFuture<Boolean> addPlayer(String name);

    /**
     * Resolves the UUID for the given name according to the configured
     * whitelist mode (this may perform a Mojang API lookup), then removes it.
     * Runs off the calling thread.
     */
    CompletableFuture<Boolean> removePlayer(String name);

    /**
     * Resolves the UUID for the given name according to the configured
     * whitelist mode (this may perform a Mojang API lookup), then checks it.
     * Runs off the calling thread.
     */
    CompletableFuture<Boolean> isWhitelisted(String name);
}
