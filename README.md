# Space Colony: NOVA STATION-7

A turn-based Android crew management game built in Java.

---

## Getting Started

### Requirements
- Android Studio Hedgehog or newer
- Java 8
- Android SDK 34 (min API 26)

### Clone and Run
```bash
git clone https://github.com/MjMahim/SpaceColony_NovaStation_7.git
```
Open the project in Android Studio, let Gradle sync, then run on an emulator or physical device. No API keys or external configuration needed.

---
```
## Project Structure
app/src/main/java/com/spacecolony/
├── model/          # All game logic — CrewMember, Threat, Storage, etc.
├── adapter/        # RecyclerView adapters
├── ui/             # Fragments (one per screen)
└── MainActivity.java
The model layer has zero Android dependencies — pure Java. The UI layer (Fragments) talks to the model through the Singleton accessors (`Storage.getInstance()`, etc.) and drives the combat loop directly in `MissionControlFragment`.
```
---

## Architecture Notes

**Inheritance chain**  
`CrewMember` (abstract) → `Pilot` / `Engineer` / `Medic` / `Scientist` / `Soldier`

Each subclass overrides `act()` with its own damage formula. `Soldier` additionally overrides `defend()`. The Fragment calls `actor.act()` without knowing the concrete type — standard polymorphism.

**Singletons**  
Three classes use the Singleton pattern: `Storage` (crew registry), `MedbayManager` (recovery queue), `StatisticsManager` (per-crew stats). All backed by `HashMap<Integer, T>` for O(1) lookup by crew ID.

**Mission context**  
`ActiveMission` is a static holder that stores the current `MissionType` so subclass `act()` methods can check for their specialisation bonus without needing it passed as a parameter.

**Persistence**  
`StorageManager.saveToFile(ctx)` serialises the roster to `crew_data.json` in internal storage. `loadFromFile(ctx)` reads it back and reconstructs the correct subclass from the saved role string. Called from `MainActivity.onPause()`.

---

## Dependencies

```gradle
implementation 'com.anychart:anychart-android:1.1.5'  // statistics chart
// Navigation Component, ViewBinding, RecyclerView — standard AndroidX
```

---

## Known Limitations

- `StatisticsManager` and `Storage` Singletons are in-memory only between saves — if the process is killed without `onPause` firing, unsaved changes are lost.
- `ActiveMission` uses a global static variable as mission context. This works for single-mission flow but would need refactoring for anything concurrent.
- No unit tests — all testing was done manually on an emulator.
