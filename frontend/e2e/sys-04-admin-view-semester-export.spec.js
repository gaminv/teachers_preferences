const XLSX = require("xlsx");
const fs = require("fs");
const { test, expect } = require("@playwright/test");
const {
  apiCreateTeacherAndToken,
  apiSavePreferences,
  loginViaUi
} = require("./support/systemHelpers");

test("SYS-04 admin views semester preferences and exports Excel", async ({ page, request }) => {
  const { loginValue, token } = await apiCreateTeacherAndToken(request, "sys04");
  const subject = `Export Subject ${loginValue}`;

  await apiSavePreferences(request, token, [{
    type: "semester",
    subject,
    groups: "EXP-101"
  }]);

  await loginViaUi(page, "admin", "admin1", /\/admin$/);
  await expect(page.getByTestId("admin-semester-list")).toContainText(subject);

  const exportResponsePromise = page.waitForResponse(
    response => response.url().includes("/api/admin/preferences/export") && response.request().method() === "GET"
  );
  const downloadPromise = page.waitForEvent("download");
  await page.getByTestId("admin-export-button").click();
  const exportResponse = await exportResponsePromise;
  const download = await downloadPromise;

  expect(exportResponse.ok()).toBeTruthy();
  expect(exportResponse.headers()["content-disposition"]).toContain("preferences.xlsx");
  expect(download.suggestedFilename()).toBe("preferences.xlsx");

  const exportedPath = await download.path();
  const exportedBuffer = fs.readFileSync(exportedPath);
  expect(exportedBuffer.length).toBeGreaterThan(1024);

  const workbook = XLSX.read(exportedBuffer, { type: "buffer" });
  const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
  const exportedText = XLSX.utils.sheet_to_json(firstSheet, { header: 1 }).flat().join(" | ");
  expect(exportedText).toContain(loginValue);
  expect(exportedText).toContain(subject);
});
