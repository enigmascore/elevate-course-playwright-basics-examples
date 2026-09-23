import { NavLink, Outlet, useNavigate } from "react-router-dom";

import { clearSession, readSession } from "../session";

export default function Layout() {
  const navigate = useNavigate();
  const session = readSession();

  function logout() {
    // a NATIVE confirm - the dialogs lesson handles it with page.on("dialog")
    if (window.confirm("Log out of Bookshelf?")) {
      clearSession();
      void navigate("/login");
    }
  }

  return (
    <>
      <header className="site">
        <a className="brand" href="/books">
          Bookshelf
        </a>
        <nav aria-label="Main">
          <NavLink to="/books">Books</NavLink>
          <NavLink to="/loans">Loans</NavLink>
        </nav>
        <span>{session?.name}</span>
        <button type="button" onClick={logout}>
          Log out
        </button>
      </header>
      <main>
        <Outlet />
      </main>
    </>
  );
}
