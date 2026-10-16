## 2026-10-10 - Bounded terrain-preloader population

DragonAPI now persists finite population scopes shared by ChromatiCraft and GeoStrata. The actual NeoForge 26.3 GenerationTask automatically supplies its square; optional Chunky 1.5.4 hooks observe processed chunks (including skipped FULL chunks) with Chunky's own rounded shape predicate. Completion continues after terrain generation; pause/continue/cancel and interrupted-producer persistence retain queued job cursor/RNG state. /reikapregen provides manual bounded backlog processing and dimension-local status/controls. Supporting chunk tickets cannot admit jobs outside the selected origins.

Both queues now explicitly schedule their entire footprint asynchronously at FULL via the 26.3 addTicketAndLoadWithRadius API, rather than assuming a loading ticket schedules all required FULL tasks. Temporary tickets keep the dimension active, refresh on server ticks and release on loss of admission/shutdown. A shared server hook prevents empty-player pausing while a bounded pass runs. See TestInstance/docs/bounded-pregeneration.md for commands, bounds, restart semantics and the integration API. Tests use disposable levels; the user's Test save has not been populated or edited.

Validation: all 14 overworld GameTests passed with real Chunky NeoForge 1.5.4, all four bounded-population tests passed with Chunky absent, and all 87 DragonAPI unit tests passed. Logs: TestInstance/build/bounded-pregen-validation.log and TestInstance/build/bounded-pregen-no-chunky-validation.log. Fast GameTests service the chunk mailbox and yield to asynchronous workers; production loading stays asynchronous.

## 2026-10-10 — modern worldgen height audit and biome map legends

- Audited active generation in the whole Reika family against the 26.3 Overworld (-64..319). Remapped ReactorCraft deep ore bands, ElectriCraft silver/nickel/platinum and high aluminum, and RotaryCraft optional extra iron; retained ore counts/sizes, biome/dimension restrictions and Nether/End heights.
- Extended ChromatiCraft tiered ore placement and its deepslate host rule, natural caverns, cave indicators/plants, Rainbow underground lights/sparkle, cliff roots, gravel and springs. Extended GeoStrata stony pockets/vines/vents while retaining already-modern rock layers and RF seeds. Deepslate host selection now uses ElectriCraft/vanilla iron variants.
- Added shared dimension-bounded height sampling and focused unit/in-world placement checks. BiomeMapCommand now writes names/RGB codes from the modern registry, distinct ocean/mod colours and exact sampled-biome totals, replacing its empty legacy hierarchy legend.
- Detailed ranges, retained cases, palette diagnosis and validation: `TestInstance/docs/worldgen-height-audit-2026-10-10.md`. Changes affect future generation after restart; the user's saved terrain is not rewritten.

- Validation completed: all 9 affected Overworld GameTests passed in a fresh isolated build; 2 height-boundary and 5 vent-selection unit tests passed.

# ReactorCraft → Minecraft 26.3 / NeoForge port

## TestInstance worldgen census — 2026-10-10

Audited the display-named `worldgen` save read-only across 13,455 FULL Overworld
chunks. ReactorCraft has 1,923,779 surviving ore blocks across all 14 Overworld
block IDs, including all eight fluorite colors. ElectriCraft has 1,002,443 ore
blocks across its five custom types. Nether/End-only ores are outside this save's
generated terrain. Full block, biome, structure, queue and ore-height exports:
`../TestInstance/docs/worldgen-audit-2026-10-10.md`.

Fixed ChromatiCraft Snow outpost foundation supports filling authored rooms;
the isolated natural-dungeon GameTest passes with complete Snow air-cell checks.
The world itself was not edited. See ChromatiCraft/PORTING.md for the defect,
source-parity behavior and queued-generation limitations on measured density.

## RotaryCraft liquid pipe breaking — 2026-10-10

Active liquid pipes inherited machine hardness 4 and bypassed `PIPEHARDNESS`.
`BlockBasicMachine` now applies the synced float setting to mining progress for
`MachineRegistry.isPipe()` blocks, including hoses, fuel lines, valves, bypasses,
separation/suction pipes and bedrock pipes. The existing default of 0 breaks pipes
instantly; nonzero settings are clamped to 0–1 and retain vanilla/NeoForge mining
speed and harvest checks. Reading the setting during mining also honors config
load/sync/reload after block registration. Machine hardness, blast resistance,
loot and fluid behavior are preserved.

Validation: `:RotaryCraft:compileJava --offline` passes against NeoForge
26.3.0.51-beta; in-game breaking has not been exercised.

## Current practices and audit fixes — 2026-10-07

The authoritative target is Minecraft **26.3**, NeoForge **26.3.0.51-beta**, Java 25 and Gradle 9.7.1.
Use the generated/cached 0.51 source jars; `Sources/minecraft` and `Sources/NeoForge` are historical
26.2 references. Current APIs include reloadable recipe/loot registries, Feature interfaces and SDL input codes.
The build allowlist selects the active cluster, including WIP dependencies; a red build is expected during a full port.
Item and fluid automation capabilities are registered in both ReactorCraft and RotaryCraft.

The cross-project audit is tracked in [MODDING-PRACTICES-AUDIT.md](../MODDING-PRACTICES-AUDIT.md).
The shared protocol now binds payloads to channels, bounds payload/array/compressed-NBT allocation,
rejects serverbound sync traffic and validates GUI targets. Remote CPU controls keep their menu/layout checks.
Reactor inventories preserve handler identities during load and apply transactional slot/side/extraction rules.
Persistence uses public ValueInput/ValueOutput codecs while preserving the flat existing save format.
Delta sync includes explicit deletion keys, and absent CPU tags clear their links.
Fuel burnup, magnet charge and named waste identities use typed components; the damage hook remains only
as a compatibility surface. Legacy item data migrates at inventory/entity load boundaries.
Gameplay config is FML SYNCED; legacy `.cfg` options import into typed client/synced TOML specs with originals retained.
Resource duplicates are retired into reference folders and packaging fails on conflicts.

Audit validation: all seven active modules compile; DragonAPI/Rotary/Reactor unit suites pass
40 tests, and the six dedicated-server audit GameTests pass. Rotary/Reactor server datagen and
Reactor client datagen regenerated real recipes, typed component ingredients and translations.
Protocol 2.0.0 requires updated clients and servers. Remote-client visual recipe display and
live remote config reload were not exercised; see the root audit for the exact validation scope.
Rotary inventory subclass loaders now inherit shared old-item migration instead of bypassing it.

The dated sections below preserve historical decisions and observations. Any 26.2 API assumptions,
missing-capability claims, damage-carrier designs, green-only allowlisting, or JEI-unavailability claims
in those sections are superseded by this status and root AGENTS.md. They are historical evidence, not instructions.

## NeoForge 26.3.0.45 bump — 2026-10-06

FML 12 removed `ModConfig.Type.COMMON` (now `LOCAL`) and does not migrate the file name, so
`ReactorCraft` registers `LOCAL` under the existing `reactorcraft-common.toml`. JEI is now
31.9.0.58; the ReactorCraft unit tests load it cleanly. Full breaking-change audit:
`../PORT-26.3-RESEARCH.md`, section "NeoForge 26.3.0.26 → 26.3.0.45 bump".

## VoidMonster sibling port — 2026-10-04

VoidMonster is now included in the **Minecraft 26.3 / NeoForge 26.3.0.26-beta** build and
TestInstance launcher. Its complete active source set lives under `VoidMonster/src/main/java`;
root-level 1.7.10 source remains pristine for parity/reference. Entity/world/client behavior,
registered loot/damage data, native component custom drops, and optional Rotary radar/turret
integration are implemented. Builds/datagen pass, the world-policy checks pass 22/22, and
nine server contracts pass both with and without RotaryCraft. Legacy external mod integrations
remain explicit gates; in-world visual comparison remains outstanding. See
[VoidMonster/PORTING.md](../VoidMonster/PORTING.md) for the accepted boundary, migration
decisions, source-parity notes, resource provenance and exact validation commands.

> **STATUS UPDATE (supersedes the "In progress — TE cluster batch" / "~2527 errors" notes below):**
> the TE cluster is **done** — all 46 concrete TEs + the 10 `base/` classes are on the allowlist and
> `:ReactorCraft:compileJava` + `:test` build **clean** (0 errors). The remaining work is faithfulness/
> capability gaps, not compilation. See `docs/issues/` (per-area audits) and especially
> `docs/issues/te-cluster.md`. Known shared-with-RotaryCraft deferrals (do NOT fix in ReactorCraft
> alone): sided item-IO not enforced by the item capability, and no external `Capabilities.Fluid.BLOCK`
> — both mirror `RotaryBlockEntities` and need a shared DragonAPI change.

Status board + conventions for porting ReactorCraft (Reika's original 1.7.10 source) to
NeoForge 26.2, matching the already-ported siblings (DragonAPI, RotaryCraft, ElectriCraft,
GeoStrata). **RotaryCraft is the Rosetta Stone** — for any subsystem here there is almost
always a compiling RC/DragonAPI equivalent. Open it and match it; do not reinvent.

The root parent repo's `STATUS.md` documents the 26.2 API patterns (submit pipeline,
GuiGraphics, ColorCollection, EntityTypes, etc.) — read it before touching rendering/GUI.

## Hard rules (from the user, standing)

- **No stubs.** A file is either pristine 1.7.10 (untouched) or fully ported. Never leave a
file mechanically half-converted — it destroys the done/not-done signal and we lose track.
- **Recipes are data-driven.** Do NOT port `ReactorRecipes.java` (34 KB) as runtime code.
Re-express every recipe as a datagen provider (see RotaryCraft `data/` + `RotaryRecipes`).
Use the ORIGINAL recipes from upstream as the source of truth — never invent recipes.
- **Use sources.** Original 1.7.10 is `git show upstream/master:<Path>` here is not set up
(origin = ReikaKalseki). Read original via the working tree (it IS the original until ported)
or `git show HEAD:<path>`. Reference RC port for the target shape.
- **Concrete colour identities.** When a V33a metadata family represented separately obtainable colour variants, register one stable block/item id per colour in 26.2; never recreate metadata as an integer/property on one block. ChromatiCraft cave crystals/dye trees and GeoStrata luminous crystals establish the rule for all future integrations.
- No AI-slop comments narrating what changed.

## Mechanical state already done (this scaffolding pass)

- Added as submodule (origin = ReikaKalseki/ReactorCraft), wired into root `settings.gradle`.
- 272 `.java` moved flat-root → `src/main/java/reika/reactorcraft/...`; `package`/`import`
`Reika.* → reika.*` lowercased (class names preserved). Sources still use the 1.7.10 API.
- Resources → `src/main/resources/assets/reactorcraft/{textures,sounds,resources}`.
- `build.gradle`, `gradle.properties`, `src/main/templates/META-INF/neoforge.mods.toml`
(deps: dragonapi + rotarycraft). Mirrors ElectriCraft.
- ChromatiCraft imports neutralized (commented `// CHROMA-PORT:`); usage sites handled
per-file when that file is ported (only 10 files touch it).

## Dependency order (port in this sequence — can't build a thing before its base)

1. `ReactorCraft.java` (main, @Mod) + `ReactorConfig` → mirror `RotaryCraft.java`
2. `registry/` — DeferredRegisters + the enum registries. `**ReactorTiles` ↔ RC `MachineRegistry`**
  (implements `reika.dragonapi.interfaces.registry.TileEnum`). `ReactorBlocks`/`ReactorItems`/
   `ReactorEntities`/`ReactorSounds` ↔ RC equivalents.
3. `base/` (17) — the TE/block/item/render/gui base hierarchy (see reuse map below)
4. `blocks/` (13) + `blocks/multi/` (6) + `items/` (19)
5. `tileentities/**` (47 total across fission/fusion/htgr/powergen/processing/waste)
6. `container/` (10) → Menu; `guis/` (13) → AbstractContainerScreen (submit pipeline)
7. `renders/` (26) + `models/` (21) → submit pipeline; atlas sizes per memory
8. `world/` (2) + `entities/` (9) + `event/` (2) + `auxiliary/**` (49) as pulled in
9. **Recipes LAST**, as datagen. `nei/` (5) → JEI later (JEI has no 26.2 build yet — gate out).

## Base-class reuse map (verified against ported DragonAPI/RC)


| ReactorCraft base (1.7.10)                                           | Re-root onto                                                         | Notes                                                                                                                                      |
| -------------------------------------------------------------------- | -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| `TileEntityReactorBase extends TileEntityRegistryBase<ReactorTiles>` | DragonAPI `base/BlockEntityBase`                                     | `TileEntityRegistryBase` does NOT exist in port; RC's `RotaryCraftBlockEntity extends BlockEntityBase`. Key off `ReactorTiles` (TileEnum). |
| `ReactorRenderBase extends TileEntityRenderBase`                     | DragonAPI `base/BlockEntityRenderBase`                               | mirror RC `RotaryTERenderer`; submit() pattern                                                                                             |
| `ReactorItemBase extends Item implements IndexedItemSprites`         | RC item base / plain `Item`                                          | `IndexedItemSprites` not located — find RC item-icon convention or drop                                                                    |
| `ReactorGuiBase extends GuiContainer`                                | `AbstractContainerScreen`                                            | GuiGraphics drawing (STATUS.md)                                                                                                            |
| `ReactorStructureBase extends StructureBase`                         | DragonAPI `instantiable/data/blockstruct/*` (`MultiBlockBlueprint`?) | locate exact base                                                                                                                          |
| `BlockReCMultiBlock extends BlockMultiBlock<Boolean>`                | locate DragonAPI multiblock base                                     | not found by name — may be renamed                                                                                                         |
| interfaces `RenderFetcher`, `Transducerable`                         | locate in DragonAPI                                                  | not found by name — verify/port                                                                                                            |


`TileEnum` and `BlockEntityBase` confirmed present. Others marked "locate" need a grep pass
(may have been renamed during the DragonAPI port) or a small port of the missing base.

## Historical cluster investigation (superseded strategy): no thin slice exists — the TE layer is a monolithic knot

Investigated and confirmed: `TileEntityReactorBase` (the fat god-base, 361 lines) does
`this instanceof TileEntityReactorBoiler/SolenoidMagnet/ReactorGenerator/TurbineCore/SteamLine/ HeatPipe` inside its own heat-conduction/transducer methods, so it cannot compile without those
6 concrete TEs. And `ReactorTiles` (the registry enum, `extends TileEnum`) references **every**
machine TE. So base ⇄ registry ⇄ all-TEs are mutually dependent — you cannot compile one machine
without effectively the whole `base/` + `tileentities/`** + registry + `entities/EntityNeutron`

- the typed interfaces (`ReactorTyped`/`Temperatured`/`TypedReactorCoreTE`) cluster.

**Implication:** port that cluster as one focused batch, not file-by-file-green. Recommended:

1. Resolve the cross-cutting bases/interfaces first: re-root `TileEntityReactorBase` onto
  `BlockEntityBase`; confirm port locations of `RenderFetcher`, `Transducerable`, `ShaftMachine`,
   `ThermalMachine`, `TemperatureTE`, `Variables` (RC api / `interfaces.blockentity`).
2. Gate OpenComputers (`li.cil.oc.*`, e.g. `Visibility` in `getTransducerVisibility`) the same way
  as ChromatiCraft — it's an optional dep not in this build. Use `@ModDependent`/strip or remove.
3. Python-assisted mechanical bulk across the cluster: `World→Level`, `NBTTagCompound→CompoundTag`,
  `ForgeDirection→Direction`, `MathHelper→Mth`, `(x,y,z)→BlockPos`, the `interfaces.tileentity→  interfaces.blockentity` remap — THEN manual fixup of method semantics against RC TEs.
4. Files are edited in place but **added to the build `include` allowlist only once the whole
  batch compiles** — so the build stays green throughout (unported/in-progress files are simply
   excluded). The allowlist remains the "verified" set; `git diff` shows in-progress edits.
5. Recipes/datagen and renders come after the TE layer is green.

This batch is a large, sustained effort with design decisions — best done deliberately (and after
committing the current clean, green foundation), not squeezed into autonomous ticks.

## Python's role (AFTER the slice validates the rules)

Once one machine compiles, the exact find/replace rules (vanilla type/package renames,
DragonAPI helper signatures) are known and safe. Apply them **incrementally, package by
package, spot-checkable** — never a single blind 272-file sweep (leaves imports modern but
bodies 1.7.10 → nothing compiles AND trackability is lost).

## Verified-increment workflow (IN USE)

`ReactorCraft/build.gradle` has a `sourceSets.main.java { include ... }` allowlist. The module
compiles ONLY listed (fully-ported) files, so `./gradlew :ReactorCraft:compileJava` stays green
while the other ~270 files remain pristine 1.7.10. **The include list is the done-tracker** — add
a file the instant it is ported, rebuild to verify. Delete the whole block when the mod is done.

### Ported & compile-verified

- `api/NeutronShield.java` — pure interface; `World`+`(x,y,z)` → `Level`+`BlockPos`.
- `auxiliary/ReactorBlock.java` — empty marker interface (no edit needed).
- `auxiliary/SteamTile.java` — `int getSteam()` (no edit needed).
- `auxiliary/MultiBlockTile.java` — extends DragonAPI `BreakAction`; import remap (see below).
- `auxiliary/ReactorPowerReceiver.java` — extends RC `ShaftPowerReceiver` (no edit needed).

### Content slice (26.2 DeferredRegister layer — compile-verified)

- `ReactorCraft.java` (minimal @Mod), `registry/ReactorDataComponents` (CANISTER_FLUID via
  `SimpleFluidContent`, FUEL_BURNUP, WASTE_ISOTOPE — `createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID)`),
  `registry/{ReactorItems,ReactorBlocks}` (ThreadLocal `setId` pattern, per-variant DeferredItems),
  `registry/{FluoriteTypes,CraftingItems,ReactorOreType}` (modern identity enums),
  `registry/{ReactorRecipeTypes,ReactorRecipeSerializers}` + `auxiliary/recipe/{ProcessorRecipe,CentrifugeRecipe}`
  (Codec/StreamCodec), `registry/ReactorTabs`.
- `registry/ReactorFluids.java` — **complete ReactorCraft-owned fluid set (24)**, transcribed verbatim
  from the 1.7.10 `new Fluid("rc ...")` block (density/viscosity/temperature/luminosity preserved):
  heavy_water, hydrofluoric_acid, uranium_hexafluoride, ammonia, sodium, chlorine, oxygen, liquid_oxygen,
  low_pressure_ammonia, low_pressure_water, hot_sodium, warm_sodium, deuterium, tritium, carbon_dioxide,
  hot_carbon_dioxide, fusion_plasma, corium, nuclear_waste, lithium, lifbe, lifbe_fuel, lifbe_fuel_preheat,
  hot_lifbe. RotaryCraft-owned fluids (lubricant, liquid nitrogen, steam) are NOT registered here — use
  `RotaryFluids`. Each fluid = `FluidType` + one `BaseFlowingFluid.Source` with flowing wired to the
  source (avoids the `BaseFlowingFluid#isSame` NPE). The 1.7.10 `setGaseous` flag has no 26.2 Properties
  setter; the gaseous nature survives via the original (often low/negative) density.
- `client/ReactorFluidModels.java` — 26.2 removed the texture/tint accessors from
  `IClientFluidTypeExtensions`; still/flow sprites + tint now ride a `FluidModel.Unbaked` registered via
  `RegisterClientExtensionsEvent`'s sibling `RegisterFluidModelsEvent`. Every fluid reuses the vanilla
  `minecraft:block/water_{still,flow}` sprites (no binary authoring) with a distinct
  `FluidTintSources.constant(ARGB)` colour faithful to its nature, so all 24 render.

### Worldgen + datagen slice (compile + datagen + test verified, this session)

Independent of the TE knot (datapack/registry-only), so allowlisted and green now:

- `world/ReactorOreConfig.java` + `world/ReactorOreFeature.java` + `registry/ReactorFeatures.java` —
  one `Feature<ReactorOreConfig>` ports the 1.7.10 `BasicReactorOreGenerator` vein scatter (ellipsoidal
  `WorldGenMinable` blob, per-vein fluorite colour roll, ammonium lava-adjacency). Dimension base-stone
  replaceable sets via `dimType` (overworld/nether/end). Biome restriction lives in the biome modifiers.
