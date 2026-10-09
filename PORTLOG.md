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
6. Entities: imp, heatscar spider (+ baby), nitro creeper.
7. World generation: trees, berry bushes, crops, clouds, glowshrooms, saguaro, vines, nether trees; clouds.
8. JEI, config, game tests, release.

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
- Leaves get the vanilla 1.21 distance when the tree is done (otherwise they would decay at once).
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
