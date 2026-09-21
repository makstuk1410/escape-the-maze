import { ApiError, apiClient, gameApi, type GameState } from "../api";
import { authState } from "../auth";
import { KeyboardInput, type KeyboardCommand } from "../input/KeyboardInput";
import { formatDifficulty } from "../views/shared";
import { GameSocket } from "../websocket/GameSocket";
import { CanvasRenderer } from "./CanvasRenderer";
import { activeEffectsLabel, applyStateUpdate, fogIntensity, formatDirection, formatScore, isFrozen, renderConnectionStatus, renderHealth, resizeCanvasForDisplay } from "./gameplayState";

/** Owns every browser resource needed while one maze is visible. */
export class GameplayPage {
  private resizeObserver?: ResizeObserver;
  private timer?: number;
  private fogRefresh?: number;
  private keyboard?: KeyboardInput;
  private socket?: GameSocket;
  private animation?: number;
  private escapeHandler?: (event: KeyboardEvent) => void;

  async render(app: HTMLDivElement): Promise<void> {
    const gameId = sessionStorage.getItem("escape-the-maze.current-game-id");
    if (!authState.isAuthenticated() || !gameId) { location.hash = "#game-setup"; return; }
    app.innerHTML = markup();
    const elements = getElements(app);
    const renderer = new CanvasRenderer(elements.canvas);
    let state: GameState | undefined;
    let player: { x: number; y: number } | undefined;
    let lift = 0;
    let moving = false;
    let jumping = false;
    let resultShown = false;

    const draw = (): void => {
      resizeCanvasForDisplay(elements.canvas);
      if (!state) { renderer.renderPlaceholder(); return; }
      renderer.renderViewport({ tiles: state.tiles, playerX: player?.x ?? state.player.x, playerY: player?.y ?? state.player.y, playerLift: lift, fogIntensity: fogIntensity(state.effects.fogUntil), size: 11 });
    };
    const renderState = (): void => {
      if (!state) return;
      elements.score.textContent = formatScore(state.score);
      renderHealth(elements.hearts, elements.health, state.player.health);
      elements.meta.textContent = `${state.difficulty}  •  ${state.generator} MAZE`;
      elements.effects.textContent = activeEffectsLabel(state.effects);
      elements.pause.disabled = state.status !== "RUNNING";
      draw();
    };
    const stopTimer = (): void => { if (this.timer !== undefined) { clearInterval(this.timer); this.timer = undefined; } };
    const startTimer = (endsAt: string): void => {
      stopTimer(); const end = new Date(endsAt).getTime();
      const update = (): void => { const seconds = Math.max(0, Math.ceil((end - Date.now()) / 1000)); elements.timer.textContent = `${String(Math.floor(seconds / 60)).padStart(2, "0")}:${String(seconds % 60).padStart(2, "0")}`; if (seconds === 0) stopTimer(); };
      update(); this.timer = window.setInterval(update, 1000);
    };
    const animateTo = (target: { x: number; y: number }, jumped: boolean): void => {
      const start = player ?? target;
      if (start.x === target.x && start.y === target.y) { player = target; lift = 0; draw(); return; }
      if (this.animation !== undefined) cancelAnimationFrame(this.animation);
      const started = performance.now(); const duration = (isFrozen(state?.effects.frozenUntil ?? null) ? 550 : 150) + (jumped ? 110 : 0);
      const animate = (now: number): void => { const progress = Math.min(1, (now - started) / duration); const eased = 1 - (1 - progress) ** 3; player = { x: start.x + (target.x - start.x) * eased, y: start.y + (target.y - start.y) * eased }; lift = jumped ? Math.sin(progress * Math.PI) * .35 : 0; draw(); if (progress < 1) { this.animation = requestAnimationFrame(animate); } else { player = target; lift = 0; this.animation = undefined; draw(); } };
      this.animation = requestAnimationFrame(animate);
    };
    const leave = (): void => { sessionStorage.removeItem("escape-the-maze.current-game-id"); location.hash = "#menu"; };
    const showResult = (): void => {
      if (!state || resultShown || state.status === "RUNNING" || state.status === "PAUSED") return;
      resultShown = true;
      const content = state.status === "WON" ? ["RUN COMPLETE", "You escaped!", `Your ${formatDifficulty(state.difficulty)} run is complete. The server verified your score.`, `New personal best for ${formatDifficulty(state.difficulty)}!`, "Play again"] : state.status === "LOST" ? ["RUN ENDED", "The maze got you.", "You ran out of health. Win a run to save a score to the leaderboard.", `Try again to improve your ${formatDifficulty(state.difficulty)} best.`, "Try again"] : ["TIME EXPIRED", "Time ran out.", "The exit stayed ahead of you. Win a run to save a score to the leaderboard.", `Try a faster route on your next ${formatDifficulty(state.difficulty)} run.`, "Play again"];
      [elements.resultKicker.textContent, elements.resultTitle.textContent, elements.resultCopy.textContent, elements.resultNote.textContent, elements.resultPrimary.textContent] = content;
      elements.resultScore.textContent = formatScore(state.score); elements.pause.disabled = true; this.keyboard?.stop(); stopTimer(); elements.resultModal.hidden = false;
    };
    const resume = async (): Promise<void> => {
      if (!state || state.status !== "PAUSED") return;
      elements.continueButton.disabled = true;
      try { state = await gameApi.resumeGame(gameId); elements.pauseModal.hidden = true; renderState(); startTimer(state.endsAt); this.keyboard?.start(); elements.status.textContent = "LIVE • Space + direction jumps"; }
      catch (error) { elements.status.textContent = error instanceof ApiError ? error.message : "Game could not resume."; }
      finally { elements.continueButton.disabled = false; }
    };
    elements.resultPrimary.addEventListener("click", () => { sessionStorage.removeItem("escape-the-maze.current-game-id"); location.hash = "#game-setup"; });
    elements.resultMenu.addEventListener("click", leave); elements.leaveButton.addEventListener("click", leave); elements.continueButton.addEventListener("click", () => void resume());
    this.escapeHandler = (event) => { if (event.key === "Escape" && !elements.pauseModal.hidden) { event.preventDefault(); void resume(); } };
    addEventListener("keydown", this.escapeHandler);
    elements.pause.addEventListener("click", async () => {
      if (!state || state.status !== "RUNNING") return;
      elements.pause.disabled = true;
      try { state = await gameApi.pauseGame(gameId); renderState(); stopTimer(); this.keyboard?.stop(); elements.status.textContent = "Run paused"; if (state.status === "PAUSED") elements.pauseModal.hidden = false; else showResult(); }
      catch (error) { elements.pause.disabled = false; elements.status.textContent = error instanceof ApiError ? error.message : "Game could not pause."; }
    });
    this.resizeObserver = new ResizeObserver(draw); this.resizeObserver.observe(elements.canvas);
    this.fogRefresh = setInterval(() => { if (state) { elements.effects.textContent = activeEffectsLabel(state.effects); if (fogIntensity(state.effects.fogUntil) > 0) draw(); } }, 50);
    draw(); elements.status.textContent = "Connecting…";
    try {
      state = await gameApi.getGame(gameId); player = { ...state.player }; renderState(); if (state.status === "RUNNING") startTimer(state.endsAt); if (state.status === "PAUSED") elements.pauseModal.hidden = false; showResult();
      const token = apiClient.getAccessToken(); if (!token) throw new Error("Sign in again to connect to this game.");
      this.socket = new GameSocket(); const socket = this.socket;
      socket.connect(gameId, token, { onState: (update) => { const wasJump = jumping; moving = false; jumping = false; if (!state || update.stateVersion <= state.stateVersion) return; state = applyStateUpdate(state, update); renderState(); animateTo(state.player, wasJump); if (state.status === "PAUSED") { stopTimer(); this.keyboard?.stop(); elements.pauseModal.hidden = false; } showResult(); }, onConnectionStatus: (connection) => { renderConnectionStatus(connection, elements.connection, elements.connectionLabel, elements.connectionBanner); if (connection === "LIVE") elements.status.textContent = "LIVE • Space + direction jumps"; else { moving = false; elements.status.textContent = "Movement paused"; } } });
      const sendMovementCommand = ({ direction, jump }: KeyboardCommand): void => { if (state?.status !== "RUNNING") { elements.status.textContent = "The run is not active."; return; } if (moving || this.animation !== undefined) { elements.status.textContent = "Finishing move…"; return; } try { jump ? socket.sendJump(direction) : socket.sendMove(direction); moving = true; jumping = jump; elements.status.textContent = jump ? `Jumping: ${formatDirection(direction)}` : `Sent: ${formatDirection(direction)}`; } catch (error) { elements.status.textContent = error instanceof Error ? error.message : "Move was not sent."; } };
      this.keyboard = new KeyboardInput(sendMovementCommand);
      if (window.matchMedia("(max-width: 720px) and (pointer: coarse)").matches) {
        let jumpArmed = false;
        elements.jumpButton.addEventListener("click", () => { jumpArmed = !jumpArmed; elements.jumpButton.classList.toggle("is-armed", jumpArmed); elements.jumpButton.setAttribute("aria-pressed", String(jumpArmed)); elements.status.textContent = jumpArmed ? "Jump ready: choose a direction" : "Jump cancelled"; });
        elements.touchDirections.forEach((button) => button.addEventListener("click", () => { sendMovementCommand({ direction: button.dataset.direction as "UP" | "DOWN" | "LEFT" | "RIGHT", jump: jumpArmed }); jumpArmed = false; elements.jumpButton.classList.remove("is-armed"); elements.jumpButton.setAttribute("aria-pressed", "false"); }));
      }
      if (state.status === "RUNNING") this.keyboard.start();
    } catch (error) { elements.status.textContent = error instanceof ApiError ? error.message : "Game could not be loaded"; }
  }

