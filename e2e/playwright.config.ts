import { defineConfig } from "@playwright/test";

// The Bookshelf Playwright suite. One SUITE = one directory under suites/ = one
// PROJECT below = one fresh backend ( support/fixtures.ts boots a backend
// container before the suite's first test and stops it after the last ).
//
// Knobs ( env vars, no code changes ):
//   HEADLESS=1   run headless ( the browser is VISIBLE by default; make test sets it )
//   SLOWMO=<ms>  slow every Playwright action down, e.g. SLOWMO=500 to watch
const headless = !!process.env.HEADLESS;
const slowMo = Number(process.env.SLOWMO ?? 0);

export default defineConfig({
  testDir: "./suites",
  outputDir: "./test-results",
  // Each suite's worker owns THE backend on port 8086, so suites must run one
  // after another - parallel workers would fight over it.
  workers: 1,
  fullyParallel: false,
  // The journeys are stateful ( register -> activate -> log in ): a retry
  // mid-suite would replay steps against data the failed attempt already
  // created. Fail honestly instead.
  retries: 0,
  reporter: [["list"], ["html", { outputFolder: "./reports", open: "never" }]],
  // a test that waits on a real email can legitimately spend a while polling;
  // slowed runs stretch every action, so give them a much larger budget
  timeout: slowMo > 0 ? 600_000 : 120_000,
  expect: { timeout: 10_000 },
  use: {
    baseURL: "http://localhost:5173",
    headless,
    launchOptions: { slowMo },
    // a trace on EVERY failure ( on-first-retry would never fire with retries: 0 )
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
  },
  projects: [
    { name: "fundamentals", testDir: "./suites/fundamentals" },
    { name: "isolation", testDir: "./suites/isolation" },
    // the setup project logs in once and saves the browser state to .auth/alice.json;
    // the storage-state suite then starts every test already logged in
    { name: "setup", testDir: "./suites/storage-state", testMatch: /auth\.setup\.ts/ },
    {
      name: "storage-state",
      testDir: "./suites/storage-state",
      testIgnore: /auth\.setup\.ts/,
      dependencies: ["setup"],
      use: { storageState: ".auth/alice.json" },
    },
    { name: "email", testDir: "./suites/email" },
    { name: "journey", testDir: "./suites/journey" },
    { name: "review", testDir: "./suites/review" },
    // Chromium only. To run in Firefox or WebKit as well, uncomment and run
    // `pnpm exec playwright install firefox webkit` once:
    // { name: "fundamentals-firefox", testDir: "./suites/fundamentals", use: { ...devices["Desktop Firefox"] } },
    // { name: "fundamentals-webkit", testDir: "./suites/fundamentals", use: { ...devices["Desktop Safari"] } },
  ],
  // ONE stateless Vite dev server serves every suite; only the backend restarts
  // per suite ( a webServer starts once per run, a fixture once per worker )
  webServer: {
    command: "pnpm dev",
    cwd: "../frontend",
    url: "http://localhost:5173",
    reuseExistingServer: true,
    timeout: 60_000,
  },
});
