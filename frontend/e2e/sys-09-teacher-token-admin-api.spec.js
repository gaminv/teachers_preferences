const { test, expect } = require("@playwright/test");
const {
  apiCreateTeacherAndToken
} = require("./support/systemHelpers");

test("SYS-09 teacher token cannot access admin API", async ({ request }) => {
  const { token } = await apiCreateTeacherAndToken(request, "sys09");

  const response = await request.get("/api/admin/preferences", {
    headers: { Authorization: `Bearer ${token}` }
  });

  expect(response.status()).toBe(403);
});
