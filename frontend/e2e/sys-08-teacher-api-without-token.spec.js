const { test, expect } = require("@playwright/test");

test("SYS-08 teacher API access without token is forbidden", async ({ request }) => {
  const response = await request.get("/api/teacher/preferences?type=semester");

  expect(response.status()).toBe(403);
});
