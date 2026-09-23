import { expect, test } from "../../support/fixtures";
import { clearMessages, extractLink, waitForMessageTo } from "../../support/mailhog";

// Lesson 10 - waiting on something OUTSIDE the browser. Bookshelf's emails are
// sent by a scheduled sender a moment after the request, so the test clears the
// inbox first and then POLLS MailHog for the message.
test.describe.configure({ mode: "serial" });

test("register, activate via the emailed link, log in", async ({ page, runId }) => {
  const email = `carol.${runId}@example.com`;
  await clearMessages();

  await page.goto("/register");
  await page.getByLabel("Name").fill("Carol");
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill("turnthepage");
  await page.getByRole("button", { name: "Create account" }).click();
  await expect(page.getByRole("heading", { name: "Check your email" })).toBeVisible();

  // logging in before activation is refused - with a specific message
  await page.goto("/login");
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill("turnthepage");
  await page.getByRole("button", { name: "Log in" }).click();
  await expect(page.getByRole("alert")).toHaveText("Please activate your account first");

  // the email arrives asynchronously: poll, never assert immediately
  const message = await waitForMessageTo(email, { subjectContains: "Activate" });
  const link = extractLink(message, "/activate?token=");
  await page.goto(link);
  await expect(page.getByRole("heading", { name: "Your account is activated" })).toBeVisible();

  await page.getByRole("link", { name: "log in" }).click();
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill("turnthepage");
  await page.getByRole("button", { name: "Log in" }).click();
  await expect(page).toHaveURL(/\/books/);
  await expect(page.getByText("Carol")).toBeVisible();
});

test("a forgotten password is reset through the emailed link", async ({ page }) => {
  await clearMessages();

  await page.goto("/forgot-password");
  await page.getByLabel("Email").fill("bob@example.com");
  await page.getByRole("button", { name: "Send reset link" }).click();
  await expect(page.getByRole("heading", { name: "Check your email" })).toBeVisible();

  const message = await waitForMessageTo("bob@example.com", { subjectContains: "Reset" });
  await page.goto(extractLink(message, "/reset-password?token="));
  await page.getByLabel("New password").fill("secondchapter");
  await page.getByRole("button", { name: "Set new password" }).click();
  await expect(page.getByRole("heading", { name: "Password changed" })).toBeVisible();

  await page.getByRole("link", { name: "Log in" }).click();
  await page.getByLabel("Email").fill("bob@example.com");
  await page.getByLabel("Password").fill("secondchapter");
  await page.getByRole("button", { name: "Log in" }).click();
  await expect(page).toHaveURL(/\/books/);
  await expect(page.getByText("Bob")).toBeVisible();
});
