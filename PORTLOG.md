# Natura 1.21.1 port log

Port of Natura 4.3.2 (Minecraft 1.12.2, progwml6 / Slime Knights, code CC0, textures CC-BY 3.0) to Minecraft 1.21.1 /
NeoForge 21.1. The 1.12 source is the specification: `git clone -b 1.12 https://github.com/progwml6/Natura.git _original`
(used at d5b4bdd). Behaviour is kept as in 1.12, quirks included; every difference is listed here.

## Phases

1. Woods: logs, leaves, saplings (no trees yet), planks, slabs, stairs, fences, fence gates, doors, trapdoors,
   buttons, pressure plates, bookshelves, workbenches, sticks. ✅
2. Trees: the 1.12 tree generators for sapling growth. ✅
3. Overworld plants and food: crops (barley, cotton), berry bushes, saguaro, bluebells, colored grass (+ slab, stairs),
   materials, food, soups, seed bags, bone meal bag. ✅
4. Nether blocks: heat sand, tainted soil, nether glass, thorn vines, glowshrooms, respawn obelisk, netherrack furnace,
   blaze hopper, blaze rails, nether lever / button / pressure plate; the clouds. ✅
5. Tools: nether wood and quartz tools, kamas, bows, flint and blaze, imp armor. ✅
6. Entities: imp, heatscar spider (+ baby), nitro creeper. ✅
7. World generation: trees, berry bushes, crops, clouds, glowshrooms, saguaro, vines, nether trees, soil veins. ✅
8. Integrations (JEI, Jade), logo, release. ✅

## Names

1.12 kept variants as metadata of shared blocks; 1.21 has one block per variant:

| 1.12 | 1.21.1 |
|---|---|
| `overworld_logs` / `overworld_logs2` / `nether_logs` type=X | `X_log` |
| `redwood_logs` bark / heart / root | `redwood_bark`, `redwood_heart`, `redwood_root` |
| `nether_logs2` meta 0-11 (trunk quarters) / 12-15 (bark) | `bloodwood_log` (`piece` 0-11 = 1.12 meta) / `bloodwood_bark` |
| `overworld_leaves(2)` / `redwood_leaves` / `nether_leaves(2)` type=X | `X_leaves`; darkwood: `darkwood_leaves`, `darkwood_flowering_leaves`, `darkwood_fruit_leaves` |
| `overworld_sapling(2)` / `redwood_sapling` / `nether_sapling(2)` foliage=X | `X_sapling` |
| `overworld_planks` / `nether_planks` type=X | `X_planks` |
| `overworld_slab(2)` / `nether_slab` type=X | `X_slab` |
| `overworld_stairs_X` / `nether_stairs_X` | `X_stairs` |
| `X_trap_door` | `X_trapdoor` (vanilla name) |
| `overworld_doors` / `nether_doors` item + `overworld_door_X` block | `X_door` (block and item) |
| `overworld_bookshelves` / `nether_bookshelves` type=X | `X_bookshelf` |
| `overworld_workbenches` / `nether_workbenches` type=X | `X_workbench` |
| `sticks` meta | `X_stick` |
| `colored_grass` / `_slab` / `colored_grass_stairs_X` | `topiary_grass`, `bluegrass`, `autumnal_grass` (+ `_slab`, `_stairs`) |
| `overworld_berrybush_X` / `nether_berrybush_X` | `X_bush` |
| `bluebells_flower` | `bluebells` |
| `materials`, `edibles`, `empty_bowls`, `seed_bags`, `overworld_seeds`, `overworld_seed_bags` metas | the meta names (`barley`, `raspberry`, `ghostwood_bowl`, `barley_seeds`, ...); `potashapple` / `cactusjuice` → `potash_apple` / `cactus_juice` |
| `clouds` white / dark / ash / sulfur | `white_cloud`, `dark_cloud`, `ash_cloud`, `sulfur_cloud` |
| `nether_heat_sand`, `nether_tainted_soil` metas, `nether_glass` soul / heat, `nether_thorn_vines` | `heat_sand`, `tainted_soil`, `tainted_farmland_dry`, `tainted_farmland_heated`, `soul_glass`, `heat_glass`, `thorn_vines` |
| `nether_glowshroom` metas, `nether_X_large_glowshroom` | `X_glowshroom`, `X_large_glowshroom` |
| `netherrack_furnace` + `lit_netherrack_furnace` | `netherrack_furnace` (with a lit state) |
| `blaze_rail_golden` | `blaze_powered_rail` (also `blaze_activator_rail`, `blaze_detector_rail`) |
| `soups` metas | `X_mushroom_stew`, `X_glowshroom_stew`, `glowshroom_stew` (1.12 `vanilla_glowshroomstew`), `berry_medley` |

