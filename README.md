# ServerDoctor Lite

[![Version](https://img.shields.io/badge/version-0.9.1--BETA-blue)](CHANGELOG.md)
[![Platform](https://img.shields.io/badge/platform-Spigot%201.21.x–26.1.x-orange)](docs/INSTALLATION.md)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

**ServerDoctor Lite** is a Spigot plugin that helps Minecraft server owners monitor performance, spot warnings, find lag-heavy chunks, and share diagnostic reports with hosts or developers.

**Current release:** `0.9.1-BETA` (public beta)

---

## Features

- Quick health snapshot and **GOOD / WARNING / CRITICAL** status
- TPS, MSPT, memory, entity, and chunk monitoring
- Chunk analyzer with optional click-to-teleport
- Smart recommendations (advisory only)
- Safe cleanup preview and confirm
- Lag spike detection with optional Discord alerts
- Full diagnostic `.txt` export for support tickets
- Optional SpigotMC update notifications (notify only, no auto-download)
- Plugin impact scanner — `/doctor plugins` for advisory performance review hints
- Historical performance tracking — `/doctor history` with in-memory trends and optional CSV logs
- Scheduled diagnostic reports — `/doctor schedule` (optional automatic summaries to file or Discord)

**Lite edition** — no web dashboard or automatic world editing. See [CHANGELOG.md](CHANGELOG.md) for details.

---

## Requirements

- **Spigot** or **Paper** 1.21.x through 26.1.x
- **Java** 21+ (Java 25 recommended for Minecraft 26.1)

---

## Quick start

1. Download **`ServerDoctor-0.9.0-BETA.jar`** from [GitHub Releases](https://github.com/your-org/ServerDoctor/releases).
2. Place it in your server's `plugins/` folder.
3. Start or restart the server.
4. Run `/doctor about` to verify the install.
5. Run `/doctor reload` after editing `plugins/ServerDoctor/config.yml`.

---

## Documentation

| Guide | Description |
|-------|-------------|
| [Installation](docs/INSTALLATION.md) | Download, install, upgrade, build from source |
| [Commands](docs/COMMANDS.md) | Full command reference |
| [Permissions](docs/PERMISSIONS.md) | Permission nodes and staff setup |
| [Configuration](docs/CONFIGURATION.md) | All `config.yml` options |
| [Release packaging](docs/RELEASE.md) | How to publish JARs (GitHub / SpigotMC) |
| [Changelog](CHANGELOG.md) | Version history |

---

## Common commands

```text
/doctor              Quick stats
/doctor status       Overall health summary
/doctor report       Full report + recommendations
/doctor chunks       Heaviest loaded chunks
/doctor export       Diagnostic .txt report
/doctor update       SpigotMC version status
/doctor plugins      Plugin stack impact scan
/doctor history      Performance trends overview
/doctor schedule     Scheduled report status
/doctor help         Commands you can use
```

---

## Build from source

```bash
./gradlew build
```

Output: `build/libs/ServerDoctor-0.9.0-BETA.jar` (local build — attach to GitHub Releases, do not commit to Git)

---

## SpigotMC description (short)

Copy/paste starter text:

```text
ServerDoctor Lite helps you monitor server health on Spigot/Paper 1.21.x through 26.1.x.
Check TPS, MSPT, memory, entities, and loaded chunks with /doctor and /doctor status.
Find lag-heavy chunks, export diagnostic reports for your host, and get optional Discord alerts.
Safe cleanup tools let you preview entity removal before confirming.
Beta build 0.9.0-BETA — Lite edition, MIT licensed.
```

---

## License

[MIT License](LICENSE) — Copyright (c) 2026 jimm

---

## Support

- **Issues:** GitHub Issues (update repository URL when published)
- **Docs:** [docs/](docs/)

Replace `your-org/ServerDoctor` with your real GitHub path before publishing.
