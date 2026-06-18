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
| `/doctor update` | `serverdoctor.update.notify` |
| `/doctor update check` | `serverdoctor.update.check` |
| `/doctor plugins` | `serverdoctor.plugins` |
| `/doctor history` | `serverdoctor.history` |
| `/doctor history spikes` | `serverdoctor.history.spikes` |
| `/doctor history performance` | `serverdoctor.history.performance` |
| `/doctor schedule` | `serverdoctor.schedule` |
| `/doctor investigate start` | `serverdoctor.investigate` |
| `/doctor investigate stop` | `serverdoctor.investigate` |
| `/doctor investigate status` | `serverdoctor.investigate` |
| `/doctor investigate summary` | `serverdoctor.investigate` |
| `/doctor baseline create` | `serverdoctor.baseline` |
| `/doctor baseline compare` | `serverdoctor.baseline` |
| `/doctor baseline status` | `serverdoctor.baseline` |
| `/doctor baseline delete` | `serverdoctor.baseline` |
| `/doctor baseline delete confirm` | `serverdoctor.baseline` |

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

---

## `/doctor plugins`

Scans **enabled** plugins and shows an advisory impact report:

- Total installed plugins and stack category (light / moderate / heavy / very large)
- Plugin names (optional full list via config)
- Approximate scheduled task and event listener counts per plugin
- Whether Paper timings appear enabled (from `config/paper-global.yml` when detectable)
- **Worth reviewing** notes for high task/listener counts or large stacks

**Does not** claim any plugin is definitely causing lag. Uses wording like “worth reviewing” and “possible performance contributor.”

Requires `plugin-impact-scanner.enabled: true` in config.

---

## `/doctor history`

Performance history overview from in-memory samples:

- Average TPS, MSPT, and memory over the tracked period
- Recent lag spike count and cleanup run count (this server session)
- Simple trend summary (improved / stable / degraded)

Requires `history.enabled: true` in config.

---

## `/doctor history spikes`

Lists recent lag spikes detected since startup:

- Timestamp
- Severity (Mild / Moderate / Severe)
- TPS, MSPT, and memory at detection time

Shows up to the 10 most recent events.

---

## `/doctor history performance`

Rolling performance averages and trend comparison:

- All tracked samples, ~last 15 minutes, ~last 60 minutes (based on sample interval)
- Whether TPS, MSPT, and memory improved or degraded over the tracked window

---

## `/doctor schedule`

Shows scheduled diagnostic report settings:

- Enabled or disabled
- Interval in hours
- Whether files are saved and Discord summaries are enabled
- **Next** scheduled report time (this server session)
- **Last** generated report time and filename (if any)

Automatic reports use the same diagnostic content as `/doctor export`, saved under `plugins/ServerDoctor/scheduled-reports/`. Discord receives a **short summary only** when `send-to-discord` is true and Discord webhooks are configured.

Off by default — set `scheduled-reports.enabled: true` in config.

---

## `/doctor investigate`

Temporary troubleshooting sessions (in-memory only). Use when investigating lag or performance issues over a period of time.

| Subcommand | Description |
|------------|-------------|
| `start` | Begin a new session; resets counters and records who started it |
| `stop` | End the active session with a short summary |
| `status` | Whether a session is active, who started it, duration, and live tracked counts |
| `summary` | Full session summary with advisory final recommendation |

**During an active session**, ServerDoctor tracks:

- Periodic TPS/MSPT and memory samples
- Lag spike events (from lag spike detection)
- Heaviest chunk seen when you run `/doctor chunks` (if enabled in config)
- Cleanup preview and confirm usage (if enabled)
- Recommendation bullets shown in supported commands (if enabled)

**Auto-stop:** Sessions end automatically after `investigation.auto-stop-minutes` (default 30). The last summary remains available until you run `start` again.

**Wording:** Summaries use advisory language (“may indicate”, “worth reviewing”) — they do not claim an exact root cause.

Requires `investigation.enabled: true` in config.

---

## `/doctor baseline`

Save and compare a performance snapshot when your server is in a known-good state.

| Subcommand | Description |
|------------|-------------|
| `create` | Save current metrics to `plugins/ServerDoctor/baselines/default.yml` (overwrites previous) |
| `compare` | Show changes vs baseline with **improved**, **stable**, or **degraded** labels per metric |
| `status` | Whether a baseline exists and its saved values |
| `delete` | Warn before removal — does not delete until you confirm |
| `delete confirm` | Permanently remove the saved baseline file |

**Saved snapshot includes:** date/time, executor, server/Java/plugin versions, TPS, MSPT, memory %, players, worlds, chunks, entities, plugin count, and overall status.

**Compare output** includes short advisory suggestions (e.g. run `/doctor chunks` if entities rose significantly). Wording does not claim an exact root cause.

Baseline summary and comparison are included in `/doctor export` when a baseline exists.

Requires `baseline.enabled: true` in config.

---

## `/doctor update`

Shows:

- Your **current** installed version
- The **latest known** version from the last SpigotMC check (or “not checked yet”)
- Whether an **update is available**
- A clickable link to the SpigotMC resource page (when configured)

Does not download anything.

---

## `/doctor update check`

Runs a **live** SpigotMC version check asynchronously and prints the result when finished.

Requires `serverdoctor.update.check`. The network request does not block the main server thread.
