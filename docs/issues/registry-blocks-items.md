# Audit: `registry/`, `blocks/`, `items/` (allowlisted / ported code)

Scope: every file in these three packages that is currently on the `build.gradle` compile
allowlist (see PORTING.md). Compared line-by-line against the 1.7.10 original via
`git show origin/master:<Path>`. Excludes the `reactor_mat`/`MatBlocks` split (done), the
damage-value item-variant decision (WASTE/FUEL-burnup/PLUTONIUM-stage/fluorite-colour — deliberate),
un-ported files, and the mod-wide missing-block-tags (P1) / steel-texture-fallback (P2) issues
already tracked in `README.md`. The registry layer is mostly faithful (fluids, sounds, options,
reactor-type math, crafting-item list all match the original verbatim), but the item-variant
refactor introduced one severe identity bug, several silently-wrong variant counts, and a
systematic loss of `Item.Properties` (stack size, durability, equippable) that the code comments
claim were applied but never are.

---

### [P1] Uranium and plutonium fuel rods collapsed into the same registered `Item`
- **File:** `registry/ReactorItems.java:80-82, 119-120`
- **Problem:** `FUEL_ROD` is one `DeferredItem<ItemReactorFuel>` (line 80). Both `ItemRef FUEL`
  (line 119) and `ItemRef PLUTONIUM` (line 120) wrap that *same* `FUEL_ROD` instance —
  `ref(FUEL_ROD, 16)` and `ref(FUEL_ROD, 8)` respectively — so `ReactorItems.FUEL.getItemInstance()`
  and `ReactorItems.PLUTONIUM.getItemInstance()` return the identical `Item` object.
- **Why wrong:** This is **not** the excluded "variants should be separate items" case. In 1.7.10,
  uranium fuel (`ItemReactorFuel`) and plutonium fuel (`ItemPlutonium`) were two distinct registered
  items (`git show origin/master:Registry/ReactorItems.java:60` — `PLUTONIUM(96, "item.plutonium",
  ItemPlutonium.class)` vs `FUEL(1, "item.fuel", ItemReactorFuel.class)`). The damage-value scheme
  is meant to carry *burnup stage within each fuel type*, not to distinguish uranium from plutonium.
  Concrete breakage in already-allowlisted consumers:
  - `tileentities/fission/TileEntityFuelRod.java:62-66` — `is.getItem() == ReactorItems.FUEL
    .getItemInstance()` and the following `is.getItem() == ReactorItems.PLUTONIUM.getItemInstance()`
    test the same `Item`, so the second branch is unreachable (or the first is, depending on order) —
    the TE cannot actually tell uranium rods from plutonium rods.
  - `tileentities/fission/breeder/TileEntityBreederCore.java:93,115,142` — breeder output/detection
    keys off `ReactorItems.PLUTONIUM.getItemInstance()`, which is now indistinguishable from a plain
    uranium fuel rod, so breeding logic misidentifies its own product.
  - `registry/ReactorFuel.java:29,64-68` — `itemMap` is a `HashMap<Item, ReactorFuel>`; the static
    initializer puts `URANIUM` then `PLUTONIUM` under the same key, so `PLUTONIUM` silently
    overwrites `URANIUM`. `ReactorFuel.getFrom(ItemStack)` can now never return `URANIUM` — every
    fuel rod in the world is treated as plutonium (wrong fission/consume/waste/temperature rates).
- **Fix:** Register a separate `PLUTONIUM_ROD` `DeferredItem<ItemReactorFuel>` (mirrors the original
  `ItemPlutonium`, which is itself not ported — plain `ItemReactorFuel` is a reasonable modern stand-in)
  and point `ItemRef PLUTONIUM` at it instead of `FUEL_ROD`.
- **Size:** M

---

