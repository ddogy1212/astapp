# ASTAPP · Astronomy Studio 🌌

A monochrome Android astronomy explorer with 7 home-screen widgets, NASA archival imagery, JPL-based planetary orbits, and a location-aware observing guide.

**Build:** 0.4.0 beta · Java / OpenGL ES 2.0 · Android 8.0+ · no login or ads.

## Features (v0.4)
- **3D Solar Explorer:** GPU-rendered orbits in a J2000 heliocentric coordinate system using JPL approximate planetary elements for 1800–2050. Drag to orbit the camera, pinch to zoom; tap a planet to open its science card. Default inner-planet view prevents Mercury–Mars overlap, with outer/full buttons. Planet images are camera-facing **archival NASA photo sprites**, not photorealistic, rotating, correctly oriented 3D globes. Planet marker sizes are exaggerated for legibility.
- **Solar home widgets:** 2×2, 4×2, 4×4. Separate inner (0–2.15 AU) and outer (0–34.5 AU) *linear-distance* diagrams. Previous/now/next month controls directly on the home screen.
- **Cosmic Discovery widgets:** 2×1, 2×2, 4×2, 4×4. Actual planets, moons, dwarf planets, nebulae, star clusters, and galaxies; next/favorite actions. Tapping a widget opens the corresponding catalog object.
- **Cosmic Catalog:** 35 science cards with full-bleed NASA imagery when verified, distances and regions, accessible explanations, observation notes, source links, favorites gallery, search, category filters and random discovery. Next/save controls are visible immediately below the hero photo.
- **Observing guide:** user-triggered, optional foreground-only GPS, manual coordinates, 21:00 local-device-time plan; Open-Meteo cloud forecasts, solar altitude, approximate lunar illumination, astronomical target altitude and basic visibility scores. Five highest-ranked *above-horizon* candidates and their NASA links are shown. The score is an **informal heuristic**, not an observing success probability.
- **Moon:** NASA archived Moon image with calculated (simplified) phase shadow, time until full moon. **Not** real-time imagery or true sky orientation.
- **Events:** selected offline eclipse/meteor shower dates with links to NASA and IMO. It is **not** a live event service.
- **UI:** monochrome typography and cards with full-color genuine astronomical photos, five equal-width system-inset-aware bottom tabs, minimum 46dp tap surfaces, first-run tutorial, photo retry controls, clear attribution.

## Scientific limitations
The orbit calculations use JPL/NASA *Approximate Positions of the Planets* (1800–2050) rather than high-precision Horizons integrations. Earth uses the Earth–Moon barycenter approximation. Light-time, physical ephemerides, gravitational perturbations, precession corrections for deep-sky coordinates, atmospheric refraction, topography and daylight saving changes are not fully modelled. **Do not use ASTAPP for spacecraft navigation or safety-critical observations.**

A planet photo sprite is an *archival observation*, not an accurate 3D textured sphere, not a real-time view, and may be a scientific mosaic or image processed at non-visible wavelengths. Photos are obtained from NASA Images or NASA Science and credited with source IDs/URLs in the app. Missing images stay empty with an explanation; we do not invent photos. Selected NASA photos are downloaded during CI and bundled as offline assets; other photos use the NASA Images API and a local cache.

## Build + tests
- [Android APK workflow](.github/workflows/android.yml): fetches verified offline NASA previews; builds debug APK and runs `testDebugUnitTest`.
- [Pixel UI smoke workflow](.github/workflows/android-visual-smoke.yml): launches APK on Android 15 emulator, saves screenshots (home/catalog/observe), checks logcat for fatal app errors.
- JDK 17 + Android SDK 35 + Gradle 8.11.1:
```sh
gradle :app:assembleDebug :app:testDebugUnitTest
```
Output: `app/build/outputs/apk/debug/app-debug.apk`.

Download built APK from Actions → Android APK → latest successful run → Artifacts → **ASTAPP-debug-APK**. APK is a **debug beta**. Production release requires device QA (Samsung launcher including gestures, DPI and font scaling), a consistent signing key, Play Store checks, and explicit privacy disclosures.

## Privacy
GPS is optional and requested only if the user taps the location button. Chosen latitude/longitude and favorites are stored locally. The selected location coordinates are sent to **Open-Meteo** for cloud forecasts; the user can enter a location manually instead. NASA image queries are sent to NASA Images. There are no app accounts, personal photo uploads, background GPS requests, or advertising identifiers.

## Sources
- JPL orbital elements: https://ssd.jpl.nasa.gov/planets/approx_pos.html
- NASA Images API: https://images.nasa.gov/
- NASA Solar System: https://science.nasa.gov/solar-system/
- NASA Messier catalog: https://science.nasa.gov/mission/hubble/science/explore-the-night-sky/hubble-messier-catalog/
- NASA Hubble M13 photo: https://science.nasa.gov/image-detail/m13-xlarge_web/
- NASA eclipse resources: https://science.nasa.gov/eclipses/
- IMO meteor calendar: https://www.imo.net/calendar/
- Open-Meteo cloud forecasts: https://open-meteo.com/en/docs
