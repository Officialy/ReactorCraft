# ReactorCraft renders/GUI audit

Scope: `renders/`, `models/`, `container/`, `guis/`, `client/`, plus the registration wiring in
`registry/ReactorModelLayers.java`, `registry/ReactorMenus.java`, and `ReactorCraft.java`. Audited
only allowlisted/ported code (`build.gradle` `include` list); the ~16 unported 1.7.10 render stubs
and the old `container/Container*.java`/`guis/Gui*.java` pairs are catalogued below but not treated
as bugs — they are pristine, unregistered, and (for the old Container/Gui pair) only referenced by
the equally pristine `ReactorGuiHandler.java`, so none of it is reachable at runtime. The 10 ported
BER machine renderers, the pipe/line renderers, the fusion-marker line-guide renderer, and all 10
Menu/Screen pairs are faithfully built on the 26.2 submit/AbstractContainerScreen patterns and are
correctly wired end-to-end (registered `BlockEntityRenderer`s match registered `BlockEntityType`s;
every `MenuType` has a bound screen via `RegisterMenuScreensEvent`). The one systemic defect found is
real and mod-wide: four machine GUIs read their progress-bar percentage from a live, tick-only timer
object instead of the NBT-synced field that already exists for it, so those bars never move for the
player even though the underlying machine works correctly server-side. The other gaps are the
already-tracked "12 machines have no BER" coverage hole (cross-referenced here against the concrete
block/BE list) plus a couple of small faithfulness misses in the turbine and toroid-magnet renderers.

## [P1] Four machine GUIs read an un-synced live timer instead of the synced field
- **File:** `src/main/java/reika/reactorcraft/tileentities/processing/TileEntityUProcessor.java:239-245`,
  `TileEntitySynthesizer.java:96-100`, `TileEntityElectrolyzer.java:96-100`,
  `TileEntityCentrifuge.java:191-193`
- **Problem:** `getIntermediateTimerScaled`/`getOutputTimerScaled` (Processor), `getTimerScaled`
  (Synthesizer, Electrolyzer), and `getProcessingScaled` (Centrifuge, denominator only) all read from
  a `ParallelTicker`/`StepTimer` field (`timer`, `steptimer`) that is only ever mutated inside
  `updateEntity(Level,BlockPos)` — the tick method. `BlockReactorMachine.getTicker` (line ~106-108)
  returns `null` when `level.isClientSide()`, so that method **never runs on the client**. Each TE
  does sync a plain int for the same value (`output_timer`/`intermediate_timer` via `readSyncTag`
  for Processor; `time`/`split` for Electrolyzer/Centrifuge) but the getters used by the GUIs never
  read those synced fields — they read the dead client-side timer object instead.
- **Why wrong:** the progress arrows in `ScreenProcessor`, `ScreenSynthesizer`, and
  `ScreenElectrolyzer` are permanently stuck at 0 width for every player, forever, even while the
  machine is actively processing (server-verified: the synced ints do update, they're just not
  consulted). `ScreenCentrifuge`'s bar at least moves (its numerator `split` IS synced and IS read),
  but the denominator `timer.getCap()` is stuck at whatever `StepTimer`'s no-arg default construction
  value is client-side, not the recipe-power-dependent real cap set server-side by `setTimer()`, so
  the bar's scale is wrong whenever the real cap differs from the client-side default. This bug exists
  latent in the original 1.7.10 source too (`git show origin/master:.../TileEntityUProcessor.java`
  has the identical `timer.getPortionOfCap(...)` getter), but was masked there because 1.7.10
  `updateEntity` runs universally on both logical sides — porting to the modern server-only
  `BlockEntityTicker` model exposed it.
- **Fix:** make the getters read the already-synced ints instead of the live timer object, e.g.
  `getIntermediateTimerScaled(int p) { return p * intermediate_timer / <cap constant or a newly
  synced cap field>; }`. For Processor/Synthesizer/Electrolyzer the cap is a per-recipe/tier constant
  that also needs syncing (add a synced `cap`/`intermediateCap`/`outputCap` field, or recompute the
  cap client-side from already-synced state if it's derivable, mirroring how RotaryCraft's powered
  screens sync percent-complete directly rather than raw tick+cap pairs). For Centrifuge specifically,
  sync `timer.getCap()`'s value (e.g. as a `cap` int alongside `split`) and use it in
  `getProcessingScaled` instead of `timer.getCap()`.
