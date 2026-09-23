import { expect, test } from "../../support/fixtures";

// Lesson 9 - storage state. playwright.config.ts gives this project
// `use: { storageState: ".auth/alice.json" }`, so every test here begins already
// logged in - no form, no click. The saved JWT is still valid against THIS
// suite's fresh backend because the signing secret is fixed and Alice is seeded.
test("the books page opens directly, no login step", async ({ page }) => {
  await page.goto("/books");

  await expect(page).toHaveURL(/\/books/);
  await expect(page.getByRole("heading", { name: "Books" })).toBeVisible();
  await expect(page.getByText("Alice")).toBeVisible();
});

test("the saved state lives in localStorage", async ({ page }) => {
  await page.goto("/books");

  const session = await page.evaluate(() => localStorage.getItem("bookshelf.session"));
  expect(session).not.toBeNull();
  expect(JSON.parse(session!)).toMatchObject({ email: "alice@example.com", name: "Alice" });
});
