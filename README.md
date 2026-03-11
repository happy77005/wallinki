# Walllinki 🎨 - Dynamic Harmony Wallpaper Engine

Walllinki is a premium Android application designed to bring life to your device's home screen. It automates wallpaper transitions based on the time of day, allowing you to curate different "moods" for your morning and evening routines.

Built with a focus on **aesthetics**, **stability**, and **battery efficiency**.

---

## ✨ Features

-   **🌅 Morning & Evening Slots**: Curate separate sets of wallpapers for the day (6 AM - 6 PM) and night (6 PM - 6 AM).
-   **💎 Glassomorphic UI**: A sleek, modern interface built with Jetpack Compose, featuring background blurs, gradients, and micro-animations.
-   **🔄 Dynamic Wallpaper Service**: A custom-built engine that renders your selected images as a Live Wallpaper.
-   **🔋 Battery Optimized**: Uses Android's `WorkManager` for background scheduling, ensuring minimal impact on battery life.
-   **🔐 Secure Access**: Leverages persistable URI permissions to access your photos locally without needing broad storage access.

---

## 🛠️ Technical Stack

-   **Language**: Kotlin
-   **UI**: Jetpack Compose (Material 3)
-   **Background Work**: WorkManager
-   **Wallpaper Engine**: Android `WallpaperService` (Live Wallpaper)
-   **Image Loading**: Coil
-   **Persistence**: SharedPreferences with `kotlinx-serialization`

---

## 🚀 Installation & Setup

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/happy77005/wallinki.git
    ```
2.  **Open in Android Studio**:
    Make sure you have the latest version of Bumblebee or newer.

---

## 🏗️ Architecture

Walllinki is built with a robust, modular architecture designed for performance and reliability:

-   **UI Layer (Jetpack Compose)**: Utilizes modern declarative UI to provide a high-end Glassomorphic user experience. State management is handled with Compose's `remember` and `mutableState` to ensure a reactive interface.
-   **Service Layer (Live Wallpaper Engine)**: A custom `DynamicWallpaperService` extending Android's `WallpaperService`. It handles the low-level lifecycle of a live wallpaper, including surface management and high-performance bitmap rendering centered on the canvas.
-   **Background Layer (WorkManager)**: Employs a `PeriodicWorkRequest` to handle time-based triggers. This ensures that the wallpaper updates at the precise 6 AM and 6 PM milestones without requiring the app to be in the foreground, all whilst remaining battery-efficient.
-   **Data & Persistence Layer**:
    -   **SharedPreferences**: Stores user settings and chosen image URIs.
    -   **kotlinx-serialization**: Serializes the complex `TimeSlot` data class (and its associated URIs) into JSON for robust persistent storage.
    -   **Persistable URIs**: Implements secure media access by requesting long-term permissions for user-selected assets, ensuring the app retains access across device reboots.

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*Crafted with ❤️ for a better Android experience.*
