import { expect, test } from "../../support/fixtures";

// Lesson 1 - the smallest real test: open a page, check what a person would see.
// `page` is a fixture: a fresh incognito-like browser context, made for THIS test.
test("the login page is up", async ({ page }) => {
  await page.goto("/login");

  await expect(page).toHaveTitle("Bookshelf");
  await expect(page.getByRole("heading", { name: "Log in to Bookshelf" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Log in" })).toBeEnabled();
});