- **Size:** M

## [P2] Turbine core always renders the spinning wheel, even when the multiblock isn't formed
- **File:** `src/main/java/reika/reactorcraft/renders/RenderTurbine.java:62-73`
- **Problem:** the legacy `RenderTurbine.renderTileEntityTurbineCoreAt` only draws the blade-wheel
  model when `tile.hasMultiBlock()` is true; otherwise it draws a flat "cap" quad using the turbine
  multiblock's own block texture (`ReactorBlocks.TURBINEMULTI.getIcon(0,3)`). The ported
  `renderModel` calls `models[stage].renderAll(...)` unconditionally and never checks
  `TileEntityTurbineCore.hasMultiBlock()` (confirmed present and functional at
  `TileEntityTurbineCore.java:107,123-124`).
- **Why wrong:** an unformed/incomplete turbine core (e.g. freshly placed, structure broken) shows
  the full spinning blade wheel in-world instead of the flat cap the original used to signal "not yet
  a working turbine" — visually misleading about machine state.
- **Fix:** branch on `turb.hasMultiBlock()` in `renderModel`: draw the wheel as now when true; when
  false, submit a flat quad using the turbine-multiblock's own texture (or, simplest 26.2-idiomatic
  fix, just skip the BER draw entirely and let the block's own baked model show, if `BlockTurbineMulti`
  already carries a normal cube/cap model for that case — check `blocks/multi/BlockTurbineMulti.java`).
- **Size:** M

## [P2] Turbine blade wheel never reflects the core's damage/wear state
- **File:** `src/main/java/reika/reactorcraft/models/ModelTurbine.java:73-106`,
  `src/main/java/reika/reactorcraft/renders/RenderTurbine.java:62-73`
- **Problem:** the legacy `ModelTurbine.renderBlades` computed the actual angular separation as
  `getAngularSeparation() * (damage + 1)` — a damaged turbine (`TileEntityTurbineCore.getDamage()`,
  fully implemented and NBT-synced-adjacent, drives `getDamageEfficiency()`) shows visibly fewer
  blades. The ported `ModelTurbine.renderAll`/`renderBlades` has no `damage`/`int` parameter at all;
  `RenderTurbine` never reads `getDamage()`.
  end
- **Why wrong:** a badly worn turbine (which is meaningfully less efficient, per
  `getDamageEfficiency()`) looks visually identical to a pristine one — players get no visual cue to
  go repair/replace it, unlike the original.
- **Fix:** thread `turb.getDamage()` through to `renderBlades` and multiply `angularSep(stage)` by
  `(damage + 1)` as the original did (need `getDamage()` synced to the client if not already — check
  `TileEntityTurbineCore` sync tag).
- **Size:** S

## [P3] Toroid-magnet aim indicator drops the IO-goggles "always opaque" override
- **File:** `src/main/java/reika/reactorcraft/renders/RenderMagnet.java:69-95`
- **Problem:** the legacy `renderAngleLine` forces the indicator's alpha to 255 (ignoring the natural
  fade) whenever the player is wearing/recently used IO goggles
  (`ItemIOGoggles.NBT_KEY` check against `worldObj.getTotalWorldTime()`); otherwise it uses
  `tile.getAlpha()`. The port always uses `Math.min(255, tile.getAlpha())` with no goggles check at
  all — the goggles-only permanent-visibility behavior is gone. Not a hard regression (the natural
  decay path is faithfully ported and the indicator still functions), just a dropped conditional.
- **Why wrong:** minor — the aim line/circles now always fade out ~4-8 seconds after the magnet's
  aim last changed, even for a player actively using IO goggles to line up multiple toroid magnets,
  where the original would keep it lit the whole time goggles are worn.
- **Fix:** low priority; if RotaryCraft's `ItemIOGoggles` equivalent exists in the port, gate the
  alpha the same way the original did. Otherwise leave as-is and drop the stale doc claim of full
  parity, or note the goggle gate as intentionally dropped.
- **Size:** S

