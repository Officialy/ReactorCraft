# Audit: `data/` datagen providers + generated resources

Scope: the `data/` providers, `src/generated/`, and hand-written resources, compared against the
1.7.10 original (`git show origin/master:ReactorRecipes.java`, `.../Registry/ReactorOres.java`).
The registry/blocks/items and reactor_mat work is done (`ff01669`, `1ffa7f6`); the display-name lang
gap is fixed (`3f5e889`). What remains is one mod-wide P1 (block tags) plus recipe/model/loot gaps —
several of which the in-code comments wrongly call "deferred/blocked" but are actually unblocked now.

---

### [P1] No block `TagsProvider` — every `requiresCorrectToolForDrops()` block is undiggable-for-drops
- **File:** `data/` (no `BlockTagsProvider` exists); `data/ReactorDataProviders.java` (never registers one)
- **Problem:** ReactorCraft's shared property builders `ore()`, `fluoriteOreBlock()`, `machineProperties()`,
  `matProperties()` (`registry/ReactorBlocks.java:65-92`) all call `.requiresCorrectToolForDrops()`, but
  datagen emits **zero** `tags/block/**`. `runServerData` output has no `mineable/*` tags at all.
- **Why wrong:** in vanilla/NeoForge a `requiresCorrectToolForDrops()` block that is in no `mineable/*`
  tag is never "correctly harvested" by ANY tool → it drops nothing in survival for every ore, mat,
  machine, and multiblock casing. (RotaryCraft/GeoStrata blocks never call
  `requiresCorrectToolForDrops()`, so they need no tags — this requirement is ReactorCraft-port-specific,
  and there is **no sibling TagsProvider to mirror**; write one from vanilla `BlockTagsProvider`.)
- **Fix:** add a `BlockTagsProvider` registered in `ReactorDataProviders` (Server). Put every
  tool-required block in `minecraft:mineable/pickaxe` (tool is always pickaxe) + the tier tag from the
  original `ReactorOres` harvest levels (0→no tier, 1→`needs_stone_tool`, 2→`needs_iron_tool`):
  - **Ores** — pitchblende 1, end_pitchblende 1, cadmium 2, indium 2, silver 2, ammonium 1, calcite 0,
    magnetite 1, thorium 2; **all 8 fluorite ores** 0.
  - **Mat blocks** (concrete/slag/calcite_block/scrubber/lodestone_block/graphite_block) + CORIUMFLOWING
    — pickaxe, `needs_stone_tool` (metal/rock mat tier; conservative).
  - **Machines + multiblock casings** (all `machineProperties()` blocks) — pickaxe, `needs_iron_tool`.
  - **Exclude** ducts/lines (GASPIPE/MAGNETPIPE/WASTEPIPE/STEAMLINE/HEATPIPE — no tool requirement, break
    by hand) and THORIUM_FUEL/STEAM (`noLootTable`, unobtainable). No tags needed.
- **Size:** M

---

### [P2] ~40 machine-block crafting recipes missing — stale "deferred" comment, actually unblocked
- **File:** `data/ReactorRecipeProvider.java` (the `crossMod`/comment block ~line 35-52, 147)
- **Problem:** The provider's comment says the processor/centrifuge and other machine-block crafting
  recipes are "deferred with their (not-yet-ported) `BlockReactorTile` blocks — those result items don't
  exist yet." That is **stale**: every machine block (`ReactorBlocks.FUEL/CONTROL/…/HEATPIPE`, ~46) is
  registered now. The original `ReactorRecipes.addMachines()` has a crafting recipe for each.
- **Why wrong:** none of the ~40 machines can be crafted in survival — they exist only via the creative
  tab. Ingredients (RotaryCraft `ItemStacks.basepanel`, `steelingot`, `steelblock`, etc. and ReactorCraft
  `CraftingItems`) are all registered, so the recipes are writable now.
- **Fix:** port `addMachines()` as datagen shaped recipes (transcribe verbatim from
  `git show origin/master:ReactorRecipes.java`). Update the stale comment. Large but mechanical.
- **Size:** L

### [P2] Crafting-component recipes missing (items exist, recipes don't)
- **File:** `data/ReactorRecipeProvider.java`
- **Problem:** `CraftingItems` MAGNETIC, MAGNETCORE, HYSTERESIS, HYSTERESISRING, WIRE, COOLANT, FABRIC,
  CARBIDEFLAKES, CARBIDE, TURBCORE, BACKING, ALLOY(partial) are registered items but have no datagen
  recipe; the original `ReactorRecipes` crafts each.
- **Why wrong:** these intermediates are uncraftable, which also blocks the machine recipes above that
  consume them.
- **Fix:** add the shaped/shapeless recipes from the original. **Size:** M

### [P2] Mat blocks fall back to `block/steel` despite real textures existing
- **File:** `data/ReactorModelProvider.java` (`BLOCK_TEX` map)
- **Problem:** `BLOCK_TEX` wires only graphite_block/calcite_block/lodestone_block; **concrete, slag,
  scrubber** fall through to `cube_all` + `block/steel` even though real art ships at
  `textures/block/mat/concrete.png`, `mat/slag.png`, `mat/scrubber_0..5.png`. Scrubber is also
  multi-sided (`_0.._5`) and its `isMultiSidedTexture()` is ignored.
- **Fix:** map concrete→`mat/concrete`, slag→`mat/slag`, and give scrubber a multi-sided model from the
  `mat/scrubber_*` sprites. **Size:** S–M

