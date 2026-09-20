import { ApiClient } from "./ApiClient";
import type { AuthSession, CurrentUser, LoginRequest, RegisteredUser, RegisterRequest } from "./types";

/** REST functions for account creation, login, and the current user. */
export class AuthApi {
  constructor(private readonly client: ApiClient) {}

  register(request: RegisterRequest): Promise<RegisteredUser> {
    return this.client.post<RegisteredUser>("/api/auth/register", request);
  }

  async login(request: LoginRequest): Promise<AuthSession> {
    const session = await this.client.post<AuthSession>("/api/auth/login", request);
    this.client.setAccessToken(session.accessToken);
    return session;
  }

  getCurrentUser(): Promise<CurrentUser> {
    return this.client.get<CurrentUser>("/api/auth/me", { authenticated: true });
  }

  async logout(): Promise<void> {
    try {
      await this.client.post<void>("/api/auth/logout", undefined, { authenticated: true });
    } finally {
      this.client.clearAccessToken();
    }
  }
}