## [P3] Dead 1.7.10 GUI/container pair and `ReactorGuiHandler` left in the tree
- **File:** `src/main/java/reika/reactorcraft/container/Container*.java` (10 files, e.g.
  `ContainerNuclearCore.java`, `ContainerCentrifuge.java`), `src/main/java/reika/reactorcraft/guis/Gui*.java`
  (10 files, e.g. `GuiNuclearCore.java`), `src/main/java/reika/reactorcraft/ReactorGuiHandler.java`,
  `src/main/java/reika/reactorcraft/guis/GuiReactorBook.java`,
  `src/main/java/reika/reactorcraft/guis/GuiReactorBookPage.java`
- **Problem:** these are the pristine pre-port 1.7.10 sources (still import `org.lwjgl.input.Keyboard`,
  `net.minecraftforge.common.util.ForgeDirection`, etc.) coexisting in the same packages as their
  ported `Menu*`/`Screen*` replacements. Not in the `build.gradle` allowlist, so they don't compile
  into the mod, and their only referrer (`ReactorGuiHandler`, itself unported/unused — the 1.7.10
  `IGuiHandler` concept has no equivalent in 26.2, menus open via `player.openMenu`) is equally dead.
  Confirmed via grep: no allowlisted file references any of `Container*`/`Gui*` (old) by name.
- **Why wrong:** pure clutter/confusion risk — a future contributor could mistake `GuiCentrifuge.java`
  for the live GUI when `ScreenCentrifuge.java` is the real one; also 20+ files of unreachable dead
  weight in packages that otherwise hold only live code.
- **Fix:** delete the 10 `Container*.java`, 10 `Gui*.java` (old), `ReactorGuiHandler.java`,
  `GuiReactorBook.java`, `GuiReactorBookPage.java` once the reactor-handbook screens
  (`OFF-50` TODO in `ReactorGuiBase.java:80-81`) are re-ported or explicitly dropped — don't delete
  `GuiReactorBook`/`GuiReactorBookPage` until that TODO is resolved one way or the other, since they're
  the reference for the deferred handbook-button feature.
- **Size:** S

## Renderer/GUI coverage checklist

Machine → in-world BER state: **(a)** ported + registered, **(b)** ported but not registered (dead),
**(c)** stubbed, **(d)** unported 1.7.10 (file exists, untouched, not on allowlist). All 46 registered
`BlockEntityType`s cross-referenced against `ReactorModelLayers.registerEntityRenderers`; only
`BlockReactorMachineModelled` blocks (empty in-world model, BER-only) are relevant — non-Modelled
blocks (fuel rod, coolant cell, boiler, pipes handled separately, etc.) use ordinary baked block
models and need no BER.

| Machine (BE) | Block | Renderer state | Notes |
|---|---|---|---|
| control_rod | Modelled | (a) `RenderControl` | rod slides with `getRodPosition()`, synced |
| turbine_core | Modelled | (a) `RenderTurbine` | see P2 findings above (multiblock gate, damage) |
| condenser | Modelled | (a) `RenderCondenser` | static, faithful |
| toroid_magnet | Modelled | (a) `RenderMagnet` | + aim-line overlay; see P3 goggles finding |
| solenoid_magnet | Modelled | (a) `RenderSolenoid` | coil spin via synced omega/phi; multiblock-gated correctly |
| steam_grate | Modelled | (a) `RenderSteamGrate` | static, faithful |
| waste_storage | Modelled | (a) `RenderWasteStorage` | static, faithful |
| electrolyzer | Modelled | (a) `RenderElectrolyzer` | static, faithful |
| solar_exchanger | Modelled | (a) `RenderSolarExchanger` | static, faithful |
| fusion_marker | Modelled | (a) `RenderFusionMarker` | line-guide build helper, faithful |
| gas_duct / magnetic_pipe / waste_pipe | Duct | (a) `ReactorPipeRenderer` | shared BER, per-fluid tint/sprite |
| steam_line / heat_pipe | Line | (a) `ReactorLineRenderer` | shared BER, heat pipe warm-tinted |
| heavy_pump | Modelled | (d)/(c) `RenderHeavyPump` unported | no BER registered — invisible in-world (noOcclusion) |
| isotope_centrifuge | Modelled | (d) `RenderCentrifuge` unported | no BER registered |
| uranium_processor | Modelled | (d) `RenderProcessor` unported | no BER registered |
| reactor_pump | Modelled | (d) `RenderReactorPump` unported | no BER registered |
| heat_exchanger | Modelled | (d) `RenderExchanger` unported | no BER registered |
| gas_collector | Modelled | (d) `RenderGasCollector` unported | no BER registered |
| turbine_flywheel | Modelled | (d) unported (no dedicated Render file found under this name) | no BER registered |
| reactor_generator | Modelled | (d) `RenderGenerator` unported | no BER registered |
| high_pressure_turbine | Modelled | (d) `RenderBigTurbine` unported | no BER registered |
| steam_diffuser | Modelled | (d) `RenderSteamDiffuser` unported | no BER registered |
| solar_top | Modelled | (d) `RenderSolarTop` unported | no BER registered |
| mini_turbine | Modelled | (d) `RenderMiniTurbine`/`RenderTurbineWheel` unported | no BER registered |
| fusion_heater / fusion_injector | non-Modelled | (d) `RenderFusionHeater`/`RenderFusionInjector` unported | not BER-only blocks; lower priority |

