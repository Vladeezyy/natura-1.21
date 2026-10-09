# Natura for Minecraft 1.21.1 (unofficial port)

An unofficial port of [Natura](https://github.com/progwml6/Natura) 4.3.2 (Minecraft 1.12.2) by mDiyo, progwml6 and
the Slime Knights to Minecraft 1.21.1 / NeoForge 21.1.

> Heyo, Redwood trees! Chop them all day long!

Natura adds trees (maple, silverbell, amaranth, tiger, willow, eucalyptus, hopseed, sakura, the giant redwood and the
nether ghostwood, bloodwood, fusewood and darkwood), crops, berry bushes, nether blocks, tools and mobs. The port
follows the 1.12 behaviour; every difference is listed in [PORTLOG.md](PORTLOG.md).

**Status:** work in progress, see the phases in [PORTLOG.md](PORTLOG.md).

## Building

JDK 21, then:

```
./gradlew build
```

The jar is in `build/libs`. Resources are generated from the 1.12 resources by `tools/gen_resources.py` (see the
script header).

## License

Like the original: the code is public domain under [CC0](http://creativecommons.org/publicdomain/zero/1.0/), the
textures and binaries are under [CC-BY 3.0](http://creativecommons.org/licenses/by/3.0/) (original authors: mDiyo,
progwml6 and the Slime Knights). See [LICENSE.md](LICENSE.md).
