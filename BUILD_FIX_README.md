# MealWise build fix

The original project failed for two independent reasons:

1. `google-services.json` was missing while the Google Services Gradle plugin was always applied.
2. AndroidX Hilt 1.4.0 and Lifecycle 2.11.0 require `compileSdk = 37`, while the project used 36.

## Changes made

- Changed `compileSdk` from 36 to 37.
- Kept `targetSdk` at 36.
- Made the Google Services plugin conditional:
  - If `app/google-services.json` exists, the plugin is applied.
  - If it does not exist, Firebase configuration is skipped and the app can still compile.
- Firebase and Hilt dependencies remain in the project for future development.
- Removed generated `.gradle` and `build` folders from the shared ZIP.
- Removed the machine-specific `local.properties`; Android Studio recreates it automatically.

## Before building

In Android Studio:

1. Open **Tools > SDK Manager**.
2. Under **SDK Platforms**, install **Android API 37**.
3. Click **Apply** and wait for installation to finish.
4. Click **File > Sync Project with Gradle Files**.
5. Use **Build > Clean Project**, then **Build > Rebuild Project**.

## When you want Firebase

1. Create or open a Firebase project.
2. Add an Android application with package name `com.example.mealwise`.
3. Download `google-services.json`.
4. Copy it to:

   `MealWise/app/google-services.json`

5. Sync Gradle and rebuild.

Do not place the file in the project root or in `app/src/main`.
