#!/usr/bin/env python3
"""Generates the 1.21.1 resources of the Natura port from the 1.12 Natura resources.

    python3 -I tools/gen_resources.py <1.12 src/main/resources/assets/natura> <src/main/resources>

Never edit src/main/resources/{assets,data} by hand: delete them and run this again.

1.12 Natura kept its variants as metadata of shared blocks (overworld_logs type=maple, overworld_planks type=...);
1.21 has one block per variant. The 1.12 Forge blockstates (forge_marker 1) are read for every texture, so the
models show the same textures; the shapes are the 1.21 vanilla templates of the same 1.12 vanilla models.
"""
import json
import os
import re
import gzip
import shutil
import struct
import sys
import zipfile

SRC = sys.argv[1]
OUT = sys.argv[2]
NS = "natura"
ASSETS = os.path.join(OUT, "assets", NS)
DATA = os.path.join(OUT, "data")
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
# the 1.21.1 vanilla assets (blockstates / models of the templates), unpacked by ModDevGradle on the first build
VANILLA_JAR = os.path.join(ROOT, "build", "moddev", "artifacts", "neoforge-21.1.256-client-extra-aka-minecraft-resources.jar")


def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


# ---------------------------------------------------------------- 1.12 Forge blockstates

def load_state(name):
    with open(os.path.join(SRC, "blockstates", name + ".json"), encoding="utf-8") as f:
        return json.load(f)


def tex_path(t):
    """1.12 texture location -> 1.21 (blocks/ -> block/, items/ -> item/)."""
    if t.startswith("#"):
        return t
    ns, _, path = t.partition(":") if ":" in t else ("minecraft", "", t)
    path = re.sub(r"^blocks/", "block/", path)
    path = re.sub(r"^items/", "item/", path)
    if ns == "minecraft":
        path = VANILLA_TEXTURES.get(path, path)
    return ns + ":" + path


VANILLA_TEXTURES = {
    "block/netherrack": "block/netherrack",
}


def merge(dst, src):
    for k, v in src.items():
        if k == "textures":
            dst.setdefault("textures", {}).update(v)
        elif k in ("transform", "__comment"):
            continue
        else:
            dst[k] = v


def resolve(name, **props):
    """The merged 1.12 variant of blockstate `name` for the given property values: model, textures, x, y, uvlock."""
    bs = load_state(name)
    out = {}
    merge(out, bs.get("defaults", {}))
    variants = bs.get("variants", {})
    if not props and isinstance(variants.get("normal"), dict):
        merge(out, variants["normal"])
    for prop, value in props.items():
        group = variants.get(prop)
        if isinstance(group, dict) and str(value) in group and isinstance(group[str(value)], dict):
            merge(out, group[str(value)])
    if props:
        key = ",".join(f"{k}={v}" for k, v in sorted(props.items()))
        full = variants.get(key)
        if isinstance(full, list):
            full = full[0]
        if isinstance(full, dict):
            merge(out, full)
    textures = out.get("textures", {})
    for _ in range(5):  # resolve "#name" references inside the 1.12 texture map
        for k, v in list(textures.items()):
            if isinstance(v, str) and v.startswith("#") and v[1:] in textures:
                textures[k] = textures[v[1:]]
    out["textures"] = {k: tex_path(v) for k, v in textures.items()}
    return out


def tex(name, key, **props):
    return resolve(name, **props)["textures"][key]


# ---------------------------------------------------------------- 1.21 models / blockstates

def model(name, parent, textures, render_type=None):
    obj = {"parent": parent, "textures": textures}
    if render_type:
        obj["render_type"] = render_type
    w(os.path.join(ASSETS, "models", "block", name + ".json"), obj)
    return f"{NS}:block/{name}"


def item_model(name, parent=None, layer0=None):
    if layer0:
        obj = {"parent": "minecraft:item/generated", "textures": {"layer0": layer0}}
    else:
        obj = {"parent": parent}
    w(os.path.join(ASSETS, "models", "item", name + ".json"), obj)


def blockstate(name, variants):
    w(os.path.join(ASSETS, "blockstates", name + ".json"), {"variants": variants})


def multipart(name, parts):
    w(os.path.join(ASSETS, "blockstates", name + ".json"), {"multipart": parts})


def simple_block(name, parent, textures, render_type=None):
    m = model(name, parent, textures, render_type)
    blockstate(name, {"": {"model": m}})
    item_model(name, m)


def pillar(name, end, side):
    """1.12 Natura logs turned the plain column model for x / z (x=90, x=90 y=90), not cube_column_horizontal."""
    m = model(name, "minecraft:block/cube_column", {"end": end, "side": side})
    blockstate(name, {"axis=y": {"model": m}, "axis=z": {"model": m, "x": 90}, "axis=x": {"model": m, "x": 90, "y": 90}})
    item_model(name, m)


def vanilla_json(path):
    with zipfile.ZipFile(VANILLA_JAR) as z:
        return json.loads(z.read("assets/minecraft/" + path))


def from_vanilla(name, vanilla, textures, render_type=None, extra=None, item=True, parents=None):
    """The blockstate of vanilla block `vanilla` with its models swapped for copies using our textures.

    textures: texture key -> our texture (a single string for every key). extra: vanilla model -> our model (as is)."""
    state = vanilla_json(f"blockstates/{vanilla}.json")
    made = {}

    def swap(m):
        if extra and m in extra:
            return extra[m]
        if m not in made:
            src = vanilla_json(f"models/block/{m.split('/')[-1]}.json")
            suffix = m.split("/")[-1][len(vanilla):]
            tex_map = {k: (textures if isinstance(textures, str) else textures[k]) for k in src.get("textures", {})}
            parent = (parents or {}).get(src["parent"], src["parent"])
            made[m] = model(name + suffix, parent, tex_map, render_type)
        return made[m]

    def walk(o):
        if isinstance(o, dict):
            return {k: (swap(v) if k == "model" else walk(v)) for k, v in o.items()}
        if isinstance(o, list):
            return [walk(x) for x in o]
        return o

    w(os.path.join(ASSETS, "blockstates", name + ".json"), walk(state))
    if item:
        src = vanilla_json(f"models/item/{vanilla}.json")
        parent = src["parent"]
        item_model(name, swap(parent) if parent.startswith("minecraft:block/") else parent)


