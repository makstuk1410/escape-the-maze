export type { Difficulty, Direction, GameState } from "../api/types";

export interface MoveCommand {
  type: "MOVE";
  commandId: string;
  direction: import("../api/types").Direction;
}
