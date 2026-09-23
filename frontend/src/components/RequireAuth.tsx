import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";

import { readSession } from "../session";

// Every page but the auth screens sits behind this: no session, no page.
export default function RequireAuth({ children }: { children: ReactNode }) {
  if (!readSession()) return <Navigate to="/login" replace />;
  return children;
}