def slab(name, full_model, all_tex):
    from_vanilla(name, "oak_slab", all_tex, extra={"minecraft:block/oak_planks": full_model})


def stairs(name, all_tex):
    from_vanilla(name, "oak_stairs", all_tex)


def button(name, texture):
    from_vanilla(name, "oak_button", texture)


def pressure_plate(name, texture):
    from_vanilla(name, "oak_pressure_plate", texture)


def trapdoor(name, texture):
    """1.12 Natura trapdoors did not turn the closed model with the facing: the iron trapdoor (not orientable) models."""
    from_vanilla(name, "iron_trapdoor", texture, "minecraft:cutout")


def fence(name, texture):
    from_vanilla(name, "oak_fence", texture)


def fence_gate(name, texture):
    from_vanilla(name, "oak_fence_gate", texture)


def door(name, bottom, top, item_tex):
    from_vanilla(name, "oak_door", {"bottom": bottom, "top": top}, "minecraft:cutout", item=False)
    item_model(name, layer0=item_tex)


def leaves(name, texture):
    m = model(name, "minecraft:block/leaves", {"all": texture}, "minecraft:cutout_mipped")
    blockstate(name, {"": {"model": m}})
    item_model(name, m)


def cross_block(name, texture, item_tex=None):
    m = model(name, "minecraft:block/cross", {"cross": texture}, "minecraft:cutout")
    blockstate(name, {"": {"model": m}})
    item_model(name, layer0=item_tex or texture)


# ---------------------------------------------------------------- textures

def copy_textures():
    src = os.path.join(SRC, "textures")
    for root, _, files in os.walk(src):
        rel = os.path.relpath(root, src)
        parts = rel.split(os.sep)
        if parts[0] == "blocks":
            parts[0] = "block"
        elif parts[0] == "items":
            parts[0] = "item"
        dst = os.path.join(ASSETS, "textures", *parts) if rel != "." else os.path.join(ASSETS, "textures")
        for f in files:
            if f.endswith((".png", ".mcmeta")):
                os.makedirs(dst, exist_ok=True)
                shutil.copyfile(os.path.join(root, f), os.path.join(dst, f))


# ---------------------------------------------------------------- the content

OVERWORLD = ["maple", "silverbell", "amaranth", "tiger", "willow", "eucalyptus", "hopseed", "sakura", "redwood"]
NETHER = ["ghostwood", "bloodwood", "fusewood", "darkwood"]
WOODS = OVERWORLD + NETHER

# (1.12 item id, meta) -> new id, for recipes; 1.12 translation key -> new key, for the languages
OLD = {}
KEYS = {}


def old(old_id, meta, new_id, key=None, kind="block"):
    OLD[(old_id, meta)] = new_id
    if key:
        prefix = "tile" if kind == "block" else "item"
        KEYS[f"{prefix}.natura.{key}"] = f"{'block' if kind == 'block' else 'item'}.{NS}.{new_id}"


LOOT = {}  # block id -> loot table
BLOCK_TAGS = {}
ITEM_TAGS = {}


def tag(kind, name, *values):
    table = BLOCK_TAGS if kind == "block" else ITEM_TAGS
    table.setdefault(name, [])
    for v in values:
        if v not in table[name]:
            table[name].append(v)


def nid(name):
    return f"{NS}:{name}"


def self_drop(name):
    LOOT[name] = {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": nid(name)}],
                                                         "conditions": [{"condition": "minecraft:survives_explosion"}]}]}


def slab_drop(name):
    LOOT[name] = {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": nid(name), "functions": [
        {"function": "minecraft:set_count", "add": False, "count": 2, "conditions": [
            {"condition": "minecraft:block_state_property", "block": nid(name), "properties": {"type": "double"}}]},
        {"function": "minecraft:explosion_decay"}]}]}]}


def door_drop(name):
    LOOT[name] = {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": nid(name), "conditions": [
        {"condition": "minecraft:block_state_property", "block": nid(name), "properties": {"half": "lower"}}]}],
                                                         "conditions": [{"condition": "minecraft:survives_explosion"}]}]}


SILK_OR_SHEARS = {"condition": "minecraft:any_of", "terms": [
    {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}},
    {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
        {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]}


def leaves_drop(name, drop, chance):
    """1.12 BlockLeaves.getDrops: 1 in `chance`, fortune: chance - (2 << fortune), at least 10; no apples, no sticks."""
    chances = [1.0 / chance] + [1.0 / max(10, chance - (2 << f)) for f in (1, 2, 3)]
    LOOT[name] = {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        {"type": "minecraft:item", "name": nid(name), "conditions": [SILK_OR_SHEARS]},
        {"type": "minecraft:item", "name": drop, "conditions": [{"condition": "minecraft:survives_explosion"},
                                                               {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": chances}]}]}]}]}


def fixed_drop(name, item, count):
    LOOT[name] = {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": item, "functions": [
        {"function": "minecraft:set_count", "add": False, "count": count}, {"function": "minecraft:explosion_decay"}]}]}]}


