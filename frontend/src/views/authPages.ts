import { ApiError } from "../api";
import { authState } from "../auth";
import { brandPanel, setMessage } from "./shared";

export function renderLoginPage(app: HTMLDivElement): void {
  app.innerHTML = `
    <main class="auth-page">
      ${brandPanel("THE ESCAPE BEGINS", "Enter the maze. Beat the clock. Own the leaderboard.")}
      <section class="form-panel" aria-labelledby="login-heading">
        <form class="auth-card login-card" id="login-form" novalidate>
          <h2 id="login-heading">Welcome back</h2>
          <p class="form-intro">Sign in to continue your escape.</p>
          <div class="form-fields login-fields">
            <label for="identifier">Email or username</label>
            <input id="identifier" name="identifier" type="text" autocomplete="username" placeholder="you@example.com" required>
            <label for="password">Password</label>
            <input id="password" name="password" type="password" autocomplete="current-password" placeholder="••••••••" required>
          </div>
          <p class="recovery-row"><a href="#forgot-password">Forgot password?</a></p>
          <p class="form-message" id="form-message" role="status" aria-live="polite"></p>
          <button class="primary-button" type="submit">Sign in</button>
          <p class="sign-in-row">New here? <a href="#register">Create account</a></p>
        </form>
      </section>
    </main>
  `;

  const form = app.querySelector<HTMLFormElement>("#login-form");
  const message = app.querySelector<HTMLParagraphElement>("#form-message");
  const submitButton = form?.querySelector<HTMLButtonElement>('button[type="submit"]');
  if (!form || !message || !submitButton) throw new Error("Login form could not be initialized.");

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const data = new FormData(form);
    const identifier = String(data.get("identifier") ?? "").trim();
    const password = String(data.get("password") ?? "");
    if (!identifier || !password) {
      setMessage(message, "Enter your email or username and password.", true);
      return;
    }

    submitButton.disabled = true;
    submitButton.textContent = "Signing in…";
    setMessage(message, "");
    try {
      const snapshot = await authState.login({ identifier, password });
      if (snapshot.status !== "authenticated") {
        setMessage(message, snapshot.error ?? "We could not sign you in. Please try again.", true);
        return;
      }
      window.location.hash = "#menu";
    } catch (error) {
      setMessage(message, error instanceof ApiError ? error.message : "We could not sign you in. Please try again.", true);
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = "Sign in";
    }
  });
}

export function renderRegisterPage(app: HTMLDivElement): void {
  app.innerHTML = `
    <main class="auth-page">
      ${brandPanel("YOUR NEXT RUN AWAITS", "Create your account, start your run, and claim your place on the leaderboard.")}
      <section class="form-panel" aria-labelledby="register-heading">
        <form class="auth-card register-card" id="register-form" novalidate>
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

  const form = app.querySelector<HTMLFormElement>("#register-form");
  const message = app.querySelector<HTMLParagraphElement>("#form-message");
  const submitButton = form?.querySelector<HTMLButtonElement>('button[type="submit"]');
  if (!form || !message || !submitButton) throw new Error("Register form could not be initialized.");

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const data = new FormData(form);
    const username = String(data.get("username") ?? "").trim();
    const email = String(data.get("email") ?? "").trim();
    const password = String(data.get("password") ?? "");
    const confirmPassword = String(data.get("confirmPassword") ?? "");
    const validationError = validateRegistration(username, email, password, confirmPassword);
    if (validationError) {
      setMessage(message, validationError, true);
      return;
    }

    submitButton.disabled = true;
    submitButton.textContent = "Creating account…";
    setMessage(message, "");
    try {
      await authState.register({ username, email, password });
      form.reset();
      setMessage(message, "Account created. Sign in to start your first run.");
    } catch (error) {
      setMessage(message, error instanceof ApiError ? error.message : "We could not create your account. Please try again.", true);
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = "Create account";
    }
  });
}

function validateRegistration(username: string, email: string, password: string, confirmPassword: string): string | undefined {
  if (username.length < 3) return "Display name must contain at least 3 characters.";
  if (!email || !email.includes("@")) return "Enter a valid email address.";
  if (password.length < 8) return "Password must contain at least 8 characters.";
  if (password !== confirmPassword) return "Passwords do not match.";
  return undefined;
}
