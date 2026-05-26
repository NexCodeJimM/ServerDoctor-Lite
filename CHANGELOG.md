# Changelog

All notable changes to **ServerDoctor Lite** will be documented in this file.

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

---

## [0.9.1-BETA] - 2026-05-23

### Added

- **Lite Plugin Impact Scanner** — `/doctor plugins` lists installed plugins, stack category, approximate task/listener counts, Paper timings detection, and advisory “worth reviewing” notes
- Config section `plugin-impact-scanner:` with thresholds and plugin list toggle
- Permission `serverdoctor.plugins` (default: op)
- Smart recommendations on `/doctor plugins` (e.g. enable Paper timings for deeper profiling)
- **Historical performance tracking** — periodic lightweight snapshots (TPS, MSPT, memory, chunks, entities)
- **`/doctor history`**, **`/doctor history spikes`**, **`/doctor history performance`** — in-memory trends and optional CSV logs in `plugins/ServerDoctor/history/`
- Config section `history:` and permissions `serverdoctor.history`, `.history.spikes`, `.history.performance`
- **Scheduled diagnostic reports** — automatic health summaries on a timer (default: every 24 hours, disabled by default)
- **`/doctor schedule`** — view enabled status, interval, next run, and last report file
- Reports saved to `plugins/ServerDoctor/scheduled-reports/`; optional short Discord summary (not the full file)
- Config section `scheduled-reports:` and permission `serverdoctor.schedule`
- **Multi-version support** — targets Spigot/Paper `api-version: 1.21` through 26.1.x (Java 21 bytecode)

---

## [0.9.0-BETA] - Public beta

First public beta release of **ServerDoctor Lite** for Paper 26.1.x.

### Added

- **Core diagnostics** — `/doctor` quick snapshot and `/doctor report` health warnings
- **TPS / MSPT monitoring** — Paper API metrics with configurable thresholds
- **`/doctor status`** — overall GOOD / WARNING / CRITICAL summary
- **Chunk analyzer** — `/doctor chunks` ranks heaviest loaded chunks
- **Clickable chunk teleport** — `/doctor tpchunk` (players only)
- **Smart recommendations** — advisory tips on report, chunks, and export
- **Safe cleanup** — `/doctor cleanup preview` and `/doctor cleanup confirm` with entity protection
- **Discord alerts** — optional webhook notifications (URL hidden in chat)
- **Diagnostic export** — `serverdoctor-diagnostic-*.txt` full shareable reports
- **Lag spike detection** — `/doctor spikes`, alerts, and `lag-spikes.log`
- **`/doctor about`** — version and plugin information (`serverdoctor.about`, default: true)
- **SpigotMC update checker** — `/doctor update`, `/doctor update check`, startup and join notifications (notify only)
- Automatic health alerts, config reload, permission-based help and tab completion

### Fixed

- Pre-release stability review: config clamping, folder safety, permission consistency
- Console and player command edge cases, reload restarts all background services
- Discord webhook URL never exposed in chat, logs, or exports

### Repository

- MIT `LICENSE` added
- `docs/` guides: installation, commands, permissions, configuration, release packaging
- Shortened root `README.md` for GitHub and SpigotMC
- `release/` folder documented for local JAR packaging (binaries gitignored)
- Expanded `.gitignore` for reports, plugin logs, and release artifacts

---

## [0.2.0] - Internal development

Pre-beta development builds (superseded by `0.9.0-BETA`).

<details>
<summary>Development history</summary>

- Smart Recommendations Engine, Chunk Analyzer, Safe Cleanup, Lag Spike Detection
- Cleanup cooldown and logging, clickable teleport, full diagnostic export
- `/doctor status`, stability fixes, and expanded permissions

</details>

---

## [0.1.0] - Initial development

### Added

- Core `/doctor` quick server report
- `/doctor report` health report with warnings
- `/doctor alerts` automatic alert status
- `/doctor export` text report files
- `/doctor reload` config reload
- `/doctor help` permission-filtered help menu
- `/doctor discord` Discord webhook status (URL hidden in chat)
- TPS and MSPT monitoring via Paper API
- Automatic in-game alerts with per-type cooldown
- Optional Discord webhook alerts (Java `HttpClient`, async)
- Configurable thresholds in `config.yml`
- Configurable chat messages with `&` color codes
- Modern Paper `paper-plugin.yml` setup (Paper 26.1.x, Java 25)

---

[0.9.1-BETA]: #
[0.9.0-BETA]: #
[0.2.0]: #
[0.1.0]: #
