#!/bin/bash
# The 1.20.1 resources: the 1.21.1 generator (tools/gen_resources.py, from the 1.12 assets), then the formats of 1.20.1
# (tools/resources_1_20_1.py, the same script as modding-kb scripts/resources_1_20_1.py). Needs one
# ./gradlew :common:compileJava first (the vanilla 1.20.1 assets in common/build/moddev/artifacts).
set -e
cd "$(dirname "$0")/.."
NEO=$(ls ~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/21.1.*/*/neoforge-21.1.*-universal.jar 2>/dev/null | tail -1)
rm -rf common/src/main/resources/assets common/src/main/resources/data
python3 -I tools/gen_resources.py _original/src/main/resources/assets/natura common/src/main/resources
# the biome tags NaturaBiomeTags checks (1.21 c: names, which neither 1.20.1 loader has)
python3 -I tools/resources_1_20_1.py common/src/main/resources --mod natura ${NEO:+--neoforge-jar "$NEO"} \
    --code-tags "worldgen/biome:is_beach,is_end,is_forest,is_hill,is_jungle,is_mountain,is_nether,is_plains,is_river,is_sandy,is_swamp"
