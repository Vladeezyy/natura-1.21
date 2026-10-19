#!/usr/bin/env python3
"""Turns the 1.21.1 resources (tools/gen_resources.py on main) into the 26.x formats, in place:

    python3 tools/convert_26.py common/src/main/resources [vanilla-1.21.1-client-extra.jar]   (needs Pillow, so no -I)

- recipes: ingredients are strings ("minecraft:x", "#tag") instead of {"item": ...} / {"tag": ...}; a choice of
  several tags (the berry medley) becomes an item tag of its own (data/natura/tags/item/recipe);
- every item needs a client item definition (assets/natura/items/<id>.json): the leaves and colored grass items get
  the 1.12 static colours as constant tints (1.21.1: item colour handlers), the bows the "pulling" model while used
  (1.21.1: item properties; 1.12 only ever showed the first pulling frame);
- spawn eggs: no more two-colour template, their textures are drawn from the 1.21.1 egg textures and the 1.12 colours
  (the jar argument; default: the main worktree's ../natura-1.21 build);
- imp armor: an equipment asset (assets/natura/equipment/imp.json) with the layer textures moved to
  textures/entity/equipment;
- the repair tags of the bloodwood tools and the imp armor (empty: 1.12 had no repair item).
Running it again changes nothing.
"""
import io
import json
import pathlib
import sys
import zipfile

from PIL import Image

root = pathlib.Path(sys.argv[1])
NS = "natura"
assets = root / "assets" / NS
data = root / "data" / NS
TOOLS = pathlib.Path(__file__).resolve().parent
VANILLA = pathlib.Path(sys.argv[2]) if len(sys.argv) > 2 else (TOOLS.parent.parent / "natura-1.21" / "common" / "build" / "moddev" / "artifacts"
                                                              / "vanilla-1.21.1-20240808.144430-client-extra-aka-minecraft-resources.jar")


def dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def ingredient(value):
    if isinstance(value, list):
        values = [ingredient(v) for v in value]
        return values[0] if len(values) == 1 else values
    if isinstance(value, dict):
        if "item" in value:
            return value["item"]
        if "tag" in value:
            return "#" + value["tag"]
    return value


def merged(recipe_name, index, value):
    """26.x: a list of choices holds items only; choices with tags become an item tag of their own."""
    if isinstance(value, list) and any(v.startswith("#") for v in value):
        tag = f"recipe/{recipe_name}_{index}"
        dump(data / "tags" / "item" / (tag + ".json"), {"values": [{"id": v, "required": False} for v in value]})
        return f"#{NS}:{tag}"
    return value


# recipes
for recipe in sorted((data / "recipe").glob("*.json")):
    obj = json.loads(recipe.read_text(encoding="utf-8"))
    if "key" in obj:
        obj["key"] = {k: merged(recipe.stem, k, ingredient(v)) for k, v in obj["key"].items()}
    if "ingredients" in obj:
        obj["ingredients"] = [merged(recipe.stem, i, ingredient(v)) for i, v in enumerate(obj["ingredients"])]
    if "ingredient" in obj:
        obj["ingredient"] = ingredient(obj["ingredient"])
    dump(recipe, obj)

# static colours (client/NaturaClient, client/GrassColorizer)
vanilla = zipfile.ZipFile(VANILLA)


def vanilla_image(path):
    return Image.open(io.BytesIO(vanilla.read("assets/minecraft/" + path))).convert("RGBA")


def colormap_lookup(image, temperature, humidity):
    humidity *= temperature
    i = int((1.0 - temperature) * 255.0)
    j = int((1.0 - humidity) * 255.0)
    r, g, b, _ = image.getpixel((i, j))
    return r << 16 | g << 8 | b


