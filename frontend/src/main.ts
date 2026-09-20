import { ApiError } from "./api";
import { authState } from "./auth";
import "./styles/main.css";

const app = document.querySelector<HTMLDivElement>("#app");

if (!app) {
  throw new Error("Application root was not found.");
}

app.innerHTML = `
  <main class="auth-page">
    <section class="brand-panel" aria-labelledby="game-title">
      <div class="brand-copy">
        <p class="eyebrow">YOUR NEXT RUN AWAITS</p>
        <h1 id="game-title">ESCAPE<br>THE MAZE</h1>
        <span class="title-accent" aria-hidden="true"></span>
        <p class="brand-tagline">Create your account, start your run, and claim your place on the leaderboard.</p>
      </div>
    </section>

    <section class="form-panel" aria-labelledby="register-heading">
      <form class="register-card" id="register-form" novalidate>
        <h2 id="register-heading">Start your escape</h2>
        <p class="form-intro">Create an account to save your score.</p>

        <div class="form-fields">
          <label for="username">Display name</label>
          <input id="username" name="username" type="text" autocomplete="username" placeholder="Maze Runner" minlength="3" maxlength="50" required>

          <label for="email">Email</label>
          <input id="email" name="email" type="email" autocomplete="email" placeholder="you@example.com" maxlength="255" required>

          <label for="password">Password</label>
          <input id="password" name="password" type="password" autocomplete="new-password" placeholder="••••••••" minlength="8" maxlength="72" required>

          <label for="confirm-password">Confirm password</label>
          <input id="confirm-password" name="confirmPassword" type="password" autocomplete="new-password" placeholder="••••••••" minlength="8" maxlength="72" required>
        </div>

        <p class="form-message" id="form-message" role="status" aria-live="polite"></p>
        <button class="primary-button" type="submit">Create account</button>
        <p class="sign-in-row">Already have an account? <a href="#login">Sign in</a></p>
      </form>
    </section>
  </main>
`;

const form = document.querySelector<HTMLFormElement>("#register-form");
const message = document.querySelector<HTMLParagraphElement>("#form-message");
const submitButton = form?.querySelector<HTMLButtonElement>('button[type="submit"]');

if (!form || !message || !submitButton) {
  throw new Error("Register form could not be initialized.");
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  const data = new FormData(form);
  const username = String(data.get("username") ?? "").trim();
  const email = String(data.get("email") ?? "").trim();
  const password = String(data.get("password") ?? "");
  const confirmPassword = String(data.get("confirmPassword") ?? "");

  const validationError = validateRegistration(username, email, password, confirmPassword);
  if (validationError) {
    showMessage(validationError, true);
    return;
  }

  submitButton.disabled = true;
  submitButton.textContent = "Creating account…";
  showMessage("");

  try {
    await authState.register({ username, email, password });
    form.reset();
    showMessage("Account created. Sign in to start your first run.");
  } catch (error) {
    showMessage(error instanceof ApiError ? error.message : "We could not create your account. Please try again.", true);
  } finally {
    submitButton.disabled = false;
    submitButton.textContent = "Create account";
  }
});

function validateRegistration(username: string, email: string, password: string, confirmPassword: string): string | undefined {
  if (username.length < 3) return "Display name must contain at least 3 characters.";
  if (!email || !email.includes("@")) return "Enter a valid email address.";
  if (password.length < 8) return "Password must contain at least 8 characters.";
  if (password !== confirmPassword) return "Passwords do not match.";
  return undefined;
}

function showMessage(text: string, isError = false): void {
  message!.textContent = text;
  message!.classList.toggle("is-error", isError);
  message!.classList.toggle("is-visible", Boolean(text));
}
