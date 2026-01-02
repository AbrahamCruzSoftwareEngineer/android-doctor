# AndroidDoctor Architecture

AndroidDoctor consists of two main components:

- **Gradle Plugin (Collector)**
- **CLI Reporter (Presenter)**

This architecture separates **data collection** from **analysis and reporting**, allowing AndroidDoctor to stay safe, deterministic, and extensible.

---

## 1. Gradle Plugin (Collector)

The Gradle plugin runs inside the user’s Android project.  
Its purpose is to collect **deterministic metadata** that cannot be reliably gathered from outside the build.

### Responsibilities

- Inspect Gradle configuration
- Detect Android modules, apps, and libraries
- Record:
    - Gradle, AGP, and Kotlin versions
    - Build types and flavor dimensions
    - Compose configuration
    - `kapt` usage
    - Module structure and relationships
    - Build features enabled/disabled
- Emit a structured `report.json` file at a known location in the project

### Non-Responsibilities

The plugin intentionally **does not**:

- Perform deep analysis or scoring
- Fail builds or enforce policies
- Modify source code or Gradle files
- Run AI models

It focuses solely on **safe, deterministic data collection**.

---

## 2. CLI Reporter (Presenter)

The CLI runs **outside** the Android project (locally or in CI).

It consumes `report.json` and performs analysis, scoring, and reporting.

### Responsibilities

- Parse `report.json`
- Compute:
    - **Build Health Score** (0–100)
    - **Compose Readiness Score** (0–100)
- Detect:
    - Build smells and risks
    - Modernization opportunities
- Generate:
    - Markdown report (developer / PR friendly)
    - HTML report (stakeholder / manager friendly)
    - Optional CI-friendly output (machine-parsable)

### Future Responsibilities

- Provide **optional AI-powered explanations** and recommendations
- Offer **prioritized modernization plans** based on project context
- Integrate with:
    - GitHub/GitLab CI
    - PR comment bots
    - Optional SaaS dashboards

The CLI is where most of the “brains” and UX live.

---

## 3. Data Contract: `report.json`

The `report.json` file is the **contract** between the Gradle plugin (Collector) and the CLI (Reporter).

### Goals

- Stable, versioned schema
- Backward-compatible changes
- Safe to generate in CI
- Easy to consume by tools and scripts

### Conceptual Structure

A future version of the schema will likely include:

- `schemaVersion`
- `project` metadata
- `tooling` versions (Gradle, AGP, Kotlin)
- `modules` list with:
    - type (app, library, etc.)
    - Android/Non-Android flags
    - Compose enabled/disabled
    - `kapt` usage
- `buildConfig`:
    - build types
    - flavors
    - variant count
- `features` and flags

The exact schema will be documented in `REPORT_SCHEMA.md` once stabilized.

---

## 4. Why Split Plugin and CLI?

Splitting the system provides important benefits:

### ✔ Safety

- The plugin does not fail builds or modify files.
- All “risky” features (like AI) live outside the build system.

### ✔ Flexibility

- The CLI can evolve rapidly without impacting Gradle builds.
- New reporting formats or integrations can be added independently.

### ✔ Extensibility

- Future backends (e.g., SaaS) can consume the same `report.json`.
- Third-party tools can integrate by reading the same contract.

### ✔ CI & Workflow Friendly

- The plugin runs during the build to produce `report.json`.
- The CLI runs in:
    - Local dev scripts
    - CI jobs
    - Pre-merge checks
    - PR bots

---

## 5. High-Level Flow

Android Project (user repo)
    |
    | 1. Run Gradle with AndroidDoctor plugin
    v
[Collector]
  - Collects deterministic metadata
  - Emits `report.json`
    |
    | 2. Run AndroidDoctor CLI
    v
[Reporter]
  - Parses `report.json`
  - Analyzes + scores
  - Generates Markdown / HTML / CI output

## 6. Future: Optional SaaS / Dashboard Layer

In the long term, AndroidDoctor may offer an optional hosted dashboard that builds on top of the existing `report.json` output.

### Potential capabilities
- Aggregate multiple reports over time
- Track modernization progress across releases
- Provide organization-wide dashboards
- Benchmark multiple projects or teams
- Offer trend analysis (e.g., build health over time, Compose adoption curve)

### Key principles
- **Completely optional** — the open-source core remains fully functional without any SaaS.
- **No impact on build safety** — the Gradle plugin never communicates externally.
- **CLI-first** — reports can be manually uploaded or integrated into CI.

This layer enhances visibility for larger organizations but is not required for individuals or small teams.

---

## 7. Architecture Principles

These principles guide how AndroidDoctor is designed, extended, and used.

### 1. Deterministic First
All core checks rely on predictable, stable signals from the Gradle build — not runtime heuristics or AI.

### 2. Advisory, Not Enforcing
AndroidDoctor never fails builds, rewrites files, or blocks releases.
It provides insight, not enforcement.

### 3. Separation of Concerns
- **Plugin:** Collects data
- **CLI:** Analyzes and reports
- **AI (optional):** Explains and prioritizes
- **SaaS (optional):** Visualizes and tracks

### 4. Safe to Adopt Incrementally
Teams may start by:
- Generating `report.json`
- Running the CLI locally

Then optionally adopt CI integration, AI explanations, or dashboards.

### 5. Ecosystem-Friendly
Designed to work smoothly with:
- Standard Gradle + AGP setups
- Multi-module Android projects
- CI platforms like GitHub Actions, GitLab CI, Jenkins

## 6. Future: Optional SaaS / Dashboard Layer

In the long term, AndroidDoctor may offer an optional hosted dashboard that builds on top of the existing `report.json` output.

### Potential capabilities
- Aggregate multiple reports over time
- Track modernization progress across releases
- Provide organization-wide dashboards
- Benchmark multiple projects or teams
- Offer trend analysis (e.g., build health over time, Compose adoption curve)

### Key principles
- **Completely optional** — the open-source core remains fully functional without any SaaS.
- **No impact on build safety** — the Gradle plugin never communicates externally.
- **CLI-first** — reports can be manually uploaded or integrated into CI.

This layer enhances visibility for larger organizations but is not required for individuals or small teams.

---

## 7. Architecture Principles

These principles guide how AndroidDoctor is designed, extended, and used.

### A. Deterministic First
All core checks rely on predictable, stable signals from the Gradle build — not runtime heuristics or AI.

### B. Advisory, Not Enforcing
AndroidDoctor never fails builds, rewrites files, or blocks releases.  
It provides insight, not enforcement.

### C. Separation of Concerns
- **Plugin:** Collects data
- **CLI:** Analyzes and reports
- **AI (optional):** Explains and prioritizes
- **SaaS (optional):** Visualizes and tracks

Each layer can evolve independently.

### D. Safe to Adopt Incrementally
Teams may start by:
- Generating `report.json`
- Running the CLI locally

Then optionally adopt CI integration, AI explanations, or dashboards.

### E. Ecosystem-Friendly
Designed to work smoothly with:
- Standard Gradle + AGP setups
- Multi-module Android projects
- CI platforms like GitHub Actions, GitLab CI, Jenkins

### F. Open, Extensible Core
The `report.json` schema and core analysis logic are open and versioned, enabling:
- Community contributions
- Custom tooling built on top
- Long-term maintainability
