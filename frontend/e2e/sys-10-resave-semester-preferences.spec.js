const { test, expect } = require("@playwright/test");
const {
  TEACHER_PASSWORD,
  apiGetTeacherPreferences,
  apiLogin,
  apiRegisterTeacher,
  fillSemesterPreference,
  loginViaUi,
  savePreferencesViaUi,
  uniqueLogin
} = require("./support/systemHelpers");

test("SYS-10 repeated semester preference saving keeps only the latest set", async ({ page, request }) => {
  const loginValue = uniqueLogin("sys10");
  const firstSubject = `Old Semester ${loginValue}`;
  const secondSubject = `New Semester ${loginValue}`;
  await apiRegisterTeacher(request, `Teacher ${loginValue}`, loginValue, TEACHER_PASSWORD);

  await loginViaUi(page, loginValue, TEACHER_PASSWORD, /\/teacher$/);
  await page.getByTestId("teacher-semester-link").click();
  await fillSemesterPreference(page, { subject: firstSubject, groups: "OLD-101" });
  await savePreferencesViaUi(page, 1);

  await page.getByTestId("preference-0-subject").fill(secondSubject);
  await page.getByTestId("preference-0-groups").fill("NEW-202");
  await savePreferencesViaUi(page, 1);

  await page.reload();
  await expect(page.getByTestId("preference-0-subject")).toHaveValue(secondSubject);
  await expect(page.getByTestId("preference-0-groups")).toHaveValue("NEW-202");

  const token = await apiLogin(request, loginValue, TEACHER_PASSWORD);
  const preferences = await apiGetTeacherPreferences(request, token, "semester");
  expect(preferences).toHaveLength(1);
  expect(preferences[0].subject).toBe(secondSubject);
});
