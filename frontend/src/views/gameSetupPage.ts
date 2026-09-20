import { ApiError, gameApi, type Level } from "../api";
import { authState } from "../auth";
import { formatDifficulty, formatGenerator } from "./shared";

export async function renderGameSetupPage(app: HTMLDivElement): Promise<void> {
  if (!authState.isAuthenticated()) { window.location.hash = "#login"; return; }
  app.innerHTML = `
    <main class="setup-page"><aside class="setup-brand-panel"><div class="setup-brand-copy">
      <p class="eyebrow">NEW GAME</p><h1>BUILD YOUR<br>ESCAPE.</h1><span class="title-accent" aria-hidden="true"></span>
      <p class="setup-tagline">Choose a challenge. The maze generator is fixed for fair rankings.</p>
      <section class="selected-run-card" aria-label="Selected run"><span>SELECTED RUN</span><strong id="selected-level">Loading level…</strong><p id="selected-generator"></p></section>
    </div></aside><section class="setup-content" aria-labelledby="setup-heading">
      <header class="setup-header"><h2 id="setup-heading">Choose a difficulty</h2><p>Each difficulty locks its generator so its leaderboard remains fair.</p></header>
      <div class="difficulty-grid" id="difficulty-grid" aria-live="polite"></div><p class="ranked-notice">Ranked runs use the generator shown on the selected difficulty.</p>
      <p class="setup-message" id="setup-message" role="status" aria-live="polite"></p><button class="primary-button setup-start-button" type="button" id="start-game-button" disabled>Loading levels…</button>
    </section></main>`;
  const grid = app.querySelector<HTMLDivElement>("#difficulty-grid");
  const title = app.querySelector<HTMLElement>("#selected-level");
  const generator = app.querySelector<HTMLElement>("#selected-generator");
  const message = app.querySelector<HTMLParagraphElement>("#setup-message");
  const start = app.querySelector<HTMLButtonElement>("#start-game-button");
  if (!grid || !title || !generator || !message || !start) throw new Error("Game setup screen could not be initialized.");
  try {
    const levels = await gameApi.getLevels();
    let selected = levels.find((level) => level.difficulty === "NORMAL") ?? levels[0];
    if (!selected) throw new Error("No game levels are available.");
    const updateSelection = (level: Level): void => {
      selected = level;
      grid.innerHTML = levels.map((item) => difficultyOption(item, item.difficulty === level.difficulty)).join("");
      title.textContent = `${level.difficulty}  •  ${level.logicalMazeWidth} × ${level.logicalMazeHeight} maze`;
      generator.textContent = `${formatGenerator(level.generator)} generator  •  fixed for ${formatDifficulty(level.difficulty)}`;
      start.textContent = `Start game  •  ${level.difficulty} / ${level.generator}`;
      grid.querySelectorAll<HTMLButtonElement>("[data-difficulty]").forEach((button) => button.addEventListener("click", () => {
        const next = levels.find((item) => item.difficulty === button.dataset.difficulty);
        if (next) updateSelection(next);
      }));
    };
    updateSelection(selected); start.disabled = false;
    start.addEventListener("click", async () => {
      start.disabled = true; start.textContent = "Creating your maze…"; message.textContent = "";
      try { const game = await gameApi.createGame(selected.difficulty); window.sessionStorage.setItem("escape-the-maze.current-game-id", game.gameId); window.location.hash = "#gameplay"; }
      catch (error) { message.textContent = error instanceof ApiError ? error.message : "We could not create this game."; message.classList.add("is-error"); }
      finally { start.disabled = false; start.textContent = `Start game  •  ${selected.difficulty} / ${selected.generator}`; }
    });
  } catch (error) {
    grid.innerHTML = "<p class=\"setup-load-error\">Levels could not be loaded. Start the backend and sign in again.</p>";
    message.textContent = error instanceof Error ? error.message : "Levels could not be loaded.";
    message.classList.add("is-error"); start.textContent = "Levels unavailable";
  }
}

function difficultyOption(level: Level, selected: boolean): string {
  return `<button class="difficulty-option${selected ? " is-selected" : ""}" type="button" data-difficulty="${level.difficulty}" aria-pressed="${selected}"><strong>${level.difficulty}</strong><span>${level.logicalMazeWidth} × ${level.logicalMazeHeight} maze  •  ${formatGenerator(level.generator)} generator</span></button>`;
}
