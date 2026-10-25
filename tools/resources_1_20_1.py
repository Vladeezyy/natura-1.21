#!/usr/bin/env python3
"""
resources_1_20_1.py: turns a mod's 1.21.1 resources into the Minecraft 1.20.1 formats (Forge 47 + Fabric 0.92). Idempotent.

  python3 -I scripts/resources_1_20_1.py <resources dir> --mod <mod id> [--neoforge-jar <neoforge-21.1.x-universal.jar>]
                                          [--materials tritanium,dilithium]

--code-tags: c: tags only the code checks, e.g. "worldgen/biome:is_forest,is_nether" (1.21 names; neither 1.20.1 loader has
them: Forge says forge:is_forest, Fabric API 0.92 c:forest): shipped from NeoForge 21.1 plus the loaders' own tags.
--neoforge-jar: for the contents of the 1.21 c: tags the data refers to (~/.gradle/caches/modules-2/files-2.1/
net.neoforged/neoforge/21.1.*/*/neoforge-21.1.*-universal.jar); without it those tags are left to the loaders (and are
then empty on both: Forge has forge:, Fabric the old c: names). --materials: the mod's own materials, also put into the
1.20.1 forge: tags and Fabric's old c: names so other mods find them.

What changes (generalised from the Matter Overdrive backport, mc/1.20.1: tools/backport_resources_1_20_1.py):
- data folders are plural (recipes, loot_tables, tags/items, tags/blocks, structures, advancements, ...);
- recipe results are {"item": ...} (cooking recipes: just the id); NeoForge recipe conditions ("neoforge:conditions")
  become Forge ones ("conditions", condition types neoforge: -> forge:);
- loot tables: copy_components from a block entity -> copy_nbt Items + copy_name; enchanted_count_increase (looting) ->
  looting_enchant; match_tool item predicates in the 1.20.1 form (items as a list or a tag, enchantments list);
- the 1.21 c: tags the data refers to are shipped with the mod (every entry optional);
- vanilla entries of the mod's tags are optional (one missing 1.20.5+ entry drops the whole tag in 1.20.1);
- NeoForge biome modifiers -> Forge ones (data/<mod>/forge/biome_modifier, forge: types, "forge:conditions");
- OBJ models: neoforge:obj -> forge:obj; structure NBT gets the 1.20.1 data version; pack.mcmeta with pack format 15.
"""
import argparse
import gzip
import json
import re
import shutil
import struct
import zipfile
from pathlib import Path

DATA_VERSION_1_20_1 = 3465
PLURAL = {"recipe": "recipes", "loot_table": "loot_tables", "structure": "structures", "advancement": "advancements",
          "predicate": "predicates", "item_modifier": "item_modifiers", "function": "functions"}
TAG_PLURAL = {"item": "items", "block": "blocks", "entity_type": "entity_types", "fluid": "fluids", "game_event": "game_events",
              "worldgen/biome": "worldgen/biome"}
# the 1.21 c: biome tags -> what Fabric API 0.92 calls them (Forge 1.20.1: forge:<the same name>, added as well)
FABRIC_0_92_BIOME = {"is_beach": "beach", "is_end": "in_the_end", "is_forest": "forest", "is_hill": "extreme_hills",
                     "is_jungle": "jungle", "is_mountain": "mountain", "is_nether": "in_nether", "is_overworld": "in_overworld",
                     "is_plains": "plains", "is_river": "river", "is_swamp": "swamp", "is_desert": "desert", "is_ocean": "ocean",
                     "is_savanna": "savanna", "is_taiga": "taiga", "is_badlands": "badlands", "is_snowy": "snowy",
                     "is_mushroom": "mushroom", "is_sandy": "desert"}
# referenced c: tags that NeoForge 21.1 does not have
VANILLA_ONLY = {"flowers/small": "#minecraft:small_flowers", "flowers/tall": "#minecraft:tall_flowers"}

