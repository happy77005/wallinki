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
    git clone https://github.com/yourusername/walllinki.git
    ```
2.  **Open in Android Studio**:
    Make sure you have the latest version of Bumblebee or newer.
3.  **Build & Run**:
    Connect your device and click the "Run" button or use Gradle:
    ```bash
    ./gradlew assembleDebug
    ```
4.  **Set as Wallpaper**:
    Once installed, open Walllinki, add your images, and tap **"Apply Dynamic Wallpaper"**. Follow the system prompts to set Walllinki as your Active Live Wallpaper.

---

## 📸 Preview

| Home Screen | Image Selection |
| :---: | :---: |
| ![Header](C:/Users/harip/.gemini/antigravity/brain/8b1f10ef-5852-4934-af2b-96519a342a4a/walllinki_logo_1773060998597.png) | *(Add your screenshot here)* |

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*Crafted with ❤️ for a better Android experience.*