The translations come from the 1.12 language files with the keys renamed (`tile.natura.overworld_logs.maple.name` →
`block.natura.maple_log`), the Mantle tooltips (`.tooltip`) too. Recipes are the 1.12 JSON recipes with the items
renamed and the ore dictionary turned into tags (`plankWood` → `#minecraft:planks`, `stickWood` → `#c:rods/wooden`, ...).

The port adds Ukrainian (`uk_ua`), Spanish (`es_es`) and Portuguese (Brazil) (`pt_br`), and the keys the 1.12 Russian and
German files lacked (the doors of five woods, the Jade setting); they are in `tools/lang` and written for the port, not by
native speakers. The 1.12 Russian darkwood flower tooltip had a literal `\Ц` instead of a line break; fixed.

## Phase 1: woods

As in 1.12:
- Strength: the effective 1.12 numbers (1.12 stored resistance x3 and divided by 5, and `setHardness` raised it to
  hardness x5). Planks, slabs and stairs 2 / 2 (vanilla planks resist 3), logs 1.5 / 3, nether logs 3.5 / 24, redwood
  pieces 2 / 2, bloodwood 8 / 8 with metal sounds, fences and gates 2 / 3, doors and trapdoors 3 / 3, buttons and
  plates 0.5, bookshelves 1.5, workbenches 2.5.
- Fire: logs and overworld planks 5 / 20, all slabs 5 / 20 (the nether slabs too), overworld leaves 30 / 60, redwood
  leaves 5 / 20. Nether logs, leaves and planks do not burn, and neither do Natura stairs, fences, gates, doors,
  trapdoors, buttons, plates, bookshelves or workbenches (1.12 never registered them with the fire block).
- Leaves drop their sapling 1 in 25 (darkwood 1 in 20) with the 1.12 fortune steps (21, 17, 10), no apples and no
  sticks; darkwood fruit leaves drop a potash apple instead (phase 3 item). Blood leaves also drop redstone, rolled
  with a Random made from the same fixed seed every time, so it depends only on the fortune level.
- Redwood leaves keep their own decay: a leaf checks only after a log within 4 or a leaf within 2 blocks broke, and
  decays when no log is left in the 9x9x9 cube around it (the other leaves use vanilla decay, as they did in 1.12).
- Harvesting fusewood sets off an explosion at the block corner that hurts but breaks nothing (2.0 on Hard, 1.75 on
  Easy / Normal, none on Peaceful).
- Bloodwood log placement picks the trunk quarter from the hit position with the 1.12 coordinates (on side faces one
  of them is the fixed face coordinate, so only two of the four quarters can be placed from each side).
- Bookshelves drop 3 books even with Silk Touch (1.12 bookshelves were not full cubes, so no silk harvest) and power
  enchanting tables. Workbenches open the vanilla crafting grid.
- Leaves tints from the 1.12 LeavesColorizer: maple 0xcc5718, amaranth biome foliage + 0x222222 (added to the packed
  colour), eucalyptus / hopseed / redwood biome foliage, the others untinted; items use the 1.12 static colours.
- Logs, the redwood pieces and planks smelt into charcoal (1.12 smelting list); 4 sticks from two planks placed
  diagonally (the 1.12 Natura stick recipe).
- Trapdoors keep the 1.12 model that does not turn with the facing (the iron trapdoor templates).

Differences:
- Every 1.12 metadata variant is its own block (see "Names").
- Bloodwood: 1.12 had four bark metas (12-15) and two bloodwood items (0, 15); 1.21 has `bloodwood_log` (the 12
  trunk pieces) and one `bloodwood_bark`. Both are named "Bloodwood" like the one 1.12 name.
- Tool tiers: 1.12 needed a stone axe for fusewood and an iron axe for bloodwood to mine them faster; every axe is
  effective in 1.21 (both still drop by hand, as in 1.12).
- Leaves decay: vanilla 1.21 (distance 7 through leaves) instead of the 1.12 4-block check, except redwood. The
  redwood flag is cleared once a log is found (1.12 kept checking every random tick).
- Fences and fence gates connect to all wooden fences (1.12: any wood material fence).

## Phase 2: trees

The 1.12 generators are ported line by line (`world/tree`), with the 1.12 random call order: OverworldTreeGenerator
(maple, silverbell, amaranth, tiger), Willow, Eucalyptus, Hopseed, Sakura (the 1.12 big oak), Redwood, Ghostwood (big
oak on nether soil), Fusewood, Darkwood and Bloodwood. The redwood trunk rings and roots (about 1200 block placements
in 1.12) are data in `RedwoodShapes`, generated from the 1.12 source.

