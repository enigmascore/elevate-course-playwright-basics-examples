import { type FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { api, ApiError } from "../api";
import { saveSession } from "../session";

const MESSAGES: Record<string, string> = {
  WRONG_CREDENTIALS: "Wrong email or password",
  NOT_ACTIVATED: "Please activate your account first",
};

export default function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const session = await api.login(email, password);
      saveSession(session);
      void navigate("/books");
    } catch (e) {
      const code = e instanceof ApiError ? e.code : undefined;
      setError((code && MESSAGES[code]) ?? "Something went wrong - please try again");
    }
  }

  return (
    <main>
      <h1>Log in to Bookshelf</h1>
      <form className="card" onSubmit={submit} noValidate>
        <div className="field">
          <label htmlFor="email">Email</label>
          <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>
        {error && (
          <p className="error" role="alert">
            {error}
          </p>
        )}
        <button type="submit">Log in</button>
      </form>
      <p>
        New here? <Link to="/register">Create an account</Link>
      </p>
      <p>
        <Link to="/forgot-password">Forgotten your password?</Link>
      </p>
    </main>
  );
}