def woods():
    log_types = {"overworld_logs": ["maple", "silverbell", "amaranth", "tiger"], "overworld_logs2": ["willow", "eucalyptus", "hopseed", "sakura"]}
    for old_id, types in log_types.items():
        for meta, t in enumerate(types):
            r = resolve(old_id, type=t, axis="y")
            pillar(f"{t}_log", r["textures"]["end"], r["textures"]["side"])
            old(old_id, meta, f"{t}_log", f"{old_id}.{t}")
    for meta, t in enumerate(["ghostwood", "darkwood", "fusewood"]):
        r = resolve("nether_logs", type=t, axis="y")
        pillar(f"{t}_log", r["textures"]["end"], r["textures"]["side"])
        old("nether_logs", meta, f"{t}_log", f"nether_logs.{t}")
    for meta, t in enumerate(["bark", "heart", "root"]):
        simple_block(f"redwood_{t}", "minecraft:block/cube_all", {"all": tex("redwood_logs", "all", type=t)})
        old("redwood_logs", meta, f"redwood_{t}", f"redwood_logs.{t}")
    # bloodwood: 1.12 nether_logs2 meta 0-11 are the trunk quarters (cube with per-face textures), 12-15 all bark
    v = {}
    for piece in range(12):
        r = resolve("nether_logs2", meta=piece)
        m = model(f"bloodwood_log_{piece}", "minecraft:block/cube", r["textures"])
        v[f"piece={piece}"] = {"model": m}
    blockstate("bloodwood_log", v)
    item_model("bloodwood_log", f"{NS}:block/bloodwood_log_0")
    simple_block("bloodwood_bark", "minecraft:block/cube_all", {"all": tex("nether_logs2", "bark", meta=15)})
    old("nether_logs2", 0, "bloodwood_log")
    old("nether_logs2", 15, "bloodwood_bark")
    KEYS["tile.natura.nether_logs2"] = "block.natura.bloodwood_log"
    KEYS["tile.natura.nether_logs2#bark"] = "block.natura.bloodwood_bark"

    leaf_types = {"overworld_leaves": ["maple", "silverbell", "amaranth", "tiger"], "overworld_leaves2": ["willow", "eucalyptus", "hopseed", "sakura"]}
    for old_id, types in leaf_types.items():
        for meta, t in enumerate(types):
            leaves(f"{t}_leaves", tex(old_id, "all", type=t))
            old(old_id, meta, f"{t}_leaves", f"{old_id}.{t}")
            leaves_drop(f"{t}_leaves", nid(f"{t}_sapling"), 25)
    leaves("redwood_leaves", tex("redwood_leaves", "all", type="normal"))
    old("redwood_leaves", 0, "redwood_leaves", "redwood_leaves.normal")
    leaves_drop("redwood_leaves", nid("redwood_sapling"), 25)
    for meta, t in enumerate(["ghostwood", "bloodwood", "fusewood"]):
        leaves(f"{t}_leaves", tex("nether_leaves", "all", type=t))
        old("nether_leaves", meta, f"{t}_leaves", f"nether_leaves.{t}")
        leaves_drop(f"{t}_leaves", nid(f"{t}_sapling"), 25)
    for meta, (t, new) in enumerate([("darkwood", "darkwood_leaves"), ("darkwood_flowering", "darkwood_flowering_leaves"), ("darkwood_fruit", "darkwood_fruit_leaves")]):
        leaves(new, tex("nether_leaves2", "all", type=t))
        old("nether_leaves2", meta, new, f"nether_leaves2.{t}")
        # 1.12 BlockNetherLeaves2 keeps the default 1 in 20; the fruit leaves drop a potash apple instead of a sapling
        leaves_drop(new, nid("potash_apple") if t == "darkwood_fruit" else nid("darkwood_sapling"), 20)

    sapling_types = {"overworld_sapling": ["maple", "silverbell", "amaranth", "tiger"], "overworld_sapling2": ["willow", "eucalyptus", "hopseed", "sakura"],
                     "redwood_sapling": ["redwood"], "nether_sapling": ["ghostwood", "fusewood", "darkwood"], "nether_sapling2": ["bloodwood"]}
    for old_id, types in sapling_types.items():
        for meta, t in enumerate(types):
            texture = tex(old_id, "cross", foliage=t)
            m = model(f"{t}_sapling", "minecraft:block/cross", {"cross": texture}, "minecraft:cutout")
            blockstate(f"{t}_sapling", {"stage=0": {"model": m}, "stage=1": {"model": m}})
            item_model(f"{t}_sapling", layer0=texture)
            old(old_id, meta, f"{t}_sapling", f"{old_id}.{t}")
            self_drop(f"{t}_sapling")
            tag("block", "minecraft:saplings", nid(f"{t}_sapling"))

    plank_types = {"overworld_planks": OVERWORLD, "nether_planks": ["ghostwood", "bloodwood", "darkwood", "fusewood"]}
    for old_id, types in plank_types.items():
        for meta, t in enumerate(types):
            plank = tex(old_id, "all", type=t)
            simple_block(f"{t}_planks", "minecraft:block/cube_all", {"all": plank})
            old(old_id, meta, f"{t}_planks", f"{old_id}.{t}")
    slab_types = {"overworld_slab": ["maple", "silverbell", "amaranth", "tiger", "willow"], "overworld_slab2": ["eucalyptus", "sakura", "hopseed", "redwood"],
                  "nether_slab": ["ghostwood", "bloodwood", "darkwood", "fusewood"]}
    for old_id, types in slab_types.items():
        for meta, t in enumerate(types):
            slab(f"{t}_slab", f"{NS}:block/{t}_planks", tex(old_id, "all", type=t, half="bottom"))
            old(old_id, meta, f"{t}_slab", f"{old_id}.{t}")
            slab_drop(f"{t}_slab")
    for t in WOODS:
        stair_file = f"overworld_stairs_{t}" if t in OVERWORLD else f"nether_stairs_{t}"
        stairs(f"{t}_stairs", tex(stair_file, "all"))
        old(stair_file, 0, f"{t}_stairs", stair_file)
        button(f"{t}_button", tex(f"{t}_button", "texture", facing="up", powered="false"))
        pressure_plate(f"{t}_pressure_plate", tex(f"{t}_pressure_plate", "texture", powered="false"))
        trapdoor(f"{t}_trapdoor", tex(f"{t}_trap_door", "texture", facing="north", half="bottom", open="false"))
        fence(f"{t}_fence", tex(f"{t}_fence", "texture"))
        fence_gate(f"{t}_fence_gate", tex(f"{t}_fence_gate", "texture", facing="south", in_wall="false", open="false"))
        for old_id, new in ((f"{t}_button", f"{t}_button"), (f"{t}_pressure_plate", f"{t}_pressure_plate"), (f"{t}_trap_door", f"{t}_trapdoor"),
                            (f"{t}_fence", f"{t}_fence"), (f"{t}_fence_gate", f"{t}_fence_gate")):
            old(old_id, 0, new, old_id)
        for b in (f"{t}_planks", f"{t}_stairs", f"{t}_button", f"{t}_pressure_plate", f"{t}_trapdoor", f"{t}_fence", f"{t}_fence_gate"):
            self_drop(b)
        world = "overworld" if t in OVERWORLD else "nether"
        meta = (OVERWORLD if t in OVERWORLD else ["ghostwood", "bloodwood", "darkwood", "fusewood"]).index(t)
        r = resolve(f"{world}_bookshelves", type=t)
        simple_block(f"{t}_bookshelf", "minecraft:block/cube_column", {"end": r["textures"]["end"], "side": r["textures"]["side"]})
        old(f"{world}_bookshelves", meta, f"{t}_bookshelf", f"{world}_bookshelves.{t}")
        fixed_drop(f"{t}_bookshelf", "minecraft:book", 3)
        r = resolve(f"{world}_workbenches", type=t)
        simple_block(f"{t}_workbench", "minecraft:block/cube", {k: r["textures"][k] for k in ("particle", "down", "up", "north", "east", "south", "west")})
        old(f"{world}_workbenches", meta, f"{t}_workbench", f"{world}_workbenches.{t}")
        self_drop(f"{t}_workbench")
        stick = f"{t}_stick"
        item_model(stick, layer0=f"{NS}:item/sticks/{stick}")
        old("sticks", WOODS_STICK_META[t], stick, f"sticks.{stick}", kind="item")

    door_info = [("overworld", "eucalyptus"), ("overworld", "hopseed"), ("overworld", "sakura"), ("overworld", "redwood"), ("overworld", "redwood_bark"),
                 ("overworld", "tiger"), ("overworld", "maple"), ("overworld", "silverbell"),
                 ("nether", "ghostwood"), ("nether", "bloodwood"), ("nether", "darkwood"), ("nether", "fusewood")]
    metas = {"overworld": 0, "nether": 0}
    for world, t in door_info:
        r = resolve(f"{world}_door_{t}", facing="east", half="lower", hinge="left", open="false")
        door(f"{t}_door", r["textures"]["door_bottom"], r["textures"]["door_top"], f"{NS}:item/doors/{t}_door")
        door_drop(f"{t}_door")
        old(f"{world}_doors", metas[world], f"{t}_door")
        KEYS[f"item.natura.{world}_doors.{t}_door"] = f"block.{NS}.{t}_door"
        metas[world] += 1

    for t in WOODS:
        for kind in ("block", "item"):
            tag(kind, "minecraft:planks", nid(f"{t}_planks"))
            tag(kind, "minecraft:wooden_slabs", nid(f"{t}_slab"))
            tag(kind, "minecraft:wooden_stairs", nid(f"{t}_stairs"))
            tag(kind, "minecraft:wooden_buttons", nid(f"{t}_button"))
            tag(kind, "minecraft:wooden_pressure_plates", nid(f"{t}_pressure_plate"))
            tag(kind, "minecraft:wooden_trapdoors", nid(f"{t}_trapdoor"))
            tag(kind, "minecraft:wooden_fences", nid(f"{t}_fence"))
            tag(kind, "minecraft:fence_gates", nid(f"{t}_fence_gate"))
            tag(kind, "c:bookshelves", nid(f"{t}_bookshelf"))
            tag(kind, "c:player_workstations/crafting_tables", nid(f"{t}_workbench"))
        tag("block", "minecraft:enchantment_power_provider", nid(f"{t}_bookshelf"))
        tag("item", "c:rods/wooden", nid(f"{t}_stick"))
        for b in (f"{t}_planks", f"{t}_slab", f"{t}_stairs", f"{t}_button", f"{t}_pressure_plate", f"{t}_trapdoor", f"{t}_fence", f"{t}_fence_gate",
                  f"{t}_bookshelf", f"{t}_workbench"):
            tag("block", "minecraft:mineable/axe", nid(b))
    for name, _, _ in DOORS:
        for kind in ("block", "item"):
            tag(kind, "minecraft:wooden_doors", nid(name))
        tag("block", "minecraft:mineable/axe", nid(name))
    logs = [f"{t}_log" for t in ("maple", "silverbell", "amaranth", "tiger", "willow", "eucalyptus", "hopseed", "sakura", "ghostwood", "darkwood", "fusewood", "bloodwood")]
    logs += ["redwood_bark", "redwood_heart", "redwood_root", "bloodwood_bark"]
    for b in logs:
        self_drop(b)
        for kind in ("block", "item"):
            tag(kind, "minecraft:logs", nid(b))
        tag("block", "minecraft:mineable/axe", nid(b))
        if b not in ("ghostwood_log", "darkwood_log", "fusewood_log", "bloodwood_log", "bloodwood_bark"):
            for kind in ("block", "item"):
                tag(kind, "minecraft:logs_that_burn", nid(b))
    for b in [f"{t}_leaves" for t in OVERWORLD + ["ghostwood", "bloodwood", "fusewood"]] + ["darkwood_leaves", "darkwood_flowering_leaves", "darkwood_fruit_leaves"]:
        for kind in ("block", "item"):
            tag(kind, "minecraft:leaves", nid(b))
        tag("block", "minecraft:mineable/hoe", nid(b))
    for t in OVERWORLD + NETHER:
        tag("item", "minecraft:saplings", nid(f"{t}_sapling"))
    tag("block", "natura:nether_sapling_soil", "minecraft:soul_sand", "minecraft:netherrack")


