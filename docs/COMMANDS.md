# Commands

All commands use the base **`/doctor`** command. Tab completion shows only subcommands you have permission to use.

**Console:** Every command works from the server console **except** `/doctor tpchunk` (players only).

---

## Quick reference

| Command | Permission |
|---------|------------|
| `/doctor` | `serverdoctor.use` |
| `/doctor about` | `serverdoctor.about` |
| `/doctor help` | `serverdoctor.use` |
| `/doctor status` | `serverdoctor.status` |
| `/doctor report` | `serverdoctor.report` |
| `/doctor alerts` | `serverdoctor.alerts` |
| `/doctor discord` | `serverdoctor.alerts` |
| `/doctor spikes` | `serverdoctor.spikes` |
| `/doctor chunks` | `serverdoctor.chunks` |
| `/doctor tpchunk <world> <chunkX> <chunkZ>` | `serverdoctor.tpchunk` |
| `/doctor cleanup preview` | `serverdoctor.cleanup.preview` |
| `/doctor cleanup confirm` | `serverdoctor.cleanup.confirm` |
| `/doctor export` | `serverdoctor.export` |
| `/doctor reload` | `serverdoctor.reload` |

---

## `/doctor`

Quick snapshot: players, memory, worlds, entities, TPS, MSPT, plugin version, and uptime.

---

## `/doctor about`

Plugin name, version, Lite edition, author, supported platform, and short description.

---

## `/doctor help`

Lists commands you are allowed to run (permission-filtered).

---

## `/doctor status`

Overall server health: **GOOD**, **WARNING**, or **CRITICAL**, plus per-metric status, latest lag spike line, and recommendation count.

---

## `/doctor report`

Full health report with warnings, optional recommendations, and latest lag spike mention when available.

---

## `/doctor alerts`

Shows whether automatic alerts are enabled, check interval, cooldown, and live metric status vs thresholds.

Players with `serverdoctor.alerts` receive in-game alert messages when limits are exceeded.

---

## `/doctor discord`

Shows Discord alert settings. The webhook URL is **never** displayed in chat (only “configured” or “not configured”).

---

## `/doctor spikes`

Lag spike detection settings, alert cooldown status, and latest spike summary if one was recorded since startup.

---

## `/doctor chunks`

Scans **all loaded chunks** on the main thread and lists the top heaviest chunks with stats and optional recommendations.

If chunk teleport is enabled and you have `serverdoctor.tpchunk`, chunk locations in chat are **clickable** (players only).

**Tip:** Run during lower traffic on large servers — the scan can cause a brief lag spike.

---

## `/doctor tpchunk <world> <chunkX> <chunkZ>`

Teleports a **player** to the center of a chunk at a safe Y level (highest block + 1).

- Disabled when `chunk-teleport-enabled: false` in config.
- Console cannot use this command.

---

## `/doctor cleanup preview`

Scans loaded worlds and shows entity counts and how many entities would match cleanup rules. **Removes nothing.**

Always run preview before confirm.

---

## `/doctor cleanup confirm`

Removes entity types enabled in config (defaults: dropped items only). Shows a warning first, then results.

Protected (never removed): players, named entities, tamed animals, villagers, armor stands, item frames, minecarts, boats, and boss mobs.

Subject to cooldown (`cleanup.cooldown-seconds` in config).

---

## `/doctor export`

Writes a read-only diagnostic `.txt` file to:

`plugins/ServerDoctor/reports/serverdoctor-diagnostic-YYYY-MM-DD-HH-mm-ss.txt`

Includes server info, performance, chunks, warnings, recommendations, and config thresholds. **Does not run cleanup.**

---

## `/doctor reload`

Reloads `config.yml` and restarts background alert and lag spike checks.