### [P2] Fuel/pellet/magnet variant counts don't match the original — and don't even match the registered item's own `dataValues`
- **File:** `registry/ReactorItems.java:80-82, 116, 119-124, 131`
- **Problem:** The `ItemRef` variant counts are transcribed wrong, and separately don't agree with
  the `dataValues` baked into the underlying item constructor:
  - `FUEL = ref(FUEL_ROD, 16)` — original `ReactorItems.getNumberMetadatas()` case `FUEL: return 100`.
  - `PLUTONIUM = ref(FUEL_ROD, 8)` — original `PLUTONIUM: return 100` (and see the P1 above — should
    not share `FUEL_ROD` at all).
  - `PELLET = ref(FUEL_PELLET, 8)` but `FUEL_PELLET = reg("fuel_pellet", () -> new
    ItemReactorFuel(itemProperties(), 1))` — the item itself is built with `dataValues=1`, while the
    `ItemRef` claims 8; original is `PELLET: return 25`.
  - `BREEDERFUEL = ref(BREEDER_FUEL, 8)`, item built with `dataValues=1`; original `BREEDERFUEL:
    return 20`.
  - `MAGNET_ITEM = new ItemReactorMulti(itemProperties(), 4)` and `MAGNET = ref(MAGNET_ITEM, 4)`;
    original `MAGNET: return 8`.
- **Why wrong:** These counts are live-consumed, not dead data. `guis/GuiReactorBook.java:193`
  (`(nanoTime...)%ReactorItems.MAGNET.getNumberMetadatas()`), `tileentities/htgr/
  TileEntityPebbleBed.java:186-188` (`is.getDamageValue() == ReactorItems.PELLET
  .getNumberMetadatas()-1`), and `tileentities/fission/breeder/TileEntityBreederCore.java:114`
  (`dmg == ReactorItems.BREEDERFUEL.getNumberMetadatas()-1`) all gate "is this fuel fully spent/
  charged" on the wrong threshold — pellets convert to depleted at 8/25 of their real burnup,
  breeder fuel "completes" at 8/20, and toroid magnets cap their charge display at 4/8 stages.
- **Fix:** Correct the `ItemRef` variant counts to 100/100/25/20/8 respectively, and make each
  underlying item's own `dataValues` argument agree with the `ItemRef` (currently `FUEL_PELLET`/
  `BREEDER_FUEL` are built with `dataValues=1`, contradicting their own `ItemRef`).
- **Size:** S

---

### [P2] Block hardness/resistance wrong for every ore, machine, mat, and pipe/line block
- **File:** `registry/ReactorBlocks.java:66-67` (`ore()`), `registry/ReactorBlocks.java:90-91`
  (`machineProperties()`), `blocks/BlockReactorMachine.java:48` (`super(properties.strength(4, 15))`)
- **Problem:** Three block families all get the wrong destroy-time/blast-resistance:
  - **Ores** (`PITCHBLENDE_ORE` … `THORIUM_ORE`, all `FLUORITE_ORE` colours): `ore()` sets
    `strength(3.0F, 5.0F)`. Original `BlockReactorOre` ctor: `setHardness(2)`/`setResistance(5)` —
    hardness is 50% too high. `BlockFluoriteOre extends BlockFluorite`, whose ctor is
    `setHardness(1.2F)`/`setResistance(4F)` — both wrong for fluorite ore specifically.
  - **Machines** (every `BlockReactorMachine`/`Modelled` in `ReactorBlocks.java:147-192`):
    `machineProperties()` gives `strength(4.0F, 15.0F)`, and `BlockReactorMachine`'s constructor
    (line 48) *hardcodes* `properties.strength(4, 15)` again regardless of what's passed in. Original
    `BlockReactorTile` ctor: `setHardness(2F)`/`setResistance(10F)`.
  - **Mat blocks** (`CONCRETE`…`GRAPHITE`, via `ReactorBlocks.java:129`): also routed through
    `machineProperties()` (4.0/15.0). Original `BlockReactorMat` ctor: `setHardness(1.5F)`/
    `setResistance(10F)`.
  - **Ducts/lines** (`BlockReactorDuct`, `BlockReactorLine`, e.g. `GASPIPE`/`STEAMLINE`/`HEATPIPE`):
    these extend `BlockReactorMachineModelled` → `BlockReactorMachine`, so they inherit the same
    hardcoded 4.0/15.0. Original `BlockDuct`: `setHardness(clamp(PIPEHARDNESS config, 0, 1))`/
    `setResistance(1F)`; `BlockSteamLine`: `setHardness(0F)`/`setResistance(1F)`. Pipes/lines are
    supposed to be nearly instant-break; instead they are as tough as a full machine block.
