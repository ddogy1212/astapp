# ASTAPP 🌌
Android astronomy launcher widgets and an observing companion.

**Status:** v0.3.0 native Android beta (built by GitHub Actions). Built in native Java, min SDK 26. No user account or paid services.

## Features
- Three Solar-System widgets: 2×2, 4×2, 4×4. **Two separately scaled true-distance orbital panels**: inner 0–2.15 AU / outer 0–34.5 AU, with the actual JPL-derived azimuths and radii within each panel. NASA archived planet photographs are downloaded when available. Date buttons work directly on widgets.
- Four Cosmic Discovery widgets: 2×1, 2×2, 4×2, 4×4. Browse and save verified astronomical objects, offline labels and descriptions, NASA Images API photo retrieval with a local cache and clear attribution.
- Native app: gesture-controlled tilted solar system, time travel, observing planner, approximate lunar phase, events, and a cosmic object catalog.
- Widget buttons work on the home screen; tapping the image opens the relevant app section.
- **Location permissions are optional**: the observing site initially defaults to Seoul reference coordinates until the user actively taps the '실제 GPS 위치' button. The app asks for foreground location permission then and does not use background GPS. Manual location remains available.

## Scientific honesty
Solar positions are **approximations**, not byte-for-byte Horizons vectors. Based on NASA/JPL's *Approximate Positions of the Planets* orbital elements for 1800–2050 (J2000 ecliptic reference frame): https://ssd.jpl.nasa.gov/planets/approx_pos.html . The displayed Earth position uses the Earth–Moon barycenter as an approximation. Corrections for relativistic light-time, lunar perturbations and planetary surface spin are not included. Orbit and dot markers are deliberately enlarged; use the explicit linear-distance display for orbital spacing, or the compressed mode to see inner planets. This is not a spacecraft navigation tool.

Observing planner: geometrical altitude of planets for a user-selected reference location and time. It is **not** a guaranteed visibility forecast (haze, obstructions, moonlight, and cloud cover can affect viewing). Moon phase uses a simplified synodic approximation, clearly labeled. Events are a curated offline schedule, with links to NASA/IMO, **not** a live API feed or local eclipse visibility calculation. NASA imagery is fetched at runtime from public NASA Images API search when internet exists; images might be missing or held under third-party copyright. A cached NASA image is an illustrative photograph, not necessarily the astronomical object in the same time/zoom as the rendering.

## v0.3 UX and catalog changes\n- Discovery now includes 35 curated objects and first-class planet, moon, dwarf planet, nebula, cluster, galaxy, and star filters. In-app random discovery and text search included.\n- Widgets show verified NASA planet/solar-system archive photos when found (not fabricated renders). Authentic imagery keeps its scientific colors; the rest of the UI is monochrome.\n- Tapping a widget photo opens that exact object's details, not a different random item.\n- Android 15/16 system status/navigation bar and gesture insets are applied to the root. Bottom navigation is placed **above** the OS bar in a compact five-tab layout.\n- No photo is claimed live; some NASA image searches fail or supply only a source link.\n\n## Build APK
GitHub Actions workflow **Android APK** runs on push to main and can also be started manually. Open Actions → Android APK → latest successful run → Artifacts → **ASTAPP-debug-APK**, unzip, install APK.
Local build (JDK 17 + Android SDK 35):
```sh
gradle :app:assembleDebug
```
APK at `app/build/outputs/apk/debug/app-debug.apk`. Debug builds are test versions; release distribution needs signed release artifacts, policy review, tests and a privacy notice.

## Accuracy and design notes\nPlanetary photographs are archived NASA mission imagery, not live snapshots, and might be unavailable until downloads finish. The NASA moon photograph is also archival; its current-phase shadow is computationally applied and is *not* a live or pixel-accurate Moon view. Not every faint star or nebula has a true optical photograph; in such cases the app presents source text instead of fabricated pictures. The geocentric topocentric planet altitude calculations are approximations without full light-time/refraction/parallax corrections. The observation ranking is based on altitude, **not** a complete visibility or cloud-risk score.\n\n## Privacy / sources
Preferences and favorites are only stored on-device. The app only reads location after the user taps the GPS button. The last chosen coordinates and favorites are stored on-device. When weather forecast is requested, selected coordinates are sent to Open-Meteo. Planet and nebula photos are downloaded from NASA Images. The app does not upload personal photos. When a NASA object is viewed, a public image search request may be sent to images.nasa.gov and images-assets.nasa.gov. External URLs for cited facts: NASA/JPL, NASA Science Hubble Messier Catalog, NASA Images, NASA Eclipse, International Meteor Organization. See SOURCES.md.
