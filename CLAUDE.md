# MOLIDO PHONE AI — BUILD CONTRACT

## Core contract
1. Keep the app lightweight and offline-first.
2. Base build must require no VPS, paid API, or API key.
3. Do not add INTERNET permission unless explicitly requested.
4. Preserve Persian-first UX and English fallback.
5. Every feature must compile on GitHub Actions.
6. Never silently add phone-control, remote-control, network upload, or external-service behavior.
7. Keep user data local by default.
8. Prefer Android platform APIs and small dependencies.
9. Any future cloud AI adapter must be disabled by default and require explicit user configuration.
10. Do not commit secrets, tokens, keystores, or private endpoints.

## Quality gates
- Build: `gradle :app:assembleDebug`
- Unit tests: `gradle :app:testDebugUnitTest`
- APK expected at: `app/build/outputs/apk/debug/app-debug.apk`
