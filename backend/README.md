# Escape the Maze Backend

This directory contains the Spring Boot backend being introduced alongside the
legacy JavaFX application. The JavaFX project at the repository root remains
unchanged while domain logic is migrated incrementally.

## Run locally

1. Start PostgreSQL:

   ```powershell
   docker compose up -d
   ```

2. Start the backend:

   ```powershell
   mvn spring-boot:run
   ```

The health endpoint is available at `GET /api/health`.

## Configuration

The application reads database settings from `DB_HOST`, `DB_PORT`, `DB_NAME`,
`DB_USERNAME`, and `DB_PASSWORD`. Development defaults match `compose.yaml`.

Do not use the example development password outside local development.
