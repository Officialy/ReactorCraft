# AI-slop / port-narration comment catalog

Comments that narrate the **port process** or reference old/new API/versions rather than
explaining what the code does. Rule (from `PORTING.md`): *"No AI-slop comments narrating what
changed."* Keep comments that explain game mechanics or non-obvious runtime behaviour, and keep the
`@author Reika Kalseki` license headers.

- **REMOVE** — pure port narration; delete the line/comment.
- **TRIM** — comment mixes real behaviour info with port history; strip the port-history half.

Collected by a module-wide sweep. Two packages (`blocks/`, and `registry/`+`base/`+`api/`) were not
finished before the session limit — see the bottom of this file. Verify each against the current
file before deleting (line numbers drift).

Totals captured so far: **~56 REMOVE, ~69 TRIM** across ~60 files.

---

## tileentities/ + world/  (27 REMOVE, 13 TRIM)

### REMOVE
- `tileentities/TileEntityReactorGenerator.java:116` — "// CoFH IEnergyReceiver is gone in 26.2 — push energy to the adjacent block's NeoForge transfer-API EnergyHandler capability instead."
- `tileentities/TileEntityReactorGenerator.java:229` — "// 1.21.5: BlockEntity.getRenderBoundingBox was removed; kept as a helper for the renderer's bounds."
- `tileentities/TileEntityReactorGenerator.java:243` — "// IndustrialCraft EU output removed (IC2 not in the 26.2 build; PowerTypes.EU no longer exists)."
- `tileentities/TileEntityMagneticPipe.java:23` — "// CHROMA-PORT: import reika.chromaticraft.api.interfaces.WorldRift;"
- `tileentities/TileEntityMagneticPipe.java:92` — "// CHROMA-PORT: ChromatiCraft WorldRift cross-dimension charge transfer gated out (mod not in build)."
- `tileentities/TileEntityMagneticPipe.java:163` — "// CHROMA-PORT: WorldRift acceptance gated out (ChromatiCraft not in build)."
- `tileentities/TileEntityHeavyPump.java:355` — "// CONFIG-PORT: ReactorConfig is not yet wired to a NeoForge ModConfigSpec accessor; ... honour that default."
- `tileentities/TileEntityGasCollector.java:154` — "// 1.21.5: BlockEntity.getRenderBoundingBox was removed; kept as a helper for the renderer's bounds."
- `tileentities/TileEntityHeatPipe.java:71` — "//this.ventHeat(world, pos); //TODO fix heat pipe heat loss"
- `tileentities/TileEntityHeatPipe.java:117` — "// CHROMA-PORT: ChromatiCraft WorldRift cross-dimension heat transfer gated out (mod not in build)."
- `tileentities/TileEntityFusionMarker.java:93` — "// 1.21.5: BlockEntity.getRenderBoundingBox was removed; kept as a helper for the renderer's bounds."
- `tileentities/fusion/TileEntityToroidMagnet.java:170` — "// CHUNKLOAD-PORT: ChunkManager.instance.loadChunks(this); — DragonAPI chunkloading manager is not ported yet ..."
- `tileentities/fusion/TileEntityToroidMagnet.java:181` — "// CHUNKLOAD-PORT: ChunkManager.instance.unloadChunks(this);"
- `tileentities/fusion/TileEntityToroidMagnet.java:450` — "// 1.21.5: BlockEntity.getRenderBoundingBox was removed; ..."
- `tileentities/fusion/TileEntityToroidMagnet.java:535` — "// CHUNKLOAD-PORT: ChunkManager.instance.unloadChunks(this);"
- `tileentities/fusion/TileEntitySolenoidMagnet.java:254` — "// 1.21.5: BlockEntity.getRenderBoundingBox was removed; ..."
- `tileentities/waste/TileEntityWastePipe.java:19` — "// CHROMA-PORT: import reika.chromaticraft.api.interfaces.WorldRift;"
- `tileentities/waste/TileEntityWastePipe.java:63` — "// CHROMA-PORT: WorldRift acceptance gated out (ChromatiCraft not in build)."
- `tileentities/waste/TileEntityWastePipe.java:68` — "// MOD-PORT: RotaryCraft BlockEntityCrystallizer is not yet ported ... re-add `|| te instanceof BlockEntityCrystallizer` once it lands ..."
- `tileentities/processing/TileEntityUProcessor.java:111` — "// MOD-PORT: IC2 PURECRUSHEDU input gated out (IC2 not in 26.2 build); re-add when IC2Handler ports."
- `tileentities/processing/TileEntityElectrolyzer.java:434` — "// MOD-PORT: legacy ItemMatch(\"salt/dustSalt\") oredict string -> direct match ... re-add a salt tag match if ..."
- `tileentities/fission/TileEntityReactorBoiler.java:100` — "// MOD-PORT: scrap drop (RotaryCraft ItemStacks.scrap) restored when that item is ported."
- `tileentities/fission/TileEntityReactorBoiler.java:194` — "// MOD-PORT: scrap drops (RotaryCraft ItemStacks.scrap) restored when that item is ported."
- `tileentities/fission/breeder/TileEntitySodiumHeater.java:77` — "// MOD-PORT: scrap/ironscrap drops (RotaryCraft ItemStacks) restored when those items are ported."
- `tileentities/powergen/TileEntitySteamLine.java:92` — "// MOD-PORT: RotaryCraft PipePump connection gated out (BlockEntityPipePump/MachineRegistry.PIPEPUMP not ported)."
- `tileentities/powergen/TileEntitySteamLine.java:130` — "// CHROMA-PORT: ChromatiCraft WorldRift cross-dimension steam transfer gated out (mod not in build)."
- `tileentities/powergen/TileEntityHiPTurbine.java:177` — "// MOD-PORT: BuildCraft tank output (BCMachineHandler) dropped — BuildCraft not in build."
- `tileentities/powergen/TileEntityHiPTurbine.java:202` — "// DRAGONAPI-PORT: AtmosphereHandler.isNoAtmo (Galacticraft vacuum check) gone — assume atmosphere."
- `tileentities/powergen/TileEntitySolarExchanger.java:26` — "// MOD-PORT: implements RotaryCraft's SodiumSolarOutput once that ... interface is ported; ... gated out."
- `tileentities/powergen/TileEntitySteamGrate.java:66` — "// DRAGONAPI-PORT: AtmosphereHandler.isNoAtmo (Galacticraft vacuum check) gone — assume atmosphere."

