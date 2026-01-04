# Todo App

## Overview
Todo App is a simple, offline-first Android application that helps students organize tasks and deadlines in one place. It focuses on clarity, speed, and reliability so daily planning stays effortless.

## Basic Features
- **CRUD:** Create, read, update, and delete tasks with due dates.
- **Persistent storage:** Tasks and history are saved locally (file/JSON) and remain after closing the app.
- **Multiple views:** List View (manage), Calendar View (plan), History View (review), plus grouping for organization.
- **Time/date handling:** Due dates, day-based filtering, and calendar navigation to avoid deadline conflicts.
- **Scales >20 items:** Efficient Compose lists support large task sets smoothly.

## Key Features
- Fast task entry and editing
- Calendar-based planning
- Completed-task history
- Search and filters by text/date
- On-device privacy; no network required

## Tech Stack
- Kotlin, Jetpack Compose (Material 3)
- Gradle (Kotlin DSL), Min SDK 21, Target SDK 36

## Run
1. Open the project in Android Studio or VS Code.
2. Connect a device (USB debugging) or start an emulator.
3. Build & install:
	- Windows: `./gradlew.bat installDebug`
	- macOS/Linux: `./gradlew installDebug`
4. APK output: `app/build/outputs/apk/debug/app-debug.apk`.

