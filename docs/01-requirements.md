# Functional Requirements

## Scope

This document defines the functional requirements for the browser-based
version of Escape the Maze. The MVP is a single-player maze game with
authenticated users, server-authoritative gameplay, saved results, and a
leaderboard.

## Accounts and Authentication

**Related use cases:** [UC-01 Register an Account](02-use-cases.md#uc-01-register-an-account), [UC-02 Log In](02-use-cases.md#uc-02-log-in), and [UC-03 Log Out](02-use-cases.md#uc-03-log-out).

- **FR-01**: A visitor can register an account with a unique username, email,
  and password.
- **FR-02**: A registered user can log in and log out.
- **FR-03**: The application identifies the currently authenticated user.
- **FR-04**: Only authenticated users can create games, play games, and save
  leaderboard results.

## Game Creation

**Related use case:** [UC-04 Create a Game](02-use-cases.md#uc-04-create-a-game).

- **FR-05**: An authenticated user can choose a difficulty level before
  creating a game.
- **FR-06**: An authenticated user can choose a supported maze-generation
  algorithm before creating a game.
- **FR-07**: The application creates a new game session with a unique game
  identifier, generated maze, player, score, health, timer, and status.
- **FR-08**: A game session belongs to the user who created it.
- **FR-09**: A user can retrieve the current state of their own active game.

## Gameplay

**Related use case:** [UC-05 Play a Game](02-use-cases.md#uc-05-play-a-game).

- **FR-10**: The player can send movement commands in the four cardinal
  directions: up, down, left, and right.
- **FR-11**: The server validates every movement command before changing the game state.
- **FR-12**: The player cannot move through walls or outside the maze.
- **FR-13**: Moving onto an empty tile has no gameplay effect.
- **FR-14**: Moving onto a gold tile increases the score once; collected gold
  cannot award points again.
- **FR-15**: Moving onto a spikes tile damages the player according to the
  configured rules.
- **FR-16**: Moving onto a freeze tile applies the configured freeze effect.
- **FR-17**: Moving onto a fog tile applies the configured fog effect.
- **FR-18**: Moving onto the exit tile finishes the game with a `WON` status.
- **FR-19**: The game finishes with a `LOST` status when the player's health
  reaches zero.
- **FR-20**: The game finishes with a `TIMED_OUT` status when the game timer
  expires.
- **FR-21**: A finished game no longer accepts movement commands.

## Real-Time Communication

**Related use cases:** [UC-05 Play a Game](02-use-cases.md#uc-05-play-a-game) and [UC-06 Resume an Active Game](02-use-cases.md#uc-06-resume-an-active-game).

- **FR-22**: The browser connects to the created game through a WebSocket.
- **FR-23**: The server sends the initial and updated authoritative game state
  to the connected browser.
- **FR-24**: A user can send commands only to a game session they own.
- **FR-25**: The browser displays the state received from the server,
  including player position, health, score, timer, status, and active effects.
- **FR-26**: If the WebSocket connection is interrupted, the browser informs
  the user and attempts to reconnect or lets the user resume the game state.

## Results and Leaderboard

**Related use cases:** [UC-05 Play a Game](02-use-cases.md#uc-05-play-a-game) and [UC-07 View the Leaderboard](02-use-cases.md#uc-07-view-the-leaderboard).

- **FR-27**: The server saves a completed game result exactly once.
- **FR-28**: A saved result is associated with the authenticated user who owns
  the game session.
- **FR-29**: The saved result includes game identifier, difficulty, generator,
  score, final status, start time, and completion time.
- **FR-30**: A user can view a leaderboard of completed game results.
- **FR-31**: The leaderboard displays verified server-calculated scores, not
  scores supplied by the browser.

## User Interface

**Related use cases:** [UC-01 Register an Account](02-use-cases.md#uc-01-register-an-account), [UC-02 Log In](02-use-cases.md#uc-02-log-in), [UC-04 Create a Game](02-use-cases.md#uc-04-create-a-game), [UC-05 Play a Game](02-use-cases.md#uc-05-play-a-game), and [UC-07 View the Leaderboard](02-use-cases.md#uc-07-view-the-leaderboard).

- **FR-32**: The application provides pages for registration, login, menu,
  difficulty/generator selection, gameplay, game result, and leaderboard.
- **FR-33**: The gameplay page renders the maze, player, visible hazards,
  score, health, remaining time, and game status in a browser canvas.
- **FR-34**: The result screen lets the user start another game or return to the menu.

## Non-Functional Requirements

### Performance

- **NFR-01**: The application should load the initial frontend page within three seconds on a typical broadband connection.
- **NFR-02**: The backend should create a maze and return a new game session within two seconds for every supported difficulty.
- **NFR-03**: Under normal conditions, a valid movement command should receive a WebSocket state response within 500 milliseconds.
- **NFR-04**: The Canvas renderer should target 60 frames per second while drawing the visible maze viewport.

### Security

- **NFR-05**: Passwords must never be stored or logged in plain text; they must be stored using a secure one-way password hash.
- **NFR-06**: Protected REST endpoints and WebSocket connections must require an authenticated user identity.
- **NFR-07**: The server must authorize access to a game session by checking that its owner is the authenticated user.
- **NFR-08**: The browser must not be trusted to calculate or submit health, score, player position, game completion, or leaderboard results.
- **NFR-09**: Authentication tokens/cookies must be transmitted only over HTTPS in production and protected from client-side JavaScript where possible.

### Reliability and Data Integrity

- **NFR-10**: The server must save a completed game result at most once, even if duplicate completion events or WebSocket commands are received.
- **NFR-11**: A temporary WebSocket interruption must not change the authoritative game state or reset an active game session.
- **NFR-12**: The leaderboard must contain only results calculated and saved by the server.
- **NFR-13**: Invalid REST requests and WebSocket commands must return a safe, understandable error without stopping the application.

### Usability and Compatibility

- **NFR-14**: The application must be usable in current desktop versions of Chrome, Firefox, Edge, and Safari.
- **NFR-15**: Gameplay must support keyboard controls using WASD and arrow keys.
- **NFR-16**: The interface must clearly communicate loading, authentication, connection failure, victory, death, and timeout states.
- **NFR-17**: The game layout must remain usable at a browser viewport width of at least 1024 pixels.

### Maintainability and Testability

- **NFR-18**: Backend domain/gameplay code must not depend on JavaFX, browser, Canvas, or WebSocket framework classes.
- **NFR-19**: The domain layer must be covered by automated unit tests for movement, tile effects, game completion, and maze generation.
- **NFR-20**: REST and WebSocket contracts must be documented in this repository before the corresponding frontend integration is complete.
- **NFR-21**: Database schema changes must be versioned through migrations.
- **NFR-22**: Application configuration, secrets, and database credentials must be supplied through environment-specific configuration and must not be committed to the repository.
