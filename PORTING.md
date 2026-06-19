# ReactorCraft → NeoForge 26.2 port

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
2. `registry/` — DeferredRegisters + the enum registries. **`ReactorTiles` ↔ RC `MachineRegistry`**
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
| ReactorCraft base (1.7.10) | Re-root onto | Notes |
|---|---|---|
| `TileEntityReactorBase extends TileEntityRegistryBase<ReactorTiles>` | DragonAPI `base/BlockEntityBase` | `TileEntityRegistryBase` does NOT exist in port; RC's `RotaryCraftBlockEntity extends BlockEntityBase`. Key off `ReactorTiles` (TileEnum). |
| `ReactorRenderBase extends TileEntityRenderBase` | DragonAPI `base/BlockEntityRenderBase` | mirror RC `RotaryTERenderer`; submit() pattern |
| `ReactorItemBase extends Item implements IndexedItemSprites` | RC item base / plain `Item` | `IndexedItemSprites` not located — find RC item-icon convention or drop |
| `ReactorGuiBase extends GuiContainer` | `AbstractContainerScreen` | GuiGraphics drawing (STATUS.md) |
| `ReactorStructureBase extends StructureBase` | DragonAPI `instantiable/data/blockstruct/*` (`MultiBlockBlueprint`?) | locate exact base |
| `BlockReCMultiBlock extends BlockMultiBlock<Boolean>` | locate DragonAPI multiblock base | not found by name — may be renamed |
| interfaces `RenderFetcher`, `Transducerable` | locate in DragonAPI | not found by name — verify/port |

`TileEnum` and `BlockEntityBase` confirmed present. Others marked "locate" need a grep pass
(may have been renamed during the DragonAPI port) or a small port of the missing base.

## REVISED STRATEGY: no thin slice exists — the TE layer is a monolithic knot
Investigated and confirmed: `TileEntityReactorBase` (the fat god-base, 361 lines) does
`this instanceof TileEntityReactorBoiler/SolenoidMagnet/ReactorGenerator/TurbineCore/SteamLine/
HeatPipe` inside its own heat-conduction/transducer methods, so it cannot compile without those
6 concrete TEs. And `ReactorTiles` (the registry enum, `extends TileEnum`) references **every**
machine TE. So base ⇄ registry ⇄ all-TEs are mutually dependent — you cannot compile one machine
without effectively the whole `base/` + `tileentities/**` + registry + `entities/EntityNeutron`
+ the typed interfaces (`ReactorTyped`/`Temperatured`/`TypedReactorCoreTE`) cluster.

**Implication:** port that cluster as one focused batch, not file-by-file-green. Recommended:
1. Resolve the cross-cutting bases/interfaces first: re-root `TileEntityReactorBase` onto
   `BlockEntityBase`; confirm port locations of `RenderFetcher`, `Transducerable`, `ShaftMachine`,
   `ThermalMachine`, `TemperatureTE`, `Variables` (RC api / `interfaces.blockentity`).
2. Gate OpenComputers (`li.cil.oc.*`, e.g. `Visibility` in `getTransducerVisibility`) the same way
   as ChromatiCraft — it's an optional dep not in this build. Use `@ModDependent`/strip or remove.
3. Python-assisted mechanical bulk across the cluster: `World→Level`, `NBTTagCompound→CompoundTag`,
   `ForgeDirection→Direction`, `MathHelper→Mth`, `(x,y,z)→BlockPos`, the `interfaces.tileentity→
   interfaces.blockentity` remap — THEN manual fixup of method semantics against RC TEs.
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

### In progress — TE cluster (edited, NOT yet in allowlist until cluster greens)
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
  if needed); `TileEntityLine.getTexture()` returns `ResourceLocation`; `EntityNeutron(Level,BlockPos,
  Direction,NeutronType)` ctor; `Proportionality` has no NBT methods (serialize manually).

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
- Systematic remap also seen here: **`reika.dragonapi.libraries.world.* → libraries.level.*`**.

### Deferred — need datagen / coupled (NOT leaves)
- `auxiliary/PoisonGasDamage.java`, `auxiliary/RadiationDamage.java` — extend DragonAPI
  `CustomStringDamageSource`, whose ctor is now `(Holder<DamageType>, String)`; the old overridable
  `DamageSource` booleans (`isFireDamage`/`isProjectile`/`getHungerDamage`/`setDamageBypassesArmor`…)
  are gone (data-driven `DamageType`). Port these alongside `DamageType` JSON datagen — mirror how
  RotaryCraft built its `MachineDamage` damage sources.

