#!/usr/bin/env python3
from __future__ import annotations

import pathlib
import re
import sys
from typing import Iterable

ROOT = pathlib.Path(__file__).resolve().parents[1]


def to_token_regex(token: str) -> re.Pattern[str]:
    escaped = re.escape(token.lower())
    uses_word_boundaries = all(ch.isalnum() or ch in "_-" for ch in token)
    if uses_word_boundaries:
        return re.compile(rf"(?<![a-z0-9]){escaped}(?![a-z0-9])")
    return re.compile(escaped)


def collect_violations(files: Iterable[pathlib.Path], tokens: list[str]) -> list[str]:
    patterns = {token: to_token_regex(token) for token in tokens}
    violations: list[str] = []

    for file in files:
        if not file.exists() or not file.is_file():
            continue
        for index, line in enumerate(file.read_text(encoding="utf-8", errors="ignore").splitlines(), start=1):
            normalized = line.lower()
            matched = next((token for token, regex in patterns.items() if regex.search(normalized)), None)
            if matched:
                rel = file.relative_to(ROOT)
                violations.append(f"{rel}:{index} contains forbidden token '{matched}'")

    return violations


def verify_public_free_only() -> list[str]:
    forbidden = ["premium", "licensevalidator", "useridentity", "licensing", "entitlement"]
    source_roots = [ROOT / "cli/src/main/kotlin", ROOT / "cli/src/main/resources"]
    allowed_extensions = {"kt", "md", "js", "css", "html"}

    source_files: list[pathlib.Path] = []
    for root in source_roots:
        if not root.exists():
            continue
        for path in root.rglob("*"):
            if path.is_file() and path.suffix.lstrip(".") in allowed_extensions:
                source_files.append(path)

    return collect_violations(source_files, forbidden)


def verify_no_private_coordinates() -> list[str]:
    token_file = ROOT / "config/guardrails/private-coordinate-tokens.txt"
    forbidden = [
        line.strip()
        for line in token_file.read_text(encoding="utf-8").splitlines()
        if line.strip() and not line.strip().startswith("#")
    ]

    files_to_scan = {
        ROOT / "settings.gradle.kts",
        ROOT / "build.gradle.kts",
        ROOT / "gradle/libs.versions.toml",
    }

    for path in ROOT.rglob("build.gradle.kts"):
        rel = path.relative_to(ROOT)
        rel_text = rel.as_posix()
        if rel_text.startswith("samples/") or "/.gradle/" in f"/{rel_text}" or "/build/" in f"/{rel_text}":
            continue
        files_to_scan.add(path)

    return collect_violations(sorted(files_to_scan), forbidden)


def main() -> int:
    public_violations = verify_public_free_only()
    private_coord_violations = verify_no_private_coordinates()

    if public_violations:
        print("Public/free guardrail failed:")
        print("\n".join(public_violations))

    if private_coord_violations:
        print("Private coordinate guard failed:")
        print("\n".join(private_coord_violations))

    if public_violations or private_coord_violations:
        return 1

    print("Guardrails passed: no forbidden public/premium tokens or private coordinates found.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
