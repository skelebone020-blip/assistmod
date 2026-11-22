# Assist Menu (Fabric, Minecraft 26.3)

Press **Right Shift** in-game to open the menu. Click an option to toggle it (hover for a description). Settings are saved to `config/assistmod.properties`. The key can be rebound in Options > Controls.

Assists:
- **Auto Bridge** - places a block under you when there's air (uses your held block, or the first block in your hotbar)
- **Auto Sprint** - sprints while you hold forward
- **Auto Jump** - turns on vanilla auto-jump for one-block ledges
- **Edge Guard** - sneaks at ledges so you don't walk off
- **Auto Walk** - holds the forward key
- **Auto Mine** - holds the attack/mine button
- **Fullbright** - night vision

## Build
Needs JDK 25.

    gradle wrapper --gradle-version latest   # once, if there's no gradlew yet
    ./gradlew build

Jar: `build/libs/assistmod-1.0.0.jar`. Needs Fabric Loader 0.19.5+ and Fabric API for 26.3.
Test in dev: `./gradlew runClient`.

## Adding an assist
1. Add an entry to `Assist.java`.
2. Handle it in `AssistHandler.tick()`.
The menu picks it up automatically.

Many multiplayer servers prohibit movement/placement assists. Use on singleplayer or servers that allow it.
