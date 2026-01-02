# AndroidDoctor Roadmap

> This roadmap is intentionally high-level and may evolve as we learn from real
Android projects and community feedback.

AndroidDoctor is being developed in planned phases. Each phase focuses on delivering
a stable foundation before introducing the next layer of functionality.

---

## Phase 0 — Repo & Planning (Current)

**Goal:** Establish a clean, well-structured foundation.

- Public GitHub repository created
- Initial folder structure added (`plugin/`, `cli/`, `docs/`, `samples/`)
- Core documentation:
    - README.md
    - ROADMAP.md
    - VISION.md
    - ARCHITECTURE.md
- Project philosophy and architecture aligned

_No implementation of the Gradle plugin or CLI is expected in this phase._

---

## Phase 1 — Skeleton Implementation

**Goal:** A minimal, end-to-end system with placeholder logic.

- Add Gradle plugin module (`plugin/`)
- Add CLI module (`cli/`)
- Implement:
    - A plugin task that produces a basic, hard-coded `report.json`
    - A CLI command that reads the file and prints minimal output
- No actual project analysis yet

This phase ensures the project builds, runs, and can pass data through the pipeline.

---

## Phase 2 — Core Deterministic Checks

**Goal:** Provide useful, deterministic insights without requiring AI.

### Build Health Checks
- kapt usage detection (per module)
- Module graph size & "monolith smell"
- Build variant count analysis (types × flavors)
- Outdated tooling versions (Gradle, AGP, Kotlin)
- Build feature hygiene (unused features enabled)

### Compose Modernization Checks
- Compose enabled or not
- Compose BOM usage detection
- XML vs Compose usage heuristic (migration readiness)
- Compiler metrics readiness
- Deprecated Gradle patterns

### Reporting Enhancements
- Build Health Score (0–100)
- Compose Readiness Score (0–100)
- Top 5 prioritized actions based on deterministic heuristics

---

## Phase 3 — Reporting Experience & UX

**Goal:** Make AndroidDoctor actionable and enjoyable to use.

- Beautiful Markdown output
- HTML report (dashboard-like)
- A 30/60/90-day modernization plan
- Better CLI UX (subcommands, formatting, flags)
- Configurable plugin settings:
    - Ignore modules
    - Control output destination
    - CI-friendly modes

---

## Phase 4 — AI Assist (Optional, Pro Tier)

**Goal:** AI-powered explanations and safe guidance.

- AI explanations of findings (“why this matters”)
- AI-based prioritization
- Migration playbooks
- Safe examples tailored to project context

_AndroidDoctor remains deterministic; AI only enhances explanations._

---

## Phase 5 — Integrations & Ecosystem

**Goal:** Make AndroidDoctor part of daily workflow.

- GitHub/GitLab CI annotations
- PR comment bot
- Optional SaaS dashboard for trend tracking
- Upload reports for org-level insights
- Integration with other devtools

---

## Out of Scope (for now)

- Auto-fixing code or Gradle configs
- Enforcing failures (build-breaking policies)
- Deep static analysis of Android source files

AndroidDoctor is an **advisor**, not an enforcer.
