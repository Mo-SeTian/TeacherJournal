---
description: Gradle build for TeacherJournal (sets JAVA_HOME to openjdk@17, runs specified task)
---

Set up the Java environment and run a Gradle build for the Android project.

Steps:
1. Run the build command with the correct JAVA_HOME:
   ```bash
   export JAVA_HOME=/opt/homebrew/opt/openjdk@17
   export PATH=$JAVA_HOME/bin:$PATH
   ./gradlew $ARGUMENTS 2>&1 | tail -40
   ```
2. If `$ARGUMENTS` is empty, default to `:app:compileDebugKotlin`.
3. Report the build result (success/failure) and any errors found.
4. If the build fails, analyze the error output and suggest or apply fixes.

Common invocations:
- `build` → compile check (`:app:compileDebugKotlin`)
- `build assemble` → full debug APK (`:app:assembleDebug`)
- `build clean` → clean build (`clean :app:compileDebugKotlin`)
- `build test` → run unit tests (`:app:testDebugUnitTest`)
