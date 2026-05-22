const { test, expect } = require("@playwright/test");
const {
  TEACHER_PASSWORD,
  uniqueLogin
} = require("./support/systemHelpers");

test("SYS-06 invalid login through UI", async ({ page }) => {
  await page.goto("/login");
  await page.getByTestId("login-input").fill(uniqueLogin("missing-sys06"));
  await page.getByTestId("password-input").fill(TEACHER_PASSWORD);

  const responsePromise = page.waitForResponse(
    response => response.url().includes("/api/auth/login") && response.request().method() === "POST"
  );
  await page.getByTestId("login-submit").click();

  expect((await responsePromise).status()).toBe(401);
  await expect(page.getByTestId("login-error")).toBeVisible();
  await expect(page).toHaveURL(/\/login$/);
});
