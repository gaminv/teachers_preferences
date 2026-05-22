const fs = require("node:fs");
const XLSX = require("xlsx");
const { test, expect } = require("@playwright/test");

const TEACHER_PASSWORD = "123456";

function uniqueLogin(prefix) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100000)}`;
}

async function expectOkResponse(response) {
  expect(response.ok(), `${response.request().method()} ${response.url()} returned ${response.status()}`).toBeTruthy();
}

async function login(page, loginValue, password, expectedUrl) {
  await page.goto("/login");
  await page.getByTestId("login-input").fill(loginValue);
  await page.getByTestId("password-input").fill(password);

  const responsePromise = page.waitForResponse(
    response => response.url().includes("/api/auth/login") && response.request().method() === "POST"
  );
  await page.getByTestId("login-submit").click();
  await expectOkResponse(await responsePromise);
  await expect(page).toHaveURL(expectedUrl);
}

async function attachScreenshot(page, name) {
  await test.info().attach(name, {
    body: await page.screenshot({ fullPage: true }),
    contentType: "image/png"
  });
}

async function fillCommonPreferenceFields(page, entryIndex, values) {
  await page.getByTestId(`preference-${entryIndex}-building-room`).fill(values.roomSearch);
  await page.getByTestId(`preference-${entryIndex}-room-option-0`).click();
  await expect(page.getByTestId(`preference-${entryIndex}-building-room`)).toHaveValue(values.roomValue);
  await page.getByTestId(`preference-${entryIndex}-building-room-priority`).selectOption(values.roomPriority);

  for (const os of values.computers) {
    await page.getByTestId(`preference-${entryIndex}-computer-${os}`).check();
  }
  await page.getByTestId(`preference-${entryIndex}-computers-priority`).selectOption(values.computersPriority);

  await page.getByTestId(`preference-${entryIndex}-load-type`).selectOption(values.loadType);
  await page.getByTestId(`preference-${entryIndex}-load-type-priority`).selectOption(values.loadTypePriority);
  await page.getByTestId(`preference-${entryIndex}-board-type`).selectOption(values.boardType);
  await page.getByTestId(`preference-${entryIndex}-board-type-priority`).selectOption(values.boardTypePriority);
  await page.getByTestId(`preference-${entryIndex}-format`).selectOption(values.format);
  await page.getByTestId(`preference-${entryIndex}-format-priority`).selectOption(values.formatPriority);
  await page.getByTestId(`preference-${entryIndex}-comments`).fill(values.comments);
  await page.getByTestId(`preference-${entryIndex}-comments-priority`).selectOption(values.commentsPriority);
}

async function savePreferences(page, expectedCount) {
  const responsePromise = page.waitForResponse(
    response => response.url().includes("/api/teacher/preferences") && response.request().method() === "POST"
  );
  await page.getByTestId("preferences-save").click();
  const response = await responsePromise;
  await expectOkResponse(response);
  expect(await response.json()).toHaveLength(expectedCount);
  await expect(page.getByTestId("preferences-success")).toBeVisible();
}

test.describe("Teachers Preferences production-like system workflow", () => {
  test("admin and teacher complete a real end-to-end preference workflow with Excel export evidence", async ({ page }) => {
    test.setTimeout(120_000);

    const teacherLogin = uniqueLogin("e2e-teacher");
    const teacherFullName = `E2E Teacher ${teacherLogin}`;
    const semesterLabel = `Semester workflow ${teacherLogin}`;
    const sessionLabel = `Session workflow ${teacherLogin}`;
    const semesterSubject = `Algorithms E2E ${teacherLogin}`;
    const copiedSemesterSubject = `Algorithms Lab E2E ${teacherLogin}`;
    const sessionSubject = `Exam E2E ${teacherLogin}`;

    await test.step("admin configures the teacher dashboard and creates a teacher", async () => {
      await page.goto("/login");
      await page.evaluate(() => localStorage.clear());
      await login(page, "admin", "admin1", /\/admin$/);
      await expect(page.getByTestId("admin-register-full-name")).toBeVisible();

      await page.getByTestId("admin-semester-button-text").fill(semesterLabel);
      await page.getByTestId("admin-semester-deadline").fill("2099-12-31");
      await page.getByTestId("admin-session-button-text").fill(sessionLabel);
      await page.getByTestId("admin-session-deadline").fill("2099-12-31");

      page.once("dialog", dialog => dialog.accept());
      await page.getByTestId("admin-config-save").click();

      await page.getByTestId("admin-register-full-name").fill(teacherFullName);
      await page.getByTestId("admin-register-login").fill(teacherLogin);
      await page.getByTestId("admin-register-password").fill(TEACHER_PASSWORD);

      const registerResponse = page.waitForResponse(
        response => response.url().includes("/api/auth/register") && response.request().method() === "POST"
      );
      await page.getByTestId("admin-register-submit").click();
      await expectOkResponse(await registerResponse);
      await expect(page.getByTestId("admin-register-success")).toContainText(teacherLogin);
      await attachScreenshot(page, "01-admin-created-teacher");

      await page.getByTestId("admin-logout").click();
      await expect(page).toHaveURL(/\/login$/);
    });

    await test.step("teacher fills semester preferences using every semester and common control", async () => {
      await login(page, teacherLogin, TEACHER_PASSWORD, /\/teacher$/);
      await expect(page.getByTestId("teacher-semester-link")).toContainText(semesterLabel);
      await expect(page.getByTestId("teacher-session-link")).toContainText(sessionLabel);

      await page.getByTestId("teacher-semester-link").click();
      await expect(page).toHaveURL(/\/teacher\/semester$/);

      await page.getByTestId("preference-0-subject").fill(semesterSubject);
      await page.getByTestId("preference-0-groups").fill("A-101, A-102");
      await page.getByTestId("preference-0-day-0").check();
      await page.getByTestId("preference-0-day-2").check();
      await page.getByTestId("preference-0-days-priority").selectOption("5");
      await page.getByTestId("preference-0-times").fill("09:00-12:00");
      await page.getByTestId("preference-0-times-priority").selectOption("4");

      await fillCommonPreferenceFields(page, 0, {
        roomSearch: "3/101",
        roomValue: "3/101",
        roomPriority: "3",
        computers: ["windows"],
        computersPriority: "2",
        loadType: "compact",
        loadTypePriority: "1",
        boardType: "digital",
        boardTypePriority: "5",
        format: "in-person",
        formatPriority: "4",
        comments: "Semester e2e comment",
        commentsPriority: "3"
      });

      await page.getByTestId("preferences-add-entry").click();
      await expect(page.getByTestId("preference-1-subject")).toBeVisible();
      await page.getByTestId("preference-1-copy-prev").click();
      await expect(page.getByTestId("preference-1-subject")).toHaveValue(semesterSubject);
      await page.getByTestId("preference-1-subject").fill(copiedSemesterSubject);
      await page.getByTestId("preference-1-groups").fill("LAB-201");

      await page.getByTestId("preferences-add-entry").click();
      await expect(page.getByTestId("preference-2-subject")).toBeVisible();
      await page.getByTestId("preference-2-remove").click();
      await expect(page.getByTestId("preference-2-subject")).toHaveCount(0);

      await savePreferences(page, 2);
      await page.reload();
      await expect(page.getByTestId("preference-0-subject")).toHaveValue(semesterSubject);
      await expect(page.getByTestId("preference-1-subject")).toHaveValue(copiedSemesterSubject);
      await attachScreenshot(page, "02-teacher-semester-persisted");
    });

    await test.step("teacher fills session preferences and verifies persistence after reload", async () => {
      await page.goto("/teacher");
      await page.getByTestId("teacher-session-link").click();
      await expect(page).toHaveURL(/\/teacher\/session$/);

      await page.getByTestId("preference-0-subject").fill(sessionSubject);
      await page.getByTestId("preference-0-groups").fill("EX-202");
      await page.getByTestId("preference-0-preferred-dates").fill("10.01, 12.01");
      await page.getByTestId("preference-0-preferred-dates-priority").selectOption("5");
      await page.getByTestId("preference-0-avoid-dates").fill("15.01");
      await page.getByTestId("preference-0-avoid-dates-priority").selectOption("4");
      await page.getByTestId("preference-0-new-year-pref").fill("After New Year");
      await page.getByTestId("preference-0-new-year-pref-priority").selectOption("3");

      await fillCommonPreferenceFields(page, 0, {
        roomSearch: "11/321",
        roomValue: "11/321",
        roomPriority: "5",
        computers: ["linux"],
        computersPriority: "4",
        loadType: "even",
        loadTypePriority: "2",
        boardType: "marker",
        boardTypePriority: "1",
        format: "remote",
        formatPriority: "5",
        comments: "Session e2e comment",
        commentsPriority: "2"
      });

      await savePreferences(page, 1);
      await page.reload();
      await expect(page.getByTestId("preference-0-subject")).toHaveValue(sessionSubject);
      await expect(page.getByTestId("preference-0-preferred-dates")).toHaveValue("10.01, 12.01");
      await attachScreenshot(page, "03-teacher-session-persisted");
    });

    await test.step("admin sees saved data and exports the same workflow to Excel", async () => {
      await page.goto("/teacher");
      await page.getByTestId("teacher-logout").click();
      await login(page, "admin", "admin1", /\/admin$/);

      await expect(page.getByTestId("admin-semester-list")).toContainText(semesterSubject);
      await expect(page.getByTestId("admin-semester-list")).toContainText(copiedSemesterSubject);
      await expect(page.getByTestId("admin-session-list")).toContainText(sessionSubject);
      await attachScreenshot(page, "04-admin-sees-saved-preferences");

      const downloadPromise = page.waitForEvent("download");
      await page.getByTestId("admin-export-button").click();
      const download = await downloadPromise;
      expect(download.suggestedFilename()).toBe("preferences.xlsx");

      const downloadPath = await download.path();
      expect(fs.statSync(downloadPath).size).toBeGreaterThan(1024);

      const workbook = XLSX.readFile(downloadPath);
      const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
      const exportedText = XLSX.utils.sheet_to_json(firstSheet, { header: 1 }).flat().join(" | ");
      expect(exportedText).toContain(teacherLogin);
      expect(exportedText).toContain(semesterSubject);
      expect(exportedText).toContain(copiedSemesterSubject);
      expect(exportedText).toContain(sessionSubject);
    });
  });

  test("invalid login is rejected by the real UI and backend", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("login-input").fill(uniqueLogin("missing-user"));
    await page.getByTestId("password-input").fill(TEACHER_PASSWORD);

    const responsePromise = page.waitForResponse(
      response => response.url().includes("/api/auth/login") && response.request().method() === "POST"
    );
    await page.getByTestId("login-submit").click();

    expect((await responsePromise).status()).toBe(401);
    await expect(page.getByTestId("login-error")).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });
});
