# Installation Guide

This document covers every way to install and run **Escape Dungeon** — from pre-built downloads to building from source and creating distribution packages.

---

## Table of Contents

1. [Pre-Built Downloads (Recommended)](#1-pre-built-downloads-recommended)
2. [Windows Installer](#2-windows-installer)
3. [Running the JAR Manually](#3-running-the-jar-manually)
4. [Building from Source](#4-building-from-source)
5. [Creating Distribution Packages](#5-creating-distribution-packages)
6. [Building the Windows Installer](#6-building-the-windows-installer)
7. [Uninstallation & User Data](#7-uninstallation--user-data)
8. [Troubleshooting](#8-troubleshooting)

---

## 1. Pre-Built Downloads (Recommended)

Pre-built packages are available in the [GitHub Releases](https://github.com/R4ZXRN3T/escape-dungeon/releases) page. Each package includes a **bundled Java 17 runtime**, so no separate Java installation is required.

| Platform                      | File                                           |
|-------------------------------|------------------------------------------------|
| Windows x86_64                | `EscapeDungeon-1.0.0-Windows-x86_64.zip`       |
| Linux x86_64                  | `EscapeDungeon-1.0.0-Linux-x86_64.zip`         |
| macOS x86_64 (Intel)          | `EscapeDungeon-1.0.0-MacOS-x86_64.zip`         |
| macOS aarch64 (Apple Silicon) | `EscapeDungeon-1.0.0-MacOS-aarch64.zip`        |

### Windows

1. Download `EscapeDungeon-1.0.0-Windows-x86_64.zip`.
2. Extract the archive to a folder of your choice.
3. Run **`EscapeDungeon.exe`**.

### Linux

1. Download `EscapeDungeon-1.0.0-Linux-x86_64.zip`.
2. Extract the archive:
   ```bash
   unzip EscapeDungeon-1.0.0-Linux-x86_64.zip -d EscapeDungeon
   ```
3. Make the launcher executable and run it:
   ```bash
   cd EscapeDungeon
   chmod +x EscapeDungeon
   ./EscapeDungeon
   ```

### macOS

1. Download the appropriate archive for your Mac:
   - **Apple Silicon (M1/M2/M3/M4):** `EscapeDungeon-1.0.0-MacOS-aarch64.zip`
   - **Intel:** `EscapeDungeon-1.0.0-MacOS-x86_64.zip`
2. Extract the archive (double-click or use `unzip`).
3. Open `EscapeDungeon.app`.
4. If macOS blocks the app with a "developer cannot be verified" warning:
   - Go to **System Settings → Privacy & Security** and click **Open Anyway**, or
   - Right-click `EscapeDungeon.app` and choose **Open**.

---

## 2. Windows Installer

A Windows installer is included in the releases (`Escape-Dungeon_setup.exe`). Optionally, it can be generated using [Inno Setup](https://jrsoftware.org/isinfo.php) and the included `EscapeDungen_setup.iss` script (see [Section 6](#6-building-the-windows-installer))

1. Run **`Escape-Dungeon_setup.exe`**.
2. Follow the on-screen wizard.
3. The game will be installed to `C:\Program Files\escape-dungeon` by default.
4. Launch from the Start Menu or Desktop shortcut.

---

## 3. Running the JAR Manually

If you have **Java 17+** installed on your system, you can run the game from the JAR file directly.

### Prerequisites

- [Adoptium Temurin JDK 17](https://adoptium.net/) or any other Java 17+ JDK/JRE.
- Verify your installation:
  ```bash
  java -version
  # Should show version 17 or later
  ```

### Steps

1. Obtain the JAR file:
   - Unzip pre-built: `EscapeDungeon-1.0.0-portable.zip` from releases 
   - The jar is in the folder (or build it yourself — see below).
2. Make sure the `assets/` folder is in the same directory as the JAR, or run from the project root.
3. Run:
   ```bash
   java -jar EscapeDungeon-1.0.0.jar
   ```
   On macOS, add the `-XstartOnFirstThread` flag:
   ```bash
   java -XstartOnFirstThread -jar EscapeDungeon-1.0.0.jar
   ```

---

## 4. Building from Source

### Prerequisites

| Requirement | Version | Notes                                                 |
|-------------|---------|-------------------------------------------------------|
| **JDK**     | 17+     | [Adoptium Temurin](https://adoptium.net/) recommended |
| **Git**     | any     | To clone the repository                               |

Gradle does **not** need to be installed separately — the included Gradle Wrapper (`gradlew` / `gradlew.bat`) handles everything.

### Clone & Run

```bash
git clone https://github.com/R4ZXRN3T/escape-dungeon.git
cd escape-dungeon
```

**Linux / macOS:**
```bash
./gradlew lwjgl3:run
```

**Windows (PowerShell or CMD):**
```powershell
.\gradlew.bat lwjgl3:run
```

### Build a Runnable JAR

```bash
# Cross-platform JAR (works on Windows, Linux, and macOS)
./gradlew lwjgl3:jar

# Platform-specific JARs (smaller size, strips unused native libraries)
./gradlew lwjgl3:jarWin      # Windows only
./gradlew lwjgl3:jarLinux    # Linux only
./gradlew lwjgl3:jarMac      # macOS only
```

The output JAR is located at:
```
lwjgl3/build/libs/EscapeDungeon-1.0.0.jar
```

### Useful Gradle Commands

| Command                          | Description                           |
|----------------------------------|---------------------------------------|
| `gradlew lwjgl3:run`             | Run the game directly                 |
| `gradlew lwjgl3:jar`             | Build a runnable JAR                  |
| `gradlew build`                  | Compile all modules                   |
| `gradlew clean`                  | Delete all build outputs              |
| `gradlew test`                   | Run unit tests                        |
| `gradlew --refresh-dependencies` | Force re-download of all dependencies |

---

## 5. Creating Distribution Packages

Distribution packages bundle the game JAR with a platform-specific Java 17 runtime so end users don't need Java installed. This uses the [Construo](https://github.com/fourlastor-jams/construo) Gradle plugin.

### Build Individual Platforms

```bash
./gradlew lwjgl3:packageWinX64      # Windows x86_64
./gradlew lwjgl3:packageLinuxX64    # Linux x86_64
./gradlew lwjgl3:packageMacX64     # macOS Intel
./gradlew lwjgl3:packageMacM1      # macOS Apple Silicon
```

Output ZIP files are placed in `lwjgl3/build/construo/dist/`.

### Build All Platforms at Once (Windows Only)

A PowerShell script is provided to build all four platform packages, copy assets, and create optimally compressed ZIP archives:

```powershell
.\buildall.ps1
```

**Requirements for `buildall.ps1`:**
- Windows with PowerShell
- [7-Zip](https://www.7-zip.org/) installed (must be on `PATH` or at `C:\Program Files\7-Zip\7z.exe`)

The final packages are placed in the `final/` directory.

---

## 6. Building the Windows Installer

The repository includes an [Inno Setup](https://jrsoftware.org/isinfo.php) script for creating a full Windows installer with Start Menu shortcuts, desktop icon, and a proper uninstaller.

### Prerequisites

- [Inno Setup 6](https://jrsoftware.org/isdl.php) installed on Windows.
- The Windows distribution package must already exist at `final/EscapeDungeon-1.0.0-Windows-x86_64/`.

### Steps

1. Build the Windows package first (see [Section 5](#5-creating-distribution-packages)).
2. Open `EscapeDungen_setup.iss` in the Inno Setup Compiler.
3. Click **Build → Compile** (or press `Ctrl+F9`).
4. The installer `Escape-Dungeon_setup.exe` will be created in the `final/` directory.

The installer:
- Installs to `C:\Program Files\escape-dungeon` by default.
- Creates Start Menu and optional Desktop shortcuts.
- Supports 30+ languages.
- On uninstall, offers to remove user save data from `%APPDATA%\escape-dungeon`.

---

## 7. Uninstallation & User Data

### User Data Location

The game stores save data (gold, owned weapons, equipped weapon, settings) in:

| Platform | Path                                            |
|----------|-------------------------------------------------|
| Windows  | `%APPDATA%\escape-dungeon\`                     |
| Linux    | `~/.config/escape-dungeon/`                     |
| macOS    | `~/Library/Application Support/escape-dungeon/` |

### Removing the Game

- **Portable (ZIP) install:** Simply delete the extracted folder. Optionally delete the user data directory listed above.
- **Windows Installer:** Use **Add or Remove Programs** in Windows Settings. The uninstaller will ask whether to also delete saved data.

---

## 8. Troubleshooting

### The game won't start

- **Verify Java version:** Run `java -version` and ensure it reports 17 or later. The pre-built packages include their own runtime, so this only matters when running the JAR manually.
- **Missing assets:** When running the JAR directly, make sure the `assets/` folder is in the working directory.

### Black screen or rendering issues

- Try running with the ANGLE renderer (included by default via `gdx-lwjgl3-angle`).
- Update your GPU drivers.

### macOS: "App is damaged" or "cannot be verified"

- Right-click the `.app` and select **Open**, or allow it in **System Settings → Privacy & Security**.
- If extracted via Terminal, you may also need:
  ```bash
  xattr -cr EscapeDungeon.app
  ```

### Gradle build fails

- Make sure you are using **JDK 17** (not a JRE, and not a newer incompatible version).
- Try `./gradlew --refresh-dependencies` to re-download dependencies.
- Run `./gradlew clean` before rebuilding.

### Performance issues

- Open **Settings** in-game and lower the FPS cap or disable VSync.
- Switch to **Windowed** mode instead of Fullscreen/Borderless.

---

*For further help, open an issue on [GitHub](https://github.com/R4ZXRN3T/escape-dungeon/issues).*

