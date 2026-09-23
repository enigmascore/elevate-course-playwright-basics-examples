import { USERS, login } from "../../support/auth";
import { test as setup } from "../../support/fixtures";

// The SETUP project: log in once through the real form, then save the browser
// state ( cookies + localStorage, which holds Bookshelf's JWT ) to a file. The
// storage-state project depends on this project and starts from that file.
setup("log in as Alice and save the browser state", async ({ page }) => {
  await login(page, USERS.alice);

  await page.context().storageState({ path: ".auth/alice.json" });
});
