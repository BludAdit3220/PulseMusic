<div align="center">

<table>
  <tr>
    <td align="center" width="300">
      <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="140" height="140" alt="PulseMusic Logo">
      <h1>PulseMusic</h1>
    </td>
    <td align="center" width="300">
      <img src="assets/vinyl.svg" width="160" height="160" alt="Spinning Vinyl">
    </td>
  </tr>
</table>

<img src="assets/equalizer.svg" alt="Music Equalizer Animation" width="100%">

<br/>

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android Badge">
<img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Badge">
<img src="https://img.shields.io/badge/Architecture-Modular-000000?style=for-the-badge&logo=awsorganizations&logoColor=white" alt="Modular Architecture Badge">

<br/><br/>
<b>A modern, highly modular Android music player built with a sleek interface and powerful integrations. PulseMusic brings all your favorite music services and features together into one seamless, unified audio experience.</b>
<br/><br/>
</div>

<div align="center">
  <img src="assets/sound-wave.svg" width="100%" alt="Sound Wave Divider">
</div>

<table>
  <tr>
    <td width="75%">
      <h2><img src="https://raw.githubusercontent.com/FortAwesome/Font-Awesome/6.x/svgs/solid/compact-disc.svg" width="24" height="24" alt="Features Icon"> Features</h2>
      <ul>
        <li><b>Multi-Source Streaming</b> — Seamless playback integrating Spotify Core and YouTube (InnerTube).</li>
        <li><b>Advanced Lyrics</b> — Real-time synced lyrics powered by BetterLyrics, LrcLib, and Kugou.</li>
        <li><b>Music Recognition</b> — Identify songs playing around you instantly with the built-in ShazamKit module.</li>
        <li><b>Scrobbling</b> — Automatic track tracking through Last.fm integration.</li>
        <li><b>Canvas & Visuals</b> — Dynamic UI that responds to the music, including custom Turntable widgets.</li>
        <li><b>Modular Architecture</b> — Built from the ground up to be scalable, maintainable, and highly decoupled.</li>
      </ul>
    </td>
    <td width="25%" align="center">
      <img src="assets/floating-notes.svg" width="130" height="130" alt="Floating Notes">
    </td>
  </tr>
</table>

<div align="center">
  <img src="assets/sound-wave.svg" width="100%" alt="Sound Wave Divider">
</div>

## <img src="https://raw.githubusercontent.com/FortAwesome/Font-Awesome/6.x/svgs/solid/mobile-screen.svg" width="24" height="24" alt="Screenshots Icon"> App Gallery

<div align="center">
  <table>
    <tr>
      <td><img src="assets/Home.jpeg" width="250" alt="Home"/></td>
      <td><img src="assets/Library.jpeg" width="250" alt="Library"/></td>
      <td><img src="assets/Genres.jpeg" width="250" alt="Genres"/></td>
    </tr>
    <tr>
      <td><img src="assets/History.jpeg" width="250" alt="History"/></td>
      <td><img src="assets/History-2.jpeg" width="250" alt="History Details"/></td>
      <td><img src="assets/Mood.jpeg" width="250" alt="Mood"/></td>
    </tr>
  </table>
</div>

<div align="center">
  <img src="assets/sound-wave.svg" width="100%" alt="Sound Wave Divider">
</div>

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

<div align="center">
  <img src="assets/sound-wave.svg" width="100%" alt="Sound Wave Divider">
</div>

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