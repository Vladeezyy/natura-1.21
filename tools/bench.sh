#!/bin/sh
# Dev measurements (common/.../dev/DevBench.java) on a fresh dev server world, each case in its own world:
#   gen with Natura world generation and without it (all Natura features off), crops (Natura barley vs vanilla wheat),
#   carts (1024 minecarts on blaze rails vs vanilla rails: the cost of the rail code).
# LOADER=neoforge|fabric (default neoforge). ONLY=gen|crops|carts runs one group. Each run is stopped after LIMIT
# seconds (default 180). Prints the "[bench]" lines. JFR=1 also records a flight recording of each case.
cd "$(dirname "$0")/.."
LOADER=${LOADER:-neoforge}
SIZE=${SIZE:-24}
LIMIT=${LIMIT:-180}
RUN=$LOADER/run
CFG=$RUN/config/natura-common.toml
stop() { # a dev server of this checkout still running (the time limit stops Gradle, not the server it started)
	for pid in $(pgrep java); do
		lsof -a -p "$pid" -d cwd -Fn 2>/dev/null | grep -qx "n$PWD/$RUN" && kill "$pid"
	done
	sleep 2
}
prop() { # server.properties key value
	if grep -q "^$1=" $RUN/server.properties; then
		sed -i '' "s|^$1=.*|$1=$2|" $RUN/server.properties
	else
		echo "$1=$2" >> $RUN/server.properties
	fi
}
bench() { # case name, -D properties, natura world generation on/off
	stop
	rm -rf $RUN/world build-bench.log
	mkdir -p $RUN/config && echo "eula=true" > $RUN/eula.txt
	# 1.21.2+: an empty server stops ticking after 60 s; the crops and carts cases tick longer than that
	touch $RUN/server.properties
	prop pause-when-empty-seconds 0
	# the same world in every case: a fixed seed; crops and carts on a flat world (no terrain vegetation dropping items)
	prop level-seed 20261020
	case "$1" in gen*) prop level-type minecraft:normal ;; *) prop level-type minecraft:flat ;; esac
	[ -f "$CFG.bak" ] && cp "$CFG.bak" "$CFG"
	if [ "$3" = off ] && [ -f "$CFG" ]; then cp "$CFG" "$CFG.bak"; sed -i '' -E 's/^([[:space:]]*"?Generate [^=]*"?[[:space:]]*=[[:space:]]*)true/\1false/' "$CFG"; fi
	jfr=""; [ -n "$JFR" ] && jfr="-XX:StartFlightRecording=filename=$PWD/$RUN/bench-$1-%p.jfr,settings=profile,dumponexit=true"
	start=$(date +%s)
	JDK_JAVA_OPTIONS="$2 -Dnatura.bench.size=$SIZE $jfr" perl -e "alarm $LIMIT; exec @ARGV" ./gradlew :$LOADER:runServer --console=plain > build-bench.log 2>&1
	end=$(date +%s)
	stop
	[ -f "$CFG.bak" ] && mv "$CFG.bak" "$CFG"
	echo "== $LOADER $1 ($((end - start)) s)"; grep -E '\[bench\]|Crash|Exception' build-bench.log | grep -vE 'authlib|minecraftservices|NoSuchFileException: server.properties' | sed -E 's/^.*\[bench\]/[bench]/' | head -5
}
if [ -z "$ONLY" ] || [ "$ONLY" = gen ]; then
	bench gen-natura "-Dnatura.bench=gen" on
	bench gen-off "-Dnatura.bench=gen" off
fi
if [ -z "$ONLY" ] || [ "$ONLY" = crops ]; then
	bench crops-natura "-Dnatura.bench=crops -Dnatura.bench.crop=natura" on
	bench crops-wheat "-Dnatura.bench=crops -Dnatura.bench.crop=wheat" on
fi
if [ -z "$ONLY" ] || [ "$ONLY" = carts ]; then
	bench carts-blaze "-Dnatura.bench=carts -Dnatura.bench.rail=blaze" on
	bench carts-vanilla "-Dnatura.bench=carts -Dnatura.bench.rail=vanilla" on
fi