def items():
    plant_items()


DOORS = [("eucalyptus_door", 0, 0), ("hopseed_door", 0, 0), ("sakura_door", 0, 0), ("redwood_door", 0, 0), ("redwood_bark_door", 0, 0),
         ("tiger_door", 0, 0), ("maple_door", 0, 0), ("silverbell_door", 0, 0), ("ghostwood_door", 0, 0), ("bloodwood_door", 0, 0),
         ("darkwood_door", 0, 0), ("fusewood_door", 0, 0)]
WOODS_STICK_META = {"maple": 0, "silverbell": 1, "amaranth": 2, "tiger": 3, "willow": 4, "eucalyptus": 5, "hopseed": 6, "sakura": 7, "redwood": 8,
                    "ghostwood": 9, "darkwood": 10, "fusewood": 11, "bloodwood": 12}


# ---------------------------------------------------------------- 1.12 custom models

def copy_models():
    """The 1.12 Natura block models (bushes, saguaro, grass, slabs, stairs, ...) with 1.21 paths."""
    src = os.path.join(SRC, "models", "block")
    for f in os.listdir(src):
        if not f.endswith(".json"):
            continue
        with open(os.path.join(src, f), encoding="utf-8") as fh:
            m = json.load(fh)
        if "parent" in m:
            parent = m["parent"]
            m["parent"] = parent if ":" in parent else "minecraft:" + parent
        if "textures" in m:
            m["textures"] = {k: tex_path(v) for k, v in m["textures"].items()}
        w(os.path.join(ASSETS, "models", "block", f), m)


