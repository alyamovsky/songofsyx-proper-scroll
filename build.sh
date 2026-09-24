#!/usr/bin/env bash
# Builds the mod into build/mod/ProperScroll. Needs a JDK 21+ and the game jar.
# On Windows run it from Git Bash (or copy build/mod from another machine).
set -euo pipefail
cd "$(dirname "$0")"

CP_SEP=":"
case "$(uname -s)" in
    Darwin)
        CANDIDATES=("$HOME/Library/Application Support/Steam/steamapps/common/Songs of Syx/SongsOfSyxMac.app/Contents/Resources/SongsOfSyx.jar") ;;
    MINGW*|MSYS*|CYGWIN*)
        CP_SEP=";"
        CANDIDATES=("$(cygpath -u 'C:\Program Files (x86)\Steam\steamapps\common\Songs of Syx\SongsOfSyx.jar')") ;;
    *)
        CANDIDATES=("$HOME/.steam/steam/steamapps/common/Songs of Syx/SongsOfSyx.jar"
                    "$HOME/.local/share/Steam/steamapps/common/Songs of Syx/SongsOfSyx.jar") ;;
esac

GAME_JAR="${SOS_JAR:-}"
if [ -z "$GAME_JAR" ]; then
    for c in "${CANDIDATES[@]}"; do
        if [ -f "$c" ]; then
            GAME_JAR="$c"
            break
        fi
    done
fi
if [ -z "$GAME_JAR" ] || [ ! -f "$GAME_JAR" ]; then
    echo "Game jar not found. Looked at:" >&2
    printf '  %s\n' "${CANDIDATES[@]}" >&2
    echo "Set SOS_JAR to the path of SongsOfSyx.jar" >&2
    exit 1
fi
echo "Game jar: $GAME_JAR"

MAJOR=$(javap -cp "$GAME_JAR" -constants game.VERSION | sed -n 's/.*VERSION_MAJOR = \([0-9]*\);/\1/p')
MINOR=$(javap -cp "$GAME_JAR" -constants game.VERSION | sed -n 's/.*VERSION_MINOR = \([0-9]*\);/\1/p')
echo "Game version: $MAJOR.$MINOR"

OUT=build
MOD="$OUT/mod/ProperScroll"
rm -rf "$OUT"
mkdir -p "$OUT/classes" "$OUT/test-classes" "$MOD/V$MAJOR/script"

# --release 21 matches the JRE bundled with the game
javac --release 21 -encoding UTF-8 -Xlint:all -Werror -cp "$GAME_JAR" -d "$OUT/classes" $(find src/main/java -name '*.java')
javac --release 21 -encoding UTF-8 -cp "$OUT/classes" -d "$OUT/test-classes" $(find src/test/java -name '*.java')
java -cp "$OUT/classes$CP_SEP$OUT/test-classes" properscroll.ScrollMathTest

cp mod/_Info.txt "$MOD/_Info.txt"
jar cf "$MOD/V$MAJOR/script/ProperScroll.jar" -C "$OUT/classes" .
echo "Built $MOD"

# Release zip: unpack into the game's mods folder. jar without a manifest is a plain zip, works everywhere the JDK does.
MOD_VERSION=$(sed -n 's/^VERSION: "\(.*\)",/\1/p' mod/_Info.txt)
ZIP="$OUT/ProperScroll-$MOD_VERSION.zip"
jar cfM "$ZIP" -C "$OUT/mod" .
echo "Packed $ZIP"
