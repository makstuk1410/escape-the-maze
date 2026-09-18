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
`mvn spring-boot:run`, then run the frontend from `../frontend` with
`npm run dev`. The health endpoint is available at `GET /api/health`.

## Configuration

The application reads database settings from `DB_HOST`, `DB_PORT`, `DB_NAME`,
`DB_USERNAME`, and `DB_PASSWORD`. Development defaults match `compose.yaml`.

Do not use the example development password outside local development.
