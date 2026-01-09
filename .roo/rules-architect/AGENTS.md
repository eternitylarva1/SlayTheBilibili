# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Architecture Rules (Non-Obvious Only)

- The mod uses ModTheSpire's patching system with `@SpirePatch` annotations - patches modify game bytecode at runtime
- Boss selection flow: `BossChoicePatch.ChangeBossRoom` replaces treasure room with `BossSelectRoom`, which opens `BossSelectScreen` for Twitch voting
- Monster naming system uses weighted random selection stored in static maps (`SlayTheStreamer.votedTimes`, `SlayTheStreamer.usedNames`) - state persists across runs
- Twitch integration is optional but required for core functionality - patches check for `bettertwitchmod` to avoid conflicts
- Config is stored externally in `LOCALAPPDATA/ModTheSpire` - not part of the project directory
- Heavy use of reflection (`basemod.ReflectionHacks`) to access private game fields - creates fragility with game updates
- The modid "versus" vs package "chronometry" mismatch is intentional for historical reasons
