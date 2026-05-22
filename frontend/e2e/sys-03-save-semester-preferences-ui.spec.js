const { test, expect } = require("@playwright/test");
const {
  TEACHER_PASSWORD,
  apiRegisterTeacher,
  fillSemesterPreference,
  loginViaUi,
  savePreferencesViaUi,
  uniqueLogin
} = require("./support/systemHelpers");

test("SYS-03 save semester preferences through UI", async ({ page, request }) => {
  const loginValue = uniqueLogin("sys03");
  const subject = `Semester Subject ${loginValue}`;
  await apiRegisterTeacher(request, `Teacher ${loginValue}`, loginValue, TEACHER_PASSWORD);

  await loginViaUi(page, loginValue, TEACHER_PASSWORD, /\/teacher$/);
  await page.getByTestId("teacher-semester-link").click();
  await fillSemesterPreference(page, { subject, groups: "A-101" });
  await savePreferencesViaUi(page, 1);

  await expect(page.getByTestId("preferences-success")).toBeVisible();
  await page.reload();
  await expect(page.getByTestId("preference-0-subject")).toHaveValue(subject);
  await expect(page.getByTestId("preference-0-groups")).toHaveValue("A-101");
});
