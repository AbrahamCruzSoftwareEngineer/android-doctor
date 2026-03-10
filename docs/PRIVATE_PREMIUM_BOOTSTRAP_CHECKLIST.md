# Private Premium Repository Bootstrap Checklist

Use this checklist when creating the future private `android-doctor-premium` repository.

## 1) Repository setup

- Create private repository: `android-doctor-premium`.
- Configure private CI and artifact publication.
- Restrict access to maintainers and premium engineering team.

## 2) Dependency model

- Depend on public artifacts from `android-doctor`:
  - `com.androiddoctor:core:<version>`
  - optional free renderer/public shared artifacts as needed.
- Keep dependency direction one-way:
  - private premium modules -> public `:core`
  - never the reverse.

## 3) Private module skeleton

- `:renderer-premium`
- `:analysis-premium`
- `:export-premium`
- `:cli-premium`
- `:licensing`

## 4) Premium-only concerns (must remain private)

- Advanced dashboard rendering and premium visualizations.
- Deeper recommendation/risk prioritization logic.
- Team/CI/history/trend features.
- Licensing/entitlement checks.

## 5) Compatibility and versioning

- Track `:core` semver compatibility.
- Add integration tests to validate private modules against supported public-core versions.
- Pin and upgrade public-core versions deliberately.

## 6) Security checks

- Validate no premium source/artifacts are pushed to public repo.
- Validate no private coordinates are referenced from public modules.
- Keep private signing/secrets in private CI only.
