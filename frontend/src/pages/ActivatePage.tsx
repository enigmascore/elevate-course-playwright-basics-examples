import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";

import { api } from "../api";

export default function ActivatePage() {
  const [params] = useSearchParams();
  const token = params.get("token") ?? "";
  const [state, setState] = useState<"working" | "done" | "failed">("working");

  useEffect(() => {
    let cancelled = false;
    api
      .activate(token)
      .then(() => !cancelled && setState("done"))
      .catch(() => !cancelled && setState("failed"));
    return () => {
      cancelled = true;
    };
  }, [token]);

  return (
    <main>
      {state === "working" && <h1>Activating your account...</h1>}
      {state === "done" && (
        <>
          <h1>Your account is activated</h1>
          <p>
            You can <Link to="/login">log in</Link> now.
          </p>
        </>
      )}
      {state === "failed" && (
        <>
          <h1>That activation link is not valid</h1>
          <p>It may have been used already. Try logging in, or register again.</p>
        </>
      )}
    </main>
  );
}
