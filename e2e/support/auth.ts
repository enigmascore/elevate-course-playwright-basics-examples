import { expect, type Page } from "@playwright/test";

// The two users seed.sql creates, already activated.
export const USERS = {
  alice: { email: "alice@example.com", password: "bookworm", name: "Alice" },
  bob: { email: "bob@example.com", password: "pageturner", name: "Bob" },
};

export type User = (typeof USERS)[keyof typeof USERS];

// Log in through the real form and wait until the books page has arrived - a
// test that navigates straight after the click would cancel the redirect.
export async function login(page: Page, user: User): Promise<void> {
  await page.goto("/login");
  await page.getByLabel("Email").fill(user.email);
  await page.getByLabel("Password").fill(user.password);
  await page.getByRole("button", { name: "Log in" }).click();
  await expect(page).toHaveURL(/\/books/);
}
