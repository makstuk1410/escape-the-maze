import type { GameState, StateMessage } from "../api";
import type { GameConnectionStatus } from "../websocket/GameSocket";

export function formatScore(score: number): string {
  return String(Math.max(0, score)).padStart(4, "0");
}

export function renderConnectionStatus(
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

/** Applies one incremental WebSocket message without mutating the existing state. */
export function applyStateUpdate(current: GameState, state: StateMessage): GameState {
  const tiles = current.tiles.map((row) => [...row]);
  state.changedTiles.forEach(({ x, y, type }) => {
    if (tiles[y]?.[x] !== undefined) tiles[y][x] = type;
  });
  return { ...current, tiles, player: state.player, score: state.score, status: state.status,
    endsAt: state.endsAt, effects: state.effects, stateVersion: state.stateVersion };
}

export function fogIntensity(fogUntil: string | null): number {
  if (!fogUntil) return 0;
  const elapsed = 1 - Math.max(0, new Date(fogUntil).getTime() - Date.now()) / 4_000;
  if (elapsed <= 0 || elapsed >= 1) return 0;
  if (elapsed < .25) return elapsed / .25;
  return elapsed > .75 ? (1 - elapsed) / .25 : 1;
}

export function isFrozen(frozenUntil: string | null): boolean {
  return frozenUntil !== null && new Date(frozenUntil).getTime() > Date.now();
}

export function activeEffectsLabel(effects: GameState["effects"]): string {
  const active: string[] = [];
  if (isFrozen(effects.frozenUntil)) active.push("Freeze — movement slowed");
  if (fogIntensity(effects.fogUntil) > 0) active.push("Fog — vision reduced");
  return active.length === 0 ? "No active effects" : active.join(" • ");
}

export function formatDirection(direction: "UP" | "DOWN" | "LEFT" | "RIGHT"): string {
  return direction.charAt(0) + direction.slice(1).toLowerCase();
}

export function renderHealth(heartsElement: HTMLOutputElement, valueElement: HTMLOutputElement, health: number): void {
  const boundedHealth = Math.max(0, Math.min(100, health));
  const filledHearts = Math.ceil(boundedHealth / 20);
  heartsElement.innerHTML = `${"<span class=\"heart-filled\">♥</span>".repeat(filledHearts)}${"<span class=\"heart-empty\">♡</span>".repeat(5 - filledHearts)}`;
  heartsElement.setAttribute("aria-label", `${boundedHealth} of 100 health`);
  valueElement.textContent = `${boundedHealth} / 100`;
}

export function resizeCanvasForDisplay(canvas: HTMLCanvasElement): void {
  const bounds = canvas.getBoundingClientRect();
  const pixelRatio = window.devicePixelRatio || 1;
  const width = Math.max(1, Math.round(bounds.width * pixelRatio));
  const height = Math.max(1, Math.round(bounds.height * pixelRatio));
  if (canvas.width !== width || canvas.height !== height) {
    canvas.width = width;
    canvas.height = height;
  }
}
