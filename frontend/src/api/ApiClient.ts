export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly details?: unknown,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export interface ApiRequestOptions extends Omit<RequestInit, "body" | "headers" | "method"> {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  authenticated?: boolean;
  headers?: HeadersInit;
}

/** Shared HTTP client for all REST API modules. */
export class ApiClient {
  private accessToken?: string;

  constructor(private readonly baseUrl = import.meta.env.VITE_API_BASE_URL ?? "") {}

  setAccessToken(accessToken: string): void {
    this.accessToken = accessToken;
  }

  clearAccessToken(): void {
    this.accessToken = undefined;
  }

  async get<T>(path: string, options: Omit<ApiRequestOptions, "method"> = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: "GET" });
  }

  async post<T>(path: string, body?: unknown, options: Omit<ApiRequestOptions, "method" | "body"> = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: "POST", body });
  }

  async request<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
    const { body, authenticated = false, headers: suppliedHeaders, method = "GET", ...requestOptions } = options;
    const headers = new Headers(suppliedHeaders);
    headers.set("Accept", "application/json");

    if (body !== undefined) {
      headers.set("Content-Type", "application/json");
    }
    if (authenticated) {
      if (!this.accessToken) {
        throw new ApiError("You must be logged in to make this request.", 401);
      }
      headers.set("Authorization", `Bearer ${this.accessToken}`);
    }

    const response = await fetch(`${this.baseUrl}${path}`, {
      ...requestOptions,
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
    const responseBody = await this.parseResponseBody(response);

    if (!response.ok) {
      throw new ApiError(`Request failed with HTTP ${response.status}.`, response.status, responseBody);
    }

    return responseBody as T;
  }

  private async parseResponseBody(response: Response): Promise<unknown> {
    if (response.status === 204) {
      return undefined;
    }

    const body = await response.text();
    if (!body) {
      return undefined;
    }
    if (response.headers.get("Content-Type")?.includes("application/json")) {
      return JSON.parse(body) as unknown;
    }
    return body;
  }
}
