## 0.1.4
- Speedometers and stressometers now work with tiered parts. They attach to tiered cogs, shafts and gearboxes
  and no longer break past 256 RPM. The speedometer dial still tops out at 256 RPM (Create's scale); goggles
  show the exact speed.

## 0.1.3
- Fixes clients crashing on start or when joining with 0.1.2 installed ("Itemstack ... already exists in the
  tab's list"). Redline items were being added to the creative search tab twice.

## 0.1.2
- Redline items now show up in JEI, EMI and other recipe viewers, including `@create_redline` searches.
  The creative tab was registered but empty, and viewers build their item list from creative tabs.
- The three Steam Redliner rotors (Allthemodium, Vibranium, Unobtainium) are separate entries in the creative
  tab, JEI and EMI.
- Engineer's Goggles show the speed of tiered shafts, cogwheels, gearboxes, belts and the Steam Redliner,
  next to the RPM limit for that tier. The speed turns gold above 75% of the limit.

## 0.1.1
- Works with Create 6.0.10 (previously needed 6.0.11 or newer). Servers on 6.0.10 refused to start with
  Redline installed.

## 0.1.0
First release.

- Allthemodium, Vibranium and Unobtainium shafts, cogwheels, large cogwheels and gearboxes, rated for
  1,024 / 4,096 / 16,384 RPM. Parts break past their tier's limit, and gears only connect to shafts of their
  own tier or better.
- Past 256 RPM, parts keep a smooth spin and glow brighter with every doubling of speed instead.
- Tiered belts: a belt takes the tier of the shafts it's built between. Items on belts stay at a safe speed.
- Create's own machines and generators run at any speed and pay normal stress.
- Steam Redliner: a 3x3 multiblock for the face of a superheated 3x3x3 boiler, one per boiler. It gives 4x the
  stress capacity of the steam engines it replaces. The large cog it's crafted with sets the rotor colour and
  output speed (512 / 2,048 / 8,192 RPM).
- Sheets are pressed from ingots, and a deployer upgrades shafts one tier at a time.
- Ponder scene and Create-style tooltips.