def item_tex(state_file, variant):
    """layer0 (and layer1) of a 1.12 item blockstate variant (materials, edibles, soups, ...)."""
    v = load_state(state_file)["variants"][variant]
    v = v[0] if isinstance(v, list) else v
    return {k: tex_path(x) for k, x in v["textures"].items()}


def layered_item(name, layers):
    textures = {k: v for k, v in layers.items()}
    if "layer0" in textures and textures["layer0"] == "minecraft:item/bowl":
        textures["layer0"] = "minecraft:item/bowl"
    w(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": "minecraft:item/generated", "textures": textures})


def plants():
    # colored grass: the 1.12 tinted natura_grass / slab / stairs models
    for meta, (t, new) in enumerate([("topiary", "topiary_grass"), ("bluegrass", "bluegrass"), ("autumnal", "autumnal_grass")]):
        grass = tex("colored_grass", "all", type=t)
        simple_block(new, f"{NS}:block/natura_grass", {"all": grass})
        old("colored_grass", meta, new, f"colored_grass.{t}")
        self_drop(new)
        from_vanilla(new + "_slab", "oak_slab", grass, extra={"minecraft:block/oak_planks": f"{NS}:block/{new}"},
                     parents={"minecraft:block/slab": f"{NS}:block/natura_half_slab", "minecraft:block/slab_top": f"{NS}:block/natura_upper_slab"})
        old("colored_grass_slab", meta, new + "_slab", f"colored_grass_slab.{t}")
        slab_drop(new + "_slab")
        from_vanilla(new + "_stairs", "oak_stairs", grass, parents={"minecraft:block/stairs": f"{NS}:block/natura_stairs",
                                                                   "minecraft:block/inner_stairs": f"{NS}:block/natura_inner_stairs",
                                                                   "minecraft:block/outer_stairs": f"{NS}:block/natura_outer_stairs"})
        stairs_old = "colored_grass_stairs_" + t
        old(stairs_old, 0, new + "_stairs", stairs_old)
        self_drop(new + "_stairs")
        for b in (new, new + "_slab", new + "_stairs"):
            tag("block", "minecraft:mineable/shovel", nid(b))
        tag("block", "minecraft:dirt", nid(new))
    cross_block("bluebells", tex("bluebells_flower", "cross"), None)
    old("bluebells_flower", 0, "bluebells", "bluebells_flower")
    self_drop("bluebells")
    # berry bushes: 1.12 natura_bush_small / medium / large, ripe texture at stage 3
    for world, berries in (("overworld", ["raspberry", "blueberry", "blackberry", "maloberry"]), ("nether", ["blightberry", "duskberry", "skyberry", "stingberry"])):
        for berry in berries:
            old_id = f"{world}_berrybush_{berry}"
            name = f"{berry}_bush"
            v = {}
            for age in range(4):
                r = resolve(old_id, age=age)
                parent = r["model"]
                parent = parent if ":" not in parent else parent.replace("natura:", "natura:block/")
                m = model(f"{name}_stage{age}", parent, {"all": r["textures"]["all"], "particle": r["textures"]["all"]}, "minecraft:cutout_mipped")
                v[f"age={age}"] = {"model": m}
            blockstate(name, v)
            item_model(name, f"{NS}:block/{name}_stage0")
            old(old_id, 0, name, old_id)
            self_drop(name)
            tag("block", "minecraft:mineable/hoe", nid(name))
    # crops: the vanilla crop model per stage, no item
    for crop, stages, seed, crop_item, p in (("barley", 4, "barley_seeds", "barley", 4 / 6), ("cotton", 5, "cotton_seeds", "cotton", 5 / 8)):
        v = {}
        for age in range(stages):
            m = model(f"{crop}_crop_stage{age}", "minecraft:block/crop", {"crop": tex(f"{crop}_crop", "crop", age=age)}, "minecraft:cutout")
            v[f"age={age}"] = {"model": m}
        blockstate(f"{crop}_crop", v)
        crop_drops(f"{crop}_crop", stages - 1, nid(crop_item), nid(seed), p)
        tag("block", "minecraft:crops", nid(f"{crop}_crop"))
    # saguaro: the 1.12 centre model plus a side / top / bottom part for every connection
    sag = resolve("saguaro", north="false", south="false", east="false", west="false", up="false", down="false")["textures"]
    t3 = {"side": sag["side"], "top": sag["top"], "bottom": sag["bottom"], "particle": sag["side"]}
    centre = model("saguaro", f"{NS}:block/natura_saguaro", t3, "minecraft:cutout")
    side = model("saguaro_side", f"{NS}:block/natura_saguaro_side", t3, "minecraft:cutout")
    top = model("saguaro_top", f"{NS}:block/natura_saguaro_top", t3, "minecraft:cutout")
    bottom = model("saguaro_bottom", f"{NS}:block/natura_saguaro_bottom", t3, "minecraft:cutout")
    parts = [{"apply": {"model": centre}}]
    for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        entry = {"model": side, "uvlock": True}
        if y:
            entry["y"] = y
        parts.append({"when": {facing: "true"}, "apply": entry})
    parts.append({"when": {"up": "true"}, "apply": {"model": top, "uvlock": True}})
    parts.append({"when": {"down": "true"}, "apply": {"model": bottom, "uvlock": True}})
    multipart("saguaro", parts)
    item_model("saguaro", "minecraft:block/cube")
    w(os.path.join(ASSETS, "models", "item", "saguaro.json"), {"parent": "minecraft:block/cube", "textures": {
        "down": sag["bottom"], "up": sag["top"], "north": sag["side"], "south": sag["side"], "west": sag["side"], "east": sag["side"], "particle": sag["side"]}})
    old("saguaro", 0, "saguaro", "saguaro")
    self_drop("saguaro")
    baby = model("saguaro_baby", f"{NS}:block/natura_saguaro_baby", {"all": tex("saguaro_baby", "all", age=0), "particle": tex("saguaro_baby", "all", age=0)}, "minecraft:cutout")
    blockstate("saguaro_baby", {"age=0": {"model": baby}, "age=1": {"model": baby}})
    item_model("saguaro_baby", baby)
    old("saguaro_baby", 0, "saguaro_baby", "saguaro_baby")
    self_drop("saguaro_baby")
    fruit_tex = tex("saguaro_fruit", "all", facing="south")
    fruit = model("saguaro_fruit", f"{NS}:block/natura_saguaro_fruit", {"all": fruit_tex, "particle": fruit_tex}, "minecraft:cutout")
    blockstate("saguaro_fruit", {"facing=south": {"model": fruit}, "facing=west": {"model": fruit, "y": 90}, "facing=north": {"model": fruit, "y": 180},
                                  "facing=east": {"model": fruit, "y": 270}})
    with open(os.path.join(SRC, "models", "item", "saguaro_fruit.json"), encoding="utf-8") as fh:
        m = json.load(fh)
    m["parent"] = "minecraft:block/block"
    m["textures"] = {k: tex_path(v) for k, v in m["textures"].items()}
    m["textures"]["particle"] = m["textures"]["all"]
    w(os.path.join(ASSETS, "models", "item", "saguaro_fruit.json"), m)
    old("saguaro_fruit", 0, "saguaro_fruit", "saguaro_fruit")
    LOOT["saguaro_fruit"] = {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [
        {"type": "minecraft:item", "name": nid("saguaro_fruit_item")}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]}
    item_model("saguaro_fruit_item", layer0=f"{NS}:item/food/saguaro_fruit")
    old("saguaro_fruit_item", 0, "saguaro_fruit_item", "saguaro_fruit_item", kind="item")


def crop_drops(name, max_age, crop, seed, p):
    """1.12 BlockOverworldCrops: the crop when ripe (else the seed), and when ripe 3 more tries at a seed
    (rand(2 * max) <= age), never with fortune (1.12 dropped crops with fortune 0)."""
    ripe = {"condition": "minecraft:block_state_property", "block": nid(name), "properties": {"age": str(max_age)}}
    LOOT[name] = {"type": "minecraft:block", "pools": [
        {"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": crop, "conditions": [ripe]}, {"type": "minecraft:item", "name": seed}]}]},
        {"rolls": 1, "bonus_rolls": 0, "conditions": [ripe], "entries": [{"type": "minecraft:item", "name": seed, "functions": [
            {"function": "minecraft:set_count", "add": False, "count": {"type": "minecraft:binomial", "n": 3, "p": p}}]}]}],
                  "functions": [{"function": "minecraft:explosion_decay"}]}


