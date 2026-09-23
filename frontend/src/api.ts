import { clearSession, readSession } from "./session";

// One thin fetch wrapper: relative /api urls ( Vite proxies them to the
// backend ), the Bearer token when there is one, and typed errors.

export type FieldErrors = Record<string, string>;

export class ApiError extends Error {
  status: number;
  code?: string;
  errors?: FieldErrors;

  constructor(status: number, message: string, code?: string, errors?: FieldErrors) {
    super(message);
    this.status = status;
    this.code = code;
    this.errors = errors;
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  const session = readSession();
  if (session) headers.set("Authorization", `Bearer ${session.token}`);

  const response = await fetch(path, { ...init, headers });

  if (response.status === 401 && session) {
    // the token is stale or forged: forget it and start again at /login
    clearSession();
    window.location.assign("/login");
  }

  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as {
      message?: string;
      code?: string;
      errors?: FieldErrors;
    };
    throw new ApiError(response.status, body.message ?? response.statusText, body.code, body.errors);
  }

  if (response.status === 204 || response.status === 202) return undefined as T;
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

function json(body: unknown, method = "POST"): RequestInit {
  return { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) };
}

export type Book = {
  id: number;
  title: string;
  author: string;
  genre: string;
  alreadyRead: boolean;
  hasCover: boolean;
};

export type Loan = { id: number; bookTitle: string; borrower: string; dueDate: string };

export type LoginResponse = { token: string; name: string; email: string };

export const api = {
  login: (email: string, password: string) =>
    request<LoginResponse>("/api/auth/login", json({ email, password })),
  register: (name: string, email: string, password: string) =>
    request<void>("/api/auth/register", json({ name, email, password })),
  activate: (token: string) => request<void>("/api/auth/activate", json({ token })),
  forgotPassword: (email: string) => request<void>("/api/auth/forgot-password", json({ email })),
  resetPassword: (token: string, password: string) =>
    request<void>("/api/auth/reset-password", json({ token, password })),

  books: (q: string) => request<Book[]>(`/api/books${q ? `?q=${encodeURIComponent(q)}` : ""}`),
  addBook: (form: FormData) => request<Book>("/api/books", { method: "POST", body: form }),
  deleteBook: (id: number) => request<void>(`/api/books/${id}`, { method: "DELETE" }),

  loans: () => request<Loan[]>("/api/loans"),
};
