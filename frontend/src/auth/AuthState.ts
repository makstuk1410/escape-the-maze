import { ApiError, type ApiClient } from "../api/ApiClient";
import type { AuthApi } from "../api/AuthApi";
import type { CurrentUser, LoginRequest, RegisterRequest, RegisteredUser } from "../api/types";

export type AuthStatus = "anonymous" | "restoring" | "authenticating" | "authenticated";

export interface AuthSnapshot {
  status: AuthStatus;
  user?: CurrentUser;
  error?: string;
}

type AuthListener = (snapshot: AuthSnapshot) => void;

const accessTokenStorageKey = "escape-the-maze.access-token";

/**
 * Shared browser authentication state for every page.
 * The token remains only for the current browser tab/session.
 */
export class AuthState {
  private snapshot: AuthSnapshot = { status: "anonymous" };
  private readonly listeners = new Set<AuthListener>();

  constructor(
    private readonly authApi: AuthApi,
    private readonly apiClient: ApiClient,
    private readonly storage: Pick<Storage, "getItem" | "setItem" | "removeItem"> = window.sessionStorage,
  ) {}

  getSnapshot(): AuthSnapshot {
    return this.snapshot;
  }

  isAuthenticated(): boolean {
    return this.snapshot.status === "authenticated";
  }

  subscribe(listener: AuthListener): () => void {
    this.listeners.add(listener);
    listener(this.snapshot);
    return () => this.listeners.delete(listener);
  }

  async restore(): Promise<AuthSnapshot> {
    const storedToken = this.storage.getItem(accessTokenStorageKey);
    if (!storedToken) {
      return this.update({ status: "anonymous" });
    }

    this.apiClient.setAccessToken(storedToken);
    this.update({ status: "restoring" });
    try {
      const user = await this.authApi.getCurrentUser();
      return this.update({ status: "authenticated", user });
    } catch (error) {
      this.clearSession();
      return this.update({ status: "anonymous", error: errorMessage(error) });
    }
  }

  async register(request: RegisterRequest): Promise<RegisteredUser> {
    return this.authApi.register(request);
  }

  async login(request: LoginRequest): Promise<AuthSnapshot> {
    this.update({ status: "authenticating" });
    try {
      const session = await this.authApi.login(request);
      this.storage.setItem(accessTokenStorageKey, session.accessToken);
      const user = await this.authApi.getCurrentUser();
      return this.update({ status: "authenticated", user });
    } catch (error) {
      this.clearSession();
      const message = error instanceof ApiError && error.status === 401
        ? "Incorrect username/email or password."
        : errorMessage(error);
      return this.update({ status: "anonymous", error: message });
    }
  }

  async logout(): Promise<void> {
    try {
      if (this.apiClient.getAccessToken()) {
        await this.authApi.logout();
      }
    } finally {
      this.clearSession();
      this.update({ status: "anonymous" });
    }
  }

  private clearSession(): void {
    this.apiClient.clearAccessToken();
    this.storage.removeItem(accessTokenStorageKey);
  }

  private update(snapshot: AuthSnapshot): AuthSnapshot {
    this.snapshot = snapshot;
    this.listeners.forEach((listener) => listener(this.snapshot));
    return this.snapshot;
  }
}

function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Authentication failed.";
}
