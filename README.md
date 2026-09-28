# Villager News Addon Port — NeoForge 1.21.1

An unofficial NeoForge port of the **Villager News Addon Port** for Minecraft Java Edition. This workspace targets **Minecraft 1.21.1**, **NeoForge 21.1.x**, and **Java 21** while preserving the original Villager News models, textures, animations, audio, dialogue data, and contextual behavior where the 1.21.1 API permits it. Made entirely with AI (Chat GPT).

Current source version: **1.3.6**.

## Features

* Detailed animated Villager News models converted for Entity Model Features
* Biome, profession, and profession-level villager textures
* The Mayor, Testificate Man, Villager Number 5, Villager Number 9, and Villager Unreachable as named characters
* Wooly the Sheep and the Villager News wandering trader
* 2,212 original voice clips across 523 dialogue groups
* 22 original short reaction effects, including synchronized villager and wandering-trader hurt effects
* Context-aware dialogue for player actions, nearby mobs, weather, dimensions, combat, trading, work, sleep, spawning, growth, and other world events
* Multi-part conversations between nearby villagers
* Facial expressions and gestures synchronized with each voice line through EMF animation variables
* Server-controlled dialogue selection, sound playback, cooldowns, and villager behavior
* Speakers look toward the player, entity, block, or villager they are talking about
* Removable villager noses, character cosmetics, cosmetic reactions, and missing-nose conversations
* Character trades for the Mayor Hat, Testificate Man Helmet, Moustache, and Microphone
* Persistent natural spawning for one of each special character in distant villages
* A craftable Villager News Handbook with searchable dialogue guidance and settings
* Fresh Animation compatibility, aswell as it's addon by prioritizing Villager News over them
* Compatible with Villager Retaliation

## Requirements

* Minecraft Java Edition 1.21.1
* NeoForge 21.1.x
* Java 21
* Entity Model Features 3.3.9 for NeoForge 1.21.1
* Entity Texture Features 7.0.5 for NeoForge 1.21.1
* Entity Sound Features 0.8.2 for NeoForge 1.21.1

EMF, ETF, and ESF are external dependencies. They are not bundled or modified by this project.

Placeholder API is not used by the current Java source and is therefore not required.

## Installation

Put the Villager News Addon Port jar together with the matching NeoForge builds of EMF, ETF, and ESF in the Minecraft `mods` folder.

For multiplayer, install the mod and its required dependencies on both the server and every connecting client. Dialogue selection and the authoritative villager state are handled server-side, while models, animations, textures, subtitles, and sound playback are client-side.

## Characters

Use a name tag on a villager to select a character model and voice:

|Name tag|Character|
|-|-|
|`Mayor`, `Mayor Villager`, or `The Mayor`|Mayor Villager|
|`Testificate Man`|Testificate Man|
|`Villager Number 5` or `Villager #5`|Villager Number 5|
|`Villager Number 9` or `Villager #9`|Villager Number 9|
|`Villager Unreachable` or `Can't Catch Me!`|Villager Unreachable|

Name a sheep `Wooly` or `Wooly The Sheep` to use Wooly's model(**Broken** atm), animations, and sounds. Ordinary villagers and wandering traders receive their Villager News appearance and dialogue automatically.

Special characters can also appear naturally as new distant villages are generated. Each character appears once at a time and becomes eligible to spawn again after being killed.

## Items

All custom items are available in the **Villager News** creative-mode tab.

Craft the Villager News Handbook from three pieces of paper. It includes the add-on overview, character and cosmetic guides, settings reference, social/support pages, and the searchable Triggers \& Reactions guide.

Shear an adult villager to remove its nose. Interact with that villager while holding the nose to return it. The Mayor, Testificate Man, Villager #5, and Villager #9 sell their matching cosmetics. Cosmetics can be given to ordinary villagers and removed again with shears.

Minecraft 1.21.1 uses the classic `models/item` item-model format. The source still retains the original held/worn model assets for reference, but the newer `assets/<modid>/items/\\\\\\\*.json` display-context selector format from Minecraft 26.x is not used by the 1.21.1 runtime.

## Dialogue

Villagers react to what happens around them. They can comment when a player approaches, stares, changes game mode, wears armor, receives an effect, breaks or places a block, uses an item, completes a trade, or spawns a villager with a spawn egg. They also react to profession, workstation, level, biome, weather, time of day, nearby entities, damage source, sleep/wake events, and other villagers.

The server chooses the exact voice variant and broadcasts its matching animation. Each speaker remains occupied for the real length of the clip, preventing unrelated lines from overlapping. Conversation partners take turns and continue looking at each other throughout multi-part exchanges.

## Building from source

On Windows:

```powershell
.\\\\\\\\gradlew.bat build
```

On Linux or macOS:

```bash
./gradlew build
```

The distributable jar is written to `build/libs`.

To include the operator-only dialogue test command in a development build, set `dialogue\\\\\\\_test\\\\\\\_command=true` in `src/main/resources/villager-news-addon-port-build.properties` (or the corresponding generated build setting) before building. Use `/dialoguetest <1-523>` in game to spawn the matching speaker and subject, play the selected dialogue variant, and remove the test actors afterwards. Use `/dialoguetest continuous` to run all groups in order.

Run the asset and dialogue verification with:

```powershell
node tools/verify-port.mjs
```

## Porting notes

The port deliberately does not try to emulate Minecraft 26.x's submitted-render pipeline. In 1.21.1 the Villager News sign is rendered through a normal entity `RenderLayer`, and the 26.x render-state/extractor classes are not part of the port.

The 26.3 Pale Oak sign is unavailable in Minecraft 1.21.1. Sign serialization therefore uses an explicit schema marker and handles legacy 26.3 sign indices when loading old data. The active 1.21.1 sign palette contains 11 supported standing-sign types.

Likewise, the 26.3-specific `SULFUR\\\\\\\_CUBE\\\\\\\_HOT` damage type does not exist in 1.21.1. Magma-cube damage is handled through the attacker/direct entity type instead.

## Credits

Villager News and the original add-on assets were created by **Oreville Studios Ltd** and **Element Animation**. The converted models, textures, animations, and audio remain the property of their respective owners. See `LICENSE` for repository licensing details.

