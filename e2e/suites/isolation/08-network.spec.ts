import { expect, test } from "../../support/fixtures";

// Lesson 8 - the network. Mock a response, wait for a real one, and talk to the
// API directly with the `request` fixture.
test("page.route() answers the search request instead of the backend", async ({ loggedInPage: page }) => {
  await page.route("**/api/books?q=*", (route) =>
    route.fulfill({
      json: [{ id: 999, title: "A Mocked Book", author: "Route Fulfill", genre: "Fiction", alreadyRead: false, hasCover: false }],
    }),
  );

  await page.getByLabel("Search").fill("anything");

  await expect(page.getByTestId("book-row")).toHaveCount(1);
  await expect(page.getByTestId("book-row")).toContainText("A Mocked Book");
});

test("waitForResponse observes the real search request", async ({ loggedInPage: page }) => {
  const responsePromise = page.waitForResponse((response) => response.url().includes("/api/books?q=hobbit"));
  await page.getByLabel("Search").fill("hobbit");
  const response = await responsePromise;

  expect(response.status()).toBe(200);
  const books = (await response.json()) as { title: string }[];
  expect(books.map((book) => book.title)).toEqual(["The Hobbit"]);
});

test("the request fixture calls the API without a browser", async ({ request }) => {
  // log in over HTTP - the same endpoint the form posts to - and use the token
  const login = await request.post("http://localhost:8086/api/auth/login", {
    data: { email: "alice@example.com", password: "bookworm" },
  });
  expect(login.ok()).toBeTruthy();
  const { token } = (await login.json()) as { token: string };

  const books = await request.get("http://localhost:8086/api/books?q=dune", {
    headers: { Authorization: `Bearer ${token}` },
  });
  expect(books.status()).toBe(200);
  expect(await books.json()).toMatchObject([{ title: "Dune", author: "Frank Herbert" }]);
});
