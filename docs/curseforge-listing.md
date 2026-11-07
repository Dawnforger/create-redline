# CurseForge listing: Create: Redline

CurseForge project ID **1790136** (authors.curseforge.com/#/projects/1790136), submitted 2026-11-07 and awaiting moderation.

**Project name:** Create: Redline
**Summary (one line):** Allthemodium, Vibranium and Unobtainium kinetics that push Create past 256 RPM, plus the Steam Redliner boiler upgrade.
**Class:** Mods · **Main category:** Technology · **Additional:** Technology > Processing, Addons
**Game version:** 1.21.1 · **Loader:** NeoForge · **License:** MIT
**Source:** https://github.com/Dawnforger/create-redline · **Issues:** https://github.com/Dawnforger/create-redline/issues
**Relations:** Create (required), Allthemodium (optional)
**Allow 3rd-party distribution:** Yes (needed for modpacks)

---

## Description

**Create: Redline** is a Create addon for All The Mods-style packs. It lets your factory run past Create's 256 RPM limit, with each step gated behind the Allthemodium ores.

### Tiered kinetics
| Tier | Max RPM |
|---|---|
| Create (vanilla) | 256 |
| Allthemodium | 1,024 |
| Vibranium | 4,096 |
| Unobtainium | 16,384 |

- **Shafts, cogwheels, large cogwheels, gearboxes and belts** come in each tier. Run any part past its tier's limit and it breaks, just like vanilla Create parts past 256.
- **Gears and gearboxes only connect to shafts of their own tier or better.** Cog-to-cog meshing is always allowed, so you can gear back down to normal machines.
- **Belts take the tier of the shafts you build them between.** Items on them stay at a safe speed, so funnels and processing keep working.
- **Create's own machines and generators run at any speed** and pay the usual stress.
- **Past 256 RPM, parts glow instead of strobing.** They keep a smooth spin and get brighter with each doubling of speed.

### Steam Redliner
A 3x3 multiblock that covers a whole face of a **3x3x3 boiler with 9 Blaze Burners superheated by Blaze Cake**. It gives **4x the stress capacity** of the 9 Steam Engines it replaces, so it's an endgame upgrade for your engine stack. You can fit one per boiler.

The large cog you craft it with sets the rotor colour and output speed. Stress capacity is the same for every version:
- Allthemodium: 512 RPM
- Vibranium: 2,048 RPM
- Unobtainium: 8,192 RPM

### Progression
Press ingots into **sheets**, then use a **Deployer** to apply sheets to shafts and upgrade them one tier at a time. Every tiered part is made from tiered shafts.

Ponder scenes and Create-style tooltips are included. Hold W over the Steam Redliner.

### Requirements
- Minecraft 1.21.1, NeoForge, Create 6.0.11+
- Allthemodium (optional). Recipes use the `c:ingots/*` and `c:plates/*` tags, so other mods' metals and plates work too.

### Credits
Built on [Create](https://www.curseforge.com/minecraft/mc-mods/create) by the Create Team. Parts of the code are adapted under Create's MIT license. No Create art is included: the tier colours are generated when the game loads, from your installed Create's textures. Allthemodium, Vibranium and Unobtainium are materials from the [Allthemodium](https://www.curseforge.com/minecraft/mc-mods/allthemodium) mod by the All The Mods team.

### Source & issues
Source code (MIT) and the issue tracker: https://github.com/Dawnforger/create-redline

### AI disclosure
This mod was built with substantial help from an AI coding assistant (Claude, by Anthropic).
**Dawnforger** designed the mechanics, tiers and progression, made the balance, recipe and naming decisions,
and playtested everything in game. The AI wrote most of the code, data files, tooling and this description
under that direction. The project logo was also made with AI tools.
