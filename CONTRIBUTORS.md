# PulseMusic Contributors

PulseMusic is a free/libre software music player released under the GNU General Public License v3.0.

## Derived from ArchiveTune

**Copyright © Aditya Parasher and ArchiveTune contributors**

Original project: https://github.com/BludAdit3220/ArchiveTune  
License: GNU General Public License v3.0

Components derived from ArchiveTune include (but are not limited to):
- Core MVVM architecture and Hilt dependency injection setup
- `:innertube`, `:canvas`, `:shazamkit`, `:spotifycore`, `:unison`, `:simpmusic`, `:paxsenix` modules
- `FloatingNavigationToolbar` composable (M3 Expressive floating pill navigation)
- Full player UI: `Player.kt`, `PlayerComponents.kt`, `MiniPlayer.kt`
- Canvas artwork player (`CanvasArtworkPlayer.kt`)
- `LyricsV2.kt`, `LyricsEnhanced.kt`, `LyricsGlassStyle.kt` (Glass Lyrics system)
- `PulseMusicTheme.kt` (adapted from `ArchiveTuneTheme.kt`)
- Album-driven color extraction and MaterialKolor palette system
- AOD player screen (`AodPlayerScreen.kt`)
- `WavySlider.kt` and playback components
- Library screens with tag system
- All widget receivers and playback services
- Discord RPC integration

---

## Derived from PulseMusic

**Copyright © Metrolist contributors**

Original project: https://github.com/metrolist/Metrolist  
License: GNU General Public License v3.0

Components derived from Metrolist include (but are not limited to):
- `HorizontalMultiBrowseCarousel` home screen pattern
- EQ screen and frequency response graph (`ui/screens/equalizer/`)
- Wrapped / Year in Music screens (`ui/screens/wrapped/`)
- Podcast support screens (`ui/screens/podcast/`)
- `SquigglySlider.kt`, `VolumeSlider.kt`
- `AppNavigation.kt` (NavigationRail for tablets)
- `ListenTogetherScreen.kt` and `ListenTogetherManager.kt`
- `StatsScreen.kt`, `RecognitionScreen.kt`, `RecognitionHistoryScreen.kt`
- `DraggableLyricsProviderList.kt`, `DraggableScrollBarOverlay.kt`
- `Material3SettingsGroup.kt`, `SettingsSleepTimerDialog.kt`
- `EnumDialog.kt`, `ExpandableText.kt`, `IntegrationCard.kt`
- Alarm/sleep timer settings

---

## PulseMusic Original Contributions

**Copyright © PulseMusic contributors**

New code written for PulseMusic that did not exist in either source project:
- `PulseMusicTheme.kt` — vault amber identity, warm dark surface overrides, merged multi-seed palette
- `Typography.kt` — DM Serif Display + Plus Jakarta Sans + JetBrains Mono type system
- `PulseMusicShell.kt` — adaptive shell composable (phone/tablet layout switcher)
- `HeroCarouselSection.kt` — M3 HorizontalMultiBrowseCarousel hero section
- `GlassPlayerBackground.kt` — combined Canvas artwork + parallax blur player background
- Merged `AndroidManifest.xml` (AOD activity, ListenTogether service, PulseMusic scheme)
- `app/build.gradle.kts` — merged build config with foss/gms flavors

---

## How to Contribute

PulseMusic is open-source under GPL-3.0. Contributions are welcome.  
Please read CONTRIBUTING.md (when created) before submitting pull requests.

All contributions must be compatible with GPL-3.0 and may not include proprietary code.

---

*This file is maintained as part of PulseMusic's GPL-3.0 compliance obligations.*
*Both source projects were GPL-3.0 licensed; all derived works must remain GPL-3.0.*