As in 1.12:
- Sapling growth: vanilla ticking, the sapling is removed, the tree grown, and the sapling put back if nothing grew
  where it stood. The sapling sizes are the 1.12 ones (maple / silverbell 4-5, amaranth 9-16, tiger 6-9, willow 4-8,
  eucalyptus 6-8, hopseed 2-4, darkwood / fusewood 4-5, bloodwood 8-15 down from the ceiling, redwood 80-139).
- The overworld tree check for space never fails (1.12 bug), so those trees grow through anything; eucalyptus,
  hopseed, willow, fusewood and darkwood check their space, bloodwood places over anything.
- Saplings look for the ground down to the "Sea level" config option (64): in a normal world a sapling on dirt below
  y 65 does not grow, as in 1.12. Flat worlds use "Flat Sea level" (1), counted from the world bottom.
- Willows only below y 128, eucalyptus finds its ground between y 96 and 32 when not planted on dirt, bloodwood looks
  for its ceiling up to y 120 (absolute heights, as in 1.12).
- Redwood: only with 40 redwood saplings in the 7x7 square; they all turn into the tree, the roots grow through dirt.

Differences:
- Leaves get the vanilla 1.21 distance when the tree is done, at most 6: 1.12 leaves only checked for decay once
  something next to them changed, so the far ones of a grown tree (willow curtains, the hopseed crown edge) stayed
  until disturbed; a vanilla leaf recomputes its distance on a neighbour update and then decays, as in 1.12.
- Blocks are sent to the clients (1.12 placed the hopseed trunk with flag 0, invisible until the chunk reloaded).
- 1.12 world limits (y 0, 256) are the 1.21 world bottom and top.
- The 1.12 "Retroactive Generation" option is gone (1.21 has no retrogen).
- Config: `config/natura-common.toml`, the same options, sections and defaults as the 1.12 `natura.cfg` (the world
  generation ones are used from phase 7).

## Phase 3: overworld plants and food

As in 1.12:
- Barley (4 stages) and cotton (5 stages) grow like vanilla crops on farmland (planted only on farmland, but they stay
  on grass and dirt), bone meal adds 2-5 stages. A ripe crop is picked by clicking it (right or left): one crop at the
  player's feet, back to stage 1 (barley) / 2 (cotton). Broken ripe: the crop and three tries at a seed (2/3 barley,
  5/8 cotton), never with Fortune (1.12 dropped crops with fortune 0). Crops are in no creative tab.
- Berry bushes grow from small to ripe and stack up to three high; overworld ones 1 in 20 random ticks with light 8,
  nether ones 1 in 75 in any light. A ripe bush is picked by clicking (one berry, back to stage 2). Bone meal grows a
  small bush 1-2 stages, and on a grown bush may start a new one above, but never ripens it. Bushes can be placed
  anywhere; overworld ones burn (4 / 25).
- Saguaro: connects to other saguaro and opaque blocks on all six sides, hurts (1) what touches it, grows fruit on a
  free side (1 in 20) while it rains with air above it. Placed on sand or saguaro, it does not break when that goes.
  A baby saguaro grows (1 in 200, twice) into a saguaro (1 in 20 a big candelabra one) and breaks without sand. The
  fruit drops the saguaro fruit food (3, 0.3), which plants a baby on sand or terracotta. Saguaro smelts into green dye.
- Colored grass: topiary (biome grass colour), bluegrass and autumnal (the Natura colour maps by temperature and
  rainfall), plains plants grow on it; slabs and stairs.
- Food: 1.12 nutrition, saturation, eating time and effects (always applied). Imp meat poisons even cooked; the
  bloodwood / darkwood / fusewood glowshroom stews feed 3 instead of 6. Stews stack to 1 and give their bowl back (the
  wooden ones for the nether bowls). The potash apple is eaten at once (1 tick, see phase 1).
- Seed bags plant the 3x3 around the clicked farmland (soul sand for nether wart), the bone meal bag fertilizes the
  3x3; 1.12 recipes for the bags, flour, bread from flour, cake, wool / string from cotton, gunpowder from sulfur, ...
  The wheat flour recipe follows the "Enable wheat to flour recipe" option (`natura:config_option_enabled` condition).
- Cows and sheep follow a player holding any 1.12 "materials" item, chickens barley or cotton seeds (speed 0.25). The
  1.12 barley breeding never worked (it compared stacks by identity), so barley does not breed them.
