# AndroidDoctor Free / Premium Separation Plan

## 1) Current Repository Evaluation

### Current modules

The repository currently has two Gradle modules:

- `:plugin` (analysis and report collection)
- `:cli` (report loading, rendering, export, and capability routing)

`settings.gradle.kts` confirms this layout.

### Where premium code currently exists (public repo)

Premium logic is currently present in `:cli` and mixed with free logic:

- `PremiumHtmlRenderer` exists in public code.
- `PremiumCapabilities` exists in public code.
- CLI plan selection includes a `PREMIUM` branch.
- Capability-based branching is done in public code.
- License key lookup code exists in public code.
- PDF export code exists in public code.
- HTML templates/components include premium-specific UI markers and premium-only copy.

### Coupling assessment

The architecture has a useful **starting seam** (`CapabilitySet`), but premium and free implementations are compiled together in the same public artifact. This means the current design is **not secure enough** for a true public/free split.

- Good: interface-driven dispatch via `CapabilitySet`.
- Bad: premium implementations and assets live inside the public repository.
- Bad: capability checks are effectively runtime gating, which does not prevent source leakage.

### Renderer separation status

Renderer separation partially exists:

- Free and premium HTML renderers are separate classes.
- Shared HTML building blocks and assets are still shared in one module and include premium behavior.

This is a partial separation, not a repository-level isolation.

---

## 2) Recommended Strategy

## ✅ Recommendation: Option C (private premium artifact built from private repository)

Use:

1. **Public repo**: `android-doctor` with free-only modules and extension interfaces.
2. **Private repo**: `android-doctor-premium` that depends on public published artifacts.
3. Publish premium as private artifact(s), then assemble/distribute a premium CLI from the private repo.

Why this is safest and most maintainable:

- Prevents premium source exposure by keeping premium code in a separate private VCS.
- Keeps dependency direction clean: premium depends on public core, never the reverse.
- Avoids submodule complexity and accidental sync leaks.
- Supports future premium features as additive modules without reworking public code.

Option A and Option C are compatible in practice. The key security property is **separate private repository**; artifact publication is the cleanest integration mechanism.

---

## 3) Target Module Layout

## Public repository (`android-doctor`)

- `:core`  
  Analysis engine, report model (`AndroidDoctorReport`), extension interfaces.
- `:cli-free`  
  Free command-line app wiring + free renderers.
- `:renderer-free`  
  Basic HTML + Markdown output.
- `:plugin` (optional during transition, then refactor into/onto `:core`)  
  Gradle integration and report generation.

## Private repository (`android-doctor-premium`)

- `:renderer-premium`  
  Premium dashboard HTML/JS/CSS and visualizations.
- `:analysis-premium`  
  Advanced recommendation engine, deeper analysis.
- `:export-premium`  
  PDF and advanced exports.
- `:cli-premium`  
  Premium CLI wiring (selects premium renderer stack).
- `:licensing`  
  License validation and entitlement logic.

All private modules depend on published public artifacts, for example:

```kotlin
implementation("com.androiddoctor:core:<version>")
implementation("com.androiddoctor:renderer-free:<version>")
```

---

## 4) Refactoring Steps (Migration Plan)

### Phase 0 — Prepare seams (in public repo)

1. Introduce neutral extension points in `:core`:
   - `ReportRenderer`
   - `RecommendationProvider`
   - `Exporter`
2. Move `AndroidDoctorReport` into `:core` (single canonical model).
3. Keep free implementations only in public modules.

### Phase 1 — Remove premium implementation from public repo

1. Delete premium classes/assets from public repo:
   - `PremiumHtmlRenderer`
   - `PremiumCapabilities`
   - premium-specific branches/copy/assets in shared HTML templates
   - `LicenseValidator`
   - premium-only exports (e.g., PDF if designated premium)
2. Replace with free-only wiring in `:cli-free`.
3. Keep interfaces only; no premium concrete implementations in public code.

### Phase 2 — Create private premium repo

1. Create `android-doctor-premium` private repository.
2. Implement premium renderers and analytics against public interfaces.
3. Add licensing/entitlement checks only in private code.
4. Build premium CLI distribution from private repo.

### Phase 3 — Packaging and distribution

1. Publish public artifacts to public registry.
2. Publish premium artifacts to private registry.
3. Premium assembly (`:cli-premium`) includes private modules and public dependencies.

### Phase 4 — Verification gates to prevent leakage

In public CI, enforce checks:

- Forbidden token/package scan (fail on `premium`, `license`, private package prefixes in source paths where disallowed).
- Dependency audit to block private coordinates.
- Release gate that verifies public source tree has no premium modules/assets.

---

## 5) Developer Workflow (after split)

## Free user workflow

- Run plugin analysis and generate `report.json`.
- Use `cli-free` for:
  - terminal summary
  - markdown report
  - basic HTML report

## Premium user workflow

- Same base analysis (`report.json`) from public core/plugin.
- Use private `cli-premium` for:
  - advanced dashboard
  - deeper analytics
  - trend/history views
  - CI/team outputs
  - PDF export

This preserves a common analysis pipeline and avoids duplicating core logic.

---

## 6) Risks and Mitigations

- **Risk: accidental premium code reintroduced in public repo**  
  Mitigation: CI guardrails + CODEOWNERS + package boundaries.
- **Risk: model drift between public and private repos**  
  Mitigation: strict semantic versioning and compatibility tests in premium repo.
- **Risk: duplicated rendering logic**  
  Mitigation: keep shared report schema stable in `:core`; keep premium rendering fully private.

---

## 7) Immediate Next Actions

1. Create `:core` and move `AndroidDoctorReport` + rendering/export interfaces.
2. Rename current CLI path to `:cli-free` and strip all premium code.
3. Remove `pdfbox` from public dependencies if PDF is premium-only.
4. Stand up private `android-doctor-premium` repo and implement premium modules there.
5. Add public CI leak-prevention checks before next release.
