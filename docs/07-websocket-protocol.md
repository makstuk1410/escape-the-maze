# WebSocket Protocol

## Connection

- Endpoint: `/ws/games/{gameId}`
- Browser connection URL: `/ws/games/{gameId}?access_token={jwt}`
- The browser connects after `POST /api/games` returns a game identifier.
- The handshake validates `access_token` using the same JWT verification as
  protected REST requests and rejects missing, invalid, or expired tokens with
  `401 Unauthorized` before opening the socket.
- The server verifies that the authenticated user owns `{gameId}` before
  opening the socket. An unknown game returns `404`; another user's game
  returns `403`.
- All messages are JSON objects containing a `type` field.

## Client-to-Server Messages

| Type | Required fields | Purpose |
|---|---|---|
| `MOVE` | `commandId`, `direction` | Request one logical move. |
| `JUMP` | `commandId`, `direction` | Request a two-tile jump over one walkable tile. |

### MOVE

```json
{
  "type": "MOVE",
  "commandId": "8e7d6cf1-4e8e-4b9d-b14c-58fdf10ad67f",
  "direction": "RIGHT"
}
```

### JUMP

```json
{
  "type": "JUMP",
  "commandId": "1a2c4780-cc2c-431c-a3e8-8322f50c3c20",
  "direction": "RIGHT"
}
```

The server accepts a jump only when both the crossed tile and landing tile are
inside the maze and walkable. The crossed tile's gold or hazard effect is not
applied; the landing tile is processed normally. A jump cannot pass through a
wall.

`direction` must be one of `UP`, `DOWN`, `LEFT`, or `RIGHT`. The browser sends
a requested direction only; it does not send player coordinates, health,
score, or status.

## Server-to-Client Messages

| Type | Required fields | Purpose |
|---|---|---|
| `STATE` | `stateVersion`, `player`, `score`, `status`, `endsAt`, `effects` | Send the current authoritative game state. |
| `GAME_FINISHED` | `stateVersion`, `status`, `score` | Notify the browser that a terminal state was reached. |
| `ERROR` | `code`, `message` | Explain a rejected command or connection-level error. |

### STATE

The server sends `STATE` immediately after a successful connection and after a
command that changes or confirms the game state.

```json
{
  "type": "STATE",
  "stateVersion": 12,
  "player": {
    "x": 11,
    "y": 5,
    "health": 80
  },
  "score": 10,
  "status": "RUNNING",
  "endsAt": "2026-09-17T12:05:00Z",
  "effects": {
    "frozenUntil": null,
    "fogUntil": "2026-09-17T12:01:04Z"
  },
  "changedTiles": [
    {
      "x": 11,
      "y": 5,
      "type": "EMPTY"
    }
  ]
}
```

`changedTiles` is empty when no tile changed. It lets the browser update
collected gold without receiving the entire maze after every move.

### GAME_FINISHED

```json
{
  "type": "GAME_FINISHED",
  "stateVersion": 18,
  "status": "WON",
  "score": 110
}
```

`status` is one of `WON`, `LOST`, or `TIMED_OUT`. The server persists the
completed result before or atomically with emitting this event.

### ERROR

```json
{
  "type": "ERROR",
  "code": "GAME_NOT_RUNNING",
  "message": "This game has already finished."
}
```

Possible MVP error codes are `UNAUTHORIZED`, `FORBIDDEN`, `GAME_NOT_FOUND`,
`GAME_NOT_RUNNING`, `INVALID_MESSAGE`, and `INVALID_DIRECTION`.
