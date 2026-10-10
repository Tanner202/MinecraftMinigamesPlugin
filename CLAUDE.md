Do not restructure code, rename things, or add features beyond what I asked for. If you think a bigger change is worthwhile, suggest it and let me decide.

## Response Style

Keep responses short but descriptive. After making changes, always tell me:

- **Overview:** a few sentences on what changed and why
- **Files:** every file you created, modified, or deleted, with a one-line note on each
- **Anything to check:** new behavior, edge cases, or things I should test in-game

Skip filler, long preambles, and restating my request. Don't leave out any changed file, even for small edits.

## Code Guidelines

- Follow the existing code style and structure of the project.
- Keep changes small and focused.
- Explain any non-obvious logic so I can understand and maintain it myself.

## Project Info

- Spigot minigames plugin. Maven, Java 8 source level, Spigot API `1.21.8-R0.1-SNAPSHOT` (`remapped-mojang`). Package root: `com.tanner.minigames`.
- Games are listed in the `GameType` enum (COLORSWAP, DRAGON_ESCAPE, SPLEEF, TNT_WARS). Each game extends `Game`, and `Arena` creates it with a `switch` on the type.
- Kits live in `kit/` (`KitType` enums, implementations under `kit/kittype/<game>/`).
- Arenas are defined in `config.yml` and loaded by `ArenaManager`; invalid entries are skipped with a warning.
- Commands (`/arena`, `/party`, `/setdragonwaypoints`) are registered in `Minigames.onEnable` and declared in `plugin.yml`.

## Build

- The `package` phase copies the jar to `$PLUGIN_OUTPUT_DIR`, so that env var must be set.
- Needs Spigot installed locally via BuildTools with `--remapped`. Claude's cloud environment doesn't have it, so Claude can't compile or test; changes must be tested in-game.
