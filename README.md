<div align="center">

![mWL Updated by Knabbiii](https://cdn.modrinth.com/data/cached_images/60783b8de3b0977ef263fd9a43bdb1ff93057374.png)

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/mwl-updated?logo=modrinth&style=for-the-badge&label=Downloads&color=d004f7)](https://modrinth.com/plugin/mwl-updated)
[![License: MIT](https://img.shields.io/github/license/Knabbiii/mWL-Updated?color=76b469&label=License&style=for-the-badge&logo=github)](https://opensource.org/licenses/MIT)
[![GitHub release](https://img.shields.io/github/v/release/Knabbiii/mWL-Updated?style=for-the-badge&label=Release&color=d004f7&logo=github)](https://github.com/Knabbiii/mWL-Updated/releases)

</div>

> **A powerful and flexible whitelist plugin for Paper servers — with temporary entries, bypass permissions, and compatibility from 1.18.x to Paper 26.1.x.**

## Features

- **Temporary whitelist** — Add players for a limited time, auto-removed on expiry
- **Bypass permission** — Let staff or VIPs join regardless of whitelist status (`mwl.whitelist.bypass`)
- **Dual database support** — JSON (zero setup) or MongoDB
- **Expiry notifications** — Warn players when their whitelist time is running out
- **Auto-kick on remove** — Optionally kick online players when removed
- **Reload support** — Reload config and language files without restarting
- **Fine-grained permissions** — Per-command permission nodes
- **Multi-language** — Ships with English and Russian, easily customizable
- **LuckPerms compatible** — Bypass permission works seamlessly with permission plugins
- **Anonymous metrics** — Optional bStats integration (can be disabled in config)

## Installation

1. Download the latest `.jar` from the [releases page](https://github.com/Knabbiii/mWL-Updated/releases)
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/mWL/config.yml`

## Configuration

```yaml
locale: en
time-format: "dd/MM/yyyy HH:mm:ss"
whitelist:
  status: false               # Is whitelist enabled
  mode: ONLINE                # ONLINE (license UUIDs) / OFFLINE (cracked UUIDs)
  remove-on-expired: false    # Remove player from database when their time expires on join
  kick-on-remove: false       # Kick online players when removed from whitelist
  bypass:
    permission:
      enabled: true           # Allow bypass via permission node
      permission: mwl.whitelist.bypass
  player-check:
    enabled: false            # Periodically check all online players
    initial-delay: 0
    delay: 60000
  expired-notify:
    enabled: true             # Notify players when their time is running low
    time: 86400000
database:
  type: JSON                  # JSON or MONGO
  mongodb:
    url: "mongodb://admin:admin@127.0.0.1/mwl"
    name: "mwl"
```

## Commands

| Command | Description |
|---------|-------------|
| `/mwl add <player>` | Permanently add a player to the whitelist |
| `/mwl remove <player>` | Remove a player from the whitelist |
| `/mwl addtemp <player> <time>` | Temporarily add a player |
| `/mwl settemp <player> <time>` | Set expiry time for an existing entry |
| `/mwl extendtemp <player> <time>` | Extend a player's remaining time |
| `/mwl check <player>` | Check if a player is whitelisted |
| `/mwl list` | List all whitelisted players |
| `/mwl toggle [enable\|disable]` | Toggle or set whitelist status |
| `/mwl info` | Show plugin information |
| `/mwl reload` | Reload config and language files |
| `/mwl help` | Show help overview |

**Time format:** `1y 2mo 3w 4d 5h 6m 7s` (y=year, mo=month, w=week, d=day, h=hour, m=minute, s=second)

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `mwl.command.add` | Add players | op |
| `mwl.command.remove` | Remove players | op |
| `mwl.command.addtemp` | Add temporary players | op |
| `mwl.command.settemp` | Set expiry time | op |
| `mwl.command.extendtemp` | Extend expiry time | op |
| `mwl.command.check` | Check whitelist status | op |
| `mwl.command.list` | List whitelisted players | op |
| `mwl.command.toggle` | Toggle whitelist | op |
| `mwl.command.info` | View plugin info | op |
| `mwl.command.reload` | Reload config | op |
| `mwl.command.help` | View help | op |
| `mwl.whitelist.bypass` | Bypass whitelist check entirely | false |

## Requirements

- **Minecraft:** 1.18.x+ (compatible with 1.18.x – 1.21.x and Paper 26.1.x)
- **Server:** Paper or Paper forks (Purpur, etc.)
- **Java:** 17+ (Java 25+ required for Paper 26.1.x)

## Credits

**Original Developer:** [TRUEC0DER](https://modrinth.com/user/TRUEC0DER) — [Original mWL](https://modrinth.com/plugin/mwl)

This is an updated fork with bug fixes, broader version compatibility (1.18.x – 26.1.x), and improvements for modern Paper servers.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

*Made with care for the Minecraft community*
