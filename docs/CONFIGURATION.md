# Configuration

Config file (created on first run):

`plugins/ServerDoctor/config.yml`

After editing, run **`/doctor reload`** — no full server restart required.

Invalid numbers are **clamped to safe ranges** automatically (for example, check intervals cannot go below 1 second).

---

## Health thresholds

| Key | Default | Meaning |
|-----|---------|---------|
| `memory-warning-percent` | `80` | Warn when JVM memory usage exceeds this percent |
| `entity-warning-limit` | `3000` | Warn when total entities exceed this count |
| `loaded-chunk-warning-limit` | `5000` | Warn when loaded chunks exceed this count |
| `tps-warning-threshold` | `18.0` | Warn when 1-minute TPS average is below this |
| `mspt-warning-threshold` | `50.0` | Warn when MSPT exceeds this (milliseconds per tick) |

---

## Automatic alerts

| Key | Default | Meaning |
|-----|---------|---------|
| `alerts-enabled` | `true` | Enable periodic health checks and in-game alerts |
| `alert-check-interval-seconds` | `60` | How often to check (seconds) |

Alerts use a per-metric cooldown (5 minutes). Players need `serverdoctor.alerts` to see chat warnings.

---

## Discord (optional)

| Key | Default | Meaning |
|-----|---------|---------|
| `discord-alerts-enabled` | `false` | Send health and lag spike alerts to Discord |
| `discord-webhook-url` | `""` | Webhook URL — **never shown in-game** |
| `discord-alert-title` | `ServerDoctor Alert` | Embed title prefix |

Create a webhook in Discord: **Server Settings → Integrations → Webhooks**.

---

## Chunk analyzer

| Key | Default | Meaning |
|-----|---------|---------|
| `chunk-analyzer-enabled` | `true` | Allow `/doctor chunks` |
| `chunk-analyzer-top-limit` | `5` | How many top chunks to list |
| `chunk-warning-entity-limit` | `100` | Per-chunk entity count used in scoring |
| `chunk-warning-dropped-item-limit` | `50` | Per-chunk dropped item threshold |
| `chunk-warning-hopper-limit` | `30` | Per-chunk hopper threshold |
| `chunk-teleport-enabled` | `true` | Clickable teleport links in `/doctor chunks` (still requires `serverdoctor.tpchunk`) |

---

## Smart recommendations

| Key | Default | Meaning |
|-----|---------|---------|
| `recommendations-enabled` | `true` | Show tips on report, chunks, and export |

Recommendations are **advisory only** — nothing is changed automatically except when you run `/doctor cleanup confirm`.

---

## Cleanup

```yaml
cleanup:
  enabled: true
  include-dropped-items: true
  include-hostile-mobs: false
  include-passive-mobs: false
  cooldown-seconds: 300
  log-actions: true
```

| Key | Default | Meaning |
|-----|---------|---------|
| `enabled` | `true` | Allow cleanup preview and confirm |
| `include-dropped-items` | `true` | Remove ground items on confirm |
| `include-hostile-mobs` | `false` | Remove hostile mobs on confirm |
| `include-passive-mobs` | `false` | Remove passive mobs on confirm |
| `cooldown-seconds` | `300` | Minimum time between confirm runs |
| `log-actions` | `true` | Log confirms to `plugins/ServerDoctor/logs/cleanup.log` |

**Never removed:** players, named entities, tamed animals, villagers, armor stands, item frames, minecarts, boats, boss mobs (including warden).

---

## Lag spike detection

```yaml
lag-spike-detection:
  enabled: true
  check-interval-seconds: 30
  mspt-spike-threshold: 80.0
  tps-drop-threshold: 15.0
  alert-cooldown-seconds: 300
  log-spikes: true
```

| Key | Default | Meaning |
|-----|---------|---------|
| `enabled` | `true` | Run periodic spike checks |
| `check-interval-seconds` | `30` | Check frequency (seconds) |
| `mspt-spike-threshold` | `80.0` | Spike when MSPT is above this |
| `tps-drop-threshold` | `15.0` | Spike when TPS is below this |
| `alert-cooldown-seconds` | `300` | Minimum time between spike alerts |
| `log-spikes` | `true` | Log to `plugins/ServerDoctor/logs/lag-spikes.log` |

Players with `serverdoctor.alerts` receive spike chat alerts. Discord receives spikes if webhooks are configured.

---

## Messages

Under `messages:` you can customize chat text using `&` color codes.

| Key | Placeholders |
|-----|----------------|
| `no-permission` | `{permission}` |
| `report-generated` | `{file}` |

Use `/doctor reload` after editing messages.

---

## Example: safer defaults for public survival

```yaml
cleanup:
  enabled: true
  include-dropped-items: true
  include-hostile-mobs: false
  include-passive-mobs: false
```

Keep mob cleanup off unless staff explicitly need it.

---

## Example: stricter performance alerts

```yaml
tps-warning-threshold: 19.0
mspt-warning-threshold: 40.0
lag-spike-detection:
  tps-drop-threshold: 18.0
  mspt-spike-threshold: 60.0
```

Tune for your hardware and player count.