- **Why wrong:** Every block in the mod is measurably harder/more blast-resistant to break than the
  original (ore blocks: 3.0 vs 2.0 hardness; machines/mats: 4.0/15.0 vs 2.0/10.0 or 1.5/10.0;
  pipes/lines: 4.0/15.0 vs ~0/1). Because `BlockReactorMachine`'s constructor unconditionally
  overwrites the passed-in properties, no caller can fix this by changing `machineProperties()`
  alone — the override in the block constructor must also go.
- **Fix:** Change `ore()` to `strength(2.0F, 5.0F)`; change `machineProperties()` to
  `strength(2.0F, 10.0F)` and remove the `.strength(4, 15)` override in `BlockReactorMachine`'s
  constructor (let the caller's properties through unmodified); give `BlockReactorMat` its own
  `matProperties()` at 1.5F/10F instead of reusing `machineProperties()`; give `BlockReactorDuct`/
  `BlockReactorLine` their own low-hardness properties (~0-1F/1F) instead of inheriting the machine
  default. Per-ore harvest-tool tiers (pitchblende=1, cadmium/indium/silver/thorium=2, calcite=0,
  etc., from the original `ReactorOres` enum) are a separate, larger gap — cross-reference the
  mod-wide missing-block-tags P1 already in `README.md` rather than refiling here.
- **Size:** M

---

### [P2] Item `Properties` (stack size, durability, equippable, craft remainder) documented as "done at registration" but never actually applied
- **File:** `registry/ReactorItems.java:42-47` (`itemProperties()`); comments claiming otherwise in
  `items/ItemHeavyBucket.java:14-15`, `items/ItemRadiationGoggles.java:14-15`,
  `items/ItemCanister.java:14-16`, `base/ItemReactorTool.java:12-13`
- **Problem:** `ReactorItems.itemProperties()` only sets the registry id (`p.setId(k)`) — nothing
  else. But the class javadocs on the tool-family items assert the missing behaviour was moved to
  registration:
  - `ItemReactorTool` (base of `ItemHeavyBucket`, `ItemRadiationGoggles`, `ItemCanister`): "The
    1.7.10 `setMaxStackSize(1)`/`setMaxDamage(0)`/canRepair=false are now expressed on the item
    `Properties` at registration" — grep of `ReactorItems.java` shows no `.stacksTo(1)` call
    anywhere; every tool item is registered with the default stack size (64).
  - `ItemRadiationGoggles`: "the 1.7.10 `isValidArmor(stack,0,e)` (helmet slot) is now the
    `EQUIPPABLE` data component (HEAD) applied to the Properties at registration" —
    `GOGGLES_ITEM = reg("radiation_goggles", () -> new ItemRadiationGoggles(itemProperties()))`
    (`ReactorItems.java:117`) never sets `EQUIPPABLE`; the item cannot be worn in the head slot at
    all. (No allowlisted consumer reads `GOGGLES` yet, so this is currently latent rather than an
    active regression — still worth fixing before radiation-protection logic is wired up.)
  - `ItemCanister`: "the 1.7.10 self crafting-remainder... is a registration/recipe-level concern" —
    `CANISTER = reg("canister", () -> new ItemCanister(itemProperties(), 1))` sets no
    `craftRemainder`.
- **Why wrong:** The comments assert a design decision that was never implemented, which is worse
  than no comment at all — it reads as done and will not be caught by a green compile.
  `ItemHeavyBucket`/`ItemCanister`/`ItemRadiationGoggles` can currently stack past 1 in an inventory,
  contradicting both the 1.7.10 behaviour and the port's own stated intent.
- **Fix:** Give `ReactorItems.itemProperties()` a tool-specific variant (or add an overload taking
  `stacksTo(1)`), and apply it to `CANISTER`, `GOGGLES_ITEM`; add `.component(DataComponents
  .EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)...build())` for the goggles; add
  `.craftRemainder(...)` for the canister per the recipe design. Fix `ItemHeavyBucket` at the same
  time since it needs the stack-size treatment too (see next entry — it isn't registered at all yet).
- **Size:** S

---

### [P2] `ItemHeavyBucket` exists and is allowlisted but is never registered
- **File:** `items/ItemHeavyBucket.java` (whole file); `registry/ReactorItems.java` (no reference)
- **Problem:** `ItemHeavyBucket` compiles and is on the build allowlist, but no `DeferredItem` for
  it exists anywhere in `ReactorItems.java`. Grepping the whole `src/main/java` tree for
  `ItemHeavyBucket` finds only its own class file — zero consumers, zero registration.
- **Why wrong:** The original `Items/ItemHeavyBucket.java` is a real content item (heavy-water
  bucket, `setContainerItem(Items.bucket)`). Its port-side class exists and presumably compiles
  clean, but currently no player can ever obtain a heavy-water bucket — the class is dead weight
  that looks "ported" (on the allowlist) but contributes nothing in-game.
- **Fix:** Register it in `ReactorItems.java` (e.g. `HEAVY_BUCKET = reg("heavy_water_bucket", () ->
  new ItemHeavyBucket(itemProperties().stacksTo(1).craftRemainder(Items.BUCKET)))`) and wire it into
  the relevant fill/empty interactions once those are ported.
- **Size:** S

---

### [P3] `WASTE_ITEM` and `REACTOR_BOOK` are registered as plain vanilla `Item`, silently dropping all original behaviour
- **File:** `registry/ReactorItems.java:114-115`
- **Problem:** `WASTE_ITEM = reg("waste", () -> new Item(itemProperties()))` and `REACTOR_BOOK =
  reg("reactor_book", () -> new Item(itemProperties()))` register bare vanilla items. The
  originals (`ItemNuclearWaste`, `ItemReactorBook`; neither allowlisted yet) have entity-tied
  behaviour: `ItemNuclearWaste.onUpdate` applies `RadiationIntensity.HIGHLEVEL` poisoning to any
  entity holding/near it and grants `ReactorAchievements.HOLDWASTE`; it also spawns a custom
  `EntityNuclearWaste` when dropped, and adds isotope/half-life tooltip info. `ItemReactorBook`
  opens a GUI on right-click.
- **Why wrong:** Not a bug introduced by careless registry code — it's the natural, currently
  correct stand-in until those classes are ported. But it is easy to forget once `waste`/
  `reactor_book` "compile green" and are in players' hands: right now picking up nuclear waste is
  completely harmless, and the book item does nothing. Flagging so the done/not-done signal isn't
  lost (per the project's "no stubs" rule, this pair is functionally the same as a stub, just
  achieved by using `Item` directly instead of leaving a TODO).
- **Fix:** No action needed until `ItemNuclearWaste`/`ItemReactorBook` are ported — at that point
  swap the registration to the real classes. Left here as a tracked reminder, not an isolated bug.
- **Size:** S (when its dependencies land)

---

### [P3] `FUEL_BURNUP`/`WASTE_ISOTOPE` data components registered but never consumed; contradicts their own doc comments
- **File:** `registry/ReactorDataComponents.java:29-39`; contradicting comments at
  `registry/ReactorItems.java:77, 86`
- **Problem:** `FUEL_BURNUP` and `WASTE_ISOTOPE` `DataComponentType`s are registered, and
  `ReactorItems.java` comments assert "burnup carried by the FUEL_BURNUP data component" (line 77)
  and waste isotope "formerly the WASTE item metadata" pointing at `WASTE_ISOTOPE`. In reality,
  every actual consumer (`tileentities/fission/breeder/TileEntityBreederCore.java`,
  `tileentities/htgr/TileEntityPebbleBed.java`, `tileentities/processing/
  TileEntityWasteDecayer.java`) reads/writes `ItemStack.getDamageValue()`/`setDamageValue()`
  directly — the damage-value scheme, not these components. Only `CANISTER_FLUID` (also declared in
  this file) is actually used, in `auxiliary/ReactorStacks.java:56`.
- **Why wrong:** Dead registrations with misleading documentation; a future contributor reading the
  `ReactorItems.java` comments would look for `FUEL_BURNUP`/`WASTE_ISOTOPE` usages that don't exist.
- **Fix:** Either wire fuel burnup/waste isotope through these components (matching the stated
  design and diverging from the damage-value approach used elsewhere), or delete the two unused
  components and correct the comments to describe the damage-value scheme that is actually in use.
- **Size:** S

---

### [P3] `ReactorTiles.isPipe()` includes `WASTEPIPE`, which the original excluded
- **File:** `registry/ReactorTiles.java:210-212`
- **Problem:** Ported: `isPipe() { return this == GASPIPE || this == MAGNETPIPE || this ==
  WASTEPIPE; }`. Original (`git show origin/master:Registry/ReactorTiles.java:380-382`):
  `isPipe() { return this == GASPIPE || this == MAGNETPIPE; }` — `WASTEPIPE` was deliberately not
  included.
- **Why wrong:** No allowlisted code currently calls `ReactorTiles.isPipe()` (its only caller,
  `blocks/BlockReactorTile.java`, is unported 1.7.10), so this is latent rather than active
  breakage. But whoever wires up the render/GUI logic that consumes `isPipe()` next (per the
  original's use in `hasRender()`-gating) will silently get different behavior for waste pipes than
  the original had.
- **Fix:** Drop `|| this == WASTEPIPE` unless a deliberate behavior change is intended (if so,
  document why).
- **Size:** S

---

### [P3] `BlockReactorMachine.hasVerticalPlacement` is a dead public field
- **File:** `blocks/BlockReactorMachine.java:43, 59`
- **Problem:** `public boolean hasVerticalPlacement = false;` is read in `getStateForPlacement`
  (line 59) to choose between vertical and horizontal facing, but nothing in the allowlisted
  codebase ever sets it to `true` — every machine places with horizontal-only facing regardless of
  intent.
- **Why wrong:** Looks like a per-subclass configuration point but is currently unreachable dead
  code; misleading to future maintainers who might assume some machine already opts into vertical
  placement.
- **Fix:** Either wire it for the machines that need vertical placement (e.g. anything read via
  look-vector in the original TEs) or remove the field and the branch until a real consumer exists.
- **Size:** S

---

### [P3] `BlockThoriumFuel.canOverwrite` detects pipes by class-name string matching instead of block identity
- **File:** `blocks/BlockThoriumFuel.java:63-67`
- **Problem:** `String n = b.getClass().getSimpleName().toLowerCase(Locale.ENGLISH); return
  n.contains("duct") || n.contains("conduit") || n.contains("cable") || n.contains("pipe");` — a
  fragile reflection/string heuristic, acknowledged by its own `MOD-PORT` comment. Original
  (`git show origin/master:Blocks/BlockThoriumFuel.java:252`): `if (b == BlockRegistry.PIPING
  .getBlockInstance()) return true;` — a direct block-identity check.
- **Why wrong:** Any block whose class name happens to contain "pipe"/"duct"/"cable"/"conduit" (from
  this mod or another) will be treated as overwritable by molten LiFBe fuel, and any legitimate pipe
  block whose class doesn't match those substrings will incorrectly block the fuel dump. Silent,
  data-dependent behavior instead of an explicit allow-list.
- **Fix:** Replace the string heuristic with an explicit block/tag check (e.g. a `common:pipe` tag,
  or direct references to the known pipe block constants) once the relevant pipe blocks are
  identified.
- **Size:** S

---

## Summary

- **P1:** 1
- **P2:** 4
- **P3:** 5

**Top 3 by impact:**
1. **Uranium/plutonium fuel-rod identity collapse** (`registry/ReactorItems.java:80-82,119-120`) —
   breaks fuel-type discrimination across `TileEntityFuelRod`, `TileEntityBreederCore`, and
   `ReactorFuel.getFrom()`; the mod's central fission mechanic silently treats all fuel as one type.
2. **Block hardness/resistance wrong across every ore/machine/mat/pipe block**
   (`registry/ReactorBlocks.java:66-67,90-91`, `blocks/BlockReactorMachine.java:48`) — every block
   in the mod is harder to break than 1.7.10, and pipes/lines (meant to be near-instant-break) are
   as tough as a full machine due to a hardcoded constructor override.
3. **Fuel/pellet/magnet variant counts wrong and self-inconsistent**
   (`registry/ReactorItems.java:80-82,116,119-124,131`) — live-consumed by `TileEntityPebbleBed`,
   `TileEntityBreederCore`, and `GuiReactorBook`, so depletion/conversion/charge-display thresholds
   are all off by a large margin (e.g. pellets convert to depleted at 8/25 of their real burnup).
