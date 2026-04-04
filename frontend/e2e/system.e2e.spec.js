const { test, expect } = require("@playwright/test");

function uniqueLogin(prefix) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100000)}`;
}

async function registerTeacher(page, login) {
  await page.goto("/register");
  await page.getByTestId("register-full-name").fill(`Teacher ${login}`);
  await page.getByTestId("register-login").fill(login);
  await page.getByTestId("register-password").fill("123456");
  await page.getByTestId("register-submit").click();
  await expect(page.getByTestId("register-success")).toBeVisible();
}

async function login(page, login, password) {
  await page.goto("/login");
  await page.getByTestId("login-input").fill(login);
  await page.getByTestId("password-input").fill(password);
  await page.getByTestId("login-submit").click();
}

test.describe("Teachers Preferences real system tests", () => {
  test("teacher can register, log in and open semester form", async ({ page }) => {
    const loginValue = uniqueLogin("teacher-system");

    await registerTeacher(page, loginValue);
    await login(page, loginValue, "123456");

    await expect(page).toHaveURL(/\/teacher$/);
    await page.getByTestId("teacher-semester-link").click();
    await expect(page).toHaveURL(/\/teacher\/semester$/);
    await expect(page.getByTestId("preference-0-subject")).toBeVisible();
  });

  test("teacher can save semester preferences and admin can export them", async ({ page }) => {
    const loginValue = uniqueLogin("teacher-semester");
    const subject = `Алгебра-${Date.now()}`;

    await registerTeacher(page, loginValue);
    await login(page, loginValue, "123456");
    await page.getByTestId("teacher-semester-link").click();
    await page.getByTestId("preference-0-subject").fill(subject);
    await page.getByTestId("preference-0-groups").fill("A-101");
    await page.getByTestId("preferences-save").click();
    await expect(page.getByTestId("preferences-success")).toBeVisible();

    await page.getByTestId("teacher-logout").click();
    await login(page, "admin", "admin1");

    await expect(page).toHaveURL(/\/admin$/);
    await expect(page.getByTestId("admin-semester-list")).toContainText(subject);

    const downloadPromise = page.waitForEvent("download");
    await page.getByTestId("admin-export-button").click();
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toBe("preferences.xlsx");
  });

  test("teacher can save session preferences and see persisted data after reload", async ({ page }) => {
    const loginValue = uniqueLogin("teacher-session");
    const subject = `Сессия-${Date.now()}`;

    await registerTeacher(page, loginValue);
    await login(page, loginValue, "123456");
    await page.getByTestId("teacher-session-link").click();
    await page.getByTestId("preference-0-subject").fill(subject);
    await page.getByTestId("preference-0-groups").fill("EX-202");
    await page.getByTestId("preferences-save").click();
    await expect(page.getByTestId("preferences-success")).toBeVisible();

    await page.reload();
    await expect(page.getByTestId("preference-0-subject")).toHaveValue(subject);
    await expect(page.getByTestId("preference-0-groups")).toHaveValue("EX-202");
  });

  test("invalid login is rejected in the real UI", async ({ page }) => {
    await login(page, "missing-user", "123456");
    await expect(page.getByTestId("login-error")).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });
});