grass = {
    "topiary_grass": colormap_lookup(vanilla_image("textures/colormap/grass.png"), 0.5, 1.0),
    "bluegrass": colormap_lookup(Image.open(assets / "textures/colormap/bluegrasscolor.png").convert("RGBA"), 0.5, 0.5),
    "autumnal_grass": colormap_lookup(Image.open(assets / "textures/colormap/orangegrasscolor.png").convert("RGBA"), 1.0, 1.0),
}
LEAVES = {"maple_leaves": 0xcc5718, "amaranth_leaves": 10079569, "eucalyptus_leaves": 7842607, "hopseed_leaves": 7842607, "redwood_leaves": 7842607}


def argb(rgb):
    value = 0xFF000000 | rgb
    return value - (1 << 32)  # a signed int, as the vanilla item definitions write it


def item_model(name):
    entry = {"type": "minecraft:model", "model": f"{NS}:item/{name}"}
    tint = None
    if name in LEAVES:
        tint = LEAVES[name]
    elif name.endswith("_leaves"):
        tint = 0xFFFFFF
    else:
        for kind, color in grass.items():
            if name == kind or name.startswith(kind + "_"):
                tint = color
    if tint is not None:
        entry["tints"] = [{"type": "minecraft:constant", "value": argb(tint)}]
    return entry


# client item definitions
models = assets / "models" / "item"
for model in sorted(models.glob("*.json")):
    name = model.stem
    if "_pulling_" in name or name == "bow":
        continue
    obj = json.loads(model.read_text(encoding="utf-8"))
    if "overrides" in obj:
        obj.pop("overrides")
        dump(model, obj)
    if name.endswith("_bow"):
        definition = {"type": "minecraft:condition", "property": "minecraft:using_item",
                      "on_true": {"type": "minecraft:model", "model": f"{NS}:item/{name}_pulling_0"}, "on_false": item_model(name)}
    else:
        definition = item_model(name)
    dump(assets / "items" / (name + ".json"), {"model": definition})

# spawn eggs: 1.12 colours on the 1.21.1 egg textures
EGGS = {"imp": (0xF29735, 0x2E1F10), "heatscarspider": (0xE64D10, 0x57B1BD), "babyheatscarspider": (0xE64D10, 0x57B1BD),
        "nitrocreeper": (0xF73E6C, 0x9B5004)}
base = vanilla_image("textures/item/spawn_egg.png")
overlay = vanilla_image("textures/item/spawn_egg_overlay.png")


def tinted(image, rgb):
    out = Image.new("RGBA", image.size)
    r0, g0, b0 = rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255
    for x in range(image.width):
        for y in range(image.height):
            r, g, b, a = image.getpixel((x, y))
            out.putpixel((x, y), (r * r0 // 255, g * g0 // 255, b * b0 // 255, a))
    return out


for egg, (background, highlight) in EGGS.items():
    texture = Image.alpha_composite(tinted(base, background), tinted(overlay, highlight))
    path = assets / "textures" / "item" / f"{egg}_spawn_egg.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    texture.save(path)
    dump(models / f"{egg}_spawn_egg.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{egg}_spawn_egg"}})

# imp armor: equipment asset
armor = assets / "textures" / "models" / "armor"
for layer, folder in (("imp_layer_1.png", "humanoid"), ("imp_layer_2.png", "humanoid_leggings")):
    source = armor / layer
    target = assets / "textures" / "entity" / "equipment" / folder / "imp.png"
    if source.exists():
        target.parent.mkdir(parents=True, exist_ok=True)
        source.replace(target)
if armor.exists() and not any(armor.iterdir()):
    armor.rmdir()
    if not any(armor.parent.iterdir()):
        armor.parent.rmdir()
dump(assets / "equipment" / "imp.json", {"layers": {"humanoid": [{"texture": f"{NS}:imp"}], "humanoid_leggings": [{"texture": f"{NS}:imp"}]}})

# repair tags (empty)
for tag in ("bloodwood_tool_materials", "imp_armor_materials"):
    dump(data / "tags" / "item" / (tag + ".json"), {"values": []})
