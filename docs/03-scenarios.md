# Scenarios

## SC-01 Win a Game

**Related use case:** [UC-05 Play a Game](02-use-cases.md#uc-05-play-a-game)

**Preconditions:** The user is authenticated, owns an active game, and the
game status is `RUNNING`.

1. The player sends a movement command toward the exit tile.
2. The server verifies that the target tile is walkable.
3. The server moves the player to the exit tile.
4. The server applies the exit tile rule.
5. The server adds the configured exit score and sets status to `WON`.
6. The server saves the final game result once, associated with the owner.
7. The server sends a `GAME_FINISHED` message with status `WON` and the final
   score.
8. The browser shows the victory result screen and offers play again or return
   to menu actions.

**Expected result:** The verified final score is eligible for the leaderboard;
the completed game rejects further movement commands.

## SC-02 Player Death

**Related use case:** [UC-05 Play a Game](02-use-cases.md#uc-05-play-a-game)

**Preconditions:** The user is authenticated, owns an active game, and the
player has health above zero.

1. The player moves onto a spikes tile.
2. The server validates the move and applies spike damage according to the
   configured rules.
3. The server checks the player's remaining health.
4. If health reaches zero, the server sets the game status to `LOST`.
5. The server saves the final game result once, associated with the owner.
6. The server sends a `GAME_FINISHED` message with status `LOST` and the final
   score.
7. The browser shows the death result screen.

**Expected result:** The game ends immediately after health reaches zero and
does not accept further movement commands.

## SC-03 Game Timer Expires

**Related use case:** [UC-05 Play a Game](02-use-cases.md#uc-05-play-a-game)

**Preconditions:** The user is authenticated, owns an active game, and the
game status is `RUNNING`.

1. The server reaches the game session's configured expiry time.
2. The server confirms that the game has not already finished.
3. The server sets the game status to `TIMED_OUT`.
4. The server saves the final game result once, associated with the owner.
5. The server sends a `GAME_FINISHED` message with status `TIMED_OUT` and the
   final score.
6. The browser shows the time-out result screen.

**Expected result:** The server, rather than the browser countdown, determines
whether the timer has expired. Further movement commands are rejected.

## SC-04 Reconnect to an Active Game

**Related use case:** [UC-06 Resume an Active Game](02-use-cases.md#uc-06-resume-an-active-game)

**Preconditions:** The user is authenticated and owns a game session with
status `RUNNING`.

1. The browser loses its WebSocket connection or the user refreshes the page.
2. The browser informs the user that the connection was interrupted.
3. The browser requests the current game state using the game identifier.
4. The server authenticates the user and verifies ownership of the game.
5. The server returns the authoritative game state, including timer, player,
   score, status, and active effects.
6. The browser re-establishes the WebSocket connection.
7. The browser redraws the game from the returned state and enables input.

**Alternative result:** If the session finished while disconnected, the server
returns its final status and the browser displays the appropriate result
screen instead of enabling movement.
