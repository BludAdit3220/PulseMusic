# PulseMusic - IzzyOnDroid Release

**PulseMusic** is a modern, privacy-centric, and feature-rich music streaming client for Android. Built with a focus on performance and a clean aesthetic, it provides a premium listening experience by leveraging the power of YouTube Music while adhering to the latest Material 3 design standards.

## Key Features

*   **Ad-Free Experience:** Stream your favorite music without any interruptions or advertisements.
*   **Background Playback:** Keep the music going even when your screen is off or you are multitasking.
*   **Material You Integration:** A beautiful, dynamic UI that harmonizes with your system's wallpaper and theme.
*   **High-Quality Audio:** Powered by **Media3** and **ExoPlayer** for a robust and stable audio engine.
*   **Lyrics Synchronization:** Integrated with **LrcLib** and **BetterLyrics** for real-time, synchronized lyrics.
*   **Scrobbling Support:** Native integration with **Last.fm** to track your listening habits.
*   **Fast and Lightweight:** Optimized for speed and battery efficiency using **Kotlin Coroutines** and **Ktor**.
*   **Privacy-First:** No trackers, no data collection, and no unnecessary permissions.

## Technical Highlights

PulseMusic is architected using modern Android development practices:
- **Jetpack Compose:** For a fluid and reactive user interface.
- **Hilt:** For robust dependency injection.
- **Room Database:** For efficient local data management and caching.
- **Multi-Module Architecture:** Ensures maintainability and separation of concerns across features like `innertube`, `kugou`, and `lrclib`.

## IzzyOnDroid Edition

This specific release is optimized for the **IzzyOnDroid** repository:
- **Zero Proprietary Bloat:** All Google Play Services (GMS) dependencies, such as Google Cast, have been removed.
- **No Self-Updater:** The built-in updater is disabled to allow the IzzyOnDroid store to manage updates seamlessly.
- **FOSS Friendly:** Compiled with a focus on open-source compatibility.

Experience the pulse of your music. PulseMusic is more than just a player; it's a refined way to connect with the songs you love.
