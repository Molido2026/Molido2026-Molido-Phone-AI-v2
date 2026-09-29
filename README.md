# MOLIDO PHONE AI

Lightweight, Persian-first, offline Android phone companion.

## Included
- Offline local assistant brain
- Chat session
- Persistent memory
- Persistent notes
- Persian / English UI
- RTL / LTR layout
- Dark / light theme
- GitHub Actions debug APK build
- Android 26+ / Java 17 / Kotlin / Jetpack Compose

## Privacy
The base app does not send user text to external AI services and does not request Internet access.

## GitHub Actions
Workflow file:

`.github/workflows/main.yml`

After pushing to `main`:

`Actions -> MOLIDO PHONE AI - Android Build`

The APK is uploaded as the artifact:

`molido-phone-ai-debug-apk`

## Local build
Use Gradle 8.7+ with Java 17:

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

APK:

`app/build/outputs/apk/debug/app-debug.apk`
