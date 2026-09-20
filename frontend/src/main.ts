import { ApiError, gameApi, leaderboardApi, type Difficulty, type GameState, type Level } from "./api";
import { authState } from "./auth";
import { CanvasRenderer } from "./game/CanvasRenderer";
import "./styles/main.css";

const app = document.querySelector<HTMLDivElement>("#app");
let canvasResizeObserver: ResizeObserver | undefined;

if (!app) {
  throw new Error("Application root was not found.");
}

window.addEventListener("hashchange", renderCurrentPage);
void authState.restore().then(renderCurrentPage);

function renderCurrentPage(): void {
  canvasResizeObserver?.disconnect();
  canvasResizeObserver = undefined;
  if (window.location.hash === "#menu") {
    void renderMenuPage();
    return;
  }
  if (window.location.hash === "#game-setup") {
    void renderGameSetupPage();
    return;
  }
  if (window.location.hash === "#gameplay") {
    void renderGameplayCanvasPage();
    return;
  }
  if (window.location.hash === "#register") {
    renderRegisterPage();
    return;
  }
  renderLoginPage();
}

function renderLoginPage(): void {
  app!.innerHTML = `
    <main class="auth-page">
      ${brandPanel("THE ESCAPE BEGINS", "Enter the maze. Beat the clock. Own the leaderboard.")}
      <section class="form-panel" aria-labelledby="login-heading">
        <form class="auth-card login-card" id="login-form" novalidate>
          <h2 id="login-heading">Welcome back</h2>
          <p class="form-intro">Sign in to continue your escape.</p>
          <div class="form-fields login-fields">
            <label for="identifier">Email or username</label>
            <input id="identifier" name="identifier" type="text" autocomplete="username" placeholder="you@example.com" required>
            <label for="password">Password</label>
            <input id="password" name="password" type="password" autocomplete="current-password" placeholder="••••••••" required>
          </div>
          <p class="recovery-row"><a href="#forgot-password">Forgot password?</a></p>
          <p class="form-message" id="form-message" role="status" aria-live="polite"></p>
          <button class="primary-button" type="submit">Sign in</button>
          <p class="sign-in-row">New here? <a href="#register">Create account</a></p>
        </form>
      </section>
    </main>
  `;

  const form = app!.querySelector<HTMLFormElement>("#login-form");
  const message = app!.querySelector<HTMLParagraphElement>("#form-message");
  const submitButton = form?.querySelector<HTMLButtonElement>('button[type="submit"]');
  if (!form || !message || !submitButton) throw new Error("Login form could not be initialized.");

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const data = new FormData(form);
    const identifier = String(data.get("identifier") ?? "").trim();
    const password = String(data.get("password") ?? "");
    if (!identifier || !password) {
      setMessage(message, "Enter your email or username and password.", true);
      return;
    }

    submitButton.disabled = true;
    submitButton.textContent = "Signing in…";
    setMessage(message, "");
    try {
      const snapshot = await authState.login({ identifier, password });
      if (snapshot.status !== "authenticated") {
        setMessage(message, snapshot.error ?? "We could not sign you in. Please try again.", true);
        return;
      }
      window.location.hash = "#menu";
    } catch (error) {
      setMessage(message, error instanceof ApiError ? error.message : "We could not sign you in. Please try again.", true);
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = "Sign in";
    }
  });
}

