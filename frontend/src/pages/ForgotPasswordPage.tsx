import { type FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { api, ApiError } from "../api";
import FieldError from "../components/FieldError";

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string | undefined>();

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!email.trim()) {
      setError("Email is required");
      return;
    }
    try {
      await api.forgotPassword(email);
      void navigate("/check-email");
    } catch (e) {
      setError(e instanceof ApiError && e.errors ? e.errors.email : "Something went wrong - please try again");
    }
  }

  return (
    <main>
      <h1>Forgotten your password?</h1>
      <p>Tell us your email and we will send a link to choose a new one.</p>
      <form className="card" onSubmit={submit} noValidate>
        <div className="field">
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            aria-describedby="email-error"
          />
          <FieldError id="email-error" message={error} />
        </div>
        <button type="submit">Send reset link</button>
      </form>
      <p>
        <Link to="/login">Back to log in</Link>
      </p>
    </main>
  );
}
