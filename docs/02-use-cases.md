# Use Cases

## UC-01 Register an Account

**Actor:** Visitor

**Preconditions:** The visitor is not authenticated.

**Main flow:**

1. The visitor opens the registration page.
2. The visitor provides a username, email, and password.
3. The system validates the submitted data.
4. The system creates the account and securely stores the password hash.
5. The system authenticates the new user and opens the menu page.

**Alternative flow:** If the username or email already exists, the system
explains the validation error and does not create an account.

## UC-02 Log In

**Actor:** Registered user

**Preconditions:** The user has an existing account.

**Main flow:**

1. The user opens the login page.
2. The user enters their email or username and password.
3. The system verifies the credentials.
4. The system creates an authenticated session.
5. The system opens the menu page.

**Alternative flow:** If the credentials are invalid, the system displays an
error and keeps the user unauthenticated.

## UC-03 Log Out

**Actor:** Authenticated user

**Preconditions:** The user is authenticated.

**Main flow:**

1. The user selects log out.
2. The system removes the authenticated session/token.
3. The system returns the user to the login or home page.

## UC-04 Create a Game

**Actor:** Authenticated user

**Preconditions:** The user is authenticated and has selected one of `EASY`,
`NORMAL`, `HARD`, or `EXPERT`, plus a supported maze generator.

**Main flow:**

1. The user selects one difficulty (`EASY`, `NORMAL`, `HARD`, or `EXPERT`)
   and a maze-generation algorithm.
2. The user starts a new game.
3. The system creates a game session owned by the user.
4. The system generates a maze, player state, timer, and initial game state.
5. The system returns the game identifier and initial state to the browser.
6. The browser opens the game page and connects to the game WebSocket.

## UC-05 Play a Game

**Actor:** Authenticated user

**Preconditions:** The user owns an active game session and has a connected
game page.

**Main flow:**

1. The user presses WASD or an arrow key.
2. The browser sends a movement command through the WebSocket.
3. The system verifies that the user owns the game and that the game is
   running.
4. The system validates the target maze cell.
5. If the target is walkable, the system moves the player and applies the tile
   effect.
6. The system checks for gold collection, damage, freeze, fog, death, timeout,
   or victory.
7. The system sends the authoritative updated state to the browser.
8. The browser renders the updated maze, player, HUD, and active effects.

**Alternative flows:**

- If the target cell is a wall or outside the maze, the system keeps the
  player's position unchanged and sends the current state.
- If the player reaches zero health, the system ends the game with `LOST`.
- If the player reaches the exit, the system ends the game with `WON`.
- If the timer expires, the system ends the game with `TIMED_OUT`.

## UC-06 Resume an Active Game

**Actor:** Authenticated user

**Preconditions:** The user owns an unfinished game session.

**Main flow:**

1. The user refreshes the page or reconnects after a connection interruption.
2. The browser requests the current state of the user's game session.
3. The system verifies ownership and returns the authoritative state.
4. The browser reconnects to the game WebSocket.
5. The browser renders the returned state and the user continues playing.

**Alternative flow:** If the game is already finished, the system returns its
final state and the browser shows the result screen.

## UC-07 View the Leaderboard

**Actor:** Visitor or authenticated user

**Preconditions:** At least one completed result has been saved.

**Main flow:**

1. The actor opens the leaderboard page.
2. The browser shows the `EASY`, `NORMAL`, `HARD`, and `EXPERT` leaderboard
   selectors.
3. The actor selects one difficulty leaderboard.
4. The browser requests leaderboard results for that difficulty only.
5. The system returns completed, server-verified game results that were played
   at the selected difficulty.
6. The browser displays usernames, scores, and final statuses for that
   difficulty.

**Alternative flow:** If the selected difficulty has no completed results,
the system returns an empty ranking and the browser shows an empty-state
message for that difficulty.
