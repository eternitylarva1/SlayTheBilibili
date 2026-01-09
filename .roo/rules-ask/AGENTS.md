# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Documentation Rules (Non-Obvious Only)

- The modid in `ModTheSpire.json` is "versus" but the actual Java package is "chronometry" - this is intentional
- Config is stored in `LOCALAPPDATA/ModTheSpire/SlayTheStreamer/config.prefs`, not in the project directory
- Localization files are in `SlayTheStreamer/localizations/{lang}/uiStrings.json` - loaded via BaseMod's custom string loader
- The mod requires Twitch Integration mod to function - patches check for `bettertwitchmod` to avoid conflicts
- Boss selection uses a custom `BossSelectScreen` that replaces the treasure room before boss fights
- Monster naming uses a weighted random formula based on voting history: `Math.pow((votedTimes + 15), 1.05D) / Math.pow((chosenTimes + 5), 2.5D)`