- The 1.12 ore dictionary names used by recipes are tags: `c:crops/barley`, `c:crops/<berry>`, `c:dusts/sulfur`,
  `c:dyes/blue`, `natura:wooden_bowls` (vanilla and Natura bowls), `c:seeds/barley`, ...

Differences:
- The double berry medley recipe (two bowls, four berries) made one stack of two stews that cannot stack; 1.21 cannot
  craft that, so only the single recipe is left (it needs three berries per stew, the double one two).
- The grass colour maps use the biome temperature without the 1.12 height cooling (1.21 keeps that private).
- Saguaro connections are block state properties (1.12 computed them when drawing).

## Phase 4: nether blocks and clouds

As in 1.12:
- Heat sand: falling, collision 14/16, burns (1) players without boots and mobs that are not fire immune.
- Tainted soil (and its dry / heated farmland look-alikes): nether plants grow on it, it burns forever (fire source).
- Soul glass has no collision and slows (II, 1 s) whoever is in it; heat glass burns like heat sand. Both resist 1800
  and drop only with Silk Touch. They are smelted from soul sand / heat sand.
- Thorn vines: vines with hardness 1 that prick (1, cactus or lava) what moves through them, 1 in 30 rolled with a
  Random made from a fixed seed every time, so the roll always comes out the same (as in 1.12).
- Glowshrooms glow 9, spread like mushrooms, stay on mycelium, netherrack, soul sand or tainted soil in any light; bone
  meal (40%) grows the 1.12 huge green / blue / purple glowshroom. Huge glowshroom pieces break at once (no hardness in
  1.12), only their top 1/16 is solid, slow falling things, give mobs (not players) nausea, and drop 0-2 small ones.
- Respawn obelisk: right click (not sneaking) sets the respawn point, lights it (7), says "<name>: Spawn point set!";
  the player respawns on top of it in any dimension.
- Netherrack furnace: a furnace whose fuel burns twice as long. Blaze hopper: a vanilla hopper. Blaze rails: top speed
  0.65; the powered one also pushes off a solid block (on the axis across the rail, as in 1.12) and speeds up / brakes.
- Netherrack lever, button (stone timing, wooden clicks) and pressure plate (mobs only, like stone).
- Clouds: you sink into them (only the bottom 1/16 is solid, nothing on a cloud), falling slows to a crawl and does no
  damage; a sulfur cloud explodes (1) with flint and steel or a burning arrow; clouds never drop from explosions.
  "Enable Clouds" (off by default) only controls their world generation, as in 1.12.

Differences:
- One netherrack furnace block with a lit state instead of two blocks.
- Breaking an obelisk clears the breaker's respawn point (1.12 reset the spawn dimension, leaving a stale position).
- Tainted soil also carries nether wart and the 1.21 nether plants (1.12: every nether plant type).

## Phase 5: tools and imp armor

As in 1.12:
- Ghostwood tools are wooden, darkwood and nether quartz stone, fusewood iron, each with the vanilla stats of that
  material (1.12 used the vanilla materials). Bloodwood has its own: mines like iron (not obsidian: 1.12 set its harvest
  level 2), 350 uses, speed 7, +3 damage, enchantability 24, no repair item. The 1.12 ids are kept (`ghostwood_sword`,
  `netherquartz_kama`, ...).
- Kamas are swords that work as shears: cobweb and leaves break at 15, wool at 5, they shear sheep (any shearable mob)
  and get the sheared drops of leaves, grass and vines (the leaves loot tables use the NeoForge `shears_dig` ability, so
  any shears do).
- Bows: ghostwood 384, bloodwood 1501, darkwood 162, fusewood 28 uses, otherwise vanilla bows. Their "pull" model
  property compared the drawn item with a field that was still null when the bow was made, so they only ever show the
  first pulling frame; kept.
- Flint and blaze: 64 uses, lights fire in the air next to the clicked face (portals too), every use wears it.
- Imp armor ("Impskin"): durability factor 33, 1 / 2 / 3 / 1 (helmet, chestplate, leggings, boots), enchantability 15,
  no repair item. The creative tab pieces come enchanted (Protection / Blast Protection / Projectile Protection /
  Feather Falling, each with Fire Protection), the crafted ones do not.

Differences:
- 1.12 kamas also broke the block normally after shearing it (sheared drops plus the normal ones); here a kama breaking
  leaves gives the sheared drops only, like shears.

## Phase 6: mobs