def plant_items():
    for meta, name in enumerate(["barley", "barley_flour", "wheat_flour", "cotton", "sulfur_powder", "ghostwood_fletching", "imp_leather", "flame_string", "blue_dye"]):
        layered_item(name, item_tex("materials", name))
        old("materials", meta, name, f"materials.{name}", kind="item")
    for meta, wood in enumerate(["ghostwood", "bloodwood", "darkwood", "fusewood"]):
        layered_item(f"{wood}_bowl", item_tex("empty_bowls", f"{wood}_bowl"))
        old("empty_bowls", meta, f"{wood}_bowl", f"empty_bowls.{wood}_bowl", kind="item")
    edibles = ["impmeat_raw", "impmeat_cooked", "raspberry", "blueberry", "blackberry", "maloberry", "blightberry", "duskberry", "skyberry", "stingberry",
               "potashapple", "cactusjuice"]
    for meta, name in enumerate(edibles):
        new = {"potashapple": "potash_apple", "cactusjuice": "cactus_juice"}.get(name, name)
        layered_item(new, item_tex("edibles", name))
        old("edibles", meta, new, f"edibles.{name}", kind="item")
    soups = ["ghostwood_mushroomstew", "bloodwood_mushroomstew", "darkwood_mushroomstew", "fusewood_mushroomstew", "vanilla_glowshroomstew",
             "ghostwood_glowshroomstew", "bloodwood_glowshroomstew", "darkwood_glowshroomstew", "fusewood_glowshroomstew", "berry_medley"]
    for meta, name in enumerate(soups):
        new = name.replace("mushroomstew", "mushroom_stew").replace("glowshroomstew", "glowshroom_stew").replace("vanilla_", "")
        layers = item_tex("soups", name)
        if name.endswith("mushroomstew"):
            layers = {"layer0": layers["layer0"], "layer1": f"{NS}:item/bowls/stew/stew_mushroom"}
        layered_item(new, layers)
        old("soups", meta, new, f"soups.{name}", kind="item")
    for meta, name in enumerate(["wheat_seed_bag", "carrots_seed_bag", "potatoes_seed_bag", "nether_wart_seed_bag"]):
        layered_item(name, item_tex("seed_bags", name))
        old("seed_bags", meta, name, f"seed_bags.{name}", kind="item")
    for meta, name in enumerate(["barley_seed_bag", "cotton_seed_bag"]):
        layered_item(name, item_tex("overworld_seed_bags", name))
        old("overworld_seed_bags", meta, name, f"overworld_seed_bags.{name}", kind="item")
    for meta, name in enumerate(["barley_seeds", "cotton_seeds"]):
        layered_item(name, item_tex("overworld_seeds", name))
        old("overworld_seeds", meta, name, f"overworld_seeds.{name}", kind="item")
    item_model("bonemeal_bag", layer0=f"{NS}:item/bags/bonemeal_bag")
    old("bonemeal_bag", 0, "bonemeal_bag", "bonemeal_bag", kind="item")
    # 1.12 ore dictionary names as tags
    for kind in ("item",):
        tag(kind, "natura:wooden_bowls", "minecraft:bowl", *[nid(f"{w_}_bowl") for w_ in ("ghostwood", "bloodwood", "darkwood", "fusewood")])
        tag(kind, "c:crops/barley", nid("barley"))
        tag(kind, "c:crops/cotton", nid("cotton"))
        tag(kind, "c:crops", nid("barley"), nid("cotton"))
        tag(kind, "c:foods/flour", nid("barley_flour"), nid("wheat_flour"))
        tag(kind, "c:dyes/blue", nid("blue_dye"))
        tag(kind, "c:dyes", nid("blue_dye"))
        tag(kind, "c:dusts/sulfur", nid("sulfur_powder"))
        tag(kind, "c:seeds/barley", nid("barley_seeds"))
        tag(kind, "c:seeds/cotton", nid("cotton_seeds"))
        tag(kind, "c:seeds", nid("barley_seeds"), nid("cotton_seeds"))
        for berry in ("raspberry", "blueberry", "blackberry", "maloberry", "blightberry", "duskberry", "skyberry", "stingberry"):
            tag(kind, f"c:crops/{berry}", nid(berry))
            tag(kind, "c:foods/berry", nid(berry))
            tag(kind, "c:foods/fruit", nid(berry))
        tag(kind, "c:crops/strawberry")
        tag(kind, "c:crops/cranberry")


