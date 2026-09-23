import { type FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { api, ApiError, type FieldErrors } from "../api";
import FieldError from "../components/FieldError";

const EMAIL = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

// mirrors the backend's AuthValidator rules, so a bad form never leaves the browser
function validate(name: string, email: string, password: string): FieldErrors {
  const errors: FieldErrors = {};
  if (!name.trim()) errors.name = "Name is required";
  if (!email.trim()) errors.email = "Email is required";
  else if (!EMAIL.test(email.trim())) errors.email = "Email must look like name@example.com";
  if (!password) errors.password = "Password is required";
  else if (password.length < 8) errors.password = "Password must be at least 8 characters";
  return errors;
}

export default function RegisterPage() {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<FieldErrors>({});
  const [failure, setFailure] = useState<string | null>(null);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setFailure(null);
    const clientErrors = validate(name, email, password);
    setErrors(clientErrors);
    if (Object.keys(clientErrors).length > 0) return;

    try {
      await api.register(name, email, password);
      void navigate("/check-email");
    } catch (e) {
      if (e instanceof ApiError && e.errors) setErrors(e.errors);
      else setFailure(e instanceof ApiError ? e.message : "Something went wrong - please try again");
    }
  }

  return (
    <main>
      <h1>Create your account</h1>
      <form className="card" onSubmit={submit} noValidate>
        <div className="field">
          <label htmlFor="name">Name</label>
          <input
            id="name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            aria-describedby="name-error"
          />
          <FieldError id="name-error" message={errors.name} />
        </div>
        <div className="field">
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            aria-describedby="email-error"
          />
          <FieldError id="email-error" message={errors.email} />
        </div>
        <div className="field">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            aria-describedby="password-error"
          />
          <FieldError id="password-error" message={errors.password} />
        </div>
        {failure && (
          <p className="error" role="alert">
            {failure}
          </p>
        )}
        <button type="submit">Create account</button>
      </form>
      <p>
        Already registered? <Link to="/login">Log in</Link>
      </p>
    </main>
  );
}
