<p align="center">
  <img src="docs/branding/android-doctor-banner.png" alt="AndroidDoctor" />
</p>

# 📱 AndroidDoctor

> **Advisory tool for Android build health, performance, and Jetpack Compose modernization.**

---

## 🎯 What is AndroidDoctor?

AndroidDoctor is a developer tool that analyzes Android projects and produces **clear, actionable guidance** about:

- Build health & build-time optimization  
- Jetpack Compose readiness  
- Modern Android tech adoption (Kotlin, Gradle, Compose, Hilt, etc.)  

Unlike traditional linting or strict build tools, AndroidDoctor is:

### ✔ Advisory, not enforcing  
- Does **not** fail builds  
- Does **not** modify code  
- Provides recommendations and explanations  
- Focuses on *clarity*, *safety*, and *modernization*

The goal is to help Android teams **ship faster**, **reduce tech debt**, and **modernize safely**, especially during migrations to Jetpack Compose.

---

## 🏗 High-Level Architecture

AndroidDoctor is composed of two main components:

### **1. Gradle Plugin (Collector)**
Runs inside the Android project and gathers deterministic information such as:

- Gradle / Kotlin / AGP versions  
- kapt usage  
- module graph  
- build variants  
- Compose configuration  

Produces a structured `report.json`.

### **2. CLI Reporter**
A standalone tool that reads `report.json` and generates:

- Markdown report (PR-friendly)  
- HTML report (stakeholder-friendly)  
- Future: CI annotations and dashboard exports  

### ⭐ AI layer (optional, future)
AI will add explanations, prioritization, and safe modernization recommendations.  
It will **consume structured findings only** — the deterministic plugin remains the core.

---

## 📦 Repository Structure

```text
android-doctor/
├─ plugin/        # Gradle plugin (Collector)
├─ cli/           # CLI Reporter            
├─ docs/          # Roadmap, vision, architecture, schema
├─ samples/       # Sample Android projects
└─ README.md      # You are here
```

### Sandbox test app

The repository includes `samples/android-doctor-architecture-test-app`, a purposely mixed-architecture Android app (MVC + MVP + MVVM + MVI signals) with known smells and legacy dependencies so AndroidDoctor can be validated against realistic findings.


---

## ✅ Public Release Gate (Free/Public Repo)

Before merging a release candidate from this public repository, run:

```bash
./gradlew check
./gradlew doctorTest
```

A public release candidate is **blocked** if any of the following fail:

- `verifyPublicFreeOnly` (premium/licensing token leak guard)
- `:cli:jacocoTestCoverageVerification` (free-path coverage gate)
- `doctorTest` (free HTML/Markdown workflow verification)

This gate helps ensure the public repository remains free-only and release-safe.
