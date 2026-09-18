import { CanvasRenderer } from "./game/CanvasRenderer";
import { KeyboardInput } from "./input/KeyboardInput";
import { plannedViews } from "./views";
import "./styles/main.css";

const app = document.querySelector<HTMLDivElement>("#app");

if (!app) {
  throw new Error("Application root was not found.");
}

app.innerHTML = `
  <main class="app-shell">
    <header class="app-header">
      <div>
        <p class="eyebrow">ESCAPE THE MAZE</p>
        <h1>Browser frontend scaffold</h1>
      </div>
      <span class="status-pill"><span class="status-dot"></span>Backend ready</span>
    </header>
    <section class="app-content">
      <aside class="panel">
        <h2>Planned screens</h2>
        <nav aria-label="Planned screens">
          <ul class="view-list">
            ${plannedViews.map((view) => `<li>${view}</li>`).join("")}
          </ul>
        </nav>
      </aside>
      <section class="game-preview panel" aria-labelledby="canvas-title">
        <div class="panel-heading">
          <div>
            <p class="eyebrow">CANVAS PREVIEW</p>
            <h2 id="canvas-title">Gameplay renderer boundary</h2>
          </div>
          <span class="key-hint">WASD / arrows</span>
        </div>
        <canvas id="game-canvas" width="720" height="520" aria-label="Maze preview"></canvas>
      </section>
    </section>
  </main>
`;

const canvas = document.querySelector<HTMLCanvasElement>("#game-canvas");

if (!canvas) {
  throw new Error("Game canvas was not found.");
}

const renderer = new CanvasRenderer(canvas);
renderer.renderPlaceholder();

const input = new KeyboardInput((direction) => {
  renderer.renderPlaceholder(direction);
});

input.start();
