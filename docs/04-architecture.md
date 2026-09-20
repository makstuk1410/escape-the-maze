# Architecture

```mermaid
flowchart LR
    U[Authenticated user]

    subgraph B[Browser]
        P[Pages: login, menu, game, leaderboard]
        F[TypeScript application]
        C[Canvas renderer and input]
        P --> F --> C
    end

    subgraph S[Spring Boot backend]
        R[REST controllers]
        W[WebSocket handler]
        G[GameService]
        D[Domain: maze generators, rules, GameSession]
        A[Spring Security]
        R --> G
        W --> G
        G --> D
        A --> R
        A --> W
    end

    subgraph M[Server memory]
        GS[Active GameSession]
    end

    subgraph DB[PostgreSQL]
        US[(users)]
        GR[(game_results)]
    end

    U --> P
    F -->|HTTPS REST: register, login, create game, leaderboard| R
    F <-->|Authenticated WebSocket: MOVE and STATE| W
    G <--> GS
    R <--> US
    G -->|save finished result once| GR
    GR -->|verified leaderboard results| R
```

## Responsibilities

- **Browser:** displays Figma-based pages, captures input, and renders the
  authoritative game state in Canvas.
- **Spring Boot backend:** authenticates users, owns game rules, validates
  moves, and sends real-time state updates.
- **Server memory:** holds active `GameSession` objects while games are
  running; it is not used as permanent score storage.
- **PostgreSQL:** stores accounts and completed, server-calculated game
  results for the leaderboard.

## Trust Boundary

The browser is not trusted to decide movement validity, player health, score,
game status, or leaderboard results. Those decisions are made by `GameService`
after authentication and ownership checks.
