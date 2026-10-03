# Agent Tooling Layout — banana-project-mobile

Used with **Antigravity**, **KiloCode**, and **OpenCode**. One source of truth with thin per-tool bridges.

## TL;DR
- **Source of truth = `AGENTS.md` + `.agents/`.** All three tools read `AGENTS.md`, and all three discover `.agents/skills/`.
- **Bridges** are `.opencode/` (OpenCode) and `.kilo/` (KiloCode).

## Discovery matrix

| Capability | Antigravity (canonical) | OpenCode bridge | KiloCode bridge |
|---|---|---|---|
| Instructions / rules | `.agents/rules/*.md` (needs `trigger:` frontmatter) + `AGENTS.md` | `opencode.json` → `instructions: [".agents/rules/*.md"]` | `kilo.jsonc` → `instructions: [".agents/rules/*.md"]` |
| Skills | `.agents/skills/<name>/SKILL.md` | same dir (native compat) | same dir (native compat) |
| Hooks / plugins | `.agents/hooks.json` | `.opencode/plugins/*.ts` | `.kilo/plugins/*.ts` |
| MCP servers | `.agents/mcp_config.json` | `opencode.json` → `mcp` | `kilo.jsonc` → `mcp` |

## Canonical assets (`.agents/`)
- `skills/kotlin-android-master` — Kotlin / Jetpack Compose architecture, TDT, memory and APK-size discipline.
- `skills/github-project-manager` — GitHub Projects #6 lifecycle (issue → branch → PR).
- `rules/github-project-tracking.md` — mandatory issue-first / branch-first / PR-first workflow.
- `rules/secret-prevention.md` — never commit secrets; `.env.example` only.
- `hooks.json` — Antigravity secret-guard (pre-command) + codegraph auto-sync (post-edit/stop).
- `mcp_config.json` — CodeGraph + GitHub MCP servers (Antigravity).

Rules under `.agents/rules/` **must** start with YAML frontmatter (`trigger:` + `description:`), or Antigravity silently discards them.

## Bridges
- `opencode.json` — `instructions` (bridges `.agents/rules`) + `mcp` (CodeGraph + GitHub).
- `kilo.jsonc` — same.
- `.opencode/plugins/secret-guard.ts`, `.kilo/plugins/secret-guard.ts` — block `git commit` when the secret scanner finds issues.

## Environment
- `GITHUB_PERSONAL_ACCESS_TOKEN` — required by the GitHub MCP server.
  - Antigravity: `${GITHUB_PERSONAL_ACCESS_TOKEN}` in `.agents/mcp_config.json`.
  - OpenCode / Kilo: `{env:GITHUB_PERSONAL_ACCESS_TOKEN}` in their config.

## Maintenance rules
- **Skill:** add once under `.agents/skills/<name>/SKILL.md` (name must match the folder).
- **Rule:** add `.agents/rules/<name>.md` with `trigger:` frontmatter — auto-bridged via the `instructions` glob.
- **MCP server:** add to `.agents/mcp_config.json`, `opencode.json`, and `kilo.jsonc`.
- **Hook/plugin:** Antigravity in `.agents/hooks.json`; OpenCode in `.opencode/plugins/`; Kilo in `.kilo/plugins/`.

See `../banana-project-go-api/AGENT-TOOLING.md` for the reference implementation (which also has a subagent + Go test plugins).
