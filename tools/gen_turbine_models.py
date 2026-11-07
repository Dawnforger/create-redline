"""Generate the Steam Redliner block, rotor and item models.

Usage: python3 tools/gen_turbine_models.py   (run from the project root)

Everything is authored facing UP (tank below at y<0, output shaft on top); the blockstate and the rotor visual
rotate it to the real facing. The housing spans the full 3x3 face (-16..32 px), which is also Minecraft's
element coordinate limit. Boxes are split on 16 px block boundaries so Create's textures tile at native scale
instead of being stretched.
"""
import json
import math
from pathlib import Path

OUT = Path("src/main/resources/assets/create_redline/models")

TEX = {
    "plate": "create:block/industrial_iron_block",
    "copper": "create:block/copper_casing",
    "brass": "create:block/brass_casing",
    "brass_block": "create:block/brass_block",
    "iron_top": "create:block/industrial_iron_block_top",
    "axis": "create:block/axis",
    "axis_top": "create:block/axis_top",
    "particle": "create:block/copper_casing",
}


def splits(lo, hi):
    """Cut [lo, hi] at multiples of 16 so every piece maps into one texture tile."""
    cuts = [lo] + [c for c in range(-16, 49, 16) if lo < c < hi] + [hi]
    return list(zip(cuts, cuts[1:]))


def local(lo, hi):
    base = math.floor(lo / 16) * 16
    return lo - base, hi - base


def face_uvs(f, t):
    (x1, x2), (y1, y2), (z1, z2) = local(f[0], t[0]), local(f[1], t[1]), local(f[2], t[2])
    return {
        "north": [16 - x2, 16 - y2, 16 - x1, 16 - y1],
        "south": [x1, 16 - y2, x2, 16 - y1],
        "west": [z1, 16 - y2, z2, 16 - y1],
        "east": [16 - z2, 16 - y2, 16 - z1, 16 - y1],
        "up": [x1, z1, x2, z2],
        "down": [x1, 16 - z2, x2, 16 - z1],
    }


def box(frm, to, tex, rotation=None, faces_tex=None, split=True):
    """One logical box -> list of model elements. faces_tex overrides texture per face (e.g. shaft ends)."""
    ranges = [splits(frm[i], to[i]) if split else [(frm[i], to[i])] for i in range(3)]
    out = []
    for xr in ranges[0]:
        for yr in ranges[1]:
            for zr in ranges[2]:
                f, t = [xr[0], yr[0], zr[0]], [xr[1], yr[1], zr[1]]
                uvs = face_uvs(f, t)
                el = {"from": f, "to": t, "faces": {
                    d: {"uv": uv, "texture": "#" + (faces_tex or {}).get(d, tex)} for d, uv in uvs.items()}}
                if rotation:
                    el["rotation"] = rotation
                out.append(el)
    return out


def housing():
    e = []
    # Back plate against the tank.
    e += box([-16, 0, -16], [32, 2, 32], "plate")
    # Outer copper walls.
    e += box([-16, 2, -16], [32, 16, -12], "copper")
    e += box([-16, 2, 28], [32, 16, 32], "copper")
    e += box([-16, 2, -12], [-12, 16, 28], "copper")
    e += box([28, 2, -12], [32, 16, 28], "copper")
    # Brass bezel lip framing the rotor chamber.
    e += box([-12, 13, -12], [28, 16, -8], "brass")
    e += box([-12, 13, 24], [28, 16, 28], "brass")
    e += box([-12, 13, -8], [-8, 16, 24], "brass")
    e += box([24, 13, -8], [28, 16, 24], "brass")
    # Corner bolts.
    for x, z in [(-15, -15), (27, -15), (-15, 27), (27, 27)]:
        e += box([x, 16, z], [x + 4, 17, z + 4], "brass_block")
    # Static stator vanes behind the rotor + rear bearing.
    e += box([-12, 2, 7], [28, 3, 9], "iron_top")
    e += box([7, 2, -12], [9, 3, 28], "iron_top")
    e += box([4, 2, 4], [12, 4, 12], "brass_block", split=False)
    return e


TIERS = ["allthemodium", "vibranium", "unobtainium"]


def blade_stage(y0, y1, half_len, angles, width=4):
    e = []
    lo, hi = 8 - half_len, 8 + half_len
    for along, angle in angles:
        rot = {"origin": [8, (y0 + y1) / 2, 8], "axis": "y", "angle": angle} if angle else None
        if along == "x":
            e += box([lo, y0, 8 - width / 2], [hi, y1, 8 + width / 2], "blade", rot)
        else:
            e += box([8 - width / 2, y0, lo], [8 + width / 2, y1, hi], "blade", rot)
    return e


def rotor():
    e = []
    # Front stage: 8 blades at 0/45/90/135 degrees.
    e += blade_stage(10, 12, 18, [("x", 0), ("z", 0), ("x", 45), ("z", 45)])
    # Rear stage: staggered 22.5 degrees, slightly shorter.
    e += blade_stage(6, 8, 16, [("x", 22.5), ("x", -22.5), ("z", 22.5), ("z", -22.5)])
    # Hub, nose and output shaft stub.
    e += box([5, 4, 5], [11, 13, 11], "brass_block", split=False)
    e += box([6, 13, 6], [10, 14, 10], "brass_block", split=False)
    e += box([6, 14, 6], [10, 16, 10], "axis", faces_tex={"up": "axis_top", "down": "axis_top"}, split=False)
    return e


def model(elements, extra=None, blade=None):
    m = {"textures": {**TEX, "blade": blade or TEX["plate"]}, "ambientocclusion": False, "elements": elements}
    m.update(extra or {})
    return m


ITEM_DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.3, 0.3, 0.3]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.15, 0.15, 0.15]},
    "fixed": {"rotation": [90, 0, 0], "translation": [0, 0, 0], "scale": [0.3, 0.3, 0.3]},
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.2, 0.2, 0.2]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.2, 0.2, 0.2]},
    "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 0, 0], "scale": [0.2, 0.2, 0.2]},
}


def write(rel, data):
    p = OUT / f"{rel}.json"
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(data, indent=2) + "\n")


if __name__ == "__main__":
    write("block/steam_redliner/block", model(housing()))
    write("block/steam_redliner/rotor", model(rotor()))
    write("block/steam_redliner/structure", {"textures": {"particle": TEX["particle"]}})
    # Rotor colour per crafting cog: tinted blades. The item picks its variant via the
    # create_redline:rotor item property (= KineticTier ordinal, registered in RedlineClient).
    overrides = []
    for i, tier in enumerate(TIERS, start=1):
        blade = f"create:block/industrial_iron_block_{tier}"  # runtime sprite, see gen_tier_palettes.py
        write(f"block/steam_redliner/rotor_{tier}", model(rotor(), blade=blade))
        write(f"item/steam_redliner_{tier}", model(housing() + rotor(), {"display": ITEM_DISPLAY}, blade=blade))
        overrides.append({"predicate": {"create_redline:rotor": i}, "model": f"create_redline:item/steam_redliner_{tier}"})
    write("item/steam_redliner", model(housing() + rotor(), {"display": ITEM_DISPLAY, "overrides": overrides}))
    old = OUT / "block/steam_redliner.json"
    if old.exists():
        old.unlink()
