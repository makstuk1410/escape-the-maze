import { authState } from "./auth";
import { GameplayPage } from "./game/GameplayPage";
import { renderLoginPage, renderRegisterPage } from "./views/authPages";
import { renderGameSetupPage } from "./views/gameSetupPage";
import { renderLeaderboardPage, renderMenuPage } from "./views/rankingsPages";
import "./styles/main.css";

const app = document.querySelector<HTMLDivElement>("#app");
if (!app) throw new Error("Application root was not found.");

let gameplayPage: GameplayPage | undefined;

window.addEventListener("hashchange", renderCurrentPage);
void authState.restore().then(renderCurrentPage);

function renderCurrentPage(): void {
  gameplayPage?.dispose();
  gameplayPage = undefined;

  switch (location.hash) {
    case "#menu": void renderMenuPage(app!); break;
    case "#game-setup": void renderGameSetupPage(app!); break;
    case "#gameplay":
      gameplayPage = new GameplayPage();
      void gameplayPage.render(app!);
      break;
    case "#leaderboard": void renderLeaderboardPage(app!); break;
    case "#register": renderRegisterPage(app!); break;
    default: renderLoginPage(app!);
  }
}
