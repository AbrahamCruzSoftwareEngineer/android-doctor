# AndroidDoctor Core Module (`:core`)

This module defines the **public, stable extension surface** for AndroidDoctor.

## Purpose

- Provide canonical shared models used by public and private editions.
- Expose neutral extension interfaces for rendering, exporting, and recommendation logic.
- Keep business-tier specifics (free/premium) out of core contracts.

## Public API Surface

### Canonical report model

- `com.evolutiondso.androiddoctor.core.model.AndroidDoctorReport`

This model is the source of truth for report data exchanged between plugin, CLI, and future private extensions.

### Extension interfaces

- `ReportRenderer`
- `Exporter`
- `RecommendationProvider`
- `FeatureSet`

These interfaces are intentionally premium-agnostic and safe to publish in the public repository.

## Dependency Direction (non-negotiable)

- ✅ `:core` may be consumed by public modules (e.g., `:cli`, `:plugin`) and future private modules.
- ✅ Future premium/private implementations should depend on `:core`.
- ❌ `:core` must **never** depend on premium/private code.
- ❌ Public modules must **never** depend on private/premium artifacts.

## Compatibility expectations

Treat API changes in `:core` as semver-governed:

- **Patch**: bug fixes, internal changes, docs.
- **Minor**: additive, backward-compatible model/interface changes.
- **Major**: breaking changes to model fields, interface signatures, package names.

When in doubt, prefer additive evolution and keep older fields/signatures until the next major release.

## Security boundary reminder

Premium behavior must not be implemented in this repository.

`:core` only defines neutral contracts and shared data types. Premium implementations belong in a separate private repository/artifact.
