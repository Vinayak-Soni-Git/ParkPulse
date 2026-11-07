# ParkPulse – Smart City Park Survey Android App

**Discover. Survey. Preserve.**

ParkPulse is an offline-first Android application designed to help users manage, inspect, and survey public parks. It provides interactive park maps, geographic boundary drawing, GPS-based geofence validation, digital park surveys, camera-based evidence collection, and local-first data synchronization.

The application is built with **Kotlin, Jetpack Compose, Clean Architecture, and MVVM**, with a focus on maintainability, reliable offline functionality, and a modern user experience.

---

## Features

### 1. Authentication
- Mock email/password authentication.
- Persistent login session using DataStore.
- Automatic session restoration.
- Logout with local session clearing.
- Splash screen with animated branding.

### 2. Parks Dashboard
- View available parks in a dashboard.
- Search parks by name.
- Filter parks by their current status.
- Display park type, area, last survey date, and synchronization status.
- Local park data persistence using Room.
- Preloaded Jaipur park dataset.

### 3. Interactive Park Map
- Interactive map powered by osmdroid.
- Display park boundaries as geographic polygons.
- Different polygon colors based on park status.
- Display the user's current location.
- Select a park to view its details.
- Show park area, distance from the user, and compass direction.

### 4. Create New Park
- Create park boundaries by tapping on the map.
- Add at least three polygon vertices.
- Drag existing vertices to adjust boundaries.
- Undo, clear, cancel, and finish polygon drawing.
- Calculate area and perimeter.
- Save newly created parks locally.

### 5. GPS-Based Survey and Geofence Validation
- Validate whether the user's location is inside the selected park boundary.
- Require GPS accuracy of 25 metres or better.
- Prevent survey check-in when location validation fails.
- Record check-in coordinates, timestamp, and GPS accuracy.
- Record:
  - Number of trees.
  - Availability of lights.
  - Cleanliness condition.
  - Survey notes.
- Save survey records locally.

### 6. CameraX Photo Capture
- CameraX-powered camera preview and capture.
- Capture photographic evidence for park surveys.
- Permanently burn survey metadata into image pixels.
- Watermark includes park information, coordinates, GPS accuracy, date, and time.
- Preview, retake, and save captured photos.
- Store photos locally and associate them with survey records.
- Debug-only camera test mode for testing without an active park survey.

### 7. Offline-First Synchronization
- Store parks, surveys, and photos locally before synchronization.
- Observe network connectivity.
- Manual Sync Now functionality.
- Synchronize parks, surveys, and photos in dependency order.
- Track synchronization states:
  - PENDING
  - SYNCING
  - SYNCED
  - FAILED
- Retry failed synchronization operations.
- Preserve local data when offline or when synchronization fails.
- Mock backend abstraction for simulating server responses.
- Idempotency and dependency-aware synchronization.
- Conflict handling that preserves local records.

---

## Technology Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Design System | Material 3 |
| Architecture | Clean Architecture + MVVM |
| Dependency Injection | Hilt |
| Local Database | Room |
| Preferences | DataStore |
| Asynchronous Operations | Kotlin Coroutines and Flow |
| Navigation | Navigation Compose |
| Maps | osmdroid |
| Location | Fused Location Provider |
| Camera | CameraX |
| Image Loading | Coil |
| Animations | Lottie Compose |
| Build System | Gradle with Kotlin DSL |
| Code Generation | KSP |

---

## Architecture

ParkPulse follows Clean Architecture principles with separation between presentation, domain, and data responsibilities.

### Architecture overview

```text
                PRESENTATION
        ┌─────────────────────────┐
        │   Jetpack Compose UI    │
        │                         │
        │ Screens and Components │
        │       ViewModels        │
        └────────────┬────────────┘
                     │
                     ▼
                   DOMAIN
        ┌─────────────────────────┐
        │       Use Cases         │
        │                         │
        │ Repository Interfaces  │
        │ Business Rules          │
        │ GIS Calculations        │
        └────────────┬────────────┘
                     │
                     ▼
                    DATA
        ┌─────────────────────────┐
        │ Repository Implementations│
        │                         │
        │ Room Database           │
        │ DataStore               │
        │ Mock Backend API        │
        │ Network Monitor         │
        │ Local Photo Storage     │
        └─────────────────────────┘
```

### Architectural principles

