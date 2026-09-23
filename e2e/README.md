# The Bookshelf Playwright suite

End-to-end tests that drive the REAL system: the front end on 5173, the
backend container on 8086, Postgres and MailHog in docker. The course pages
excerpt these files; this README is the map.

## Layout

```
playwright.config.ts    projects ( one per suite ), ports, HEADLESS / SLOWMO knobs
support/backend.ts      start / stop / readiness of the backend CONTAINER
support/fixtures.ts     the worker-scoped `backend` fixture ( auto ), `runId`, `loggedInPage`
support/auth.ts         the two seeded users and a login() helper
support/mailhog.ts      clear the inbox, poll for a message, pull a link out of it
suites/<name>/          one SUITE = one Playwright project = one fresh backend
fixtures/               files tests upload ( cover.png, notes.txt )
reports/, test-results/ output ( gitignored ); test-results/backend.log is the backend's output
```

## Quick start

From the repository root:

```
make docker-up    # once per session: Postgres + MailHog, build the backend image
make test         # all suites, headless
make test-headed  # watch it ( SLOWMO=500 make test-headed to slow every action )
make report       # the HTML report of the last run
```

One suite only, from this folder:

```
pnpm exec playwright test --project=fundamentals
```

## The rules that keep suites honest

- **Each suite gets a FRESH backend.** The `backend` fixture in
  `support/fixtures.ts` starts the backend container before a suite's first
  test and stops it after the last. Starting the backend rebuilds its
  database from `seed.sql`, so a suite can never see another suite's data.
- **Tests within a suite share that backend.** Tolerate each other's data
  and make your own unique: put `runId` in every title or email you create.
  Never assert a total count of rows you did not create.
- **Emails are asynchronous.** Clear the inbox first ( `clearMessages()` ),
  then `waitForMessageTo(...)` - never assert the moment the form submits.
- **Locators: role and label first.** `getByRole`, `getByLabel`, then
  `getByTestId` where the app promises one ( `book-row`, `loan-row` ). CSS is
  the last resort, never the first.
- **No `waitForTimeout`.** Web-first assertions wait for you.
- **Import `test` and `expect` from `../../support/fixtures`**, never from
  `@playwright/test` - or the backend fixture is not in play.

## Adding a suite

See [docs/creating_a_new_suite.md](docs/creating_a_new_suite.md).

## Port map

| Port | What                                         |
| ---- | -------------------------------------------- |
| 5173 | the front end ( Vite dev server, `baseURL` ) |
| 8086 | the backend container                         |
| 1027 | MailHog SMTP ( the backend sends here )       |
| 8027 | MailHog HTTP: inbox UI and the API tests poll |
| 5435 | Postgres                                      |
