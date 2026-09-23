import { expect, test } from "../../support/fixtures";

// Lesson 2 - locators are DESCRIPTIONS, resolved when an action or assertion
// needs them. The hierarchy: role, label, placeholder, text, test id, then CSS.
test("finding things the way a person does", async ({ loggedInPage: page }) => {
  // by ROLE and accessible name: what a screen reader would announce
  await expect(page.getByRole("heading", { name: "Books" })).toBeVisible();
  await expect(page.getByRole("link", { name: "Loans" })).toBeVisible();

  // by LABEL: the <label> a form field wears
  await expect(page.getByLabel("Search")).toBeEmpty();
  await expect(page.getByPlaceholder("Title or author")).toBeVisible();

  // a row is a role too; filter narrows a locator by the text inside it
  const hobbit = page.getByRole("row").filter({ hasText: "The Hobbit" });
  await expect(hobbit).toContainText("J. R. R. Tolkien");

  // the test-id CONTRACT: the app promises data-testid="book-row" on every book
  await expect(page.getByTestId("book-row")).toHaveCount(6);
});

test("a locator that matches several elements is STRICT", async ({ loggedInPage: page }) => {
  // six books, six Delete buttons: an action on this locator would throw
  // "strict mode violation: resolved to 6 elements"
  const deleteButtons = page.getByRole("button", { name: /^Delete / });
  await expect(deleteButtons).toHaveCount(6);

  // narrow it - by accessible name, by filtering, or ( last resort ) by position
  await expect(page.getByRole("button", { name: "Delete Dune" })).toBeVisible();
  await expect(deleteButtons.first()).toHaveAccessibleName("Delete A Brief History of Time");
});
