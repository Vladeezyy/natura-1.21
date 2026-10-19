#!/bin/sh
# Dev measurements (src/main/java/.../dev/DevBench.java) on a fresh dev server world, each case in its own world:
#   gen with Natura world generation, gen without it (all Natura features off), crops (Natura barley vs vanilla wheat).
# GEN_ONLY=1 / CROPS_ONLY=1 run one half. Prints the "[bench]" lines. JFR=1 also records a flight recording of each case to neoforge/run/bench-<case>.jfr (NeoForge dev server).
cd "$(dirname "$0")/.."
SIZE=${SIZE:-24}
CFG=neoforge/run/config/natura-common.toml
bench() { # case name, -D properties, natura world generation on/off
	pkill -f 'neoforge.enabledGameTestNamespaces=natura' 2>/dev/null
	rm -rf neoforge/run/world build-bench.log
	mkdir -p neoforge/run/config && echo "eula=true" > neoforge/run/eula.txt
	[ -f "$CFG.bak" ] && cp "$CFG.bak" "$CFG"
	if [ "$3" = off ] && [ -f "$CFG" ]; then cp "$CFG" "$CFG.bak"; sed -i '' -E 's/^([[:space:]]*"?Generate [^=]*"?[[:space:]]*=[[:space:]]*)true/\1false/' "$CFG"; fi
	jfr=""; [ -n "$JFR" ] && jfr="-XX:StartFlightRecording=filename=$PWD/neoforge/run/bench-$1-%p.jfr,settings=profile,dumponexit=true"
	JDK_JAVA_OPTIONS="$2 -Dnatura.bench.size=$SIZE $jfr" perl -e 'alarm 900; exec @ARGV' ./gradlew :neoforge:runServer --console=plain > build-bench.log 2>&1
	[ -f "$CFG.bak" ] && mv "$CFG.bak" "$CFG"
	echo "== $1"; grep -E '\[bench\]|Crash|Exception' build-bench.log | sed -E 's/^.*\[bench\]/[bench]/' | head -5
}
if [ -z "$CROPS_ONLY" ]; then
	bench gen-natura "-Dnatura.bench=gen" on
	bench gen-off "-Dnatura.bench=gen" off
fi
[ -n "$GEN_ONLY" ] && exit 0
bench crops-natura "-Dnatura.bench=crops -Dnatura.bench.crop=natura" on
bench crops-wheat "-Dnatura.bench=crops -Dnatura.bench.crop=wheat" on
