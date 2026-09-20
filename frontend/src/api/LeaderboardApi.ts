import { ApiClient } from "./ApiClient";
import type { Difficulty, Leaderboard } from "./types";

/** Public REST functions for completed, server-verified rankings. */
export class LeaderboardApi {
  constructor(private readonly client: ApiClient) {}

  getLeaderboard(difficulty: Difficulty): Promise<Leaderboard> {
    return this.client.get<Leaderboard>(`/api/leaderboard?difficulty=${difficulty}`);
  }
}
