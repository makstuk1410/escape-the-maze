import type { Direction, StateMessage } from "../api";
import type { MoveCommand } from "../types/game";

/** Authenticated native WebSocket connection for one server-owned game session. */
export class GameSocket {
  private socket?: WebSocket;

  connect(gameId: string, accessToken: string, onState: (state: StateMessage) => void): Promise<void> {
    this.close();
    const configuredBaseUrl = import.meta.env.VITE_WS_BASE_URL;
    const baseUrl = configuredBaseUrl || `${window.location.protocol === "https:" ? "wss" : "ws"}://${window.location.host}`;
    const url = new URL(`${baseUrl}/ws/games/${encodeURIComponent(gameId)}`);
    url.searchParams.set("access_token", accessToken);

    return new Promise((resolve, reject) => {
      let opened = false;
      const socket = new WebSocket(url);
      this.socket = socket;
      socket.addEventListener("message", (event) => {
        if (typeof event.data !== "string") return;
        try {
          const message: unknown = JSON.parse(event.data);
          if (isStateMessage(message)) onState(message);
        } catch {
          // Ignore malformed or future protocol messages that this client cannot apply.
        }
      });
      socket.addEventListener("open", () => {
        opened = true;
        resolve();
      }, { once: true });
      socket.addEventListener("error", () => reject(new Error("Game WebSocket connection failed.")), { once: true });
      socket.addEventListener("close", () => {
        if (!opened) reject(new Error("Game WebSocket connection was rejected."));
      }, { once: true });
    });
  }

  sendMove(direction: Direction): void {
    this.send({ type: "MOVE", commandId: commandId(), direction });
  }

  close(): void {
    this.socket?.close();
    this.socket = undefined;
  }

  private send(command: MoveCommand): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      throw new Error("Game WebSocket is not connected.");
    }
    this.socket.send(JSON.stringify(command));
  }
}

function isStateMessage(message: unknown): message is StateMessage {
  return typeof message === "object"
    && message !== null
    && (message as { type?: unknown }).type === "STATE"
    && typeof (message as { stateVersion?: unknown }).stateVersion === "number"
    && typeof (message as { player?: { x?: unknown } }).player?.x === "number"
    && typeof (message as { player?: { y?: unknown } }).player?.y === "number";
}

function commandId(): string {
  return typeof crypto.randomUUID === "function"
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}
