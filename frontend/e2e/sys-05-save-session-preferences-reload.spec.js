const { test, expect } = require("@playwright/test");
const {
  TEACHER_PASSWORD,
  apiRegisterTeacher,
  fillSessionPreference,
  loginViaUi,
  savePreferencesViaUi,
  uniqueLogin
} = require("./support/systemHelpers");

test("SYS-05 save session preferences and verify after reload", async ({ page, request }) => {
  const loginValue = uniqueLogin("sys05");
  const subject = `Session Subject ${loginValue}`;
  await apiRegisterTeacher(request, `Teacher ${loginValue}`, loginValue, TEACHER_PASSWORD);

  await loginViaUi(page, loginValue, TEACHER_PASSWORD, /\/teacher$/);
  await page.getByTestId("teacher-session-link").click();
  await fillSessionPreference(page, { subject, groups: "EX-202" });
  await savePreferencesViaUi(page, 1);

  await page.reload();
  await expect(page.getByTestId("preference-0-subject")).toHaveValue(subject);
  await expect(page.getByTestId("preference-0-groups")).toHaveValue("EX-202");
  await expect(page.getByTestId("preference-0-preferred-dates")).toHaveValue("10.01, 12.01");
});