- `data/ReactorWorldGenProvider.java` — `RegistrySetBuilder` emitting one configured + placed feature
  per `ReactorOreType`, with the 26.2 Y-band remap (indium/thorium dropped below 0, magnetite up the
  taller mountains). `data/ReactorBiomeModifierProvider.java` — literal `neoforge:add_features` JSON
  (biome tags don't resolve in `RegistrySetBuilder`): pitchblende → oceans/rivers, ammonium → nether,
  endblende → end, rest → overworld, all at `UNDERGROUND_ORES`.
- `data/ReactorLang.java` (prettified en_us for every block/item), `data/ReactorModelProvider.java`
  (reflective `BlockModelGenerators` sinks → `cube_all` + flat items, GeoStrata/RC pattern),
  `data/ReactorLootProvider.java` (faithful `getOreDrop`: material-droppers + fluorite drop product,
  smeltables drop self).
- `data/ReactorRecipeProvider.java` (`RecipeProvider.Runner`) — smelting (every ore → product, fluorite
  → gem, calcite → lime), crafting (tank, canister part, control rod, empty canister, fuel-rod/depleted
  2×2 compaction), and **cross-mod RotaryCraft-typed recipes emitted under the `reactorcraft` namespace**
  (Cd+In+Ag `ShapelessBlastFurnaceRecipe`, coal-dust `FrictionHeaterRecipe`, uranium/emerald
  `GrinderRecipe`) — keeps the dependency direction correct (RC has no ReactorCraft dep).
- `data/ReactorMachineRecipeProvider.java` — the `ProcessorRecipe`/`CentrifugeRecipe` UF6 chemistry as
  **literal JSON** matching the codec field layout, because constructing a `FluidStack` during datagen
  trips "Components not bound yet" (the fluid-stack analogue of the `ItemStackTemplate` rule). Values
  transcribed verbatim from `TileEntityUProcessor.Processes.UF6` / `TileEntityCentrifuge.Centrifuging.UF6`.
- `data/ReactorDataProviders.java` — `GatherDataEvent.Client` (lang, models) + `.Server` (loot, worldgen
  registry set, biome modifiers, recipes, machine recipes).
- `src/test/.../RegistryIntegrityTest.java` (+ `neoForge.unitTest` harness in build.gradle) — asserts all
  24 fluids register as distinct sources with bound distinct `FluidType`s, the processor/centrifuge recipe
  types + serializers are registered and distinct, every `ReactorOreType` resolves block+product with a
  sane Y-band, and the 8 fluorite colours have distinct ore blocks + gems. **4 tests green.**

Verified: `:ReactorCraft:compileJava` + `:RotaryCraft:compileJava`, `:ReactorCraft:runClientData` +
`:ReactorCraft:runServerData`, and `:ReactorCraft:test` all BUILD SUCCESSFUL.

### In progress — TE cluster batch (this session; allowlist expanded, compile NOT green yet)

**Registry / blocks layer (new, mirrors RotaryCraft):**
- `registry/ReactorTiles.java` — rewritten: 46 enum constants, each → one `ReactorBlocks` machine
  `DeferredBlock`, `TileEnum` with `getMachine(Level,BlockPos)` / `getMachineMapping(Block)`, no metadata.
- `registry/ReactorBlockEntities.java` — generated: one `BlockEntityType` per machine + capability
  registration (`Item` via `HasItemHandler`, `Fluid` via `TileEntityTankedReactorMachine`).
- `registry/ReactorBlocks.java` — 46 machine blocks (`BlockReactorMachine` / `Modelled` / `Duct` / `Line`).
- `blocks/BlockReactorMachine.java` (+ Modelled, Duct, Line) — `BlockTEBase` + `MachineRegistryBlock`.
- `ReactorCraft.java` — wires `ReactorBlockEntities`, `ReactorEntities`, radiation `MobEffect`, capabilities,
  `ReactorTiles.loadMappings()`, `isLocked()` → false.

**Mechanical bulk (110 files remapped via `scripts/te_cluster_remap.py`; 46 TE ctors via `add_te_constructors.py`):**
- Package remaps, `ForgeDirection`→`Direction`, `World`→`Level`, stripped `RenderFetcher`/`IHasWork`/Waila/OC/FML.
- **Known bad remap:** blind `inv[`→`itemHandler.getStackInSlot(` left syntax errors in some files; fixed partially
  via `fix_inv_remap.py` + `fix_metadata.py`. Residual `inv`/`getSizeInventory`/`FluidRegistry`/`tank.getLevel()`
  calls remain across TE bodies.

**Support registries ported/stubbed for cluster:**
- `WorkingFluid`, `ReactorFuel`, `ReactorOptions` (defaults), `ReactorAchievements` (no-op triggers),
  `ReactorFluids.getLegacyFluid(String)`, `ReactorItems` `ItemRef` compat layer, `ReactorStacks`, `RadiationShield`,
  `HydrogenExplosion` (delegates to `Level.explode`).

**Allowlist:** curated TE batch ≈ **100** `include` lines in `build.gradle` (base + 46 TEs + registry + blocks +
  minimal auxiliary). Was briefly 189 (whole `auxiliary/` — pulled back; many files still 1.7.10).

**Compile status:** `:ReactorCraft:compileJava` **FAILED** (~2527 errors after fix pass 2; was ~2600).
Pass 2 (`scripts/te_cluster_fix_pass2.py`, 37 files): repaired bad `getLegacyFluid /*PORT*/` syntax,
HybridTank `readFromNBT`/`writeToNBT`, tank `getFluidLevel()`, `getContainerSize()`, WorldLocation/Coordinate
`load()`, ReactorControlLayout NBT helpers, HydrogenExplosion import. Residual bulk: old fluid-handler
signatures (`drain(int,boolean)`, `FluidTankInfo`), int-coord `getBlockEntity`, missing DragonAPI types
(`Coordinate`, `TileEntityCache`), `ReactorTiles.getTE` arity, TEs still extending raw `BlockEntity`.

**Next to green (ordered):**
1. Finish `TileEntityNuclearCore` + `TileEntityReactorPiping` body fixups (already in allowlist, partially ported).
2. Systematic TE pass: `getSizeInventory`→`getContainerSize`, `tank.getLevel()`→`getFluidLevel()`, coords→`BlockPos`,
   `ReactorFluids` instead of `FluidRegistry`, finish `inv`→`itemHandler` migration.
3. `TileEntityUProcessor` + `TileEntityCentrifuge` → `RecipeManager` lookup (`ProcessorRecipe`/`CentrifugeRecipe`).
4. Wire `EntityNeutron` + finish sibling entities; menus deferred; BER deferred.
5. Re-expand allowlist only when compile green.

**Previously (still valid):**
- `base/TileEntityReactorBase.java` — **re-rooted** `TileEntityRegistryBase<ReactorTiles>` →
`BlockEntityBase implements Transducerable`. Removed `RenderFetcher`/`getRenderer` (gone — render
via registered BER), `shouldRenderInPass` (no render passes), `writeToNBT/readFromNBT` (use
save/load + sync tags), `canUpdate` → `shouldRunUpdateCode`. `ForgeDirection`→`Direction`
(`offsetX`→`getStepX`, inherit base `dirs`). OC `getOCNetworkVisibility` removed (OC not in build).
Bodies ported to verified sigs: `ReikaWorldHelper`/`ReikaBlockHelper` now in `libraries.level`;
`getAmbientTemperatureAt(Level,BlockPos[,float])`, `temperatureEnvironment(Level,BlockPos,int)`,
`getBlockVolume(Level,BlockPos)`; `setBlockToAir`→`removeBlock`; `createExplosion`→
`explode(...,Level.ExplosionInteraction.BLOCK)`; `world.provider.dimensionId!=-1`→
`dimension()!=Level.NETHER`; `Transducerable.getMessages(Level,BlockPos,Direction)`.
- `base/TileEntityInventoriedReactorBase.java` — `ISidedInventory` → `Container, HasItemHandler`
backed by `ManagedItemHandler` (RC `InventoriedRCBlockEntity` pattern). Full `Container` contract
implemented via the handler; NBT via `ValueOutput/Input` + `itemHandler.serialize/deserialize`.
**Contract for subclasses (cores/waste):** implement `getContainerSize()` (was `getSizeInventory`),
`isItemValidForSlot(int,ItemStack)`, and the sided hooks `canItemEnterFromSide(Direction)` /
`canItemExitToSide(Direction)` / `canRemoveItem(int,ItemStack)` (was `ForgeDirection`). Sided I/O
must be wired via the block's item-capability wrapper using those hooks (follow-up).
- `base/` subclasses ported (B5–B9): `TileEntityInventoriedReactorBase` (Container),
`TileEntityTankedReactorMachine` (NeoForge IFluidHandler+PipeConnector), `TankedReactorPowerReceiver`,
`TileEntityNuclearBoiler`, `TileEntityIntermediateBoiler` (2-tank), `TileEntityWasteUnit`,
`TileEntityLine`. **Remaining base/: `TileEntityNuclearCore` (492 lines), `TileEntityReactorPiping`
(461)** — the two big multiblock/pipe bases, next.
- More contracts established: `ReactorTiles.getMachineFromBlock(Block)` (no metadata); `ReactorItems`
registry getter `getItemInstance()`; `ReactorItems.WASTE` carries the isotope index via
`ItemStack.getDamageValue()` (faithful to the 1.7.10 metadata scheme — revisit as a data component
if needed); `TileEntityLine.getTexture()` returns `ResourceLocation`; `EntityNeutron(Level,BlockPos, Direction,NeutronType)` ctor; `Proportionality` has no NBT methods (serialize manually).

**Contract this base now imposes on downstream cluster files (make them conform when porting):**

- `ReactorTiles` (enum impl `TileEnum`): `static getTE(Level,BlockPos)`, `allowTickAcceleration()`,
`isReactorCore()`, constants `CONTROL/CPU/EXCHANGER/REFLECTOR`. `getBlockState()` from TileEnum.
- `ReactorType`: `HTGR`, `FUSION`, `getTypeMismatchHeatEfficiency()`.
- `TileEntityReactorPiping`: **rename `getLevel()`→`getFluidLevel()`** (clashes with
`BlockEntity.getLevel()`); `getFluidType()` returns a `net.minecraft...material.Fluid`.
- `Temperatured`: `getTemperature()/setTemperature(int)`; `TypedReactorCoreTE.getReactorType()`.
- Concrete TEs keep their display getters (TurbineCore `getPower/getOmega/getName/getLubricant`,
SteamLine `getSteam/getSourceReactorType:Proportionality<ReactorType>`, HeatPipe `getNetHeatEnergy`,
ReactorGenerator `getGeneratedOutputForDisplay`).
- Systematic remap also seen here: `**reika.dragonapi.libraries.world.* → libraries.level.***`.

### Deferred — need datagen / coupled (NOT leaves)

- `auxiliary/PoisonGasDamage.java`, `auxiliary/RadiationDamage.java` — extend DragonAPI
`CustomStringDamageSource`, whose ctor is now `(Holder<DamageType>, String)`; the old overridable
`DamageSource` booleans (`isFireDamage`/`isProjectile`/`getHungerDamage`/`setDamageBypassesArmor`…)
are gone (data-driven `DamageType`). Port these alongside `DamageType` JSON datagen — mirror how
RotaryCraft built its `MachineDamage` damage sources.

### FIXED: nested-class import lowercasing (from the reorg script)

The lowercase pass wrongly lowercased the *container* of nested-class imports (it only preserved
the final segment's case). E.g. `EntityNeutron.NeutronType` became `entityneutron.NeutronType`.
A resolver pass against the real files fixed all 12 `reika.reactorcraft.`* cases (incl.
`TileEntityWaterCell.LiquidStates`, `RadiationHandler.RadiationLevel`,
`RadiationEffects.RadiationIntensity`, `TileEntityToroidMagnet.Aim`, etc.). **The same bug may
exist in `reika.dragonapi.*` / `reika.rotarycraft.*` nested-class imports** — not auto-fixed
(can't safely resolve across the relocated sibling modules); each surfaces as an obvious
compile error when its owning file is ported. Fix in place then.

### Systematic import remaps (DragonAPI port relocations)

DragonAPI's `interfaces/tileentity/` package is GONE — wholesale-moved to `interfaces/blockentity/`.
Remap **applied to the cluster** for classes that exist there: `BreakAction`, `ChunkLoadingTile`,
`ToggleTile`, `InertIInv`, `NonIFluidTank`, `ThermalTile`. Non-cluster batches still hold the old
path (pristine) — remap them when ported.

### KEY BLOCKER: RenderFetcher / TextureFetcher removed in the port (26 TEs)

`reika.dragonapi.interfaces.tileentity.RenderFetcher` and `TextureFetcher` have **no equivalent**
in the ported DragonAPI (the 26.2 render overhaul removed the fetcher pattern). 26 cluster TEs
(`TileEntityReactorBase implements RenderFetcher`, etc.) depend on it. Needs a porting decision,
not a remap: in 26.2 machines render via a registered `BlockEntityRenderer` (see RC `RotaryTERenderer`

- the submit pipeline) rather than the TE handing back a render object. Resolve this before the
TE-cluster can compile. `Transducerable` is fine at `reika.rotarycraft.api.interfaces.Transducerable`.

### api/ leftovers (deliberately NOT ported yet — coupled, not leaf)

- `api/MagneticOreOverride.java` — returns `IIcon[]` (1.7.10 render concept; no 1:1 target).
Port alongside its consumer's renderer (TextureAtlasSprite/ResourceLocation model).
- `api/RadiationHandler.java` — reflection bridge into `auxiliary/RadiationEffects`; its reflected
method signatures (and the hardcoded `"Reika.ReactorCraft..."` class-name strings) only settle
once `RadiationEffects` is ported. Do it with that file.

## ~~BLOCKER~~ RESOLVED: DragonAPI `ParticleEntity` ported (DragonAPI commit, green)

DragonAPI now has `ParticleEntity` (+ an `InertEntity(EntityType<?>,Level)` ctor). The entity layer
is unblocked. Next: port `EntityNeutron` (ctor `(EntityType<? extends Entity>,Level)` for registration

- `(Level,BlockPos,Direction,NeutronType)` gameplay; block-interaction API `isOpaqueCube`→
`isSolidRender`/`getExplosionResistance`/`getLightOpacity`→`getLightBlock`/`hasTileEntity`→
`getBlockEntity!=null`; gate WorldRift; FML `GameRegistry.findBlock`→registry lookup), register it in
`ReactorEntities` (RotaryEntities pattern), then `EntityPlasma`/`EntityRadiation`/`EntityFusion`.
Original blocker analysis (kept for reference):

### (was) DragonAPI `ParticleEntity` not ported

`entities/EntityNeutron` and `entities/EntityPlasma` extend `reika.dragonapi.base.ParticleEntity`,
which **does not exist in the ported DragonAPI** (only `InertEntity` was ported, and it's minimal —
TODO stubs, uses `EntityTypes.ARROW`, doesn't implement `Entity`'s `defineSynchedData`/
`add|readAdditionalSaveData`). Original is `git show upstream/master:Base/ParticleEntity.java` in
the DragonAPI submodule (a moving projectile-style entity: motion via `motionX/Y/Z`, per-block
`onEnterBlock` collision, `IEntityAdditionalSpawnData`, despawn-over-time/distance).

Consequence: `EntityNeutron` (→ nested `NeutronType`) blocks `ReactorType` → `ReactorTiles` → all 47
concrete TEs. So the TE cluster cannot compile until DragonAPI gets `ParticleEntity` (a cross-module
port: 1.7.10 Entity API → modern — `motionX/Y/Z`→`setDeltaMovement`, `onUpdate`→`tick`,
`setDead`→`discard`, `IEntityAdditionalSpawnData`→`IEntityWithComplexSpawn`, NBT→ValueInput/Output,
`Coordinate`→`WorldLocation`). `InertEntity` likely needs hardening too.

**Template + plan (this is tractable):** RotaryCraft `entities/EntityDischarge` is the proven
ported pattern — `extends Entity implements IEntityWithComplexSpawn`; `defineSynchedData(Builder)`,
`read/addAdditionalSaveData(ValueInput/Output)`, `writeSpawnData/readSpawnData(RegistryFriendlyByteBuf)`;
registered via `RotaryEntities` `DeferredRegister<EntityType<?>>` + `EntityType.Builder.of(Ctor, MobCategory.MISC).build(ResourceKey)` with ctor `(EntityType<? extends Entity>, Level)`. Entity-layer
sub-project order: (1) harden DragonAPI `InertEntity` — add `InertEntity(EntityType<?>, Level)` ctor
(it currently hardcodes `EntityTypes.ARROW`, so every subclass mis-registers); (2) port
`ParticleEntity` onto it (motion→`setDeltaMovement`, `onUpdate`→`tick`, `onEnterBlock(Level,BlockPos)`,
spawnLocation via `WorldLocation` for NBT + raw ints for the spawn buffer; drop the `CubeDirections`
ctor — not ported); (3) port `EntityNeutron`/`EntityPlasma`/`EntityRadiation`/`EntityFusion`
(block-interaction API: `isOpaqueCube`→`isSolidRender`, `getExplosionResistance`,
`getLightOpacity`→`getLightBlock`, `hasTileEntity`→`getBlockEntity != null`); (4) register them in
`ReactorEntities`; (5) renders later. Then `ReactorType`→`ReactorTiles`→TEs unblock.

**Unblocked alternative work in ReactorCraft** (no ParticleEntity dependency): the item layer
(`ReactorItemBase`, `ItemReactorTool`, `items/`**), `registry/ReactorItems`/`ReactorBlocks` for
non-TE blocks, and auxiliary support that only touches `InertEntity`-based `EntityRadiation`.

## Content layer (the remaining bulk) + the item-variant decision

The entity/radiation subsystem + TE base hierarchy are done. What remains is the
content/registration layer, all rooted at the `**ReactorCraft` main class** (everything
references `MODID`/`radiation`/`fusionDamage`/`instance`), so the cluster can't enter the
allowlist until a large connected chunk lands together:

- `ReactorCraft` main (35 KB @Mod) — bus-register the DeferredRegisters (incl. `ReactorEntities.ENTITIES`),
define the `radiation` MobEffect (Holder) + `fusionDamage` DamageSource + `MODID`/`instance`.
- registries: `ReactorItems`, `ReactorBlocks`, `ReactorTiles` (TE registry → BlockEntityType), plus
`ReactorOptions` (config), `ReactorSounds`, `ReactorAchievements`, `ReactorFuel`, `RadiationShield`,
`FluoriteTypes`, `WorkingFluid` — all are the 1.7.10 DragonAPI enum-registry style and must be
**rewritten to DeferredRegister** (like `ReactorEntities` was).
- the 47 concrete TEs under `tileentities/`** (now have a ported base hierarchy to extend).
- blocks/, items/, container/→Menu, guis/→Screen, renders/ (submit), recipes → **datagen**.

### DECISION — item metadata variants → data-component refactor

The RC port registers ONE `DeferredItem` per item; there is no `getStackOfMetadata`. ReactorCraft's
metadata-variant items (FUEL burnup, PLUTONIUM stages, **WASTE ≈1000 isotopes**, fluorite colours)
**carry the variant in `ItemStack.getDamageValue()/setDamageValue()`** (single registered item +
int variant). Already used this way in `TileEntityWasteUnit` + `EntityNeutron` (committed). Rationale: ~1000 isotopes can't be separate items; a data-component is cleaner.

## ChromatiCraft (optional integration, 10 files)

Imports neutralized now. Usage sites (mostly `WorldRift`, `ChromatiAPI`, adjacency-upgrade,
`CrystalElement`) are gated/removed when each owning file is ported. ChromatiCraft is not in
this build (commented in `settings.gradle`).
## Shared DragonAPI seam update — 2026-07-24

DragonAPI now has a real NeoForge 26.2 `ChunkManager` backed by a persistent scoped
`TicketController`. It validates block-entity ticket owners on reload, reconciles exact requested
chunk sets, and supports load/unload plus the original block-coordinate `getChunkSquare` API.
ReactorCraft's existing `CHUNKLOAD-PORT` sites are therefore no longer blocked on a missing manager;
when their owning tile files are next accepted, restore the preserved calls and verify their
activity/removal lifecycle rather than leaving the comments dormant.

## Shared ChromatiCraft tile-persistence seam update — 2026-07-24

ChromatiCraft's modern generic tile block now restores the old `NBTTile` placement/drop contract:
custom data is applied after placer assignment, copied back onto loot drops, and owner-gated tiles reject
unauthorized mining. The new pylon Power Crystal uses that path for its multi-owner UUID set. This is
a ChromatiCraft-local implementation, but it confirms the 26.2 `CUSTOM_DATA` + loot-context pattern to
reuse when ReactorCraft ports any equivalent owner/configuration-bearing block entities; do not lose
machine data by relying on a plain `dropSelf` table alone.

## Shared ChromatiCraft structure/link seam update — 2026-07-25

ChromatiCraft's active pylon, repeater, compound-repeater, and pylon-broadcast multiblocks now use
canonical generated Minecraft structure NBT; runtime `FilledBlockArray` matching is an adapter derived
from those templates. The broadcast template records not-yet-registered chroma-fluid cells as
`structure_void` and overlays exact registry-ID checks at runtime, preserving the dependency without
inventing a substitute block or allowing a false-positive match.
Use the same NBT-first approach for ReactorCraft structures or monuments instead of rebuilding geometry
in Java. ChromatiCraft also now proves a server-global codec-backed `SavedData` web keyed by owner and
colour, with durable resource-key `WorldLocation` nodes, chunk-loaded link tiles, and focused donation /
throughput GameTests. This is the current reference for any ReactorCraft cross-dimension linked-machine data.

## Shared ChromatiCraft dynamic-drop/datagen seam update — 2026-07-26

ChromatiCraft's NBT-backed encrusted crystal proves the 26.2 pattern for blocks whose synchronized
block-entity state determines drops: register an explicit empty loot table so validation remains
data-driven, then emit the authoritative per-face, growth-scaled items from the removal callback.
Its sixteen shard sprites were mechanically cropped from the V33a item sheet and its names/models are
datagen-owned. Reuse this split for ReactorCraft machines whose persisted state cannot be represented by
a static loot table.

## Shared ChromatiCraft server-behavior checkpoint — 2026-07-26

The active pylon now proves two more 26.2 translations of V33a world behavior: colour-specific combat
effects are delegated to the already-ported `CrystalPotionController`, and anti-capture rejection checks
all 26 neighbouring cells before clearing the shell without drops, applying entity momentum/fall state,
disabling player flight, seeding fires, and damaging its booster set. The combined suite is green at 35
required tests. ReactorCraft ports should likewise keep gameplay effects in their owning controller and
keep audiovisual payloads clientbound without weakening server authority.

## Shared ChromatiCraft payload/render/casting seam — 2026-07-26

ChromatiCraft now supplies the reference 26.2 split for server-authoritative machine effects: typed
clientbound payload records, radius/player distribution, client particle handling, and a registered
submit-pipeline entity renderer. Its six active network blocks also pass client model datagen.

The casting-table work establishes the modern structure rule for machine upgrade tiers: each tier is
generated as canonical Minecraft structure NBT, while Java adds only alternative matches a single NBT
palette cannot encode. The focused contract now round-trips all three tiers and their alternative
matches. Use the same template-first and managed-container persistence seam here.

## Shared ChromatiCraft casting-recipe/container seam — 2026-07-26

ChromatiCraft now proves the complete 26.2 custom-recipe registration path: a typed recipe/input,
map and network codecs, DeferredRegister recipe type/serializer, registry-aware server datagen, and
exact physical matching. The first 24 V33a CrystalStone recipes are generated as datapack JSON rather
than runtime maps. Its owner-bound casting stand also demonstrates a modern inert one-slot
`WorldlyContainer`, `ValueInput`/`ValueOutput` persistence, `CUSTOM_DATA` owner round-trip, dynamic
contents drops, and server-authoritative interaction/spread behavior. The combined suite is green at
40/40. Reuse these patterns for ReactorCraft custom machine recipes and owner/configuration-bearing
auxiliary inventories; do not regress to hardcoded recipe tables or item metadata.

## Shared ChromatiCraft atomic-controller/receiver seam — 2026-07-26

ChromatiCraft's casting table now supplies the complete machine-controller companion to that recipe
and container seam. A registered ten-slot receiver selects data-driven recipes by physical tier,
validates NBT-backed structures, links and locks external inventories, waits a declared duration,
requests missing network energy, and commits inputs, crafting remainders, aura, output, XP,
progression, and history atomically. Active work and history use modern `ValueInput`/`ValueOutput`
persistence, dynamic inventory drops are retained, and all visual/data assets remain datagen-owned.
The focused GameTest proves that no inventory state changes before the final tick.

The shared crystal receiver also now caps delivery against its current stored value, returns the exact
accepted amount, avoids mutating request tags, reports compound-request results correctly, and uses the
modern adjacent-update interface. ReactorCraft energy/fluid/item receivers should copy these invariants:
capacity checks must use current state, requested values must be bounded before routing, caller-owned
value objects must not be mutated, and multi-resource operations need meaningful aggregate success.

## Shared ChromatiCraft metadata-split/progression seam — 2026-07-26

ChromatiCraft has now converted the complete 13-variant V33a `CLUSTER` metadata family into distinct
registered item identities while retaining original ordinal order, names, and authoritative sprite
indices. Recipes consume those stable identities through datapack JSON; client definitions and language
remain datagen-owned. This is the preferred pattern for small, finite metadata families. ReactorCraft's
very large isotope space still follows its documented single-item data-component decision, but small
finite families should use distinct registry identities when doing so makes recipes and persistence
unambiguous.

The casting controller also restores the V33a distinction between machine capability and player
permission: table XP plus NBT structure controls the physical tier, while `CRYSTALS`, `RUNEUSE`,
`MULTIBLOCK`, `PYLON`, and `REPEATER` progression gates the player who starts the recipe. Completion
awards both full table XP and the original quarter-rate player XP. ReactorCraft machines with research,
achievement, owner, or operator gates should preserve the same separation instead of treating a valid
multiblock or upgraded machine as implicit player authorization. The combined suite is green at 41/41.

## Shared ChromatiCraft live-network casting seam — 2026-07-28

ChromatiCraft now exercises its machine-controller and receiver contracts against a real routed source:
a registered pylon outside direct table range selects a colour-matched repeater, pays attenuation, and
fills the casting table's exact aura deficit before the 400-tick recipe advances. Completion atomically
drains the aura and all 24 auxiliary stands, emits the source-exact high-energy core, and awards table
XP. This closes the persistence/path/receiver/controller chain as an integrated server loop rather than
as isolated unit seams.

The recipe dependency work also confirms two reusable migration rules. Small finite metadata families
(charged shards and tiered resources) should become stable registered item identities with authoritative
legacy sprites; recipes remain generated datapack data. Construction-order overwrites in old Java recipe
builders must be audited as executable behavior: port the final coordinate map, not every transient
assignment and not a guessed symmetric layout. The combined required GameTest target is now 47/47.

ReactorCraft's future network-fed or multiblock recipes should use the same NBT-first structures,
bounded difference requests, asynchronous delivery, and atomic completion invariants. Its large isotope
families remain the documented data-component exception; this small-family identity pattern does not
supersede that decision.

## Shared ChromatiCraft cancellation/reload seam — 2026-07-28

ChromatiCraft now validates both failure and persistence paths for long-running external-inventory
crafts. Removing a mandatory block from the canonical NBT structure cancels the operation without
consuming the center or auxiliary inventories, awarding progress, or leaving external slots locked.
Separately, a pylon-fed craft is saved and reconstructed after routed aura delivery, retaining the
active recipe key, timer, energy, and inputs through modern block-entity persistence before committing
atomically. The combined suite is green at 48/48.

ReactorCraft multiblock machines should adopt the same invariants: structural invalidation must be a
lossless cancellation boundary, external inventory locks must always be released, and persisted work
must resume from an explicit recipe/process identity rather than recomputing against potentially
changed inputs or consuming resources a second time.

## Shared ChromatiCraft multi-resource atomicity seam — 2026-07-28

ChromatiCraft's casting controller now has a focused three-resource transaction boundary. The V33a
Lumen Core recipe matches its canonical NBT-backed L3 structure and exact final 24-stand map, requires
60000 BLACK, YELLOW, and BLUE lumens concurrently, and commits all three energy debits, every external
inventory input, output, and XP as one operation. The companion chained MULTIBLOCK regression proves
that a controller can complete successive external-inventory recipes without retaining stale stand
contents or XP state. The combined required suite is green at 50/50.

ReactorCraft processes that combine multiple tanks, energy stores, inventories, or catalysts should
copy this invariant: validate the entire resource vector before beginning and again before commit,
then mutate it as one server-authoritative transaction. A partial debit in one resource followed by a
failure in another is data loss, even when every individual capacity check is correct.

The dependency audit also reinforces the no-placeholder boundary. ChromatiCraft's Element Unit was
not accepted merely because its Binding Crystal input exists; all sixteen behavioral Elemental Stones
and their chroma-fluid charging/deposit semantics must land first. ReactorCraft dependency slices must
likewise be defined by complete behavior, not only by the presence of registry identities.

## Shared ChromatiCraft NBT-alternative and GUI seam — 2026-07-28

ChromatiCraft's L3 casting monument remains canonical generated structure NBT. A source-parity test
identified and corrected its outer repeater stalks to two smooth-stone blocks beneath each rune and
repeater. Runtime matching now overlays colour-agnostic rune checks for the sixteen functional outer
positions without moving geometry back into Java. This is the required pattern for ReactorCraft
monuments too: generated NBT owns coordinates and palette; Java contributes only semantic alternatives
that a single palette cannot encode.

The casting-table controller also restores V33a's exact four-side repeater grouping and bounded
throughput formula, with grouping state and multiplier persisted and covered by the live routed-network
suite. The combined required GameTest boundary is now 56/56.

ChromatiCraft's first 26.2 GUI vertical supplies the companion client/server pattern: a registered
typed `MenuType`, a `MenuProvider` block entity, `ServerPlayer.openMenu(provider, pos)`, an owner/range
validated server menu, and a screen bound by `RegisterMenuScreensEvent`. The initially recorded
casting screen was only a diagnostic dashboard and has since been replaced by the real V33a layout,
source textures, overlays, stand preview, and lumen bars; diagnostic presentation is not accepted as
a GUI port. Display-only screen queries must not mutate machine state, and gameplay-dependent overlay
state must be synchronized from the server. ReactorCraft's existing menu system follows the same
contract. Future GUI ports in either module must carry their real configuration/progress
synchronization and gameplay actions, not merely recreate a slot layout or render a decorative shell.

## Shared ChromatiCraft renderer/worldgen seam — 2026-07-28

ChromatiCraft's item stand now demonstrates the Minecraft 26.2 submit-render boundary for dynamic
block-entity contents: its exact legacy model geometry is a registered model layer using the original
texture, synchronized server inventory state is extracted into render-state objects, and held items
are resolved through `ItemModelResolver` before immutable model/item/text nodes are submitted. ReactorCraft render ports should use the same split and
must not force block remeshes merely to update dynamic contents.

The first ChromatiCraft overworld generator is also fully data-driven. Cave crystals use a registered
feature plus generated configured/placed registries and NeoForge biome modifiers, while the feature
itself retains the legacy per-chunk scatter and placement contract. The focused test suite verifies
support, liquid, exposure, and colour semantics; all 57 required tests pass. This is the model for
ReactorCraft generators that own nonstandard scatter algorithms: datapack registration and biome
selection remain data, while a small feature class may preserve the original algorithm. Structures
and monuments in both modules remain NBT-template-first; Java owns placement policy and semantic
alternatives, not canonical coordinate geometry.

- 2026-07-28: ChromatiCraft render follow-up landed: registered sound playback for casting stands, direct-texture stand BER + special item model, correct casting-table top/bottom/side datagen, and a dynamic chunk-mesh cave-crystal renderer. See ChromatiCraft/PORTING.md.

## Shared block-entity sync and NBT pylon-generation seam — 2026-07-28

A ChromatiCraft item-stand rendering failure exposed a DragonAPI-wide persistence bug: full
block-entity synchronization called the compatibility `saveAdditional(CompoundTag)` overload and
therefore bypassed subclasses using the Minecraft 26.2 `ValueOutput` override. Full sync and update
packets now originate from `saveWithoutMetadata(registryAccess)`. ReactorCraft block entities with
inventories, tanks, owners, or custom fields inherit the corrected behavior and must continue to
serialize through the modern override rather than parallel packet-only state.

ChromatiCraft natural pylons now demonstrate the required worldgen split for both active ports:
generated NBT owns canonical monument geometry; a registered configured/placed feature and biome
modifier own datapack visibility; Java owns the legacy shuffled-grid policy, terrain clearance,
adaptive foundation, colour substitution, optional damage, and post-placement block-entity setup.
The focused NBT placement regression passes as part of the **58/58 required GameTest** suite.
ReactorCraft monuments and large machines should follow this boundary rather than duplicating their
coordinate palettes inside a feature class.

The same checkpoint restores registered pylon ambient sound and client particles, corrects cave
crystals to the single V33a outline texture plus runtime tint, and permanently renames the item-stand
renderer without a `Port` suffix. Forced Java compilation plus client/server datagen are green.

- ChromatiCraft client sound datagen now emits all 87 active event-to-OGG mappings; validation found zero missing audio assets.
### 2026-07-29 — non-recipe-book machine recipe classification

`ProcessorRecipe` and `CentrifugeRecipe` now report `isSpecial()`. Their intentional
`PlacementInfo.NOT_PLACEABLE` contracts describe fluid/machine processing, not invalid empty
crafting ingredients; this prevents Minecraft 26.2 recipe finalization from warning and ignoring
them. The same foundational correction was applied to ChromatiCraft casting recipes and
RotaryCraft crystallizer/drying-bed recipes. All three modules compile, and the headless integrated
recipe load emits none of the prior empty-ingredient warnings.
## Shared translucent custom-model lesson — 2026-07-29

ChromatiCraft's cave-crystal correction establishes an important 26.2 rendering rule: vertex alpha
does not by itself select a translucent chunk layer when the source sprite is opaque. Custom baked
geometry that depended on a legacy translucent render pass must explicitly force translucent material
classification, while preserving the source's complete face topology; a visually similar primitive
substitute is not source parity. The corrected cave crystal combines forced translucency, alpha-220
vertices, full emission, and the exact neighbor-sensitive V33a mesh.


## Shared positional-audio and menu-container lessons — 2026-07-29

ChromatiCraft's pylon audit exposed two cross-module porting traps. A registered positional
`SoundEvent` still cannot attenuate correctly when its OGG is stereo; ambient machine and structure
loops that were spatial in 1.7.10 must use mono assets while preserving their original samples and
event attenuation. Also, DragonAPI's item-handler slot helper intentionally ignores block entities
that only implement vanilla `Container`; menus for those inventories must register vanilla `Slot`
instances directly. The casting table now proves that seam with a focused 46-slot GameTest.

## Shared legacy-variant registry lesson — 2026-07-29

ChromatiCraft's cave-crystal audit found an interim port pattern that must not be copied into
ReactorCraft: one modern block plus a sixteen-way `color` state property recreated 1.7.10 metadata
instead of giving independently obtainable variants stable registry identities. The accepted
ChromatiCraft cave-crystal, lamp, super-crystal, rune, and encrusted families now use one block and
block item per colour; structure matchers accept the relevant family where the original ignored
metadata. ReactorCraft legacy metadata families should make the same distinction explicitly: use
concrete registry identities for independently named/obtainable variants, and reserve block-state
properties for actual placed-state behavior. Do not introduce generic metadata-emulation adapters.

The same checkpoint reinforces the custom-model rendering rule recorded above: exact legacy geometry
can still show seams under modern backface culling. Where the source effectively rendered both sides,
emit both windings (with matching inverse normals) rather than inventing corrective rotations.
## Shared custom-model interaction-shape boundary — 2026-07-29

ChromatiCraft's cave crystal now demonstrates the complete custom-model block contract: the rendered
position seed and neighbor decisions live in common code, `getShape` supplies the matching mining
ray/hover outline, collision deliberately shares or specializes that silhouette, and translucent
blocks publish separate empty occlusion/visual-light shapes with the intended shade/skylight values.
Do not call client model or block-entity-renderer classes from a common block to derive bounds; that
will fail on a dedicated server. ReactorCraft model ports with non-cubic or state-dependent geometry
must instead expose a server-safe geometry recipe consumed by both rendering and voxel-shape code.
Voxel shapes may approximate angled faces with cached stepped boxes, but their extrema and dynamic
variant selection must agree with the visible model.
## Shared exact-outline versus voxel-collision rule — 2026-07-29

NeoForge 26.2 custom block-outline renderers can replace vanilla's axis-aligned `VoxelShape` outline
with arbitrary submitted line geometry. ChromatiCraft cave crystals now extract edges directly from
the selected baked model quads, which prevents the exact diagonal client outline from drifting away
from the visible model. This changes presentation only: standard block collision and ray clipping
remain `VoxelShape`/axis-aligned-AABB based. ReactorCraft non-cubic model ports may use the same
client-only outline technique, but must retain a common-code voxel approximation for physical
collision, mining targeting, pathfinding, and dedicated-server behavior.
## Shared client-outline, reusable-FX, and biome-foundation lessons — 2026-07-29

ChromatiCraft's casting stand extends the exact-outline rule from dynamic baked blocks to Techne-style
block-entity models: bake the same `LayerDefinition`, walk model-part polygons, apply the renderer's
identical pose transform, and deduplicate polygon edges before submitting a custom outline. Keep the
common-code `VoxelShape` separate and authoritative for physical collision.

Its FX pass also demonstrates that legacy particle families should be ported once as reusable modern
`SingleQuadParticle` primitives, then configured by packet/tile call sites with the original lifetime,
gravity, blend, velocity, colour, and size parameters. Replacing distinct full-bright/additive effects
with generic dust or vanilla sparks may compile but is behavior loss.

For custom overworld biomes, use a datagen `Registries.BIOME` bootstrap plus generated biome tags and
a TerraBlender region registered during common setup. Treat the biome definition/placement seam as a
foundation only when its decorator blocks, entities, or features are not yet ported; enumerate those
dependencies explicitly and never fill the gap with invented vegetation. Legacy independently
obtainable colour variants used by decorators must receive concrete registry identities, matching the
crystal-family rule above.

- **Biome feature-order rule (2026-07-29):** custom biomes must preserve the relative ordering of every
  placed feature shared with vanilla biomes. Rainbow Stream originally put river seagrass before the
  ordinary vegetation shared with vanilla River, producing a `FeatureSorter` cycle; it now follows the
  vanilla River order with seagrass last.
- **BER-only block-model rule (2026-07-29):** a block drawn entirely by a block-entity renderer needs a
  genuinely geometry-free local baked model. `builtin/entity` is not a safe modern blank stand-in and can
  draw the missing-model overlay alongside the BER; GeoStrata Ocean Spike now uses an empty-elements model.

- **Animated legacy strip rule (2026-07-29):** tall V33a animated PNG strips must be sampled as
  `TextureAtlasSprite`s from their stitched atlas. Binding a strip directly as a standalone render-type
  texture can exceed the GPU maximum texture height (`roundflare` is 256x46080) and crash during upload.
### 2026-07-29 — reversed-depth and legacy emissive-pass lesson

ChromatiCraft's pylon correction establishes two additional 26.2 port rules. Custom pipelines must follow Minecraft's reversed-depth convention (`GREATER_THAN_OR_EQUAL`); carrying legacy/conventional `LESS_THAN_OR_EQUAL` makes additive geometry appear through occluders while disappearing in clear view. Legacy full-bright second render passes should become separate model elements with `light_emission: 15`, preserving their animated overlay assets and face selection rather than flattening each variant to a cube-all base texture. Direction formerly inferred from neighbouring metadata variants should be represented by an explicit modern blockstate where placement direction is gameplay-visible.

## Shared ElectriCraft dynamic-conductor and BER asset rule — 2026-07-30

ElectriCraft's renderer audit confirms that legacy dynamic conductors must remain dynamic in 26.2:
use a particle-only generated world model and a `BlockEntityRenderer` which emits the original
centre/end sprites and connection geometry. Do not substitute a cube-all model behind that renderer.
The 1.7.10 material/insulation metadata wire family is now eighteen concrete block and BlockItem
registrations (one per conductor and insulation state), with matching generated recipes, loot,
translations, item models, and BE type coverage; development ports may make this identity break
without a compatibility tag bridge. The RF cable uses the same centre-plus-arm renderer with its
original `rf`/`rf_end` textures. For legacy Techne machines, remove the static steel world cube
entirely (particle-only) and submit the original model through the 26.2 BER pipeline; retain source
textures and any live overlays/text such as fuse heat, transformer pulse, battery charge, and meter
readouts.


### Cross-port note — ChromatiCraft Luminous Cliffs flora (2026-07-30)

ChromatiCraft's reported Luminous Cliffs/Rainbow Stream feature-order cycle is corrected, and the
Luminous Cliffs Glow Daisy/Glow Root feature slice is active. Both flowers are concrete 26.2 block
identities. The remaining V33a Glow Root ambient-drop dependency requires Fertility Seed to become
seven concrete item identities; do not recreate its former item-damage metadata.
### Cross-module DragonAPI progressive breaker registration — 2026-08-02

`ProgressiveRecursiveBreaker` was ported but absent from `TickRegistry`; DragonAPI common setup now
registers it. This was found through ChromatiCraft's Manipulator cliff reveal and restores execution
for every queued progressive recursive operation used by the Reika modules.
### Cross-module DragonAPI modern Container insertion — 2026-08-02

`ReikaInventoryHelper.addToIInv(ItemStack, Container)` now uses `ItemStack.EMPTY`, real container/item
stack limits, whole-stack capacity preflight, and an exact remainder check. The inherited 1.7.10
null-slot and slot-count logic could reject empty inventories or truncate transfers on 26.2. The fix
was proven by ChromatiCraft casting a full 64-item result directly into an adjacent chest and applies
to every Reika module still using the shared `Container` insertion path.

## Shared ElectriCraft source-parity model checkpoint — 2026-08-09

The complete ElectriCraft client-asset pass is now source-parity audited against V31a. All eight
Techne machine BlockItems use 26.2 `SpecialModelRenderer`s backed by the same model layers and
unmodified legacy textures as their BERs, rather than generated steel cubes. Motor rotor/coils/fins,
transformer winding density, resistor colour bands, fuse heat textures, generator flip orientation,
and meter readout transforms preserve their runtime source behavior. The two battery items and all
five wireless-charger tiers likewise use component-aware special casing models; the placed charger
has a six-way facing state and selects the original front/back/tier-side sprites at runtime.

Dynamic wire and RF-cable world models remain particle-only plus BER geometry. Their renderer must
retain the legacy non-X direction inversion which paired the connection query with the opposite-side
visual branch; removing only the visual compensation puts north/south/up/down arms on the wrong
faces. Inventory conductors use the original three contiguous 0.4-block segments for every bare and
insulated material, with centre/end sprites selected by generated model templates.

Runtime six-face geometry follows vanilla 26.2 `FaceInfo` winding, UV-corner order, transformed
normals, and render-type culling. This is required for directional artwork: arbitrary quad winding
can cull the battery bottom or mirror the wireless-charger front even when every texture path exists.
The battery glow artwork is always present, matching V31a; stored energy selects normal versus
full-bright lighting instead of deleting the empty-battery layer.

Validation for this checkpoint covers 184 ElectriCraft client JSON files, 88 mod model references,
70 mod texture references, all 138 packaged PNGs, and all four animation metadata files. No model or
texture reference is unresolved. The eleven Techne texture blobs, three GUI/sheet blobs, and eighteen
modern item sprites extracted from the legacy sheet are byte/pixel-identical to V31a. Ores retain the
legacy ore artwork composited onto the corresponding modern vanilla stone/deepslate bases. Client
datagen, Java compilation, and the packaged ElectriCraft jar all complete successfully.

## Shared ElectriCraft survival GameTest checkpoint — 2026-08-09

ElectriCraft now registers eight in-code 26.2 GameTests and a datagen-owned 11×6×11 arena. The
suite runs on the real dedicated-server classpath and covers every machine BlockEntity registration
and first tick, block/item/machine registry round-trips, mixed wire variants and connectivity,
negative-Y survival battery placement and item consumption, sided transactional FE battery access,
state-preserving battery/fuse/charge-pad drops, vanilla's post-removal machine-break lifecycle, and
the core survival recipe catalog.

The first run exposed four production failures: normal machine/world lookup still returned null,
item lookup collapsed most content to the generator, battery placement rejected all modern negative-Y
terrain and duplicated its item, and the precise resistor BlockEntityType accepted the ordinary
resistor block instead of its own. The stateful-drop pass additionally replaced legacy world lookups
performed after block removal with loot-context state/BlockEntity data, so ordinary machines now
drop reliably and fuse, battery, RF-battery, and wireless-charge-pad configuration survives a
break/place cycle. The final dedicated-server run reports all eight required tests passing.

## Shared ElectriCraft interaction/render stabilization — 2026-08-09

The follow-up survival pass moves all eighteen wire BlockItems onto a registered 26.2 special-model
renderer. It emits the same V31a three-segment geometry and per-material centre/end textures as the
world conductor; normals are transformed through the active pose, removing the inventory shading
artifact that occurred when raw normals survived GUI transforms. Generated item definitions retain
the legacy JSON mesh as the safe base model and select `electricraft:wire` for live rendering.

Wire selection and collision shapes now join a material-width centre to only the connected faces,
rather than presenting an invisible full cube or a single inflated envelope. The original dynamic
component envelopes are restored for resistor, precise resistor, fuse, relay, and transformer. The
transformer port must construct its AABB directly: modern AABBs are immutable, so discarded legacy
setter return values silently restore a full cube. All eight Techne machines additionally register a
polygon-derived custom block-outline renderer, cached by block and horizontal facing, so hover and
mining outlines match the model geometry without making its detailed mesh the physical collision.

Meter text is submitted only through `SubmitNodeCollector.submitText`; sending glyph vertices into
the meter texture's entity-cutout consumer caused `IllegalStateException: Missing elements in vertex`
on placement. Motor cooling fins retain ordinary world lighting at the cool base colour and become
full-bright only when their temperature-derived colour changes. ElectriCraft now has a
server-authoritative Jade provider for conductor values/limits, storage, conversion, transformer,
relay/fuse, and charger state, and its descriptor is verified to load on a dedicated server.

The GameTest suite is now nine tests. Its new shape contract caught the immutable transformer-AABB
regression on the first run; after correction all nine required tests pass on `runServer`. Client
datagen and `:ElectriCraft:compileJava` also pass. DragonAPI's shuffled-grid warning now checks the
actual overlap condition (`2 * deviation >= separation`), so ChromatiCraft's valid 55/20 warp-node
grid no longer emits a false warning or stack dump.

## Shared ElectriCraft concrete identity and live-power checkpoint — 2026-08-09

ElectriCraft's six V31a battery metadata tiers and four fuse ratings are now ten concrete 26.2
block/BlockItem identities. Battery energy remains custom stack data, while tier and fuse amperage
come exclusively from the registered block. Datagen owns every corresponding blockstate, item
special-model definition, translation, recipe output, and loot table. The creative tab emits an
empty and a fully charged presentation stack for every battery tier. The old metadata-packed and
misnamed registry identities were removed; the port intentionally makes no compatibility aliases.

The misleading converter/motor identities are now `induction_generator` and `induction_motor`, with
source-faithful names. Placement initializes the internal V31a I/O direction from the modern facing
blockstate: conversion/wire-component tiles use the player's look direction and transformers use the
right-angle winding direction. This fixes east/west transformer ports without regressing north/south.
Generator and resistor model parts now use `OverlayTexture.NO_OVERLAY`; packed overlay zero was the
cause of the false red/hurt tint on cool placed machines.

The RF cable now resolves its tall animated `rf`/`rf_end` strips through the stitched block atlas in
both the BER and a dedicated special item renderer, preserving animation frames and the isolated
quarter-block item silhouette. Its selectable/collision geometry follows the centre and connected
arms. The RF limit GUI again uses V31a's 197×103 texture, six paired decimal-step buttons, shift
acceleration, Reset, labels, and immediate server packets; zero is the source-faithful zero limit.

Two independent server defects had made electrical networks inert. `TickRegistry` now supplies the
active `MinecraftServer` to SERVER handlers, allowing `ElectriNetworkManager` to run queued network
ticks/repaths in the overworld, and `NetworkBlockEntity.isConnectable` now calls
`Level.hasChunkAt(BlockPos)`. The old call passed block coordinates into `hasChunk(int, int)`, whose
parameters are chunk coordinates, so almost every machine outside the origin rejected network
membership. Wire-network removals also use their `WorldLocation` map keys instead of tile objects.

The dedicated-server suite now contains twelve tests, including ten concrete battery/fuse identity
checks, all four transformer orientations, and a real RotaryCraft creative-coil → induction generator
→ copper wire → redstone battery transfer. The first power test reproduced empty `0SR/0W/0SN`
networks and directly found the chunk-coordinate bug. After correction all twelve required tests pass;
client/server datagen and `:ElectriCraft:compileJava` pass as well.

The fuse BER now uses the culling cutout render pipeline. The Techne cuboids intentionally meet at
coplanar internal joins; submitting them through the no-cull pipeline rendered both sides of those
joins and caused side-seam z-fighting. Culling restores the closed-box behavior while retaining the
binary-alpha regions of the source-identical fuse textures.

## Shared GameTest harness hardening (2026-08-20)

DragonAPI now bounds and diagnoses every headless family GameTest run. Suites are split into
12-test batches, completed structures/chunks and synthetic players are released between batches,
and mock players are not persisted as random playerdata files. A wall-clock watchdog reports
effective fast-forward ticks/s, wall MSPT, progress, active tests, heap use, player count and the
time since the server thread last completed a tick; it dumps the wedged thread after 30 seconds and
terminates only the test JVM after 180 seconds without progress. Direct runs use a 512 MiB/4 GiB
heap envelope and INFO console logging. Spark, Jade and JEI are omitted from headless runs unless
explicitly requested, removing Spark's duplicate `test-mock-player` crash and Jade's incomplete
GameTest registry callback. Full usage and tuning notes are in the root `GAMETESTS.md`.
Each owning mod's test server also uses an isolated `run-gametest` directory, and the watchdog
disarms once assertions complete so final world saving is never treated as a hung server tick.

Jade 26.2.9 asserts that every registered provider UID has its own config translation while a
screen initializes. ElectriCraft's `electricraft:machine_data` provider had only registered the
parent category label, causing title-screen initialization to abort after the panorama and logo but
before vanilla added its buttons. Its datagen provider now emits both
`config.jade.plugin_electricraft` and `config.jade.plugin_electricraft.machine_data`.

ElectriCraft now owns a direct `runGameTest` configuration and defaults to `electricraft:*`.
`wire_and_machine_shapes` also found a real far-coordinate regression: wire-component AABBs added
float fractions to GameTest's +/-15-million coordinates, rounding 3/4-width components into full
cubes. World-space AABB construction now uses doubles and the focused shape test passes.

ChromatiCraft's 2026-08-20 Proxima pass restored the Portal Rift and Void Rift renderers, End Crystal
mover, portal/casting command Structures, Ender-fluid collision callback, source-faithful Ethereal
Luma mosaic, stable aurora animation, bounded Sky River chunk readiness, fissure write-window
preflight, six creative tabs and Heat Lamp edit debounce. Its compile plus client/server datagen pass;
see `ChromatiCraft/PORTING.md` for behavioral details and the focused in-world verification scope.

The 2026-08-21 ChromatiCraft correction pass fixed the Portal Rift's vertical-strip UV sampling,
restored the original 16x16 particle-atlas row selection and Ethereal Luma `aether_flow2` sprite,
restored the portal's source-authentic positional sound event and Minecraft-owned attenuation,
corrected a Proxima-entry static initializer crash, and made the three command casting temples
terrain-safe with tiered tables. The ChromatiCraft compile passes; see its ledger for the exact
in-world checks.

The later 2026-08-21 source-parity audit found V33a's server-authoritative positional portal event.
The 2026-08-22 ChromatiCraft acceptance correction supersedes that literal 90-tick playback because
it caused a 0-4.5-second approach delay: the portal now uses the pylon-style position-bound tickable
loop with continuous distance fade and formed-pad teardown. The same work retained grouped/centered V33a
repeater beams, periodic Power Crystal connection synchronization and
its item renderer, real portal/Dimension Core/Void Rift effect assets, translucent particle ordering,
separate Luma/Chroma/Ender fluid movement and an Ethereal Luma bucket. It also centered Proxima tree
cluster placement to eliminate far-chunk half-trees, raised monuments over their full footprint,
restored L2/L3 Item Casting Stands, and reduced Glow Cave locate probing. ChromatiCraft compile and
client datagen pass; only the three directly affected portal/casting/monument GameTests were run, and
all passed. See `ChromatiCraft/PORTING.md` for the full acceptance checklist and the intentional
sixteen-core monument gate.

The 2026-08-22 follow-up restores player-placement priming for the sixteen-core monument ensemble,
moves extended ChromatiCraft effects into 26.2's actual post-terrain phase, clips Void Rift aura strips
behind opaque terrain, replaces the Item Casting Stand's per-cuboid outline with its exposed union,
adds Liquid Ender's bucket, separates thin/non-swimming Ethereal Luma travel, removes black Dimension
Core item backgrounds, restores active Power Crystal inventory animation and exact socket discovery,
and removes square pylon flare borders. ChromatiCraft compile/client datagen and the two focused
monument/power-crystal tests pass; render, fluid-feel and audio acceptance remains in-world.

The later 2026-08-22 monument follow-up fixed a shared 26.2 persistence seam in DragonAPI:
`BlockEntityBase` chunk/update tags now dispatch modern `ValueOutput`/`ValueInput` subclass state
instead of directly selecting the obsolete `CompoundTag` compatibility overload. ChromatiCraft's
vanilla-based Structure Controller now has the same explicit update packet/tag contract, restoring
the partial Dimension Core ensemble before the sixteen-core ritual gate. The Portal Rift's vanilla
texture reference was also updated for 26.2's nested End-portal asset path and its V33a special item
renderer was ported. ChromatiCraft compile/client datagen and the single focused
`monument_client_sync_contract` GameTest pass.

The 2026-09-13 ChromatiCraft Vibrant Pod follow-up fixes legacy 0..16 UVs being passed to the
26.2 normalized sprite API and restores the animated overlay's texture-derived transparency.
ChromatiCraft and dependency compilation pass. See `ChromatiCraft/PORTING.md` for details;
in-world visual verification remains after a client restart.

## DragonAPI BugCatcher audit remediation (2026-09-20)

The full `DragonAPI Port` Linear finding set (OFF-55 through OFF-202) was reconciled against the
26.2 source. Confirmed defects were repaired across packet/tank synchronization, inventories,
recipes, biome mutation and map output, file/network/XML/config resource handling, reflection,
arrays and parsers, block flood-fill traversal, trackers, render helpers, and legacy registry-tag
bridges. Tank packets now carry the fluid registry identifier so a positive sync can reconstruct an
empty client tank; the DragonAPI, RotaryCraft, ReactorCraft, and ElectriCraft packet readers use the
same wire format. Findings based on impossible states or intentional reversible behavior were left
as no-code false positives. DragonAPI compilation and its Gradle test lifecycle pass (the module
currently has no Java test sources); dependent-mod compilation is the integration check for the
shared packet API.

## GeoStrata survival pass (2026-09-24)

The GeoStrata audit found that 1.7.10's ore renderer drew an underlying rock and a source ore
icon in two passes. Its texture-clipping routine existed but was disabled. The 26.2 port now
generates a layered model for every registered rock/ore pair and datagenerates transparent ore
inclusion sprites from the patched local Minecraft texture artifact; mod ores use recoloured
inclusion patterns until their source textures are available. The client creative gate is enabled.
Icy ore veins, including those in Arctic Spires, yield the existing `lowtempdiamonds` item instead
of vanilla diamonds. Jade now supplies rock, vein, vent and RF crystal information. The pass also
ports the adjustable partial bounds block, RF crystal energy storage/transfer and growth, vent and
arctic air effects, quartz bricks, common item tags and the original buying trade outputs via
26.2 villager trade data. GeoStrata's `SURVIVAL_TODO.md` records remaining visual and parity work.
GeoStrata compilation and both data generators pass. A development client reached the main menu
with Jade loaded and the generated models baked. That smoke run found Jade's required plugin
translation and the partial block's particle texture reference missing; both were corrected. A
fresh-world visual and mechanics pass remains outstanding.
The original `BOXRECIPES` setting now selects between two datagenerated deco-brick recipes through
a NeoForge load-time condition; both client and server data generation pass after this change.

GeoStrata follow-up: layered ore models now use an opaque host-rock base beneath the transparent
ore and host masks; the complete ore matrix is grouped by rock in its own creative tab. Vents
restore redstone-triggered activation and datagenerate dimension-specific host textures. Jade
labels, lava rock names/tick timing, partial-block shared-face rendering and break particles, and
manual RF crystal attachment were corrected. RotaryCraft's Rotational Dynamo now has shaft-power
to RF conversion, its original default recipe, renderer, and creative access so the RF crystal
can be tested. Client/server data generation and compilation passed; in-world behavior remains
to be verified. All 15 RotaryCraft GameTests passed, including shaft-power-to-RF conversion
through the new dynamo; GeoStrata crystal charging still needs an in-world transfer check.

## RotaryCraft progression to ReactorCraft entry (2026-09-25)

The playable route now includes the original blast-furnace alloy processing, high-temperature
combustor, red-gold dust and ingot, tungsten and bedrock gear lines, ethanol crystals from clean
sludge, and the grinder's coal-dust, netherrack-dust, and tar outputs needed for jet fuel. The
fractionator was verified to produce jet fuel from ethanol and its six solids under shaft power;
its ghast tear remains as the original solvent requirement. The rock melter turns ethanol crystals
into liquid ethanol, with each loaded recipe's own temperature enforced. The ordered progression
GameTest now checks the live recipe ingredients from early steel through the microturbine, jet
engine, bedrock 16x gear and 4x gearbox, as well as the fractionator's runtime solid ingredients
and ethanol output recipes. It also checks all four extractor stages for iron, gold, lapis and
redstone, plus the tungsten-flake and aluminum-powder bonus sources. ReactorCraft's alloy,
uranium processor, isotope centrifuge and UF6
recipes load together; the blast furnace produces the original three cadmium-indium-silver alloy
ingots at 1600 C. The jet engine's 65536 rad/s shaft speed reaches the ReactorCraft centrifuge's
262144 rad/s requirement through a 4x acceleration gearbox. A combined-mod GameTest also confirms
that the isotope centrifuge receives 262144 rad/s and nonzero power from a RotaryCraft shaft
provider on its bottom face; the gearbox acceleration ratios have separate in-world tests.

Worktable basic crafting and bucket-filler fluid transfer are restored and covered by in-world
tests. The original red-gold and clean-sludge item artwork is unstiched from the 1.7.10 item sheet
by datagen, with modern item models and language entries. The ReactorCraft fluid recipe codecs use
fluid stack templates so the UF6 recipes load with the combined mods. Later ReactorCraft reactor
operations and the remaining RotaryCraft recipes outside this entry path still require separate
parity work.

## RotaryCraft fractionator and utility-machine follow-up (2026-09-25)

The fractionator's original six solids and consumption weights are now a loaded, generated recipe:
blaze powder 1.5, coal dust 1, magma cream 0.75, pink dye 0.5, netherrack dust 2, and tar 1.5.
The recipe also declares the ghast-tear solvent and ethanol-to-jet-fuel fluids. The machine and JEI
read the recipe rather than a separate ingredient table. The in-world test fills the solids in
reverse recipe order, verifies the original weights, and produces jet fuel without consuming the
solvent. The 1.7.10 pressure calculation's 20-tick moving torque average is restored, including
its save data; pressure again controls the redstone signal and no longer discards stored fuel at
the original no-op overpressure threshold.

The Boring Machine now holds its shaft position when a selected cell cannot be cut, applies its
Fortune and Silk Touch enchants through block loot, saves those enchants, and routes broken
container contents to an adjacent inventory or drops them. It checks placer break permissions,
preserves the IgnoredByBorer hardness hook, and emits the original slice-dig API event. Its GameTest
bores two slices, checks
the 7x5 mask on the second slice, and verifies Silk Touch stone in an adjacent chest. The Sonic
Borer now keeps pressure when its scan encounters an undroppable block and passes the checked
range to the projectile so it cannot rescan a changed surface while firing. Its GameTest charges
against bedrock, then clears the first solid surface when the shield is removed. The Woodcutter
GameTest fells an oak tree and replants its sapling; its Efficiency-adjusted operation interval is
clamped to one tick in instant-cut mode. The original non-instant setting again makes unsupported
tree blocks fall and processes one block each tick. Machine enchantment levels are now restored by
registry key on reload, fixing
the shared persistence path used by the Woodcutter and other RotaryCraft machines. The GameTest
arena is now 9x9x9 to hold the Sonic Borer's complete seven-block cross-section.

The fuel line was an additional progression blocker: its fluid predicate compared each live
fluid to a deferred registration holder, rejecting both RotaryCraft jet fuel and ethanol. It now
accepts the registered fluids, preserves the original named fuel integrations (including the
ReactorCraft LiFBe fuels), exposes its actual block identity, and reports non-self connections.
The fractionator GameTest now carries its produced jet fuel through a fuel line into a jet engine,
so the ethanol-to-ReactorCraft shaft-power path checks real fluid movement as well as crafting.
DragonAPI's server-placer accessor also now returns null for unowned machine instances; the
borer's permission check exposed a null dereference there in the GameTest arena.

## ReactorCraft fuel chemistry and live shaft chain (2026-09-26)

The combined-mod GameTest now drives the isotope centrifuge with a fueled RotaryCraft jet engine,
a bedrock 4x acceleration gearbox, and a west-to-up bevel gear. It checks the engine's rated
65536 rad/s, the gearbox's 262144 rad/s output, and nonzero power at the centrifuge's bottom face.
The same test places a uranium processor, supplies water, fluorite, and a uranium ingot, transfers
50 mB of its UF6 output into the centrifuge, and checks that the running machine consumes that
fluid and produces enriched or depleted dust.

The uranium processor and isotope centrifuge now use the loaded ReactorCraft machine recipes at
runtime instead of parallel hardcoded process maps. The original second processor process,
lithium plus emerald dust and hydrofluoric acid into LiFBe, is generated as
`reactorcraft:processor/lifbe` with its original amounts and 120/600 tick stages. The fluid
recipe types now match a fluid input rather than returning false. The processor still accepts
the original external `gemFluorite` catalyst tag alongside the generated fluorite ingredient.
The processor's LiFBe output has its own in-world GameTest. The fuel-rod and first reactor
operation checks are recorded in the checkpoint below.
The combined-mod `rotarycraft:*` run passed all 30 required GameTests after this change. The
LiFBe check supplies enough lithium to keep the original input-fluid-selected process active
through six intermediate batches and its 600-tick output stage.
The processor also checks fluid identity as well as free capacity before spending reagents, so a
leftover UF6 output cannot silently eat the next LiFBe batch. Its catalyst slot accepts the
loaded recipe's declared ingredient in addition to the original fluorite compatibility path.

## ReactorCraft fission and steam output checkpoint (2026-09-26)

ReactorCraft now has its own headless in-world GameTest run (`:ReactorCraft:runGameTest`), using
RotaryCraft's shared arena. Its first tests check the generated four-dust-to-two-rods recipe,
uranium burnup through depleted fuel, the plutonium final-stage exhaustion, active fuel-rod
fission under neutrons, fission heat boiling adjacent water, and boiler steam passing through a
steam line and grate into a lubricated turbine. A RotaryCraft bedrock gearbox accepts the
turbine's shaft output, and a Rotational Dynamo converts it into positive RF/t. The fully spent
plutonium transition was corrected to produce an empty slot instead of repeatedly clamping past
the final damage stage; the fuel rod now treats that result as empty when routing spent fuel.

These checks prove the fission-to-RF route as connected stages. The turbine transport test
preheats its boiler so it can isolate steam movement from the separate live-fission heat test.
Later work should verify automated coolant replenishment, sustained fuel and steam operation,
turbine multiblocks, and generator conversion under a complete reactor layout.

## RotaryCraft production, farming and automation checkpoint (2026-09-26)

The Composter now accumulates successive compost batches: DragonAPI's managed-inventory
`addOrSetStack` helper previously grew a detached stack copy, so only the first batch persisted.
The Composter also implements its container clearing and output-removal behavior, including
permitting the output slot to be emptied. Its in-world test processes two wheat with yeast,
checks two compost in the output, and clears the inventory.

The Pump now processes only actual source fluid states, updates neighbors when removing a
source, runs its world-changing work on the server, and stops at its original 400-damage
failure threshold without repeating the break effect. Its in-world test checks source removal,
water collection, and horizontal pipe output. It also retains the original five-source lava
exception when 26.2 lava source conversion is enabled; a separate test confirms that an
isolated lava source is drained and removed. The Auto Breeder has an in-world test that
supplies shaft power from below, feeds a cow, and checks that exactly one wheat is consumed.

The Refrigerator now sets its shaft input from its block facing, identifies its block and tank,
and exposes liquid nitrogen through the fluid handler and pipe output. Its in-world test
drives it from a creative coil, processes one ice block into the original torque-scaled
1600 mB of liquid nitrogen, and checks pipe access. These five focused RotaryCraft GameTests
pass on Minecraft 26.2. The next automation pass should restore the Item Cannon's inventory
transfer, target persistence and player-facing target controls, then cover the remaining
farming machines (fertilizer, sprinklers, harvester) and fluid-producing machines with
in-world tests.

### Combined-instance ChromatiCraft worldgen diagnostic (2026-09-26)

A client run failed to generate overworld chunk `[-6, 13]` during the features step.
`OverworldStructureFeature.tryGenerateInChunk` used `WorldGenRegion.getBiome`, whose fiddled
sampling can request a chunk outside the feature step's available region. ChromatiCraft now
uses the same biome manager with an uncached noise-biome source for its natural-structure
biome checks. `:ChromatiCraft:compileJava --offline` passes; revisit the affected chunk in
the original C2ME-enabled world to verify runtime behavior. The detailed change is recorded
in `ChromatiCraft/PORTING.md`.

## RotaryCraft Jade and JEI integration checkpoint (2026-09-27)

Jade now has a server-backed provider for every RotaryCraft `BlockBasicMachine` that lacks a
specialized pipe, engine, reservoir, or gearbox panel. It reports shutdown and operating status,
missing shaft power, temperature, range, fluid tank contents, and comparator state where the
machine exposes those values. The labels are generated through `RotaryLang`.

JEI now registers categories and machine catalysts for all fourteen RotaryCraft datapack recipe
types. It also presents the legacy Magnetizer table and the fixed Composter, Refrigerator, and
Obsidian Maker processes; the Worktable is a catalyst for JEI's vanilla crafting category.
NeoForge's `OnDatapackSyncEvent` sends the fourteen custom recipe types to clients, and the JEI
plugin refreshes its entries from `RecipesReceivedEvent` when joining a dedicated server or
after a datapack reload. The code compiles with `:RotaryCraft:compileJava --offline`, and
`:RotaryCraft:runClientData --offline` generated the new English labels. A graphical Jade/JEI
client check and multiplayer datapack-reload check remain to be run.
The Big Furnace's current `BlockEntityLavaSmeltery` still uses placeholder stone output
instead of the original furnace recipe lookup; its machine behavior must be ported before
there is a real process to expose in JEI.

## NeoForge 26.2 fluid capability migration checkpoint (2026-09-27)

DragonAPI now has a transaction-aware `HybridTankResourceHandler` with complete `FluidStack`
snapshots and a `FilteredFluidResourceHandler` for sided views. `HasFluidResourceHandler`
lets the RotaryCraft, ReactorCraft, and ElectriCraft block entity registries expose
`Capabilities.Fluid.BLOCK`. The legacy tank data remains authoritative, preserving named
NBT and direct machine operations while external transfers use `FluidResource` and one
transaction. The old `IFluidHandler` methods and `PipeConnector` protocol are still present
in many machines and are **not** considered migrated.

RotaryCraft has capability implementations in the reservoir, common powered-liquid bases,
common pipe base, and `RCFluidReceiver` (including the Ground Hydrator). The pipe discovers
capability-only neighbors and uses `ResourceHandlerUtil.move` for external transfers.
ElectriCraft's Transformer exposes its liquid-nitrogen input through a fluid capability.
ReactorCraft has capability implementations in its common tanked-machine and pipe bases,
the Centrifuge, Electrolyzer, Synthesizer, Tritizer, U Processor, Fusion Heater and Injector,
Thorium Core, Turbine Core, Steam Injector, Gas Collector, Heavy Pump, Reactor Pump, Heat
Exchanger, Solar Exchanger, Condenser, and Steam Diffuser. Reactor Pump now pushes to
external block capabilities with an atomic move. Both RotaryCraft and ReactorCraft Jade
providers and DragonAPI's fluid-event query read capabilities; the three DragonAPI Lua
tank methods read the new handler and validate their requested index.

Verification on 26.2: `:DragonAPI:compileJava`, `:RotaryCraft:compileJava`,
`:ReactorCraft:compileJava`, and `:ElectriCraft:compileJava` pass. Focused GameTests pass
for reservoir transaction rollback and sided access, pipe-to-vanilla-cauldron conservation,
Ground Hydrator water acceptance, and Electrolyzer transaction rollback and sided access.

Remaining migration work: port the other RotaryCraft machine tanks and fluid interactions;
replace legacy `PipeConnector`/`FluidAction` transfers with transactional capability paths;
convert remaining ReactorCraft specialized pipe and item-container paths; migrate
`HybridTank` off deprecated `FluidTank`; and port ChromatiCraft's deferred old-Forge fluid
cluster fully before adding it to the build slice. Audit side restrictions and component
identity during those changes. See `IFLUIDHANDLER-26.2-MIGRATION-REPORT.md` for the
area-by-area inventory; its 2026-09-26 counts are the baseline before these edits.

Further work in this checkpoint: RotaryCraft engine, Controller, Gearbox, Advanced Gear,
Spillway, Obsidian Maker, Vacuum, Aerosolizer, Fluid Cannon, Bucket Filler and
`EnergyToPowerBase` now expose transactional fluid views. Pulse Furnace exposes its water,
jet fuel and oxygen inputs as three filtered tanks. Steam Turbine combines its liquid
nitrogen tank with a rollback-safe steam energy input on the block-facing side. The Friction
Boiler now pushes steam upward through an atomic capability move. Engine and
Reservoir player buckets now require an exact 1000 mB transaction before replacing the held
item; engine extraction also uses the unsided transactional view. Obsidian Maker,
Fermenter, Pulse Furnace and Big Furnace player buckets use the same exact-fill rule.
Bucket Filler transfers
between its tank and item fluid capability in one transaction, with item replacement handled
through `ItemAccess` and its managed inventory. ReactorCraft's Fuel Dump, Intermediate Boiler
family and Water Cell intake also use modern capability paths. ReactorCraft's machine block
now transfers Synthesizer, Electrolyzer, Heavy Pump, boiler, U Processor, Centrifuge and
Turbine Core buckets/canisters transactionally; the U Processor's inventory container path
uses an item capability and commits only a complete fluid move. The Centrifuge keeps its
pipe-facing view input-only while allowing player canister withdrawal through the unsided
view. The active ChromatiCraft `CrystalTank` API now extends `HasFluidResourceHandler`.

Additional focused 26.2 GameTests pass for Gas Engine sides, Controller-to-Engine fuel,
Aerosolizer rollback, Gearbox's material-specific lubricant limit, Water Cell intake from a
RotaryCraft reservoir, Steam Turbine steam rollback and sidedness, and both Bucket Filler
container modes. All five active module `compileJava` tasks have passed after their
respective changes. This is still a partial migration: many machine implementations retain
`IFluidHandler` and the old `PipeConnector` contract, `HybridTank` inherits deprecated
`FluidTank`, and ChromatiCraft's old-Forge fluid classes remain outside its accepted build
slice. Passing module compiles do not establish an all-modern fluid path.

The subsequent pipe pass gave transactional capability views to the remaining active
RotaryCraft `PipeConnector` classes: Spiller, Centrifuge, Grinder, Pump, Solar Tower, Gas Tank,
Filling Station's shared `PoweredLiquidInOut` base, and both sprinklers' shared base. Gas Tank
reports its torque- and fluid-dependent usable capacity through the shared tank handler.
Sprinklers journal their integer water store and draw from pipe capabilities atomically.
Spiller and Solar Tower gained working fluid intake where their old `fillPipe` methods
returned zero. Both RotaryCraft and ReactorCraft pipe branches now transfer to and from
`PipeConnector` machines through `Capabilities.Fluid.BLOCK` and `ResourceHandlerUtil.move`;
their direct pipe-to-pipe balancing paths are still legacy internal paths. The Friction Boiler
also pushes steam to the block above in one capability transaction. Focused tests pass for
Pump-to-pipe water/lava output, pipe-to-Pulse-Furnace water conservation, and ReactorCraft
Gas Duct-to-RotaryCraft Reservoir deuterium conservation. The reservoir is covered in that
test because its normal gas evaporation consumes 100 mB per tick.

### Legacy fluid API removed from all compiled code (2026-09-27)

This completes the 26.2 fluid migration for everything in the build. It is a prerequisite for
26.3, where NeoForge deletes `IFluidHandler`, `FluidTank`, `FluidAction` and the old `FluidUtil`
outright (see `PORT-26.3-RESEARCH.md`).

- `HybridTank` no longer extends the deprecated `FluidTank`. It owns its `fluid`, `capacity` and
  `validator` fields, and its direct `fill`/`drain` take `doFill`/`doDrain` booleans, which is the
  1.7.10 `HybridTank`'s own contract. Their bodies are NeoForge's former `FluidTank` logic, so
  component matching and `onContentsChanged` timing are unchanged. Named NBT is unchanged.
- `PipeConnector.fillPipe`/`drainPipe` are removed. Nothing called them any more: both pipe
  families already moved fluid through `Capabilities.Fluid.BLOCK`. The interface now only
  describes connection and flow direction.
- 306 legacy members were removed across 63 RotaryCraft, 21 ReactorCraft and 1 ElectriCraft
  classes: the `IFluidHandler` overrides and the `fillPipe`/`drainPipe` implementations.
  Before deletion, each `fillPipe`/`drainPipe` side rule was checked against that machine's
  `getFluidHandler(side)`; all match. Where they disagreed, 1.7.10 decided, and in every case the
  capability view was the faithful one:
  - Heat Exchanger drains only horizontally (`offsetY == 0`); the legacy override also allowed
    the top.
  - Grindstone and Big Furnace accept fluid through the inherited receiver (any side, and
    horizontal sides, respectively); the legacy overrides returned 0.
  - Magnet Engine inherits `EnergyToPowerBase`'s liquid nitrogen input; the legacy override
    reported no tanks.
  - Obsidian Maker accepts water and lava from horizontal sides (1.7.10 `canFill` requires
    `offsetY == 0`); the legacy `fillPipe` returned 0.
  - Friction Boiler accepts water through the inherited `PoweredLiquidIO` input on horizontal
    sides; the legacy `fillPipe` returned 0.

  The audit covered every compiled module's machines: those whose view is inherited from a base, and
  those with their own view (Pump, Pulse Furnace, Solar Tower, both Centrifuges, Grinder, Fluid
  Compressor, Bucket Filler, Spiller, Engine Controller, Vacuum, sprinklers, Gearbox, Advanced
  Gear, Spillway and Aerosolizer). No deleted member belonged to an inner or anonymous
  `HybridTank` subclass, so no tank-level override was lost.
- Machine policy that the views call (`canFill`, `canDrain`, `isValidFluid`) is kept. The
  Electrolyzer's recipe-input test became `isElectrolysisInput(Fluid)`. The Steam Turbine's
  transactional steam handler now goes through the 1.7.10 `addEnergy(amount, doAdd)` helper
  instead of duplicating its clamp.
- The legacy `fluids.FluidUtil.getFluidContained` was replaced by
  `transfer.fluid.FluidUtil.getFirstStackContained` in three places: `ReikaFluidHelper`,
  ChromatiCraft `BlockParticleSpawner`, and RotaryCraft `BlockEntityBlockCannon`. This matches
  1.7.10's `getFluidForFilledItem`. The Block Cannon previously called `.get()` on an empty
  `Optional`, so any non-fluid item in its inventory threw.

Verification: `compileJava` passes for all six modules. A bytecode scan of every module's
`build/classes` finds no reference to `neoforge/fluids/capability/*`, the legacy
`fluids/FluidUtil`, `FluidActionResult`, or `IFluidTank`. Full unselected GameTest suites:
RotaryCraft 47/47, ReactorCraft 6/6, ElectriCraft 12/12.

ChromatiCraft ran 168 tests and 160 passed. The 8 failures are unrelated to this change:
`liquid_chroma_elemental_loop`, `pool_alloying_survival_loop`,
`ball_lightning_entity_contract`, `geode_ore_survival_path`, `tiered_plant_survival_sources`,
`proxima_tree_cluster`, `music_puzzle_layout` and `proxima_structure_placement`. Compiled
ChromatiCraft references neither `HybridTank` nor `ReikaFluidHelper.getFluidForItem`, and the
only ChromatiCraft file edited, `BlockParticleSpawner`'s fluid-item click, is not on any failing
path. The failures are pre-existing and need their own investigation.

Still outside this checkpoint:
- ChromatiCraft's deferred 1.7.10 fluid cluster imports `net.minecraftforge` and is not in the
  build slice. It adopts the capability pattern when each cluster is ported.
- Direct pipe-to-pipe balancing still manipulates Reika's own tanks directly. It uses no
  NeoForge API and is unaffected by 26.3.
- The item and energy legacy APIs (`IItemHandler`, `SlotItemHandler`, `ItemStackHandler`,
  `IEnergyStorage`) are also deleted in 26.3 and are a separate migration.

### Legacy item and energy API removed from all compiled code (2026-09-27)

NeoForge 26.3 also deletes `items.*` (`IItemHandler`, `ItemStackHandler`, `SlotItemHandler`, the
wrappers) and `energy.*` (`IEnergyStorage`, `EnergyStorage`). A bytecode scan found six compiled
classes still using them; all six are ported.

- **Cannons** (`BlockEntityInventoriedCannon`: AA Gun, Freeze Gun, Multi Cannon, Railgun) no
  longer `implements IItemHandler`. They are a `WorldlyContainer`, the modern form of their 1.7.10
  `ISidedInventory`: automation inserts only what `isItemValid` accepts (nothing for an
  `InertIInv`), through any face, and never extracts. `RotaryBlockEntities` now exposes any
  `WorldlyContainer` block entity as `Capabilities.Item.BLOCK` through NeoForge's
  `WorldlyContainerWrapper`. Before this, cannons had no item capability at all, and hoppers
  bypassed the ammo filter. The new GameTest `aa_gun_ammo_capability` checks that gunpowder is
  accepted, sticks are refused, and extraction yields nothing.
- `ReikaEnergyStorage` extends `SimpleEnergyHandler`. The owner is marked changed on committed
  energy changes (and on `setEnergy`), not on simulations.
- `ReikaRFHelper.drainStorage` follows the 1.7.10 shape again. It takes a block entity, extracts
  through `Capabilities.Energy.BLOCK` on all six faces, then drains `EnergyHandler` fields, then
  lowers int fields named "energy". An `EnergyHandler` overload keeps the port's storage-level
  entry point.
- The Lua `getStoredRF` and `getMaxStoredRF` methods read the energy capability on the requested
  compass side ("unknown" means unsided, as 1.7.10's `ForgeDirection.UNKNOWN` did) instead of
  casting the block entity to `IEnergyStorage`. `LuaMethod.hashCode` no longer throws for
  methods without a required class.
- Magnet Engine `isValidSupplier` checks for an FE capability on the facing side. Its missing FE
  *input* capability is pre-existing unported work and stays marked as a todo.

Verification: all six modules compile. The bytecode scan finds no reference to
`neoforge/items/*`, `neoforge/energy/*`, or the removed fluid API. A full `-Xlint:removal`
recompile finds no remaining NeoForge for-removal usage that would break: `MachineScreen` and
`ReactorGuiBase` only *define* `getGuiLeft`-style getters, which become ordinary methods once
NeoForge drops its versions. GameTests: RotaryCraft 48/48, ReactorCraft 6/6, ElectriCraft 12/12.
ChromatiCraft and GeoStrata reference none of the changed classes.

`-Xlint:removal` only covers `forRemoval` markers, while PR #3489 removed *every* deprecated
NeoForge member. So each member that PR deleted outside the transfer packages was also
cross-checked against our source:
- `Font`/glyph `renderType`, `GlyphRenderTypes`, `NeoForgeRenderTypes`
- `AbstractContainerScreen` getters, `getSlotUnderMouse`
- `CreativeModeTab` tabs image
- `FlowerPotBlock` pot map
- `ConditionContext` / `IContext.registryAccess`
- `AddServerReloadListenersEvent`, `ModifyDefaultComponentsEvent.modify`, `TagsUpdatedEvent` getters
- the four-argument fluid `move`

None is used. `ChromaFluids` overrides the kept three-argument `move`, and `BoxRecipeCondition`
does not call `registryAccess`. A `-Xlint:deprecation` pass shows only vanilla Mojang
"do not call directly" markers and library deprecations.

Deviation to revisit: the Lua RF methods no longer carry a required class, so they apply to any
block entity and throw when it has no energy capability. 1.7.10 limited them to RF tiles. This is
dormant for now: no ComputerCraft or OpenComputers integration is in the build, and the RotaryCraft
handbook already lists every documented method because its class filter is commented out. When
Lua integration returns, add a capability-based validity hook to `LuaMethod`.

Remaining for-removal warnings belong to other owners:
- JEI API deprecations (about 180 uses in the RotaryCraft and ReactorCraft JEI plugins).
- Vanilla `GameTestHelper.makeMockServerPlayerInLevel` (45 uses in `ChromaGameTests`).

## ReactorCraft Jade and JEI integration checkpoint (2026-09-27)

ReactorCraft now registers a Jade provider on its machine blocks and formed multiblock blocks.
It reads machine status, temperature, shaft power and requirements, occupied inventory slots,
fluid tanks, and comparator output from the server where those interfaces are available. The
plugin descriptor and all labels are present in generated client resources.

JEI now covers the Processor and Isotope Centrifuge datapack recipe types and the active fixed
process lists for the Electrolyzer, Synthesizer, and Tritizer. It displays fluid and item
inputs, outputs, process times, temperature and speed requirements, and chance information
provided by those definitions. NeoForge recipe sync sends the two custom types to multiplayer
clients; JEI refreshes them after datapack reload. `:ReactorCraft:compileJava --offline` and
`:ReactorCraft:runClientData --offline` pass. A graphical Jade/JEI check and multiplayer reload
check remain. The three fixed process lists still live in legacy runtime code; migrating those
definitions to datagen is separate porting work, after which JEI should read the new recipe types.

## 26.3 compilation and datagen checkpoint (2026-09-28)

The repository now targets Minecraft 26.3 and NeoForge `26.3.0.26-beta`. ReactorCraft's ore
feature uses the 26.3 `Feature` interface and `MapCodec`; its world registry bootstraps
`worldgen/feature` entries and matching placed features. The recipe and loot providers use the
reloadable registry layer. `:ReactorCraft:compileJava` and `:ReactorCraft:runServerData` pass.
RotaryCraft and ElectriCraft also pass compile and server datagen; GeoStrata passes compile.
The complete build remains red in ChromatiCraft's 26.3 migration. See the implementation-status
section of the root `PORT-26.3-RESEARCH.md` for the current cross-module status and TerraBlender
runtime incompatibility.

ChromatiCraft's 26.3 feature, placement, Proxima noise/material, recipe, and loot registry
clusters have since been migrated in the active source slice. Its focused compile now reports
250 errors, down from 1,202; ReactorCraft and the other previously compiling modules still
complete their compile tasks. ChromatiCraft datagen and GameTests remain pending a compilable
slice.

### 26.3: full compile and first runtime pass (2026-09-28)

All six modules now compile on 26.3, and DragonAPI's tests compile. The last ChromatiCraft errors:
- `setShade(boolean)` on NeoForge's quad builders became `setShadeOverride(@Nullable Direction)`.
  26.2 lit a `shade=false` quad with `cardinalLighting.up()`, so `false` maps exactly to
  `Direction.UP` and `true` to `null`.
- `GlowCavePiece`'s `PerlinSimplexNoise(random, [-2, -1, 0])` uses vanilla's own migration of
  that same octave set (`Biome#FROZEN_TEMPERATURE_NOISE`): three offset-free `SimplexNoise` layers
  in a `NoiseStack`.
- The custom sky hook now receives the fog buffer; `RenderSystem.setShaderFog(skyFog)` is what
  26.2's `setupFog` runnable did.
- `ItemAbilities.HOE_TILL` is gone because tilling moved to data-driven `BlockTransformer`s, and
  NeoForge data-map entries are appended *after* vanilla's, so they cannot override grass to
  farmland. DragonAPI now posts `BlockTransformResultEvent` from `MixinBlockTransformer` (wrapping
  the transformer's `getOptionalState`), and Luminous Cliffs' `createCliffFarmland` listens to it.

Runtime findings (compile does not catch these):
- **Mixin targets.** Verify them against the real jar
  (`DragonAPI/build/moddev/artifacts/minecraft-patched-26.3.0.26-beta.jar`), not
  `Sources/minecraft`. That checkout's unpatched text differs where NeoForge patches, for example
  the first-person renderer's map check, which NeoForge keeps as `instanceof MapItem`.
  - `MixinEnvironmentAttributeSystem` moved to `addDynamicLayers`.
  - `MixinGameTestRunner` now shadows `server` rather than `level`.
  - The held-map mixin keeps its `MapItem` constant wrap.

  A bytecode audit (target class, injector method, `@At` descriptor, accessor and shadow) now
  reports 0 problems for every mixin.
- **Block-state serialisation changed twice.** In JSON, `BlockState.CODEC` takes a block id string
  or an object keyed `id`, and `{"Name": ...}` fails; ElectriCraft's hand-written ore features were
  flattened (no `config` wrapper), moved to `worldgen/feature`, and given string states. In NBT, the
  keys are now `id`/`properties`, and `NbtUtils.readBlockState` silently returns **air** without
  `id`. Every provider that hand-built palette NBT (the RotaryCraft, ElectriCraft and ChromatiCraft
  test arenas, and **all of `ChromaStructureTemplateProvider`**) wrote `Name` while stamping the
  current DataVersion, so no datafixer upgraded it. All four are fixed. RotaryCraft and ElectriCraft
  arenas are regenerated; ChromatiCraft's templates regenerate with its datagen.
- **Motion blocking is tag-driven** (`blocks_motion*`, including heightmaps). NeoForge does not
  populate it for modded blocks. Done; see the next entry.
- **TerraBlender 26.3.0.0.7 does not load on NeoForge 26.3.0.20 or later** (NeoForge #3527 removed
  `DataPackRegistryEvent$NewRegistry`; upstream TerraBlender issue #239). GeoStrata and
  ChromatiCraft cannot launch, so their datagen, and with it ChromatiCraft's stale
  `worldgen/configured_feature` output, and their GameTests wait on a TerraBlender fix or a
  NeoForge pin at 26.3.0.19.

GameTests on 26.3: RotaryCraft 48/48, ReactorCraft 6/6, ElectriCraft 12/12. Client launch is untested.

### 26.3 motion and fluid tags, and shaders (2026-09-28)

**Block tags.** In 26.3, `#minecraft:blocks_motion_no_leaves` (through `blocks_motion`) drives the
motion heightmaps, `causes_suffocation` (which still also requires a full collision shape) and
`blocks_fluid_flow`. Flowing fluid replaces only `#minecraft:washed_away_by_fluids`. In 26.2 all of
this came from `isSolid()`, and fluid washed away anything that did not block motion.
`DragonAPI LegacyMotionTags` classifies each mod's registry.
- **The rule.** A block blocks motion if it is solid (26.2's rule; `calculateSolid` is unchanged in
  26.3), **or** if the mod says its 1.7.10 material blocked movement. Leaves go into
  `#minecraft:leaves`. Otherwise the block is washable, except doors and signs.
- **Why the material rule.** 1.7.10 judged fluids, precipitation height and teleport landing by
  `Material.blocksMovement()`, and ReactorCraft and ElectriCraft made every non-fluid block
  `Material.iron` or `rock`. The 26.2 shape rule therefore let water destroy steam lines, heat pipes,
  the scrubber, steam and every ElectriCraft wire, which was a port regression. Those two providers
  pass `block -> !(block instanceof LiquidBlock)`.
- **Results.**
  - RotaryCraft: new `RoCBlockTagsProvider`; 178 motion-blocking, and 2 washable (canola and HSLA
    fluid, correct under either rule). The mining pipe is solid in only some states and is tagged
    by its default state.
  - ReactorCraft: 102 motion-blocking, none washable.
  - ElectriCraft: 47 motion-blocking, none washable.
  - GeoStrata and ChromatiCraft providers are wired but still use the shape rule. Their 1.7.10
    materials need an audit when TerraBlender lets their datagen run.
- **Tests.** `steam_line_holds_back_water` and `wire_holds_back_water` pin the tags and the
  behaviour.

**Shaders.** All 11 of our GLSL files are in the 26.3 dialect:
- `#extension GL_ARB_separate_shader_objects : require`
- `#moj_import` becomes `#include`
- every stage `in`/`out` has an explicit `layout(location)`, numbered to match the vanilla vertex
  shader each one pairs with (`screenquad`, `position_tex_color` or `particle`, read from the 26.3
  jar)
- `reika_color.glsl` drops `#version` and gains an include guard, like vanilla 26.3 includes

Each file compiles with the Vulkan SDK's `glslc` using the game's `GlslCompiler` settings (Vulkan
1.2 target, auto-bound uniforms, no automatic location mapping, `RENDERPEARL_*` macros, and
`<namespace:path>` includes resolved from our assets and the 26.3 jar). An unconverted shader fails
the same check. What still needs a client launch is uniform and sampler binding against each
pipeline's bind group layouts.

GameTests after these changes: RotaryCraft 48/48, ReactorCraft 7/7, ElectriCraft 13/13.

## 26.3 cross-stack integration follow-up (2026-09-28)

GeoStrata and ChromatiCraft now depend on the locally patched TerraBlender NeoForge
artifact `26.3-26.3.0.0.7-local.1`. Its removed datapack registry event was migrated
to `NewDatapackRegistryEvent.worldRegistry`. The six-mod offline compile passes;
GeoStrata and ChromatiCraft server datagen pass; ChromatiCraft client datagen passes.
GeoStrata registers the previously missing `geo_rock` feature-type codec. ChromatiCraft
uses one registry entry per shared feature codec and supplies empty common item tags
for the optional Burrow loot materials. The generated 26.3 data was refreshed.
GeoStrata client datagen passes, and the full ChromatiCraft 26.3 GameTest suite passes
168/168 required tests.

## Jade 26.3 client startup repair (2026-09-28)

Jade `26.3.1+neoforge` rejects a block data provider that also implements
`IComponentProvider`. RotaryCraft's `RotaryMachineJadeProvider` and ReactorCraft's
`ReactorJadePlugin.MachineData` still combined the server data and client tooltip
interfaces, causing a client loading failure when Jade discovered the RotaryCraft
plugin. Both integrations now register distinct providers with the same UID and NBT
keys, preserving the server-backed tooltip content. The RotaryCraft and ReactorCraft
compile and jar tasks pass. A ReactorCraft development client with Jade loaded both
plugins successfully and continued through client resource loading.

ChromatiCraft's Fluid Radiator GameTest exposed two DragonAPI open-path helpers that are shared
with this module. `CoordHelper.getAdjacentCoordinates` now returns neighbors of the supplied
position, and `ReikaBlockHelper.getBlockVolume` returns zero for an empty voxel shape. The
ChromatiCraft focused transfer GameTest covers both fixes.

## 26.3 ammonia synthesis and JEI recipe startup (2026-09-28)

JEI first loaded `FluidSynthesis.AMMONIA` during recipe registration and exposed a
DragonAPI port gap: the legacy ore-name `FlexibleIngredient` constructor built an empty
`ItemMatch`, which immediately threw a registration exception. DragonAPI now retains
the corresponding `c:` item tag and a mutable set of explicit alternatives. Matching,
display stacks, and ingredient IDs include both sources. ReactorCraft publishes its
lime and ammonium dust under `c:dusts/quicklime` and `c:dusts/ammonium`; the original
fallback stacks remain accepted even when another mod supplies those tags.

`ammonia_synthesis_ingredients` checks that the fluid recipe initializes, both dust
tags load, and the item alternatives remain usable and displayable. ReactorCraft server
datagen and the focused GameTest passed. The full development client JEI startup has
not been rerun for this change.

## RotaryCraft survival beta follow-up (2026-09-29)

All six mod repositories and the parent stack were committed and pushed before starting this
work. ChromatiCraft implementation was left for the later agent. RotaryCraft now restores the
Steel Purifier with original V33a conversion/crafting/thermal behavior, a reloadable datapack
recipe and synchronized JEI/menu support. PURIFIER is appended to retain existing enum indices.

The expanded machine sweep owns one arena per active registry entry (134), removing the old
50-position cap. It exposed Winder's wrong block entity and missing lifecycle calls in Sprinkler,
ECU, Power Bus, Drying Bed and Ground Hydrator. Powered processing tests exposed Compactor's
missing power direction and lost output-stack writeback. Its V33a environmental and thermal
behavior was restored. Beam Mirror beam cleanup and Music Box dedicated-server score-file
handling were repaired with functional regressions. See RotaryCraft/SURVIVAL-BETA.md for the
coverage boundary, remaining machines and beta acceptance work. The iron-as-steel fixture is
a separate GameTest resource source set and must never appear in release data.

## NeoForge update deferred (2026-09-30)

The `26.3.0.37-beta` upgrade was reverted at the owner's request; the family remains
on `26.3.0.26-beta` with its existing `COMMON` config registrations. All six mods
compiled and packaged on `.37`, and ReactorCraft's five registry tests plus the
focused ammonia synthesis GameTest passed in the existing headless mode. The
normal runtime failed to load JEI `31.7.0.45` because it references the removed
`ModConfig.Type.SERVER` field. The latest published JEI `31.8.0.48` sources also
still reference that field. Revisit the upgrade when a compatible JEI is available;
migrate `COMMON` to `LOCAL` and preserve `reactorcraft-common.toml` explicitly to
retain existing settings.


## RotaryCraft survival follow-up and production login (2026-09-30)

Rechecked the intervening DragonAPI/RotaryCraft/ElectriCraft World Rift work and retained the
NeoForge 26.3.0.26-beta pin. RotaryCraft's baseline passed 201/201. The survival follow-up now
passes 215/215 required GameTests and all 11 JUnit checks; all six release builds pass.

Original four-disc/four-pattern yields are datagen-backed bulk shaped recipes. The targeted
vanilla crafting-table transfer repair preserves max-stack-one storage, whole-batch backpressure,
craft awards/remainders and ordinary vanilla crafting. Drying Bed tests exposed lost output
writeback, missing inventory persistence/survival drops and wrong fluid sides; these were restored,
including registry-aware detached entity loading and persistent progress. Ground Hydrator tests
now check actual farmland irrigation and its 25 mB threshold. Handbook MODINTERFACE formatting
now receives the original RF/Extractor values.

The six release jars plus local TerraBlender boot, save and stop in an installed production
NeoForge server outside Gradle. A real six-mod development client logged in over the network,
loaded the world and completed JEI startup after fixing only the isolated offline test whitelist.
The client reconnected after a production-server restart with saved Purifier/Drying Bed entities.
An external release-client installation, authenticated two-player play and end-to-end survival
remain acceptance work. See RotaryCraft/SURVIVAL-BETA.md for the exact coverage boundary.


## Reactor-type GameTests and sodium solar integration (2026-09-30)

Added 26 required GameTests in `ReactorTypeGameTests`, expanding ReactorCraft from 8 to 34.
All six generating `ReactorType` values now have functional coverage:

| Type | Verified behavior |
| --- | --- |
| FISSION | Existing uranium crafting, neutron heat and water boiling; added plutonium feeding and final-stage consumption. |
| BREEDER | Fuel-slot validation, neutron heat, sodium heating, temperature gate, whole-batch backpressure and hot-sodium exchange. |
| HTGR | A real 25-core pebble arrangement feeds and retains its 24 pellets while generating heat; CO2 heating, temperature gate, backpressure and hot-CO2 exchange. |
| THORIUM | Breeder-neutron rejection, thorium-neutron fuel-to-hot-LiFBe conversion, source heat, exchanger and emergency fuel dump. |
| FUSION | Actual preheater casing, isotope consumption and plasma conservation in charged magnetic outlets; casing, temperature and isotope-dose gates; a real fusion event emits three neutrons into absorbers; sustained absorbed neutron heat reaches water through a heat pipe. |
| SOLAR | A contiguous 35-mirror field discovers and aims at its tower, invalidates on removal, and generates ordinary water-mode power; a real two-block ReactorCraft receiver enables sodium power and warm-sodium return with inventory conservation; powered warm-sodium cooling, output capacity, sided extraction and transaction rollback. |

Auxiliary exchangers receive actual shaft power from redstone-enabled creative coils. The solar
tower must generate its own power from its mirrors and receiver. Starting coolant/preheater/
receiver temperatures isolate thermal operating stages, as the existing steam tests do. These
checks do not claim a cold-start survival build or a continuously operating full fusion toroid.
`NONE` is a non-generating type, not an additional reactor.

The tests and source audit exposed and repaired:

- Breeder automation rejecting all empty modern `ItemStack` slots.
- Intermediate heaters comparing a `FluidStack` with a `Fluid`; both sodium and CO2 heating
  now work, and a batch waits for its full output capacity instead of losing overflow.
- The obsolete sodium-solar output integration gate. The exchanger now implements the existing
  RotaryCraft interface and reports the exact warm-sodium amount accepted.
- RotaryCraft solar-field discovery left commented out, distance calculations ignoring the
  candidate tower, and a primary-tower comparison that restricted generation to Y=0. Restored
  the original contiguous plant discovery, height/falloff rules, aiming and light weighting;
  safe invalidation handles removed blocks. Vertical tank pooling respects capacity.
- Independent RotaryCraft/ReactorCraft sodium registry identities. A receiver exposes its cold
  sodium through the shared interface without introducing a reverse module dependency.
- DragonAPI using solid-render status for air exposure, causing modelled iron piping to unseal
  reactors. Modern motion tags and collision shapes preserve legacy material behavior.
- Heat pipes replacing a donor's whole stored heat with the transfer delta and recording incoming
  source heat with a negative weight. Transfers now retain the donor's residual heat and positive
  provenance; a steam boiler keeps accepting heat after becoming fusion/HTGR-typed while the
  generating cores retain their external-heating protection.

Validation on Minecraft 26.3 / NeoForge 26.3.0.26-beta / Java 25:
34/34 ReactorCraft GameTests, 215/215 RotaryCraft GameTests, 5 ReactorCraft and 11 RotaryCraft
JUnit checks; all six release builds pass offline. Logs are in the workspace's ignored `build/`
directory: `reactor-types-regression.log` and `reactor-types-final-validation.log`.
ChromatiCraft implementation and its pending files remain with the other session.


## Decorative fluorite and CPU remote (2026-09-30)

- Decorative fluorite is registered as eight colour-specific blocks. The original floblock textures,
  1.2 hardness/4 resistance, wooden-pickaxe tier, nine gems -> one block and reverse recipes,
  colour-preserving loot and creative entries are generated/registered. A neutron sets ACTIVATED,
  giving light 15 and coloured particles; the original random tick deactivates it. Optional coloured
  lighting remains marked for integration. Fluorite ore excitation still awaits BlockFluoriteOre.
- ItemRemoteControl is fully ported and registered as remote_control. Original recipe:
  SES/BCB/BPB (steel, ender pearl, stone buttons, circuit board, base panel). Charge remains damage
  0..32000 kJ for RotaryCraft's charging station. Binding is free; successful menu opening consumes
  one kJ. Range is 4*floor(log2(charge)) with the original 0.5m allowance; cross-dimension control
  requires charge >8192. Normal and charged creative stacks and charge/range/link tooltips are present.
- REMOTE_CPU is a persistent and synchronized GlobalPos component. Missing worlds/deleted CPUs
  fail without spending charge. The CPU menu creates a detached client display and receives
  menu-owned rod snapshots, so remote views do not query another dimension's client blocks.
  Control packets resolve through the player's current CPU menu, verifying target CPU/rod membership;
  stale, closed and invalid menus are rejected. Local CPU opening uses the same protocol.
- Reactor temperature monitoring links use dimension-aware saved positions, deduplicate repeated
  links, unlink from a previous CPU when relinked, and mark both core and controller dirty. Missing
  monitored blocks are handled safely. Older coordinate-only links resolve to the CPU's dimension
  even when the chunk loads before setLevel, and can still be removed correctly.
- Fifteen new content/remote tests bring ReactorCraft to 49/49: eight colour-specific crafting,
  irradiation, light, wooden-pickaxe loot and decay tests; charge/range/dimension boundaries; binding,
  original remote recipe and disk/network stack round trips; removed CPU/missing dimension failures;
  actual server menu opening with one-kJ debit using a negotiated mock connection; rod membership
  validation; detached remote display snapshots; monitor linking/deduplication/chunk-save migration.
  The existing 34 reactor tests remain passing, including breeder sodium and the solar tower.
- Continued RotaryCraft by fully porting the Distiller and adding twelve functional GameTests.
  Its three original fluid conversions are data-driven, synchronized and exposed in JEI/handbook;
  the current shared tree passes 228 RotaryCraft GameTests. Foreign-fluid fixtures are test-only.
- Limit: new fluorite particles, Distiller basins and remote CPU display have not been visually
  inspected in a real client in this slice; mock menu tests do not prove a real network session.

Final release validation: ReactorCraft and RotaryCraft release jars build successfully; their 5 and
11 JUnit checks pass (DragonAPI: 3). Jar inspection confirms all new content/resources and excludes
GameTest-only fluid substitutions. The attempted six-mod build currently stops in the independent
ChromatiCraft work at ChromaBlocks: PushReaction.BLOCK is absent in the 26.3 API; that work is
left to its owning agent. ReactorCraft alone was rerun after the last edits: all 49 tests pass.


## RotaryCraft Terraformer continuation (2026-09-30)

The Terraformer is fully registered and ported against the 26.3 source APIs, including its original
36 directed biome steps, 54-slot menu, water tank, six-sided summative power, redstone activation,
Tile Selector, persisted work queue and diamond/config-controlled terrain editing. Datagen provides
the original crafting recipe, conversion recipes, biome-family tags, models, loot and translations;
loaded recipes are synchronized for the menu and JEI. Modern 4x4 biome cells charge sixteen original
column costs as one atomic operation and update the complete vertical column through FillBiomeCommand.

Terrain uses real registered target-biome features with FeaturePlacer.placeWithBiomeCheck, limited
to selected loaded cells and permitted non-block-entity positions. Soil starts at original Y=30;
climate freezing/melting also applies without the diamond. Area-provider import has an API bridge
and retains a BUILDCRAFT-PORT integration marker for the absent 26.3 BuildCraft API. Fifty-one new
functional tests use real creative coils and cover each conversion plus gates, inventory/fluid
transactions, save/load, selector/menu validation, area import, climate, soil, trees, boundaries,
survival drops and recipe codecs. Real-client menu/JEI visuals and real biome packet delivery remain
unverified for this new machine. Concurrent AE2/Item Filter port files remain with their owning agent.

Terraformer validation: 51/51 new tests and 284/284 full RotaryCraft GameTests pass on the shared
tree; RotaryCraft release build and all 11 JUnit checks pass with fresh isolated compiler outputs.
The temporary validation configuration and worlds stay under ignored build directories.


## Applied Energistics 2 integration (2026-09-30)

AE2 is the 26.3 port fork (`Other/Applied-Energistics-2`, published to mavenLocal as
`org.appliedenergistics:appliedenergistics2:${ae2_version}`, needs GuideME `26.3.0-port`). It is
`compileOnly` in the root build; the AE2 + GuideME jars live in `ChromatiCraft/run/mods` (and in
`ChromatiCraft/run-data/mods` + `run-client-data/mods`, so ChromatiCraft datagen sees the AE items).
Modern AE2's mod id is `ae2` (`ModList.APPENG` was still `appliedenergistics2` - every AE check was dead).

**DragonAPI layer** (`instantiable.modinteract` / `modinteract.deepinteract` / `modinteract.itemhandlers`):
`BasicAEInterface`/`DirectionalAEInterface` (managed grid node + in-world node host + action host),
`MESystemReader` (storage/crafting/watchers/`MESystemEffect`s on every grid), `MEWorkTracker`,
`MENetwork`, `AEPatternHandling` (pattern decode/encode, pattern-provider insertion), `AppEngHandler`
(AE items by registry id - no AE classes). **Rule for AE-typed code:** machines implement only the
AE-free `MEGridHost`, register their BE type with `AEHooks.registerGridHost`, keep AE objects in
`Object` fields and call AE helpers only behind `ModList.APPENG.isLoaded()`. Never pass an AE-typed
value where a *different* declared type is expected in a class that loads without AE (the verifier loads
both to prove assignability) - that is why `BasicAEInterface.updateReader` exists.

**Ported:** RotaryCraft AutoCrafting Unit (+ Crafting Pattern GUI, whose decode was stubbed) and Item
Filter (both whole machines), Item Pump pattern loading into pattern providers, certus/fluix grinding;
ReactorCraft waste-in-ME neutron leak; ChromatiCraft ME Inventory Link ability, Tool Storage crate as
a grid node, Void/Crystal/Corrosion Resistant Cells, the Unknown Artefact ME drain, the Tool Storage
AE recipe, the ME Item Distributor (whole tile + GUI + TESR + ae2-conditional casting recipe; 2026-10-01).
ElectriCraft and GeoStrata had no AE integration in 1.7.10 (checked upstream/master). Found on the way: `KeyedItemStack` equality never matched (METADATA fell through to false).

**Not portable / deferred:** RotaryCraft `OreForcer` certus (AE2 has no certus ore) and
`fixAEPowerRatio` (AE2 has no RotaryCraft power unit); Bundled Bus (needs ProjectRed); ChromatiCraft
Pattern Cache + Pattern Crystal (V33a WIP: no recipe/research, the crystal's pattern was `null`) and
Remote Terminal (commented out in V33a). ChromatiCraft files whose AE hooks arrive with the file
itself (not yet accepted): CastingAutomationSystem, Casting Auto/Injector, Router Hub, Item Inserter,
Bulk Mover, ChromaticEventManager, Crystal Furnace/Ore Creator (charged certus ore - AE2 has none).

**Tests:** `RotaryAETests` / `ChromaAETests` build real ME networks and only register with AE2 loaded;
drop the AE2 + GuideME jars into `<mod>/run-gametest/mods` to run them (`-PgameTestSelector=<mod>:ae2_*`),
then remove them so the normal suite stays lean.


## RotaryCraft Chunk Loader and tag audit (2026-10-01)

The Chunk Loader is now fully ported on 26.3: original bottom power and speed/radius rules,
configuration gate, original crafting ingredients, model, particles, handbook, loot and item
rendering. Ticket sets expand/contract with power and release on power loss or any block
removal. DragonAPI's restored-ticket validation rebuilds the live ownership cache, preventing
saved outer chunks from remaining forced after first-tick power loss. Runtime caches clear
at server shutdown to avoid ownership collisions when another integrated world opens in the
same JVM. The restore regression
uses the real NeoForge validation callback with saved owner sets; it is not a cold server restart.

RotaryCraft's generated block/item tags now cover machine/pipe pickaxe speed, tool and armor
categories (including special wearable equipment), shears enchantments, source-backed material
compatibility and hardened-glass block/item parity. HSLA is kept out of ordinary steel by
default, preserving the purifier's foreign-steel boundary; alloy tungsten is not pure tungsten.
The original published aluminum-alloy compact/aluminum powder entries are retained under
common aluminum tags, matching V33a RotaryRegistration rather than deriving tags from names.

Ten new Chunk Loader functional tests plus five live-tag tests pass, along with the new
placement test: 301/301 RotaryCraft GameTests and 11/11 JUnit checks; release build passes.
Nineteen machines remain from the owner's original missing-machine list. The client visuals,
real network joining and release-jar server/client play remain outside this headless validation.

## RotaryCraft Fuel Enhancer (2026-10-01)

The Fuel Enhancer is now fully ported against V33a's actual source. Both petroleum fuel and
kerosene convert to jet fuel at 4:1, once per tick, with logarithmic shaft-speed scaling and
the original five catalysts (including pink dye). Recipes are supplied by datagen and synced
to clients; common fuel/kerosene tags replace global legacy fluid names. No foreign fuel is
invented by RotaryCraft: an integration or datapack must populate these tags with registered
fluids. Optional recipe usability callbacks remain available. Difficulty-dependent catalyst
consumption retains the original division by 100 and petroleum's additional 1.5 multiplier.

Ported the inventoried two-tank base, stable transactional capability views, top intake,
horizontal output, fuel-line routes, nine-slot menu with complete power values, crafting,
generated loot/models/tags, handbook, comparator and animated model/fluid basins. The vanilla
Container bridge uses live stacks for hopper merges, preventing loss that snapshot-only reads
caused during validation. Detached inventory loads preserve their supplied registry context.
Any-removal inventory drops and manual versus automated retrieval are independently tested.

Thirty-three new functional tests plus the new registry placement test bring RotaryCraft to
335/335 passing GameTests. All 11 JUnit checks and the release build pass. Release jar inspection
confirms generated resources and excludes foreign-fluid and deterministic-consumption fixtures.
Eighteen machines remain from the owner's original missing list. New Fuel Enhancer visuals,
real multiplayer interaction and third-party fuel integrations still need live-game validation.


## RotaryCraft Fuel Engine (2026-10-01)

Ported V33a's Fuel Engine: 524288 W at 256 rad/s / 2048 Nm, separate 24000 mB fuel/water/
lubricant tanks, original startup, firing and lubricant costs, inertial coast-down, all ECU
settings and normal/flipped ECU fuel transfer. Preserve the integer turbofuel efficiency
expression and original thermal cooling, throttling and double-explosion failure. Added
the original survival recipe, generated models/loot/mining and motion tags, menu, handbook,
item power tooltip, original texture, crank/piston animation, smoke and diesel sound.

Fuel lines now accept the same explicit common fuel tags as the engine. Fluid transactions,
ECU partial-capacity backpressure and manual buckets conserve fluids. Save/load retains tank
contents, flipping, power and timer phase; reload does not reset an ongoing fuel interval.
DragonAPI's atmosphere query events preserve the combustion gate and density/damage extension
points. Absent Advanced Rocketry/Galacticraft/Satisforestry modern adapters are marked;
external fuel comes from populated `c:fuel`/`c:turbofuel` tags, never an invented RotaryCraft fuel.

43 new functional tests plus the registry placement test: 379/379 full RotaryCraft GameTests,
11/11 JUnit checks and successful release build. Tests include actual shafts in four directions,
real fluid pipes, all ECU settings, redstone hysteresis, cooling, vacuum, survival interactions,
and resumed fuel cadence after reload. Overheat dispatches the actual strength-4/8 explosion
events; the test cancels their execution to isolate neighboring structures, so blast damage
and fire spread remain unmeasured. Release fixtures are excluded from the built jar.
Seventeen machines remain from the owner's original missing list. This validation does not
include client visuals/audio, real multiplayer joining or release-jar play outside development.

## RotaryCraft Filling Station survival fix (2026-10-01)

Fixed the missing HasItemHandler binding that omitted all four station slots, completing the
single-tank inventory base's Container/WorldlyContainer and transactional capability behavior.
DragonAPI ArmorSlot now permits valid nonempty equipment. Restore real horizontal shaft input,
1024 W gating, V33a's speed-based filling rate, ethanol crystals and general filled-container
draining with safe empty-container remainders. Fillable carries explicit fluid and amount;
jetpacks and integrated gears persist fluid identity, reject mixing and conserve transferred
quantity. Preserve legacy station ItemsRaw inventories and amount-only jetpack fuel saves.

Thirty-six new functional station tests bring RotaryCraft to 415/415 GameTests. All 11 JUnit
checks, datagen and the release build pass. Release-jar inspection verifies station data,
mining/motion and jetpack armor tags, plus exclusion of the test-only rocket-fuel substitution.
Headless tests cover actual mouse/shift transfers, worn equipment, creative-coil power,
all pack variants, both gearbox fluids, hoppers, transactions, persistence, comparators and
exactly-once survival/command-removal drops. Client visuals and network joining remain outside
this validation. No ReactorCraft implementation changes were needed for this fix.

## RotaryCraft Mob Radar and ElectriCraft RF sprites (2026-10-01)

Ported the V33a Mob Radar with bottom shaft input, original power/range formula and cap,
modern full-height square scans, independent player/animal/hostile categories, RadarJammer
API and static reset, immutable server-synced contacts, saved filters/ownership, original
survival recipe, loot/mining/motion tags, handbook, menu, model, antenna and GUI icons.
Restore its three-quarter collision height and owner-only through-wall HUD rendering.
The Motion Tracker remains an absent tool dependency, preserved through MOTION-PORT and
an optional item tag; the compass substitute exists only in GameTest resources.
DragonAPI restores original mob colors/subclass shading and corrects the modern hostile
classification from all Mob subclasses to Monster plus the existing exceptional categories.

ElectriCraft now generates a minecraft block-atlas directory source for textures/blocks.
This resolves its RF core/end sprite IDs and other original legacy wire/battery sprites
while retaining the valid 20-frame RF animation metadata.

Validation: 446/446 RotaryCraft GameTests (30 new functional radar checks and placement),
11/11 JUnit checks, 13/13 ElectriCraft GameTests, successful datagen and both release builds.
Jar checks verify radar data/assets/tags, exclusion of the tracker fixture and the RF atlas
source/animations. Sixteen machines remain from the original missing list. Client visuals,
real networking and release-jar play remain outside these headless checks. No ReactorCraft
implementation changes were needed for this slice.

## RotaryCraft raw iron balance and lapis/redstone conversion (2026-10-01)

Add the owner's requested raw-iron extractor route. Minecraft 26.3's Fortune III ore_drops
formula averages 2.2 raw iron per mined ore. Original iron ore extraction yields 1.5^4 =
5.0625 ingots on average. Equalizing raw extraction requires a per-stage duplication chance
of (5.0625/2.2)^(1/4)-1 = 23.16451155%; use 23%, giving 2.28886641 per raw iron and
5.035506102 per Fortune III mined ore. The reference is vanilla Fortune III; higher modded
Fortune levels are outside this calculation. Ore remains better through vanilla Fortune 0-III.

Three separate raw intermediates, with generated models reusing existing iron sprites,
preserve this provenance through all four recipes and saves. Explicit duplication_chance
is optional in recipe JSON, carried over network sync and displayed by JEI. It takes
precedence over bedrock guaranteed crushing for raw iron; original ore/rarity/Nether
behavior remains intact. Raw solutions retain the original tungsten bonus.

Restore exact V33a furnace output counts and XP: lapis flakes -> six lapis lazuli (0.6 XP),
redstone flakes -> four redstone dust (0.5 XP), each 200 ticks. Generated advancements
unlock the recipes. Tests uncovered copy-only extractor inventory mutations; consumption,
interstage merging, stacked output and bonus counts now explicitly write back to the
ManagedItemHandler. Twenty new tests cover the balance, actual raw/ore processing,
inventory conservation, persistence, codecs and real multi-output furnace behavior.
No ReactorCraft implementation changes were required.

Validation: RotaryCraft passes 466/466 required GameTests and 11/11 JUnit tests, server/client
datagen and the release build. Release-jar inspection confirms the 40 extractor recipes,
four raw rates, three existing-iron texture mappings and both counted smelts/advancements.

## Tagged RotaryCraft modded/custom extractor integration (2026-10-01)

Restore all 90 original modded ore families through common item tags: copper shares its
modern vanilla chain; 89 other families gain complete four-stage chains, original sprites,
counts/rarity XP and tagged furnace products with original RotaryCraft fallbacks. Three
extra stage-zero recipes preserve Nether Force/Mimichite/Essence metadata variants.
Final-stage bonus recipes restore the original secondary table with ore-tag availability
gates in place of mod-ID gates, including coal's pitchblende alternative. The original
integer-reciprocal bonus roll is preserved. The raw-iron 23% balance remains unchanged.

ReactorCraft now publishes pitchblende/end pitchblende, cadmium, indium, silver, calcite,
magnetite, thorium, ammonium and every colored fluorite ore in common item and block tags.
Products publish corresponding common ingot/dust/gem tags. ElectriCraft publishes its six
ore/material families too. Tagged smelts prefer an installed external product, using a
deterministic registry-ID ordering and the original RotaryCraft output as fallback.

Datapack custom extraction uses component-bearing generic intermediates, tagged inputs,
configurable counts/rates/bonuses, name/tint and component-sensitive smelting. GUI and
automation validate the synchronized recipe set. Fix component-blind merging, counted
primary/secondary output backpressure and partial-water processing. The full tag catalog
and five complete custom recipe examples are in RotaryCraft/EXTRACTOR-TAGS.md.

Validation: 104 new RotaryCraft tests; full suites pass 570/570 RotaryCraft, 13/13 ElectriCraft
and 49/49 ReactorCraft GameTests, plus 11/11 RotaryCraft JUnit. Datagen and all three builds
succeed. Release-jar verification confirms the 399 extraction recipes, 89 counted tagged
smelts, all original sprites/models, unchanged raw rates, cross-mod packaged tags and
exclusion of test-only custom/fake-ore fixtures. Log: build/mod-ore-final-validation.log.

## 2026-10-02 — ChromatiCraft overworld generation parity

ChromatiCraft now owns a saved, bounded post-population queue for full V33a dungeon retry squares
(48/64/96 blocks) and floating-island air checks after vegetation. Luminous Cliffs terrain shaping
runs before the 26.3 vanilla carver pass, with a persisted stage attachment preventing later refill.
The Ocean Temple water-surface anchoring defect is corrected to seabed anchoring. Full detail and
focused test evidence are in `ChromatiCraft/PORTING.md`; no ReactorCraft runtime behavior changes.
ChromatiCraft compilation passes, with 5/5 focused worldgen parity GameTests and 8/8 existing
structure regression GameTests passing; fresh-world visual/density acceptance remains outstanding.

## 2026-10-03 — ChromatiCraft overworld survival audit

Completed the source-inventory audit and closed Ocean Temple entrance/shaft, dungeon biome-boundary,
natural finishing, Warp Nexus seed, missing Cliffs flower, GeoStrata natural-rock support and lore-tower
island/headroom/loot gaps. Natural lore towers join the saved post-population queue. Fixed Lock Key
chunk-unload notification that reloaded delegates and caused an infinite server save/unload cycle.
Final all-Reika survival generation contracts pass 8/8 and existing biome-fragment puzzles 3/3; both
servers save and exit cleanly. Details and logs are in ChromatiCraft/PORTING.md. No ReactorCraft runtime
behavior changes. The early external test mistakenly loaded a stale FTB Library 26.2.0.8 snapshot
from TestInstance/mods. Both real client folders already contain FTB Library 26.3.0.1. The launcher
now sources headless external dependencies from TestInstance/run/mods and lets clients discover
that folder normally, instead of adding legacy snapshots to their runtime. No jars or saves changed.
Pylon worldgen tests pass 3/3 and Data Node tests 2/2. A fresh normal-noise/TerraBlender probe passes
across 54 FULL chunks in six biome regions, retaining progression ores, shaped Cliffs ground,
ChromatiCraft trees and glowing plants after all-Reika decoration; explored queues drain and the
server saves/exits successfully. Tower/artefact caches now distinguish actual worlds with equal
seeds. GameTest disables structure sets, so natural density/client visual acceptance is not claimed;
island candidates and placement remain covered by their focused parity tests.
The corrected current-external-mod runs also pass: 8/8 survival contracts plus the 54-chunk
normal-world population probe with FTB Library 26.3.0.1, FTB Ultimine 26.3.0.6, AE2 and GuideME.
The previously reported FTB incompatibility applies only to the stale launcher snapshots.

## 2026-10-03 — First-login items, original chest loot and all-mod advancements

DragonAPI's original PlayerFirstTimeTracker now subscribes to the server login event. Restore
ReactorCraft, ElectriCraft and ChromatiCraft book-tracker registration alongside RotaryCraft's
existing registration. Each original tracker ID completes only after delivery; the existing
death-persistent player tag survives reconnects and NeoForge death cloning. Existing carried
books avoid duplicates. Delivery fills available slots and drops the exact remainder, including
full creative inventories, rather than using Inventory.add's creative discard behavior. Fake
players do not receive starter items. Existing players whose trackers never ran receive their
missing books on their next login, subject to the original handbook configuration options.

Restore all 28 live RotaryChests V33a entries across seven vanilla chest tables through generated
injection subtables and NeoForge global loot modifiers. The bonus chest adds HSLA steel, 1–5
ingots, weight 6, tier 1. Original counts, weights and tier gates remain data-driven, and changing
CHESTGEN takes effect at loot generation. Like the existing ChromatiCraft implementation, the
modern additive pool includes weight 100 for no extra item: relative original weights survive,
but absolute probabilities differ from 1.7.10's shared vanilla weight denominator. Existing
ChromatiCraft chest injection remains active.

Generate 213 gameplay advancements (recipe-unlock advancements are additional): RotaryCraft
46, ReactorCraft 28, ElectriCraft 22, GeoStrata 42, ChromatiCraft 75. Restore both original
achievement catalogs, authored English descriptions, dependencies and challenge flags. Only
actual item milestones use inventory criteria; book reading, mining, machine operation and
combat keep action callbacks. The shared RotaryCraft achievements option also gates automatic
RotaryCraft/ReactorCraft inventory criteria. Restore missing machine/output/tool hooks, the
real registered plutonium hazard, and the fully functioning Gravel Gun with its original recipe,
charge/ammunition/damage/PvP rules and three combat milestones. Modern Gravel Gun recipe gating
resolves common item tags without constructing stacks during registry bootstrap; fix the old
runtime gate resolver that discarded valid selected materials.

ReactorCraft canister contents now use immutable FluidStackTemplate-backed CanisterContents.
This is required for 26.3 advancement/recipe reload, which precedes fluid component binding.
Preserve the previous SimpleFluidContent saved JSON shape, including explicit empty contents;
the UF6/ammonia display icons retain their actual fluids and amounts.

ElectriCraft and GeoStrata had no upstream achievement page; their new generated tabs cover
electrical machines/wires/storage and rock exploration/masonry/resources. DragonAPI is the
shared library/award bridge and has no standalone gameplay tab. ChromatiCraft projects its
74 authored progression stages plus a root, retaining original research text, hidden-stage
presentation and progression notifications. Its original research DAG remains authoritative;
advancements synchronize on stage changes, reset and login, including existing-save backfill.
Inactive optional integrations remain hidden and ungranted. The vanilla display has one parent;
actual research still requires every original prerequisite.

Close two authored upstream entries that had no live award sites. LIGHTFALL now tracks actual
supporting light-block removal through a fatal fall, clearing the marker after real safe landing,
flight, water, teleport, dimension change or reconnect. Supporting collision rather than a
fixed tick timeout handles delayed client onGround reports. Restore the original public
LightBridgePowerLossEvent on NeoForge as well. FIFTYGW measures simultaneous outlet power in
one connected reactor steam circuit (steam lines, high-pressure turbine chains, grates and
powered matching-fluid steam clouds feeding normal turbines). One high-pressure turbine cannot
reach 50 GW: count each producing chain outlet once, aggregate connected outlets, and exclude
separate plants. The measurement uses loaded chunks only and caches each circuit for the current
tick; turbine updates check once per second. This gives "single reactor" a concrete connected
steam-plant meaning, rather than summing a player's unrelated reactors. These callbacks implement
the original authored milestone descriptions; they are additions to the upstream award sites.

The full ChromatiCraft port boundary still applies to research for subsystems outside its
accepted source slice. No dependency behavior was removed to make compilation pass.

Validation: all server/client data providers pass. Generated-data and release-jar audits validate
all 213 displays, translations, parents, icons and criteria, the seven loot modifiers, and the
Gravel Gun recipe/models. Full server suites pass RotaryCraft **583/583** (13 new regressions),
ReactorCraft **60/60** (11 new regressions), and ElectriCraft **13/13**. ChromatiCraft passes its
seven new advancement/integration regressions and six existing progression regressions. All
six module builds pass. Logs: `build/login-advancements-data-complete.log`,
`build/login-advancements-final-all.log`, `build/login-advancements-reactor-regression-complete.log`,
`build/login-advancements-chroma-complete.log`, and `build/login-advancements-package-validation.log`.
The final collision-based delayed-ground check passes **13/13** focused RotaryCraft advancement
tests, followed by all six module builds (`build/login-advancements-delayed-ground-final.log`).
RotaryCraft's 11 and ReactorCraft's five JUnit tests pass. Final jars include the original public
light-bridge event, the causal fall tracker and the connected-plant power measurement.

## 2026-10-03 — All-mod overworld generation inventory audit

Source/data inspection for the owner's question about missing overworld generation; no generation
implementation or new runtime validation is claimed by this audit. Compared active providers,
generated biome modifiers and current feature bodies with retained original source and the
ChromatiCraft survival-generation ledger.

Owner's scope correction: Skypeaters were unfinished upstream and are deliberately disabled;
they are not missing port work. Retrogeneration is out of scope. Forestry, Thaumcraft and
Mystcraft integrations are deferred until viable targets exist and are not current generation
tasks. Historical observations about those systems are retained elsewhere in the ledger.

- ElectriCraft has no active worldgen/placed-feature/biome-modifier datagen path. The legacy
  BasicElectriOreGenerator's actual placement is commented out. Tin, silver, nickel, aluminum
  and platinum therefore lack ElectriCraft natural generation. Copper uses vanilla's ore and
  still generates through vanilla, but the original ElectriCraft copper scatter is absent too.
  ReactorCraft supplies its own silver generation, not the original ElectriCraft distribution.
  The original equivalent-ore/config/discretization behavior also needs a complete modern path.
- RotaryCraft's original optional ExtraIronGenerator is absent. ConfigRegistry.EXTRAIRON remains,
  but has no active generation consumer or feature data. Original source (659e4e688) registered it
  only above factor 1, with floor(10*(factor-1)) veins of size 10 at inclusive y=4..67. At the
  default factor 1 the original generator would do nothing.
- GeoStrata's primary natural feature families are registered: rocks, glow crystals/vines, lava
  rock, ocean spikes, flux crystals, vents, ore veins, Arctic Spires and creepvines (Void Opals
  belong to the End). Missing behavior includes the rock-hosted ore conversion branches and
  Ore Mode consumers; original biome painting around successful creepvines/spires (TerraBlender
  does provide both named biomes, but independently of those placement footprints); the Arctic
  Spire generation event; and the ChromatiCraft-specific Glowing Cliffs vine eligibility.
  Arctic cluster placement is reduced to an 8x8 candidate square rather than original +/-64:
  its retained 24-block spacing rejects every second candidate after a successful first spire.
  Glow-crystal tree-density admission is an approximation, and vine admission omits the original
  adjacent-solid check. These are incomplete source behavior, not absent feature registration.
- ReactorCraft has an active path for every modern ore type. Pitchblende modifiers cover only
  ocean/river tags, omitting original Mushroom Island/shore and Rainbow Forest admission.
  Active ore config uses literal counts and does not consume original ore-density,
  discretization, optional-ore/equivalent-ore or rainbow-fluorite generation settings. Current
  thorium is additionally assigned to the Overworld, whereas retained original ReactorOres
  assigns it to the Nether; this is a dimension-parity difference rather than a missing
  overworld ore type. Existing intentional height remaps are not classified as missing content.
- ChromatiCraft's ordinary overworld inventory has active generation paths, as recorded by the
  recent survival audit. The ledger records Ender Forest lightning without rain and Rainbow Forest
  height cooling as outstanding biome behavior. Broad natural-density/client
  visual acceptance remains separate from the passing focused and 54-chunk population contracts.
- DragonAPI has no independent natural overworld content family to restore within this scope.

## 2026-10-04 — GeoStrata vent selection during parallel world generation

Investigated the Distant Horizons feature-placement NPE at `VentGenerator.getVentTypeFor`.
The shared feature held mutable dynamic weights and an RNG shared between placements. A
concurrent worker could replace them between WeightedRandom's total-weight calculation and
its cumulative selection, allowing a nonempty vent distribution to return null.

Vent selection now builds a local snapshot of positive weights and uses only that placement's
RandomSource. Preserve the original spawn curves, terrain-height normalization, dimension
admission, Ender vent selection, cold-biome restriction and density/placement rules. When no
type has positive weight (including y=0 and normalized heights past the curve endpoint), skip
that individual attempt instead of inventing an arbitrary zero-weight vent. DragonAPI's shared
WeightedRandom API is unchanged.

Added GeoStrata's NeoForge JUnit harness and five regressions covering empty distributions,
height/dimension/biome eligibility, an exact zero RNG roll, another placement interleaved during
the RNG roll, and eight concurrent seeded selections compared with serial results.
Validation: all five regressions pass under the NeoForge JUnit harness; `:GeoStrata:jar`
also succeeds on 26.3. Log: `build/vent-generator-regression.log`. The live Distant Horizons
world-generation workload has not been rerun in this session.


## 2026-10-04 — ChromatiCraft item slice and Iris compatibility

ChromatiCraft now accepts the original Anti-Attenuation Gem, Recall Tablet, Capture Star and Recipe
Tablet, their datagen recipes/models/lang and original sprites. All ten dimensional resource identities
and all 28 tiered identities are registered, with original discovery stages and corrected dimensional
deco resource loot. Acquisition paths still depend on their respective ported structures; the wider
item backlog remains pending. Inventory/serialization regressions pass 4/4 and the module builds.

Address the reported camera-following/black-square pylon and Cliffs mist shader issues using 26.3
world submit geometry, explicitly assigned public Iris emissive/particle programs, and alpha-backed
atlas sprite aliases generated by a custom SpriteSource. Cliffs clouds now render before shader
composition from extracted world-space snapshots. Preserve vanilla effects and source animations;
Iris remains optional. Current 26.3 Minecraft/NeoForge sources and installed Iris 1.11.7 API/bytecode
were the references. Compile and both datagens pass. In-game shader-pack visual verification remains
pending; passing server tests do not certify the rendered result. Details and logs are in
ChromatiCraft/PORTING.md's 2026-10-04 entry.

## 2026-10-04 — ChromatiCraft progression text concealment

Restore the original progression-driven glyph text in ChromatiCraft's active 26.3 item, ability
and Lexicon slice. Use styled Components, the exact V33a glyph texture and a client datagen font
provider. Live research gates cover tiered/dimensional resources, discovered colors, boosted
shards/stones and original GROUPS/CORES/ALLOYS names. Unknown ores retain their localized host
name. Client-only name hooks keep server data untouched; fragment tooltips and progression/
casting titles now conceal and reveal according to original eligibility. Preserve the original
Teleportation Pump's [Unknown]/random amount behavior and avoid invented relay/potion gates.

The changed source classes compile independently against 26.3; glyph/component/resource smoke
checks pass. Six real GameTests pass using an isolated last-built module with these classes and
resources overlaid (build/chroma-text-snapshot-gametest.log). The whole current ChromatiCraft
build remains red in concurrent VoidMonster port work, which is retained on the allowlist. Full
client datagen, a rebuilt release jar and live visual acceptance remain unvalidated. See
ChromatiCraft/PORTING.md's progression text entry for source-parity boundaries and full details.

Coverage includes the Unknown Artefact's original scrolling glyph font and fragment-readability/
read-only eligibility checks.

## 2026-10-04 — VoidMonster Nether cap and ChromatiCraft compatibility

Fix natural Nether duplicates by querying the server's actual loaded entities before spawning;
above-ceiling monsters do not necessarily tick into the old cache. The four-retry singleton
regression and both complete VoidMonster server suites pass 9/9, with and without RotaryCraft.
Existing monsters are preserved. Correct the generated legacy Lua custom-drop example's root
handling and use 26.3 LivingEntity.damageCooldownTime for the lethal health-drain repeat window.

Accept the complete authored ChromatiCraft compatibility cluster: corruption essence/progression,
MOBBAIT, death fog/emitter, sixteen colored altars, Nether/Overworld trap layouts, cross-dimension
four-TNT transfer and destruction ritual, native renderers/shaders/networking, Lexicon previews and
original casting recipes/data. Preserve pylon-attributed fragment and two additional loot/XP
roll bonuses and add the native arrival chunk ticket. VoidMonster remains optional. Compatibility tests pass 10/10 with
VoidMonster and 4/4 without; existing structure regressions pass 17/17 and text regressions 6/6.
DragonAPI structure arrays now record declared states during worldless datagen while retaining
actual live state capture for scans. Full compile/build and both datagens pass; the earlier text
entry's concurrent red-build limitation is resolved. Live visual acceptance remains separate.

See [VoidMonster/PORTING.md](../VoidMonster/PORTING.md) and
[ChromatiCraft/PORTING.md](../ChromatiCraft/PORTING.md) for scope, provenance and validation logs.


### ChromatiCraft item continuation / shared DragonAPI corrections — 2026-10-04

Write the Excavation, Construction and Hover Stars in the active 26.3 slice, with source recipes,
mode networking/UI, selection rendering, original sprites and six runtime regression tests. Native
casting data now expresses the six Excavation Star Enchanting upgrades and Growth Power I while
preserving the central item's components and source fragment requirements. Both casting codecs
carry the new fields; ChromatiCraft network protocol is 2.

Correct ProgressiveRecursiveBreaker's default private-drop inventory, state/blacklist matching,
native tool loot, client/server world cleanup, duplicate completion, queued-state revalidation and
legacy setBlock update arguments. DecimalLineSegment uses a valid compact canonical record
constructor and retains endpoint copying. These shared changes affect consumers in the other ports;
run their relevant recursive-mining regressions when Gradle validation becomes available.

Validation is pending: the Gradle attempt did not execute because automatic approval review returned
an account usage-limit error. New enum references and original asset provenance were checked
statically. Do not reuse older successful build/test logs as evidence for this later slice. See
ChromatiCraft/PORTING.md for exact source contracts, tests, remaining backlog and validation commands.

### RotaryCraft priority continuation — 2026-10-05

Continue the owner's non-ChromatiCraft scope by restoring the full V33a Sonic Weapon and
Motion Tracker on Minecraft26.3, including native registrations/rendering/menus/packets,
original effects, scanning rules, source recipes/artwork and actual Mob Radar owner HUD access.
Repair the required spring -> Winder -> Worktable -> charged-tool chain with per-stack charge,
source material values, real inventory write-backs, transactional sided ports and guarded
controls. Correct both shared RotaryCraft inventory bases' registry-aware detached loading
while retaining fixed handler identity, and restore real spring-powered discharge plus Smoke
Detector charge/range/battery state. No ChromatiCraft source, data or tasks are part of this slice.

Validation: 658/658 required RotaryCraft GameTests and 11/11 JUnit checks pass, including74
new Sonic/Motion/charging checks. Server/client datagen and the release build succeed. The
shared one-slot GUI now references its actual original screen/power-tab assets. See
RotaryCraft/SURVIVAL-BETA.md for provenance, source rounding, corrected chunk-loading test
fixtures, artifact verification and the fifteen remaining machines from the original list.
Logs: build/sonic-motion-final.log and build/sonic-motion-release.log. Live client visual
acceptance and real multiplayer joins remain unverified by these headless checks.


### RotaryCraft Defoliator continuation - 2026-10-05

Continue the non-ChromatiCraft scope with the full V33a Defoliator on Minecraft26.3:
original mechanical probing/range, poison/chlorine intake and potion conversion, vegetation
loot, permissions/owner identity, poison exposure, native item/fluid transactions and real
hopper/pipe automation. Restore the original recipe, GUI, renderer, model, handbook and
survival access. Preserve source slot/potion metadata semantics and prevent chemical or
bottle loss under backpressure. Target-centered client particles now use a complete packet
and require no surviving target block entity. No ChromatiCraft source, data or tasks changed.

The final version passes 719/719 required RotaryCraft GameTests and 11/11 JUnit checks;
server/client datagen and the release build succeed. Jar inspection verifies the native
classes/data/models, original recipe and byte-identical V33a artwork, with no fixture-resource
leaks. See RotaryCraft/SURVIVAL-BETA.md for source parity, the 60 Defoliator checks, fixture
corrections and fourteen remaining machines. Logs: build/defoliator-final.log and
build/defoliator-artifact-verification.json. Live client visuals/multiplayer joins remain
unverified.


### ChromatiCraft item-first continuation — 2026-10-05

Continue full ChromatiCraft ports while other agents own the other mods. Compile the three previous
wand ports and Star Enchanting; restore Creature Sounding Star, Multitool, Ore Harvester, Aura
Visualization Crystals, Spawner Interference Crystal and all six animated Magic Branch materials
with original behavior, recipes, sprites and native 26.3 hooks. Fix literal structure datagen's bare
zero-offset coordinates without relaxing strict authored geometry counts. Native compile and both
asset/data generators pass through the Spawner slice; all ten preceding wand/tool regressions pass.
The three new network/spawner checks exposed mock-player/connection fixture assumptions, now fixed;
corrected runtime suite and Magic Branch checks remain in progress. No other mod source is edited.
See ChromatiCraft/PORTING.md for contracts, exact validation logs, optional integration and live shader
visual gaps. The earlier approval quota limitation no longer applies; Gradle executes successfully.

### Void Monster circular screen warp follow-up — 2026-10-05

Guard VM/Chroma void screen effects against offscreen/nonfinite focus and Iris shadow renders.
VM now tracks the actual client entity, updates focus/distance through its fade, and resets when
that entity is removed/replaced or the level changes. Native administrative generic-kill damage
bypasses the gameplay damage veto, making /kill actually remove normal/ghost monsters.
Three projection regressions and the 22 existing policy assertions pass; all ten native VM server
contracts pass. Both changed modules built against the complete 26.3 RotaryCraft validation jar.
Ordinary workspace compilation remains red at the independent pile-driver forward reference, and
a later combined run hit the concurrently changing Chroma laser-model ROTATEABLE reference after
the VM tests passed. No ReactorCraft implementation or dependency allowlist was changed. Live visual
retest of the reported circle remains required. Details/logs are in VoidMonster/PORTING.md and
ChromatiCraft/PORTING.md.


### RotaryCraft priority continuation - Pile Driver - 2026-10-05

Honor the owner's continuation priority for RotaryCraft and exclusion of ChromatiCraft.
Fully port the Pile Driver, recovered spawner item and hammer-tip mining-pipe support from
V33a to Minecraft 26.3. Preserve depth power, strongest opposing input, full hammer travel,
21-cell impacts, repeated hits, weak lower layers, native loot, shockwaves, entity effects,
spawner type/custom parameters and exact crafting data. Generate all impact rules and
340 optional GeoStrata rock profiles; exercise every profile with the actual GeoStrata mod.
Complete owner protection/revalidation, phase/sync and minimum-height handling. Restore
pipe geometry, empty data-driven loot and horizontal cleanup without changing Chroma code.

Final validation passes 797/797 full RotaryCraft GameTests, 11/11 JUnit checks and the release
build. The 77 new base acceptance checks plus 17 GeoStrata tests pass 94/94; server/client
datagen succeeds. Jar inspection verifies 27 required classes/resources, source recipe,
models, byte-identical V33a model artwork and no test fixture resource leaks. See
RotaryCraft/SURVIVAL-BETA.md for complete parity notes, optional integration limits and the
thirteen remaining machines. Logs/evidence: build/piledriver-final.log,
build/piledriver-final-acceptance.log and build/piledriver-artifact-verification.json.
Live client visuals and real multiplayer joins remain unverified. No ChromatiCraft source,
resources or tasks were part of this slice.

### RotaryCraft block debris texture correction — 2026-10-07

Fix the reported shaft-junction/DC-engine debris and the shared machine-model datagen fallback.
V33a BlockModelledMachine.registerBlockIcons uses RotaryCraft steel for engines, shafts,
gearboxes, flywheels and other modelled machines; their BER artwork is separate from the
atlas sprite used by block particles. Replace nonexistent registry-name texture references
with the original steel/chassis sprites, including static-machine and legacy block aliases.
Regenerate assets and add missing-block-texture and modelled-machine steel regression checks.
Minecraft 26.3 client datagen, all 16 current JUnit checks and jar assembly pass. Independent
parent/texture-reference resolution covers 192 blockstates and 205 models with no missing
particle sprites, explicitly checking 57 engine/transmission variants for steel. Jar inspection
confirms the corrected models are packaged. Evidence: build/rotary-particle-validation.log and
build/rotary-particle-asset-verification.json. Live client visuals remain unverified.


### RotaryCraft shaft-junction model alignment — 2026-10-07

Side-conversation rendering fix: restore V33a Techne axis conversion and live ioside yaw,
select both junction bend geometries, preserve their original UV mirror flags, flat model
part pivots, shaft animation and gear omission after failure. Share the two models' common
geometry/rendering implementation and select the bedrock junction artwork in the renderer.
No power-source traversal or explosion behavior is changed by this rendering slice.

Validation: all three SplitterRenderTest checks pass, covering actual block-entity IO versus
baked shaft directions in all 16 merge/split orientations, flat part pivots and both variants'
animation/broken frames. Final renderer/model sources compile directly against the current
Minecraft/NeoForge 26.3 classpath. A normal test launch encountered concurrent item-renderer
source/test compile errors; the focused rerun used the already compiled native test classes
and passed. Logs: build/splitter-render-focused-test.log and build/splitter-isolated-compile.log.
Live client visual confirmation remains outstanding.


## 2026-10-07 — RotaryCraft item renderer parity

User-requested snapshot of all then-current work committed first: parent 5d29645,
ChromatiCraft 5f358788, ReactorCraft 15862a8, RotaryCraft ccdf41b3 and VoidMonster 6c72720.
The subsequent RotaryCraft item pass restores original inventory transforms and actual
model-rendered inventory geometry across 118 special item variants, material/ratio models,
upgrade artwork, six pipe families and ten handheld tools. Explicit generated pipe poses
restore an isometric icon and shrink both held-hand scales to 0.25. Bounds measure the same
vertices submitted through Minecraft 26.3's native feature pipeline. No ReactorCraft
production code was changed by this item pass. Other agents' concurrent changes preserved.

Client datagen, full RotaryCraft JUnit 22/22, release build and packaged-item resource
verification pass. Full details and limits are in RotaryCraft/SURVIVAL-BETA.md's
2026-10-07 item renderer entry. Live in-game and shader visual confirmation is outstanding.


### RotaryCraft machine sound, wool heat and HUD/tooltips — 2026-10-07

Fix the reported pump distance/re-entry audio, white-wool disappearance at ambient
heat, DC engine Jade rows and missing machine-item Shift power data on Minecraft
26.3 / NeoForge 26.3.0.51-beta.

- Replace the pump server five-second sound re-fire with a client tickable loop.
  Preserve its power/torque/fluid/broken-state gates, original base volume and
  config modulation; support wool muffling, shutdown/unload, replacement, level
  changes and sound-manager reload/recovery. Keep spatial linear attenuation.
- Correct DragonAPI playback: Level.playLocalSound's boolean is distance delay,
  not attenuation. Construct the sound instance with explicit LINEAR/NONE and no
  unintended propagation delay. Normalize the negative custom-distance sentinel
  to vanilla range. RotaryCraft event/broadcast ranges now cover the actual
  sound asset fade distances (16/24/32/48 blocks; jet startup retains 40).
- Classify wool, snow and ice by real block identity/tags in environmental heat.
  White wool's SNOW map colour previously selected snow vaporization even at 29C.
  Preserve true snow/ice melting and wool ignition at 600C. Use wool tags for
  muffling so all sixteen colours work and snow does not count as wool.
- Expose optional thermal state through ThermalTile.hasTemperature(), reusing
  the engine's existing variant-specific method. DragonAPI owns the sole
  server-backed engine temperature row; DC and other nonthermal engines have
  none. Show fuel only on fuel-consuming engines.
- Register MachineBlockItem to call the existing complete machine tooltip helper,
  including engine variant data and fuel-engine specialization. Use the 26.3
  TooltipFlag Shift/all-information hooks, including recipe viewer indexing,
  rather than a hardcoded left-Shift keyboard query.

Validation: all five new in-world feedback contracts pass (all wool colours at
29C, real snow/ice melting and hot-wool ignition, wool beside a powered DC engine,
coloured mufflers, actual registered item tooltips across power machines, pump
sound operating gates). Full RotaryCraft JUnit passes 27/27, including the actual
Jade providers and three audio regressions covering every registered sound's
network range, default-distance sentinel and mono Ogg assets. Both release jars
assemble; artifact inspection confirms the new/changed classes. Evidence:
build/rotary-machine-feedback-gametest.log,
build/rotary-machine-feedback-junit.log and
build/rotary-machine-feedback-artifacts.json.

Live client listening while walking toward/away from machines, sound reload and
visual tooltip acceptance remain unverified. No ChromatiCraft implementation or
port allowlist was changed. Existing renderer/datagen changes were preserved.

Additional baseline validation: both existing pump source-drain GameTests pass
(2/2, water and lava). Evidence: build/rotary-machine-feedback-pump-parity.log.

## ComputerCraft (CC: Tweaked) integration — 2026-10-07

CC: Tweaked 1.120.3 (mod id `computercraft`, jar in `TestInstance/run/mods`) is wired `compileOnly` in the root
`build.gradle` (a `fileTree` on `TestInstance/run/mods/cc-tweaked-*.jar`, same double-load reasoning as AE2).
`ModList.COMPUTERCRAFT` was still keyed on the 1.7.10 id `"ComputerCraft"` and so was never loaded; it is now
`"computercraft"`, which also brings the Distribution Clutch's COMPUTER control mode and the RotaryCraft handbook
ComputerCraft page to life.

**Shape (mirrors AEHooks/AECompat).** 1.7.10 `TileEntityBase` was an `@Injectable` `IPeripheral` and DragonAPIInit
registered `PeripheralHandlerCC` (an `IPeripheralProvider`). A block entity cannot implement `IPeripheral` any more
(`BlockEntity.getType()` returns the `BlockEntityType`, CC's returns a String), so:
- `DragonAPI/.../modinteract/CCHooks` — CC-free, called from the DragonAPI constructor; loads `CCCompat` only when CC is present.
- `CCCompat` — registers `PeripheralCapability` on every block entity type in a Reika namespace (296 with the family loaded);
  the provider answers only for `BlockEntityBase` on the server and returns a cached `IDynamicPeripheral` wrapper
  (cached in `BlockEntityBase.computerPeripheral`, an `Object` field so CC-less installs never resolve the type).
  `equals` compares the block entity, so repeat lookups do not re-attach computers.
- Method calls run through `ILuaContext.executeMainThreadTask` (CC calls peripherals from the computer thread and
  these methods change the world). `LuaMethodException` -> `LuaException` (1.7.10 `invokeCC`); bad-argument casts
  and other runtime failures also become Lua errors naming the method (1.7.10 let them escape as "Java Exception Thrown").
- Lua numbers arrive as `Double` (verified in CC's `CobaltLuaMachine.toObject`), so the 1.7.10 `(Double)args[i]` casts stand.
- Peripheral type = 1.7.10 `getType()` (class name minus "TileEntity"), now `BlockEntityBase.getPeripheralType()`,
  which cuts a "TileEntity" or "BlockEntity" affix so 1.7.10 script names survive (`BlockEntityShaft` -> `Shaft`).
  The creative coil is its own class now, so it reports `CreativeCoil` where 1.7.10 said `AdvancedGear`.
- Per-block-entity method list: `LuaMethod.getMethodsFor` (sorted, so indices are stable). Where two mods register the
  same name for classes one block entity both extends, the more specific class wins (a name can only be called once).
- CC's lookup takes the capability first and falls back to its generic `inventory`/`fluid_storage` peripherals only
  when it is null (checked in `PlatformHelperImpl$PeripheralAccess`), so Reika machines offer Reika's methods
  (getSlot/getTanks/printInv/...) and not CC's pushItems/pullItems — 1.7.10 parity.

**Method registration.** `LuaMethod.registerMethods` walked the classpath with Guava `ClassPath`, which finds no mod
classes under FML's module layer outside dev. It now reads NeoForge's mod-file scan data, honours `@ModTileDependent`
(class presence) and `@ModDependentMethod`, and the registry is a `ConcurrentHashMap` (mods construct in parallel).
Only ElectriCraft called it before; RotaryCraft, ReactorCraft and ChromatiCraft now do from their constructors.
Registered at boot: DragonAPI 17, RotaryCraft 34, ReactorCraft 9, ChromatiCraft 4, ElectriCraft 6 (70).
`getStoredRF` lost its `@ModTileDependent` on the CoFH API: the port reads the always-present NeoForge energy capability.

**Methods ported in this pass.**
- DragonAPI `getNBTTag` (was fully commented; 1.7.10 used `writeToNBT`, the port had `load`): saves without metadata
  and maps each tag type; LIST/COMPOUND return the string form as before, LongArrayTag returns its array.
- RotaryCraft (12 were fully commented): getMobs, getBlockAtPos, shiftPlane, addNote, setTorque, setSpeed, setRatio,
  getRatio, getEnergy, setAfterburner, getRequirements, setECU — every target machine exists now. Notes:
  getMobs keeps the 1.7.10 "Entity"-prefixed class names, returns the UUID as a string (the UUID object was
  unrepresentable), skeleton type 1 = WitherSkeleton, villager profession = registry name, enderman carried block =
  registry name + state string. getBlockAtPos returns registry name + state string for 1.7.10's id + metadata.
  setAfterburner read `args[1]` for a one-argument signature; it now takes the last argument.
  `ReikaEntityHelper.getCreeperFuse/isCreeperCharged/isPigZombieAngry` ported for getMobs.
- RotaryCraft `toggleMetadata` (missing from the port) plus the Blower's "check metadata" filter it toggles: the port
  had dropped the filter as "metadata no longer exists", but this port keeps item variants in the damage value, so it
  compares that again (NBT `metac`, `PacketRegistry.BLOWERMETA`, the GUI's fourth button at texture row 72).
- ReactorCraft's 9 methods were clean but never allowlisted; now `auxiliary/lua/**` (checkFuel/checkPebbleLevel used
  `getStackInSlot`, now `getItem`).
- ChromatiCraft: elementColor, getLumens, isConnected, setEnchantments ported and allowlisted. isConnected and
  setEnchantments cast Lua numbers to Integer in V33a (always a ClassCastException with CC); they read `Number` now.
  setEnchantments takes registry names or the 1.7.10 numeric ids (`ReikaEnchantmentHelper.getLegacyEnchantment`,
  table read from the decompiled 1.7.10 `Enchantment`).
- RotaryCraft handbook ComputerCraft page: icon restored (circuit board = 1.7.10 `ItemStacks.pcb`), real machine icons,
  and each machine lists only the methods valid for its block entity class again.
- ChromatiCraft World Rift: `PeripheralRelay` (DragonAPI, CC-free). A computer beside a rift reaches the peripheral
  of the tile beyond the far rift's Manipulator-chosen face (V33a forwarded every IPeripheral call there). The rift
  invalidates its capabilities on link, reset, direction change and when the far neighbour changes; relay chains are
  depth-capped at 8.

**Still waiting (fully written, not built).** ChromatiCraft LuaAddNote (Crystal Music), LuaCrystalConsole,
LuaGetTankFraction (Crystal Tank) are ported against those machines' APIs and wait for them to be allowlisted. The
three Forestry alveary methods stay pristine until a 26.3 Forestry exists. ElectriCraft getFullStoredEnergy/
getMaxStoredEnergy (EU battery) stay as they are: the battery itself is IC2-only and unported.
OPENCOMPUTERS-PORT: OpenComputers has no 26.3 build; 1.7.10's `invokeOC`, `PeripheralHandlerOC`, the OC node
lifecycle in TileEntityBase and the per-mod `getOCNetworkVisibility` overrides wait for one.
`ReikaRecipeHelper`'s ComputerCraft turtle-recipe replacement reflected on the 1.7.10 `TurtleRecipe.m_recipe`
field; the class was never resolved in the port (like the AE recipe classes beside it), and CC: Tweaked's turtle
recipes are data recipes now.
Pre-existing, noticed here: the Blower's ore-dictionary match (`useOreDict`, also Lua `toggleOreDict`) is still
commented out in `doStacksMatch`.

**Verification.** `RotaryCCTests` (registered only with CC loaded): `cc_machine_peripheral_methods` reads a live
shaft's `getPower` through the peripheral, retunes a creative coil with `setSpeed`, checks peripheral equality and that
a string argument comes back as a Lua error; `cc_ignores_non_reika_blocks`. Run with the CC jar copied into
`RotaryCraft/run-gametest/mods` (then removed): `gradlew :RotaryCraft:runGameTest -PgameTestSelector=rotarycraft:cc_*`
— 2/2 pass, and again with CC in `TestInstance/run-gametest/mods` and the whole family loaded (`:TestInstance:runGameTest -PexcludeExternalMods`). A CC-absent family boot (`:TestInstance:runGameTest -PexcludeExternalMods`) is clean and registers all 70
methods. (`:TestInstance:runGameTest` with the external mods currently fails before loading: `mezz_config_gui` in
`TestInstance/run/mods` needs `mezz_config`, which is not there.)

To unblock RotaryCraft compilation for the test, three mechanical follow-ups to the uncommitted earlier refactor
were made: removed the now-conflicting private `getLiveStack` in `InventoriedPoweredLiquidIO`/
`InventoriedPowerLiquidInOut` (ManagedItemHandler made it public, same body), JEI Magnetizer slots use
`addIngredients(recipe.input)` (input became an Ingredient), and the Defoliator/Sonic Weapon tests pass a
`PacketHandlerCore` to the new `DataPacket.decode(buf, handler)`.


## 2026-10-07 — DragonAPI port-audit follow-through

Implemented the five work groups recommended in DragonAPI/PORT-AUDIT-REVIEW.md after owner approval.
The full behavior/evidence/remaining-work table lives there; the original 61-item audit is preserved.
Input polling now runs on the physical client with explicit key IDs, live action bindings, SDL Alt and
session cleanup. Real disconnects, loaded artifact versions, robust cache parsing, typed WorldID/NBT,
Lua argument/return contracts and real command trees replace the selected incomplete implementations.
Server protection retains the current BreakBlockEvent and now checks mayInteract. Ingredient caches
are bounded and invalidated at reload/session boundaries; component predicates no longer compare equal
merely because their default-stack sample is empty. Worldgen profiling releases its Level. Native text
particles, additional player/glow rendering, popup artwork and current-screen resize are wired on the client.

DragonAPI wire protocol is now **2.1.0**; both peers must update. Biome packets use 512-sample batches to
stay under the existing 4096-int limit. World identity and version cache files are per-save; ambiguous
working-directory legacy files remain preserved and are not assigned automatically to an arbitrary save.

Validation: 49 unit tests (DragonAPI 12 + RotaryCraft 32 + ReactorCraft 5), seven dedicated GameTests
(rotarycraft:dragonapi_audit_*), all active modules compiled through :TestInstance:compileJava, and
ChromatiCraft client datagen. Logs: dragonapi-audit-tests.log, dragonapi-audit-gametest.log,
dragonapi-audit-datagen.log. The opt-in TestInstance AuditClientChecks fixture runs in a copied disposable
save and checks real server key decoding, text particles, custom player-model submission and fresh PNG
output (dragonapi-audit-client.log). The final run emitted DRAGONAPI_AUDIT_CLIENT_PASS and exited
successfully in 2m 11s. It is inert during normal play.

Not blanket certification of the original 61 findings: legacy dispatcher/particle engines, event-bus
profiling instrumentation, other client packet TODOs and unavailable integrations remain explicitly
listed in the review. Five RotaryCraft item PNGs have invalid original bytes and need verified artwork
restoration; no replacement artwork was invented. No physical keyboard or human visual pass is claimed.


## 2026-10-07 — Extended DragonAPI audit review (sections 2/3)

Reviewed the new base/registry/resource and instantiable findings. Current evidence, proposed-fix
corrections and the next dependency order are recorded in DragonAPI/PORT-AUDIT-REVIEW.md under
"Extended review of sections 2 and 3". No gameplay/build/resource implementation changed in this pass.
Confirmed priority work includes DecimalPosition recursion, block-array/blueprint mutation and iteration,
WeightedRandom persistence, key/comparator contracts, CoreContainer lookup/reach and RF/FE availability.
SYNCED is the correct 26.3 config type; several renderer/particle/path claims are already resolved.
A disposable Java 25 probe of the actual ValueSortedMap reproduced equal-value key loss and the custom
value comparator receiving keys (dragonapi-audit-review-probe.log). The previous unit/GameTest/client
results were not rerun and do not cover these newly confirmed defects. Preserve seeded coordinate
hashes and original reference sources during the next full ports.


## 2026-10-08 — Luminous Cliffs, vegetation and Void Monster visual corrections

ChromatiCraft now protects its Luminous Cliffs lake water, four-block beds and one-column banks
from 26.3 carving-mask application, composing with Blender's existing filter. Noise caves remain
below the bed and vanilla carvers remain active in dry raised plateaus. Reika's simplex thresholds,
elliptical undercuts, ocean floor and biome-edge interpolation are retained, with relief above Y62
stretched to a Y300 terrain ceiling; islands leave room for their six-block contours. Existing chunks
are not reshaped. Native worldgen parity suite: five passed, including real noise/carver chunks,
water/bed preservation, dry caves, heights above Y255 and floating-island candidate placement.

Flowing, dark and high water now retain a softer teal/pink tint instead of the old white "clear"
multiplier. Aura Ivy uses double-sided wall planes and flat selection shapes, with attachment
inherited down hanging runs and a registered biome grass tint; its native siting/shape test passed.
GeoStrata icicles now supply bounded packed-ice UVs for every segment, including geometry below
the block that previously read neighbouring atlas textures. Ivy assets regenerated through datagen.

Void Monster distortion is composed after GuiRenderer.endFrame so it includes HUD and open screens.
Visibility, shadow-pass rejection and the original fade envelope remain. Server AI is unchanged:
distance-dependent pursuit, successful-hit 50-tick cooldown, randomly entered 40-tick healing bursts
at 0.25 health/tick times difficulty, and immunity while healing. Twelve standalone native contracts
passed, including new healing-window and pursuit checks; existing four JUnit cases passed/up-to-date.
The first disposable client launch caught and corrected an incorrect GuiRenderer package in the
mixin descriptor. Final in-client verification is being recorded in the module ledgers.


Final client validation: isolated full-family 26.3.0.51 run in a copied disposable save exited
successfully (`build/cliffs-client-final.log`, CLIFFS_CLIENT_PASS). The inspected
`build/cliffs-client-run/screenshots/cliffs-visuals.png` shows water at Y100 plus a falling stream,
packed-ice textures on all icicle segments and flat ivy against its wall.
`gui-before-warp.png` / `gui-after-warp.png` show an open screen's grid and text plus the tutorial
overlay bending under the single post-GUI distortion pass. No Iris/Distant Horizons were loaded
in this fixture; shader-pack compatibility is not claimed by this visual check.

Final packaging: :TestInstance:compileJava and the ChromatiCraft/VoidMonster/GeoStrata jar tasks passed (build/cliffs-final-artifacts.log). The temporary client-fixture class was removed from TestInstance's normal compiled source set; its source and screenshots remain under root build/.


## 2026-10-08 — Extended DragonAPI priority repairs and checkpoint

Implemented the five priority groups from DragonAPI/PORT-AUDIT-REVIEW.md: real position/structure
mutations and complete iteration; weighted persistence and strict component/key/comparator contracts;
menu relay/index/reach/fallback behavior; native RF/FE plus supported wood/ore catalogues; and owned
client resources with reload/source metadata, real sound playback and artifact-hash registration.
Full results and remaining port boundaries are recorded in that review. Seeded Coordinate hashes are
unchanged, original recipes/artwork/reference implementations are retained, and no active source was
removed from a build allowlist to obtain passing validation.

26.3-specific corrections: BlockState.CODEC uses id/properties; ore catalogue initialization belongs
after DefaultDataComponentsBoundEvent because TagsUpdatedEvent precedes bound item defaults; client
reload listeners use AddClientReloadListenersEvent. Component ordering also supports transient native
components without a persistent codec. Structured/Filled offsets and mirrors preserve data and placement
overrides, including layouts without a Level. The grinder locked-seed query uses KeyedItemStack.match.

Validation: 57 unit tests (DragonAPI 20 + RotaryCraft 32 + ReactorCraft 5), all 11 required
rotarycraft:dragonapi_* dedicated GameTests and active family compilation passed in 46s.
Log: dragonapi-extended-validation.log. The extended disposable client fixture also passed
in 1m 57s (dragonapi-extended-client.log, DRAGONAPI_AUDIT_CLIENT_PASS): Minecraft pack reload,
owned asset bytes/source metadata, Chroma glow woods, server key transitions, text/player render
submission and fresh biome PNG. Known invalid textures/puzzle timeout/Windows counter errors remain.

Owner requested committing existing changes before further work, then pushing. Seven mod repository
checkpoint commits and parent 63beb7e preserve all then-current work, including other recent family
visual/worldgen changes. Publishing the exact eight configured origin branches was explicitly approved.

Remaining: verified 26.3 update feed, absent Hydraulic/Pneumatic integration work, unlanded wood/plank
recipes and legacy API consumers, dormant payload/particle/tier clusters, and five invalid original
RotaryCraft textures. The excluded pristine Chroma crafter must migrate the integer helper to
ItemHashMap.add(map, stack, amount) during its full port; no half-port was applied to that file.


## 2026-10-08 — First project backlog batch: turbines and shared API repairs

Completed the N04 source implementation against the original ReactorCraft model/render source at
commit 899bf8ff and the generated Minecraft/NeoForge 26.3 sources. Normal and high-pressure turbines
now render the original six-face unformed cap using turbine_multi sprite 3; unformed flywheels use
flywheel_multi sprite 1. Existing original PNGs are used directly, with native entity-format UVs,
normals, packed light and overlay. Both blade rings use the original angular separation multiplied
by damage + 1, spacing worn blades around the full wheel. Shafts rotate with the wheel; high-pressure
housing remains stationary. The originally rendered stage-zero Shape220b cover plate and its
transforms are restored. Upstream commented-out Shape4 parts remain reference information.

Normal, high-pressure and flywheel renderers capture facing, stage, damage, formed state, light and
spin before deferred geometry submission. Their callbacks retain no live block entity or mutable
render-state object. Spin sign and original per-type 0.2/0.5 animation rates are preserved; flywheel
spin follows its adjacent turbine at six times that turbine's rate. The original waterlubber remote
cosmetic-wear condition remains in extraction. Item-model four-argument renderAll overloads retain
healthy damage-zero behavior. The new cap helper is included in the active build slice.

Shared API work in the same batch: tracked-item dropper authorization/persistent identities;
explicit transactional or native/public-setter energy drains with one aliased-capability budget;
and canonical UUID-string PlayerMap lookup. See DragonAPI/PORT-AUDIT-REVIEW.md. No active source
was removed from an allowlist to obtain a passing result.

Recovered three corrupt RotaryCraft item textures (target, base railgun ammunition, molten-HSLA
bucket) as exact pixels from the original pinned items2.png atlas. Recovery provenance, mapping,
source hash and a repeatable verify-only script are in RotaryCraft/docs/asset-recovery.md. Original
pre-recovery bytes remain in Git and local build backups. Advanced-gear and tungsten-spring assets
still need verified model/artwork provenance; the original railgun's remaining sprite groups remain
recorded for full variant-model selection.

Validation: 65 unit tests passed (DragonAPI 25, RotaryCraft 32, ReactorCraft 8), including the
new UUID, forced/transactional/adapter/alias-budget and native turbine mesh tests. All 12 required
`rotarycraft:dragonapi_*` dedicated GameTests passed. `build/backlog-first-validation.log` records
BUILD SUCCESSFUL in 1m 45s with the active family compiled on Minecraft 26.3 / NeoForge 26.3.0.51-beta.

The disposable native client passed in 2m 5s (`build/backlog-first-client.log`):
TURBINE_AUDIT_CLIENT_PASS verifies registered normal stages 0–4/high-pressure stages 0–6, wear,
formed flywheel/six-face caps, original texture decoding, per-type animation rates and callback
correctness after the same state object is re-extracted. DROPPER_AUDIT_CLIENT_PASS verifies an
actual client request/server reply/client decode for a tracked item credited to an offline UUID.
Malformed client UUID aliases do not overwrite valid metadata. DRAGONAPI_AUDIT_CLIENT_PASS also
retains the existing resource reload/key/text/player-render/biome-PNG checks. Recovered target,
base railgun and molten-HSLA sprites decode natively; their earlier bad-PNG diagnostics are gone.

The first client attempt stalled at the fixture's initial screen gate after resource reload; the
opt-in fixture now disables focus-loss pausing and closes its initial vanilla PauseScreen. This
handling affects only audit runs. The final passing run retains the known two unrecovered Rotary
asset errors, other missing-model texture warnings, Chroma 4x4 puzzle timeout and Windows counter
errors. Automated native geometry checks do not claim human visual or Iris/shader acceptance.

Final ReactorCraft regression rerun after the per-type animation-rate changes: all 8 tests passed
(`build/backlog-turbine-final-tests.log`, BUILD SUCCESSFUL in 20s).

The maintained full checklist is `../PROJECT-BACKLOG.md`; N01/N02/N04/N05 are checked off,
N03 remains partial, and N06/N07/N08 plus the wider dependency-ordered port work remain open.


## 2026-10-08 — Second backlog batch: original assets and RayBlend completion

Resolved N03's remaining source provenance: tungsten-alloy material is the original COMPACTS
metadata 12, type 6 on sheet 0, hence items.png cell 108. The recovery script checks both original
atlas hashes and verifies four sprites as exact 16x16 RGBA pixels. The zero-byte advancedgear PNG
is preserved under RotaryCraft/reference/legacy-assets/, outside the atlas scan; worm gear, CVT,
coil, high gear and creative coil retain generated special item models and original 3D geometry.
The material's misleading original springtungsten name had caused a second mismatch: it was
registered as a charged ItemCoil. Its unchanged ID now uses the original stackable crafting-material
behavior. Full railgun sprite-group/projectile porting is tracked separately as RC19.

N06 baseline: the native layout test reported 9 omitted rooms out of 240 planned rooms but passed.
It now requires every authored profile, along with connected, correctly sized, nonuniform regions,
original compactness caps, valid sudoku colours, cages, starting crystals and room/door geometry.
Failed region attempts previously left cells removed from the remaining pool, and rejected full
shapes were retried unchanged. New attempts return all owned cells and slots before reseeding;
frontier weighting measures the region plus its candidate instead of the frontier itself.
Insertion-ordered point collections avoid identity tie-breaking in colliding hash-map tree bins.
Work is bounded at forty room attempts, eighty region attempts and eighty start candidates.
Exhaustion throws for a new plan; it never admits a shortened authored progression.

Layout version 1 is persisted in structure pieces and floor save/client tags, and participates in
the plan cache key. Missing version fields mean legacy version 0. Existing floors and chunk-clipped
pieces replay the old algorithm, including any rooms that were historically omitted. A recalculated
live generator resolves the version of matching saved floors for solving/force-opening. Unsupported
future versions are rejected rather than silently reinterpreted. The committed pre-fix implementation
(ChromatiCraft 6e3638aaea2f82583d180548787bfcd91c9399a4) independently produced twelve golden plans;
all matched legacy replay before those hashes were retained in the permanent regression suite.

Validation: all 240 authored rooms in the original 36-layout seed matrix generate reproducibly;
128 additional widely spaced seeds (including Long.MIN_VALUE/Long.MAX_VALUE) also complete and
match their twins. All four native rayblend_* tests pass, including actual old/new piece and floor
persistence, client-tag replay and existing puzzle-solving/reward behavior. The permanent unit
suite passes 69 tests: DragonAPI 25, RotaryCraft 33, ReactorCraft 8, ChromatiCraft 3.
Logs: build/backlog-rayblend-baseline.log, build/backlog-rayblend-ordered.log,
build/backlog-rayblend-compatibility.log and build/backlog-second-units.log. The legacy compatibility
fixture intentionally replays an old omitted 4x4 room; its historical diagnostic does not indicate
failure of version 1. The independent comparison source/fixture was temporary under build/ and is
not included in production sources or release jars.


Final native client validation passed in 2m 1s (build/backlog-second-client.log).
GEAR_ITEM_AUDIT_CLIENT_PASS resolves actual generated native item models and verifies original
3D geometry for worm gear, CVT, coil, high gear and creative coil. It verifies the tungsten
material's real reloaded components (stack size 64, no damage/charge) and absence of the empty
advancedgear resource. The original four recovered sprites decode natively. TURBINE,
DROPPER and DRAGONAPI_AUDIT_CLIENT_PASS retain the first batch's regression coverage.
No corrupt-PNG error or new 4x4 generation failure appears in this client run. A full decode scan
passes all 2179 RotaryCraft PNGs. Current RotaryCraft/ChromatiCraft jars contain neither the
archived obsolete PNG nor temporary reference/test classes. Other missing-texture warnings and
Windows OSHI/Perflib diagnostics remain; this does not claim actual Iris/Distant Horizons/shader
acceptance. N03/N06 are closed in PROJECT-BACKLOG.md; N07/N08 and RC19 remain open.

Git diff whitespace checks pass for the changed source and ledgers. The user's IDE-maintained
.idea/workspace.xml still has its pre-existing/dynamically updated whitespace; it was not edited.
This batch and the earlier first-batch changes remain uncommitted and unpushed.


## 2026-10-08 — RC19: full railgun ammunition/projectile/recipe port

Completed the shared RotaryCraft railgun slice on Minecraft 26.3 / NeoForge 26.3.0.51-beta,
using the current generated Minecraft and cached NeoForge sources. Original parity reference:
RotaryCraft b2288638f7f4703bb4555f72a2b86f72407cc2ae. Full contract/provenance is recorded in
RotaryCraft/docs/railgun-port.md and docs/asset-recovery.md; no active code was excluded for green.

All sixteen tiers preserve original mass, torque and damage, strongest-affordable selection and
successful-spawn consumption. Native persistent/networked ammunition components retain old DAMAGE
compatibility. Slugs and explosive shells are registered moving entities with swept collision,
constant velocity, source muzzle/expiry, native NBT/complex spawn identity and owner attribution.
Original target extension hooks, effect removal, non-player equipment drops, player equipment,
knockback, dragon damage/achievements and pre-terrain impact event are retained. Each entity is
processed once rather than hundreds of times inside the historical terrain loop.

Datagen tags and native SavedData replace legacy block identities/damage metadata for original
terrain families, recursive radius-five breaking, grass/dirt/stone/cobble fatigue and highest-tier
radii. Break authorization, both block-damage gates, loaded chunks, native loot and replacement
invalidation are covered. Turret damage receives generated armor/shield-bypass/projectile tags.
Generated recipes retain original shapes/materials and conditional difficulty yields 16/8/2 with
the original medium fallback for NOOB/UNPLAYABLE: 46 slug definitions, 16 admitted per setting,
plus the original 16-shell recipe. Exact-tier ingredients and synced handbook displays retain
components. DragonAPI's shared display helper also fixes empty cells, choices and cooking input.
Four original sprite groups and eight-plane projectile geometry use current native item/render APIs.

Validation: all 60 required native railgun GameTests pass (build/railgun-gametest.log, 1m 24s).
The final family run passes all 69 unit tests (DragonAPI 25, RotaryCraft 33, ReactorCraft 8,
ChromatiCraft 3), packages both release jars and passes the disposable native client in 1m 50s
(build/railgun-final-validation.log). Client checks cover every typed/legacy tier's actual model,
registered slug/shell geometry with deferred-state reuse, synced component-bearing handbook inputs
and an actual server-created tier-13 projectile's client spawn identity; prior turbine, gear,
dropper and DragonAPI client checks also pass. Final datagen passes in
build/railgun-final-datagen.log. All 2182 RotaryCraft PNGs decode and all seven recovered sprites
match original pixels. build/railgun-artifact-verification.json verifies packaged native classes,
recipes, models/textures and damage tags, and excludes the archived asset/TestInstance/JUnit code.

RC19 is checked off in PROJECT-BACKLOG.md (7 complete, 155 open, 162 total). Actual remote-client
and Iris/Distant Horizons/shader acceptance remain open. The optional unsupported Thaumcraft
void-metal subsystem is preserved in upstream reference, outside this supported slice; railgun
ammunition/target extension contracts remain available. Other missing-texture warnings and Windows
counter diagnostics remain. No ReactorCraft implementation changed in this slice. This batch and
prior backlog changes remain uncommitted and unpushed; user-maintained IDE edits are preserved.


## 2026-10-09 — Separate Play and Automated Checks; Proxima water colour correction

TestInstance now has two permanent native client runs: runClient / TestInstance - Play uses the
existing TestInstance/run saves/config/mods with dragonapi.auditClient=false; runClientAudit /
TestInstance - Automated Checks uses build/dragonapi-client-audit and its own clientAudit argument
files, opens audit-world and exits after checks. Each IntelliJ application run prepares its own
arguments before launching. The old one-off audit init script is retired: it had overwritten the
normal client argument files, leaving the opt-in flag and automatic audit-world launch behind.
Normal play arguments are regenerated and the user confirms the normal client launches correctly.
Existing unrelated IDE settings and saves are preserved. Usage is documented in TestInstance/README.md.

The reported white Proxima puzzle water exposed a colour migration error. EverFluid used the
modern greyscale vanilla water_still texture with a white multiplier and no tint. Its old blue
sprite already supplied colour, so the earlier ledger's claim that grey water matched upstream
was incorrect. Tint slot zero and a registered constant normal-water blue (26.3 vanilla
0x3F76E4) restore colour while preserving source/flow height, corner averaging, face brightness,
two-sided geometry and source-bound flow logic. Structure/Monument biome water likewise used
V33a's white multiplier as an absolute modern colour; their generated water colour now uses the
same native normal-water blue. Remaining Proxima/Rainbow biome colours are retained.

Current validation: final ChromatiCraft server datagen, launcher argument preparation and client
compilation pass (build/proxima-water-datagen.log, 1m 4s). The native client audit subsequently passes (final evidence below).
WaterPuzzleClientChecks exercises actual reloaded source/flow models, all tint-indexed faces,
original geometry/animated material and both synced biome definitions without editing play saves.


## Lua enchantment export — D18, 2026-10-09

ItemStackLuaBlock exports the item registry identifier rather than a translation key. Applied
and stored enchantments are exported separately under their native component names, with sorted
registry identifiers and exact levels (including explicitly empty component maps). Custom data
remains separate and the source stack is never changed. This repairs the missing enchantment
information; it is not a claim that every unrelated item component now has a human Lua adapter.

NBTLuaBlock derives readable enchantment details from old numeric IDs, namespaced list IDs,
modern component maps and historical component maps nested under levels. Its explicit
HolderLookup.Provider overload resolves actual registry names on either server or remote client;
without a provider it prints canonical identities and levels. Numeric vanilla IDs use the
existing source-backed 1.7.10 mapping. Unknown old mod IDs are identified as unknown and retained,
never assigned a guessed modern identity. Applied and stored lists are both handled; malformed
entries and every original field remain available. Parsing disabled means no derived display.

Written map/list containers now carry an explicit type: empty maps/lists, singleton lists,
mixed primitive/record lists and their ordering survive object export. Written lists also emit
anonymous record entries in their original order in Lua text, instead of sorting numeric keys
lexically or moving all records after scalar values. Existing parsed-script inference is retained.

Five focused unit regressions pass. Native integration checks actual registered sword components,
both enchantment kinds, registry ID/count/custom data, source conservation and original numeric-ID
name resolution with an explicit level registry. All 13 DragonAPI native tests pass in
build/lua-enchantment-native.log (46s); the module-family unit results total 74 (30 DragonAPI,
33 RotaryCraft, 8 ReactorCraft, 3 ChromatiCraft). Initial family run:
build/lua-enchantment-units.log; final DragonAPI/Reactor rerun: build/lua-enchantment-native.log.


Final water/run-separation validation: build/proxima-water-client-final.log passes in 1m 43s,
including WATER_PUZZLE_AUDIT_CLIENT_PASS for the actual reloaded source/flow meshes, every face's
tint, original animated sprite/two-sided geometry and the synced Structure/Monument biome colours.
Existing turbine/gear/railgun/dropper/DragonAPI client checks pass too. All five native water-puzzle
layout/lock/flow/structure/descent tests pass (build/proxima-water-gameplay.log, 52s). The first
client fixture compared decoded opaque ARGB against RGB; correcting only that assertion restores
the native check. No production colour or gameplay was weakened to pass the fixture.

The finished family unit results are 74/74 (DragonAPI 30 including five Lua regressions,
RotaryCraft 33, ReactorCraft 8, ChromatiCraft 3). The final DragonAPI unit/jar run is in
build/proxima-water-client-final.log; all 13 native DragonAPI checks, including actual Lua item
components and legacy-name resolution, pass in build/lua-enchantment-native.log. Both changed
module jars contain the required implementation and corrected generated data. Independent checks
confirm audit execution leaves both normal Play argument files unchanged, testing disabled and
no automatic audit-world launch. Report: build/proxima-water-final-verification.json.

IntelliJ refresh created duplicate Play/Automated Checks entries alongside the temporary manual
repair; duplicates are removed while keeping the generated definitions and the user's selected
normal Gradle play run. Task definitions in TestInstance/build.gradle persist the separation.
Restart Play to load the code/resource changes; no new world or save removal is required. D18 is
complete (8 checklist tasks done, 154 open); D19 config version-reset is the next confirmed gap.
Remote-client and actual Iris/Distant Horizons/shader acceptance remain open. Work is uncommitted
and unpushed; user play saves and unrelated IDE edits remain intact.

## 2026-10-09 — D19 configuration format reset and migration preservation

Restored ControlledConfig's original explicit format-ID contract from DragonAPI cc8a1c0b via a
fourth constructor argument. Existing three-argument family callers remain unversioned; no mod
release or Minecraft version is used as an arbitrary reset policy. Initialization and reload now
check the marker before orphan cleanup, validation and saving. Missing/matching markers retain
custom values, while malformed markers abort without silently accepting integer-getter repair.

Resets copy exact primary-file bytes to a new .reset-backup-UUID before clearing any parsed data.
No source or previous backup is deleted, and preservation failures abort before clearing settings.
The first .pre-toml backup remains intact. Additional options restore original defaults rather
than their previous loaded values; format markers survive shared Control categories. Explicit
protected resets regenerate without recursive initialization/spec registration. Fixed segmented
values being overwritten by a second primary-file read. Existing loaded client/synced TOML specs
remain authoritative and their files are untouched. Contract: DragonAPI/docs/controlled-config-migration.md.

Seven focused regressions pass, including exact backups, repeated resets, additional defaults,
safe failures, shared categories, actual parsed TOML/loaded ModConfigSpec and real segmented data.
Family unit suites pass 81/81 (37 + 33 + 8 + 3), and all 13 native DragonAPI tests pass in
build/config-reset-validation.log (1m 45s). Final DragonAPI unit/jar verification passes in
build/config-reset-final.log (20s). D19 is complete: 9 backlog tasks done, 153 open; D20's coherent
block-array APIs are next. Remote-client config and actual shader/LOD acceptance remain open.
Changes remain uncommitted and unpushed; normal Play launch definitions and saves are unchanged.

## 2026-10-09 — D20 block-array API restoration and requested local checkpoint

Completed DragonAPI's deferred block-array cluster against de1bcf1f and current 26.3 sources:
coordinate rotations/mirrors preserve exact declared states, alternatives and placement overrides;
copies/merges/replacements keep membership and representative state consistent without world reads.
Bounds/settings/order and independent MultiKey containers are preserved. The original transform
contract rotates coordinates only, not facing properties or selected NBT. Material tallies retain
real component identities, first-alternative semantics and one item per admitted coordinate without
mutating display stacks. Blueprint import is restored for all horizontal directions, anti-mappings
survive import, and rectangular checks retain source reference conventions without transposition.

Selected-NBT entity checks use explicit registry context and current native save/ValueInput APIs,
require exact state/class/selected-field equality and preserve unrelated destination inventory.
Weak references/class-only prototypes, actual source/flowing fluid family policies, rejection of
waterlogged substitutes and soft/empty checks are complete. Full contract and deliberate source
corrections: DragonAPI/docs/block-array-port.md. Turbine blueprint import remains available through
the restored fillFrom surface rather than an omitted method or fabricated replacement layout.

Four new unit regressions and five new native GameTests pass. Final family units: 85/85
(DragonAPI 41, RotaryCraft 33, ReactorCraft 8, ChromatiCraft 3); all 18 native DragonAPI tests pass
in build/block-array-validation.log (1m 50s). The final isolated client audit passes in
build/block-array-client-final.log (1m 41s), including prior water/turbine/gear/railgun checks.
Normal Play argument hashes remain unchanged. D20 completes the next requested set: 10 tasks done,
152 open; D21's source/caller review is next. D20 and the prior pending batches are included in
the owner's requested local checkpoint, including required datagen outputs that would otherwise
be omitted by the generated-resource ignore rule. Unrelated local IDE settings are retained
outside the checkpoint. Remote-client and actual shader/LOD acceptance remain open.

### DragonAPI recursive breaker and shared sound delivery — D21, 2026-10-09

Reviewed the original de1bcf1f breaker and accepted Excavation Star, Fertility Seed and cliff-stone
callers against generated 26.3 sources. Confirmed property-collection identity is not water
identity and Level.playLocalSound remains empty on the server path. Actual captured fluid state
now selects source/flowing water versus other standalone fluids; waterlogged solids retain their
block sound. Native broadcasts preserve centres, state-aware sound parameters, the initiating
player's delivery, range filtering and NeoForge sound hooks. Nonbreaking paths remain silent.

Repair the same sided problem in the shared block/ambient/entity sound overloads, including the
cliff callback's placement effect and ReactorCraft cooling/extinguish consumers. Client playback
and delay flags remain local; server calls now use native packets. Default-state fallback retains
sounds for removed/not-yet-placed blocks. Full contract: DragonAPI/docs/recursive-breaker-port.md.

Nine new native tests cover real nearby/distant connections, source/flowing water/lava, solids,
waterlogged solids, fourteen helper overloads, silent traversal and hook cancellation/modification.
All 27 native DragonAPI tests and 85 family unit tests pass in build/recursive-breaker-validation.log
(1m 7s). All fifteen accepted wand tests and the cliff transparification test pass in
build/recursive-breaker-wand-callers.log (2m 16s) and build/recursive-breaker-cliff-caller.log (39s).
The isolated client audit passes in build/recursive-breaker-client.log (1m 42s), including the new
local/decoded-server sound-engine check and prior water/turbine/gear/railgun/dropper checks. Normal
Play argument hashes remain unchanged and audit fixtures remain opt-in. This completed batch is
included in the local checkpoint: 11 tasks done, 151 open. Special-tree depth rules remain D22;
remote-client and actual shader/LOD acceptance stay separate open tasks.

### DragonAPI tree-breaking depth policy — D22, 2026-10-09

Restore the original Sequoia 350, Twilight Oak 200, Darkwood 32 and Giant Pink Tree 180
breadth-first wave limits from de1bcf1f. The disabled missing-ModWoodList premise is false:
the current catalogue compiles and retains all four identities. Put the original thirty-wave
default on TreeType and override exactly the four catalogue entries; getTreeBreaker now consumes
the descriptor policy. Existing providers retain their default and modern providers can describe
their limits without forged legacy identity. Spatial bounds, state matching, diagonal spread,
explicit enqueueing and completion behavior remain intact.

Three unit and six native regressions cover every catalogue limit, original bounds, native
30/32/180/200/350-wave stopping boundaries, axis/leaf states, unrelated species and actual supported
glow wood with diagonal leaves and boundary rejection. Optional external mods are not impersonated:
their wave tests delegate the real catalogue policy through vanilla-state descriptors; catalogue
bounds are verified separately. Full contract: DragonAPI/docs/tree-breaker-port.md. Validation
passes in build/tree-depth-validation.log (1m 38s): all 88 family unit tests (44 + 33 + 8 + 3)
and all 33 native DragonAPI tests. All accepted family consumers compile. Normal Play arguments
match the previous checkpoint and automated checks remain disabled there. This completed batch
is included in the local checkpoint: 12 tasks done, 150 open. D23 common-material flammability
review is next; D36 optional wood associations and remote-client/shader acceptance remain open.

### Shared flammability catalogue and actual world states — D23, 2026-10-09

DragonAPI's common-material catalogue now uses native 26.3 fire data plus generated reloadable
note-block/jukebox exceptions from its original de1bcf1f catalogue. The shared world helper passes
the actual state to NeoForge's contextual flammability hook and fallback, so waterlogged wood
remains wet. Original chest/trapdoor exclusions and the UP-face contract remain intact.

Two native ReactorCraft consumer regressions verify every registered material (only graphite
is flammable through its own override) and actual EntityPlasma.onEnterBlock ignition of wood,
wool, note blocks and graphite, with waterlogged slab rejection and original continuation result.
The world-free catalogue does not invent contextual graphite odds. No plasma/material logic was
removed. All 91 family units, 38 native DragonAPI tests and these two consumer tests pass in
build/flammability-validation.log (3m 16s) and build/flammability-reactor-validation.log (38s).
Datagen and packaged-tag checks pass; normal Play arguments remain unchanged with testing off.
Full contract: DragonAPI/docs/block-flammability-port.md. This completed batch is included in the
local checkpoint: 13 tasks done, 149 open. D24 dye identity/reloads is the next backlog task.


### Unknown Artefact client follow-up — 2026-10-09

After the D23 checkpoint, the owner's artefact-zone visual report led to ChromatiCraft host
fixes for native sky brightness, source-texture sampling and world/clock signal isolation,
while retaining the original effect/gameplay as requested. The separate client audit passes
in build/artefact-zone-client.log (1m 44s), including native GPU readbacks and all previous
family client checks. Normal Play arguments remain unchanged and fixtures remain opt-in.
The prior 91 family units / 38 DragonAPI native tests / two ReactorCraft flammability consumers
remain the server validation for D23. Full rendering contract: ChromatiCraft/docs/artefact-zone-rendering.md.
The shared sun-helper climate/Cliffs review is recorded as new D63 (13 done, 150 open, 163 total).
Actual external shader/LOD visual acceptance remains open. The follow-up is locally committed.


### TestInstance ingredient crash / runtime isolation — 2026-10-09

The owner's JEI ingredient crash could not load AdjacencyUpgradeItemRenderer$Args at
15:31:59; the declared nested record was regenerated with the renderer at 15:32:02 during
the shader build. Source and jar contain it, supporting a live-output rebuild race rather
than an absent renderer implementation. Both TestInstance client runs now copy all eight
source mods into a unique verified runtime snapshot before FML discovery, retaining the
same JVM for IntelliJ Debug and a lease through client exit. Copies live outside build/;
idle cleanup cannot remove a leased client. Normal Play keeps auditing off and its game
directory; Audit keeps its disposable world and exits only after its guarded checks.

The native integrated client passes in build/client-snapshot-validation.log (2m 43s),
including 24 adjacency GUI variants and original geometry, renderer/nested-class origins
inside the snapshot, the artefact GPU check and previous family client checks. Three
launcher regressions pass in build/client-snapshot-unit-validation.log, including lazy
nested-class loading after live output deletion and active snapshot retention. Details:
TestInstance/docs/client-runtime-isolation.md. No ReactorCraft gameplay is changed.
Restart/reload normal Play to adopt the launcher; preparing the verified copy adds startup
time. D24 and D63 remain open; backlog counts stay 13 complete / 150 open / 163 total.

### ChromatiCraft rare biome regions — 2026-10-09

The owner's Luminous Cliffs frequency and detached Cliffs/Rainbow Forest patch report is repaired
in ChromatiCraft's active 26.3 slice. Both families have seeded, non-overlapping regions with a
fully admitted 384-block-wide land core and connected outer land. Existing weight options control
rarity; terrain, decorations, rivers/shores and underground caves are retained. Global and buffered
chunk biome resolvers agree, with a world-owned bounded cache and unbounded admission sampler.

Three focused JUnit regressions and the seven-mod natural-world GameTest pass. In the seed-zero
survey, 441 starting positions have nearest-Cliffs boundary distances of 1,678 / 2,858 / 4,094 blocks
(p25 / median / p75), matching the owner's approximate 3k target while allowing lucky close finds.
495 cave and 1,259 ocean/mushroom samples remain intact; old climate mappings cannot leak detached
fragments outside planned regions. Details: ChromatiCraft/PORTING.md; evidence:
ChromatiCraft/build/biome-placement-final.log. Play saves and existing generated chunks are retained.
No ReactorCraft gameplay changes; D63 remains open.


### DragonAPI dye cache and ElectriCraft consumer — D24, 2026-10-09

DragonAPI's empty dye cache is replaced with typed DYE/current colour tags, immutable
item identities, preserved multi-colour membership and original metadata/RGB/name APIs,
and authoritative tag-event rebuilds with the integrated-client guard. ElectriCraft's
disabled resistor branch now uses native held-item interaction with original band geometry,
current arithmetic, server changes, offhand/survival/creative handling and synchronization.
The same obsolete method's motor upgrades retain their original limits and fallback behavior.

All 92 affected-module units (51 DragonAPI, 33 RotaryCraft, 8 ReactorCraft) and all 19 native
ElectriCraft tests pass in build/dye-cache-validation.log (40s). Four new units and six native
fixtures include real tag application/callbacks, registered client event delivery and 56
resistor orientation/band cases. No ReactorCraft gameplay changes. The full resource-pack
reload/remote-session and broader electricity network boundaries remain open. Contract:
DragonAPI/docs/dye-cache-port.md. Normal Play remains audit-off with the stable launcher.
D24 is locally checkpointed: 14 complete / 149 open / 163 total; D25 MIDI consumers are next.


### MIDI and actual music consumers — D25, 2026-10-09

DragonAPI MIDI import now merges channel events chronologically, integrates tempo changes,
handles SMPTE divisions and zero-velocity/FIFO releases, preserves original score units,
program/velocity/percussion contracts and restores functional legacy track/grid queries.
MusicScore channel-chord NBT, independent copies and edit/range/duration caches work.
RotaryCraft disk extraction restores notes/rests; import again clears old music. Keyboard
IDs and all six instrument icons are restored through the native GUI consumer.

Seventeen new unit regressions pass with 109 affected-module units (67 DragonAPI, 34
RotaryCraft, 8 ReactorCraft; build/midi-validation.log, 1m 4s). The separate native client
passes all 49 keys, actual GuiMusic callback routing, original texture/highlight extraction
and existing shader/water/input/resource/model checks (build/midi-client-validation.log,
3m 24s). Normal Play arguments are unchanged. No ReactorCraft gameplay changes; Chroma
music subsystems, audible/music-packet/remote acceptance and D63 remain open. Contract:
DragonAPI/docs/midi-score-port.md. Local checkpoint: 15 complete / 148 open / 163 total;
D26 TileEntityCache subtype validation is next.


## Typed tile caches and unresolved locations — D26/D27, 2026-10-10

Original DragonAPI de1bcf1f cache/location contracts are retained with explicit Class<V>
validation, class/interface support, atomic typed insertion and real NBT subtype filtering.
Untyped location/value maps remain usable; NBT resolution fails before clearing an untyped
cache. Reactor control rods, pebble beds and Chroma crystal-network/pylon caches use actual
tokens. Malformed/missing/wrong-subtype entries cannot escape as a delayed caller-side cast.

Chunk/move/copy identity works without a live level. WorldLocation retains no client Level,
uses actual state properties and safe unresolved/read sentinels, restores loaded-chunk and
sky queries, numeric/current dimension persistence and dimension-aware ordering/squares.
Original face-hit identity and randomized item drops/velocity scaling are restored.

Nine new units pass with all 118 affected-module units (76 DragonAPI, 34 RotaryCraft,
8 ReactorCraft). Four native fixtures pass, including the actual CPU reload consumer,
class/interface filtering, missing dimensions/chunks, state properties, face hits and drops.
Crystal-network compilation passes. Logs: build/cache-location-validation.log (1m 2s),
build/cache-location-native-final.log (40s). Contract: DragonAPI/docs/cache-location-port.md.
No arbitrary LocationEntry serializer, real remote client or full reactor-network acceptance
is claimed. Normal Play is separate. Completed work is locally checkpointed; D28/D29 are next.


## HexGrid size semantics and inverted weighted selection — D28/D29, 2026-10-10

HexGrid's suspected endpoint-size bug is not observable: lerp discards both size fields and
uses the explicit output size. Original cube-space semantics remain, now documented and
covered by mixed-size/reverse/zero-length/adjacency regressions. No physical-grid semantics
are invented; deferred Chroma lore rendering remains open.

InvertedWeightedRandom now uses RandomSource, handles empty and zero-mass pools, rejects
invalid/unrepresentable weights before mutation, selects half-open intervals correctly and
retains append-only duplicate results. Seed/RNG control and clear/reuse are functional.
Original and current sources have no inverted persistence API or project consumers; main
WeightedRandom NBT coverage is explicitly separate. No new persistence format is invented.

Seven new regressions and all 125 affected-module units pass (83 DragonAPI, 34 RotaryCraft,
8 ReactorCraft; build/hex-inverted-validation.log, 1m 4s). Contract and source evidence:
DragonAPI/docs/hex-inverted-review.md. Completed work is locally checkpointed; D30 is next.


## Inventory and resource empty contracts — D30, 2026-10-10

Legacy arrays and native inventories now handle null/EMPTY/zero-count slots consistently.
Transactional full-fit insertion rolls back failure; administrative output writes retain
actual per-item capacities; partial insertion conserves counts/components across slots.
Snapshot-backed Container merges/decrements persist. Closing/draining use EMPTY, spills
clear before spawning and cannot duplicate, and last-block selection retains its identity.
Item Cannon consumption, original capacity gate/sound, target persistence and bottom-only
extraction are restored through native inventories and capabilities. Resource Optional,
IoSupplier and IOException contracts were reviewed and already behave correctly.

Six new native fixtures and all 44 family-loaded DragonAPI cases pass, alongside 127 units
(85 DragonAPI, 34 RotaryCraft, 8 ReactorCraft; build/inventory-family-full-compile.log, 1m 14s).
RotaryCraft-only broad selection requires Chroma for the glow-tree case. A full compile
restored missing accepted Chroma class outputs before family validation. Cannon transfer
is isolated from power acquisition; complete powered-chain/GUI/audio acceptance remains
separate. Details: DragonAPI/docs/inventory-empty-port.md. D17 player-to-chest transfer
remains open; unique-stack counting does not implement it.


## Native multiblock entity contract — D32, 2026-10-10

Removed non-overriding hasTileEntity/createTileEntity hooks after reviewing all six accepted
ReactorCraft casing families. Only turbine ELEMENT hosts an injector; other casings retain
controller lookup without invented local entities. SteamInjector now validates the actual
ELEMENT state, allowing native same-block part changes to remove its obsolete entity and
lubricant capability. FORMED changes preserve the entity and tank, detached NBT retains
contents, and restoring ELEMENT creates fresh storage. Two native regressions and all 127
affected units pass (build/multiblock-contract-final.log, 45s). Native mismatch warning at
variant transition accompanies expected cleanup; full assembly/teardown/drop/hardness
acceptance is separate. Details: DragonAPI/docs/multiblock-entity-contract.md.

D31 remains open: scalar metadata cannot describe actual 26.3 state properties; the accepted
rift has a full-state getter but its shared interface still publishes a fake-zero metadata
contract. Migrate the exported contract and actual callers deliberately, without inventing
global integer state IDs or pretending a zero metadata value preserves variant behavior.


## Replacement states, registrations and accepted client outputs — D33/D52, 2026-10-10

Restored BlockReplaceOnBreak's original abstract replacement contract and explicit nested
self policy using complete native states. The 26.3 player-removal lifecycle retains the
original state/entity/tool for harvesting; no duplicate manual loot path is added. There
are no accepted concrete consumers, so concrete gameplay/drop acceptance accompanies a
future consumer rather than an invented production block. Details: replacement-on-break-port.md.

DragonAPIMod now resolves the real loader container with class/ID/lifecycle/registration
context in failures. One new native family fixture verifies all eight actual registered
IDs and constructed base-mod instances, plus missing-ID diagnostics. The full family
DragonAPI suite passes 45 cases; final affected units pass 127. Logs:
build/registration-family-validation.log and build/final-source-and-launch-validation.log.

An incomplete incremental ChromatiCraft output was reproduced despite successful compileJava.
Root build configuration now compiles its full accepted slice when needed and verifies
primary accepted class outputs before both client preparations. All eight guards pass
(3,251 outputs); a disposable missing-output fixture is rejected without deleting real
classes. Normal Play arguments retain their original SHA256 hashes. The isolated client
audit passes artefact brightness/animation, blue puzzle water, 24 adjacency GUI models
from the immutable snapshot, rendering, music, sound, input and resource checks:
build/night-client-validation.log. D33 was compiled afterward, not claimed as a concrete
client test. Details and acceptance limits: TestInstance/docs/overnight-2026-10-10.md.


## Registered inert entity identities — D54, 2026-10-10

Reviewed all direct and indirect InertEntity subclasses and the active source boundary.
Every accepted consumer already supplies its actual EntityType; the unused world-only
constructor assigning ARROW is removed. Excluded pristine legacy consumers remain intact
and must acquire their own typed registration when fully ported. No universal placeholder
entity type is introduced. Details: DragonAPI/docs/inert-entity-identity.md.

The new native family fixture creates ten accepted inert types from their real registered
factories and checks both runtime identity and native serialized ID. Full family DragonAPI
regressions pass 46 cases, all affected units pass 127, and accepted class outputs pass
across eight modules (build/inert-identity-final-validation.log). This is constructor/save
identity coverage; per-entity gameplay, rendering, payloads and full persistence behavior
retain their own acceptance requirements. The overnight handoff records nine completed
backlog items and remaining priorities; unrelated worldgen/IDE changes remain uncommitted.


## Complete World Rift state contract — D31, 2026-10-10

DragonAPI's exported WorldRift API now requires the complete BlockState available
from the accepted Chroma rift. Block identity derives from that state; the fake-zero
metadata method is removed after confirming no project callers use it. Original
mirrored block/state reads and requested-face tile reads are preserved. Reads resolve
the far world through the owning level and retain current links, properties, actual
air states and null for missing dimensions. The unused zero/no-op coordinate metadata
helpers become real state reads/writes with native results and notification flags.

Two new native fixtures pass for all faces, state changes, relinking/reset, property
preservation, missing dimensions and real Nether targets, alongside original rift
transfer and shaft-power tests. The distant-link fixture's far target overlapped the
next arena; isolated execution passes its unload/link/fluid assertions unchanged.
All 132 affected units pass (87 DragonAPI, 34 RotaryCraft, 8 ReactorCraft, 3 Chroma),
with eight accepted-output guards verifying 3,259 classes. Evidence:
build/rift-state-native-validation.log and build/rift-state-final-validation.log.
Contract: DragonAPI/docs/world-rift-state-port.md. Normal Play argument hashes match;
play saves and unrelated worldgen/IDE work are retained. Changes are uncommitted.
No ReactorCraft gameplay changes or remote/foreign API acceptance are claimed.
Checklist: 25 complete / 138 open / 163 total. Formed-casing hardness is the next
specific review lead in the overnight handoff; D16/D17 and D53 remain open.


## Distant Horizons / Chunky initial-spawn crash — 2026-10-11

The owner's installed DH 3.3.4 + Chunky NeoForge 1.5.4 client crashes during
fresh initial-spawn chunk loads, before Chunky's ServerStartingEvent creates
its provider. DragonAPI now defers only DH's progress-listener setup until
that actual provider is ready, before DH marks the listener as bound. Native
chunk/LOD processing, both mods and the original progress callback remain.
Provider identity changes trigger one rebind; a weak reference avoids retaining
an old integrated world. Unrelated errors remain visible.

The isolated fresh-world integrated client passes both actual startup and
installed-mod lifecycle checks (build/dh-chunky-client-final.log), including
absent/ready/replacement providers, single subscriptions, real progress
delivery and shutdown. The existing graphical audit also passes; the full
client run exits successfully in 3m 39s. All 87 DragonAPI units pass. A separate
optional-mods-absent run passes 46 native DragonAPI tests and three TestInstance
launcher tests (build/dh-chunky-optional-absence-validation.log, 38s), confirming
the optional mixin does not require either mod. DH's headless wrapper
cannot host GameTestServer because it casts to DedicatedServer; that attempt
is recorded and not counted as a pass. The first fresh-save fixture omitted
26.3's separate world-generation file and was corrected before the passing
integrated run. Contract: DragonAPI/docs/dh-chunky-startup-compatibility.md.
Normal Play saves, installed jars, config and pending worldgen/IDE/D31 work
are retained; Play argument hashes match, and default audit arguments are
restored. This repair does not change backlog completion counts or
claim full external shader/LOD acceptance. Changes remain uncommitted.


## Player transfers and formed-casing mining — D17, 2026-10-11

Checked latest working progress: the full-state D31 rift port and installed DH/Chunky
startup repair have successful recorded validation, but remain separate pending changes.
The existing formed casing multiplier inverted the original hardness rule. Native mining
progress now divides by four; every real casing variant retains positive base hardness 2
and the native harvest formula. All three native multiblock regressions pass with bare
hands and an iron pickaxe (build/formed-casing-mining-validation.log).

D17 now implements the original array API's intended transfer operation; the original
body itself never transferred anything. All main/hotbar slots move only accepted amounts,
with component identity, stack limits and exact leftovers retained. Equipment/offhand
remain equipped. A native Container overload honors filters, resource-dependent capacity,
snapshot writeback and dirty notifications; self-targets change nothing and non-owning
threads fail before inventory access. The existing Inventory insertion overload remains.
Contracts: DragonAPI/docs/player-chest-transfer-port.md and formed-casing-mining-port.md.

All 48 family DragonAPI native tests and 135 affected units pass (87 DA + 34 RC + 8 ReC +
6 Chroma). Both strengthened transfer fixtures pass again after equipment/dirty coverage
improvements. Eight accepted-output guards verify 3,262 current primary outputs. Evidence:
build/player-chest-transfer-validation.log and player-chest-transfer-final-validation.log.
Normal Play argument hashes match; saves, installed mods and unrelated working changes
are preserved. No push. Full structure/drop, powered-cannon and remote/menu acceptance
remain separate; D35 is a different base-class contract. The next focused work is D53,
then D16. Checkpoint boundary: TestInstance/docs/continuation-2026-10-11.md.


## Native tick failure policy — D53, 2026-10-11

The shared BlockEntityBase lifecycle no longer swallows broad programming errors
and continues into synchronization, callbacks and machine logic after failed
animation/initialization. Failures reach the real 26.3 LevelChunk ticker: complete
crash report by default, or native entity removal when the owner's existing
NeoForge option enables it. Production code does not alter that option. Existing
caller-local handling remains; writeError logs the complete Throwable with type,
class, position, dimension and logical side before its optional chat display.
Contract and sources: DragonAPI/docs/block-entity-tick-failures.md.

All three new native fixtures pass, including the formerly swallowed five error
classes plus illegal state/linkage errors, both lifecycle hooks, both native
policies, no retry after removal, healthy replacement and explicit handled-error
logging/continuation. The strengthened family suite passes all 51 native tests;
all 135 affected units pass. All 151 registered-machine lifecycle cases pass under
the stricter policy. Eight output guards verify 3,263 accepted primary classes.
Evidence: build/tick-failure-focused-validation.log, tick-failure-family-validation.log
and tick-failure-machine-validation.log. Normal Play argument hashes match.

Only this batch is checkpointed. Other pending rift, worldgen, compatibility,
launcher and IDE work remains separate. Nothing is pushed. Next: D16's block-array
computation threading contract. Boundary: TestInstance/docs/tick-failure-continuation-2026-10-11.md.


## Block array computation and authorized fills — D16, 2026-10-11

Original and ported BlockArrayComputer branches were empty, with no real callers
beyond an unused private instance. Explicit inputs now define coordinate-NBT LOAD,
ordered snapshot ITERATE and full-state FILL using the existing array contracts.
BlockArray.getComputer captures current membership/bounds; no task retains or mutates
its source array. Copied NBT and immutable positions/results permit worker computation
with world-free callbacks. Missing inputs, concurrent execution/configuration and
callback failures report explicitly without a stale successful result.

FILL capture/execution require the owning server thread, loaded chunks, valid bounds,
expected full states and explicit caller actor/claim authorization. Every target is
preflighted before writing; denial, failed authorization and stale preflight change
nothing. Native UPDATE_ALL placement preserves neighbor/client updates and returns
actual changed counts. Explicit server submission works from workers. Later targets
changed by neighbor effects stop with partial-progress context; native world updates
are not transactional and no unsafe rollback is attempted. Contract/source boundary:
DragonAPI/docs/block-array-computer-port.md.

Coordinate NBT again writes original x/y/z, reads both original and affected-port
pos.getX()/pos.getY()/pos.getZ() keys, and derives loaded limits from accepted members
while retaining the persisted legacy limit tag. Six new unit regressions and five
native fixtures pass; the final family suite passes all 56 native tests and 141 units
(93 DA + 34 RC + 8 ReC + 6 Chroma). Eight guards verify 3,264 accepted primary outputs.
Evidence: build/block-array-computer-focused-validation.log and
block-array-computer-family-validation.log. Normal Play argument hashes match.

Only D16 code/tests/notes are checkpointed; pending rift, worldgen, compatibility,
launcher and IDE changes remain separate. Nothing is pushed. Boundary:
TestInstance/docs/block-array-computer-continuation-2026-10-11.md.


## Bounded callback flood-fill crash — 2026-10-11

The supplied server-tick report exhausted the stack in
BlockArray.recursiveAddCallbackWithBounds. Its overflow diagnostic then failed to
bootstrap string concatenation on that exhausted stack, producing a secondary
BootstrapMethodError. The truncated report does not establish the initiating item.
Both bounded callback APIs now share a constant-stack queue traversal, preserving
fixed-origin versus predecessor callbacks, inclusive depth and spread/distance
settings. Explicit/array/native bounds and optional loaded-chunk checks precede
callback reads. Independent visitation handles existing membership and later
admission after an edge rejection. Admission is deterministic breadth-first;
throwOverflow uses parameterized logging for remaining recursive search APIs.

The accepted Subterranean Route Finder depends on minimum height, then a separate
path search, so does not depend on the previous DFS insertion order. A real sneaking
tool fixture traverses a sealed 2,156-cell space and verifies actual start/deepest
trail placement and retained supporting stone. Three native search regressions
include a 20,000-cell line, limits, clamps, callback contracts and exception identity.
All seven selected item fixtures and all 59 DragonAPI native fixtures pass; all
141 units pass (93 DA + 34 RC + 8 ReC + 6 Chroma). Eight accepted-class guards verify
3,264 primary outputs. Logs: build/callback-search-focused-validation.log,
cave-exit-deep-validation.log and callback-search-family-validation.log.

Other recursive block/fluid/spherical APIs and incremental scheduling remain for
separate work. Large connected spaces can still be expensive within one tick.
Contract: DragonAPI/docs/bounded-callback-search-crash.md. Evidence/checkpoint scope:
TestInstance/docs/callback-search-crash-2026-10-11.md. Only this fix, fixtures and
notes are checkpointed; other pending changes are retained. Normal Play argument
hashes match. No live-client or optional-mod claim is made. Nothing is pushed.


## Shared machine-block mining/comparator/menu contract — D35, 2026-10-11

Original BlockTEBase/TileEntityBase source contracts restore contextual no_mine
protection through native mining progress and shared SemiUnbreakable queries.
The saved key remains; initial/delta sync carry it. Changed attached-server flags
require the owning thread and dispatch one native delta, while unchanged setters
dispatch nothing. Native saved loads retain protection; clearing restores the exact
ordinary/configured mining result. The pending Rotary PIPEHARDNESS implementation
is incorporated with native zero/nonzero/live/clamp checks and a protection guard
before instant mining. This supersedes the earlier compile-only pipe validation.

getComparatorOverride again exposes the existing eight-tick server sample. Native
shared block outputs clamp it to 0..15 on every face, with zero for absent tiles.
Changed samples notify ordinary neighbors and native comparators behind a solid
conductor, replacing a duplicate ordinary notification. Existing specialized block
overrides remain authoritative; runtime caches are recomputed after load.

Rotary empty-hand interaction now reaches the common original machine actions/menu
logic with EMPTY, preserving concrete handlers and access/crouch checks. Passive
failure becomes PASS. Native Blast Furnace opening delivers current inventory,
progress and temperature, and subsequent data updates reach the same connection.
NeoForge full-width DataSlot payloads and vanilla packets are observed; menu opening
and passive pipe clicks do not broadcast redundant full tile NBT. Existing menu
slots/data, periodic IO/Fractionator tags and explicit Reactor CPU view payloads
make the removed blanket click sync unnecessary for this shared contract.

All six focused native fixtures and the final 65-test DragonAPI native suite pass;
all 141 units pass (93 DA + 34 RC + 8 ReC + 6 Chroma). Four mod-family consumers,
actual comparator transitions, saved flags, native flag packets, worker rejection,
live pipe settings and real menu connection updates are exercised. Eight guards
verify 3,265 accepted outputs; DA/RC production jars build. Logs:
build/machine-block-focused-validation.log and machine-block-family-validation.log.
Contract/source pin: DragonAPI/docs/machine-block-contract-port.md. Full evidence:
TestInstance/docs/machine-block-contract-2026-10-11.md. Normal Play hashes match.
Native headless mocks are not authenticated remote-client or graphical acceptance.
This batch and the related pending pipe implementation are checkpointed; other
rift/worldgen/compatibility/launcher/IDE changes remain separate. Nothing is pushed.


## Tiered-resource harvesting/loot contract — D34, 2026-10-11

The missing shared player-removal hook is fully restored. The original false
canHarvestBlock is intentional because this hook owns harvesting independently
of tool suitability and must not also run native playerDestroy. Real drops are
chosen before removal, using NeoForge's pre-damage tool snapshot. Successful
server survival removal dispatches one modern BlockDropsEvent with the original
state, owner, tool, mutable items and XP. Cancellation/replacement are honored.
Creative and unowned generic drops remain empty; sufficient-tier resources retain
the original 2–6 XP, including plants whose standalone modern port had lost it.

All 15 accepted Chroma ores and seven plants now inherit the common contract.
Source count/stage/disguise/tuning/Elemental Stone roller rules remain. Generated
reloadable loot tables use a native dynamic entry instead of empty tables plus
unconditional manual drops. Native plant Fortune/Looting survives final-use tool
breakage. Native BlockItem placement is now gated for plants as well as ores, with
server break effects/removal; creative inspection/building and current host-drop
disguises remain. The latter deliberately retains the accepted port's improvement
over upstream's final tier-only drop-spawn check. Actual recursive miners and
world/Silk Touch helpers recognize these registered resources again; empty
sufficient-tier Silk Touch collections are handled safely.

Six new native fixtures, the final 11-test Chroma tiered suite and all 16 survival
fixtures pass. The survival selection also exercises eight pending overworld
fixtures in the working tree, without checkpointing those unrelated sources.
Together with 65 passing DragonAPI native fixtures these give 91 distinct IDs.
All 141 units pass (93 DA + 34 RC + 8 ReC + 6 Chroma); eight class guards verify
3,266 accepted outputs. Server datagen writes 22 loot tables with matching cache
hashes; DA/Chroma production jars build. Normal Play argument hashes are unchanged.
Source contract: DragonAPI/docs/tiered-resource-contract-port.md. Full evidence:
TestInstance/docs/tiered-resource-contract-2026-10-11.md. Validation is native
headless server behavior, not graphical or authenticated remote-client acceptance.
Only this batch's changes are checkpointed; other rift/worldgen/compatibility/
launcher/IDE work remains separate. Nothing is pushed.


## Wood/ore state and item associations — D36, 2026-10-11

WoodSpecies and ModWoodList now provide complete explicit log/leaf/sapling state
sets, native BLOCK_STATE species items, preserved supplied item components and
independent inventory snapshots. Log offsets index actual states; invalid offsets
cannot alias the preferred log. Registration validates conflicts before replacing
immutable reverse maps. Block-only lookup cannot silently choose between species.
The accepted glow tree resolves after deferred registration and retains its authored
never-decaying leaves. Natural-leaf queries require species membership and respect
native persistence for ordinary trees. Shared legacy field names require verified
associations from their modern external ports; no optional mods or IDs are invented.

TreeType defaults preserve existing providers; recursive breaking consumes exact
declared state sets, leaving other variants of the same block. Original depths,
spatial boxes and diagonal spread remain. Reactor radiation now passes the actual
leaf state. Ore state APIs retain common-tag membership, returned stacks are detached,
and native RNG can be supplied. The historical ore damage argument is a saved ordinal:
getByLegacyOrdinal names it; the deprecated old method preserves ordering and bounds.

Five new unit regressions and four new native catalogue fixtures pass. The full
DragonAPI suite passes 69 native tests; family units pass 146 (98/34/8/6). Eight guards
verify 3,268 accepted outputs, and DA/RC/ReC production jars build. The final focused
run verifies all 28 glow-leaf states against actual loaded sapling loot at the original
guaranteed Fortune-X threshold, component-bearing shared variants and supplied items.
Reactor's small caller adaptation is compile-validated, not a new radiation fixture.
Normal Play argument hashes match; headless checks do not establish visual/remote-client
acceptance. Logs: build/catalogue-family-validation.log,
catalogue-final-focused-validation.log and catalogue-radiation-caller-compile.log.
Contract: DragonAPI/docs/wood-ore-catalogue-port.md. Evidence:
TestInstance/docs/wood-ore-catalogue-2026-10-11.md. D37 plank provenance and D38 ore
product/drop/multiple-mapping/reload work remain separate. Other pending rift,
worldgen, compatibility, launcher and IDE changes remain outside this checkpoint.
Nothing is pushed.


## Complete ore/product catalogue — D38, 2026-10-11

ModOreList.getByDrop now recognizes the original declared product tags and retains
its ore-block fallback. Immutable generations preserve every overlapping ore,
product and source/native-name association; singular adapters consistently choose
original enum order. Returned stacks are detached and classification follows item
tag identity regardless of display components or stack count. Product lookup is
the source's declared-product contract, not an invented exhaustive loot/recipe map.

Full, individual and alias-triggered initialization replace the complete generation,
removing stale associations while preserving held immutable views. Alias validation
precedes mutation; synchronized publication and rollback retain a complete catalogue.
Compound common tags, previous flat spellings, unusual original Monazit/Forcicium
names, Certus gem/crystal tags and historical lapis products are retained. Native
TagKey registration and plural lookup APIs are available. The source CHARGEDCERTUS
option regains its original false default; enabled policy is read at rebuild time.

The root TagsUpdatedEvent listener consumes authoritative server/remote item tags
and skips shared integrated-client notifications. Minecraft 26.3 posts server tag
updates before default item components bind, so snapshots hold Item identities and
build stacks only when queried. No incomplete prototypes are created during binding.

Five new units and two native fixtures pass. Final family validation passes 151
units (103/34/8/6), all 71 required native DragonAPI fixtures, eight accepted-class
guards covering 3,271 primary outputs, and DA/RC production jars. Normal Play
argument hashes match the preceding checkpoint. Native reload fixtures restore all
tag bindings; headless event handling does not establish live remote/visual acceptance
or optional-mod parity. Contract: DragonAPI/docs/ore-product-catalogue-port.md.
Evidence: TestInstance/docs/ore-product-catalogue-2026-10-11.md and
build/ore-catalogue-family-validation.log. D37 plank provenance and D39 keyed-stack
matching remain separate. Pending worldgen, rift, startup compatibility, IDE and
launcher changes remain outside this local checkpoint. Nothing is pushed.


## Keyed-stack component matching — D39, 2026-10-11

Reconciled KeyedItemStack against the original metadata/subtype and NBT rules.
Item/Block constructors regain wildcard variants; ordinary wear is ignored by
default and explicitly selectable. Variant and additional native component maps
are independent immutable projections. Potion base identity and custom fields
are separated; declared family subtypes retain their real canonical defaults.
Rotary coil/charged-tool item-owned adapters preserve their original charge-as-
subtype behavior, while Reactor's non-subtype charged tools retain source policy.

Strict collection equality/order remains separate from explicit fuzzy predicates.
Locks, detached copies, exact/count behavior and registry-aware saves are retained;
old records default to ignored wear. Key construction migrates a private copy.
Railgun ammo joins the existing legacy load migration: typed tier wins and obsolete
DAMAGE is removed. ItemMatch derives extra-data policy after excluding subtype/wear
patches. The actual Chroma tool crate regains independent subtype and extra-data
flags, verified through its production counting path for swords and potions.

Final validation passes 156 units (108/34/8/6), all 74 required native DragonAPI
tests, all 60 required railgun regressions, eight accepted-class guards covering
3,274 primary outputs, and DA/RC/ReC/Chroma jars. Normal Play argument hashes match
the preceding checkpoint. Initial unbound-stack unit cases were moved into native
server fixtures; pure units do not rebind registries. Headless checks do not claim
interactive/remote-client or absent optional-mod acceptance.

Source pins and matching contract: DragonAPI/docs/keyed-stack-component-matching.md.
Evidence: TestInstance/docs/keyed-stack-matching-2026-10-11.md and
build/keyed-stack-family-validation.log / keyed-stack-railgun-validation.log.
D40 typed-map accumulation and D41 excluded fuzzy-set consumers remain for full
subsystem ports; D37 still needs authentic plank provenance. Earlier pending
worldgen, rift, compatibility, launcher, IDE and D31 work remains outside this
local checkpoint. Nothing is pushed.


## Chroma Crafter — D40 slice and D41 review, 2026-10-12

Fully ported and registered the formerly excluded Chroma Crafter and its
InventoriedRelayPowered base. Native inventory, transactional fluid capabilities,
reloadable production pool recipes, relay energy, owner policy, focus acceleration,
berry/owner doubling, stored surplus, save/resolve, particles and detached native
render submission preserve the original machine contract. The typed tally uses
ItemHashMap.add. Output blockage conserves resources; a shared tank journal
preserves native fluid component identity through nested cross-face rollback and
save/load. Client prediction retains Manipulator countdown behavior.

Datagen emits the original PYLON creation recipe (24 stands, five runes, three
aura requirements) and original shield-glass connected models, name and loot.
ChromaTiles appends the entry without shifting existing ordinals.
Native pickaxe harvesting follows the original rock material. Unsupported
BuildCraft connection policy remains behind a CHROMA-PORT adapter marker.

Final eight native crafter fixtures pass, along with two pool regressions, 74
DragonAPI native regressions, 156 units (108/34/8/6), eight class guards covering
3,279 primary outputs, server/client datagen and the Chroma jar. A reused block
entity in the expanded fluid test was corrected before its final passing rerun.
Normal Play argument hashes remain unchanged. These are working-tree headless
checks, not interactive/remote/optional-mod or direct 1.7.10-world acceptance.

D41's inspected transporter request sets and aspect recursion guard are
homogeneous; no fuzzy equality substitution is needed there. Other inspected
sets are iterated or use consistent criteria. The ItemMagicRegistry external
KeyedItemStack query overload and optional integrations still need full review.
D40 remains open for other excluded consumers; D41 remains open for the remaining
subsystem/integration audit. D37 still requires authentic plank provenance.

Contract: ChromatiCraft/docs/chroma-crafter-port.md.
Evidence: TestInstance/docs/chroma-crafter-2026-10-12.md and root build/
chroma-crafter-{datagen,family-validation,pool-validation,final-validation}.log.
Earlier pending worldgen, rift, compatibility, launcher, IDE and D31 work is
retained outside this selective local checkpoint. Nothing is pushed.


## Packet pipeline — D43 and Chroma caller follow-up, 2026-10-12

Completed PacketPipeline's native clientbound/serverbound packet construction,
namespaced connection-scoped replies and functional custom codec registration.
Each pipeline and helper share one actual bridge. Incoming handlers bind only
their received native context; nested handlers/failures restore or clear that
scope. Explicit flow/context overloads cover native callers. Custom encoders and
decoders register as bounded native PLAY payloads, use authoritative handlers,
reject duplicate/foreign/late registrations and preserve existing channel IDs,
protocol 2.1.0, legacy types and default byte framing. Original getMinecraftPacket
was active; its original reply implementation was commented out, so this also
completes the previously exposed intended reply API rather than inventing a
claimed original gameplay regression.

Eight new units and four new native fixtures cover default bytes, both directions,
codec execution, malformed/foreign rejection, nesting, exceptions, namespace
isolation and actual NeoForge registration/native stream-codec round trips.
Final checks pass all 78 DragonAPI native tests, eight crafter regressions, 164
units (116/34/8/6), eight class guards covering 3,280 primary outputs and DA/RC jars.
Normal Play hashes remain unchanged and its namespace property does not enable
the new fixture channels. Controlled receive contexts capture replies in tests;
actual remote socket delivery is not claimed. Existing deliberate tick-failure
crash reports remain expected fixture diagnostics, not failing suites.

D41 review corrects the earlier broad FabricationRecipes description: useNBT=false
stores ignore-extra-data keys while query methods construct strict keys. The full
fabrication subsystem port must use explicit recipe matching and preserve useNBT.
Its exported heterogeneous union is iterated by existing NEI callers. Current
ItemMagicRegistry keyed calls pass its own exported keys; stack overload callers
use the registry's declared policy. Pristine excluded classes are retained.
D40/D41 remain open for their full subsystem ports. D44 dormant transport,
D45 tank sender semantics, D46 legacy sub-IDs and remote acceptance remain open.

Contract: DragonAPI/docs/packet-pipeline-port.md.
Evidence: TestInstance/docs/packet-pipeline-2026-10-12.md and root build/
packet-pipeline-{final-validation,final-units,crafter-validation}.log.
The Chroma caller correction is in ChromatiCraft/docs/chroma-crafter-port.md.
Earlier pending worldgen, rift, compatibility, launcher, IDE and D31 work is
preserved outside this selective local checkpoint. Nothing is pushed.


## Native compound batching — D44, 2026-10-12

Completed the selected migration through the existing channel bridge. The
unregistered DragonPayloads early design has no repository senders/registrations;
its incomplete handlers and the former compound code are preserved byte-for-byte
as committed-source references under DragonAPI/docs/reference, outside runtime
source/resource roots. No registered content was excluded to obtain a green build.

CompoundSyncPacket is now a server queue/snapshot API which emits the existing
BE_NBT_SYNC payloads in native PacketDistributor bundles, at most 256 per bundle.
No extra transport/payload registration or wire change is introduced. Channel IDs,
protocol 2.1.0, default frames and initial vanilla full sync are retained. The
COMPOUNDSYNC option is appended without shifting old ordinals, defaults false and
wires the real BlockEntityBase periodic branch. Mode transitions carry baseline/
removal history, force authoritative state and cancel stale queued updates.

Queued tags are detached and compared to the consumed baseline, preserving
coalesced changes/removals/reversions. Failed sends retain pending work; newer
reentrant revisions survive flush. Real tile identities, weak references, loaded
chunks, dimensions, ranges and negotiated channels govern delivery. Per-dimension
4/20-tick clocks avoid duplicate callbacks; unload/stop clear state. Snapshot codecs
preserve signed int coordinates and registry identities, bound counts/sizes and
retain prior state/buffer indices after malformed reads or failed writes. Existing
compound receiver implementations retain a detached adapter. The casting table's
explicit empty output remains, with obsolete removal commentary corrected.

Validation passes all 84 DragonAPI native tests, eight crafter regressions, 170
units (122/34/8/6), final six compound fixtures, eight guards covering 3,280 primary
outputs and DA/RC development jars. Retired classes/reference files are absent
from runtime jars; archived bytes and normal Play argument hashes are verified.
Controlled transport captures are not actual remote socket/render acceptance.

Contract: DragonAPI/docs/compound-sync-port.md.
Evidence: TestInstance/docs/compound-sync-2026-10-12.md and root build/
compound-sync-{family-validation,final-validation,crafter-validation}.log.
D44 is complete. D45 tank sender semantics, D46 legacy sub-IDs, excluded Chroma
subsystem ports and release/remote acceptance remain open. Earlier pending worldgen,
rift, compatibility, launcher, IDE and D31 changes remain outside this selective
local checkpoint. Nothing is pushed.


## Subterranean Route Finder tick budget — 2026-10-12

Fixed the reported server hang/lag in the native item path. Item.use now queues
work instead of completing a synchronous BFS; sneak mode no longer performs a
preliminary region flood fill. A FIFO predecessor search retains shortest sky
routes and deepest reachable points within the original 400-block horizontal
bounds/no-ascent rule, using inclusive 26.3 vertical limits and loaded terrain.
Search, reconstruction, validation and original air-only/every-fourth/final-exit
waypoint placement run on the server thread under one shared 4 ms cooperative
target/1,024-unit tick budget. Placement is capped to sixteen updates per tick.

One pending job per player, four jobs globally and 262,144 discovered positions per
job bound memory/work; oversized searches report failure rather than a purported
deepest result. Reuse replaces pending work; logout/death/dimension changes and
unload/stop release jobs. No chunk load/generation is requested by the finder.
Existing trail traversal now stops at cyclic or unloaded links instead of looping
indefinitely. Status messages are generated by ChromaLang.

All eleven Chroma units (five new route tests) and all four actual-item/native-tick
GameTests pass. Eight guards verify 3,283 primary outputs; Chroma contributes
1,026. Server/client datagen and the final development jar pass; the jar contains
the generated messages. Normal Play argument hashes are unchanged. The reported
save/cave was not used as a performance benchmark. Restart Play to load the fix.
Details: ChromatiCraft/docs/cave-route-finder-fix.md; build/cave-route-*.log.
Validation uses the working tree, including earlier pending ports. This selective
checkpoint preserves earlier worldgen/rift/launcher/IDE/D31 work outside its
commits. D45 and the remaining backlog stay open. Nothing is pushed.


## Explicit tank sender and menu recipients — D45, 2026-10-12

Completed tank synchronization through the existing native channel bridge. Original
source c29c4fcc also left base syncTankData dormant; it is now a real public opt-in
refresh, with no added automatic reflective broadcast on every BE update. The
named-field sender retains the original 24-block same-dimension radius, with an
explicit clientbound PacketTarget overload. Snapshot reads require the owning
server thread and a loaded/live tile. Unbound channels and invalid fields cannot
produce partial frames.

Thirteen accepted RotaryCraft tank menus use CoreContainer's owner-targeted helper
instead of repeating nearby broadcasts per viewer. The reservoir's physical
dedicated-server gate is replaced by logical-server routing; Steam's initial
sendAllDataToRemote path is included. Existing nearby/tool sends and full/periodic
BE tags remain. Pristine excluded Chroma/integration source is retained.

TankSyncData caches inherited instance/subtype field discovery, rejects static,
null and shadowed targets, owns detached snapshots and validates the complete
canonical body. All four active DA/RC/ReC/EC receivers share decoding before
mutation, reject serverbound TANK and only resolve loaded client tiles. Unknown
named fluid IDs cannot fall back to the old fluid. Fill/switch/empty/capacity
semantics and existing same-fluid components survive; deprecated level-only direct
updates cannot invent an empty tank's fluid. Existing protocol 2.1.0, ordinals,
payload IDs and canonical bytes are preserved. New fluid components/type identities
remain a deliberate future wire migration, with component state supplied by BE tags.

Three units, five new native fixtures, all 89 native DragonAPI regressions, all
178 family units and eight crafter regressions pass. A final focused rerun validates
the consolidated receivers. Eight guards cover 3,285 outputs (DA 670 / RC 1,023);
development DA/RC jars build. Normal Play arguments are unchanged. Captured native
transport calls are not actual remote socket/render acceptance.

Contract: DragonAPI/docs/tank-sync-port.md. Evidence:
TestInstance/docs/tank-sync-2026-10-12.md and build/tank-sync-*.log. D45 is complete;
D46 explicit sub-IDs/negotiated schemas and remaining ports/remote acceptance stay
open. Validation includes earlier working-tree changes; this selective checkpoint
preserves worldgen/rift/compatibility/launcher/IDE/D31 work outside its commits.
Nothing is pushed.


## Explicit stable packet identities — D46, 2026-10-12

After committing/pushing all earlier work at root 2073045 and its pinned module
heads, migrated accepted packet discriminators from enum ordinals to explicit
wireId values. Seven tables preserve all 353 published numbers: category types,
DragonAPI controls, private field-sync primitive tags, Rotary/Reactor/Electri
controls and Chroma's complete 212-entry enum. Immutable ID maps reject unknown
values; duplicate/negative tables fail initialization. Chroma no longer clamps a
malformed control into another operation. All old constructor payload arguments
remain unchanged, including counts, type flags and variable-data markers.

The Gradle accepted-source inventory identifies/migrates 101 direct ordinal lines
and the blower's indexed packet control; private sync tags/category encoding use
the same API. Gameplay payload enums remain intact. Excluded pristine consumers
retain source parity and must adopt wireId during their complete subsystem ports.
The actual bridge registrar uses PROTOCOL_VERSION = 2.1.0: no number/frame/schema
change is introduced. Future incompatible IDs/schemas require deliberate version
negotiation; IDs must never be recycled.

Four new units and two native fixtures check scrambled tables, duplicates,
negative/unknown IDs, independent published table fingerprints, all public lookups,
native category round trips and old RAW bytes. All 91 DragonAPI native tests,
182 family units and five packet-selector regressions pass (the latter repeats
the two new fixtures and additionally covers Sonic/Defoliator/packet authority).
Eight guards verify 3,287 outputs; development DA/RC jars build. The final 3,461-file
accepted-source audit finds no packet-discriminator ordinal encoding, and all 353
constructor payloads are checked against the pushed checkpoint. Normal Play hashes
are unchanged. Actual remote socket/render acceptance remains separate.

Contract/mapping: DragonAPI/docs/packet-id-port.md and packet-ids-protocol-2.1.json.
Evidence: TestInstance/docs/packet-id-2026-10-12.md and build/packet-id-*.log/json.
D46 is complete for the accepted port; D47 dimension-field semantics is next. This
continuation is committed locally after the already verified published checkpoint.


## Dimension-scoped entity and biome packets — D47, 2026-10-12

Replaced ignored numeric dimension slots, zero placeholders and ResourceKey hashes
with explicit namespaced DIMENSION keys in six strict RAW packet bodies. The actual
bridge registrar now negotiates protocol 2.2.0; protocol 2.1 peers must update both
ends. All 353 historical category/control/tag numbers stay unchanged. Legacy or
malformed bodies and wrong directions are rejected before gameplay dispatch.

Restored verifyClientEntity and preserved the original existence/class-hash check.
The server ignores requests for a dimension the player has left; failure replies
retain the requested dimension/class, and only a matching client entity is removed.
Server entities cannot be removed through this operation. Player implementations
use the common Player class hash across logical sides; other entities retain exact
class-name hashes. Biome PNG start/data/end
frames retain the captured source dimension even if the completion callback moves
the player. Maps capture the actual source Level/registry and discard stale sessions;
client level unload/logout clear unfinished maps. Existing names, samples, batching,
padding, colors, grid, PNG/CSV exports and callbacks remain.

The formerly ineffective custom BIOMECHANGE client receiver now writes native quart
palettes across all loaded sections, preserves neighbors, clears tint caches and
invalidates renderer sections. It uses a namespaced biome key rather than registry
iteration order and does not generate missing chunks. The authoritative server
helper retains native FillBiomeCommand synchronization with correct inclusive 26.3
height bounds. Excluded pristine 1.7.10 consumers remain untouched.

Three new units and four native fixtures cover schema goldens, custom keys/Unicode,
malformed/old/trailing/truncated frames, entity/class/dimension checks, Nether stream
frames, callback dimension changes, full-height/neighbor palettes and stale map
sessions. The opt-in disposable client fixture covers production request/reply and
client palette application. Normal Play retains its separate arguments/directory.

Contract: DragonAPI/docs/dimension-packet-port.md. Final evidence and acceptance
boundary: TestInstance/docs/dimension-packet-2026-10-12.md. D47 is complete for the
accepted port; D48 EventProfiler listener instrumentation is next. Actual remote
client and Iris/Distant Horizons acceptance remain open.

D47 final validation: 185 family units and all 95 required native DragonAPI tests
pass. Eight guards verify 3,290 primary outputs; development DA/RC jars build. The
disposable integrated client passes native entity request/reply, stale reply guards,
client biome palettes, PNG export and the existing full audit, then exits cleanly
in 3m 20s. Normal Play argument hashes are unchanged. This batch is checkpointed
locally; ongoing IntelliJ workspace.xml changes are preserved outside the checkpoint.


## Donation head renders and personal Play identity — 2026-10-12

At the owner's request, fully ported the original spinning HSLA steel gear,
spinning toroid-magnet head render and ChromatiCraft detached-pylon flare. The
previous gear method omitted its draw; Reactor/Chroma renderer files were excluded
legacy code. They now submit the original model/sprite geometry through native
26.3 item/deferred render paths. Their original translations, animation clocks,
scales, pitch handling and priorities are retained, including RenderMagnet's
internal detached 0.4 scale/0.6 offset and the pylon's separate non-world card branch.
Deferred callbacks capture pose/color data and do not follow a reused player stack.

Client-only load-complete subscriptions restore the original Reika donor + Patreon
registration through one shared PlayerSpecificRenderer helper. Registrations are
idempotent, including duplicate roster entries and developer mirrors. Reika and
Officialy previews use the actual NeoForge production flag rather than interpreting
26.3's unobfuscated classes as a developer environment. Pylon preview construction
is deferred until first rendering, after the block registry is bound; constructing
it while NeoForge discovers subscriber classes failed the first client validation
and was corrected. The excluded legacy client proxies remain pristine/excluded.

Play now uses OfficialyAwsome / bb5029b7-9381-4d99-aaa9-106da41aa659. Mojang's public
session profile confirms that account and signed skin. Passing the default dev
token explicitly enables 26.3's native public skin/profile lookup without storing
account credentials. Automated Checks keeps its test identity unless the explicit
donatorIdentityAudit property is supplied; it uses the separate disposable directory.
The optional officialy_glow mask is missing and has not been replaced with invented
art. The three restored head effects use existing original models/sprites.

Contract/source pins and validation: TestInstance/docs/donator-head-renders-2026-10-12.md.
Logs: build/donator-render-family-validation.log and donator-render-client-validation.log.
The client fixture checks identity/skin, exact registrations, idempotence, native
gear items, deferred toroid/pylon geometry, pose restoration, transparent ordering
and the real third-person render path. Existing family regression/acceptance work
continues independently; D48 EventProfiler remains next on the numbered backlog.

Final head-render validation: 185 family units, all 95 required native DragonAPI
GameTests and eight guards (3,293 accepted primary outputs) pass. The disposable
client uses the requested OfficialyAwsome UUID, loads its downloaded skin, checks
all three original head renders and idempotence, then reaches the real third-person
render path. The existing full audit/PNG export also passes; clean exit in 3m 40s.
Default Play and Automated Checks launch files are regenerated separately after
the opt-in identity test. This continuation is checkpointed locally.

## D48 — EventProfiler actual listener instrumentation, 2026-10-13

Restored automatic per-handler profiling on the real NeoForge game bus using a
native EventBus subclass constructed with NeoForge's actual builder. Original
registration, priorities, cancellation, superclass listeners and exception handling
remain native. Temporary listener wrappers update both listener lists and target
ownership, including registration changes during a session, and are restored when
profiling stops. The adapter is isolated against the actual bus-8.0.5 sources.

Nested dispatch frames replace the overwritten single start timer; actual event
posts replace the incorrect handler-count division. Reports capture call/failure
counts and immutable sorted measurements. Profiling selects an exact concrete event
and the initiating thread; other events/threads still dispatch normally. Times are
inclusive handler times. Common aliases avoid eager client-class loading and reject
ambiguous nested names. The command accepts $ in nested Java class names, supports
console use, and preserves previous results after invalid starts. Legacy explicit
hooks remain balanced and available. No mod lifecycle bus is replaced.

Source contract: DragonAPI/docs/event-profiler-port.md. Native evidence:
TestInstance/docs/event-profiler-2026-10-13.md. The family passes 192 unit regressions
and all 96 required native DragonAPI GameTests. The new native fixture measures a
real chunk mutation, including the existing CrystalNetworker.markChunkCache
subscriber without adding manual timing calls. Eight output guards verify 3,296
accepted primary classes. The separate disposable client passes actual client-event
callback measurement and the complete existing audit, then exits cleanly in 3m 38s.
The owner's Play identity/configuration is preserved. D49 PlayerChunkTracker ownership
review is next. This continuation is checkpointed locally.

## D49 — PlayerChunkTracker ownership and loading tickets, 2026-10-13

Reviewed the original tracker and actual 26.3 ChunkMap/DistanceManager sources.
The former view-update gate left player loading participation active. Expiry's
xo/zo assignments changed interpolation fields rather than chunk-manager markers.
The two maps, tick flag and queue merge were not consistently synchronized, and
entries had no logout/respawn/dimension/unload/server-stop cleanup.

Entries now use server + UUID keys with exact player/level bindings; all conditions,
timers, callbacks and native mutations execute on the owning server thread. Off-thread
requests queue there and capture their source level. Concurrent reads and snapshot
iteration allow reentrant registration without stale removals. Terminal lifecycle
identities are weakly held; late requests and cleanup callbacks cannot resurrect them.
Same-owner renewals retain the original overwrite/reset behavior.

MixinChunkMap also gates native skipPlayer, and registration runs native move to
remove only the selected player's loading participation. Forty false/dead ticks
still expire, a true condition resets grace, and removal notifies once. Expiry/stop
uses native move + updateChunkTracking to restore tickets/view at the current
position, including unmoved players. No interpolation marker is rewritten. Callback
runtime failures are logged and release suspension; restoration runs even if the
removal callback fails. Leaving-world cleanup does not re-add departing tickets.
The original PlayerManager bridge is preserved against native ChunkMap.

Contract: DragonAPI/docs/player-chunk-tracker-port.md. Evidence:
TestInstance/docs/player-chunk-tracker-2026-10-13.md. All 192 family units and 100
required native DragonAPI fixtures pass; eight guards verify 3,297 primary outputs.
Four new native fixtures prove membership/view changes, grace/death/reset, reentrant
callbacks, failure recovery, actual teleport/respawn/logout and worker requests.
The disposable integrated client also passes native movement/view restoration and
actual logout/unload/server-stop cleanup, with exactly one notification for each
retired suspension and zero retained entries. The complete existing audit passes;
clean exit in 3m 31s. Play retains the owner's identity and disabled audit flag.
This batch is checkpointed locally. D50 update-feed verification is next.

## D50 � updater remains inactive, 2026-10-14

The owner explicitly requires the port updater to remain inactive until Reika's
release permission exists. No maintained feed, scheduled fetch or update login
hook is configured. The inherited legacy endpoint remains excluded. D50 stays
open; runtime transitions and notifications are deferred by this instruction.

DragonAPI/docs/update-checker-inactive.md records the restriction and source pin.
UpdateFeedClient is fully written future transport/parser infrastructure with no
runtime callers. Four local-only regressions cover bounded HTTP transport, missing
Last-Modified and malformed/unavailable feeds; failures never become comparison
versions. No release or feed data was published.

## D51 � packaged FILEHASH and native client lifecycle, 2026-10-14

The original ClientLoginEvent depended on an unsent legacy LOGIN packet. A
client-only native LoggingIn/LoggingOut subscriber now raises the original common
events, including automatic FILEMATCH dispatch after connection/reconnection.
The updater remains inactive. Hash registration tolerates parallel mod initialization;
read-only HashCheckEvent reports decisions after enforcement, while cancellation
remains exclusively on the original HashKickEvent. Protocol 2.2.0 and historical
packet IDs/bodies are unchanged.

The opt-in harness loads eight immutable private packaged jars in separate JVMs
and directories. Server binds IPv4 loopback only, with an ephemeral port; Play's
identity, saves and audit-disabled configuration are preserved. Only one inert
META-INF entry differs between DragonAPI variants; every original entry has
identical bytes. Matching archives pass, enabled mismatch disconnects with the
actual reason/hashes, saved-and-synchronized FILEHASH=false permits the mismatch,
and the original event cancels enforcement on the final reconnect.

Final run 20261014-d51-f passed the required packaged GameTest and all four
client/server decisions. State is ALL_PASS with final client acknowledgment PASS.
Server exits cleanly in 4m 22s; matching client in 1m 42s; different client in 1m 41s.
Fixture corrections save config before synchronization, close the actual connection
before reconnecting and enter terminal completion before client teardown.

Contract: DragonAPI/docs/packaged-filehash-port.md. Evidence/rerun commands:
TestInstance/docs/packaged-filehash-2026-10-13.md. All 196 family units and 100
ordinary native DragonAPI GameTests pass; eight output guards verify 3,302 accepted
primary outputs. D51 is complete. D50 remains owner-deferred; mixed-protocol peer
negotiation, authenticated remote hosting and shader/LOD acceptance remain open.

The ordinary disposable integrated client audit also passes and exits cleanly in
3m 56s, including native automatic login dispatch and the complete prior audit.
Actual PlayerChunkTracker logout/unload/server-stop cleanup still passes. Normal
Play retains OfficialyAwsome, bb5029b7-9381-4d99-aaa9-106da41aa659 and auditClient=false.
Evidence: build/packaged-hash-integrated-client-validation.log. Checkpointed locally.

## D55 � obsolete packaged transformers and duplicate mixin config, 2026-10-14

Verified native 26.3 sources, current family consumers and actual baseline jars.
DragonAPI's two SRG field transformers have no current consumers: the old Minecraft
block-renderer field is gone, and ModelPart traversal uses public native visit.
The historical audit's suggested Minecraft.minecraft replacement is also absent.
RotaryCraft's four SRG ShapedRecipe parser transformers target removed methods;
current original-recipe datagen uses ShapedRecipePattern.of and native codecs.
Both unused files and their explicit TOML registrations are removed.

The unused dragonapi.mixin.json is unreferenced in source/build/TOML/manifest,
has empty arrays and names a nonexistent plural package/refmap. It is removed;
the real mixins.dragonapi.json and mixins.rotarycraft.json remain byte-identical
with their metadata entries preserved. All deleted declarations and their intent
are recorded in DragonAPI/docs/resource-ownership-d55.md. ReikaRenderDispatcher
and LODModelPart source references remain available for later full porting.
No accepted Java content, recipes or rendering logic is removed or excluded.

Rebuilt jar inspection confirms no obsolete resources or transformer declarations;
31 DragonAPI mixin entries and RotaryCraft's entry remain packaged. All 196 family
units and 100 required native DragonAPI GameTests pass; eight output guards verify
3,302 primary outputs. Fresh packaged clients/server pass matching, enabled kick
with exact reason, disabled acceptance and original cancellation. Clean exits:
server 3m 16s, matching client 59s, different client 1m 2s. A first socket-abort
attempt is preserved separately and is not counted as acceptance; the unchanged
strict assertions pass on the restarted sequence.

D55 is complete. D56's complete active/optional mixin and distribution matrix
remains open. Play configuration is preserved; updater stays inactive per owner.
Evidence: DragonAPI/docs/resource-ownership-d55.md and build/d55-*.log.
This batch is checkpointed locally.

## D56 — mixin/distribution audit in progress, 2026-10-14

Added an opt-in TestInstance audit that reads each actual config and compiled
Mixin annotation, forces applicable native target definition, and verifies applied
mixin origin/interface evidence in exported transformed bytecode. It rejects
compiled-but-unregistered mixins, duplicate entries and missing evidence. Optional
absence and other-side entries are explicit results. Four configs contain 45 mixin
classes and 46 target bindings; their default required injection checks remain.

DragonAPI now declares JAVA_25 compatibility matching Java 25 outputs. The
population-server mixin was already registered; an extra audit-development entry
was caught and removed. No new population registration is claimed.

The first real packaged dedicated-server boot exposed a common startup reference
to client ColoredAltarEffects/Particle. Common altar payload registration/codec
and server sending now live in ColoredAltarNetwork; client effect handling uses
native RegisterClientPayloadHandlersEvent inside the existing client-only setup.
Payload chromaticraft:altar_visual, registrar version 1, CompoundTag fields,
radius and all particle/shock/bolt/break behavior are preserved. The dedicated
server now starts with no client particles and verifies all 30 applicable common
bindings; two optional targets are absent. It exits cleanly. Client, optional-mod
and native regression matrix work continues; D56 is not complete yet.

## D56 complete — runtime mixin/distribution validation, 2026-10-14

All 45 configured mixin classes (46 target bindings) are verified through actual
native transformation, required-injection checks and exported merged-origin or
accessor-interface evidence. The audit rejects missing registration, duplicates,
missing target evidence and absent targets from installed optional owners.
Dedicated-server scope excludes the 14 client bindings explicitly.

Five fresh packaged client/real-dedicated-server pairs pass: neither optional mod
(30 server / 44 client transformed, two absent); Chunky alone (31 / 45, one absent);
DH alone (31 / 45, one absent); both (32 / 46); and both on the server with installed
Iris/Sodium/DH/Chunky on the client (32 / 46). All peers exit cleanly. Installed
optional jars are unchanged. These are private test copies, not published releases.

DragonAPI declares JAVA_25. The already-registered population mixin is preserved.
The real dedicated boot exposed ChromatiCraft common startup loading client
ColoredAltarEffects/Particle. Common ColoredAltarNetwork now owns the unchanged
altar_visual version-1 codec and server sends; native client-only payload-handler
registration invokes the existing main-thread effects. All fields, recipient radius
and original shock/bolt/break/fragment behavior remain. The source allowlist adds
the complete common network class; no behavior/content is excluded or stubbed.

The source-folder base client passes the full existing audit in 4m 22s. The final
isolated integrated DH/Chunky run passes 46 client and 32 integrated-server bindings,
actual provider/listener lifecycle, callbacks/rebinding/shutdown and the full
existing audit; clean exit in 3m 47s. All 196 family units and 100 native DragonAPI
fixtures pass. Eight guards verify 3,305 accepted primary outputs.

Audit runs have separate directories, test identities and bounded heaps. Loopback
servers have explicit connection/polling settings. Only native warning-only
Continue Launch actions are acknowledged; fatal loading screens fail. Existing
acceptance reports are rejected by paired launch tasks, requiring fresh IDs.
Earlier fixture/native-window failures are preserved separately, not counted as
acceptance. Normal Play keeps its identity and disabled audit flag. Default audit
arguments are restored after optional runs. The updater stays owner-deferred.

Contract, full binding inventory, evidence and rerun commands:
DragonAPI/docs/mixin-runtime-matrix.md. D56 is complete; D57 live ASM/obfuscation
helpers and environment detection are next. Public authentication and visual
shader/LOD acceptance remain separate open tasks. Checkpointed locally.


## D57 — Retire legacy ASM and obfuscation framework (2026-10-14)

Removed ReikaASMHelper, Patcher, ASMException, MissingASMException and
ReikaObfuscationHelper from DragonAPI Java sources. Removed the already-excluded
ChromaASMHandler from ChromatiCraft Java sources. All six files are preserved
byte-for-byte as noncompiled .java.reference documents; the original paths and
SHA-256 digests are in DragonAPI/docs/legacy-asm/manifest.json. No runtime
transformer framework, SRG map, legacy opcode reflection or hardcoded development
probe remains in the accepted slice. No gameplay hook was removed: LuaBlock was
the only nonframework ASM-helper caller; the Chroma handler was never active.

LuaBlock delegates scalar parsing to plain Java LuaValueParser. Numeric hint
widths, integer/long/double/string inference and overflow fallback are preserved.
Boolean hints now work, and unrelated bracket text no longer steals the datatype
match. Malformed annotations retain their text. Two regression tests exercise
actual LuaBlock map export and preservation rather than the parser implementation.

Active recipe-cache and item-diagnostic callers use the real 26.3 FML 12.0.8
FMLEnvironment.isProduction() flag; DragonAPIMod names/logs development mode
accurately. Readable names are not used to detect development. Excluded original
ChromatiCraft reflection/dev callers remain untouched until their full subsystems
are ported; the archive README lists the replacement rules and original active
patch behaviors. Moving reference code does not complete those behavior ports.
Existing accepted Mixins, accessors and TestInstance's useful ASM inspection remain.

Validation: all 198 units pass (DragonAPI 145, RotaryCraft 34, ReactorCraft 8,
ChromatiCraft 11), with no failures/errors/skips. All eight accepted-class guards
pass. Native DragonAPI run passes all 100 required fixtures and the strict common
MixinAudit (30 transformed bindings, two absent optional integrations). Source
snapshot digests match; fresh DragonAPI/ChromatiCraft jars contain neither retired
classes nor reference archives. No stale retired class outputs remain. Evidence:
build/d57-family.log, build/d57-native.log, build/d57-proof.json and
build/mixin-audit/20261014-d57-native/gameTest-GameTestServer.json.

Normal Play and updater inactivity are preserved. Local checkpoint only; unrelated
.idea/workspace.xml edits are excluded. D57 is complete; D58 is next.


## D58a — Command/chat correctness and generated translations (2026-10-14)

D58 remains open. This batch fixes the actual item/coordinate/block chat format
exceptions, readable entity names and the no-op writeLocalString. The latter now
sends an intact translatable component through a client-only, player-null-safe
writeComponent path. ClientEnvironment explicitly marks nullable player/level/
resource returns and logs version-reflection failure with its cause.

Entity-list invalid-side and console/client failures use failure feedback and
return zero; headers, guide custom name, modular-logger state/error feedback and
biome-map success/error wrappers are translatable components. A native client
language provider emits 17 entries, preserving all six original language entries.
The duplicate manual language resource is removed after comparing generated values.
The produced jar contains exactly one en_us resource matching all generated keys.

Biome-map player/argument routing now uses a typed MapTarget record, replacing
Object[] and unchecked casts. Removed unused private start-time argument and
unused imports. Map failure logging includes transfer hash, dimension and throwable.
Packet decode/handling errors include type/packet and cause in one logger event,
without a separate stderr stack trace. Wire IDs, payload formats, map sampling,
permissions and callback behavior are preserved.

Validation: native client datagen and all 198 family units pass with zero failures,
errors or skips. Native run passes 101 required fixtures and strict common mixin
audit (30 transformed bindings; two optional integrations absent), clean exit in
1m 38s. New/expanded regressions cover original formatting failures, invalid-side
return status, console/client rejection, lazy guide creation retaining its key and
console biome target argument offset. All accepted-class guards pass. Evidence:
build/d58-command-family.log, build/d58-command-native.log and
build/d58-command-proof.json. Localized visual presentation was not exercised by
this server test. Normal Play and inactive updater remain unchanged.

Remaining D58 work: profiler/pregeneration and other public command translations,
biome validation detail translations, remaining raw/generic types and null
contracts, and contextual exception logging throughout accepted libraries. No
claim that D58 as a whole is finished. Local checkpoint; unrelated IDE edits kept.


## D58b — Profiler and bounded-population feedback (2026-10-16)

Profiler and pregeneration command feedback use generated translation components.
All six profiler error states and five population states have keys; 40 language
entries now generate. Profiler errors send failure feedback and return zero.
Numeric diagnostics use Locale.ROOT. Handler count/time/call/failure details remain
in both chat and contextual logs. Population status uses immutable typed snapshots;
the historical mutable diagnostic-string list API and original text are preserved.
No admission, queue, persistence, permissions or population control behavior changed.

Validated on the owner's existing local NeoForge 26.3.0.76 setting: 201 family units
(147 DragonAPI, 34 RotaryCraft, 8 ReactorCraft, 12 ChromatiCraft), zero failures,
errors or skips; 101 native fixtures and strict common MixinAudit pass, clean exit
in 1m 14s. Two new unit tests cover immutable snapshots, state transitions and
feeding/save/reload recovery. Native commands cover missing profiler arguments,
invalid event return status and population start/status/pause/resume/cancel.
Evidence: build/d58b-family.log, build/d58b-native.log, build/d58b-proof.json.
D58 remains open. Existing IDE, version, datagen-provider and other mod changes
are preserved outside this scoped checkpoint. Updater stays inactive. Next:
reflection correctness, types/null contracts and contextual failure handling.


## D58c — Java 25 reflection correctness (2026-10-16)

Private primitive readers now handle static fields, inherited fields and restore
access on failed reads. Historical null/sentinel behavior remains; failures use
contextual logger events with causes, and debug chat tolerates an uninitialized
config. Nullable lookup returns and optional selectors are explicit.

Replaced obsolete Field.modifiers mutation with the actual Java 25 Field.set
contract from installed JDK 25.0.2 sources: ordinary non-static final fields are
writable for reconstruction before publication, without changing final modifiers.
Access override is restored after success and failure. Static/record/hidden final
writes fail clearly; there is no Unsafe or JVM-internals fallback. Missing fields
report NoSuchFieldException. Parent constructors are no longer falsely returned
as inherited constructors; inherited ordinary method lookup is preserved.

Invocation caches are ClassValue-owned concurrent maps keyed by method name AND
immutable parameter types. Exact runtime-type convenience calls retain their API;
a new explicit-signature overload supports primitive/interface/nullable arguments.
Different overloads no longer reuse the first cached method. Failures retain null
results with actual invocation causes logged. No legacy ASM/SRG machinery returns.

Six regressions cover field inheritance/static/sentinels, constructor ownership,
reconstruction and access restoration on failure, prohibited final writes,
concurrent alternating overloads, and primitive/interface/null arguments.
All 207 family units and 101 native fixtures pass on the existing local 26.3.0.76
setting, with strict common MixinAudit. Evidence: build/d58c-reflection.log,
build/d58c-family-native.log and build/d58c-proof.json. D58 remains open.
The prior D58b ledger count is corrected to 201: the owner's newer ChromatiCraft
suite contains 12 tests. Existing version/datagen/IDE edits stay outside commits.


## D58d — Command failure handling and validation translations (2026-10-16)

/getuuid resolves its entity before scheduling feedback, so console use produces
the normal checked Brigadier syntax error rather than a wrapped RuntimeException.
/debugtest now requires administrator permission and supports authorized console
feedback; rejected sources cannot toggle the global flag. Feedback is translated.
Biome range/resolution/grid/seed validation details use stable reason keys and
nested translatable components, including malformed-number feedback. Limits and
seed/map algorithms are preserved. The Jade temperature row keeps its server
value as a translation argument; all 50 language entries come from datagen.

207 units pass with no failures/errors/skips; 101 native fixtures and common
MixinAudit pass, clean exit in 1m 1s. Native tests check entity/console UUID paths,
permission rejection without debug mutation, authorized console toggle (restored
in finally), and invalid numeric/grid map arguments. The temperature test checks
exactly one translated row with the server's numeric argument, and DC engines
still have none. Its prior hardcoded-English assertion failed as expected during
migration and was replaced; the rerun passes. Fresh jar has 50 entries.
Evidence: build/d58d-family.log (initial tooltip expectation), build/d58d-native.log
(final passing checks) and build/d58d-proof.json. D58 stays open; no updater/feed
behavior or normal Play setup changed. Existing owner edits remain untouched.
