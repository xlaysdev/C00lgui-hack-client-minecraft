# C00LGUI — Minecraft Client

A Minecraft client project built around a customizable in-game ClickGUI.

## Project

C00LGUI is a client-side Minecraft project. This repository contains the project source and build configuration.

## Build

The exact build requirements and commands depend on the Minecraft version, mod loader, and Gradle configuration used by this checkout.

1. Install the JDK version required by the project.
2. Open a terminal in the repository root.
3. Run the Gradle build:

   ```bash
   ./gradlew build
   ```

   On Windows, use:

   ```bat
   gradlew.bat build
   ```

4. If the build succeeds, check the `build/libs/` directory for the output JAR.

## Development

- Open the repository in your preferred Java IDE.
- Import it as a Gradle project.
- Keep Minecraft, loader, and dependency versions aligned with the build configuration.

## Status

Build and runtime status should be verified against the current source before treating a JAR as release-ready.

## License

No license has been specified yet. All rights remain with the repository owner unless a license is added.
