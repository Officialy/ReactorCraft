# Legacy-file cleanup — what was deleted and what to know before you miss it

## Status of the recorded deferrals
- **OFF-51 fluid interactions** — DONE `3563490` (full interaction map in `BlockReactorMachine`).
- **Canister per-fluid names (ReactorNames)** — DONE `3563490` (component-driven name + fluid lang).
- **ReactorConfig → ModConfigSpec** — DONE `3563490` (`ReactorOptions.SPEC`, COMMON config,
  incl. `heavywaterdimensions` honoured by the heavy pump).
- **Machine recipes (~32)** — still gated on unported RotaryCraft items (basepanel, pipe, …);
  that is a RotaryCraft content port, tracked in `datagen.md`.
- **Handbook** (ReactorBook/Descriptions/GuiReactorBook*) — not started; large standalone feature.
- **Tritium/fluorite lamp** — not started; needs the block+renderer port (queue behind the BER batches).
- **Per-machine light values** — not checked yet (see BlockReactorTile note below).

All deleted files are pristine 1.7.10 source, never on the build allowlist, and remain fully
recoverable: `git show origin/master:<OriginalPath>.java` (Reika's original) or `git log -- <path>`
for the relocated copy. This file records the still-useful knowledge that lived in them.

## blocks/BlockReactorTile.java (584 lines) — the OFF-51 fluid-interaction map
Superseded by `BlockReactorMachine` (+ per-machine blocks). Its `onBlockActivated` carried the
bucket/canister interactions the port's `useItemOn` TODO(OFF-51) still needs. The map:
- **COOLANT cell**: water bucket → fill water (return empty bucket); heavy-water bucket → fill heavy
  water; empty bucket → drain back out (water bucket / heavy bucket depending on contents).
- **SYNTHESIZER**: water bucket → +1000 mB water.
- **ELECTROLYZER**: heavy-water bucket → +1000 mB heavy water.
- **FLUIDEXTRACTOR (heavy pump)**: empty bucket + tank holding heavy water → heavy-water bucket.
- **SODIUMBOILER**: sodium canister/bucket → +1000 mB sodium.
- **BOILER**: water bucket → +1000 mB water (only if empty or already water); ammonia canister →
  +1000 mB ammonia (only if empty or already ammonia).
- **PROCESSOR / CENTRIFUGE**: canister exchange — filled canister of the right fluid deposits into
  the tank, empty canister withdraws (CANISTER_FLUID data component replaces the metadata variant).
- **MAGNET (toroid)**: RotaryCraft meter/screwdriver → aim adjustment; REMOTE item hook.
- Fallback: open GUI (already ported via `openMenuIfPresent`).
Also had per-machine `getLightValue` (glowing machines) — worth checking when lighting passes.

## Recipes / handbook
- **ReactorRecipes.java (489)** — the source of truth for the ~32 machine crafting recipes still
  gated on unported RotaryCraft ingredients (see `datagen.md`). Transcribe from
  `git show origin/master:ReactorRecipes.java` when those land; never port as runtime code.
- **registry/ReactorBook.java (413) + auxiliary/ReactorDescriptions.java + guis/GuiReactorBook.java
  + guis/GuiReactorBookPage.java** — the in-game handbook (content registry, text, GUI). Feature is
  not ported; the `reactor_book` item is a placeholder. Recover all four together when porting it.
- **ReactorNames.java** — display-name tables: `canNames` (per-fluid canister names, matches the
  original lang `can.*` keys) and `rawNames`. Useful when the canister item gets per-fluid naming.

## GUI layer (superseded by Menu*/Screen*)
- `container/Container*.java` ×10 and `guis/Gui*.java` ×11 (besides the two book GUIs above) —
  each has a ported `Menu*`/`Screen*` twin; layouts were already transcribed.
- `ReactorGuiHandler.java`, `ClientProxy.java`, `CommonProxy.java` — FML gui-handler/proxy pattern,
  gone in NeoForge (menus register via `RotaryMenus`-style `MenuType`s; sides via dist events).

## Renderers superseded or dead
- `renders/DuctRenderer.java`, `SteamLineRenderer.java` (root), `renders/RenderWaterLine.java` —
  superseded by `ReactorPipeRenderer` / `ReactorLineRenderer`.
- `renders/RenderFusionHeater.java`, `renders/RenderFusionInjector.java` — dead upstream too: their
  model rendering is commented out in the 1.7.10 originals (the machines are texture-cubes).
- `ReactorItemRenderer.java` (root) — 1.7.10 IItemRenderer; replaced by `ReactorMachineItemRenderer`.
- `TritiumLampRenderer.java` (root) — renderer for the **fluorite/tritium lamp block, which is not
  ported yet** (also `items/ItemBlockLampMulti.java` with the lamp variant names). Recover both when
  the lamp block lands.

## Registration-era leftovers
- `items/ItemBlockFluorite.java`, `ItemBlockReactorOre.java`, `ItemBlockMultiBlock.java` — metadata
  block-items; every variant is its own registered block/item now.
- `LiquidHandler.java` (root) — legacy fluid-container registration; `ReactorFluids` +
  `CANISTER_FLUID` replace it.
- `ReactorConfig.java` (root) — the full 1.7.10 config option set. `ReactorOptions` currently holds
  hardcoded defaults; when wiring a real `ModConfigSpec`, use this file (via git) as the option list
  (incl. `heavyWaterDimensions`, LODESTONERFMULT, PIPEHARDNESS-equivalents).
- `nei/` ×5 — NEI handlers; superseded by `ReactorJEIPlugin` (+ RotaryCraft's JEI plugin categories).
