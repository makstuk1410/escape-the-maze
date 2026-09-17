# Functional Requirements

## Scope

This document defines the functional requirements for the browser-based
version of Escape the Maze. The MVP is a single-player maze game with
authenticated users, server-authoritative gameplay, saved results, and a
leaderboard.

## Accounts and Authentication

- **FR-01**: A visitor can register an account with a unique username, email,
  and password.
- **FR-02**: A registered user can log in and log out.
- **FR-03**: The application identifies the currently authenticated user.
- **FR-04**: Only authenticated users can create games, play games, and save
  leaderboard results.

## Game Creation

- **FR-05**: An authenticated user can choose a difficulty level before
  creating a game.
- **FR-06**: An authenticated user can choose a supported maze-generation
  algorithm before creating a game.
- **FR-07**: The application creates a new game session with a unique game
  identifier, generated maze, player, score, health, timer, and status.
- **FR-08**: A game session belongs to the user who created it.
- **FR-09**: A user can retrieve the current state of their own active game.

## Gameplay

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

- **FR-22**: The browser connects to the created game through a WebSocket.
- **FR-23**: The server sends the initial and updated authoritative game state
  to the connected browser.
- **FR-24**: A user can send commands only to a game session they own.
- **FR-25**: The browser displays the state received from the server,
  including player position, health, score, timer, status, and active effects.
- **FR-26**: If the WebSocket connection is interrupted, the browser informs
  the user and attempts to reconnect or lets the user resume the game state.

## Results and Leaderboard

- **FR-27**: The server saves a completed game result exactly once.
- **FR-28**: A saved result is associated with the authenticated user who owns
  the game session.
- **FR-29**: The saved result includes game identifier, difficulty, generator,
  score, final status, start time, and completion time.
- **FR-30**: A user can view a leaderboard of completed game results.
- **FR-31**: The leaderboard displays verified server-calculated scores, not
  scores supplied by the browser.

## User Interface

- **FR-32**: The application provides pages for registration, login, menu,
  difficulty/generator selection, gameplay, game result, and leaderboard.
- **FR-33**: The gameplay page renders the maze, player, visible hazards,
  score, health, remaining time, and game status in a browser canvas.
- **FR-34**: The result screen lets the user start another game or return to the menu.
