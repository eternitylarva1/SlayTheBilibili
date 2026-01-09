# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build Commands

- Build: `mvn clean package` - Compiles Java 8 code and copies JAR to Steam mods directory
- The build automatically deploys to `Steam.path/common/SlayTheSpire/mods/` (configured in pom.xml)

## Project Stack

- **Language**: Java 8
- **Build Tool**: Maven
- **Framework**: Slay the Spire mod using ModTheSpire (3.23.2) and BaseMod (5.33.1)
- **Dependencies**: System-scoped JARs from Steam workshop (slaythespire, basemod, ModTheSpire, StSLib)

## Critical Non-Obvious Patterns

- **Steam Path Required**: `pom.xml` property `Steam.path` MUST be set to your Steam installation (e.g., `C:\Program Files (x86)\steam\steamapps`) - build will fail without it
- **Mod ID**: The modid in `ModTheSpire.json` is "versus" but the actual package is "chronometry" - this mismatch is intentional
- **Config Location**: Mod configuration is stored in `LOCALAPPDATA/ModTheSpire` folder, not in the project directory
- **Twitch Integration**: Requires Twitch Integration mod to be loaded; patches check for `bettertwitchmod` to avoid conflicts
- **Boss Selection**: Custom `BossSelectScreen` uses reflection to call `AbstractDungeon.setBoss()` - this is fragile and may break with game updates
- **Monster Naming**: Uses weighted random selection based on voting history; weights calculated with formula `Math.pow((votedTimes + 15), 1.05D) / Math.pow((chosenTimes + 5), 2.5D)`
- **Localization**: Strings loaded from `SlayTheStreamer/localizations/{lang}/uiStrings.json` using BaseMod's custom string loader
- **Patching**: Uses ModTheSpire's `@SpirePatch` annotations with `Prefix`, `Postfix`, `Insert`, and `Replace` methods
- **Reflection**: Heavy use of `basemod.ReflectionHacks` to access private fields in game classes

## Code Style

- Package: `chronometry` for main classes, `chronometry.patches` for ModTheSpire patches
- Logger: Use `SlayTheStreamer.log(String)` for logging (wraps Log4j)
- Config: Access via `SlayTheStreamer.config` (SpireConfig instance)
- Static state: Many static fields in `SlayTheStreamer` (bossHidden, bossSelectScreen, usedNames, etc.)
- Patch classes: Contain inner static classes for each patch point
