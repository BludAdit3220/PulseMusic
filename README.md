<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="PulseMusic Logo">

# PulseMusic

<img src="https://raw.githubusercontent.com/npanuhin/npanuhin/main/assets/equalizer.svg" alt="Music Equalizer Animation" width="100%">

<br/>

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android Badge">
<img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Badge">
<img src="https://img.shields.io/badge/Architecture-Modular-000000?style=for-the-badge&logo=awsorganizations&logoColor=white" alt="Modular Architecture Badge">

<br/>

A modern, highly modular Android music player built with a sleek interface and powerful integrations. PulseMusic brings all your favorite music services and features together into one seamless, unified audio experience.

</div>

---

## <img src="https://raw.githubusercontent.com/FortAwesome/Font-Awesome/6.x/svgs/solid/compact-disc.svg" width="24" height="24" alt="Features Icon"> Features

* **Multi-Source Streaming** — Seamless playback integrating Spotify Core and YouTube (InnerTube).
* **Advanced Lyrics** — Real-time synced lyrics powered by BetterLyrics, LrcLib, and Kugou.
* **Music Recognition** — Identify songs playing around you instantly with the built-in ShazamKit module.
* **Scrobbling** — Automatic track tracking through Last.fm integration.
* **Canvas & Visuals** — Dynamic UI that responds to the music, including custom Turntable widgets.
* **Modular Architecture** — Built from the ground up to be scalable, maintainable, and highly decoupled.

---

## <img src="https://raw.githubusercontent.com/FortAwesome/Font-Awesome/6.x/svgs/solid/mobile-screen.svg" width="24" height="24" alt="Screenshots Icon"> App Gallery

<div align="center">
  <table>
    <tr>
      <td><img src="assets/WhatsApp%20Image%202026-11-11%20at%201.17.43%20PM.jpeg" width="250" alt="Screenshot 1"/></td>
      <td><img src="assets/WhatsApp%20Image%202026-11-11%20at%201.17.44%20PM.jpeg" width="250" alt="Screenshot 2"/></td>
      <td><img src="assets/WhatsApp%20Image%202026-11-11%20at%201.17.44%20PM%20(1).jpeg" width="250" alt="Screenshot 3"/></td>
    </tr>
    <tr>
      <td><img src="assets/WhatsApp%20Image%202026-11-11%20at%201.17.44%20PM%20(2).jpeg" width="250" alt="Screenshot 4"/></td>
      <td><img src="assets/WhatsApp%20Image%202026-11-11%20at%201.17.45%20PM.jpeg" width="250" alt="Screenshot 5"/></td>
      <td><img src="assets/WhatsApp%20Image%202026-11-11%20at%201.17.45%20PM%20(1).jpeg" width="250" alt="Screenshot 6"/></td>
    </tr>
  </table>
</div>

---

## <img src="https://raw.githubusercontent.com/FortAwesome/Font-Awesome/6.x/svgs/solid/layer-group.svg" width="24" height="24" alt="Modules Icon"> Modular Structure

PulseMusic is designed using a multi-module architecture to separate concerns and speed up compilation:

* **`:app`** — The main application entry point and presentation layer.
* **`:betterlyrics` & `:lrclib`** — Core components for fetching and parsing lyrics.
* **`:innertube`** — YouTube music integration and streaming layer.
* **`:spotifycore`** — Handling interactions with the Spotify platform.
* **`:lastfm`** — Last.fm account connection and scrobbling.
* **`:shazamkit`** — Audio recognition engine.
* **`:canvas`** — Immersive background and album art rendering.
* **`:kugou`, `:paxsenix`, `:simpmusic`, `:unison`** — Extended plugins and service providers for the ultimate listening experience.

---

## <img src="https://raw.githubusercontent.com/FortAwesome/Font-Awesome/6.x/svgs/solid/code.svg" width="24" height="24" alt="Build Icon"> Getting Started

To build the project locally, clone the repository and build via Android Studio or Gradle:

```bash
git clone https://github.com/yourusername/PulseMusic.git
cd PulseMusic
./gradlew assembleDebug
```

<div align="center">
  <br/>
  <img src="https://img.shields.io/badge/Made_with-Kotlin-7F52FF?style=for-the-badge&logo=kotlin" alt="Made with Kotlin">
  <img src="https://img.shields.io/badge/Open_Source-Yes-4CAF50?style=for-the-badge" alt="Open Source">
</div>
