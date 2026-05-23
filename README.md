# ServerDoctor Lite

A lightweight Paper plugin that helps server owners monitor server health, spot warnings, and find lag-heavy loaded chunks.

**This is the Lite version.** It focuses on in-game commands, config files, and optional Discord alerts. Premium features such as a web dashboard, historical metrics, and AI diagnostics are planned separately and are **not** included in Lite.

---

## Requirements

- **Paper** 26.1.x (or compatible Paper build)
- **Java 25**

---

## Features

- Quick server snapshot (`/doctor`)
- Health report with configurable warnings (`/doctor report`)
- TPS and MSPT monitoring
- Automatic in-game alerts with cooldowns (`/doctor alerts`)
- Optional Discord webhook alerts (`/doctor discord`)
- Text report export (`/doctor export`)
- Config reload (`/doctor reload`)
- **Chunk Analyzer** — top heaviest loaded chunks (`/doctor chunks`)
- Configurable messages with `&` color codes
- Permission-based commands

---

## Commands

| Command | Description |
|---------|-------------|
| `/doctor` | Quick stats: players, memory, worlds, entities, TPS, MSPT |
| `/doctor report` | Full health report with warnings |
| `/doctor alerts` | Alert settings and live metric status |
| `/doctor discord` | Discord webhook status (URL hidden) |
| `/doctor export` | Save a `.txt` report to `plugins/ServerDoctor/reports/` |
| `/doctor reload` | Reload `config.yml` |
| `/doctor chunks` | Scan loaded chunks and show the heaviest ones |
| `/doctor help` | Command list (filtered by permission) |

---

## Permissions

| Permission | Default | Access |
|------------|---------|--------|
| `serverdoctor.use` | `true` | `/doctor`, `/doctor help` |
| `serverdoctor.report` | `op` | `/doctor report` |
| `serverdoctor.alerts` | `op` | `/doctor alerts`, `/doctor discord`, receive chat alerts |
| `serverdoctor.export` | `op` | `/doctor export` |
| `serverdoctor.reload` | `op` | `/doctor reload` |
| `serverdoctor.chunks` | `op` | `/doctor chunks` |

---

## Configuration

File location after first run: `plugins/ServerDoctor/config.yml`

### Health thresholds

- `memory-warning-percent`
- `entity-warning-limit`
- `loaded-chunk-warning-limit`
- `tps-warning-threshold`
- `mspt-warning-threshold`

### Alerts

- `alerts-enabled`
- `alert-check-interval-seconds`

### Discord (optional)

- `discord-alerts-enabled`
- `discord-webhook-url`
- `discord-alert-title`

### Chunk analyzer

- `chunk-analyzer-enabled`
- `chunk-analyzer-top-limit`
- `chunk-warning-entity-limit`
- `chunk-warning-dropped-item-limit`
- `chunk-warning-hopper-limit`

### Messages

Customize chat text under `messages:` using `&` color codes. Use `/doctor reload` after edits.

---

## Installation

1. Build the plugin (see below) or download a release JAR when published.
2. Place `ServerDoctor-0.2.0-SNAPSHOT.jar` in your server's `plugins/` folder.
3. Start or restart the server.
4. Edit `plugins/ServerDoctor/config.yml` as needed.
5. Run `/doctor reload` to apply changes without a full restart.

---

## Building from source

```bash
./gradlew build
```

Output JAR: `build/libs/ServerDoctor-0.2.0-SNAPSHOT.jar`

---

## Chunk analyzer note

`/doctor chunks` scans **all currently loaded chunks** on the main thread. On very large servers this may cause a brief lag spike. Run during low traffic or after identifying lag.

This Lite version does **not** remove entities or chunks automatically.

---

## Lite vs Premium (planned)

| Lite (this plugin) | Premium (planned separately) |
|--------------------|------------------------------|
| In-game commands | Web dashboard |
| config.yml | Historical data |
| Discord webhooks | AI diagnostics |
| Chunk analyzer | Advanced automation |

---

## Support

For issues and updates, refer to your project repository or SpigotMC resource page when published.
