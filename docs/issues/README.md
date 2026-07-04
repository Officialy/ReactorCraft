# ReactorCraft port — issue tracker

Working issue list for the ReactorCraft → NeoForge 26.2 port. Companion to `PORTING.md`
(which tracks the file-by-file port status). This directory holds the **defect / cleanup**
backlog: things that compile-or-don't but are wrong, unfaithful, incomplete, or messy.

Priority: **P1** = broken / dead feature / crash. **P2** = correctness or faithfulness gap vs the
1.7.10 original. **P3** = code quality / cleanup.

## Files
- `registry-blocks-items.md` — registry/blocks/items audit (done; all findings fixed in `ff01669`).
- `datagen.md` — datagen provider audit (1 P1 block-tags, 5 P2, 1 P3). Open.
- `slop-comments.md` — module-wide catalog of AI-slop / port-narration comments to REMOVE or TRIM
  (~56 REMOVE, ~69 TRIM). Complete except the `blocks/` and `registry`/`base`/`api` packages
  (sweep cut off by session limit — re-run those two).
- *(pending)* `te-cluster.md`, `renders-gui.md` — two audit passes still to run (see "Audits to resume").

---

## DONE
- **[P2] Split `reactor_mat` into six per-variant blocks** — `1ffa7f6`. Was one block with an
  `EnumProperty<MatBlocks> VARIANT`; now six registered blocks + a `BlockReactorMat` per variant.
  Also removed the three zero-consumer duplicate storage blocks. Compiles + datagen green.
- **registry/blocks/items audit — all 10 findings fixed** — `ff01669`. See
  `registry-blocks-items.md` for detail: uranium/plutonium item split (P1), variant counts
  (P2), block hardness/resistance restored (P2), single-stack/equippable/remainder item
  properties (P2), `ItemHeavyBucket` registered (P2), dead components/field removed, `isPipe()`
  and `BlockThoriumFuel` pipe-detection corrected (P3). Compiles + datagen + test green.
- **Datagen display-name lang** — `3f5e889`. `ReactorLang` was reflectively prettifying registry ids
  (Fuel Rod / Reactor Cpu / Slag / Thorium Ore …); now maps every id to the original `en_US.lang`
  name (Fuel Core / Central Control / Corium / Thorite …).
- **Datagen findings — most fixed** — block harvest tags P1 (`6d7d3f3`), mat textures + scrubber
  multi-side + fusion_marker icon (`abbba1c`), ammonium netherrack drop (`6d7d3f3`), mat-block +
  crafting-component recipes (`9a5ec2f`, 14 recipes). Still open in `datagen.md`: the ~40 machine-block
  crafting recipes (deferred — ingredient-gated, needs per-recipe origin diff), ~12 BER item icons
  (gated on the render port), and the literal-JSON drift (P3).

---

## CONFIRMED OPEN ISSUES (verified this session)

### [P1] No `mineable/pickaxe` (or tier) block tags anywhere — mod-wide
- **Where:** no block-`TagsProvider` exists in `data/`; datagen emits zero tag JSON.
- **Why wrong:** every block built with `requiresCorrectToolForDrops()` (all ores, storage blocks,
  the six mat blocks, and every `machineProperties()` machine) has no `minecraft:mineable/pickaxe`
  or tool-tier tag, so in survival they **cannot be harvested and drop nothing**. Creative-only.
- **Fix:** add a `BlockTagsProvider` (mirror RotaryCraft's if it has one) wiring every RC block into
  `mineable/pickaxe` + the right `needs_*_tool` tier tag; register it in `ReactorDataProviders.Server`.
- **Size:** M

### [P2] Mat / machine blocks fall back to the `block/steel` texture
- **Where:** `data/ReactorModelProvider.java` (reflective `cube_all`, texture map).
- **Why wrong:** concrete/slag/calcite/scrubber/lodestone/graphite and many machines have no mapped
  texture so they render as steel cubes; `MatBlocks.isMultiSidedTexture()` (SCRUBBER) is not honoured
  — the scrubber gets plain `cube_all` instead of a mesh/multi-sided model.
- **Fix:** wire the real 1.7.10 mat/machine sprites into the model provider's texture map; special-case
  SCRUBBER (and any `cube_column`/oriented block).
- **Size:** M

### [P3] Dead `null` platform fields in EntityNeutron
- **Where:** `entities/EntityNeutron.java:49-50` — `botaniaPlatform`/`ttPlatform` are `private static
  final Block ... = null` with no real consumer (guarded by always-false checks).
- **Fix:** remove the fields + their dead branches (and the accompanying MOD-PORT comments).
- **Size:** S

### [P1] TE cluster does not compile (~2527 errors) — tracked, in progress
- Fully documented in `PORTING.md` ("In progress — TE cluster batch"). ~46 machine TEs + base are
  mechanically remapped but bodies still hold 1.7.10 API (old fluid-handler sigs, int-coord
  `getBlockEntity`, `getSizeInventory`, `tank.getLevel()`, `inv[`, `FluidRegistry`). Excluded from the
  build allowlist so the module still builds. **Not re-enumerated here** — see PORTING.md.

---

## Audits to resume (still to run)

Read-only Sonnet audits; each writes `docs/issues/<name>.md`. Scope: audit **allowlisted/ported** code
only; don't flag known-unported 1.7.10 files; don't flag deliberate damage-value variant items —
WASTE/FUEL/PLUTONIUM/fluorite; don't re-enumerate the TE-cluster compile errors, summarize+count instead.

1. **te-cluster** — `base/` + `tileentities/**`: per-TE port-status checklist, compile-blocker pattern
   counts, and **semantic** bugs in already-ported base classes (diff vs `origin/master:Base/*`).
2. **renders-gui** — `renders/`, `models/`, `container/`, `guis/`, `client/`: stubbed/dead renderers,
   `Modelled` blocks with no registered BER, atlas-size/texture-path bugs, missing MenuTypes.

Plus finish the slop sweep for `blocks/` and `registry`+`base`+`api` (see `slop-comments.md` tail).

Done so far: registry-blocks-items (fixed), datagen (findings filed), slop (most packages).
