export type Direction = "UP" | "DOWN" | "LEFT" | "RIGHT";

export type Difficulty = "EASY" | "NORMAL" | "HARD" | "EXPERT";

export interface MoveCommand {
  type: "MOVE";
  direction: Direction;
}

export interface GameState {
  gameId: string;
  difficulty: Difficulty;
  score: number;
  health: number;
  status: "RUNNING" | "PAUSED" | "WON" | "LOST" | "TIMED_OUT";
}
