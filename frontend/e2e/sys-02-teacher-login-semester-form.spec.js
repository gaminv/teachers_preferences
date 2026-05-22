const { test, expect } = require("@playwright/test");
const {
  TEACHER_PASSWORD,
  apiRegisterTeacher,
  loginViaUi,
  uniqueLogin
} = require("./support/systemHelpers");

test("SYS-02 teacher login and opening semester form", async ({ page, request }) => {
  const loginValue = uniqueLogin("sys02");
  await apiRegisterTeacher(request, `Teacher ${loginValue}`, loginValue, TEACHER_PASSWORD);

  await loginViaUi(page, loginValue, TEACHER_PASSWORD, /\/teacher$/);
  await page.getByTestId("teacher-semester-link").click();

  await expect(page).toHaveURL(/\/teacher\/semester$/);
  await expect(page.getByTestId("preference-0-subject")).toBeVisible();
});