10 registered BERs + 2 shared pipe/line BERs = 12 renderer registrations covering 15 BE types (10
Modelled singles + 3 pipe + 2 line). 12 Modelled machine blocks have no BER at all (item icons
correctly fall back to flat `cube_all`, already tracked in `datagen.md`; in-world they are invisible
placeholders, not X-rayed, since `BlockReactorMachineModelled` applies `noOcclusion()` — confirmed at
`blocks/BlockReactorMachineModelled.java:9`).

GUI/Menu pairs — all 10 present, registered, and wired:

| Menu | Screen | MenuType registered | Screen bound (`RegisterMenuScreensEvent`) | Live progress data |
|---|---|---|---|---|
| MenuNuclearCore | ScreenNuclearCore | yes | yes | n/a (plain inventory) |
| MenuCentrifuge | ScreenCentrifuge | yes | yes | numerator synced; denominator NOT (see P1) |
| MenuWasteDecayer | ScreenWasteDecayer | yes | yes | n/a (plain inventory) |
| MenuWasteContainer | ScreenWasteContainer | yes | yes | n/a (plain inventory) |
| MenuPebbleBed | ScreenPebbleBed | yes | yes | n/a (plain grid) |
| MenuThoriumCore | ScreenThoriumCore | yes | yes | tank levels synced (fill sprite deferred, self-documented) |
| MenuSynthesizer | ScreenSynthesizer | yes | yes | NOT synced (see P1) |
| MenuProcessor | ScreenProcessor | yes | yes | NOT synced (see P1) |
| MenuElectrolyzer | ScreenElectrolyzer | yes | yes | NOT synced (see P1) |
| MenuWasteStorage | ScreenWasteStorage | yes | yes | n/a (plain grid) |
| MenuCPU | ScreenCPU | yes | yes | control-rod layout synced via `TileEntityCPU.writeSyncTag` |

No missing `MenuType` registrations, no unbound screens, no `Modelled` block silently missing from
`ReactorBlockEntities`. The live fluid-fill GUI sprite (tank contents) is deferred mod-wide — already
self-documented as a shared 26.2 gap in every affected screen's javadoc, not re-filed here.

## Summary

- **P1: 1** — the four-screen dead-timer bug (progress bars stuck/miscaled on Processor, Synthesizer,
  Electrolyzer, Centrifuge).
- **P2: 2** — turbine renders the spinning wheel even when the multiblock isn't formed; turbine blade
  count never reflects core damage.
- **P3: 2** — toroid-magnet aim indicator drops the IO-goggles always-opaque override; dead 1.7.10
  Container/Gui/GuiHandler files left in the tree alongside their ported replacements.

**Top 3:**
1. Fix the four-screen dead-timer/progress-bar bug (P1) — real player-visible breakage, affects the
   most-used processing machines (Processor, Synthesizer, Electrolyzer, Centrifuge).
2. Gate `RenderTurbine` on `hasMultiBlock()` (P2) — an unformed turbine shouldn't show a fully-formed
   spinning wheel.
3. Wire `getDamage()` into the turbine blade count (P2) — restores a meaningful wear-state visual cue.

Everything else audited (10 registered BERs' base transforms, spin math, atlas sizes vs PNG
dimensions, pipe/line renderer geometry, all 10 Menu/Screen registrations) checked out faithful to
the 1.7.10 original and consistent with the established 26.2 submit-pipeline conventions.
