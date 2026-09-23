import { expect, test } from "../../support/fixtures";

// Lesson 7 - isolation. This SUITE got a fresh backend: the books the
// fundamentals suite added are gone. WITHIN the suite the two tests below share
// one database, so each uses its own title and neither asserts a total count.
test("test A adds its own book", async ({ loggedInPage: page, runId }) => {
  const title = `Isolation A ${runId}`;

  await page.getByLabel("Title").fill(title);
  await page.getByLabel("Author").fill("Author A");
  await page.getByLabel("Genre").selectOption("History");
  await page.getByRole("button", { name: "Add book" }).click();

  await expect(page.getByRole("row").filter({ hasText: title })).toBeVisible();
  // a fresh backend: nothing from the earlier suites is here
  await expect(page.getByRole("row").filter({ hasText: "Fixture book" })).toHaveCount(0);
});

test("test B adds its own book and tolerates A's", async ({ loggedInPage: page, runId }) => {
  const title = `Isolation B ${runId}`;

  await page.getByLabel("Title").fill(title);
  await page.getByLabel("Author").fill("Author B");
  await page.getByLabel("Genre").selectOption("History");
  await page.getByRole("button", { name: "Add book" }).click();

  await expect(page.getByRole("row").filter({ hasText: title })).toBeVisible();
  // A's book MAY be here ( it is, if A ran first ) - so B never asserts "7 rows"
  await expect(page.getByTestId("book-row")).not.toHaveCount(0);
});
