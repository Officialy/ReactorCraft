# ReactorCraft port — issue tracker

Working issue list for the ReactorCraft → NeoForge 26.2 port. Companion to `PORTING.md`
(which tracks the file-by-file port status). This directory holds the **defect / cleanup**
backlog: things that compile-or-don't but are wrong, unfaithful, incomplete, or messy.

Priority: **P1** = broken / dead feature / crash. **P2** = correctness or faithfulness gap vs the
1.7.10 original. **P3** = code quality / cleanup.

## Files
- `slop-comments.md` — module-wide catalog of AI-slop / port-narration comments to REMOVE or TRIM
  (~56 REMOVE, ~69 TRIM). Complete except the `blocks/` and `registry`/`base`/`api` packages
  (sweep cut off by session limit — re-run those two).
- *(pending)* `registry-blocks-items.md`, `datagen.md`, `te-cluster.md`, `renders-gui.md` — four
  audit passes were **interrupted by a session usage limit before writing their files**. Re-run
  them (see "Audits to resume" below). Concrete findings already confirmed are seeded here.

---

## DONE
- **[P2] Split `reactor_mat` into six per-variant blocks** — `1ffa7f6`. Was one block with an
  `EnumProperty<MatBlocks> VARIANT`; now six registered blocks + a `BlockReactorMat` per variant.
  Also removed the three zero-consumer duplicate storage blocks. Compiles + datagen green.

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

## Audits to resume (interrupted by session limit — no findings file written)

Re-dispatch these read-only Sonnet audits; each writes `docs/issues/<name>.md`. Scope in the briefs
used this session (all: audit **allowlisted/ported** code only; don't flag known-unported 1.7.10 files;
don't flag deliberate damage-value variant items — WASTE/FUEL/PLUTONIUM/fluorite; don't re-enumerate the
TE-cluster compile errors, summarize+count instead):

1. **registry-blocks-items** — `registry/`, `blocks/`, `items/` (allowlisted): duplicate/dead
   registrations, wrong block/item properties vs original, metadata handling.
2. **datagen** — `data/` + generated resources: missing lang/models/loot, the tags gap above, recipe
   gaps vs `git show origin/master:ReactorRecipes.java`, fragile literal-JSON recipes.
3. **te-cluster** — `base/` + `tileentities/**`: per-TE port-status checklist, compile-blocker pattern
   counts, and **semantic** bugs in already-ported base classes (diff vs `origin/master:Base/*`).
4. **renders-gui** — `renders/`, `models/`, `container/`, `guis/`, `client/`: stubbed/dead renderers,
   `Modelled` blocks with no registered BER, atlas-size/texture-path bugs, missing MenuTypes.

Plus finish the slop sweep for `blocks/` and `registry`+`base`+`api` (see `slop-comments.md` tail).
