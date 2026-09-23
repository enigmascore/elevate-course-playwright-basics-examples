// The logged-in session: the JWT plus who it belongs to, kept in localStorage
// so a reload stays logged in. The storage-state lesson saves exactly this key.
export const SESSION_KEY = "bookshelf.session";

export type Session = { token: string; name: string; email: string };

export function readSession(): Session | null {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? (JSON.parse(raw) as Session) : null;
  } catch {
    return null;
  }
}

export function saveSession(session: Session) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY);
}