### TRIM  (strip the port-history half; keep the tank-index / behaviour info)
- `tileentities/TileEntityHeavyPump.java:69` — dimID mapping comment: drop "The legacy world-fluid logic … keyed on the old integer dimension ids".
- `tileentities/TileEntityHeavyPump.java:190` — "// --- NeoForge IFluidHandler (output-only …) ---" → "// --- Fluid tank (output-only …) ---"
- `tileentities/TileEntityGasCollector.java:89` — same "NeoForge IFluidHandler" banner → "Fluid tank".
- `tileentities/fusion/TileEntitySolenoidMagnet.java:132` — drop the "(unlike the legacy 'casing directly below' check)" aside.
- `tileentities/fusion/TileEntitySolenoidMagnet.java:149` — drop "Public:"; keep the client-phi rationale.
- `tileentities/processing/TileEntityUProcessor.java:376` — "// --- NeoForge IFluidHandler (0=input,1=intermediate,2=output) ---" → keep just the tank indices.
- `tileentities/processing/TileEntityElectrolyzer.java:151` — tank-index banner, drop "NeoForge IFluidHandler".
- `tileentities/processing/TileEntityTritizer.java:197` — same.
- `tileentities/processing/TileEntitySynthesizer.java:261` — same.
- `tileentities/processing/TileEntityCentrifuge.java:279` — same.
- `tileentities/fission/TileEntityControlRod.java:225` — "1.21.5: getRenderBoundingBox removed" → "Helper used by the renderer …".
- `tileentities/fission/thorium/TileEntityThoriumCore.java:316` — tank-index banner.
- `tileentities/powergen/TileEntitySteamInjector.java:93` — tank banner.
- `tileentities/powergen/TileEntityTurbineCore.java:714` — tank banner.
- `world/ReactorOreConfig.java:11` — drop "Transcribes … the 1.7.10 ReactorOres enum"; keep the field docs.
- `world/ReactorOreFeature.java:17` — drop "Ports the … 1.7.10 BasicReactorOreGenerator"; keep the vein-scatter description.

