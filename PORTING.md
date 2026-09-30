# ReactorCraft → NeoForge 26.2 port

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

## REVISED STRATEGY: no thin slice exists — the TE layer is a monolithic knot

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
AE recipe. Found on the way: `KeyedItemStack` equality never matched (METADATA fell through to false).

**Not portable / deferred:** RotaryCraft `OreForcer` certus (AE2 has no certus ore) and
`fixAEPowerRatio` (AE2 has no RotaryCraft power unit); Bundled Bus (needs ProjectRed); ChromatiCraft
Pattern Cache + Pattern Crystal (V33a WIP: no recipe/research, the crystal's pattern was `null`) and
Remote Terminal (commented out in V33a). ChromatiCraft files whose AE hooks arrive with the file
itself (not yet accepted): CastingAutomationSystem, Casting Auto/Injector, Router Hub, Item Inserter,
Bulk Mover, ChromaticEventManager, Crystal Furnace/Ore Creator (charged certus ore - AE2 has none).

**Tests:** `RotaryAETests` / `ChromaAETests` build real ME networks and only register with AE2 loaded;
drop the AE2 + GuideME jars into `<mod>/run-gametest/mods` to run them (`-PgameTestSelector=<mod>:ae2_*`),
then remove them so the normal suite stays lean.
