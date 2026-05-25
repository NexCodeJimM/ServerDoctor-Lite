# Permissions

ServerDoctor Lite uses one permission node per feature. Defaults are suitable for small servers: players can use basic commands; staff use `op` or your permission plugin for admin tools.

---

## Permission list

| Permission | Default | Grants access to |
|------------|---------|------------------|
| `serverdoctor.use` | `true` | `/doctor`, `/doctor help` |
| `serverdoctor.about` | `true` | `/doctor about` |
| `serverdoctor.status` | `op` | `/doctor status` |
| `serverdoctor.report` | `op` | `/doctor report` |
| `serverdoctor.alerts` | `op` | `/doctor alerts`, `/doctor discord`, receive automatic health and lag spike chat alerts |
| `serverdoctor.export` | `op` | `/doctor export` |
| `serverdoctor.reload` | `op` | `/doctor reload` |
| `serverdoctor.chunks` | `op` | `/doctor chunks` |
| `serverdoctor.tpchunk` | `op` | `/doctor tpchunk`, clickable teleport in `/doctor chunks` |
| `serverdoctor.cleanup.preview` | `op` | `/doctor cleanup preview` |
| `serverdoctor.cleanup.confirm` | `op` | `/doctor cleanup confirm` |
| `serverdoctor.spikes` | `op` | `/doctor spikes` |

---

## Recommended setups

### Small survival server

- Everyone: `serverdoctor.use`, `serverdoctor.about`
- Moderators: add `serverdoctor.status`, `serverdoctor.report`
- Admins: `op` or full ServerDoctor permissions

### Large network / dedicated staff

Use LuckPerms (or similar) groups instead of `op`:

```text
serverdoctor.use
serverdoctor.about
serverdoctor.status
serverdoctor.report
serverdoctor.alerts
serverdoctor.chunks
serverdoctor.export
serverdoctor.spikes
```

Grant cleanup only to senior admins:

```text
serverdoctor.cleanup.preview
serverdoctor.cleanup.confirm
```

### Disable cleanup entirely

Either:

- Do not grant cleanup permissions, **or**
- Set `cleanup.enabled: false` in `config.yml`

---

## Alert recipients

Automatic **health** and **lag spike** chat messages are sent only to online players who have:

`serverdoctor.alerts`

Discord webhooks are separate (config + URL); they do not use in-game permissions.

---

## Notes

- Tab completion and `/doctor help` only show commands the sender is allowed to run.
- Missing permission messages can be customized under `messages:` in `config.yml`.