  dispose(): void { this.resizeObserver?.disconnect(); this.keyboard?.stop(); this.socket?.close(); if (this.timer !== undefined) clearInterval(this.timer); if (this.fogRefresh !== undefined) clearInterval(this.fogRefresh); if (this.animation !== undefined) cancelAnimationFrame(this.animation); if (this.escapeHandler) removeEventListener("keydown", this.escapeHandler); }
}

function getElements(app: HTMLDivElement) {
  const find = <T extends Element>(selector: string): T => { const element = app.querySelector<T>(selector); if (!element) throw new Error(`Missing gameplay element: ${selector}`); return element; };
  return { canvas: find<HTMLCanvasElement>("#game-canvas"), score: find<HTMLOutputElement>("#score-value"), hearts: find<HTMLOutputElement>("#health-hearts"), health: find<HTMLOutputElement>("#health-value"), timer: find<HTMLOutputElement>("#timer-value"), meta: find<HTMLElement>("#game-meta"), effects: find<HTMLOutputElement>("#active-effects"), status: find<HTMLElement>("#movement-status"), connection: find<HTMLElement>("#connection-status"), connectionLabel: find<HTMLElement>("#connection-status-label"), connectionBanner: find<HTMLElement>("#connection-lost-banner"), pause: find<HTMLButtonElement>("#pause-button"), jumpButton: find<HTMLButtonElement>("#touch-jump-button"), touchDirections: Array.from(app.querySelectorAll<HTMLButtonElement>("[data-direction]")), pauseModal: find<HTMLElement>("#pause-modal"), continueButton: find<HTMLButtonElement>("#continue-game-button"), leaveButton: find<HTMLButtonElement>("#leave-game-button"), resultModal: find<HTMLElement>("#result-modal"), resultKicker: find<HTMLElement>("#result-modal-kicker"), resultTitle: find<HTMLElement>("#result-modal-title"), resultCopy: find<HTMLElement>("#result-modal-copy"), resultScore: find<HTMLOutputElement>("#result-score"), resultNote: find<HTMLElement>("#result-modal-note"), resultPrimary: find<HTMLButtonElement>("#result-primary-button"), resultMenu: find<HTMLButtonElement>("#result-menu-button") };
}

