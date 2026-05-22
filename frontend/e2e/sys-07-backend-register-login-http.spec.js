const { test, expect } = require("@playwright/test");
const {
  TEACHER_PASSWORD,
  apiRegisterTeacher,
  expectOkResponse,
  uniqueLogin
} = require("./support/systemHelpers");

test("SYS-07 registration and login through full backend HTTP contour", async ({ request }) => {
  const loginValue = uniqueLogin("sys07");

  await apiRegisterTeacher(request, `Teacher ${loginValue}`, loginValue, TEACHER_PASSWORD);
  const loginResponse = await request.post("/api/auth/login", {
    data: { login: loginValue, password: TEACHER_PASSWORD }
  });

  await expectOkResponse(loginResponse);
  const body = await loginResponse.json();
  expect(body.token).toBeTruthy();
  expect(body.role).toBe("TEACHER");
});
