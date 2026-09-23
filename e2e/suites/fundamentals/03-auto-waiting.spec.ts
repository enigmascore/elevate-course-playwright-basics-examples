import { expect, test } from "../../support/fixtures";

// Lesson 3 - auto-waiting. The loans page answers ~1.5 s late on purpose. There
// is NO sleep here: the assertion retries until the rows exist or its timeout
// ( expect.timeout in playwright.config.ts ) runs out.
test("the loans table appears when the slow backend answers", async ({ loggedInPage: page }) => {
  await page.getByRole("link", { name: "Loans" }).click();

  // first the loading state ...
  await expect(page.getByText("Loading loans...")).toBeVisible();

  // ... then the data. This line WAITS - it does not check once and fail.
  await expect(page.getByTestId("loan-row")).toHaveCount(4);
  await expect(page.getByRole("row").filter({ hasText: "The Hobbit" })).toContainText("Carol");
  await expect(page.getByText("Loading loans...")).toBeHidden();
});
