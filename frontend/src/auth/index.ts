import { apiClient, authApi } from "../api";
import { AuthState } from "./AuthState";

/** Default shared authentication state for browser pages. */
export const authState = new AuthState(authApi, apiClient);

export { AuthState } from "./AuthState";
export type { AuthSnapshot, AuthStatus } from "./AuthState";