### FIXED: nested-class import lowercasing (from the reorg script)
The lowercase pass wrongly lowercased the *container* of nested-class imports (it only preserved
the final segment's case). E.g. `EntityNeutron.NeutronType` became `entityneutron.NeutronType`.
A resolver pass against the real files fixed all 12 `reika.reactorcraft.*` cases (incl.
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
+ the submit pipeline) rather than the TE handing back a render object. Resolve this before the
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
+ `(Level,BlockPos,Direction,NeutronType)` gameplay; block-interaction API `isOpaqueCube`→
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
registered via `RotaryEntities` `DeferredRegister<EntityType<?>>` + `EntityType.Builder.of(Ctor,
MobCategory.MISC).build(ResourceKey)` with ctor `(EntityType<? extends Entity>, Level)`. Entity-layer
sub-project order: (1) harden DragonAPI `InertEntity` — add `InertEntity(EntityType<?>, Level)` ctor
(it currently hardcodes `EntityTypes.ARROW`, so every subclass mis-registers); (2) port
`ParticleEntity` onto it (motion→`setDeltaMovement`, `onUpdate`→`tick`, `onEnterBlock(Level,BlockPos)`,
spawnLocation via `WorldLocation` for NBT + raw ints for the spawn buffer; drop the `CubeDirections`
ctor — not ported); (3) port `EntityNeutron`/`EntityPlasma`/`EntityRadiation`/`EntityFusion`
(block-interaction API: `isOpaqueCube`→`isSolidRender`, `getExplosionResistance`,
`getLightOpacity`→`getLightBlock`, `hasTileEntity`→`getBlockEntity != null`); (4) register them in
`ReactorEntities`; (5) renders later. Then `ReactorType`→`ReactorTiles`→TEs unblock.

**Unblocked alternative work in ReactorCraft** (no ParticleEntity dependency): the item layer
(`ReactorItemBase`, `ItemReactorTool`, `items/**`), `registry/ReactorItems`/`ReactorBlocks` for
non-TE blocks, and auxiliary support that only touches `InertEntity`-based `EntityRadiation`.

## Content layer (the remaining bulk) + the item-variant decision
The entity/radiation subsystem + TE base hierarchy are done. What remains is the
content/registration layer, all rooted at the **`ReactorCraft` main class** (everything
references `MODID`/`radiation`/`fusionDamage`/`instance`), so the cluster can't enter the
allowlist until a large connected chunk lands together:
- `ReactorCraft` main (35 KB @Mod) — bus-register the DeferredRegisters (incl. `ReactorEntities.ENTITIES`),
  define the `radiation` MobEffect (Holder) + `fusionDamage` DamageSource + `MODID`/`instance`.
- registries: `ReactorItems`, `ReactorBlocks`, `ReactorTiles` (TE registry → BlockEntityType), plus
  `ReactorOptions` (config), `ReactorSounds`, `ReactorAchievements`, `ReactorFuel`, `RadiationShield`,
  `FluoriteTypes`, `WorkingFluid` — all are the 1.7.10 DragonAPI enum-registry style and must be
  **rewritten to DeferredRegister** (like `ReactorEntities` was).
- the 47 concrete TEs under `tileentities/**` (now have a ported base hierarchy to extend).
- blocks/, items/, container/→Menu, guis/→Screen, renders/ (submit), recipes → **datagen**.

### DECISION — item metadata variants → `getDamageValue()` carrier
The RC port registers ONE `DeferredItem` per item; there is no `getStackOfMetadata`. ReactorCraft's
metadata-variant items (FUEL burnup, PLUTONIUM stages, **WASTE ≈1000 isotopes**, fluorite colours)
**carry the variant in `ItemStack.getDamageValue()/setDamageValue()`** (single registered item +
int variant). Already used this way in `TileEntityWasteUnit` + `EntityNeutron` (committed). Rationale:
~1000 isotopes can't be separate items; a data-component is cleaner but a larger refactor — revisit
if the damage/durability overload bites. `getStackOfMetadata(m)` → `stack.setDamageValue(m)`;
`getItemDamage()` → `getDamageValue()`; `getNumberMetadatas()` → a constant per item in `ReactorItems`.

## ChromatiCraft (optional integration, 10 files)
Imports neutralized now. Usage sites (mostly `WorldRift`, `ChromatiAPI`, adjacency-upgrade,
`CrystalElement`) are gated/removed when each owning file is ported. ChromatiCraft is not in
this build (commented in `settings.gradle`).
