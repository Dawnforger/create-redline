# Create: Redline

[MIT License](LICENSE) © 2026 Dawnforger

Create addon for All The Mods packs. Allthemodium, Vibranium and Unobtainium shafts, cogwheels and gearboxes
rated for 1,024 / 4,096 / 16,384 RPM, plus a Steam Redliner that turns a superheated 3×3×3 boiler into 4× the
stress capacity of the steam engines it replaces.

See [GAMEPLAN.md](GAMEPLAN.md) for the design spec, code map, dev phases and release process.

```bash
./gradlew runClient     # dev client
./gradlew runData       # loot tables / tags
./gradlew build         # jar in build/libs
```

## AI disclosure

This mod was built with substantial help from an AI coding assistant (Claude, by Anthropic).
**Dawnforger** designed the mechanics, tiers and progression, made the balance, recipe and naming decisions,
and playtested everything in game. The AI wrote most of the code, data files, tooling and this description
under that direction. The project logo was also made with AI tools.

## Credits & licensing

- Built on [Create](https://github.com/Creators-of-Create/Create) by the Create Team. Some classes are adapted
  from Create's MIT-licensed code, see [LICENSES/Create-MIT.txt](LICENSES/Create-MIT.txt).
- Create's art assets are All Rights Reserved and none are included. The tier colours are generated when the game
  loads, by recolouring the textures from *your* installed Create with Minecraft's `paletted_permutations` atlas
  source. The mod ships only colour palettes (`tools/gen_tier_palettes.py`).
- Allthemodium, Vibranium and Unobtainium are materials from the [Allthemodium](https://github.com/AllTheMods/Allthemodium)
  mod by the All The Mods team. This mod only refers to their ingot tags and contains none of their code or assets.
