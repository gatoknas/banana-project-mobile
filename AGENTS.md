# Project Instructions

## Agent tooling
This repo is used with Antigravity, KiloCode, and OpenCode. Skills, rules, and hooks are shared from `.agents/`; thin per-tool bridges live in `.opencode/` and `.kilo/`. See `AGENT-TOOLING.md` for the full layout.

## Rules
- Follow `.agents/rules/github-project-tracking.md` — mandatory issue-first / branch-first / PR-first workflow on GitHub Projects #6.
- Follow `.agents/rules/secret-prevention.md` — never commit credentials or `.env` files; commit only `.env.example` templates.

## Workflow
Non-trivial changes go through the 3-gate lifecycle: create/link GitHub issues on Project #6 with the `repo:mobile` label, branch as `<issue#>-<slug>`, then open a PR with test evidence and `Closes #<issue>`.

## Build & test
See `.github/workflows/mobile-ci.yml` for the authoritative build/test/lint commands for this repo.
