#!/usr/bin/env bash
# Copies the built mod into the game's local mods folder. Run build.sh first.
# On Windows without Git Bash use install.ps1 instead.
set -euo pipefail
cd "$(dirname "$0")"

case "$(uname -s)" in
    Darwin) DEFAULT_MODS="$HOME/Library/Application Support/songsofsyx/mods" ;;
    MINGW*|MSYS*|CYGWIN*) DEFAULT_MODS="$(cygpath -u "$APPDATA")/songsofsyx/mods" ;;
    *) DEFAULT_MODS="$HOME/.local/share/songsofsyx/mods" ;;
esac
MODS_DIR="${SOS_MODS_DIR:-$DEFAULT_MODS}"

SRC=build/mod/ProperScroll
if [ ! -d "$SRC" ]; then
    echo "Nothing built yet, run ./build.sh first" >&2
    exit 1
fi
mkdir -p "$MODS_DIR"
rm -rf "$MODS_DIR/ProperScroll"
cp -R "$SRC" "$MODS_DIR/ProperScroll"
echo "Installed to $MODS_DIR/ProperScroll. Enable 'Proper Scroll' in the game launcher."
