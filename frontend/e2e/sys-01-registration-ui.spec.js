const { test, expect } = require("@playwright/test");
const {
  TEACHER_PASSWORD,
  registerTeacherViaUi,
  uniqueLogin
} = require("./support/systemHelpers");

test("SYS-01 registration through UI", async ({ page }) => {
  const loginValue = uniqueLogin("sys01");

  await registerTeacherViaUi(page, `Teacher ${loginValue}`, loginValue, TEACHER_PASSWORD);

  await expect(page.getByTestId("register-success")).toBeVisible();
  await expect(page.getByTestId("register-login")).toHaveValue(loginValue);
});