# ---------------------------------------------------------------- recipes

ORE = {"stickWood": "c:rods/wooden", "plankWood": "minecraft:planks", "logWood": "minecraft:logs", "slabWood": "minecraft:wooden_slabs",
       "treeSapling": "minecraft:saplings", "treeLeaves": "minecraft:leaves", "stairWood": "minecraft:wooden_stairs", "bowlWood": "natura:wooden_bowls",
       "chestWood": "c:chests/wooden", "cropBarley": "c:crops/barley", "cropCotton": "c:crops/cotton", "cropCarrot": "c:crops/carrot",
       "cropPotato": "c:crops/potato", "seedBarley": "c:seeds/barley", "seedCotton": "c:seeds/cotton", "dustRedstone": "c:dusts/redstone",
       "dustSulfur": "c:dusts/sulfur", "glass": "c:glass_blocks", "gemQuartz": "c:gems/quartz", "dyeBlue": "c:dyes/blue", "dyeRed": "c:dyes/red",
       "cropRaspberry": "c:crops/raspberry", "cropBlueberry": "c:crops/blueberry", "cropBlackberry": "c:crops/blackberry",
       "cropMaloberry": "c:crops/maloberry", "cropStrawberry": "c:crops/strawberry", "cropCranberry": "c:crops/cranberry"}


# 1.12 vanilla items with metadata -> the 1.21 items
VANILLA_META = {("minecraft:coal", 1): "minecraft:charcoal", ("minecraft:dye", 15): "minecraft:bone_meal", ("minecraft:wool", 0): "minecraft:white_wool",
                ("minecraft:quartz_block", 1): "minecraft:chiseled_quartz_block", ("minecraft:quartz_block", 2): "minecraft:quartz_pillar"}
# 1.12 recipes 1.21 cannot make (see PORTLOG.md)
DROPPED_RECIPES = {"berry_medley_double"}


def vanilla(item, data):
    return VANILLA_META.get((item, data), "minecraft:white_wool" if item == "minecraft:wool" else item)


def convert_ingredient(i):
    if isinstance(i, list):
        alternatives = [x for x in (convert_ingredient(x) for x in i) if x is not None]
        return alternatives or None
    if i.get("type") == "forge:ore_dict":
        return {"tag": ORE[i["ore"]]} if i["ore"] in ORE else None
    item = i["item"]
    if item.startswith("natura:"):
        new = OLD.get((item[7:], i.get("data", 0)))
        if new is None:
            return None
        return {"item": nid(new)}
    return {"item": vanilla(item, i.get("data", 0))}


def convert_result(r):
    item = r["item"]
    if item.startswith("natura:"):
        new = OLD.get((item[7:], r.get("data", 0)))
        if new is None:
            return None
        item = nid(new)
    else:
        item = vanilla(item, r.get("data", 0))
    out = {"id": item}
    if r.get("count", 1) != 1:
        out["count"] = r["count"]
    return out


SKIPPED = []


