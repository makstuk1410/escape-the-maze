import type { Direction } from "../types/game";

export interface KeyboardCommand {
  direction: Direction;
  jump: boolean;
}

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

// `event.code` identifies the physical key, independent of the active keyboard
// layout. This keeps WASD controls working with Ukrainian, Polish, and other
// layouts where `event.key` is not the Latin character.
const directionByCode: Record<string, Direction | undefined> = {
  ArrowUp: "UP",
  KeyW: "UP",
  ArrowDown: "DOWN",
  KeyS: "DOWN",
  ArrowLeft: "LEFT",
  KeyA: "LEFT",
  ArrowRight: "RIGHT",
  KeyD: "RIGHT"
};

export class KeyboardInput {
  private jumpHeld = false;

  private readonly onKeyDown = (event: KeyboardEvent): void => {
    if (event.code === "Space") {
      this.jumpHeld = true;
      event.preventDefault();
      return;
    }

    const direction = directionByCode[event.code] ?? directionByKey[event.key];

    if (!direction) {
      return;
    }

    event.preventDefault();
    this.onCommand({ direction, jump: this.jumpHeld });
  };

  private readonly onKeyUp = (event: KeyboardEvent): void => {
    if (event.code === "Space") {
      this.jumpHeld = false;
      event.preventDefault();
    }
  };

  constructor(private readonly onCommand: (command: KeyboardCommand) => void) {
  }

  start(): void {
    window.addEventListener("keydown", this.onKeyDown);
    window.addEventListener("keyup", this.onKeyUp);
  }

  stop(): void {
    window.removeEventListener("keydown", this.onKeyDown);
    window.removeEventListener("keyup", this.onKeyUp);
    this.jumpHeld = false;
  }
}