As in 1.12 (ids `imp`, `heatscarspider`, `babyheatscarspider`, `nitrocreeper`, the 1.12 spawn egg colours):
- Imp: fire-proof nether animal, 40 health, pig sounds, the 1.12 model. Follows players holding a Natura stew, runs
  from players, breeds on the glowshroom stews and berry medley (giving the bowl back). Drops 0-2 raw imp meat and
  0-5 imp leather (+ looting). Spawns in every nether biome (10, groups of 8-12) on any block in any light.
- Heatscar spider (2.7 x 1.9, 50 health, speed 0.45, damage 4, 25 xp) and its baby (1.2 x 0.8, vanilla spider
  stats): fire-proof, jump 0.62, extra fall damage past 5 blocks on top of the normal one, set the mobs they hit on
  fire (5 s Normal, 10 s Hard; players were not EntityLiving in 1.12, so not players), drop 0-5 flame string. The big
  one leaves 2-4 babies ("Minimum/Maximum Baby Heatscar Spiders on Spider Death") when it dies or despawns (1.12
  setDead). They spawn only with "Enable Heatscar Spiders" (off by default; the biome modifiers have the config
  condition), with no light check.
- Nitro creeper: fire-proof, fuse 12 + the difficulty (+12 charged), blast 3 (charged 20, mob griefing decides block
  damage), explodes when it falls more than 5 blocks, killed by one iron golem hit, drops 2-5 gunpowder (charged 20-59
  more). It saves its fuse as "Fuse", which the vanilla creeper part reads too, so a reloaded one can go off at the
  vanilla creeper's 12 tick fuse with a normal blast (1.12 did the same). Spawns in the nether (8, 4-6).

Differences:
- Entities are always registered (1.12 skipped the heatscar spiders when disabled); disabled, they just never spawn.
- Spawn eggs are in the vanilla spawn egg tab; their names come from the entity names (1.12 had no egg names).

## Phase 7: world generation

The nine 1.12 IWorldGenerators are configured features of one feature (`natura:world`, config `kind`), placed once per
chunk with no placement modifiers and added to every biome (`neoforge:any`); each runs the 1.12 per-chunk rolls in the
1.12 order:
- `overworld_trees`: forest sakura (1 in rarity x 5, three tries) and eucalyptus; plains redwood (off by default, as in
  1.12) and eucalyptus (rarity x 1.5); mountains and hills hopseed and eucalyptus (1.12 `nextInt(rarity) < 10`, so
  nearly every hill chunk); rivers sakura and willow; jungles amaranth (rarity 1: every jungle chunk); forests maple,
  silverbell, tiger; swamps three willow tries; sandy biomes saguaro. Spawn heights as in 1.12 (sea level + 16 / + 48 /
  + range); the generators look for the ground themselves.
- `nether_trees`: bloodwood at y 72 (it hangs from the ceiling), darkwood and fusewood at 32-95, three ghostwood tries
  at 16-95.
- `overworld_berry_bushes` (by the biome's base temperature and downfall, as 1.12 temperature and rainfall: raspberry
  0.6-2.0 / 0.2-0.93, blueberry 0.3-0.81 / 0.3-0.8, blackberry 0.5-5 / 0.6-3 plus swamps at rarity / 3, maloberry
  0-0.3 / 0-5) and `nether_berry_bushes` (ultrawarm dimensions, 1.12 doesWaterVaporize): a single bush, a small node, a
  shrub or a large node, ripe 1 in 5.
- `crops`: barley (1 in 5), cotton (1 in 12) in temperature and downfall 0.11-1 / 0.11-2, grown, 2 x 64 tries; bluebells
  (1 in 12) 40 tries, anywhere.
- `clouds`: white clouds where downfall > 0.15 outside the End, ash and sulfur clouds in ultrawarm dimensions, dark
  clouds in the End; only with "Enable Clouds" (off by default, as 1.12 made no cloud generators without it).
- `glowshrooms`: large glowshrooms (1 in 35, five tries, purple 1 in 3) and baby glowshrooms in the nether.
- `thorn_vines`: 1 in 40 nether chunks, 20 vines hanging from y 108.
- `nether_minables`: 4 veins of 33 tainted soil in netherrack and 4 of heat sand in soul sand at y 27-36 (1.12
  `getSeaLevel() / 2 + 1 - 5` with the 63 sea level the nether had).

As in 1.12 (quirks kept):
- Biome types are the c: biome tags (`c:is_forest`, `c:is_plains`, `c:is_mountain`, `c:is_hill`, `c:is_river`,
  `c:is_jungle`, `c:is_swamp`, `c:is_sandy` without the beaches, `c:is_nether`, `c:is_end`), checked once per chunk
  at its middle.
