import { ApiError, leaderboardApi, type Difficulty, type LeaderboardEntry } from "../api";
import { authState } from "../auth";
import { escapeHtml, formatDifficulty } from "./shared";

const DIFFICULTIES: Difficulty[] = ["EASY", "NORMAL", "HARD", "EXPERT"];

export async function renderMenuPage(app: HTMLDivElement): Promise<void> {
  const user = authState.getSnapshot().user;
  if (!authState.isAuthenticated() || !user) { window.location.hash = "#login"; return; }
  app.innerHTML = `
    <main class="menu-page"><aside class="menu-brand-panel"><div class="menu-brand-copy">
      <p class="eyebrow">ESCAPE THE MAZE</p><h1>FIND YOUR<br>WAY OUT.</h1><span class="title-accent" aria-hidden="true"></span>
      <p class="menu-tagline">Every path is a choice. Every second counts.</p>
      <section class="player-status" aria-labelledby="personal-best-title"><p class="player-name">${escapeHtml(user.username)}</p><h2 id="personal-best-title">PERSONAL BEST SCORES</h2>
        <div class="best-score-grid" id="best-score-grid" aria-live="polite">${DIFFICULTIES.map((difficulty) => bestScoreCell(difficulty, "…")).join("")}</div>
      </section>
    </div></aside><section class="menu-content" aria-labelledby="menu-heading">
      <header class="menu-header"><h2 id="menu-heading">Welcome, ${escapeHtml(user.username)}</h2><p>Choose your next move. Your best runs are saved to the leaderboard.</p></header>
      <section class="start-game-card" aria-labelledby="play-heading"><h2 id="play-heading">Ready for another run?</h2><p>Generate a new maze, choose the difficulty, and beat the clock.</p><button class="primary-button menu-play-button" type="button" id="play-button">Play now</button></section>
      <div class="menu-actions"><a class="menu-action leaderboard-action" href="#leaderboard"><strong>Leaderboard</strong><span>See your position and chase the highest score.</span></a><button class="menu-action logout-action" type="button" id="logout-button"><strong>Log out</strong><span>Leave this session safely. Your scores are saved.</span></button></div>
    </section></main>`;
  app.querySelector<HTMLButtonElement>("#play-button")?.addEventListener("click", () => { window.location.hash = "#game-setup"; });
  app.querySelector<HTMLButtonElement>("#logout-button")?.addEventListener("click", async () => { await authState.logout(); window.location.hash = "#login"; });
  const scoreGrid = app.querySelector<HTMLDivElement>("#best-score-grid");
  if (!scoreGrid) return;
  try { scoreGrid.innerHTML = (Object.entries(await loadPersonalBestScores(user.username)) as [Difficulty, number][]).map(([difficulty, score]) => bestScoreCell(difficulty, String(score))).join(""); }
  catch { scoreGrid.innerHTML = DIFFICULTIES.map((difficulty) => bestScoreCell(difficulty, "—")).join(""); }
}

export async function renderLeaderboardPage(app: HTMLDivElement): Promise<void> {
  if (!authState.isAuthenticated()) { window.location.hash = "#login"; return; }
  const user = authState.getSnapshot().user;
  let selectedDifficulty: Difficulty = "NORMAL";
  app.innerHTML = `<main class="leaderboard-page"><header class="leaderboard-top-bar"><div class="leaderboard-identity"><p>ESCAPE THE MAZE</p><span>GLOBAL RANKINGS</span></div><a class="leaderboard-back-button" href="#menu">Back to menu</a></header><section class="leaderboard-content" aria-labelledby="leaderboard-heading"><header class="leaderboard-heading"><h1 id="leaderboard-heading">Leaderboard</h1><p>Scores are ranked separately for each difficulty and use server-verified victories.</p></header><div class="leaderboard-tabs" role="tablist" aria-label="Choose leaderboard difficulty">${DIFFICULTIES.map((difficulty) => tab(difficulty, difficulty === selectedDifficulty)).join("")}</div><section class="leaderboard-table-card" aria-live="polite"><div class="leaderboard-table-header" aria-hidden="true"><span>RANK</span><span>PLAYER</span><span>SCORE</span><span>RESULT</span></div><div class="leaderboard-rows" id="leaderboard-rows"></div></section></section></main>`;
  const rows = app.querySelector<HTMLDivElement>("#leaderboard-rows");
  const tabs = app.querySelectorAll<HTMLButtonElement>("[data-leaderboard-difficulty]");
  if (!rows || !user) return;
  const load = async (): Promise<void> => {
    rows.innerHTML = `<p class="leaderboard-message">Loading ${formatDifficulty(selectedDifficulty)} rankings…</p>`;
    try {
      const leaderboard = await leaderboardApi.getLeaderboard(selectedDifficulty);
      if (leaderboard.difficulty !== selectedDifficulty) return;
      rows.innerHTML = leaderboard.entries.length ? leaderboard.entries.map((entry, index) => leaderboardRow(entry, index + 1, entry.username === user.username)).join("") : "<p class=\"leaderboard-message\">No verified victories yet. Be the first to escape this maze.</p>";
    } catch (error) { rows.innerHTML = `<p class="leaderboard-message is-error">${escapeHtml(error instanceof ApiError ? error.message : "Rankings could not be loaded. Please try again.")}</p>`; }
  };
  tabs.forEach((button) => button.addEventListener("click", () => { selectedDifficulty = button.dataset.leaderboardDifficulty as Difficulty; tabs.forEach((tabButton) => { const selected = tabButton === button; tabButton.classList.toggle("is-selected", selected); tabButton.setAttribute("aria-selected", String(selected)); }); void load(); }));
  await load();
}

async function loadPersonalBestScores(username: string): Promise<Record<Difficulty, number>> {
  const leaderboards = await Promise.all(DIFFICULTIES.map((difficulty) => leaderboardApi.getLeaderboard(difficulty)));
  return Object.fromEntries(leaderboards.map(({ difficulty, entries }) => [difficulty, Math.max(0, ...entries.filter((entry) => entry.username === username).map((entry) => entry.score))])) as Record<Difficulty, number>;
}

function bestScoreCell(difficulty: Difficulty, score: string): string { return `<div class="best-score-cell"><span>${difficulty}</span><strong>${score}</strong></div>`; }
function tab(difficulty: Difficulty, selected: boolean): string { return `<button class="leaderboard-tab${selected ? " is-selected" : ""}" type="button" role="tab" data-leaderboard-difficulty="${difficulty}" aria-selected="${selected}">${difficulty}</button>`; }
function leaderboardRow(entry: LeaderboardEntry, rank: number, isCurrentUser: boolean): string { return `<article class="leaderboard-row${isCurrentUser ? " is-current-user" : ""}"><span>#${rank}</span><span>${escapeHtml(entry.username)}${isCurrentUser ? " <em>(you)</em>" : ""}</span><strong>${entry.score.toLocaleString("en-US")}</strong><span>Completed</span></article>`; }
