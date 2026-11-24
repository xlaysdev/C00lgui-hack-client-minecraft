# C00LGUI — Modern Minecraft Client

A client-side Minecraft 1.21.11 / NeoForge project focused on a polished, touch-friendly ClickGUI and configurable modules.

## Features
- Modern dark ClickGUI with category navigation, module search, enabled-state indicators, and a dedicated settings workspace.
- Combat, movement, player, and visual module categories.
- Per-module settings with persistent configuration in `config/c00lgui.json`.
- Right Shift opens the ClickGUI; Escape closes it.
- Lightweight FPS HUD.
- Optional CombatHUD with local health, armor, attack cooldown, and crosshair-target health.

## Build
This project uses Java 21 and Gradle.

```bash
./gradlew build
```
The built mod JAR is written to `build/libs/`.

## Cloud builds
Every push to `main` starts the GitHub Actions build. When it finishes successfully, open the repository's **Actions** tab and download the `c00lgui-<commit>` artifact. The artifact contains the installable mod JAR (the sources archive is excluded). A manual build can also be started with **Actions → Build Minecraft mod → Run workflow**.

## Notes
A successful CI build confirms compilation and packaging, not that every feature has been validated in a live Minecraft world or on every server. Test module behavior in-game after installing the generated JAR.

## License
No license has been specified. All rights remain with the repository owner unless a license is added.