## renders/  (10 REMOVE, 16 TRIM)
### REMOVE
- `renders/RenderFusionMarker.java:26-38` — class-doc "26.2 port of the immediate-mode fusion-marker renderer …" (rewrite to behaviour-only, see agent notes).
- `renders/RenderFusionMarker.java:78` — "// Legacy base transform from renderTileEntityFusionMarkerAt (par2/4/6 == 0 in the BER)."
- `renders/RenderFusionMarker.java:114` — "// ---- per-part geometry (legacy LINE_LOOP -> consecutive segments; LINES -> as-is) ----"
- `renders/RenderFusionMarker.java:151` — "// cross spokes (legacy GL_LINES)"
- `renders/RenderFusionMarker.java:190` — "// ---- primitives (mirror RotaryCraft IORenderer) ----"
- `renders/ReactorPipeRenderer.java:41-52` — class-doc "26.2 BER … Port of the legacy DuctRenderer → PipeRenderer.renderLiquid …".
- `renders/ReactorPipeRenderer.java:55` — "// Legacy 1.7 constants — see Reika.RotaryCraft.Renders.PipeRenderer."
- `renders/ReactorLineRenderer.java:28-35` — class-doc "26.2 BER … Port of the legacy RenderWaterLine …".
- `renders/RenderMagnet.java:30-37` — class-doc "26.2 port of the toroid-magnet … Deferred: the legacy renderAngleLine …".
- `renders/item/ReactorMachineItemRenderer.java:65-68` — "// Was a flat 0.5 for every machine … first-pass numbers … expect another round."
### TRIM  (strip "26.2 port of …" / "legacy …" preambles; keep behaviour)
`RenderFusionMarker.java:44-49, 83, 94, 145`; `ReactorPipeRenderer.java:179`; `ReactorLineRenderer.java:42`;
`RenderMagnet.java:65`; `RenderSolenoid.java:26`; `RenderTurbine.java:27, 75` (doc is also **stale** — says
"Drawn static" but the body spins); `RenderCondenser.java:23`; `RenderElectrolyzer.java:23`;
`RenderSolarExchanger.java:23`; `RenderWasteStorage.java:23`; `RenderSteamGrate.java:23`; `RenderControl.java:23`;
`item/ReactorMachineItemRenderer.java:43-53`.

## data/ + auxiliary/  (11 REMOVE, 22 TRIM)
### REMOVE
- `data/ReactorModelProvider.java:179-182` — "// Every other casing part is connectivity-textured in the legacy getTextureIndex table …" (keep only the connected_axis fact).
- `auxiliary/RadiationEffects.java:58-60` — "// DRAGONAPI-PORT: dirtyBombs(CreeperExplodeEvent) — … not ported …".
- `auxiliary/RadiationEffects.java:70-71` — "// DRAGONAPI-PORT: powered-armor decharge … not ported …".
- `auxiliary/RadiationEffects.java:93-95` — "// ITEM-PORT: the hazmat suit pieces … not yet ported/registered …".
- `auxiliary/RadiationEffects.java:167-169` — "// BLOCK-PORT: fluorite irradiation (legacy meta+8 …) …".
- `auxiliary/RadiationEffects.java:171` — "// DRAGONAPI-PORT: Thaumcraft node tainting … gated out …".
- `auxiliary/RadiationEffects.java:239-240` — "// DRAGONAPI-PORT: ReikaItemHelper.isDenseArmor not ported …".
- `auxiliary/RadiationEffects.java:249-250` — "// DRAGONAPI-PORT: createMESystemEffect (AE2 …) gated out …".
- `auxiliary/MobEffectRadiation.java:60-61` — "// DRAGONAPI-PORT: ReikaPlayerAPI.setPlayerWalkSpeed … not ported …".
- `auxiliary/HydrogenExplosion.java:33` — "// Block scatter from the 1.7.10 falling-block loop deferred; …".
- `auxiliary/ReactorControlLayout.java:208` — inline "/*PORT*/" marker before the NBT key string.
### TRIM  (strip "26.2 port of …" / "transcribed from the 1.7.10 …" framing; keep the tables/field docs)
`data/ReactorModelProvider.java` (many: 53, 90, 156, 172, 214, 259, 277, 288, 328);
`data/ReactorWorldGenProvider.java:18, 30`; `data/ReactorBiomeModifierProvider.java:18`;
`data/ReactorLootProvider.java:19`; `data/ReactorMachineRecipeProvider.java:18`;
`data/ReactorRecipeProvider.java:35`; `data/ReactorLang.java:13`; `data/ReactorDataProviders.java:9`;
`auxiliary/MobEffectRadiation.java:25, 52, 67`; `auxiliary/HydrogenExplosion.java:7`;
`auxiliary/SlotNuclearWaste.java:18`; `auxiliary/recipe/ProcessorRecipe.java:34`;
`auxiliary/recipe/CentrifugeRecipe.java:36`.

