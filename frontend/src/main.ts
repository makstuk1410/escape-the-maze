import { ApiError, apiClient, gameApi, leaderboardApi, type Difficulty, type GameState, type LeaderboardEntry, type Level, type StateMessage } from "./api";
import { authState } from "./auth";
import { CanvasRenderer } from "./game/CanvasRenderer";
import { KeyboardInput } from "./input/KeyboardInput";
import { GameSocket, type GameConnectionStatus } from "./websocket/GameSocket";
import "./styles/main.css";

const app = document.querySelector<HTMLDivElement>("#app");
let canvasResizeObserver: ResizeObserver | undefined;
let timerInterval: number | undefined;
let fogRefreshInterval: number | undefined;
let keyboardInput: KeyboardInput | undefined;
let gameSocket: GameSocket | undefined;
let playerAnimationFrame: number | undefined;
let gameplayEscapeHandler: ((event: KeyboardEvent) => void) | undefined;

if (!app) {
  throw new Error("Application root was not found.");
}

window.addEventListener("hashchange", renderCurrentPage);
void authState.restore().then(renderCurrentPage);

function renderCurrentPage(): void {
  canvasResizeObserver?.disconnect();
  canvasResizeObserver = undefined;
  if (timerInterval !== undefined) {
    window.clearInterval(timerInterval);
    timerInterval = undefined;
  }
  if (fogRefreshInterval !== undefined) {
    window.clearInterval(fogRefreshInterval);
    fogRefreshInterval = undefined;
  }
  keyboardInput?.stop();
  keyboardInput = undefined;
  if (gameplayEscapeHandler) {
    window.removeEventListener("keydown", gameplayEscapeHandler);
    gameplayEscapeHandler = undefined;
  }
  gameSocket?.close();
  gameSocket = undefined;
  if (playerAnimationFrame !== undefined) {
    window.cancelAnimationFrame(playerAnimationFrame);
    playerAnimationFrame = undefined;
  }
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
  if (window.location.hash === "#leaderboard") {
    void renderLeaderboardPage();
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
        <div class="game-identity"><p>ESCAPE THE MAZE</p><span id="game-meta">LOADING RUN…</span></div>
        <div class="gameplay-live-details">
          <output class="timer-card" id="timer-value" aria-label="Time remaining">05:00</output>
          <span class="connection-status is-connecting" id="connection-status" role="status" aria-live="polite">
            <span class="connection-dot" aria-hidden="true"></span>
            <span class="connection-status-label" id="connection-status-label">CONNECTING</span>
          </span>
        </div>
        <div class="gameplay-actions">
          <button class="pause-button" id="pause-button" type="button">Pause</button>
          <span class="sr-only" id="movement-status" aria-live="polite">WASD / arrows • Space + direction jumps</span>
        </div>
      </header>
      <p class="connection-lost-banner" id="connection-lost-banner" role="alert"></p>
      <section class="gameplay-layout" aria-label="Maze game area">
        <aside class="run-stats" aria-label="Run statistics">
          <section class="score-card" aria-labelledby="score-label">
            <p id="score-label">SCORE</p>
            <output id="score-value">0000</output>
          </section>
          <section class="health-card" aria-labelledby="health-label">
            <p id="health-label">HEALTH</p>
            <output class="health-hearts" id="health-hearts" aria-label="100 of 100 health"><span class="heart-filled">♥</span><span class="heart-filled">♥</span><span class="heart-filled">♥</span><span class="heart-filled">♥</span><span class="heart-filled">♥</span></output>
            <output class="health-value" id="health-value">100 / 100</output>
          </section>
          <section class="controls-card" aria-labelledby="controls-label">
            <p id="controls-label">CONTROLS</p>
            <dl>
              <div><dt>Move</dt><dd>WASD / arrows</dd></div>
              <div><dt>Jump</dt><dd>Hold Space + direction</dd></div>
            </dl>
          </section>
        </aside>
        <section class="canvas-stage">
          <canvas id="game-canvas" width="720" height="720" aria-label="Maze game canvas"></canvas>
        </section>
        <aside class="game-context" aria-label="Game information">
          <section class="objective-card">
            <p>OBJECTIVE</p>
            <h2>Find the exit</h2>
            <span>Reach the green exit before time runs out.</span>
          </section>
          <section class="effects-card">
            <p>ACTIVE EFFECTS</p>
            <output id="active-effects">No active effects</output>
          </section>
          <section class="tile-guide-card">
            <p>TILE GUIDE</p>
            <ul>
              <li class="guide-gold">Gold <span>+10 points</span></li>
              <li class="guide-spikes">Spikes <span>lose health</span></li>
              <li class="guide-freeze">Freeze <span>movement slows</span></li>
              <li class="guide-fog">Fog <span>vision reduces</span></li>
            </ul>
          </section>
        </aside>
      </section>
      <div class="game-modal-overlay" id="pause-modal" hidden>
        <section class="game-modal pause-modal" role="dialog" aria-modal="true" aria-labelledby="pause-modal-title">
          <h2 id="pause-modal-title">RUN PAUSED</h2>
          <p class="game-modal-copy">Your current run is paused. Continue when you are ready.</p>
          <div class="game-modal-spacer" aria-hidden="true"></div>
          <button class="game-modal-primary" id="continue-game-button" type="button">Continue</button>
          <button class="game-modal-secondary" id="leave-game-button" type="button">Leave game</button>
          <p class="game-modal-hint">Press Esc to continue</p>
        </section>
      </div>
      <div class="game-modal-overlay" id="result-modal" hidden>
        <section class="game-modal result-modal" role="dialog" aria-modal="true" aria-labelledby="result-modal-title">
          <p class="game-modal-kicker" id="result-modal-kicker">RUN COMPLETE</p>
          <h2 id="result-modal-title">You escaped!</h2>
          <p class="game-modal-copy" id="result-modal-copy"></p>
          <section class="final-score-card" aria-label="Final score">
            <p>FINAL SCORE</p>
            <output id="result-score">0000</output>
          </section>
          <p class="result-modal-note" id="result-modal-note"></p>
          <button class="game-modal-primary" id="result-primary-button" type="button">Play again</button>
          <button class="game-modal-secondary" id="result-menu-button" type="button">Back to menu</button>
        </section>
      </div>
    </main>
  `;

  const canvas = app!.querySelector<HTMLCanvasElement>("#game-canvas");
  const scoreValue = app!.querySelector<HTMLOutputElement>("#score-value");
  const healthHearts = app!.querySelector<HTMLOutputElement>("#health-hearts");
  const healthValue = app!.querySelector<HTMLOutputElement>("#health-value");
  const timerValue = app!.querySelector<HTMLOutputElement>("#timer-value");
  const gameMeta = app!.querySelector<HTMLElement>("#game-meta");
  const activeEffects = app!.querySelector<HTMLOutputElement>("#active-effects");
  const movementStatus = app!.querySelector<HTMLElement>("#movement-status");
  const connectionStatus = app!.querySelector<HTMLElement>("#connection-status");
  const connectionStatusLabel = app!.querySelector<HTMLElement>("#connection-status-label");
  const connectionLostBanner = app!.querySelector<HTMLElement>("#connection-lost-banner");
  const pauseButton = app!.querySelector<HTMLButtonElement>("#pause-button");
  const pauseModal = app!.querySelector<HTMLElement>("#pause-modal");
  const continueGameButton = app!.querySelector<HTMLButtonElement>("#continue-game-button");
  const leaveGameButton = app!.querySelector<HTMLButtonElement>("#leave-game-button");
  const resultModal = app!.querySelector<HTMLElement>("#result-modal");
  const resultKicker = app!.querySelector<HTMLElement>("#result-modal-kicker");
  const resultTitle = app!.querySelector<HTMLElement>("#result-modal-title");
  const resultCopy = app!.querySelector<HTMLElement>("#result-modal-copy");
  const resultScore = app!.querySelector<HTMLOutputElement>("#result-score");
  const resultNote = app!.querySelector<HTMLElement>("#result-modal-note");
  const resultPrimaryButton = app!.querySelector<HTMLButtonElement>("#result-primary-button");
  const resultMenuButton = app!.querySelector<HTMLButtonElement>("#result-menu-button");
  if (!canvas || !scoreValue || !healthHearts || !healthValue || !timerValue || !gameMeta || !activeEffects || !movementStatus || !connectionStatus || !connectionStatusLabel || !connectionLostBanner || !pauseButton || !pauseModal || !continueGameButton || !leaveGameButton || !resultModal || !resultKicker || !resultTitle || !resultCopy || !resultScore || !resultNote || !resultPrimaryButton || !resultMenuButton) throw new Error("Game canvas could not be initialized.");
  const renderer = new CanvasRenderer(canvas);
  let gameState: GameState | undefined;
  let displayedPlayer: { x: number; y: number } | undefined;
  let displayedPlayerLift = 0;
  let movementInFlight = false;
  let pendingJump = false;
  let resultShown = false;
  const hidePauseModal = (): void => { pauseModal.hidden = true; };
  const showPauseModal = (): void => { pauseModal.hidden = false; };
  const leaveGame = (): void => {
    window.sessionStorage.removeItem("escape-the-maze.current-game-id");
    window.location.hash = "#menu";
  };
  const showResultModal = (state: GameState): void => {
    if (resultShown || state.status === "RUNNING" || state.status === "PAUSED") return;
    resultShown = true;
    const content = state.status === "WON"
      ? {
          kicker: "RUN COMPLETE", title: "You escaped!",
          copy: `Your ${formatDifficulty(state.difficulty)} run is complete. The server verified your score.`,
          note: `New personal best for ${formatDifficulty(state.difficulty)}!`, primary: "Play again",
        }
      : state.status === "LOST"
        ? {
            kicker: "RUN ENDED", title: "The maze got you.",
            copy: "You ran out of health. Win a run to save a score to the leaderboard.",
            note: `Try again to improve your ${formatDifficulty(state.difficulty)} best.`, primary: "Try again",
          }
        : {
            kicker: "TIME EXPIRED", title: "Time ran out.",
            copy: "The exit stayed ahead of you. Win a run to save a score to the leaderboard.",
            note: `Try a faster route on your next ${formatDifficulty(state.difficulty)} run.`, primary: "Play again",
          };
    resultKicker.textContent = content.kicker;
    resultTitle.textContent = content.title;
    resultCopy.textContent = content.copy;
    resultScore.textContent = formatScore(state.score);
    resultNote.textContent = content.note;
    resultPrimaryButton.textContent = content.primary;
    pauseButton.disabled = true;
    keyboardInput?.stop();
    stopTimer();
    resultModal.hidden = false;
  };
  const draw = (): void => {
    resizeCanvasForDisplay(canvas);
    if (gameState) {
      renderer.renderViewport({
        tiles: gameState.tiles,
        playerX: displayedPlayer?.x ?? gameState.player.x,
        playerY: displayedPlayer?.y ?? gameState.player.y,
        playerLift: displayedPlayerLift,
        fogIntensity: fogIntensity(gameState.effects.fogUntil),
        size: 11,
      });
      return;
    }
    renderer.renderPlaceholder();
  };
  const renderGameState = (): void => {
    if (!gameState) return;
    scoreValue.textContent = formatScore(gameState.score);
    renderHealth(healthHearts, healthValue, gameState.player.health);
    gameMeta.textContent = `${gameState.difficulty}  •  ${gameState.generator} MAZE`;
    activeEffects.textContent = activeEffectsLabel(gameState.effects);
    pauseButton.disabled = gameState.status !== "RUNNING";
    draw();
  };
  const animatePlayerTo = (target: { x: number; y: number }, isJump: boolean): void => {
    const start = displayedPlayer ?? target;
    if (start.x === target.x && start.y === target.y) {
      displayedPlayer = target;
      displayedPlayerLift = 0;
      draw();
      return;
    }

    if (playerAnimationFrame !== undefined) {
      window.cancelAnimationFrame(playerAnimationFrame);
    }

    const startedAt = performance.now();
    const frozen = isFrozen(gameState?.effects.frozenUntil ?? null);
    const movementDurationMs = frozen ? 550 : 150;
    const durationMs = isJump ? movementDurationMs + 110 : movementDurationMs;
    const animate = (now: number): void => {
      const progress = Math.min(1, (now - startedAt) / durationMs);
      const easedProgress = 1 - (1 - progress) ** 3;
      displayedPlayer = {
        x: start.x + (target.x - start.x) * easedProgress,
        y: start.y + (target.y - start.y) * easedProgress,
      };
      displayedPlayerLift = isJump ? Math.sin(progress * Math.PI) * 0.35 : 0;
      draw();

      if (progress < 1) {
        playerAnimationFrame = window.requestAnimationFrame(animate);
        return;
      }

      displayedPlayer = target;
      displayedPlayerLift = 0;
      playerAnimationFrame = undefined;
      draw();
    };
    playerAnimationFrame = window.requestAnimationFrame(animate);
  };
  canvasResizeObserver = new ResizeObserver(draw);
  canvasResizeObserver.observe(canvas);
  fogRefreshInterval = window.setInterval(() => {
    if (gameState) {
      activeEffects.textContent = activeEffectsLabel(gameState.effects);
      if (fogIntensity(gameState.effects.fogUntil) > 0) {
        draw();
      }
    }
  }, 50);
  draw();
  movementStatus.textContent = "Connecting…";

  resultPrimaryButton.addEventListener("click", () => {
    window.sessionStorage.removeItem("escape-the-maze.current-game-id");
    window.location.hash = "#game-setup";
  });
  resultMenuButton.addEventListener("click", leaveGame);
  leaveGameButton.addEventListener("click", leaveGame);
  const resumeGame = async (): Promise<void> => {
    if (!gameState || gameState.status !== "PAUSED") return;
    continueGameButton.disabled = true;
    try {
      gameState = await gameApi.resumeGame(gameId);
      hidePauseModal();
      renderGameState();
      startTimer(timerValue, gameState.endsAt);
      keyboardInput?.start();
      movementStatus.textContent = "LIVE • Space + direction jumps";
    } catch (error) {
      movementStatus.textContent = error instanceof ApiError ? error.message : "Game could not resume.";
    } finally {
      continueGameButton.disabled = false;
    }
  };
  continueGameButton.addEventListener("click", () => { void resumeGame(); });
  gameplayEscapeHandler = (event: KeyboardEvent): void => {
    if (event.key === "Escape" && !pauseModal.hidden) {
      event.preventDefault();
      void resumeGame();
    }
  };
  window.addEventListener("keydown", gameplayEscapeHandler);
  pauseButton.addEventListener("click", async () => {
    if (!gameState || gameState.status !== "RUNNING") return;
    pauseButton.disabled = true;
    try {
      gameState = await gameApi.pauseGame(gameId);
      renderGameState();
      stopTimer();
      keyboardInput?.stop();
      movementStatus.textContent = "Run paused";
      if (gameState.status === "PAUSED") showPauseModal();
      else showResultModal(gameState);
    } catch (error) {
      pauseButton.disabled = false;
      movementStatus.textContent = error instanceof ApiError ? error.message : "Game could not pause.";
    }
  });

  try {
    gameState = await gameApi.getGame(gameId);
    displayedPlayer = { ...gameState.player };
    renderGameState();
    if (gameState.status === "RUNNING") startTimer(timerValue, gameState.endsAt);
    if (gameState.status === "PAUSED") showPauseModal();
    showResultModal(gameState);
    const accessToken = apiClient.getAccessToken();
    if (!accessToken) throw new Error("Sign in again to connect to this game.");
    const socket = new GameSocket();
    gameSocket = socket;
    socket.connect(gameId, accessToken, {
      onState: (state) => {
        const wasJump = pendingJump;
        movementInFlight = false;
        pendingJump = false;
        if (!gameState || state.stateVersion <= gameState.stateVersion) return;
        gameState = applyStateUpdate(gameState, state);
        renderGameState();
        animatePlayerTo(gameState.player, wasJump);
        if (gameState.status === "PAUSED") {
          stopTimer();
          keyboardInput?.stop();
          showPauseModal();
        }
        showResultModal(gameState);
      },
      onConnectionStatus: (status) => {
        renderConnectionStatus(status, connectionStatus, connectionStatusLabel, connectionLostBanner);
        if (status === "LIVE") {
          movementStatus.textContent = "LIVE • Space + direction jumps";
        } else {
          movementInFlight = false;
          movementStatus.textContent = "Movement paused";
        }
      },
    });
    if (gameSocket !== socket) return;
    keyboardInput = new KeyboardInput(({ direction, jump }) => {
      if (gameState?.status !== "RUNNING") {
        movementStatus.textContent = "The run is not active.";
        return;
      }
      if (movementInFlight || playerAnimationFrame !== undefined) {
        movementStatus.textContent = "Finishing move…";
        return;
      }
      try {
        if (jump) {
          socket.sendJump(direction);
        } else {
          socket.sendMove(direction);
        }
        movementInFlight = true;
        pendingJump = jump;
        movementStatus.textContent = jump ? `Jumping: ${formatDirection(direction)}` : `Sent: ${formatDirection(direction)}`;
      } catch (error) {
        movementStatus.textContent = error instanceof Error ? error.message : "Move was not sent.";
      }
    });
    if (gameState?.status === "RUNNING") keyboardInput.start();
  } catch (error) {
    movementStatus.textContent = error instanceof ApiError ? error.message : "Game could not be loaded";
  }
}

function formatScore(score: number): string {
  return String(Math.max(0, score)).padStart(4, "0");
}

function renderConnectionStatus(
  status: GameConnectionStatus,
  statusElement: HTMLElement,
  labelElement: HTMLElement,
  bannerElement: HTMLElement,
): void {
  statusElement.className = `connection-status is-${status.toLowerCase()}`;
  labelElement.textContent = status === "LIVE"
    ? "LIVE"
    : status === "LOST"
      ? "OFFLINE"
      : status === "CONNECTING"
        ? "CONNECTING"
        : "RECONNECTING";

  const showBanner = status === "LOST" || status === "RECONNECTING";
  bannerElement.classList.toggle("is-visible", showBanner);
  bannerElement.textContent = status === "LOST"
    ? "Connection lost. Reconnecting…"
    : status === "RECONNECTING"
      ? "Reconnecting to your game…"
      : "";
}

function applyStateUpdate(current: GameState, state: StateMessage): GameState {
  const tiles = current.tiles.map((row) => [...row]);
  state.changedTiles.forEach(({ x, y, type }) => {
    if (tiles[y]?.[x] !== undefined) tiles[y][x] = type;
  });
  return {
    ...current,
    tiles,
    player: state.player,
    score: state.score,
    status: state.status,
    endsAt: state.endsAt,
    effects: state.effects,
    stateVersion: state.stateVersion,
  };
}

function fogIntensity(fogUntil: string | null): number {
  if (!fogUntil) return 0;

  const durationMs = 4_000;
  const elapsed = 1 - Math.max(0, new Date(fogUntil).getTime() - Date.now()) / durationMs;
  if (elapsed <= 0 || elapsed >= 1) return 0;
  if (elapsed < 0.25) return elapsed / 0.25;
  if (elapsed > 0.75) return (1 - elapsed) / 0.25;
  return 1;
}

function isFrozen(frozenUntil: string | null): boolean {
  return frozenUntil !== null && new Date(frozenUntil).getTime() > Date.now();
}

function activeEffectsLabel(effects: GameState["effects"]): string {
  const active: string[] = [];
  if (isFrozen(effects.frozenUntil)) active.push("Freeze — movement slowed");
  if (fogIntensity(effects.fogUntil) > 0) active.push("Fog — vision reduced");
  return active.length === 0 ? "No active effects" : active.join(" • ");
}

function formatDirection(direction: "UP" | "DOWN" | "LEFT" | "RIGHT"): string {
  return direction.charAt(0) + direction.slice(1).toLowerCase();
}

function startTimer(element: HTMLOutputElement, endsAt: string): void {
  stopTimer();
  const endTime = new Date(endsAt).getTime();
  const updateTimer = (): void => {
    const remainingSeconds = Math.max(0, Math.ceil((endTime - Date.now()) / 1000));
    element.textContent = `${String(Math.floor(remainingSeconds / 60)).padStart(2, "0")}:${String(remainingSeconds % 60).padStart(2, "0")}`;
    if (remainingSeconds === 0 && timerInterval !== undefined) {
      window.clearInterval(timerInterval);
      timerInterval = undefined;
    }
  };
  updateTimer();
  timerInterval = window.setInterval(updateTimer, 1_000);
}

function stopTimer(): void {
  if (timerInterval !== undefined) {
    window.clearInterval(timerInterval);
    timerInterval = undefined;
  }
}

function renderHealth(heartsElement: HTMLOutputElement, valueElement: HTMLOutputElement, health: number): void {
  const maximumHealth = 100;
  const boundedHealth = Math.max(0, Math.min(maximumHealth, health));
  const filledHearts = Math.ceil(boundedHealth / 20);
  heartsElement.innerHTML = `${"<span class=\"heart-filled\">♥</span>".repeat(filledHearts)}${"<span class=\"heart-empty\">♡</span>".repeat(5 - filledHearts)}`;
  heartsElement.setAttribute("aria-label", `${boundedHealth} of ${maximumHealth} health`);
  valueElement.textContent = `${boundedHealth} / ${maximumHealth}`;
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

async function renderLeaderboardPage(): Promise<void> {
  if (!authState.isAuthenticated()) {
    window.location.hash = "#login";
    return;
  }

  const user = authState.getSnapshot().user;
  const difficulties: Difficulty[] = ["EASY", "NORMAL", "HARD", "EXPERT"];
  let selectedDifficulty: Difficulty = "NORMAL";

  app!.innerHTML = `
    <main class="leaderboard-page">
      <header class="leaderboard-top-bar">
        <div class="leaderboard-identity">
          <p>ESCAPE THE MAZE</p>
          <span>GLOBAL RANKINGS</span>
        </div>
        <a class="leaderboard-back-button" href="#menu">Back to menu</a>
      </header>
      <section class="leaderboard-content" aria-labelledby="leaderboard-heading">
        <header class="leaderboard-heading">
          <h1 id="leaderboard-heading">Leaderboard</h1>
          <p>Scores are ranked separately for each difficulty and use server-verified victories.</p>
        </header>
        <div class="leaderboard-tabs" role="tablist" aria-label="Choose leaderboard difficulty">
          ${difficulties.map((difficulty) => `<button class="leaderboard-tab${difficulty === selectedDifficulty ? " is-selected" : ""}" type="button" role="tab" data-leaderboard-difficulty="${difficulty}" aria-selected="${difficulty === selectedDifficulty}">${difficulty}</button>`).join("")}
        </div>
        <section class="leaderboard-table-card" aria-live="polite">
          <div class="leaderboard-table-header" aria-hidden="true"><span>RANK</span><span>PLAYER</span><span>SCORE</span><span>RESULT</span></div>
          <div class="leaderboard-rows" id="leaderboard-rows"><p class="leaderboard-message">Loading ${formatDifficulty(selectedDifficulty)} rankings…</p></div>
        </section>
      </section>
    </main>
  `;

  const rows = app!.querySelector<HTMLDivElement>("#leaderboard-rows");
  const tabs = app!.querySelectorAll<HTMLButtonElement>("[data-leaderboard-difficulty]");
  if (!rows || !user) return;

  const renderSelectedDifficulty = async (): Promise<void> => {
    rows.innerHTML = `<p class="leaderboard-message">Loading ${formatDifficulty(selectedDifficulty)} rankings…</p>`;
    try {
      const leaderboard = await leaderboardApi.getLeaderboard(selectedDifficulty);
      if (leaderboard.difficulty !== selectedDifficulty) return;
      rows.innerHTML = leaderboard.entries.length === 0
        ? `<p class="leaderboard-message">No verified victories yet. Be the first to escape this maze.</p>`
        : leaderboard.entries.map((entry, index) => leaderboardRow(entry, index + 1, entry.username === user.username)).join("");
    } catch (error) {
      rows.innerHTML = `<p class="leaderboard-message is-error">${escapeHtml(error instanceof ApiError ? error.message : "Rankings could not be loaded. Please try again.")}</p>`;
    }
  };

  tabs.forEach((tab) => {
    tab.addEventListener("click", () => {
      selectedDifficulty = tab.dataset.leaderboardDifficulty as Difficulty;
      tabs.forEach((button) => {
        const selected = button === tab;
        button.classList.toggle("is-selected", selected);
        button.setAttribute("aria-selected", String(selected));
      });
      void renderSelectedDifficulty();
    });
  });

  await renderSelectedDifficulty();
}

function leaderboardRow(entry: LeaderboardEntry, rank: number, isCurrentUser: boolean): string {
  return `
    <article class="leaderboard-row${isCurrentUser ? " is-current-user" : ""}">
      <span>#${rank}</span>
      <span>${escapeHtml(entry.username)}${isCurrentUser ? " <em>(you)</em>" : ""}</span>
      <strong>${entry.score.toLocaleString("en-US")}</strong>
      <span>Completed</span>
    </article>
  `;
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
