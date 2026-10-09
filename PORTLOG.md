# Natura 1.21.1 port log

Port of Natura 4.3.2 (Minecraft 1.12.2, progwml6 / Slime Knights, code CC0, textures CC-BY 3.0) to Minecraft 1.21.1 /
NeoForge 21.1. The 1.12 source is the specification: `git clone -b 1.12 https://github.com/progwml6/Natura.git _original`
(used at d5b4bdd). Behaviour is kept as in 1.12, quirks included; every difference is listed here.

## Phases

1. Woods: logs, leaves, saplings (no trees yet), planks, slabs, stairs, fences, fence gates, doors, trapdoors,
   buttons, pressure plates, bookshelves, workbenches, sticks. ✅
2. Trees: the 1.12 tree generators for sapling growth.
3. Overworld plants and food: crops (barley, cotton), berry bushes, saguaro, bluebells, colored grass (+ slab, stairs),
   materials, food, soups, seed bags, bone meal bag.
4. Nether blocks: heat sand, tainted soil, nether glass, thorn vines, glowshrooms, respawn obelisk, netherrack furnace,
   blaze hopper, blaze rails, nether lever / button / pressure plate.
5. Tools: nether wood and quartz tools, kamas, bows, flint and blaze, imp armor.
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
- Saplings exist but do not grow until phase 2.
- Fences and fence gates connect to all wooden fences (1.12: any wood material fence).
