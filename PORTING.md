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
