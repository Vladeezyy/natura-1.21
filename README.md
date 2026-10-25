# Natura for Minecraft 1.20.1 (unofficial port)

An unofficial port of [Natura](https://github.com/progwml6/Natura) 4.3.2 (Minecraft 1.12.2) by mDiyo, progwml6 and
the Slime Knights to Minecraft 1.20.1 for **Forge** 47 and **Fabric** (Fabric API required). Branch `mc/1.20.1`, a
backport of the 1.21.1 version (branch `main`).

> Heyo, Redwood trees! Chop them all day long!

The port follows the 1.12 behaviour, quirks included; every difference is listed in [PORTLOG.md](PORTLOG.md), with
the 1.12 → 1.21 block and item names.

## What is in it

- **Trees**: maple, silverbell, amaranth, tiger, willow, eucalyptus, hopseed, sakura and the giant redwood in the
  overworld; ghostwood, bloodwood (it hangs from the ceiling), fusewood and darkwood in the nether. Each wood has its
  logs, leaves, sapling, planks, slab, stairs, fence, fence gate, door, trapdoor, button, pressure plate, bookshelf,
  workbench and stick. A redwood sapling needs 40 more in the 7 x 7 square around it to grow.
- **Plants and food**: barley and cotton crops, raspberry, blueberry, blackberry and maloberry bushes, the nether
  blightberry, duskberry, skyberry and stingberry bushes, saguaro cactus and its fruit, bluebells, colored grass, flour,
  stews in nether wood bowls, berry medley, seed bags and the bone meal bag.
- **Nether blocks**: heat sand, tainted soil and farmland, soul and heat glass, thorn vines, glowshrooms (small and
  large), the respawn obelisk, the netherrack furnace, the blaze hopper, blaze rails, nether lever, button and pressure
  plate; clouds (off by default, as in 1.12).
- **Tools**: ghostwood, bloodwood, darkwood, fusewood and nether quartz tools and kamas, nether wood bows, flint and
  blaze, imp armor.
- **Mobs**: the imp, the nitro creeper and the heatscar spider (off by default, as in 1.12) in the nether.
- **World generation**: all of the above trees, bushes, crops, glowshrooms, thorn vines and soils generate where 1.12
  put them (by biome type and climate).

Optional: [JEI](https://modrinth.com/mod/jei) works with the workbenches; with [Jade](https://modrinth.com/mod/jade)
the barley and cotton crops show their crop item.

## Configuration

`config/natura-common.toml` has the 1.12 options with their names and defaults: what generates and how often, the
heatscar spiders, the baby spider count, the obelisk respawn in the nether, the seeds from grass, the wheat flour
recipe and the sea level the trees grow from.

## Building

JDK 17 (Gradle itself runs on JDK 21), then:

```
./gradlew build
```

The jars are in `forge/build/libs` and `fabric/build/libs`. The game code and resources are in `common`; the
`forge` and `fabric` projects hold what differs per loader. Resources are generated from the 1.12 resources by
`tools/gen_resources_1_20_1.sh` (the 1.21.1 generator `tools/gen_resources.py`, then the 1.20.1 formats). Game tests:
`./gradlew :forge:runGameTestServer`, `./gradlew :fabric:runGametest` and `./gradlew :fabric:prodGametest` (the
release jar); the Forge release jar on a real server: `sh tools/prodserver.sh`.

## License

The code of this port: **LGPL-3.0-only** ([LICENSE](LICENSE), GPL-3.0 text in [COPYING](licenses/COPYING)), Copyright (c) 2026 Vladeezyy (the original code was public domain under [CC0](http://creativecommons.org/publicdomain/zero/1.0/)).
The textures and binaries stay under [CC-BY 3.0](http://creativecommons.org/licenses/by/3.0/) (original authors: mDiyo, progwml6 and the Slime Knights). The original license is kept in [LICENSE-ORIGINAL.md](licenses/LICENSE-ORIGINAL.md).
Forks and modified versions must stay open under the same license and credit the authors. Please download the mod only from the official release pages.
