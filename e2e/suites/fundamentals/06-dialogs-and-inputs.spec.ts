import path from "node:path";

import { expect, test } from "../../support/fixtures";

const FIXTURES = path.resolve(import.meta.dirname, "../../fixtures");

// Lesson 6 - two kinds of dialog, and the rest of the form controls.
test("a NATIVE confirm is handled with page.on('dialog')", async ({ loggedInPage: page }) => {
  // dismiss first: we stay logged in
  page.once("dialog", (dialog) => {
    expect(dialog.type()).toBe("confirm");
    expect(dialog.message()).toBe("Log out of Bookshelf?");
    void dialog.dismiss();
  });
  await page.getByRole("button", { name: "Log out" }).click();
  await expect(page).toHaveURL(/\/books/);

  // accept: out we go
  page.once("dialog", (dialog) => void dialog.accept());
  await page.getByRole("button", { name: "Log out" }).click();
  await expect(page).toHaveURL(/\/login/);
});

test("a CUSTOM modal is just DOM with role=dialog", async ({ loggedInPage: page }) => {
  await page.getByRole("button", { name: "Delete Matilda" }).click();

  const dialog = page.getByRole("dialog");
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole("heading", { name: "Delete book?" })).toBeVisible();
  await expect(dialog).toContainText("Matilda");

  // Cancel keeps the book
  await dialog.getByRole("button", { name: "Cancel" }).click();
  await expect(dialog).toBeHidden();
  await expect(page.getByRole("row").filter({ hasText: "Matilda" })).toBeVisible();
});

test("selects, checkboxes and a file input", async ({ loggedInPage: page, runId }) => {
  const title = `Cover book ${runId}`;

  await page.getByLabel("Title").fill(title);
  await page.getByLabel("Author").fill("Ann Illustrator");
  await page.getByLabel("Genre").selectOption("Children");
  await page.getByLabel("Already read").check();
  await expect(page.getByLabel("Already read")).toBeChecked();
  // setInputFiles points a file input at a file on disk - no OS dialog opens
  await page.getByLabel("Cover image").setInputFiles(path.join(FIXTURES, "cover.png"));
  await page.getByRole("button", { name: "Add book" }).click();

  const row = page.getByRole("row").filter({ hasText: title });
  await expect(row).toContainText("Yes");
  await expect(row.getByRole("img", { name: `Cover of ${title}` })).toBeVisible();
});

test("the cover must be an image", async ({ loggedInPage: page }) => {
  await page.getByLabel("Title").fill("No cover for you");
  await page.getByLabel("Author").fill("Someone");
  await page.getByLabel("Genre").selectOption("Fiction");
  await page.getByLabel("Cover image").setInputFiles(path.join(FIXTURES, "notes.txt"));
  await page.getByRole("button", { name: "Add book" }).click();

  await expect(page.getByText("Cover must be a PNG or JPEG image")).toBeVisible();
  await expect(page.getByRole("row").filter({ hasText: "No cover for you" })).toHaveCount(0);
});
