# AndroidDoctor Plugin (Collector)

> **Status:** Placeholder — implementation will begin in Phase 1.

This module will eventually contain the **Gradle plugin** responsible for collecting
deterministic project metadata, including:

- Gradle, AGP, and Kotlin versions
- kapt usage
- Module structure
- Build variants
- Compose configuration
- Other build-related signals

The plugin will output this data in a structured `report.json` file that the CLI
tool will later consume.

No implementation has been added yet. This directory currently exists only as part
of the initial project structure.
