# JavaFX-to-Web Migration Plan

## Migration Principle

The Spring Boot backend becomes authoritative for maze generation, game state,
movement validation, effects, scoring, timer expiry, and final results. The
TypeScript frontend owns browser input, Canvas rendering, camera, animation,
and page navigation.

## Source-to-Target Mapping

| Current component | Current responsibility | Target location | Action |
|---|---|---|---|
| `algorithms/*` | DFS, Prim, Kruskal, Binary Tree generation | `backend/domain/maze` | Reuse with package cleanup and injected randomness. |
| `entities/MazeObjects/MazeGrid` | Integer maze grid | `backend/domain/maze` | Reuse unchanged in behavior. |
| `entities/MazeObjects/Maze` | Generate maze, add exit and obstacles | `backend/domain/maze` | Refactor to use `TileType[][]` and backend game rules. |
| `entities/MazeObjects/Level` and `Levels` | Difficulty presets | `backend/domain/game` | Reuse/refactor as API-visible level definitions. |
| `entities/MazeObjects/Player` | Pixel position, health, speed, animation state | `backend/domain/game/PlayerState` | Refactor to logical grid position and health only. |
| `entities/Tiles/*` | Walkability and tile effects | `backend/domain/game/TileType` + `GameService` | Replace classes with enum and centralized rules. |
| `game/GameState` | Score, effects, victory state | `backend/domain/game/GameSession` | Refactor into authenticated, owned session state. |
| `game/GameTimer` | Countdown | `backend/domain/game` | Reuse timer rules; use server `endsAt` as authority. |
| `management/GameConfig` | Gameplay and JavaFX layout constants | backend `GameRules` and frontend constants | Split gameplay rules from visual settings. |
| `management/ScoreManager` | Local score text file | PostgreSQL + repository | Replace. |
| `management/MazePreview` | Console maze preview | backend test/dev utility | Optional reuse. |
| `management/Main`, `GameManager` | JavaFX application startup | Spring Boot application | Replace. |
| `gui/mainScreens/*`, `UIFactory` | Menu, screens, controls | TypeScript pages/CSS from Figma | Replace. |
| `gui/game/GameScreen`, `GameRenderer`, `TileRenderer`, `PlayerRenderer`, `Camera`, `HealthBar` | JavaFX Canvas rendering and HUD | TypeScript Canvas renderer/HUD | Replace. |
| `gui/game/InputController` | Pressed JavaFX keys | TypeScript keyboard handler | Replace. |
| `gui/game/GameLoop` | JavaFX animation frame loop | `requestAnimationFrame` | Replace. |
| `gui/game/PlayerController` | Movement, collision, jump animation | `GameService` + frontend animation | Split and refactor. |
| `gui/game/EffectProcessor` | Periodic tile effect processing | `GameService` | Replace JavaFX timeline with server rules. |
| `game/effects/FogEffect` | JavaFX fog animation | TypeScript Canvas overlay | Replace. |
| `src/main/resources/entities/*` | Player and tile image assets | `frontend/public/assets` | Move/reuse as browser assets when needed. |

## Target Responsibilities

```text
Backend:  generate maze → create GameSession → validate MOVE → apply effects
          → calculate score/status → save completed result

Frontend: Figma pages → REST/WebSocket client → keyboard input → Canvas draw
          → camera/HUD/animations/result modal

PostgreSQL: users → completed game_results → leaderboard
```

## Extraction Order

1. Copy maze generators and `MazeGrid` into a Java-only backend domain package.
2. Replace Java tile classes with serializable `TileType` values.
3. Replace pixel-based `Player` and `GameState` with `PlayerState` and
   `GameSession`.
4. Move collision, movement, effects, timer, score, and completion decisions
   from JavaFX controllers into `GameService`.
5. Expose the state through REST for creation/recovery and WebSocket for moves.
6. Build TypeScript pages and Canvas rendering from Figma designs.
7. Replace local `ScoreManager` persistence with PostgreSQL results and the
   leaderboard API.
8. Remove JavaFX dependencies and JavaFX-only source after web parity is
   verified.

## Rules That Must Not Move to the Frontend

- Whether a movement is valid.
- Player position, health, score, and status.
- Tile effects, gold collection, and timer expiry.
- Game ownership and WebSocket authorization.
- Final score persistence and leaderboard ordering.
