import { expect, test } from "../../support/fixtures";

// Review specimen A - this test PASSES, and it is still a bad test. The
// "Reviewing a test" page walks through why, line by line; 13-rewrite.spec.ts is
// the same intent done well.
test("add a book", async ({ page }) => {
  await page.goto("http://localhost:5173/login");
  await page.locator("#email").fill("alice@example.com");
  await page.locator("#password").fill("bookworm");
  await page.locator("form button").click();
  await page.waitForTimeout(1000);

  await page.locator("input#title").fill("Brittle Book");
  await page.locator("input#author").fill("Someone");
  await page.locator("select#genre").selectOption("Fiction");
  await page.locator("form.card button[type=submit]").click();
  await page.waitForTimeout(1000);

  const rows = await page.locator("tbody tr").count();
  expect(rows).toBeGreaterThan(0);
});
