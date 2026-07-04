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
- `renders-gui.md` — renders/models/container/guis/client audit (1 P1 dead-timer progress bars across
  4 machine screens, 2 P2 turbine renderer faithfulness gaps, 2 P3 cleanup). Open.
- *(pending)* `te-cluster.md` — audit pass still to run (see "Audits to resume").

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

### [P2] ~12 machine blocks still fall back to the `block/steel` texture
- **Where:** `data/ReactorModelProvider.java` (`BLOCK_TEX` / `MACHINE_ITEM_MODELS`).
- **Why wrong:** the mat blocks are fixed (`abbba1c`), but the BER-modelled machines whose renderer is
  still an unported 1.7.10 stub get a flat steel-cube inventory icon. Gated on the render port — see
  `datagen.md` and `renders-gui.md`.
- **Size:** M (unblocks per machine as its renderer is ported)

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

Plus finish the slop sweep for `blocks/` and `registry`+`base`+`api` (see `slop-comments.md` tail).

Done so far: registry-blocks-items (fixed), datagen (findings filed), slop (most packages),
renders-gui (findings filed).