async function renderGameSetupPage(): Promise<void> {
  if (!authState.isAuthenticated()) {
    window.location.hash = "#login";
    return;
  }

  app!.innerHTML = `
    <main class="setup-page">
      <aside class="setup-brand-panel">
        <div class="setup-brand-copy">
          <p class="eyebrow">NEW GAME</p>
          <h1>BUILD YOUR<br>ESCAPE.</h1>
          <span class="title-accent" aria-hidden="true"></span>
          <p class="setup-tagline">Choose a challenge. The maze generator is fixed for fair rankings.</p>
          <section class="selected-run-card" aria-label="Selected run">
            <span>SELECTED RUN</span>
            <strong id="selected-level">Loading level…</strong>
            <p id="selected-generator"></p>
          </section>
        </div>
      </aside>
      <section class="setup-content" aria-labelledby="setup-heading">
        <header class="setup-header">
          <h2 id="setup-heading">Choose a difficulty</h2>
          <p>Each difficulty locks its generator so its leaderboard remains fair.</p>
        </header>
        <div class="difficulty-grid" id="difficulty-grid" aria-live="polite"></div>
        <p class="ranked-notice">Ranked runs use the generator shown on the selected difficulty.</p>
        <p class="setup-message" id="setup-message" role="status" aria-live="polite"></p>
        <button class="primary-button setup-start-button" type="button" id="start-game-button" disabled>Loading levels…</button>
      </section>
    </main>
  `;

  const grid = app!.querySelector<HTMLDivElement>("#difficulty-grid");
  const levelTitle = app!.querySelector<HTMLElement>("#selected-level");
  const generatorText = app!.querySelector<HTMLElement>("#selected-generator");
  const message = app!.querySelector<HTMLParagraphElement>("#setup-message");
  const startButton = app!.querySelector<HTMLButtonElement>("#start-game-button");
  if (!grid || !levelTitle || !generatorText || !message || !startButton) throw new Error("Game setup screen could not be initialized.");

  try {
    const levels = await gameApi.getLevels();
    let selectedLevel = levels.find((level) => level.difficulty === "NORMAL") ?? levels[0];
    if (!selectedLevel) throw new Error("No game levels are available.");

    const updateSelection = (level: Level): void => {
      selectedLevel = level;
      grid.innerHTML = levels.map((item) => difficultyOption(item, item.difficulty === level.difficulty)).join("");
      levelTitle.textContent = `${level.difficulty}  •  ${level.logicalMazeWidth} × ${level.logicalMazeHeight} maze`;
      generatorText.textContent = `${formatGenerator(level.generator)} generator  •  fixed for ${formatDifficulty(level.difficulty)}`;
      startButton.textContent = `Start game  •  ${level.difficulty} / ${level.generator}`;
      grid.querySelectorAll<HTMLButtonElement>("[data-difficulty]").forEach((button) => {
        button.addEventListener("click", () => {
          const next = levels.find((item) => item.difficulty === button.dataset.difficulty);
          if (next) updateSelection(next);
        });
      });
    };

    updateSelection(selectedLevel);
    startButton.disabled = false;
    startButton.addEventListener("click", async () => {
      startButton.disabled = true;
      startButton.textContent = "Creating your maze…";
      message.textContent = "";
      try {
        const game = await gameApi.createGame(selectedLevel.difficulty);
        window.sessionStorage.setItem("escape-the-maze.current-game-id", game.gameId);
        window.location.hash = "#gameplay";
      } catch (error) {
        message.textContent = error instanceof ApiError ? error.message : "We could not create this game.";
        message.classList.add("is-error");
      } finally {
        startButton.disabled = false;
        startButton.textContent = `Start game  •  ${selectedLevel.difficulty} / ${selectedLevel.generator}`;
      }
    });
  } catch (error) {
    grid.innerHTML = "<p class=\"setup-load-error\">Levels could not be loaded. Start the backend and sign in again.</p>";
    message.textContent = error instanceof Error ? error.message : "Levels could not be loaded.";
    message.classList.add("is-error");
    startButton.textContent = "Levels unavailable";
  }
}

async function renderGameplayCanvasPage(): Promise<void> {
  const gameId = window.sessionStorage.getItem("escape-the-maze.current-game-id");
  if (!authState.isAuthenticated() || !gameId) {
    window.location.hash = "#game-setup";
    return;
  }

  app!.innerHTML = `
    <main class="gameplay-page">
      <header class="gameplay-header">
        <div><p class="eyebrow">ESCAPE THE MAZE</p><h1>YOUR RUN</h1></div>
        <span class="game-id-label">11 × 11 viewport</span>
      </header>
      <section class="canvas-stage" aria-label="Maze game area">
        <canvas id="game-canvas" width="720" height="720" aria-label="Maze game canvas"></canvas>
      </section>
    </main>
  `;

  const canvas = app!.querySelector<HTMLCanvasElement>("#game-canvas");
  if (!canvas) throw new Error("Game canvas could not be initialized.");
  const renderer = new CanvasRenderer(canvas);
  let gameState: GameState | undefined;
  const draw = (): void => {
    resizeCanvasForDisplay(canvas);
    if (gameState) {
      renderer.renderViewport({
        tiles: gameState.tiles,
        playerX: gameState.player.x,
        playerY: gameState.player.y,
        size: 11,
      });
      return;
    }
    renderer.renderPlaceholder();
  };
  canvasResizeObserver = new ResizeObserver(draw);
  canvasResizeObserver.observe(canvas);
  draw();

  try {
    gameState = await gameApi.getGame(gameId);
    draw();
  } catch (error) {
    const label = app!.querySelector<HTMLElement>(".game-id-label");
    if (label) label.textContent = error instanceof ApiError ? error.message : "Game could not be loaded";
  }
}

