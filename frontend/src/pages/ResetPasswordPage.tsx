import { type FormEvent, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";

import { api, ApiError } from "../api";
import FieldError from "../components/FieldError";

export default function ResetPasswordPage() {
  const [params] = useSearchParams();
  const token = params.get("token") ?? "";
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | undefined>();
  const [done, setDone] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (password.length < 8) {
      setError("Password must be at least 8 characters");
      return;
    }
    try {
      await api.resetPassword(token, password);
      setDone(true);
    } catch (e) {
      setError(
        e instanceof ApiError && e.errors
          ? e.errors.password
          : "That reset link is not valid - ask for a new one",
      );
    }
  }

  if (done) {
    return (
      <main>
        <h1>Password changed</h1>
        <p>
          <Link to="/login">Log in</Link> with your new password.
        </p>
      </main>
    );
  }

  return (
    <main>
      <h1>Choose a new password</h1>
      <form className="card" onSubmit={submit} noValidate>
        <div className="field">
          <label htmlFor="password">New password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            aria-describedby="password-error"
          />
          <FieldError id="password-error" message={error} />
        </div>
        <button type="submit">Set new password</button>
      </form>
    </main>
  );
}
