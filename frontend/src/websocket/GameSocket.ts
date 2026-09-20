import type { Direction, StateMessage } from "../api";
import type { GameCommand } from "../types/game";

export type GameConnectionStatus = "CONNECTING" | "LIVE" | "RECONNECTING" | "LOST";

export interface GameSocketHandlers {
  onState: (state: StateMessage) => void;
  onConnectionStatus: (status: GameConnectionStatus) => void;
}

const MAX_RECONNECT_DELAY_MS = 8_000;

export class GameSocket {
  private socket?: WebSocket;
  private reconnectTimer?: number;
  private reconnectAttempt = 0;
  private closedByClient = false;
  private gameId?: string;
  private accessToken?: string;
  private handlers?: GameSocketHandlers;

  connect(gameId: string, accessToken: string, handlers: GameSocketHandlers): void {
    this.close();
    this.closedByClient = false;
    this.gameId = gameId;
    this.accessToken = accessToken;
    this.handlers = handlers;
    this.open(false);
  }

  sendMove(direction: Direction): void {
    this.sendCommand("MOVE", direction);
  }

  sendJump(direction: Direction): void {
    this.sendCommand("JUMP", direction);
  }

  private sendCommand(type: GameCommand["type"], direction: Direction): void {
    const command: GameCommand = {
      type,
      commandId: crypto.randomUUID(),
      direction,
    };
    this.send(command);
  }

  close(): void {
    this.closedByClient = true;
    if (this.reconnectTimer !== undefined) {
      window.clearTimeout(this.reconnectTimer);
      this.reconnectTimer = undefined;
    }
    this.socket?.close();
    this.socket = undefined;
  }

  private open(isReconnect: boolean): void {
    if (!this.gameId || !this.accessToken) {
      return;
    }

    this.handlers?.onConnectionStatus(isReconnect ? "RECONNECTING" : "CONNECTING");
    const socket = new WebSocket(this.websocketUrl());
    this.socket = socket;

    socket.addEventListener("open", () => {
      if (socket !== this.socket) {
        return;
      }
      this.reconnectAttempt = 0;
      this.handlers?.onConnectionStatus("LIVE");
    });

    socket.addEventListener("message", (event) => {
      if (socket !== this.socket) {
        return;
      }
      try {
        const message: unknown = JSON.parse(String(event.data));
        if (isStateMessage(message)) {
          this.handlers?.onState(message);
        }
      } catch {
        // Ignore a malformed server message; the next valid state can still update the game.
      }
    });

    socket.addEventListener("close", () => {
      if (socket !== this.socket || this.closedByClient) {
        return;
      }
      this.handlers?.onConnectionStatus("LOST");
      this.scheduleReconnect();
    });
  }

  private scheduleReconnect(): void {
    if (this.closedByClient || this.reconnectTimer !== undefined) {
      return;
    }

    const delay = Math.min(1_000 * 2 ** this.reconnectAttempt, MAX_RECONNECT_DELAY_MS);
    this.reconnectAttempt += 1;
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = undefined;
      this.open(true);
    }, delay);
  }

  private send(command: GameCommand): void {
    if (this.socket?.readyState !== WebSocket.OPEN) {
      throw new Error("Waiting for a live connection.");
    }
    this.socket.send(JSON.stringify(command));
  }

  private websocketUrl(): string {
    const configuredUrl = import.meta.env.VITE_GAME_WS_URL;
    if (configuredUrl) {
      return `${configuredUrl.replace(/\/$/, "")}/ws/games/${this.gameId}?access_token=${encodeURIComponent(this.accessToken ?? "")}`;
    }

    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    return `${protocol}//${window.location.host}/ws/games/${this.gameId}?access_token=${encodeURIComponent(this.accessToken ?? "")}`;
  }
}

function isStateMessage(message: unknown): message is StateMessage {
  return typeof message === "object" && message !== null && (message as { type?: unknown }).type === "STATE";
}
