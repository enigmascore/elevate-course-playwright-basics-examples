import { expect, test } from "../../support/fixtures";

// Review specimen B - the rewrite: the shipped login fixture, role and label
// locators, unique data, no sleeps, and an assertion about the OUTCOME.
test("adding a book puts it on the shelf", async ({ loggedInPage: page, runId }) => {
  const title = `Rewritten Book ${runId}`;

  await page.getByLabel("Title").fill(title);
  await page.getByLabel("Author").fill("Someone Careful");
  await page.getByLabel("Genre").selectOption("Fiction");
  await page.getByRole("button", { name: "Add book" }).click();

  const row = page.getByRole("row").filter({ hasText: title });
  await expect(row).toBeVisible();
  await expect(row).toContainText("Someone Careful");
  await expect(row).toContainText("Fiction");
});
