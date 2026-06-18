# Installation

ServerDoctor Lite **0.9.2-BETA** for **Spigot/Paper 1.21.x through 26.2.x**.

---

## Requirements

| Requirement | Version |
|-------------|---------|
| Server software | Spigot or Paper **1.21.x** through **26.2.x** (tested targets: Spigot/Paper **26.1.x**, **26.2.x**) |
| Java | **21+** (use **Java 25** on Minecraft 26.1+ servers) |

Paper is supported and recommended for timings and richer chat features. Other forks are not officially tested.

### Spigot compatibility note

ServerDoctor Lite compiles against the **Spigot API** (`api-version: 1.21`) and avoids Paper-only compile dependencies. TPS/MSPT use reflection with safe fallbacks on older builds.

The only Paper-specific feature is **optional timings detection** on `/doctor plugins`: it reads `config/paper-global.yml` if present and reports “not detectable” on pure Spigot. No Paper API is required at runtime.

---

## Download

1. Open the [GitHub Releases](https://github.com/your-org/ServerDoctor/releases) page for this project.
2. Download **`ServerDoctor-0.9.0-BETA.jar`** from the latest release (do not use random JARs from `build/libs/` unless you built them yourself).

---

## Install on your server

1. Stop the server (recommended for a clean first install).
2. Copy `ServerDoctor-0.9.0-BETA.jar` into your server's `plugins/` folder.
3. Start the server.
4. Confirm the console shows ServerDoctor enabled without errors.
5. Run `/doctor about` in-game or from the console to verify the version.

On first run, the plugin creates:

- `plugins/ServerDoctor/config.yml`
- `plugins/ServerDoctor/reports/` (when you export)
- `plugins/ServerDoctor/logs/` (when cleanup or lag spike logging runs)

---

## Build from source (optional)

For developers or CI builds:

```bash
git clone https://github.com/your-org/ServerDoctor.git
cd ServerDoctor
./gradlew build
```

Output (local only, not committed to Git):

`build/libs/ServerDoctor-0.9.0-BETA.jar`

---

## After install

1. Edit `plugins/ServerDoctor/config.yml` for your server size and play style.
2. Run `/doctor reload` to apply changes without a full restart.
3. Grant permissions to staff (see [PERMISSIONS.md](PERMISSIONS.md)).
4. Run `/doctor status` for a quick health check.

---

## Upgrading from a previous test build

1. Replace the old JAR in `plugins/`.
2. Restart the server or use a plugin manager reload if you prefer (restart is safer for beta).
3. Compare your existing `config.yml` with the new defaults in the repository — new sections may need to be merged manually.
4. Run `/doctor reload`.

---

## SpigotMC / resource page

When publishing on SpigotMC or similar sites, attach the same JAR from GitHub Releases and link to this repository for source, issues, and full documentation.
