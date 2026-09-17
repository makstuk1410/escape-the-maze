# REST API Design

## Conventions

- Base path: `/api`
- Request and response bodies use JSON.
- Protected endpoints require an authenticated user session.
- The server returns standard HTTP status codes and a JSON error response for
  invalid requests.
- Real-time movement commands are sent through WebSocket, not REST.

## Endpoint List

| Method | Path | Authentication | Purpose |
|---|---|---|---|
| `POST` | `/api/auth/register` | No | Create a user account and authenticate the new user. |
| `POST` | `/api/auth/login` | No | Authenticate an existing user. |
| `POST` | `/api/auth/logout` | Yes | End the current authenticated session. |
| `GET` | `/api/auth/me` | Yes | Return the current authenticated user. |
| `GET` | `/api/levels` | Yes | Return supported difficulties and maze generators. |
| `POST` | `/api/games` | Yes | Create a game session owned by the authenticated user. |
| `GET` | `/api/games/{gameId}` | Yes | Return the authoritative state of the owner's game session. |
| `GET` | `/api/leaderboard` | No | Return completed, server-verified leaderboard results. |

## Endpoint Responsibilities

### Authentication

- `POST /api/auth/register` accepts username, email, and password.
- `POST /api/auth/login` accepts an account identifier and password.
- `POST /api/auth/logout` removes the current authentication session/token.
- `GET /api/auth/me` lets the browser restore the authenticated user after a
  page reload.

### Game Setup and Recovery

- `GET /api/levels` provides values the browser may offer for difficulty and
  generator selection.
- `POST /api/games` creates the maze, player, timer, and active `GameSession`.
- `GET /api/games/{gameId}` is used to restore an active game after refresh or
  WebSocket reconnection. The server must reject requests from non-owners.

### Leaderboard

- `GET /api/leaderboard` returns persisted game results ordered by the
  server-calculated score.
- The API does not expose a client-controlled score-submission endpoint.
