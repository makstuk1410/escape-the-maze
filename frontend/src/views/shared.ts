import type { Difficulty, Level } from "../api";

/** Escapes server-provided text before adding it to an HTML template. */
export function escapeHtml(value: string): string {
  return value.replace(/[&<>'"]/g, (character) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    "'": "&#039;",
    "\"": "&quot;",
  }[character] ?? character));
}

export function formatDifficulty(difficulty: Difficulty): string {
  return difficulty.charAt(0) + difficulty.slice(1).toLowerCase();
}

export function formatGenerator(generator: Level["generator"]): string {
  return generator === "DFS" ? "DFS" : generator.charAt(0) + generator.slice(1).toLowerCase();
}

export function setMessage(element: HTMLParagraphElement, text: string, isError = false): void {
  element.textContent = text;
  element.classList.toggle("is-error", isError);
  element.classList.toggle("is-visible", Boolean(text));
}

export function brandPanel(eyebrow: string, tagline: string): string {
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
