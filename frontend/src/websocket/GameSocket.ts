import type { MoveCommand } from "../types/game";

export class GameSocket {
  private socket?: WebSocket;

  connect(gameId: string): void {
    const configuredBaseUrl = import.meta.env.VITE_WS_BASE_URL;
    const baseUrl = configuredBaseUrl || `${window.location.protocol === "https:" ? "wss" : "ws"}://${window.location.host}`;
    this.socket = new WebSocket(`${baseUrl}/ws/games/${gameId}`);
  }

  send(command: MoveCommand): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      throw new Error("Game WebSocket is not connected.");
    }

    this.socket.send(JSON.stringify(command));
  }

  close(): void {
    this.socket?.close();
    this.socket = undefined;
  }
}