- The overworld generators are not limited to the overworld (biome and climate decide, as in 1.12).
- The nether berry bushes searched the ground from the x coordinate as the height (1.12 bug), so mostly they only grow
  where the rolled spot is already on the ground.
- The saguaro generator checked for sand under the rolled spot (sea level to + 15), not under the ground it found, so
  saguaros only generate where a desert's sand is right at that height: rare, and none in high deserts.
- The ash clouds use the small size where the medium one belonged; the four dark cloud sizes are all 10 blocks.
- The sakura and ghostwood generators share their height limit across all the trees they grow (1.12 kept one
  generator for the game: the limit is rolled once and only lowered where a tree did not fit), so these trees are all
  about the same size in one game session.
- Heights: the overworld ones come from the "Sea level" setting (64) as in 1.12; the nether ones are counted from the
  world bottom (y 0 in both versions).

Differences:
- Positions are rolled inside the chunk instead of 1.12's chunk + 8 (1.21 features may only write one chunk around
  their own, and 1.12's offset caused the known cascading generation); the tree generators cannot write further than
  that, so the outer branches of a giant redwood next to a chunk border can be cut.
- No dimension blacklists (1.12 "Overworld/Nether World Gen Blacklist", by dimension id): biome modifiers or datapacks
  do that now. No retrogen.
- The crops' light check (light 8 or sky) is the sky above them (the heightmap): no light is known while generating.
- Blocks are placed without neighbour updates, like 1.12's flag 2.

## Phase 8: integrations and release

- JEI: the 1.12 plugin only hid the door blocks (separate from the door items then), the crops and the lit furnace.
  Here the doors are one block with its item, the crops have no item and the lit furnace is a block state, so there is
  nothing to hide and no plugin. The workbenches open the vanilla crafting menu, so JEI's recipe transfer and Crafting
  Tweaks (1.12 had an IMC message for it) work with them as with the crafting table.
- Jade (the 1.12 Waila plugin): the barley and cotton crops show the barley and cotton items (icon and name) and
  "Growth: x %" / "Growth: Mature" (setting "Crop item and growth"). Optional: the plugin class is only loaded by Jade.
- Logo (mods list): the nine overworld sapling textures (1.12 had no logo).
- Config: the 1.12 options are in `config/natura-common.toml` (sections ENABLE-DISABLE, Entities, Mob-Changes,
  Worldgen), with the 1.12 names and defaults; the dimension blacklists and the retrogen options are gone (phase 7).
- 47 game tests (blocks, loot, recipes, trees, plants, nether blocks, tools, mobs, world generation).

## Performance check (2026-10-17)

Measured with `tools/bench.sh` (dev only: `src/main/java/.../dev/DevBench.java`, inert unless `-Dnatura.bench`), a
fresh dev server world per case, NeoForge 21.1.256 on an Apple laptop (noisy: cases repeated and alternated).

- **World generation**: a 32x32 chunk square 20000 blocks from spawn, with the Natura generation on and with every
  "Generate ..." option off. Overworld 63–72 chunks/s on, 58–73 off; nether 81–90 on, 81–97 off: within the run-to-run
  noise. JFR: all Natura code is 0.5 % of the samples while generating, most of it the nether minables (0.4 %).
  Nothing to change.
- **Grown crops kept random-ticking**: `isRandomlyTicking` was always true, while a grown crop does nothing (vanilla
  crops stop ticking at max age). Now only growing barley and cotton tick. 16384 grown plants, random tick speed 300,
  average server tick: before 1.245 ms (wheat 0.868); after, three alternated pairs 0.385 / 0.990 / 0.778 ms vs wheat
  0.608 / 1.133 / 0.954 — on par with vanilla. Game test `grown_crops_do_not_tick`.
- **Thorn vines** made a new `Random` (with the fixed 1.12 seed) for every entity inside them every tick; the roll is
  always the same, so it is computed once (same behaviour).
- **Tempting** cows, sheep and chickens built a new `Ingredient` for every animal that joined the level (every chunk
  load); it is made once now.
- Checked and left as they are: the client tints (computed when a chunk section is built, not per frame), the redwood
  leaves decay scan (only after a tree is broken, stops at the first log), the berry bush and glowshroom random ticks
  (same work as vanilla bushes and mushrooms), the tree generators (they read and write only the chunks the
  decoration may touch and set the leaves distance once per tree).
- 48 game tests pass.

## NeoForge + Fabric (2026-10-19)

The project is a MultiLoader build now (after the Iron Backpacks one): `common` (all game code and resources, compiled
against vanilla through NeoForm), `neoforge` and `fabric` (each compiles the common sources and resources into its own
jar: `natura-neoforge-*.jar`, `natura-fabric-*.jar`). What the common code needs from the loader is the `Platform`
interface (`common/.../platform`), found with `ServiceLoader`.

