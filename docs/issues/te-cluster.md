# ReactorCraft TE-cluster audit — `base/` + `tileentities/**`

Scope: `base/` (10 god-base/mixin classes), all 46 concrete TEs under `tileentities/**`, the TE
registry (`registry/ReactorTiles.java`, `registry/ReactorBlockEntities.java`), and the machine
block classes (`blocks/BlockReactorMachine*`, `BlockReactorDuct`, `BlockReactorLine`). Read-only.

## Overview

**PORTING.md is stale relative to the working tree.** It records the TE cluster at "~2527 errors,
compile FAILED" as of its last edit, but the working tree (16 files modified since the last commit
`dd7537a`, all uncommitted) has since finished that pass: `:ReactorCraft:compileJava --rerun`
compiles clean (**0 errors**, 174 deprecation warnings) and `:ReactorCraft:test` passes. All 46 TEs
and all 10 `base/` classes are on the `build.gradle` allowlist and compiling. The systematic
patterns PORTING.md named as residual blockers (`drain(int,boolean)`/`FluidTankInfo`, int-coord
`getBlockEntity`, `getSizeInventory`, `tank.getLevel()`, `inv[`, `FluidRegistry`, raw
`extends BlockEntity`) are **all fully resolved** — zero grep hits across `base/` and
`tileentities/**`. This audit therefore does two things: (1) documents that the compile-blocker
list is closed out and re-points it at the one residual pattern that *is* still live (deprecated
`IFluidHandler` API usage — the modern echo of the old fluid-handler-signature debt), and (2) does
the semantic diff this audit was really commissioned for: comparing the 10 ported `base/` classes
line-by-line against `origin/master` (Reika's 1.7.10 original) to find logic that was ported wrong
rather than merely modernized. Compiling and passing registry-integrity tests says nothing about
faithfulness — none of the existing tests tick a TE, exercise heat math, or touch sided I/O, which
is exactly where this pass found real bugs. Headline findings: the fluid capability was never wired
externally (no mod/hopper/bucket can fill or drain any reactor tank through the standard NeoForge
capability — only RC's own pipe protocol works), and the entire sided-inventory contract
(`canItemEnterFromSide`/`canItemExitToSide`/`canRemoveItem`) that 11 inventoried TEs implement is
dead code because the item capability registration ignores the side context and hands out the raw
unfiltered handler.

## Compile-blocking pattern summary (counts)

All patterns PORTING.md flagged as the "~2527 errors" residue are **resolved (0 files)** as of this
session's uncommitted work:

| Pattern (PORTING.md's residual list) | Grep hits in `base/`+`tileentities/**` |
| --- | --- |
| Old fluid-handler sigs `drain(ForgeDirection,...)` / `FluidTankInfo` | 0 |
| Int-coord `getTileEntity(x,y,z)` / non-`BlockPos` block-entity lookups | 0 |
| `getSizeInventory()` (pre-`getContainerSize()`) | 0 |
| `tank.getLevel()` (pre-`getFluidLevel()`) | 0 |
| Raw `inv[` array access (pre-`itemHandler`) | 0 |
| `FluidRegistry.` (pre-`ReactorFluids`) | 0 |
| TEs still `extends BlockEntity` raw (not through a `base/` class) | 0 |

**Residual pattern still present — deprecated `IFluidHandler` surface (the modern shape of the same
debt):** every `TileEntityTankedReactorMachine` subclass still implements the pre-`ResourceHandler`
`net.neoforged.neoforge.fluids.capability.IFluidHandler` (`drain`/`fill`/`getTanks`/
`getFluidInTank`/`getTankCapacity`/`isFluidValid`), which NeoForge has marked `@Deprecated(forRemoval)`.
**174 compiler warnings, concentrated in 22 files** (all use counts, `--rerun` clean build):

- `tileentities/processing/TileEntityUProcessor.java` — 18
- `base/TileEntityReactorPiping.java` — 14
- `tileentities/TileEntityHeavyPump.java` — 12
- `tileentities/processing/{TileEntityTritizer,TileEntitySynthesizer,TileEntityElectrolyzer,TileEntityCentrifuge}.java` — 10 each
- `tileentities/powergen/{TileEntityTurbineCore,TileEntitySteamInjector}.java` — 10 each
- `tileentities/fusion/{TileEntityFusionInjector,TileEntityFusionHeater}.java` — 10 each
- `tileentities/fission/thorium/TileEntityThoriumCore.java` — 10
- `tileentities/TileEntityGasCollector.java` — 10
- `base/TileEntityTankedReactorMachine.java` — 10
- `tileentities/powergen/TileEntityReactorPump.java` — 5
- `tileentities/fission/TileEntityWaterCell.java` — 5
- `base/ReactorGuiBase.java` — 4
- `tileentities/powergen/{TileEntitySolarExchanger,TileEntityHeatExchanger,TileEntityCondenser}.java`, `tileentities/fission/thorium/TileEntityFuelDump.java`, `tileentities/TileEntitySteamDiffuser.java`, `base/TileEntityIntermediateBoiler.java` — 1 each

This is not a compile blocker (deprecated ≠ broken) but is the real "next systematic pass": migrate
off `IFluidHandler` onto the new `ResourceHandler<FluidResource>` capability, mirroring whatever
RotaryCraft ends up doing (RotaryCraft has the identical `CAP-PORT` comment in its own piping base,
per the comment left in `registry/ReactorBlockEntities.java:200-202`). Not urgent, but 22 files is
the honest number for "how much fluid-capability code still targets a doomed API."

## Per-TE port status checklist

All 46 are allowlisted and compiling (bucket **(a) below** unless noted). Bucket legend:
**(a)** ported and looks faithful on a quick scan (imports modern, no stub/TODO markers beyond
Reika's own pre-existing ones, reasonable body size). **(b)** ported but carries a documented gap
(a `*-PORT` comment gating out an optional integration/feature) or a flagged semantic issue from
this audit. **(c)** N/A this session — no TE remains pristine 1.7.10; the whole cluster is allowlisted.

- [a] `tileentities/TileEntityFusionMarker.java` — 99 lines, clean.
- [a] `tileentities/TileEntityGasCollector.java` — 190 lines; 10 deprecated-`IFluidHandler` warnings (cosmetic).
- [a] `tileentities/TileEntityGasDuct.java` — 43 lines, trivial pipe leaf.
- [b] `tileentities/TileEntityHeatPipe.java` — `ventHeat()` call commented out with `//TODO fix heat pipe heat loss`; verified this is Reika's **own** pre-existing 1.7.10 TODO (`git show origin/master:TileEntities/TileEntityHeatPipe.java`), faithfully preserved, not a port regression.
- [a] `tileentities/TileEntityHeavyPump.java` — 430 lines; 12 deprecated-`IFluidHandler` warnings.
- [a] `tileentities/TileEntityMagneticPipe.java` — 177 lines, clean.
- [a] `tileentities/TileEntityNeutronReflector.java` — 63 lines, clean.
- [a] `tileentities/TileEntityReactorFlywheel.java` — 223 lines, clean.
- [a] `tileentities/TileEntityReactorGenerator.java` — 417 lines, clean.
- [a] `tileentities/TileEntitySolarTop.java` — 140 lines, clean.
- [a] `tileentities/TileEntitySteamDiffuser.java` — 146 lines, clean.
- [a] `tileentities/TileEntityTurbineMeter.java` — 106 lines, clean.
- [a] `tileentities/fission/TileEntityCPU.java` — 379 lines, clean.
- [a] `tileentities/fission/TileEntityControlRod.java` — 242 lines, clean.
- [a] `tileentities/fission/TileEntityFuelRod.java` — 127 lines, clean.
- [b] `tileentities/fission/TileEntityReactorBoiler.java` — 223 lines; has a `CHROMA-PORT` marker (ChromatiCraft gated out, documented, expected).
- [a] `tileentities/fission/TileEntityWaterCell.java` — 228 lines; 5 deprecated-`IFluidHandler` warnings.
- [b] `tileentities/fission/breeder/TileEntitySodiumHeater.java` — 91 lines; `CHROMA-PORT` marker (documented).
- [a] `tileentities/fission/breeder/TileEntityBreederCore.java` — 169 lines, clean.
- [a] `tileentities/fission/thorium/TileEntityFuelDump.java` — 138 lines; 1 deprecated-`IFluidHandler` warning.
- [b] `tileentities/fission/thorium/TileEntityThoriumCore.java` — 423 lines; 10 deprecated-`IFluidHandler` warnings; git-status shows this file was mid-edit this session (uncommitted).
- [a] `tileentities/fusion/TileEntityFusionHeater.java` — 292 lines; 10 deprecated-`IFluidHandler` warnings.
- [a] `tileentities/fusion/TileEntityFusionInjector.java` — 258 lines; 10 deprecated-`IFluidHandler` warnings.
- [a] `tileentities/fusion/TileEntityNeutronAbsorber.java` — 98 lines, clean.
- [a] `tileentities/fusion/TileEntitySolenoidMagnet.java` — 360 lines, clean.
- [b] `tileentities/fusion/TileEntityToroidMagnet.java` — 580 lines; `CHUNKLOAD-PORT` marker (chunk-loading manager not ported, documented no-op — see base finding below, same root cause as `TileEntityNuclearCore`).
- [a] `tileentities/htgr/TileEntityCO2Heater.java` — 85 lines, clean.
- [a] `tileentities/htgr/TileEntityPebbleBed.java` — 393 lines; git-status shows mid-edit this session (uncommitted).
- [b] `tileentities/powergen/TileEntityCentrifugalTurbine.java` — 106 lines; `//TODO Incomplete item` — verified this is Reika's own pre-existing 1.7.10 comment (the machine was unfinished in the original mod too), not a port gap.
- [a] `tileentities/powergen/TileEntityCondenser.java` — 137 lines; 1 deprecated-`IFluidHandler` warning.
- [a] `tileentities/powergen/TileEntityHeatExchanger.java` — 391 lines; 1 deprecated-`IFluidHandler` warning.
- [a] `tileentities/powergen/TileEntityHiPTurbine.java` — 402 lines; `CHROMA-PORT` marker (documented).
- [a] `tileentities/powergen/TileEntityReactorPump.java` — 197 lines; 5 deprecated-`IFluidHandler` warnings.
- [a] `tileentities/powergen/TileEntitySolarExchanger.java` — 112 lines; `CHROMA-PORT` marker + 1 deprecated-`IFluidHandler` warning.
- [a] `tileentities/powergen/TileEntitySteamGrate.java` — 146 lines, clean.
- [a] `tileentities/powergen/TileEntitySteamInjector.java` — 149 lines; 10 deprecated-`IFluidHandler` warnings.
- [b] `tileentities/powergen/TileEntitySteamLine.java` — 230 lines; `MOD-PORT` marker (RotaryCraft `PipePump` connection gated out — `BlockEntityPipePump`/`MachineRegistry.PIPEPUMP` not ported yet, documented functional gap).
- [a] `tileentities/powergen/TileEntityTurbineCore.java` — 840 lines (largest TE); 10 deprecated-`IFluidHandler` warnings.
- [a] `tileentities/processing/TileEntityCentrifuge.java` — 471 lines; 10 deprecated-`IFluidHandler` warnings.
- [a] `tileentities/processing/TileEntityElectrolyzer.java` — 534 lines; `CHROMA-PORT` marker + 10 deprecated-`IFluidHandler` warnings.
- [a] `tileentities/processing/TileEntitySynthesizer.java` — 468 lines; 10 deprecated-`IFluidHandler` warnings.
- [a] `tileentities/processing/TileEntityTritizer.java` — 285 lines; 10 deprecated-`IFluidHandler` warnings.
- [a] `tileentities/processing/TileEntityUProcessor.java` — 491 lines; `CHROMA-PORT` marker + 18 deprecated-`IFluidHandler` warnings (highest count in the cluster).
- [b] `tileentities/processing/TileEntityWasteDecayer.java` — 257 lines; git-status shows mid-edit this session (uncommitted).
- [b] `tileentities/waste/TileEntityWasteContainer.java` — 275 lines; git-status shows mid-edit this session (uncommitted).
- [b] `tileentities/waste/TileEntityWastePipe.java` — 73 lines; `CHROMA-PORT` marker (`WorldRift` gated out) + relies on `isConnectionValidForSide` (see base finding — possible axis-flip bug propagates here via `isConnectedToNonSelf`).
- [b] `tileentities/waste/TileEntityWasteStorage.java` — 248 lines; git-status shows mid-edit this session (uncommitted).

## Semantic bugs in ported base classes

Diffed against `origin/master:Base/<Name>.java` (confirmed this submodule's `origin` IS Reika's
1.7.10 source; un-prefixed `Base/`, `TileEntities/`, etc.). Filtered out pure API modernization
(`World→Level`, `NBTTagCompound→CompoundTag`, `ForgeDirection→Direction`, `writeToNBT/readFromNBT`
→`saveAdditional/loadAdditional`) — only logic drift is listed below.

### [P1] Sided item-transfer contract (`canItemEnterFromSide`/`canItemExitToSide`/`canRemoveItem`) is never enforced
> **Reconciliation (verified after the audit):** RotaryCraft's `RotaryBlockEntities.registerItemCap`
> is byte-for-byte the same (raw handler, ignores `ctx`), and DragonAPI's `ManagedItemHandler` has no
> sided variant. So this — like the fluid-cap P1 below — is a **shared port-wide deferral mirroring the
> reference mod**, not a ReactorCraft regression. Do NOT fix it in ReactorCraft in isolation: the real
> fix is a side-aware wrapper in DragonAPI/`ManagedItemHandler` applied to both mods. Reclassify as a
> shared-architecture follow-up.
- **File:** `src/main/java/reika/reactorcraft/registry/ReactorBlockEntities.java:195-198` (capability
  registration) vs. the contract declared in `src/main/java/reika/reactorcraft/base/TileEntityInventoriedReactorBase.java:104-108`
  and implemented per-TE, e.g. `src/main/java/reika/reactorcraft/base/TileEntityNuclearCore.java:274-282`
  (`canItemEnterFromSide` → `UP` only, `canItemExitToSide` → `DOWN` only).
- **Problem:** `registerItemCap` hands out the raw `ManagedItemHandler` unconditionally and ignores
  the capability's side context (`ctx`) entirely: `(be, ctx) -> be instanceof HasItemHandler h ? h.getItemHandler() : null`.
  In 1.7.10, `ISidedInventory.canInsertItem`/`canExtractItem` (ultimately calling the abstract
  `canItemEnterFromSide`/`canItemExitToSide`/`canRemoveItem` hooks) were the actual gate Forge's
  hopper/pipe code consulted. In the port, `TileEntityInventoriedReactorBase` still declares those
  three methods as an abstract "contract for subclasses" (per PORTING.md) and every subclass still
  implements them (verified in `TileEntityNuclearCore`, `TileEntityWasteUnit`), but **nothing calls
  them** — not the capability registration, not `BlockReactorMachine`. `grep` confirms zero call
  sites outside the declaring/implementing files themselves.
- **Why wrong:** any hopper, pipe, or other mod reading the block-entity item capability can insert
  or extract from *any* side, on *any* inventoried TE. Concretely: `TileEntityNuclearCore` intends
  fuel to enter only from `UP` and spent fuel/waste to exit only `DOWN` (the fuel-rod conveyor
  mechanic — see `feed()`/`feedWaste()` at lines 160-224, which assume that discipline is what keeps
  external item flow consistent with the internal slot-shifting logic); with the gate absent, a
  hopper on any side can pull waste out of slots or shove items into slots that were never meant to
  be externally writable, corrupting the intended fuel pipeline.
- **Fix:** wrap the handler returned from `registerItemCap` in a side-aware adapter that consults
  `canItemEnterFromSide(ctx)`/`canItemExitToSide(ctx)` for insert/extract when the TE is a
  `TileEntityInventoriedReactorBase`, and `isItemValidForSlot`/`canRemoveItem` per-slot — mirroring
  whatever sided-capability wrapper pattern RotaryCraft's `InventoriedRCBlockEntity` already uses
  for its own hopper-facing machines (RotaryCraft is the Rosetta Stone for this exact gap).
- **Size:** M — one wrapper class/method, reused by every inventoried TE; no TE body changes needed.

### [P1] External fluid capability never registered — only RC's own pipe protocol can move fluid
- **File:** `src/main/java/reika/reactorcraft/registry/ReactorBlockEntities.java:200-202` (comment
  admits the gap: "external `Capabilities.Fluid.BLOCK` exposure deferred... wiring them to the new
  `ResourceHandler<FluidResource>` capability is a follow-up").
- **Problem:** In 1.7.10, `TileEntityTankedReactorMachine implements IFluidHandler` was directly the
  Forge fluid capability — any hopper-equivalent, bucket, or other mod's pipe could call
  `fill`/`drain` on the tile. In the port, `IFluidHandler` is still implemented (for internal
  RC-pipe interop, since `PipeConnector`/`fillPipe`/`drainPipe` are the live path — see
  `base/TileEntityReactorPiping.java`'s `intakeFluid`/`dumpContents`, which check
  `te instanceof IFluidHandler` and calls `.drain(...)`/`.fill(...)` directly rather than through
  the capability system), but `RegisterCapabilitiesEvent` never calls
  `event.registerBlockEntity(Capabilities.Fluid.BLOCK, ...)` for any ReactorCraft TE.
- **Why wrong:** no vanilla bucket-on-block interaction, no other mod's fluid pipe, and no
  capability-based hopper-equivalent can fill or drain a ReactorCraft tank from outside the mod.
  Only ReactorCraft's/RotaryCraft's own `PipeConnector` network (their custom pipes) still works,
  because that path bypasses the capability system and calls the `IFluidHandler`/`fillPipe` methods
  directly on the tile reference. This is a real cross-mod-interop regression versus 1.7.10, not
  just a warning-noise issue — it is the *reason* the 174 deprecated-`IFluidHandler` warnings exist
  at all (the interface is kept alive only for this internal use, not deprecated-but-harmless).
- **Fix:** wire `Capabilities.Fluid.BLOCK` in `ReactorBlockEntities.registerCapabilities` for every
  `TileEntityTankedReactorMachine`/`TileEntityReactorPiping`, gating the exposed direction through
  `canReceiveFrom`/`canFill`/`canDrain` as appropriate — same shape as the item-capability fix above.
  Tracked already as a comment in-repo; this audit confirms it is a genuine functional gap, not
  speculative.
- **Size:** M — capability registration + a thin `IFluidHandler`-to-`ResourceHandler` (or direct
  `IFluidHandler`) side-gated adapter, reused across all tanked/piping TEs.

### [P2] Generic (non-pipe) `fill`/`drain` on tanked machines bypasses the sided-input gate
- **File:** `src/main/java/reika/reactorcraft/base/TileEntityTankedReactorMachine.java:85-100`.
- **Problem:** original `fill(ForgeDirection from, FluidStack resource, boolean doFill)` (1.7.10,
  `Base/TileEntityTankedReactorMachine.java:69-73`) always checked
  `this.canFill(from, resource.getFluid())` — i.e. `canReceiveFrom(from) && isValidFluid(...)` —
  before filling. The ported generic-capability `fill(FluidStack resource, FluidAction action)`
  (no `from` parameter; this is the un-sided `IFluidHandler` method) only checks
  `!resource.isEmpty() && isValidFluid(...)` — **`canReceiveFrom` is never consulted** for this
  entry point. Only the RC-specific `fillPipe(Direction from, ...)` (lines 109-113) still gates on
  `canFill`/`canReceiveFrom`.
- **Why wrong:** any caller that fills through the plain (unsided) `IFluidHandler.fill` — which
  becomes live the moment finding P1 above is fixed and the capability is registered per-side —
  would bypass the side restriction entirely (e.g. a machine that should only accept fluid from
  `DOWN` would instead accept it from every side through this path). Currently masked by P1 (no
  capability registered at all), but will resurface silently the moment someone "fixes" P1 without
  also checking this method.
- **Fix:** either drop the unsided `fill` override (let capability registration be the only sided
  gate, i.e. don't expose the handler at all on disallowed sides) or make this method reject like
  `fillPipe` does. Flag as a fixup to do *together with* the P1 fluid-capability wiring, not
  separately — same PR.
- **Size:** S.

### [P2] `isConnectionValidForSide` dropped the axis-relative direction flip (both `TileEntityLine` and `TileEntityReactorPiping`)
- **Files:**
  `src/main/java/reika/reactorcraft/base/TileEntityLine.java:83-85`,
  `src/main/java/reika/reactorcraft/base/TileEntityReactorPiping.java:124-127`.
- **Problem:** the original method in both classes was:
  ```java
public boolean isConnectionValidForSide(ForgeDirection dir) {
    if (dir.offsetX == 0 && MinecraftForgeClient.getRenderPass() != 1)
        dir = dir.getOpposite();
    return connections[dir.ordinal()];
}
```
  (`origin/master:Base/TileEntityLine.java:81-85`, `origin/master:Base/TileEntityReactorPiping.java:130-134`).
  The port collapsed this to `return connections[dir.ordinal()];` with **no flip at all** for any
  axis. The doc comment directly above it (kept verbatim in the port) still says: *"Direction is
  relative to the piping block (so DOWN means the block is below the pipe)"* — i.e. callers are
  documented to pass a direction in a convention that needs reconciling against how `connections[]`
  is actually populated (`isConnected(dirs[i])`, a plain unflipped absolute-direction neighbor
  check). Render-pass gating disappearing in 26.2 (no more multi-pass block rendering) is expected
  and correct to remove, but the `dir.offsetX == 0` (i.e. **non-X-axis / vertical**) branch of the
  flip looks like a separate, still-relevant renderer/caller-convention reconciliation that was
  deleted along with the render-pass check rather than kept.
- **Why wrong (impact, not just theoretical):** this method has two live callers that were verified
  in the ported tree: `renders/ReactorLineRenderer.java:66` (visual pipe/line connector rendering)
  and `tileentities/waste/TileEntityWastePipe.java:41` (`isConnectedToNonSelf`, gameplay logic used
  to decide whether a neighboring waste-pipe segment counts as a distinct pipe run). If the flip was
  load-bearing, vertical (up/down) connections now read the wrong array slot in both the renderer
  and this gameplay check — pipes could visually draw disconnected/connected backwards on their
  vertical faces, and `TileEntityWastePipe`'s connectivity test could give the wrong answer for
  up/down neighbors.
- **Fix:** needs a careful reconstruction of what the axis-flip was compensating for (likely a
  render-pass-1 vs render-pass-0 coordinate-convention mismatch specific to 1.7.10's block renderer
  callback, in which case dropping it entirely may coincidentally be correct now that there's only
  one render pass — but the *vertical-axis* half of the condition is independent of the render-pass
  check and deserves being re-verified against how the new renderer actually calls this, not assumed
  away). Recommend: write a small in-game repro (place a vertical run of waste pipes/lines, verify
  render + `isConnectedToNonSelf` behavior for the up/down faces) before deciding whether to restore
  the flip.
- **Size:** S–M (small code change, but needs an in-game check to confirm which behavior is correct).

### [P3] `TileEntityNuclearCore`/`TileEntityToroidMagnet` chunk-loading silently disabled
- **File:** `src/main/java/reika/reactorcraft/base/TileEntityNuclearCore.java:130-145` (`onActivityChange`/`unload`).
- **Problem:** original called `ChunkManager.instance.loadChunks(this)` /
  `ChunkManager.instance.unloadChunks(this)` when an active reactor core's activity timer
  transitioned; the port replaces both calls with `CHUNKLOAD-PORT` comments (DragonAPI's chunk
  manager isn't ported yet) — `getChunksToLoad()` is still implemented and correct (a faithful 3x3
  chunk-square reimplementation of `ChunkManager.getChunkSquare(x,z,1)`), but nothing ever calls it.
- **Why wrong:** this is a real feature gap (active reactor cores no longer force-load their chunks,
  so an active reactor can go dormant/desync if its chunk unloads) but it's **honestly documented
  in-code** as a deferred dependency, not a silent divergence — downgraded to P3 because it's
  tracked, not hidden, and blocked on an upstream DragonAPI port item, not a TE-cluster fix.
- **Fix:** port DragonAPI's `ChunkManager` (or NeoForge's `ForcedChunkManager`/ticket API
  equivalent) and re-wire; out of scope for this TE-cluster batch specifically.
- **Size:** M, but blocked on DragonAPI, not actionable here.

### No findings (verified faithful)
`TileEntityReactorBase` (god-base, 343 lines) — the heat-conduction/transducer math, temperature
diffusion loop, `getMessages` display-string logic, and dimension/Nether-ambient-temperature check
(`world.dimension() != Level.NETHER` correctly mirrors `world.provider.dimensionId != -1`) all match
line-for-line modulo the expected API renames. `TankedReactorPowerReceiver` — faithful; the
apparent NBT-duplication removal (`tank.readFromNBT`/`writeToNBT` no longer called directly here) is
correct because the immediate parent `TileEntityTankedReactorMachine` already does it — the
original had a harmless double-call, the port just de-duplicated it. `TileEntityNuclearBoiler` —
faithful; the `Proportionality<ReactorType>` NBT re-encoding uses a different (but internally
self-consistent) key scheme since `Proportionality` has no NBT methods in the port (per PORTING.md),
and the BuildCraft-only `overridePipeConnection`/`canDrain`/`drain` overrides are correctly dropped
(BuildCraft not present). `TileEntityIntermediateBoiler` — faithful; drain/fill semantics correctly
route to the RC pipe protocol, `canHeat`/`heat`/`transferFluid` math unchanged. `TileEntityWasteUnit`
— faithful; ChromatiCraft adjacency-decay-acceleration branch correctly collapses to the
"ChromatiCraft absent" behavior (`getAcceleratorBoost()` returns `1`, matching what 1.7.10 did when
the mod wasn't loaded).

## Summary

- **Compile status:** GREEN. `:ReactorCraft:compileJava --rerun` → 0 errors, 174 warnings;
  `:ReactorCraft:test` → BUILD SUCCESSFUL. PORTING.md's "~2527 errors" is stale (uncommitted
  work-in-progress since finished the pass). All 7 named systematic patterns from PORTING.md: **0
  remaining occurrences** in `base/`+`tileentities/**`.
- **Residual pattern:** deprecated `IFluidHandler` capability surface, **22 files / 174 warnings**
  (worst: `TileEntityUProcessor` 18, `TileEntityReactorPiping` 14, `TileEntityHeavyPump` 12). Not a
  build blocker; flagged as the next systematic cleanup once a `ResourceHandler<FluidResource>`
  migration path exists.
- **Per-TE checklist:** all 46 TEs ported & allowlisted. **39** clean-scan ("a"), **7** carry a
  documented gap/marker worth knowing about ("b": `TileEntityHeatPipe`, `TileEntityReactorBoiler`,
  `TileEntitySodiumHeater`, `TileEntityThoriumCore`, `TileEntityToroidMagnet`,
  `TileEntityCentrifugalTurbine`, `TileEntitySteamLine`, `TileEntityWastePipe`, plus 4 files
  (`TileEntityThoriumCore`, `TileEntityPebbleBed`, `TileEntityWasteDecayer`, `TileEntityWasteContainer`,
  `TileEntityWasteStorage`) mid-edit and uncommitted this session — re-audit those once committed).
  Zero TEs remain pristine 1.7.10 or thin-stub.
- **Top semantic bugs (base classes), by severity:**
  1. **[P1]** Sided item-transfer contract (`canItemEnterFromSide`/`canItemExitToSide`/`canRemoveItem`)
     declared and implemented by 11 inventoried TEs but never consulted — capability registration
     hands out unfiltered access from every side. (`registry/ReactorBlockEntities.java:195-198`)
  2. **[P1]** No external `Fluid` capability registered at all for any tanked/piping TE — only RC's
     internal pipe protocol can move fluid in/out; buckets, hoppers, and other mods' pipes cannot.
     (`registry/ReactorBlockEntities.java:200-202`)
  3. **[P2]** `TileEntityTankedReactorMachine`'s generic `fill(FluidStack,FluidAction)` skips the
     `canReceiveFrom` side gate that `fillPipe` still enforces — will silently reopen a hole the
     moment #2 is fixed unless fixed in the same change. (`base/TileEntityTankedReactorMachine.java:85-90`)
  4. **[P2]** `isConnectionValidForSide` lost its axis-relative direction flip in both
     `TileEntityLine` and `TileEntityReactorPiping`, with two live callers (a renderer and a
     gameplay connectivity check) that could be reading the wrong cached-connection slot for
     vertical neighbors. (`base/TileEntityLine.java:83-85`, `base/TileEntityReactorPiping.java:124-127`)
  5. **[P3]** Reactor-core chunk-loading silently disabled pending a DragonAPI `ChunkManager` port
     — honestly documented, not hidden. (`base/TileEntityNuclearCore.java:130-145`)
