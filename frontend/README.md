# Escape the Maze Frontend

Vite + vanilla TypeScript frontend for the browser version of Escape the Maze.
It deliberately uses no UI framework: Canvas rendering and the small game UI do
not need one for the MVP.

## Run locally

```powershell
npm install
npm run dev
```

Vite runs at `http://localhost:5173` and proxies `/api` and `/ws` to the Spring
Boot backend at `http://localhost:8080`.

## Current scaffold

- `src/game/CanvasRenderer.ts` owns Canvas drawing.
- `src/input/KeyboardInput.ts` converts WASD/arrow keys into movement events.
- `src/api/ApiClient.ts` is the REST boundary.
- `src/websocket/GameSocket.ts` is the real-time boundary.
- `src/views.ts` names the planned Figma-backed screens.

Authentication, real game state, and WebSocket messages will be implemented in
later migration tasks.
