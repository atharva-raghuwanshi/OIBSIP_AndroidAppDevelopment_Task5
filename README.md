# ⏱️ Stopwatch Android App

A simple, clean, and accurate Android stopwatch application built with **Java and XML**. The app provides millisecond-precision elapsed time tracking with Start, Pause/Resume, Lap, and Reset controls.

## ✨ Features

- **Millisecond-precision stopwatch** with `HH:MM:SS.mmm` display
- **Start** the stopwatch from zero
- **Pause** the stopwatch at the current elapsed time
- **Resume** a paused stopwatch without losing elapsed time
- **Lap** recording with cumulative and split lap times
- **Reset** the stopwatch and clear all recorded laps
- Clear **Running / Paused / Stopped** status indicator
- Buttons automatically enable/disable based on the current stopwatch state
- Correct timing when the app is paused or resumed
- Preserves stopwatch state across **screen rotation/configuration changes**
- Clean Android UI with rounded cards and styled controls

## 🛠️ Tech Stack

- **Language:** Java
- **UI:** Android XML
- **Build System:** Gradle
- **Android Gradle Plugin:** 8.2.2
- **Gradle:** 8.4
- **Compile SDK:** Android 34
- **Minimum SDK:** Android 8.0 (API 26)
- **Target SDK:** Android 34
- **Java:** JDK 17
- **AndroidX:** AppCompat 1.6.1

## 📁 Project Structure

```text
StopwatchApp/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/stopwatch/
│       │   └── MainActivity.java
│       └── res/
│           ├── color/
│           ├── drawable/
│           ├── layout/
│           │   └── activity_main.xml
│           ├── mipmap-anydpi-v26/
│           └── values/
│               ├── colors.xml
│               ├── strings.xml
│               └── themes.xml
├── gradle/
│   └── wrapper/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew
└── gradlew.bat
```

## ⏱️ How the Timer Works

The stopwatch does **not** simply add milliseconds every time the UI updates. Instead, it calculates elapsed time using Android's monotonic clock:

```java
elapsedNow = baseElapsedMs
        + (SystemClock.elapsedRealtime() - startRealtime);
```

`SystemClock.elapsedRealtime()` is suitable for measuring elapsed durations because it is monotonic and is not affected by changes to the device's wall-clock time.

The UI ticker only refreshes the displayed value; it does not determine how much time has actually elapsed. This helps prevent timer drift.

The project also saves the important stopwatch state during configuration changes, allowing the timer and laps to survive screen rotation.

## 🎮 Controls

| Button | Function |
|---|---|
| **Start** | Starts the stopwatch from `00:00:00.000` |
| **Pause** | Pauses the stopwatch while keeping the elapsed time |
| **Resume** | Appears after pausing and continues from the saved elapsed time |
| **Lap** | Records the current cumulative time and split time |
| **Reset** | Stops the stopwatch, resets the display, and clears laps |

## 💻 Requirements

Before building the project, make sure you have:

1. **JDK 17**
2. **Android SDK**
3. Android SDK Platform 34
4. Android Build Tools 34.0.0
5. VS Code or Android Studio

The project includes the **Gradle wrapper**, so Gradle does not need to be installed separately.

## 🚀 Build the APK

### Using VS Code / Terminal

Open a terminal in the `StopwatchApp` directory.

### Windows

```bash
gradlew.bat assembleDebug
```

### macOS / Linux

```bash
./gradlew assembleDebug
```

After a successful build, the debug APK will be located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 📱 Install on an Android Phone

Enable **Developer Options** and **USB Debugging** on your Android device, connect the phone to your computer, and run:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

You can also copy the generated `app-debug.apk` to your phone and install it manually if installation from unknown sources is allowed.

## 🧪 Testing Checklist

Use this checklist to verify the application:

- [ ] App launches successfully
- [ ] Initial display shows `00:00:00.000`
- [ ] Start begins counting
- [ ] Pause freezes the displayed time
- [ ] Resume continues from the paused time
- [ ] Lap records a lap while running
- [ ] Multiple laps appear in the scrollable list
- [ ] Reset returns the timer to zero
- [ ] Reset clears all laps
- [ ] Buttons change enabled/disabled states correctly
- [ ] Running/Paused/Stopped status updates correctly
- [ ] Timer remains accurate after leaving and returning to the app
- [ ] Screen rotation does not incorrectly reset the stopwatch

## 🧩 Main Files

### `MainActivity.java`

Contains the main stopwatch logic, including:

- Start/resume handling
- Pause handling
- Reset handling
- Lap recording
- Timer updates
- Stopwatch state management
- State restoration after configuration changes

### `activity_main.xml`

Defines the application interface, including:

- App title and subtitle
- Large elapsed-time display
- Stopwatch state label
- Start/Pause buttons
- Lap/Reset buttons
- Scrollable lap list

### `strings.xml`

Contains user-visible text such as button labels, status messages, and empty-lap instructions.

### `colors.xml` and drawable resources

Define the application's color palette, button appearances, time card, and other visual styling.

## 🔧 Customization

You can easily customize the app by editing:

- `res/values/colors.xml` — change colors
- `res/values/strings.xml` — change displayed text
- `res/layout/activity_main.xml` — modify the UI layout
- `MainActivity.java` — modify stopwatch behavior
- `res/drawable/` — customize button and card backgrounds

## 📦 Included APK

A compiled debug APK is included with the project:

```text
StopwatchApp-APK/app-debug.apk
```

This can be used to test the application without rebuilding the project.

## 📄 License

This project is provided for educational and personal development purposes. Add your preferred open-source license here if you plan to publish the project.

---

**Built with Java + Android XML.**  
A lightweight stopwatch focused on accurate elapsed-time tracking and a simple user interface.