ap = argparse.ArgumentParser()
ap.add_argument("root")
ap.add_argument("--mod", required=True)
ap.add_argument("--neoforge-jar")
ap.add_argument("--materials", default="")
# c: tags only the code checks (TagKey.create), so the data does not show them: kind:name,name;kind:name...
# e.g. --code-tags "worldgen/biome:is_forest,is_nether" (biome tags also get the forge: and Fabric 0.92 names)
ap.add_argument("--code-tags", default="")
args = ap.parse_args()
MOD = args.mod
MATERIALS = [m for m in args.materials.split(",") if m]
root = Path(args.root)
data = root / "data"
neo = zipfile.ZipFile(args.neoforge_jar) if args.neoforge_jar else None


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def save(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def move_dir(src, dst):
    if not src.exists():
        return
    for f in sorted(src.rglob("*")):
        if f.is_file():
            target = dst / f.relative_to(src)
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.move(str(f), str(target))
    shutil.rmtree(src)


def forge_conditions(conds):
    """NeoForge condition objects -> Forge 1.20.1 ones (same shape, neoforge: types -> forge:)."""
    return json.loads(json.dumps(conds).replace('"neoforge:', '"forge:'))


# 1. folders
for ns in data.iterdir():
    if not ns.is_dir():
        continue
    for old, new in PLURAL.items():
        move_dir(ns / old, ns / new)
    tags = ns / "tags"
    if tags.exists():
        for old, new in TAG_PLURAL.items():
            move_dir(tags / old, tags / new)

# 2. recipes
for f in data.glob("*/recipes/**/*.json"):
    r = load(f)
    res = r.get("result")
    if isinstance(res, dict) and "id" in res:
        res["item"] = res.pop("id")
    if r.get("type") in ("minecraft:smelting", "minecraft:blasting", "minecraft:smoking", "minecraft:campfire_cooking") and isinstance(res, dict):
        r["result"] = res["item"]
    if "neoforge:conditions" in r:
        r["conditions"] = forge_conditions(r.pop("neoforge:conditions"))
    save(f, r)


# 3. loot tables
def fix_functions(funcs):
    out = []
    for fn in funcs:
        if fn.get("function") == "minecraft:copy_components" and fn.get("source") == "block_entity":
            include = fn.get("include", [])
            if "minecraft:container" in include:
                out.append({"function": "minecraft:copy_nbt", "source": "block_entity",
                            "ops": [{"source": "Items", "target": "BlockEntityTag.Items", "op": "replace"}]})
            if "minecraft:custom_name" in include:
                out.append({"function": "minecraft:copy_name", "source": "block_entity"})
            if any(i.startswith(MOD + ":") for i in include):
                out.append({"function": MOD + ":copy_components"})  # the mod's own loot function, to write
        elif fn.get("function") == "minecraft:enchanted_count_increase" and fn.get("enchantment", "minecraft:looting") == "minecraft:looting":
            # 1.21 looting bonus -> the 1.20.1 looting_enchant (count, limit)
            out.append({k: v for k, v in fn.items() if k != "enchantment"} | {"function": "minecraft:looting_enchant"})
        else:
            out.append(fn)
    return out


def fix_item_predicate(pred):
    """1.21 item predicate (items as one id or a tag, sub-predicates) -> 1.20.1 (items list or tag, enchantments list)"""
    items = pred.get("items")
    if isinstance(items, str):
        if items.startswith("#"):
            pred.pop("items")
            pred["tag"] = items[1:]
        else:
            pred["items"] = [items]
    subs = pred.pop("predicates", None)
    if subs:
        for e in subs.get("minecraft:enchantments", []):
            ench = e.pop("enchantments")
            pred.setdefault("enchantments", []).append({"enchantment": ench if isinstance(ench, str) else ench[0], **e})


def walk_loot(node):
    if isinstance(node, dict):
        if node.get("condition") == "minecraft:match_tool" and "predicate" in node:
            fix_item_predicate(node["predicate"])
        if "functions" in node:
            node["functions"] = fix_functions(node["functions"])
        for v in node.values():
            walk_loot(v)
    elif isinstance(node, list):
        for v in node:
            walk_loot(v)


for f in data.glob("*/loot_tables/**/*.json"):
    t = load(f)
    walk_loot(t)
    save(f, t)


# 4. the 1.21 c: tags the data uses, from NeoForge 21.1 (all entries optional)
def optional(value):
    if isinstance(value, dict):
        return {"id": value["id"], "required": False}
    return {"id": value, "required": False}


def referenced_c_tags():
    found = set()
    for f in data.rglob("*.json"):
        if f.as_posix().split("/data/")[-1].startswith("c/tags/"):
            continue
        text = f.read_text(encoding="utf-8")
        for m in re.finditer(r'"#c:([a-z0-9_/]+)"', text):
            found.add(m.group(1))
        for m in re.finditer(r'"tag":\s*"c:([a-z0-9_/]+)"', text):
            found.add(m.group(1))
    return found


def copy_c_tag(kind, path, done):
    if (kind, path) in done or neo is None:
        return
    done.add((kind, path))
    target = data / "c" / "tags" / TAG_PLURAL[kind] / (path + ".json")
    if target.exists() and any(path.endswith(m) for m in MATERIALS):
        return  # the mod's own
    name = f"data/c/tags/{kind}/{path}.json"
    if path in VANILLA_ONLY:
        save(target, {"replace": False, "values": [VANILLA_ONLY[path]]})
        return
    if name not in neo.namelist():
        print("no NeoForge tag", name)
        return
    tag = json.loads(neo.read(name))
    values = []
    for v in tag["values"]:
        vid = v["id"] if isinstance(v, dict) else v
        if vid.startswith("#c:"):
            copy_c_tag(kind, vid[3:], done)
            nested = data / "c" / "tags" / TAG_PLURAL[kind] / (vid[4:] + ".json")
            # ours now: required; a nested tag NeoForge does not have either stays optional (a missing required one
            # drops the whole tag: "Couldn't load tag c:tools/shear as it is missing following references: #c:tools/shears")
            values.append(vid if nested.exists() else optional(vid))
        else:
            values.append(optional(v))
    old = load(target)["values"] if target.exists() else []
    merged = old + [v for v in values if v not in old]
    save(target, {"replace": False, "values": merged})


done = set()
for path in sorted(referenced_c_tags()):
    copy_c_tag("item", path, done)
for group in [g for g in args.code_tags.split(";") if g]:
    kind, names = group.split(":", 1)
    for name in names.split(","):
        copy_c_tag(kind, name, done)
        target = data / "c" / "tags" / TAG_PLURAL[kind] / (name + ".json")
        tag = load(target) if target.exists() else {"replace": False, "values": []}
        # the 1.20.1 loaders' own tags of the same kind (other mods' biomes are in those)
        extra = [f"#forge:{name}"] + ([f"#c:{FABRIC_0_92_BIOME[name]}"] if kind == "worldgen/biome" and name in FABRIC_0_92_BIOME else [])
        for e in extra:
            if optional(e) not in tag["values"]:
                tag["values"].append(optional(e))
        save(target, tag)

# the mod's materials in the 1.20.1 forge: tags (same paths) and Fabric's old c: names
for kind in ("items", "blocks"):
    for f in sorted((data / "c" / "tags" / kind).rglob("*.json")):
        rel = f.relative_to(data / "c" / "tags" / kind).with_suffix("").as_posix()
        if "/" not in rel or not any(rel.endswith(m) for m in MATERIALS):
            continue
        group, material = rel.split("/", 1)
        save(data / "forge" / "tags" / kind / f"{group}/{material}.json", {"replace": False, "values": [f"#c:{rel}"]})
        if not (data / "forge" / "tags" / kind / f"{group}.json").exists():
            save(data / "forge" / "tags" / kind / f"{group}.json", {"replace": False, "values": [f"#forge:{group}/{material}"]})
        group_name = {"storage_blocks": "blocks"}.get(group, group)
        save(data / "c" / "tags" / kind / f"{material}_{group_name}.json", {"replace": False, "values": [f"#c:{rel}"]})

# the mod's tags: vanilla entries optional (some name 1.20.5+ blocks, and one missing entry drops the whole tag)
for f in list(data.glob(MOD + "/tags/**/*.json")) + list(data.glob("minecraft/tags/**/*.json")):
    tag = load(f)
    tag["values"] = [optional(v) if (v["id"] if isinstance(v, dict) else v).lstrip("#").startswith("minecraft:") else v for v in tag["values"]]
    save(f, tag)

# 5. biome modifiers
src = data / MOD / "neoforge" / "biome_modifier"
dst = data / MOD / "forge" / "biome_modifier"
if src.exists():
    for f in sorted(src.glob("*.json")):
        m = json.loads(f.read_text(encoding="utf-8").replace('"neoforge:conditions"', '"forge:conditions"').replace('"neoforge:', '"forge:'))
        save(dst / f.name, m)
    shutil.rmtree(data / MOD / "neoforge")


# 6. structure templates: the 1.20.1 data version
def set_data_version(path):
    raw = gzip.decompress(path.read_bytes())
    key = b"\x03\x00\x0bDataVersion"  # TAG_Int "DataVersion" at the root: patch the 4 bytes after the name
    i = raw.find(key)
    if i < 0:
        return
    j = i + len(key)
    if struct.unpack(">i", raw[j:j + 4])[0] != DATA_VERSION_1_20_1:
        raw = raw[:j] + struct.pack(">i", DATA_VERSION_1_20_1) + raw[j + 4:]
        path.write_bytes(gzip.compress(raw))


for f in data.glob("*/structures/**/*.nbt"):
    set_data_version(f)

# OBJ models: the Forge 1.20.1 loader name
for f in (root / "assets").rglob("models/**/*.json"):
    text = f.read_text(encoding="utf-8")
    if '"neoforge:obj"' in text:
        f.write_text(text.replace('"neoforge:obj"', '"forge:obj"'), encoding="utf-8")

# 7. pack.mcmeta
save(root / "pack.mcmeta", {"pack": {"description": f"{MOD} resources", "pack_format": 15}})
print("done")