function resizeCanvasForDisplay(canvas: HTMLCanvasElement): void {
  const bounds = canvas.getBoundingClientRect();
  const devicePixelRatio = window.devicePixelRatio || 1;
  const width = Math.max(1, Math.round(bounds.width * devicePixelRatio));
  const height = Math.max(1, Math.round(bounds.height * devicePixelRatio));
  if (canvas.width !== width || canvas.height !== height) {
    canvas.width = width;
    canvas.height = height;
  }
}

async function renderMenuPage(): Promise<void> {
  const user = authState.getSnapshot().user;
  if (!authState.isAuthenticated() || !user) {
    window.location.hash = "#login";
    return;
  }

  app!.innerHTML = `
    <main class="menu-page">
      <aside class="menu-brand-panel">
        <div class="menu-brand-copy">
          <p class="eyebrow">ESCAPE THE MAZE</p>
          <h1>FIND YOUR<br>WAY OUT.</h1>
          <span class="title-accent" aria-hidden="true"></span>
          <p class="menu-tagline">Every path is a choice. Every second counts.</p>
          <section class="player-status" aria-labelledby="personal-best-title">
            <p class="player-name">${escapeHtml(user.username)}</p>
            <h2 id="personal-best-title">PERSONAL BEST SCORES</h2>
            <div class="best-score-grid" id="best-score-grid" aria-live="polite">
              ${bestScoreCell("EASY", "…")}
              ${bestScoreCell("NORMAL", "…")}
              ${bestScoreCell("HARD", "…")}
              ${bestScoreCell("EXPERT", "…")}
            </div>
          </section>
        </div>
      </aside>

      <section class="menu-content" aria-labelledby="menu-heading">
        <header class="menu-header">
          <h2 id="menu-heading">Welcome, ${escapeHtml(user.username)}</h2>
          <p>Choose your next move. Your best runs are saved to the leaderboard.</p>
        </header>
        <section class="start-game-card" aria-labelledby="play-heading">
          <h2 id="play-heading">Ready for another run?</h2>
          <p>Generate a new maze, choose the difficulty, and beat the clock.</p>
          <button class="primary-button menu-play-button" type="button" id="play-button">Play now</button>
        </section>
        <div class="menu-actions">
          <a class="menu-action leaderboard-action" href="#leaderboard">
            <strong>Leaderboard</strong>
            <span>See your position and chase the highest score.</span>
          </a>
          <button class="menu-action logout-action" type="button" id="logout-button">
            <strong>Log out</strong>
            <span>Leave this session safely. Your scores are saved.</span>
          </button>
        </div>
      </section>
    </main>
  `;

  app!.querySelector<HTMLButtonElement>("#play-button")?.addEventListener("click", () => {
    window.location.hash = "#game-setup";
  });
  app!.querySelector<HTMLButtonElement>("#logout-button")?.addEventListener("click", async () => {
    await authState.logout();
    window.location.hash = "#login";
  });

  const scoreGrid = app!.querySelector<HTMLDivElement>("#best-score-grid");
  if (!scoreGrid) return;
  try {
    const scores = await loadPersonalBestScores(user.username);
    scoreGrid.innerHTML = (Object.entries(scores) as [Difficulty, number][])
      .map(([difficulty, score]) => bestScoreCell(difficulty, String(score)))
      .join("");
  } catch {
    scoreGrid.innerHTML = ["EASY", "NORMAL", "HARD", "EXPERT"]
      .map((difficulty) => bestScoreCell(difficulty, "—"))
      .join("");
  }
}

async function loadPersonalBestScores(username: string): Promise<Record<Difficulty, number>> {
  const difficulties: Difficulty[] = ["EASY", "NORMAL", "HARD", "EXPERT"];
  const leaderboards = await Promise.all(difficulties.map((difficulty) => leaderboardApi.getLeaderboard(difficulty)));
  return Object.fromEntries(leaderboards.map(({ difficulty, entries }) => [
    difficulty,
    Math.max(0, ...entries.filter((entry) => entry.username === username).map((entry) => entry.score)),
  ])) as Record<Difficulty, number>;
}

function bestScoreCell(difficulty: string, score: string): string {
  return `<div class="best-score-cell"><span>${difficulty}</span><strong>${score}</strong></div>`;
}

