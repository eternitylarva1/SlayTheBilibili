# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Coding Rules (Non-Obvious Only)

- Always use `SlayTheStreamer.log(String)` for logging (wraps Log4j) - don't use logger directly
- Config access must go through `SlayTheStreamer.config` (SpireConfig instance) - config is stored in LOCALAPPDATA/ModTheSpire, not project directory
- When creating patches, use `@SpirePatch` with inner static classes for each patch point (see existing patches in chronometry.patches)
- Use `basemod.ReflectionHacks` to access private fields in game classes - direct field access won't work
- For boss selection, use reflection to call `AbstractDungeon.setBoss()` - this is fragile and may break with game updates
- Monster naming uses weighted random selection with formula `Math.pow((votedTimes + 15), 1.05D) / Math.pow((chosenTimes + 5), 2.5D)` - don't change this without understanding the voting system
- Localization strings must be loaded via `BaseMod.loadCustomStringsFile()` from `SlayTheStreamer/localizations/{lang}/uiStrings.json`
- Twitch integration patches must check for `bettertwitchmod` to avoid conflicts (see TwirkPatch)
- Static state in `SlayTheStreamer` (bossHidden, bossSelectScreen, usedNames, etc.) is shared across the mod - be careful with mutations
- The modid in `ModTheSpire.json` is "versus" but the package is "chronometry" - this mismatch is intentional, don't "fix" it
