# ASTAPP 🌌
Android astronomy launcher widgets and an observing companion.

**Status:** Early functional prototype. Built in native Java, min SDK 26. No user account or paid services.

## Features
- Three Solar-System widgets: 2×2, 4×2, 4×4. Top-down J2000 ecliptic view, independently adjustable dates, return-to-now. Drawn using orbital coordinates rather than arbitrary planet placement.
- Four Cosmic Discovery widgets: 2×1, 2×2, 4×2, 4×4. Browse and save verified astronomical objects, offline labels and descriptions, NASA Images API photo retrieval with a local cache and clear attribution.
- Native app: gesture-controlled tilted solar system, time travel, observing planner, approximate lunar phase, events, and a cosmic object catalog.
- Widget buttons work on the home screen; tapping the image opens the relevant app section.
- No location permissions: initial observing site is **Seoul (reference coordinates, not a GPS position)**, editable by the user.

## Scientific honesty
Solar positions are **approximations**, not byte-for-byte Horizons vectors. Based on NASA/JPL's *Approximate Positions of the Planets* orbital elements for 1800–2050 (J2000 ecliptic reference frame): https://ssd.jpl.nasa.gov/planets/approx_pos.html . The displayed Earth position uses the Earth–Moon barycenter as an approximation. Corrections for relativistic light-time, lunar perturbations and planetary surface spin are not included. Orbit and dot markers are deliberately enlarged; use the explicit linear-distance display for orbital spacing, or the compressed mode to see inner planets. This is not a spacecraft navigation tool.

Observing planner: geometrical altitude of planets for a user-selected reference location and time. It is **not** a guaranteed visibility forecast (haze, obstructions, moonlight, and cloud cover can affect viewing). Moon phase uses a simplified synodic approximation, clearly labeled. Events are a curated offline schedule, with links to NASA/IMO, **not** a live API feed or local eclipse visibility calculation. NASA imagery is fetched at runtime from public NASA Images API search when internet exists; images might be missing or held under third-party copyright. A cached NASA image is an illustrative photograph, not necessarily the astronomical object in the same time/zoom as the rendering.

## Build APK
GitHub Actions workflow **Android APK** runs on push to main and can also be started manually. Open Actions → Android APK → latest successful run → Artifacts → **ASTAPP-debug-APK**, unzip, install APK.
Local build (JDK 17 + Android SDK 35):
```sh
gradle :app:assembleDebug
```
APK at `app/build/outputs/apk/debug/app-debug.apk`. Debug builds are test versions; release distribution needs signed release artifacts, policy review, tests and a privacy notice.

## Privacy / sources
Preferences and favorites are only stored on-device. The app does not record GPS or upload personal photos. When a NASA object is viewed, a public image search request may be sent to images.nasa.gov and images-assets.nasa.gov. External URLs for cited facts: NASA/JPL, NASA Science Hubble Messier Catalog, NASA Images, NASA Eclipse, International Meteor Organization. See SOURCES.md.
