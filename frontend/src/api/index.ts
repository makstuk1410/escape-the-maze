import { ApiClient } from "./ApiClient";
import { AuthApi } from "./AuthApi";
import { GameApi } from "./GameApi";
import { LeaderboardApi } from "./LeaderboardApi";

/** Default API modules for browser pages. */
export const apiClient = new ApiClient();
export const authApi = new AuthApi(apiClient);
export const gameApi = new GameApi(apiClient);
export const leaderboardApi = new LeaderboardApi(apiClient);

export { ApiClient, ApiError } from "./ApiClient";
export type * from "./types";
