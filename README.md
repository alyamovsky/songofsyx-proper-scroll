# Proper Scroll, a Songs of Syx mod

Side panels (rooms, subjects and so on) scroll with the mouse wheel when their content is taller
than the screen. Born from a clash between the UI mods **Extra Info** and **Industry Insights**:
both append blocks to the right column of the room panel, and the bottom part ended up off screen.

## Quick start

A ready-made build is committed in `dist/ProperScroll`. Installing needs no JDK, just download the
repository (Code → Download ZIP, or `git clone`).

1. Install. macOS, Linux or Git Bash on Windows:
   ```sh
   ./install.sh
   ```
   PowerShell on Windows:
   ```powershell
   .\install.ps1
   ```
   If PowerShell refuses to run scripts: `powershell -ExecutionPolicy Bypass -File .\install.ps1`.
   The script copies `dist/ProperScroll` into the game's mods folder:
   `~/Library/Application Support/songsofsyx/mods` on macOS, `%APPDATA%\songsofsyx\mods` on Windows,
   `~/.local/share/songsofsyx/mods` on Linux. Override with `SOS_MODS_DIR`. Copying the
   `ProperScroll` folder there by hand works just as well.
2. Start the game. In the launcher's mods tab enable **Proper Scroll** next to your other mods and
   start as usual. Saves need no changes, the mod works in any game.
3. Check: open a carpenter (or any room where Extra Info and Industry Insights are active), hover the
   right column and turn the wheel. The content should reach the bottom blocks, and a thin position
   indicator appears at the right edge. Panels whose content fits look exactly as before.

Rebuilding (`./build.sh`, see "Building") is only needed after code changes or a game update; the
script refreshes `dist/` too. To remove the mod, disable it in the launcher or delete the
`ProperScroll` folder from the mods directory.

## How it works

The game puts script mod jars on the classpath **before** `SongsOfSyx.jar`, so a class with the
same name in a mod replaces the vanilla one. This mod replaces a single class,
`view.interrupter.ISidePanels` (the container of side panels). The code is vanilla (0.71.44)
except for the inner `Panel` class:

- when a panel's section is more than 40 px taller than the visible area, the mouse wheel moves
  the content. Smaller overhang is ignored: some vanilla panels (the room list, for one) are laid
  out to the screen height and then get a row stacked on top, so in vanilla they hang a couple of
  dozen pixels past the bottom edge and need no scrolling;
- a thin position indicator (3 px, in the gap between the content and the frame) is drawn at the
  right edge;
- the game's renderer cannot clip, so elements that would spill out of the panel are parked off
  screen for the duration of rendering and hover handling, and the title bar and the bottom margin
  are redrawn on top of the content;
- reopening a panel resets the scroll position.

When the content fits, the panel behaves exactly like vanilla.

The pure geometry (`properscroll.ScrollMath`) lives in a class with no game dependencies and is
covered by tests (`src/test`), which run as part of the build.

## Building

Needs a JDK 21+ and an installed copy of the game (for the classpath). On Windows run the script
from Git Bash. The game jar is looked up in the standard Steam locations for the current OS (on
Windows `C:\Program Files (x86)\Steam\...`); point `SOS_JAR` at it otherwise. The built mod is OS
independent: it is Java bytecode, a build from one machine runs on any other with the same game
version.

```sh
./build.sh
```

The script compiles, runs the tests, assembles the mod in `build/mod/ProperScroll`
(`_Info.txt` + `V71/script/ProperScroll.jar`), packs a release zip into `build/` and refreshes the
committed copy in `dist/ProperScroll`. The `V<major>` folder name is taken from the game jar.

## Installing

```sh
./install.sh        # macOS, Linux, Git Bash on Windows
.\install.ps1       # PowerShell on Windows
```

Copies `dist/ProperScroll` into the game's mods folder for the current OS (see "Quick start");
override the folder with `SOS_MODS_DIR`. Then enable **Proper Scroll** in the game launcher.

## Publishing

To the game, a mod is the `ProperScroll/` folder with `_Info.txt` and `V71/script/ProperScroll.jar`,
so it is distributed as a whole. A ready copy is committed in `dist/`, so the mod can be installed
straight from a downloaded repository. `build.sh` also packs `build/ProperScroll-<version>.zip`
(version from `mod/_Info.txt`), handy as a GitHub release asset:

```sh
gh release create v1.0.1 build/ProperScroll-1.0.1.zip
```

Users unpack the archive into the game's mods folder and enable the mod in the launcher.
CI builds are not an option: compiling needs `SongsOfSyx.jar`, which is proprietary and not
committed, so building and uploading the archive happen locally.

## Compatibility

- Built against and checked with game version 0.71.44. After a game update, compare `ISidePanels`
  with the new vanilla class and rebuild.
- Conflicts only with mods that replace the same class, `view.interrupter.ISidePanels`. The popular
  UI mods replace other classes (Extra Info: `UIRoomModule` and the room modules, Industry Insights:
  `Modules`, PrPleGooQoL: `IPromtScreen` and `ITextInput`), so they coexist with this one.

## Known limitations

- An element taller than 48 px that crosses the top edge of the scroll area is hidden as a whole
  once its top rises above the title bar (otherwise it would draw over the game's top panel). In
  practice this only affects the room panel header: it disappears when less than ~40 px of it
  remain visible.
- The indicator cannot be dragged, scrolling is wheel only.
