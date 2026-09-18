import type { Direction } from "../types/game";

const directionByKey: Record<string, Direction | undefined> = {
  ArrowUp: "UP",
  w: "UP",
  W: "UP",
  ArrowDown: "DOWN",
  s: "DOWN",
  S: "DOWN",
  ArrowLeft: "LEFT",
  a: "LEFT",
  A: "LEFT",
  ArrowRight: "RIGHT",
  d: "RIGHT",
  D: "RIGHT"
};

export class KeyboardInput {
  private readonly onKeyDown = (event: KeyboardEvent): void => {
    const direction = directionByKey[event.key];

    if (!direction) {
      return;
    }

    event.preventDefault();
    this.onMove(direction);
  };

  constructor(private readonly onMove: (direction: Direction) => void) {
  }

  start(): void {
    window.addEventListener("keydown", this.onKeyDown);
  }

  stop(): void {
    window.removeEventListener("keydown", this.onKeyDown);
  }
}
