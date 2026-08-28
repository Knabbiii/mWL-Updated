package me.truec0der.mwhitelist.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WhitelistEntry {
    UUID uuid;
    String name;
    long addedAt;
    long expiresAt;
    boolean permanent;
}