- Separation of concerns.
- Repository pattern.
- Dependency inversion.
- Reactive UI updates using Flow and StateFlow.
- Dependency injection using Hilt.
- Local database as the primary source of application data.
- Testable business logic separated from Android UI components.

---

## 📐 Geographic Calculations

ParkPulse uses geographic coordinates rather than screen pixels for its park boundary calculations.

### 1. Area calculation

Park boundaries are represented as latitude/longitude polygon vertices.

The area calculation uses geographic coordinates and spherical/geodesic calculations to estimate the surface area of the polygon on Earth.

The resulting area is displayed in:

- Square metres (m²).
- Acres.

Conversion:

```text
1 acre = 4046.8564224 square metres
```

### 2. Perimeter calculation

The perimeter is calculated by summing the geographic distances between consecutive polygon vertices, including the closing edge between the last and first vertices.

The calculation uses Earth-surface distance rather than straight-line screen coordinates.

### 3. Point-in-polygon validation

To determine whether a user is inside a park:

1. Obtain the user's current GPS latitude and longitude.
2. Retrieve the selected park's polygon boundary.
3. Validate the polygon and coordinate data.
4. Apply a point-in-polygon algorithm.
5. Separately validate GPS accuracy.
6. Permit check-in only when the point is inside the valid boundary and GPS accuracy is no greater than 25 metres.

Invalid or unverified boundaries must not be treated as confirmed geofence boundaries.

### 4. Distance and direction

- Distance is calculated between the user's location and the selected park's geographic reference point.
- Compass direction is derived from the bearing between the two geographic positions.

---

## 📡 Offline-First Design

Offline functionality is a core part of ParkPulse.

The application prioritizes local persistence so that users can continue working without an internet connection.

### Local-first workflow

```text
       User creates a record
                 |
                 ▼
          Save in Room
                 |
                 ▼
          Mark as PENDING
                 |
                 ▼
        Check connectivity
                 |
          ┌──────┴──────┐
          │             │
        Offline       Online
          │             │
          ▼             ▼
      Keep local    Start Sync
          │             │
          │             ▼
          │       Upload Park
          │             │
          │             ▼
          │       Upload Survey
          │             │
          │             ▼
          │       Upload Photos
          │             │
          │             ▼
          │       Update Status
          │
          ▼
      Retry later
```

### Synchronization strategy

The synchronization engine processes records in dependency order:

1. Parks.
2. Surveys.
3. Survey photos.

A survey depends on its associated park being acknowledged by the backend. A photo depends on its associated survey being acknowledged.

### Sync states

| State | Description |
|---|---|
| PENDING | Saved locally and awaiting synchronization |
| SYNCING | Synchronization operation is in progress |
| SYNCED | Backend successfully acknowledged the record |
| FAILED | Synchronization attempt failed and can be retried |

### Conflict resolution

The current mock synchronization implementation follows a local-first strategy:

- Local records are retained until successful backend acknowledgement.
- Failed uploads remain available for retry.
- Stable identifiers and idempotency keys help prevent duplicate creation.
- Dependent records are not uploaded before their parent records are acknowledged.
- Local changes are preserved rather than silently overwritten.
- Failed synchronization does not delete local parks, surveys, or photos.

### Concurrent Offline Edit Handling

The current ParkPulse implementation supports creating parks offline and synchronizing pending records through a mock backend. Editing existing parks is not currently supported, so concurrent offline park-edit conflicts are not yet applicable to the implemented application.

For a future multi-device implementation, I would introduce optimistic concurrency control using server-side version numbers. If two devices edit the same park from the same version, the backend would detect the version mismatch and return a conflict.

Independent field changes could be merged where safe, while conflicting changes to the same field or park polygon geometry would require explicit resolution. Both versions would be preserved until the conflict is resolved, preventing silent data loss.

### Mock backend

ParkPulse uses an in-process mock backend abstraction to simulate server communication.

It supports simulated delays, intermittent failures, and successful acknowledgements without requiring a deployed backend.

This makes it possible to demonstrate synchronization workflows during development and testing.

**Limitation:** The mock backend does not provide real remote cloud persistence. A production deployment would require a real backend service.

---

## Park Dataset and Map Attribution

The initial park dataset is based on OpenStreetMap geographic data for Jaipur, Rajasthan, India.

The dataset was processed into a local JSON seed file and imported into Room during application initialization.

