package me.truec0der.mwhitelist.api.event;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
public class PlayerWhitelistExpiredEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID uuid;
    private final String name;

    public PlayerWhitelistExpiredEvent(UUID uuid, String name) {
        super(!Bukkit.isPrimaryThread());
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
