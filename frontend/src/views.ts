export const plannedViews = [
  "Login",
  "Register",
  "Main menu",
  "Game setup",
  "Gameplay HUD",
  "Pause modal",
  "Game results",
  "Difficulty leaderboard"
] as const;

export type PlannedView = (typeof plannedViews)[number];
