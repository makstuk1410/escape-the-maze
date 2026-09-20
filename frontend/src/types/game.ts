export type { Difficulty, Direction, GameState } from "../api/types";

export interface MoveCommand {
  type: "MOVE";
  commandId: string;
  direction: import("../api/types").Direction;
}

export interface JumpCommand {
  type: "JUMP";
  commandId: string;
  direction: import("../api/types").Direction;
}

export type GameCommand = MoveCommand | JumpCommand;
