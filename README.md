<div align="center">
  
![mWL Updated by Knabbiii](https://cdn.modrinth.com/data/cached_images/60783b8de3b0977ef263fd9a43bdb1ff93057374.png)

[![Downloads](https://img.shields.io/modrinth/dt/mwl-updated?style=for-the-badge&logo=modrinth&color=71ab68)](https://modrinth.com/plugin/mwl-updated)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.18.x%20%2F%2026.2-green?style=for-the-badge&logo=minecraft)](https://www.minecraft.net/)
[![Server](https://img.shields.io/badge/Server-Paper%20%7C%20Spigot%20%7C%20Bukkit-blue?style=for-the-badge)](https://github.com/Knabbiii/mWL-Updated#requirements)

</div>

![Divider](https://cdn.modrinth.com/data/cached_images/b9a20dd8fac1ec8b2ea1f4645f43314723aaa556.png)

## Overview

mWL is a whitelist plugin for Paper, Spigot, and Bukkit servers. It gives you full control over who can join, with temporary whitelist entries, permission-based bypass, and a choice of MongoDB or JSON storage. It runs on anything from 1.18.x up to Paper 26.2.

> **Note:** mWL requires your server to run in online-mode (premium/licensed accounts). Offline/cracked UUIDs are not supported.

Useful for private servers, whitelisted communities, or any server that needs more control than vanilla's built-in whitelist.

![Divider](https://cdn.modrinth.com/data/cached_images/b9a20dd8fac1ec8b2ea1f4645f43314723aaa556.png)

## Features

| Feature | Description |
|---------|-------------|
| **Temporary Whitelist** | Add players for a limited time, removed automatically on expiry |
| **Bypass Permission** | Let staff or VIPs join regardless of whitelist status |
| **Dual Database Support** | Store players in JSON (no setup) or MongoDB |
| **Expiry Notifications** | Warn players when their whitelist time is about to run out |
| **Auto-kick on Remove** | Optionally kick online players when removed from the whitelist |
| **Reload Support** | Reload config and language files without restarting |
| **Permission System** | Fine-grained access control for every command |
| **Addon API** | Public `WhitelistAPI` (`MWhitelist.getAPI()` or Bukkit's `ServicesManager`), plus custom events for other plugins to hook into |
| **Privacy-Focused** | Optional bStats metrics, can be disabled in config |

Also lightweight, with async database operations so lookups don't freeze the server, and works with LuckPerms and other permission plugins out of the box.

![Divider](https://cdn.modrinth.com/data/cached_images/b9a20dd8fac1ec8b2ea1f4645f43314723aaa556.png)

## Configuration

Some of what you can configure:
- Whitelist on/off toggle, via command or config
- Temporary whitelist with flexible time syntax (e.g. `1d12h30m`)
- Permission bypass, assignable via any permission plugin
- Periodic player check with configurable interval
- Expiry notify threshold
- MongoDB connection settings
- bStats metrics toggle (`enableMetrics: true/false`)

```yaml
whitelist:
  status: false # Whitelist enabled
  remove-on-expired: false # Remove player from database on join if their time has expired
  kick-on-remove: false # Kick player from server when removed from the whitelist via command
  bypass:
    permission:
      enabled: true # Enable bypass via permission
      permission: mwl.whitelist.bypass
  player-check: # Periodically re-checks online players and kicks them if no longer whitelisted or expired
    enabled: false
    delay: 60000 # Milliseconds between checks
  expired-notify: # Warns a player once their remaining time drops below expired-notify.time
    enabled: true
    time: 86400000 # Milliseconds before expiry
database:
  type: JSON # JSON or MONGO
```

![Divider](https://cdn.modrinth.com/data/cached_images/b9a20dd8fac1ec8b2ea1f4645f43314723aaa556.png)

## Commands & Permissions

### Commands
| Command | Description |
|---------|-------------|
| `/mwl add <player>` | Add a player to the whitelist |
| `/mwl remove <player>` | Remove a player from the whitelist |
| `/mwl addtemp <player> <time>` | Add a player temporarily |
| `/mwl settemp <player> <time>` | Set expiry time for an existing player |
| `/mwl extendtemp <player> <time>` | Extend a player's remaining time |
| `/mwl check <player>` | Check if a player is whitelisted |
| `/mwl list` | List all whitelisted players |
| `/mwl toggle [enable\|disable]` | Toggle or set whitelist status |
| `/mwl info` | Show plugin information |
| `/mwl reload` | Reload config and language files |
| `/mwl help` | Show help overview |

### Permissions
- `mwl.command.add`: add players
- `mwl.command.remove`: remove players
- `mwl.command.addtemp`: add temporary players
- `mwl.command.settemp`: set expiry time
- `mwl.command.extendtemp`: extend expiry time
- `mwl.command.check`: check whitelist status
- `mwl.command.list`: list whitelisted players
- `mwl.command.toggle`: toggle whitelist
- `mwl.command.info`: view plugin info
- `mwl.command.reload`: reload config
- `mwl.command.help`: view help
- `mwl.whitelist.bypass`: bypass whitelist check entirely

![Divider](https://cdn.modrinth.com/data/cached_images/b9a20dd8fac1ec8b2ea1f4645f43314723aaa556.png)

## Database Support

mWL supports two storage backends:

- **JSON** (default): zero setup, stores data locally in `whitelist.json`
- **MongoDB**: for larger servers or shared data setups; configure the connection URL in `config.yml`

You can switch between backends at any time by changing `database.type` in the config and restarting.

![Divider](https://cdn.modrinth.com/data/cached_images/b9a20dd8fac1ec8b2ea1f4645f43314723aaa556.png)

## Addon API

<details>
<summary>Using the WhitelistAPI in your own plugin</summary>

mWL exposes a public `WhitelistAPI` so other plugins can add, remove, and query whitelist entries without touching internal services.

Add mWL as a `depend` (or `softdepend`) in your `plugin.yml` so it's guaranteed to be loaded first:

```yaml
depend: [mWL]
```

Get the API through Bukkit's `ServicesManager`:

```java
RegisteredServiceProvider<WhitelistAPI> provider =
        Bukkit.getServicesManager().getRegistration(WhitelistAPI.class);

if (provider == null) return; // mWL not installed

WhitelistAPI whitelistApi = provider.getProvider();
```

Or, if a hard dependency on mWL's classes is fine for your use case:

```java
WhitelistAPI whitelistApi = MWhitelist.getAPI();
```

### Adding, removing, checking

```java
UUID uuid = player.getUniqueId();

whitelistApi.addPlayer(uuid, player.getName());
whitelistApi.addPlayerTemp(uuid, player.getName(), Duration.ofDays(7).toMillis());
whitelistApi.isWhitelisted(uuid);
whitelistApi.removePlayer(uuid);

whitelistApi.setPlayerTemp(uuid, Duration.ofHours(12).toMillis());   // overwrite expiry
whitelistApi.extendPlayerTemp(uuid, Duration.ofHours(12).toMillis()); // add to current expiry
```

The name-based overloads resolve the player's real Mojang UUID for you. This can call the Mojang API, so it runs off the calling thread and returns a `CompletableFuture`:

```java
whitelistApi.addPlayer("Notch").thenAccept(success -> { /* ... */ });
```

### Reading entries

```java
Optional<WhitelistEntry> entry = whitelistApi.getEntry(uuid);
entry.ifPresent(e -> {
    e.getUuid();
    e.getName();
    e.getAddedAt();     // epoch millis
    e.getExpiresAt();   // epoch millis, only meaningful if !isPermanent()
    e.isPermanent();
});

List<WhitelistEntry> all = whitelistApi.getAll();
int size = whitelistApi.getWhitelistSize();
```

### Whitelist on/off

```java
whitelistApi.isWhitelistEnabled();
whitelistApi.setWhitelistEnabled(true);
```

### Listening to events

All events live in `me.truec0der.mwhitelist.api.event` and fire on the main thread:

| Event | Fired when |
|-------|-----------|
| `PlayerWhitelistAddEvent` | A player is added (permanently or temporarily). Cancellable |
| `PlayerWhitelistRemoveEvent` | A player is removed. Cancellable |
| `PlayerWhitelistExpiredEvent` | A temp entry expires. Not cancellable |
| `WhitelistStatusChangeEvent` | The whitelist is toggled on/off. Cancellable |

```java
@EventHandler
public void onAdd(PlayerWhitelistAddEvent event) {
    getLogger().info(event.getName() + " was whitelisted (temp: " + event.isTemp() + ")");
}
```

</details>

![Divider](https://cdn.modrinth.com/data/cached_images/b9a20dd8fac1ec8b2ea1f4645f43314723aaa556.png)

## Requirements

- **Minecraft:** 1.18.x+ (compatible with 1.18.x through 1.21.x and Paper 26.2)
- **Server:** Paper, Spigot, Bukkit, or Paper forks (Purpur, etc.)
- **Java:** 17+ (Java 25+ required for Paper 26.x)

## Credits

**Original Plugin:** [mWL](https://modrinth.com/plugin/mwl) by [TRUEC0DER](https://modrinth.com/user/TRUEC0DER)

This is an updated fork, coordinated with and known to the original author, since the original plugin is now EOL (end of life) and no longer maintained. This fork adds bug fixes, broader version compatibility (1.18.x through 26.2), and additional features for modern Paper, Spigot, and Bukkit servers.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