def recipes():
    root = os.path.join(SRC, "recipes")
    for dirpath, _, files in os.walk(root):
        for f in files:
            if f.startswith("_") or not f.endswith(".json"):
                continue
            with open(os.path.join(dirpath, f), encoding="utf-8") as fh:
                r = json.load(fh)
            name = f[:-5]
            out = None
            if name in DROPPED_RECIPES:
                continue
            result = convert_result(r["result"])
            if result is None:
                SKIPPED.append(os.path.relpath(os.path.join(dirpath, f), root))
                continue
            kind = r["type"]
            if kind in ("minecraft:crafting_shaped", "forge:ore_shaped"):
                key = {k: convert_ingredient(v) for k, v in r["key"].items()}
                if any(v is None for v in key.values()):
                    SKIPPED.append(os.path.relpath(os.path.join(dirpath, f), root))
                    continue
                out = {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": r["pattern"], "key": key, "result": result}
            elif kind in ("minecraft:crafting_shapeless", "forge:ore_shapeless"):
                ingredients = [convert_ingredient(v) for v in r["ingredients"]]
                if any(v is None for v in ingredients):
                    SKIPPED.append(os.path.relpath(os.path.join(dirpath, f), root))
                    continue
                out = {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": ingredients, "result": result}
            else:
                SKIPPED.append(os.path.relpath(os.path.join(dirpath, f), root))
                continue
            if "group" in r:
                out["group"] = r["group"]
            config = [c for c in r.get("conditions", []) if c.get("type") == "natura:config_option_enabled"]
            if config:
                out = {"neoforge:conditions": [{"type": "natura:config_option_enabled", "config_setting": c["config_setting"]} for c in config], **out}
            w(os.path.join(DATA, NS, "recipe", name + ".json"), out)


def smelt(name, ingredient, result, xp):
    w(os.path.join(DATA, NS, "recipe", name + ".json"), {"type": "minecraft:smelting", "category": "misc", "ingredient": {"item": ingredient},
                                                         "result": {"id": result}, "experience": xp, "cookingtime": 200})


def smelting():
    """1.12 FurnaceRecipes.addSmeltingRecipe: overworld logs and the redwood pieces give charcoal, flour bread, the saguaro
    cactus green, raw imp meat cooked imp meat."""
    smelt("bread_from_barley_flour", nid("barley_flour"), "minecraft:bread", 0.5)
    smelt("bread_from_wheat_flour", nid("wheat_flour"), "minecraft:bread", 0.5)
    smelt("green_dye_from_saguaro", nid("saguaro"), "minecraft:green_dye", 0.2)
    smelt("impmeat_cooked", nid("impmeat_raw"), nid("impmeat_cooked"), 0.2)
    for log in ["maple_log", "amaranth_log", "silverbell_log", "tiger_log", "eucalyptus_log", "hopseed_log", "sakura_log", "willow_log",
                "redwood_bark", "redwood_root", "redwood_heart"]:
        w(os.path.join(DATA, NS, "recipe", f"charcoal_from_{log}.json"), {"type": "minecraft:smelting", "category": "misc",
                                                                          "ingredient": {"item": nid(log)}, "result": {"id": "minecraft:charcoal"},
                                                                          "experience": 0.15, "cookingtime": 200})


# ---------------------------------------------------------------- languages

LANGS = {"en_US": "en_us", "de_DE": "de_de", "en_UD": "en_ud", "ja_JP": "ja_jp", "ru_RU": "ru_ru", "zh_CN": "zh_cn", "zh_TW": "zh_tw"}


def parse_lang(path):
    out = {}
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n").rstrip("\r")
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, _, v = line.partition("=")
            out[k.strip()] = v.replace("\\n", "\n")
    return out


def languages():
    english = parse_lang(os.path.join(SRC, "lang", "en_US.lang"))
    for old_name, new_name in LANGS.items():
        src = parse_lang(os.path.join(SRC, "lang", old_name + ".lang"))
        out = {}
        for old_key, new_key in KEYS.items():
            base = old_key.split("#")[0]
            for suffix in (".name", ".tooltip"):
                if base + suffix in src:
                    out[new_key + ("" if suffix == ".name" else ".tooltip")] = src[base + suffix]
        for tab, new in (("NaturaGeneral", "general"), ("NaturaWorld", "world"), ("NaturaDecorative", "decorative")):
            if f"itemGroup.{tab}" in src:
                out[f"itemGroup.natura.{new}"] = src[f"itemGroup.{tab}"]
        w(os.path.join(ASSETS, "lang", new_name + ".json"), dict(sorted(out.items())))
    missing = [k for k in KEYS if k.split("#")[0] + ".name" not in english]
    if missing:
        print("no English name for:", missing)


# ---------------------------------------------------------------- game test area

def gametest_area():
    """vanilla's empty 1x1x1 structure, resized to 8x8x8 (data version 3955 = 1.21.1)"""
    empty = bytes.fromhex(
        "0a0000090004" "73697a65" "0300000003" "00000001" "00000001" "00000001"
        "090008" "656e746974696573" "0000000000"
        "090006" "626c6f636b73" "0a00000001" "090003" "706f73" "0300000003" "000000000000000000000000"
        "030005" "7374617465" "00000000" "00"
        "090007" "70616c65747465" "0a00000001" "080004" "4e616d65" "000d" "6d696e6563726166743a616972" "00"
        "03000b" "4461746156657273696f6e" "00000f73" "00")
    size_at = empty.index(bytes.fromhex("0300000003")) + 5
    area = empty[:size_at] + struct.pack(">3i", 8, 8, 8) + empty[size_at + 12:]
    path = os.path.join(DATA, NS, "structure", "gametest_area.nbt")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with gzip.GzipFile(path, "wb", mtime=0) as f:
        f.write(area)


# ---------------------------------------------------------------- output

def main():
    copy_textures()
    copy_models()
    woods()
    plants()
    items()
    recipes()
    smelting()
    languages()
    gametest_area()
    for name, table in LOOT.items():
        w(os.path.join(DATA, NS, "loot_table", "blocks", name + ".json"), table)
    for kind, table in (("block", BLOCK_TAGS), ("item", ITEM_TAGS)):
        for name, values in table.items():
            ns, _, path = name.partition(":")
            w(os.path.join(DATA, ns, "tags", kind, path + ".json"), {"values": values})
    print(f"blocks with loot: {len(LOOT)}, recipes skipped for later phases: {len(SKIPPED)}")


main()
