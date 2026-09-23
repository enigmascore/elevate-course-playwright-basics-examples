import { type Page } from "@playwright/test";

import { expect, test } from "../../support/fixtures";

// Lesson 11 - a deliberate SERIAL journey: add, find, delete. Serial mode runs
// the steps in order and skips the rest if one fails; the shared `title` is the
// thread through them. Most tests should NOT be like this - independence is the
// default - but a business flow told in steps reads well and fails clearly.
test.describe.configure({ mode: "serial" });

let page: Page;
let title: string;

test.beforeAll(async ({ browser, runId }) => {
  // one page for the whole journey ( the default is one per test )
  page = await browser.newPage();
  title = `Journey ${runId}`;
  await page.goto("/login");
  await page.getByLabel("Email").fill("alice@example.com");
  await page.getByLabel("Password").fill("bookworm");
  await page.getByRole("button", { name: "Log in" }).click();
  await expect(page).toHaveURL(/\/books/);
});

test.afterAll(async () => {
  await page.close();
});

test("step 1: add the book", async () => {
  await page.getByLabel("Title").fill(title);
  await page.getByLabel("Author").fill("Serial Writer");
  await page.getByLabel("Genre").selectOption("Non-fiction");
  await page.getByRole("button", { name: "Add book" }).click();

  await expect(page.getByText(`Added "${title}"`)).toBeVisible();
});

test("step 2: find it with the search", async () => {
  await page.getByLabel("Search").fill(title);

  await expect(page.getByTestId("book-row")).toHaveCount(1);
  await expect(page.getByTestId("book-row")).toContainText("Serial Writer");
});

test("step 3: delete it through the confirm modal", async () => {
  await page.getByRole("button", { name: `Delete ${title}` }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Delete" }).click();

  await expect(page.getByRole("dialog")).toBeHidden();
  await expect(page.getByTestId("book-row")).toHaveCount(0);
  await expect(page.getByText("No books match.")).toBeVisible();
});