function difficultyOption(level: Level, selected: boolean): string {
  return `
    <button class="difficulty-option${selected ? " is-selected" : ""}" type="button" data-difficulty="${level.difficulty}" aria-pressed="${selected}">
      <strong>${level.difficulty}</strong>
      <span>${level.logicalMazeWidth} × ${level.logicalMazeHeight} maze  •  ${formatGenerator(level.generator)} generator</span>
    </button>
  `;
}

function formatDifficulty(difficulty: Difficulty): string {
  return difficulty.charAt(0) + difficulty.slice(1).toLowerCase();
}

function formatGenerator(generator: Level["generator"]): string {
  return generator === "DFS" ? "DFS" : generator.charAt(0) + generator.slice(1).toLowerCase();
}

function renderRegisterPage(): void {
  app!.innerHTML = `
    <main class="auth-page">
      ${brandPanel("YOUR NEXT RUN AWAITS", "Create your account, start your run, and claim your place on the leaderboard.")}
      <section class="form-panel" aria-labelledby="register-heading">
        <form class="auth-card register-card" id="register-form" novalidate>
          <h2 id="register-heading">Start your escape</h2>
          <p class="form-intro">Create an account to save your score.</p>
          <div class="form-fields">
            <label for="username">Display name</label>
            <input id="username" name="username" type="text" autocomplete="username" placeholder="Maze Runner" minlength="3" maxlength="50" required>
            <label for="email">Email</label>
            <input id="email" name="email" type="email" autocomplete="email" placeholder="you@example.com" maxlength="255" required>
            <label for="password">Password</label>
            <input id="password" name="password" type="password" autocomplete="new-password" placeholder="••••••••" minlength="8" maxlength="72" required>
            <label for="confirm-password">Confirm password</label>
            <input id="confirm-password" name="confirmPassword" type="password" autocomplete="new-password" placeholder="••••••••" minlength="8" maxlength="72" required>
          </div>
          <p class="form-message" id="form-message" role="status" aria-live="polite"></p>
          <button class="primary-button" type="submit">Create account</button>
          <p class="sign-in-row">Already have an account? <a href="#login">Sign in</a></p>
        </form>
      </section>
    </main>
  `;

  const form = app!.querySelector<HTMLFormElement>("#register-form");
  const message = app!.querySelector<HTMLParagraphElement>("#form-message");
  const submitButton = form?.querySelector<HTMLButtonElement>('button[type="submit"]');
  if (!form || !message || !submitButton) throw new Error("Register form could not be initialized.");

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const data = new FormData(form);
    const username = String(data.get("username") ?? "").trim();
    const email = String(data.get("email") ?? "").trim();
    const password = String(data.get("password") ?? "");
    const confirmPassword = String(data.get("confirmPassword") ?? "");
    const validationError = validateRegistration(username, email, password, confirmPassword);
    if (validationError) {
      setMessage(message, validationError, true);
      return;
    }

    submitButton.disabled = true;
    submitButton.textContent = "Creating account…";
    setMessage(message, "");
    try {
      await authState.register({ username, email, password });
      form.reset();
      setMessage(message, "Account created. Sign in to start your first run.");
    } catch (error) {
      setMessage(message, error instanceof ApiError ? error.message : "We could not create your account. Please try again.", true);
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = "Create account";
    }
  });
}

function brandPanel(eyebrow: string, tagline: string): string {
  return `
    <section class="brand-panel" aria-labelledby="game-title">
      <div class="brand-copy">
        <p class="eyebrow">${eyebrow}</p>
        <h1 id="game-title">ESCAPE<br>THE MAZE</h1>
        <span class="title-accent" aria-hidden="true"></span>
        <p class="brand-tagline">${tagline}</p>
      </div>
    </section>
  `;
}

function validateRegistration(username: string, email: string, password: string, confirmPassword: string): string | undefined {
  if (username.length < 3) return "Display name must contain at least 3 characters.";
  if (!email || !email.includes("@")) return "Enter a valid email address.";
  if (password.length < 8) return "Password must contain at least 8 characters.";
  if (password !== confirmPassword) return "Passwords do not match.";
  return undefined;
}

function setMessage(element: HTMLParagraphElement, text: string, isError = false): void {
  element.textContent = text;
  element.classList.toggle("is-error", isError);
  element.classList.toggle("is-visible", Boolean(text));
}

function escapeHtml(value: string): string {
  return value.replace(/[&<>'"]/g, (character) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    "'": "&#039;",
    "\"": "&quot;",
  }[character] ?? character));
}
