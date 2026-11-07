"""Generate tier palettes + the block-atlas source that recolours Create's textures at load time.

Create's textures are All Rights Reserved, so the mod ships no recoloured copies. Instead Minecraft's
`paletted_permutations` atlas source (the armour-trim mechanism) rebuilds them from the player's own Create
install. We ship only:
  - a palette *key* per texture: the list of colours that texture uses (no image data), and
  - one palette per tier: the replacement colour for each key colour, from our colour ramps.
Sprites come out as e.g. `create:block/cogwheel_vibranium`.

Usage: python3 tools/gen_tier_palettes.py [path/to/create.jar]   (run from the project root)
Re-run after a Create update that changes these textures, or after tweaking TIERS / SOURCES.
"""
import io
import json
import sys
import zipfile
from pathlib import Path

from PIL import Image

ASSETS = Path("src/main/resources/assets")
PALETTES = ASSETS / "create_redline/textures/palettes"
ATLAS = ASSETS / "minecraft/atlases/blocks.json"

# (shadow, mid, highlight) per tier
TIERS = {
    "allthemodium": ((90, 50, 10), (232, 176, 46), (255, 240, 160)),
    "vibranium": ((10, 70, 40), (52, 206, 120), (190, 255, 215)),
    "unobtainium": ((55, 15, 80), (176, 75, 224), (240, 190, 255)),
}
# Create texture -> how strongly to recolour (1 = full ramp, lower keeps some of the original grey/wood)
SOURCES = {
    "block/cogwheel": 1.0, "block/large_cogwheel": 1.0, "block/gearbox": 1.0,
    "block/cogwheel_axis": 0.6, "block/axis": 0.6, "block/axis_top": 0.6, "block/andesite_casing": 0.45,
    "block/industrial_iron_block": 0.85,  # Steam Redliner rotor blades
    "item/iron_sheet": 1.0,  # tier sheets
}


def lerp(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def ramp(colors, v):
    lo, mid, hi = colors
    return lerp(lo, mid, v * 2) if v < 0.5 else lerp(mid, hi, (v - 0.5) * 2)


def luminance(rgb):
    r, g, b = rgb
    return 0.299 * r + 0.587 * g + 0.114 * b


def find_create_jar():
    caches = Path.home() / ".gradle/caches/modules-2/files-2.1/com.simibubi.create"
    jars = sorted(p for p in caches.rglob("create-*.jar") if "sources" not in p.name)
    if not jars:
        sys.exit("Create jar not found; pass its path as the first argument")
    return jars[-1]


def strip(colors):
    img = Image.new("RGBA", (len(colors), 1))
    img.putdata([(*c, 255) for c in colors])
    return img


def main(jar):
    atlas_sources = []
    with zipfile.ZipFile(jar) as z:
        for name, strength in SOURCES.items():
            img = Image.open(io.BytesIO(z.read(f"assets/create/textures/{name}.png"))).convert("RGBA")
            pixels = img.get_flattened_data() if hasattr(img, "get_flattened_data") else img.getdata()
            key = sorted({(r, g, b) for r, g, b, a in pixels if a > 0})
            lums = [luminance(c) for c in key]
            lmin, span = min(lums), max(max(lums) - min(lums), 1)

            short = name.split("/")[1]
            out = PALETTES / short
            out.mkdir(parents=True, exist_ok=True)
            strip(key).save(out / "key.png")
            permutations = {}
            for tier, colors in TIERS.items():
                mapped = []
                for c in key:
                    target = ramp(colors, (luminance(c) - lmin) / span)
                    mapped.append(tuple(round(o + (t - o) * strength) for o, t in zip(c, target)))
                strip(mapped).save(out / f"{tier}.png")
                permutations[tier] = f"create_redline:palettes/{short}/{tier}"
            atlas_sources.append({
                "type": "paletted_permutations",
                "textures": [f"create:{name}"],
                "palette_key": f"create_redline:palettes/{short}/key",
                "permutations": permutations,
            })
            print(f"{name}: {len(key)} colours")

    ATLAS.parent.mkdir(parents=True, exist_ok=True)
    ATLAS.write_text(json.dumps({"sources": atlas_sources}, indent=2) + "\n")


if __name__ == "__main__":
    main(Path(sys.argv[1]) if len(sys.argv) > 1 else find_create_jar())
