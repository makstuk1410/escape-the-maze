export class ApiClient {
  constructor(private readonly baseUrl = import.meta.env.VITE_API_BASE_URL ?? "") {
  }

  async health(): Promise<{ status: string }> {
    const response = await fetch(`${this.baseUrl}/api/health`);

    if (!response.ok) {
      throw new Error(`Health request failed with HTTP ${response.status}.`);
    }

    return response.json() as Promise<{ status: string }>;
  }
}
