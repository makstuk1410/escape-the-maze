import { ApiClient } from "./ApiClient";
import type { Difficulty, GameState, Level } from "./types";

/** REST functions used before gameplay begins and during recovery. */
export class GameApi {
  constructor(private readonly client: ApiClient) {}

  getLevels(): Promise<Level[]> {
    return this.client.get<Level[]>("/api/levels", { authenticated: true });
  }

  createGame(difficulty: Difficulty): Promise<GameState> {
    return this.client.post<GameState>("/api/games", { difficulty }, { authenticated: true });
  }

  getGame(gameId: string): Promise<GameState> {
    return this.client.get<GameState>(`/api/games/${encodeURIComponent(gameId)}`, { authenticated: true });
  }
}
