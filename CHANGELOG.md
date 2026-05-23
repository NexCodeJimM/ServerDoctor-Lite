# Changelog

All notable changes to **ServerDoctor Lite** will be documented in this file.

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

---

## [0.2.0] - Unreleased

### Added

- **Chunk Analyzer** (`/doctor chunks`)
  - Scans all loaded chunks across loaded worlds
  - Collects entities, dropped items, mobs, tile entities, and hoppers per chunk
  - Ranks chunks by configurable heaviness score
  - Shows top N heaviest chunks with recommendations
- Permission `serverdoctor.chunks`
- Config options: `chunk-analyzer-enabled`, `chunk-analyzer-top-limit`, `chunk-warning-entity-limit`, `chunk-warning-dropped-item-limit`, `chunk-warning-hopper-limit`
- `ChunkAnalyzerService` and `ChunkAnalysisResult` classes
- `README.md` and `CHANGELOG.md` for public release preparation

### Changed

- Plugin version set to `0.2.0` in `paper-plugin.yml` and Gradle build

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
- `MessageUtil` formatted chat output (sections, status badges)
- Permissions: `serverdoctor.use`, `serverdoctor.report`, `serverdoctor.alerts`, `serverdoctor.export`, `serverdoctor.reload`
- Modern Paper `paper-plugin.yml` setup (Paper 26.1.x, Java 25)

---

[0.2.0]: #
[0.1.0]: #