### [P2] 13 BER machines get a flat steel-cube inventory icon instead of their 3D BER icon
- **File:** `data/ReactorModelProvider.java` (`MACHINE_ITEM_MODELS` set)
- **Problem:** 21 `BlockReactorMachineModelled` blocks exist but only 9 are in `MACHINE_ITEM_MODELS`
  (control_rod, toroid_magnet, solenoid_magnet, steam_grate, condenser, turbine_core, waste_storage,
  electrolyzer, solar_exchanger). The other ~13 — heavy_pump, isotope_centrifuge, uranium_processor,
  reactor_pump, heat_exchanger, gas_collector, turbine_flywheel, reactor_generator, fusion_marker,
  high_pressure_turbine, steam_diffuser, solar_top, mini_turbine — get a plain steel-cube item icon.
- **Why wrong:** inconsistent, unrecognisable inventory icons for half the modelled machines (in-world
  BER rendering is fine — this is only the flat item icon).
- **Fix:** add the remaining Modelled machines to `MACHINE_ITEM_MODELS` so their item icon routes through
  `ReactorMachineItemRenderer`. **Size:** M

### [P2] Ammonium ore loot drops only the dust, not the netherrack byproduct
- **File:** `data/ReactorLootProvider.java`
- **Problem:** original AMMONIUM drop is `makeListFrom(ReactorStacks.ammonium, new ItemStack(Blocks.netherrack))`
  (ammonium dust **+ netherrack**); the port drops only `ammonium_dust`.
- **Fix:** add a netherrack drop to the ammonium ore loot table. **Size:** S

---

### [P3] `ReactorMachineRecipeProvider` literal JSON can silently drift from the codecs
- **File:** `data/ReactorMachineRecipeProvider.java`
- **Problem:** processor/centrifuge recipes are hand-written JSON matching the `ProcessorRecipe`/
  `CentrifugeRecipe` `MapCodec` field layout. Verified currently valid (datagen runs green; ids/amounts/
  serializer ids all match), so **not an active bug** — but any rename/optionality change to the codec
  fields won't be caught at compile time.
- **Fix:** (optional) build these via the recipe objects behind a datagen-safe `FluidStack` shim, or add
  a test asserting the JSON round-trips through the codec. **Size:** S

### Deferred / out of scope (noted, not actionable now)
- **Multiblock recipes** (heater/injector/solenoid/generator/turbine/flywheel sub-parts) — genuinely
  blocked on the multi-meta structure items not yet ported. Leave until those land.
- **Fluorite lamp** recipe — the lamp block isn't registered at all (missing content, not a datagen bug).
- **Mat-block crafting** (CALCITE/GRAPHITE/LODESTONE/SCRUBBER/CONCRETE shaped recipes) — part of the
  recipe gap above; fold into the crafting-component/misc recipe pass.

### Already resolved (do not re-file)
- **Display-name lang** — `3f5e889` replaced the reflective prettify with the original `en_US.lang`
  names (incl. the old "Co2 Heater"/"Reactor Cpu" polish). Lang for fluids/achievements/handbook
  categories and multiblock sub-parts is still unwired, but those systems aren't registered yet.

---

## Resolution status
- **[P1] Block tags** — FIXED `6d7d3f3` (ReactorBlockTagsProvider).
- **[P2] Mat textures / scrubber multi-side** — FIXED `abbba1c`.
- **[P2] BER item icons** — DONE. The machine renderer port completed (`ae8840e`, `9e55904`,
  `0e2802d`); every modelled machine has a registered BER and its item icon routes through it.
- **[P2] Ammonium loot** — FIXED `6d7d3f3`.
- **[P2] Component + mat-block recipes** — FIXED `9a5ec2f` (14 recipes, each diffed vs origin).
- **[P2] machine-block crafting recipes** — PARTIAL `11eea98`. Triaged all ~44 addMachines() recipes;
  the 12 whose ingredients fully resolve are done (gas_duct, magnetic_pipe, waste_pipe, heat_pipe,
  toroid_magnet, fusion_heater, fusion_injector, neutron_absorber ×2, neutron_reflector,
  reactor_generator, fusion_marker). The remaining ~32 are **ingredient-gated on unported RotaryCraft
  items** — `basepanel` (~30 recipes), `pipe` (~15), plus prop/shaftitem/pcb/gearunit/silumin/bedrock
  ingot/igniter/cooling-fin/gearbox-parts. Each unblocks when its ingredient is registered; re-run the
  triage then. Pipe output counts (`DifficultyEffects.PIPECRAFT`) were fixed at 8 pending a config
  decision.
- **[P3] Literal-JSON drift** — open (low priority; not an active bug).

## Summary (original)
- **P1:** 1  (block tags — mod-wide, blocks survival harvesting of everything)
- **P2:** 5  (machine recipes, component recipes, mat textures, BER item icons, ammonium loot)
- **P3:** 1  (+ deferred/out-of-scope notes)

**Top 3 by impact:**
1. **No block TagsProvider** — every tool-required block drops nothing in survival. Mod-wide P1.
2. **~40 machine-block crafting recipes missing** (stale "deferred" comment) — machines uncraftable
   despite all blocks + ingredients existing now.
3. **Crafting-component recipes missing** — intermediates uncraftable, which also blocks the machine
   recipes that consume them.