- Registration: `Registration.block / item / register` return `BlockEntry` / `ItemEntry` / `Entry` (a holder). NeoForge:
  deferred registers on the mod bus; Fabric: registered right away. Same order as before.
- Config: the options are described once (`platform/ConfigSpec`, the ModConfigSpec calls `NaturaConfig` had). NeoForge
  builds its ModConfigSpec from it; Fabric reads and writes the same `config/natura-common.toml` itself (same sections
  and keys, so the file works on both). Fabric reads it once at start: a change needs a restart there.
- Conditions: `natura:config_option_enabled` is a NeoForge condition and a Fabric resource condition; the wheat flour
  recipe has both `neoforge:conditions` and `fabric:load_conditions`.
- Biome modifiers: NeoForge reads `data/natura/neoforge/biome_modifier` as before; Fabric reads the same files from the
  jar and applies them with `BiomeModifications` (features in every biome, nether spawns, the heatscar spider only with
  the config option, checked when a world loads).
- Biome types: the `Tags.Biomes` checks use the `c:` biome tags by id (`NaturaBiomeTags`); Fabric API has all of them.
- Leaves, thorn vines: the "sheared" drop used NeoForge's `can_item_perform_ability shears_dig`, which Fabric cannot
  read; it is `match_tool` with the `c:tools/shear` tag now (shears and the kamas are in it on both loaders).
  **Fabric only:** vanilla leaves, grass and vines check for `minecraft:shears` itself, so a kama does not shear those
  there (NeoForge: the kama has the shears abilities, as before).
- Kama on mobs: NeoForge shears every `IShearable` mob (as before); Fabric the vanilla `Shearable` ones (sheep,
  mooshroom, snow golem, bogged).
- Tainted soil: NeoForge `canSustainPlant` in a NeoForge subclass (as before); Fabric mixins into nether wart, fungi and
  roots. It also is in `minecraft:infiniburn_overworld` now (Fabric has no `isFireSource`; same effect).
- Respawn obelisk: NeoForge `getRespawnPosition` (as before); Fabric a mixin into `ServerPlayer.findRespawnAndUseSpawnBlock`
  that asks the obelisk the same way NeoForge does (a non-forced spawn block that is no bed or anchor).
- Blaze rails: NeoForge rail API (as before; the activator flag is `isActivatorRail()` now instead of the NeoForge
  constructor). Fabric: a minecart mixin gives the 0.65 top speed, treats the golden blaze rail as a powered rail and the
  blaze activator rail as an activator rail, and runs the golden rail's own push at the end of the track move.
  **Fabric only:** NeoForge runs that push just before the powered rail boost, Fabric just after it (a difference of
  under 0.005 blocks per tick).
- Blaze hopper: NeoForge `BlockEntityTypeAddBlocksEvent`; Fabric adds it to the hopper block entity type at start.
- Crop growth hooks (`CommonHooks.canCropGrow` / `fireCropGrowPost`), bone meal with its event, fake players, the
  biome downfall after modifiers: NeoForge as before; Fabric the vanilla behaviour.
- Block render layers: NeoForge reads `render_type` from the models; Fabric does not, so the Fabric client reads the same
  `render_type` from the Natura block models at start and puts each block in that layer.
- What the NeoForge access transformer opened (block constructors, `Mob.goalSelector`, `ItemProperties.register`, ...)
  is in `common/accesstransformer.cfg` (compile only) and `fabric/.../natura.accesswidener`.
