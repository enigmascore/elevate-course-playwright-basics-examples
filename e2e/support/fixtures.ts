import { type Page, test as base } from "@playwright/test";

import { login, USERS } from "./auth";
import { startBackend, stopBackend } from "./backend";
import { clearMessages } from "./mailhog";

// Every suite imports `test` and `expect` from HERE, never from @playwright/test,
// so the fixtures below are always in play.
//
// backend      WORKER scope, auto: one fresh backend per suite. A project never
//              shares a worker with another project, so the backend boots before
//              the suite's first test and stops after its last. Tests WITHIN a
//              suite share it: tolerate each other's data and use unique data.
// runId        WORKER scope: a suffix that makes this run's data unique.
// loggedInPage TEST scope: a page already logged in as Alice through the UI.
export const test = base.extend<{ loggedInPage: Page }, { backend: void; runId: string }>({
  backend: [
    async ({}, use) => {
      await startBackend();
      try {
        await clearMessages();
      } catch (error) {
        // a MailHog hiccup here would otherwise ORPHAN the just-started backend:
        // fixture setup failed, so the teardown below never runs and the stray
        // container blocks every later suite on port 8086
        await stopBackend();
        throw error;
      }
      await use();
      await stopBackend();
    },
    { scope: "worker", auto: true, timeout: 150_000 },
  ],

  runId: [
    async ({}, use) => {
      await use(String(Date.now()));
    },
    { scope: "worker" },
  ],

  loggedInPage: async ({ page }, use) => {
    await login(page, USERS.alice);
    await use(page);
  },
});

export { expect } from "@playwright/test";
