import { expect, test } from "../../support/fixtures";

// Lesson 5 - fixtures are how a test asks for what it needs. `page` is built in;
// `loggedInPage` and `runId` are ours ( support/fixtures.ts ). The `backend`
// fixture is auto: this suite already got its fresh backend before this file ran.
test("a bare page is not logged in", async ({ page }) => {
  await page.goto("/books");

  // no session in this fresh context, so the app sends us to the login screen
  await expect(page).toHaveURL(/\/login/);
});

test("loggedInPage arrives on the books page as Alice", async ({ loggedInPage: page }) => {
  await expect(page).toHaveURL(/\/books/);
  await expect(page.getByText("Alice")).toBeVisible();
});

test("runId makes this run's data unique", async ({ loggedInPage: page, runId }) => {
  const title = `Fixture book ${runId}`;

  await page.getByLabel("Title").fill(title);
  await page.getByLabel("Author").fill("A. Fixture");
  await page.getByLabel("Genre").selectOption("Science");
  await page.getByRole("button", { name: "Add book" }).click();

  await expect(page.getByRole("row").filter({ hasText: title })).toBeVisible();
});
