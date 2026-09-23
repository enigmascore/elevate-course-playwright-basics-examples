import { expect, test } from "../../support/fixtures";

// Lesson 4 - web-first assertions RETRY until they pass or time out;
// expect( value ) does not. Prefer the former for anything the browser shows.
test("assertions about what the page shows", async ({ loggedInPage: page }) => {
  await expect(page).toHaveURL(/\/books$/);
  await expect(page.getByRole("heading", { name: "Books" })).toHaveText("Books");

  await page.getByLabel("Search").fill("dune");
  await expect(page.getByLabel("Search")).toHaveValue("dune");
  // the server answers the search; the table settles at one row
  await expect(page.getByTestId("book-row")).toHaveCount(1);
  await expect(page.getByTestId("book-row")).toContainText("Frank Herbert");

  // negation: waits for the condition to become FALSE
  await expect(page.getByRole("row").filter({ hasText: "The Hobbit" })).toHaveCount(0);
  await expect(page.getByText("No books match.")).toBeHidden();
});

test("soft assertions collect several failures instead of stopping at the first", async ({
  loggedInPage: page,
}) => {
  await page.getByLabel("Search").fill("zzz-nothing-matches");

  // both run even if the first fails; the test is marked failed at the end
  await expect.soft(page.getByTestId("book-row")).toHaveCount(0);
  await expect.soft(page.getByText("No books match.")).toBeVisible();

  // a plain expect is fine for a value you already hold
  const rows = await page.getByTestId("book-row").count();
  expect(rows).toBe(0);
});
