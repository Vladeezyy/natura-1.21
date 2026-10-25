#!/bin/sh
# The Forge release jar on a real server (outside dev):  sh tools/prodserver.sh
# forge/run-prod-server is installed with the official Forge installer; a new normal world each run, waits for
# "Done", stops the server with "stop", prints the errors. (Fabric: ./gradlew :fabric:prodGametest.)
cd "$(dirname "$0")/.." || exit 1
JAVA17=${JAVA17:-$(/usr/libexec/java_home -v 17)}
FORGE=$(grep '^forge_version=' gradle.properties | cut -d= -f2)
. tools/watchdog.sh
DIR=forge/run-prod-server
JAR=$(ls forge/build/libs/natura-forge-*.jar | grep -v -- '-sources\|-dev' | head -1)
[ -n "$JAR" ] || { echo "no release jar: ./gradlew :forge:build"; exit 1; }
mkdir -p "$DIR/mods"
if [ ! -f "$DIR/run.sh" ]; then
    curl -fsSL -o "$DIR/installer.jar" "https://maven.minecraftforge.net/net/minecraftforge/forge/$FORGE/forge-$FORGE-installer.jar"
    (cd "$DIR" && "$JAVA17/bin/java" -jar installer.jar --installServer > installer.log 2>&1)
fi
rm -f "$DIR"/mods/natura-*.jar; cp "$JAR" "$DIR/mods/"
rm -rf "$DIR/world"; echo eula=true > "$DIR/eula.txt"
printf 'online-mode=false\nserver-port=25603\nspawn-protection=0\nlevel-seed=1234\n' > "$DIR/server.properties"
LOG=build-prodserver-forge.log
rm -f "$DIR/console.in"; mkfifo "$DIR/console.in"
( cd "$DIR" && sleep 100000 > console.in & cd "$DIR" && PATH="$JAVA17/bin:$PATH" sh run.sh nogui < console.in ) > "$LOG" 2>&1 &
wait_started "$LOG"; code=$?
echo "jar: $JAR"
grep -E "Done \(" "$LOG" | tail -1
sleep 10
echo stop > "$DIR/console.in"
sleep 15
pkill -f "sleep 100000" 2>/dev/null
rm -f "$DIR/console.in"
echo "--- errors"
grep -E '/(ERROR|FATAL)\]|Exception|Caused by' "$LOG" | grep -v 'Failed to load properties' | cut -c1-220 | sort | uniq -c | sort -rn | head -20
exit $code