function markup(): string { return `<main class="gameplay-page"><header class="gameplay-header"><div class="game-identity"><p>ESCAPE THE MAZE</p><span id="game-meta">LOADING RUN…</span></div><div class="gameplay-live-details"><output class="timer-card" id="timer-value">05:00</output><span class="connection-status is-connecting" id="connection-status"><span class="connection-dot"></span><span class="connection-status-label" id="connection-status-label">CONNECTING</span></span></div><div class="gameplay-actions"><button class="pause-button" id="pause-button" type="button">Pause</button><span class="sr-only" id="movement-status" aria-live="polite"></span></div></header><p class="connection-lost-banner" id="connection-lost-banner"></p><section class="gameplay-layout"><aside class="run-stats"><section class="score-card"><p>SCORE</p><output id="score-value">0000</output></section><section class="health-card"><p>HEALTH</p><output class="health-hearts" id="health-hearts">♥♥♥♥♥</output><output class="health-value" id="health-value">100 / 100</output></section><section class="controls-card"><p>CONTROLS</p><dl><div><dt>Move</dt><dd>WASD / arrows</dd></div><div><dt>Jump</dt><dd>Hold Space + direction</dd></div></dl></section></aside><section class="canvas-stage"><canvas id="game-canvas" width="720" height="720"></canvas><nav class="touch-controls" aria-label="Game controls"><div class="touch-direction-pad"><button type="button" class="touch-direction touch-up" data-direction="UP" aria-label="Move up">↑</button><button type="button" class="touch-direction" data-direction="LEFT" aria-label="Move left">←</button><button type="button" class="touch-direction" data-direction="DOWN" aria-label="Move down">↓</button><button type="button" class="touch-direction" data-direction="RIGHT" aria-label="Move right">→</button></div><button type="button" class="touch-jump" id="touch-jump-button" aria-pressed="false">JUMP</button></nav></section><aside class="game-context"><section class="objective-card"><p>OBJECTIVE</p><h2>Find the exit</h2><span>Reach the green exit before time runs out.</span></section><section class="effects-card"><p>ACTIVE EFFECTS</p><output id="active-effects">No active effects</output></section><section class="tile-guide-card"><p>TILE GUIDE</p><ul><li class="guide-gold">Gold <span>+10 points</span></li><li class="guide-spikes">Spikes <span>lose health</span></li><li class="guide-freeze">Freeze <span>movement slows</span></li><li class="guide-fog">Fog <span>vision reduces</span></li></ul></section></aside></section><div class="game-modal-overlay" id="pause-modal" hidden><section class="game-modal pause-modal"><h2>RUN PAUSED</h2><p class="game-modal-copy">Your current run is paused. Continue when you are ready.</p><div class="game-modal-spacer"></div><button class="game-modal-primary" id="continue-game-button">Continue</button><button class="game-modal-secondary" id="leave-game-button">Leave game</button><p class="game-modal-hint">Press Esc to continue</p></section></div><div class="game-modal-overlay" id="result-modal" hidden><section class="game-modal result-modal"><p class="game-modal-kicker" id="result-modal-kicker">RUN COMPLETE</p><h2 id="result-modal-title">You escaped!</h2><p class="game-modal-copy" id="result-modal-copy"></p><section class="final-score-card"><p>FINAL SCORE</p><output id="result-score">0000</output></section><p class="result-modal-note" id="result-modal-note"></p><button class="game-modal-primary" id="result-primary-button">Play again</button><button class="game-modal-secondary" id="result-menu-button">Back to menu</button></section></div></main>`; }