- Heatscar spider fall sound: `getSoundType()` without the position (the NeoForge variant asked the block at the
  spider's position; the same for every Natura and vanilla block).
- Game tests: the same 49 on both loaders (new: `blaze_rail_carts`, a cart on blaze rails runs faster than 0.4 and a
  powered blaze activator rail turns a hopper cart off; it checks the Fabric minecart mixin) (`:neoforge:runGameTestServer`, `:fabric:runGametest`, `:fabric:prodGametest`
  on the release jar). The two biome modifier tests check the biomes (features, nether spawns) instead of the NeoForge
  biome modifier registry.

## Performance on both loaders (2026-10-20)

`tools/bench.sh` (`LOADER=neoforge|fabric`, every run stopped after 3 minutes): a dev server, a fixed seed, the
crops and carts cases on a flat world (on a normal world the terrain's own vegetation dropped thousands of items under
the sped-up random ticks and drowned the difference). One run per case on an Apple laptop, so differences of ~10 %
are noise. Server tick in ms, generation in chunks per second (24x24 chunks).

| | 1.21.1 NeoForge | 1.21.1 Fabric | 26.3 NeoForge | 26.3 Fabric |
|---|---|---|---|---|
| overworld generation, Natura on / off | 57.5 / 58.1 | 54.2 / 50.4 | 113.0 / 114.6 | 116.3 / 114.2 |
| nether generation, Natura on / off | 90.2 / 97.1 | 75.0 / 96.8 | 254.8 / 290.6 | 263.2 / 287.4 |
| 16384 grown barley / wheat, random tick speed 300 | 0.35 / 0.30 | 0.66 / 0.47 | 4.77 / 5.16 | 5.10 / 4.71 |
| 1024 minecarts on blaze / vanilla rails | 2.06 / 2.13 | 1.72 / 2.88 | 5.52 / 6.05 | 7.70 / 8.72 |

- The overworld generation, the crops and the blaze rails (the rail mixins) cost the same as vanilla.
- The nether is 5-20 % slower to generate with Natura: it really places more there (nether trees, bushes, glowshrooms,
  thorn vines, soil veins); 0.2-0.3 s more for 576 chunks on 26.3.
- 26.3 random ticks cost ten times more on a flat world for both crops (vanilla wheat too): not Natura.
- The client draws nothing per frame (tints are computed when a chunk section is built); not measured.

## Forge 1.20.1 + Fabric (branch mc/1.20.1, 2026-10-26)

A backport of the 1.21.1 version: Forge 47.4.28 (ModDevGradle legacyforge; `common` compiles on Forge userdev, the
access transformer in SRG names) and Fabric 0.16.14 / Fabric API 0.92.12, Java 17. The mechanical part of the Java
changes was done by modding-kb `scripts/backport_1_20_1.py`, the resources by `tools/resources_1_20_1.py` after the
1.21.1 generator (`tools/gen_resources_1_20_1.sh`). Same behaviour as 1.21.1; what is done differently:

- Blocks: `use()` (1.21 `useItemOn` / `useWithoutItem`; the without-item part only for the main hand, as in 1.21).
- Food: the 1.12 eating times are the items' `getUseDuration` (1.20.1 food has no eating time of its own).
- Tools and imp armor: `Tier.getLevel()` (bloodwood mines like iron, 2), the imp `ArmorMaterial` is a plain object named
  `natura:imp`; on Fabric a client mixin (`ArmorTextureMixin`) finds its layer textures, which Forge does itself.
- Forge hooks: the kama has the shears tool actions and shears every `IForgeShearable` mob; tainted soil sustains the
  nether plants through `canSustainPlant(..., IPlantable)`; the obelisk respawn through `IForgeBlock.getRespawnPosition`
  (Fabric: a mixin into `Player.findRespawnPositionAndUseSpawnBlock`); the blaze hopper is added to the hopper block
  entity type at common setup on both loaders (Forge 1.20.1 has no event for it).
- Conditions: the wheat flour recipe has Forge `conditions` and `fabric:load_conditions`, the heatscar spider spawns
  `forge:conditions`; Fabric reads the Forge biome modifiers from the jar (`data/natura/forge/biome_modifier`).
- Biome types: the generation checks the 1.21 conventional biome tags (`c:is_forest`, ...), which no 1.20.1 loader has;
  the jar ships them (vanilla entries as in NeoForge 21.1, plus `forge:is_*` and the Fabric API 0.92 `c:forest`, ...).
- The `c:tools/shear` tag of 1.21 is in the jar (shears and the kamas).
- Game tests: the same 49, `:forge:runGameTestServer` (a flat world), `:fabric:runGametest`, `:fabric:prodGametest` on the
  release jar; the Forge release jar starts on a real Forge server without errors (`sh tools/prodserver.sh`).

Performance (`tools/bench.sh`, the same cases as 1.21.1, one run each, Apple laptop):

| | 1.20.1 Forge | 1.20.1 Fabric |
|---|---|---|
| overworld generation, Natura on / off (chunks/s) | 65.4 / 61.3 | 68.9 / 64.9 |
| nether generation, Natura on / off (chunks/s) | 82.4 / 84.7 | 91.9 / 92.0 |
| 16384 grown barley / wheat, random tick speed 300 (ms per tick) | 0.52 / 0.46 | 0.45 / 0.43 |
| 1024 minecarts on blaze / vanilla rails (ms per tick) | 2.62 / 3.00 | 2.06, 2.46, 2.54 / 2.07, 2.34, 2.48 |

All on par with vanilla (differences inside the run-to-run noise).