- Source: [OpenStreetMap](https://www.openstreetmap.org/)
- Data access: [Overpass API](https://overpass-api.de/)
- License: [Open Database License (ODbL)](https://opendatacommons.org/licenses/odbl/)

Park boundary data is sourced from OpenStreetMap and should be considered mapped but not independently ground-truthed. Some boundaries may require field verification.

Park operational statuses are demonstration values and should not be interpreted as verified municipal information.

OpenStreetMap map tiles are subject to the tile provider's usage policy. Offline park records and overlays are stored locally; full offline basemap availability depends on map tile caching.

---

## Getting Started

### Prerequisites

- Android Studio.
- JDK 17.
- Android SDK.
- Android device or emulator.
- Git.

### Clone the repository

```bash
git clone https://github.com/Vinayak-Soni-Git/ParkPulse.git

cd ParkPulse
```

### Open the project

1. Open Android Studio.
2. Select Open Project.
3. Choose the cloned ParkPulse directory.
4. Allow Gradle synchronization to finish.

### Build the application

Windows:

```bash
gradlew.bat assembleDebug
```

macOS/Linux:

```bash
./gradlew assembleDebug
```

### Run tests

Windows:

```bash
gradlew.bat test
```

macOS/Linux:

```bash
./gradlew test
```

### Install on a connected Android device

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Configuration note:** If your local project requires additional SDK or map configuration, follow the relevant example configuration files. Never commit private credentials, signing keys, or local machine configuration.

---

## 🔐 Demo Login

The application uses mock authentication for demonstration purposes.

| Field | Value |
|---|---|
| Email | `admin@parkpulse.com` |
| Password | `ParkPulse@123` |

These are demonstration credentials, not production authentication credentials.

---

## APK Download

The release APK will be provided through the GitHub Releases section.

**[View ParkPulse Releases](https://github.com/Vinayak-Soni-Git/ParkPulse/releases)**

Download the latest release APK and install it on a compatible Android device.

---

## Security and Permissions

ParkPulse follows a permission-on-demand approach.

- Foreground location permission is requested when location-dependent functionality is used.
- Camera permission is requested when camera functionality is first accessed.
- Permission rationale is displayed when appropriate.
- Denied permissions are handled gracefully.
- Users can be directed to Android Settings when permission is permanently denied.
- Background location access is not required.

Signing credentials and private configuration files must remain outside version control.

---

## Testing

The project includes development and testing support for core functionality.

Important scenarios to verify include:

- Authentication and session restoration.
- Park database initialization.
- Search and status filtering.
- Polygon area and perimeter calculations.
- Point-in-polygon validation.
- GPS accuracy validation.
- Survey persistence.
- Camera capture and watermarking.
- Offline record creation.
- Synchronization success and failure.
- Retry and duplicate prevention.
- Database persistence after application restart.

Debug-only testing utilities must not be exposed in release builds.

---

## Future Improvements

Given additional development time, the following improvements would be valuable:

- Integrate a production backend with authenticated REST APIs.
- Implement secure server-side synchronization.
- Add administrator and surveyor role management.
- Introduce richer survey analytics and reporting.
- Add automated GIS boundary validation.
- Improve map tile caching for offline basemap access.
- Add robust background synchronization using WorkManager.
- Implement server-driven conflict resolution and audit history.
- Add automated UI and end-to-end test coverage.
- Support multiple cities and configurable park datasets.

---

## AI Tools and Usage Disclosure

AI tools were used during the development of ParkPulse.

**Tools used:**
- Android Studio AI coding agent.
- ChatGPT.

**Areas where AI assistance was used:**
- Implementation planning and feature breakdown.
- Architecture suggestions and code generation.
- Jetpack Compose UI development and refinement.
- Room database and repository implementation assistance.
- Debugging and error investigation.
- CameraX and offline synchronization implementation assistance.
- Test-case suggestions and documentation preparation.

AI-generated suggestions and code were reviewed, integrated, tested, and refined during development. I take responsibility for understanding the submitted implementation and explaining its architectural decisions and behavior.

---

## Developer

**Vinayak Soni**

Android Developer

GitHub: [Vinayak-Soni-Git](https://github.com/Vinayak-Soni-Git)

---

## 📄 License

This project was developed as an Android Developer technical assignment.

Third-party libraries and datasets remain subject to their respective licenses and attribution requirements.
