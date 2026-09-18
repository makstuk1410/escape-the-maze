# Escape the Maze Backend

This directory contains the Spring Boot backend being introduced alongside the
legacy JavaFX application. The JavaFX project at the repository root remains
unchanged while domain logic is migrated incrementally.

## Run the complete local stack

From this directory, run:

```powershell
docker compose up --build
```

This starts PostgreSQL, the Spring Boot backend, and the compiled frontend.
Open `http://localhost:5173` in a browser. The frontend proxies `/api` and
`/ws` requests to the backend inside the Docker network.

## Run services separately for development

Start PostgreSQL with `docker compose up -d postgres`, run the backend with
`$env:SPRING_PROFILES_ACTIVE="dev"; mvn spring-boot:run`, then run the frontend from `../frontend` with
`npm run dev`. The health endpoint is available at `GET /api/health`.

## Configuration

The application reads database settings from `DB_HOST`, `DB_PORT`, `DB_NAME`,
`DB_USERNAME`, and `DB_PASSWORD`. Development defaults match `compose.yaml`.

Do not use the example development password outside local development.

## Development seed accounts

The Docker Compose backend starts with the `dev` profile. On every startup it
ensures these accounts exist; it does not create duplicates. They are created
again automatically after the PostgreSQL volume is deleted and recreated.

| Username / email | Password |
|---|---|
| `maze_tester` / `tester@escape-the-maze.local` | `TestMaze123!` |
| `leaderboard_hero` / `hero@escape-the-maze.local` | `HeroMaze123!` |

These accounts and passwords are for local development only. The seeder is not
active in production.
