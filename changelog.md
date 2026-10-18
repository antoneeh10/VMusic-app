# Changelog

<<<<<<< HEAD
## [v1.1.0] - 2026-10-18
>>>>>>> 142e030 (feat: add Spotify Canvas and experimental Spatial Sound)

### Added
- **player:** add Spotify Canvas backgrounds for a more immersive playback experience.
- **audio:** introduce experimental Spatial Sound support. Use with caution, as this feature is experimental.

### Changed
- **artist:** redesign the Artist Page with an updated user interface.

### Fixed
- **player:** fix an issue where song cover artwork failed to appear.

## [v1.0.0] - 2026-10-15

### Added
- **branding:** rebrand the application from Metrolist to VMusic, including UI strings, namespaces, and package structure.
- **lyrics quote:** add four quote templates: Default, FilmStrip, VinylRecord, and MusicNote, with SVG vector icons.
- **lyrics quote:** add a Kotlin Template DSL contribution dialog with copy-to-clipboard support for creating custom templates.
- **navigation:** introduce the Material 3 Expressive Floating Capsule Menu with animated tab transitions and expandable active tabs.
- **lyrics:** add Apple Music-inspired lyric animations with distance-based alpha dimming and subtle vertical lift effects.
- **lyrics:** add a three-dot instrumental interval indicator with progressive color filling.
- **player:** add fluid animated blurred backgrounds with organic motion, breathing-scale effects, and smooth transitions.
- **settings:** add a dedicated Experimental Features section with a toggle for the Floating Capsule Menu and a warning dialog.
- **updates:** integrate GitHub Releases API for checking stable application updates.
- **updates:** add direct APK download links for FOSS and GMS (Cast) variants.
- **credits:** add an Acknowledgments section to recognize the original Metrolist developers.

### Changed
- **package:** migrate the application package from `com.metrolist.music` to `com.vmusic.takeserver`.
- **player:** set the blurred background as the default player background.
- **player:** set the default playback slider style to Slim (`SliderStyle.SLIM`).
- **lyrics:** enable experimental lyrics by default.
- **display:** enable high refresh rate support by default.
- **settings:** move Profile and Integrations into the main settings page.
- **navigation:** replace the top-right profile icon with a direct Settings shortcut.
- **lyrics quote:** hide background color customization when a predefined template is selected.
- **about:** recognize Ardian as the lead developer and move original project credits to Acknowledgments.
- **version:** set the application version to `1.0.0` (`versionCode = 1`).

### Improved
- **updates:** validate semantic version numbers to exclude beta and release candidate versions.
- **updates:** add in-memory caching and coroutine mutex synchronization to reduce redundant GitHub API requests.
- **player:** improve background animation continuity using sinusoidal motion, organic rotation, and reverse repeat animations.
- **navigation:** refine capsule dimensions, horizontal padding, and spring-based transitions for a smoother navigation experience.
