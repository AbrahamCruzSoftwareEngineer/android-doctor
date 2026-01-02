# 📱 AndroidDoctor

> **Advisory tool for Android build health, performance, and Jetpack Compose modernization.**  
> *Status: Early planning — architecture & docs only. No implementation yet.*

---

## 🚧 Project Status

AndroidDoctor is currently in **Phase 0 (Repo + Planning)**.

This repository contains:

- Initial structure  
- Documentation  
- Roadmap  
- Vision and architecture notes  

There is **no Gradle plugin**, **no CLI**, and **no core logic** implemented yet.  
This stage is intentionally calm and foundational.

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
├─ plugin/        # Gradle plugin (Collector) — placeholder
├─ cli/           # CLI Reporter               — placeholder
├─ docs/          # Roadmap, vision, architecture, schema
├─ samples/       # Sample Android projects (future)
└─ README.md      # You are here
