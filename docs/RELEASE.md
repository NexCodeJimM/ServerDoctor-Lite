# Release packaging

This document describes how to publish **ServerDoctor Lite** builds. **Do not commit `.jar` files to Git.**

---

## Suggested repository layout

```text
ServerDoctor/
├── build.gradle              # Version: 0.9.0-BETA
├── src/                      # Plugin source
├── docs/                     # User documentation
├── release/                  # Local packaging only (JARs gitignored)
│   ├── README.md             # This folder’s purpose
│   └── 0.9.0-BETA/           # Created when preparing a release (optional locally)
│       ├── ServerDoctor-0.9.0-BETA.jar
│       ├── CHANGELOG.md      # Copy or excerpt for release notes
│       └── SHA256SUMS        # Optional checksum file
├── LICENSE
├── README.md
└── CHANGELOG.md
```

The `release/` directory is for **your workflow** when uploading to GitHub Releases or SpigotMC. Contents are listed in `.gitignore` so binaries are not pushed to the repository.

---

## Build a release JAR

```bash
./gradlew clean build
```

Expected output:

`build/libs/ServerDoctor-0.9.0-BETA.jar`

Copy to a local folder (example):

```bash
mkdir -p release/0.9.0-BETA
cp build/libs/ServerDoctor-0.9.0-BETA.jar release/0.9.0-BETA/
cp CHANGELOG.md release/0.9.0-BETA/
```

Optional checksum:

```bash
cd release/0.9.0-BETA
shasum -a 256 ServerDoctor-0.9.0-BETA.jar > SHA256SUMS
```

---

## GitHub Releases

1. Tag the commit: `git tag v0.9.0-BETA`
2. Push the tag: `git push origin v0.9.0-BETA`
3. Create a **GitHub Release** from that tag.
4. Upload `ServerDoctor-0.9.0-BETA.jar` as a release asset.
5. Paste the **0.9.0-BETA** section from `CHANGELOG.md` as release notes.

---

## SpigotMC / other sites

- Use the **same JAR** as GitHub Releases.
- Link to the GitHub repository for source and issues.
- Copy short descriptions from the root `README.md`.
- Point full setup guides to `docs/INSTALLATION.md`, `docs/COMMANDS.md`, and `docs/PERMISSIONS.md`.

---

## What not to commit

| Do not commit | Reason |
|---------------|--------|
| `build/` | Gradle output |
| `release/**/*.jar` | Distribute via Releases |
| `plugins/ServerDoctor/logs/` | Runtime logs |
| `plugins/ServerDoctor/reports/` | Runtime exports |
| Local test servers (`server/`, `world/`, etc.) | Environment-specific |

Source config to ship is only: `src/main/resources/config.yml`
