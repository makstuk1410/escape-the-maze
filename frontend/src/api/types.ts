export type Direction = "UP" | "DOWN" | "LEFT" | "RIGHT";
export type Difficulty = "EASY" | "NORMAL" | "HARD" | "EXPERT";
export type GeneratorType = "DFS" | "PRIM" | "KRUSKAL" | "BINARY_TREE";
export type TileType = "WALL" | "EMPTY" | "GOLD" | "SPIKES" | "FREEZE" | "FOG" | "EXIT";
export type GameStatus = "RUNNING" | "PAUSED" | "WON" | "LOST" | "TIMED_OUT";

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface RegisteredUser {
  id: string;
  username: string;
  email: string;
  createdAt: string;
}

export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface AuthSession {
  userId: string;
  username: string;
  email: string;
  accessToken: string;
  tokenType: "Bearer";
  expiresAt: string;
}

export interface CurrentUser {
  id: string;
  username: string;
  email: string;
  createdAt: string;
}

export interface Level {
  difficulty: Difficulty;
  generator: GeneratorType;
  logicalMazeWidth: number;
  logicalMazeHeight: number;
  gridWidth: number;
  gridHeight: number;
  timeLimitSeconds: number;
}

export interface Player {
  x: number;
  y: number;
  health: number;
}

export interface GameState {
  gameId: string;
  difficulty: Difficulty;
  generator: GeneratorType;
  width: number;
  height: number;
  tiles: TileType[][];
  player: Player;
  score: number;
  status: GameStatus;
  startedAt: string;
  endsAt: string;
  effects: { frozenUntil: string | null; fogUntil: string | null };
  stateVersion: number;
}

/** Authoritative incremental game state received over the game WebSocket. */
export interface StateMessage {
  type: "STATE";
  stateVersion: number;
  player: Player;
  score: number;
  status: GameStatus;
  endsAt: string;
  effects: { frozenUntil: string | null; fogUntil: string | null };
  changedTiles: Array<{ x: number; y: number; type: TileType }>;
}

export interface LeaderboardEntry {
  rank: number;
  username: string;
  score: number;
  status: Extract<GameStatus, "WON" | "LOST" | "TIMED_OUT">;
  endedAt: string;
}

export interface Leaderboard {
  difficulty: Difficulty;
  entries: LeaderboardEntry[];
}
