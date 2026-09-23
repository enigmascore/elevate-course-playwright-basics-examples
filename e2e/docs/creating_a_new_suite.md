# Creating a new Playwright suite

A SUITE is a directory under `suites/<name>/` mapped to one Playwright
PROJECT in `playwright.config.ts`. Each suite's worker starts its OWN
backend container: a fresh database holding exactly `seed.sql`.

## Steps

1. Create `suites/<name>/<name>.spec.ts`.
2. Register a project for it in `playwright.config.ts`:

   ```ts
   { name: "<name>", testDir: "./suites/<name>" },
   ```

3. Import `test` and `expect` from the fixtures, NOT from `@playwright/test`:

   ```ts
   import { expect, test } from "../../support/fixtures";
   ```

   The worker-scoped `backend` fixture is `auto`: it starts the backend
   container before your first test, waits until `/api/health` answers,
   clears MailHog, and stops the container after your last test. You write
   no lifecycle code.

## What the fixtures give you

- **`page`** - a fresh browser context per test, NOT logged in. Visiting a
  protected page sends it to `/login`.
- **`loggedInPage`** - a page already logged in as Alice through the real
  form, sitting on `/books`.
- **`runId`** - a string unique to this run. Put it in every title, name or
  email your test creates.
- The backend on `http://localhost:8086`, seeded with `seed.sql` only:
  Alice ( `bookworm` ) and Bob ( `pageturner` ), six books, four loans.
- The front end on `http://localhost:5173` ( `baseURL`, so
  `page.goto("/books")` works ). ONE front end serves every suite - it is
  stateless; only the backend restarts per suite.

## Rules

- Tests within a suite SHARE the backend: tolerate each other's data and
  use unique data per test ( `runId` ).
- Emails are asynchronous. Always clear, act, then poll:

  ```ts
  import { clearMessages, extractLink, waitForMessageTo } from "../../support/mailhog";

  await clearMessages();
  // ... do the thing that sends the email ...
  const message = await waitForMessageTo(email, { subjectContains: "Activate" });
  await page.goto(extractLink(message, "/activate?token="));
  ```

- Locators: `getByRole` / `getByLabel` first; `getByTestId` where the app
  promises an id; CSS last. Never select on a class name.
- No `page.waitForTimeout()`. Assert with `expect(locator)...` and let it wait.
- Assert OUTCOMES ( the row is there, the URL changed, the message reads X ),
  not "nothing threw".

## Running just your suite

```
pnpm exec playwright test --project=<name>
pnpm exec playwright test --project=<name> --headed
```

Postgres and MailHog must be up and the backend image built: `make docker-up`
from the repository root does both.
