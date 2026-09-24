---
trigger: always_on
---

# Sensitive Data & Secret Prevention (Android Mobile)

## Mandate
Never commit signing keystores, passwords, private keys, `local.properties`, or unmasked backend secrets into the Android repository.

## Rules & Best Practices

1. **Android Secrets & Keystores**:
   - `local.properties`, `*.keystore`, `*.jks`, and `google-services.json` must remain untracked and in `.gitignore`.
   - Never hardcode API keys or OAuth secrets in `BuildConfig`, strings.xml, or Kotlin source files. Use Gradle build parameters or local environment properties.

2. **Pre-Commit Verification**:
   - `scripts/secret_scanner.py` runs before commits to verify no keystores, properties, or private keys are staged.
   - If a violation is caught, unstage using `git restore --staged <file>`.