## container/  (0 REMOVE, 11 TRIM)
Every ported `Menu*.java` opens its class Javadoc with "26.2 port of `ContainerX`." — strip that
sentence, keep the slot-layout / sync description. Three also carry a buried port aside to strip:
`MenuNuclearCore.java:17` ("the old SlotFurnaces"), `MenuPebbleBed.java:18` ("the legacy GUI used"),
`MenuWasteStorage.java:18` ("like the original"). Others: `MenuCentrifuge`, `MenuWasteDecayer`,
`MenuWasteContainer`, `MenuThoriumCore`, `MenuSynthesizer`, `MenuProcessor`, `MenuElectrolyzer`, `MenuCPU`.

## guis/  (0 REMOVE, 11 TRIM)
Same pattern: every ported `Screen*.java` Javadoc starts "26.2 port of `GuiX`." — strip it. Several
also say the fluid-fill sprite is "deferred (26.2 still-sprite gap)" — reword to "not yet drawn; the
tank level still syncs." Files: `ScreenCentrifuge:19,46`, `ScreenThoriumCore:17`, `ScreenSynthesizer:19`,
`ScreenProcessor:19`, `ScreenElectrolyzer:19`, `ScreenCPU:31`, `ScreenNuclearCore:17`, `ScreenWasteDecayer:17`,
`ScreenWasteContainer:17`, `ScreenPebbleBed:17`, `ScreenWasteStorage:17`.

## client/ + entities/  (5 REMOVE, 5 TRIM)
### REMOVE
- `entities/EntityNeutron.java:47-48` — "// MOD-PORT: Botania/ThaumicTinkerer 'platform' transparency blocks …".
- `entities/EntityNeutron.java:49` — "private static final Block botaniaPlatform = null; // MOD-PORT: …".
- `entities/EntityNeutron.java:50` — "private static final Block ttPlatform = null; // MOD-PORT: …".
- `entities/EntityNeutron.java:83` — "// CHROMA-PORT: WorldRift neutron teleport gated out …".
- `entities/EntityNeutron.java:103-104` — "// BLOCK-PORT: fluorite irradiation (legacy meta+8 glow …) …".
  (Note: the two dead `botaniaPlatform`/`ttPlatform = null` fields are themselves dead code — an issue, not just a comment.)
### TRIM
`client/ReactorFluidModels.java:15` ("26.2 removed the texture/tint accessors …"),
`client/ReactorClientExtensions.java:18`, `client/ReactorMachineTooltips.java:36` ("Port of the legacy
ItemReactorPlacer.addInformation"), `entities/RenderNeutron.java:22` ("26.2 port … The legacy renderer …"),
`entities/EntityPlasma.java:98` ("// MOD-PORT: legacy ReactorCraft.fusionDamage …").

## items/  (3 REMOVE, 2 TRIM)
### REMOVE
- `items/ItemIronFinder.java:25` — "// CHROMA-PORT: import reika.chromaticraft.items.tools.ItemAuraPouch;"
- `items/ItemIronFinder.java:26` — "// CHROMA-PORT: import reika.chromaticraft.registry.ChromaItems;"
### TRIM
- `items/ItemCanister.java:14` — drop "The 1.7.10 self crafting-remainder … is a registration/recipe-level concern in 26.2 …".
- `items/ItemRadiationGoggles.java:14` — drop "The 1.7.10 isValidArmor(stack,0,e) … is now the EQUIPPABLE data component".

---

## NOT SWEPT (session limit) — re-run these
- `blocks/` package — sweep interrupted (~23 files) before delivering results.
- `registry/` + `base/` + `api/` packages — sweep interrupted before delivering results.

Known slop already visible in these from other work:
- `blocks/BlockReactorMat.java:73` — "// CoFH IEnergyReceiver gone — push to the block above via the NeoForge transfer-API energy cap." → TRIM.
- `registry/ReactorBlocks.java:113-116` — solenoid-split narration ("was one block with a `part` metadata property") → TRIM.
