# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Debug Rules (Non-Obvious Only)

- Logs are written via `SlayTheStreamer.log(String)` which wraps Log4j - check game console or log files
- Config is stored in `LOCALAPPDATA/ModTheSpire/SlayTheStreamer/config.prefs` - not in project directory
- Twitch integration requires Twitch Integration mod to be loaded; check for `bettertwitchmod` conflicts
- Boss selection uses reflection (`AbstractDungeon.setBoss()`) - may fail silently if game updates change method signature
- Monster naming weighted random selection can be debugged by checking `SlayTheStreamer.votedTimes` and `SlayTheStreamer.usedNames` maps
- Static state in `SlayTheStreamer` (bossHidden, bossSelectScreen) persists across runs - may need manual reset during debugging
