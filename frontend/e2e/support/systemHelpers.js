const { expect } = require("@playwright/test");

const TEACHER_PASSWORD = "123456";

function uniqueLogin(prefix) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100000)}`;
}

async function expectOkResponse(response) {
  const requestLabel = typeof response.request === "function"
    ? `${response.request().method()} ${response.url()}`
    : response.url();
  expect(response.ok(), `${requestLabel} returned ${response.status()}`).toBeTruthy();
}

async function loginViaUi(page, loginValue, password = TEACHER_PASSWORD, expectedUrl = /\/teacher$/) {
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

async function registerTeacherViaUi(page, fullName, loginValue, password = TEACHER_PASSWORD) {
  await page.goto("/register");
  await page.getByTestId("register-full-name").fill(fullName);
  await page.getByTestId("register-login").fill(loginValue);
  await page.getByTestId("register-password").fill(password);

  const responsePromise = page.waitForResponse(
    response => response.url().includes("/api/auth/register") && response.request().method() === "POST"
  );
  await page.getByTestId("register-submit").click();
  await expectOkResponse(await responsePromise);
  await expect(page.getByTestId("register-success")).toBeVisible();
}

async function apiRegisterTeacher(request, fullName, loginValue, password = TEACHER_PASSWORD) {
  const response = await request.post("/api/auth/register", {
    data: { fullName, login: loginValue, password }
  });
  await expectOkResponse(response);
}

async function apiLogin(request, loginValue, password = TEACHER_PASSWORD) {
  const response = await request.post("/api/auth/login", {
    data: { login: loginValue, password }
  });
  await expectOkResponse(response);
  const body = await response.json();
  expect(body.token).toBeTruthy();
  return body.token;
}

async function apiCreateTeacherAndToken(request, prefix) {
  const loginValue = uniqueLogin(prefix);
  await apiRegisterTeacher(request, `Teacher ${loginValue}`, loginValue);
  const token = await apiLogin(request, loginValue);
  return { loginValue, token };
}

function bearerHeaders(token) {
  return {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json"
  };
}

async function apiSavePreferences(request, token, preferences) {
  const response = await request.post("/api/teacher/preferences", {
    headers: bearerHeaders(token),
    data: preferences
  });
  await expectOkResponse(response);
  return response.json();
}

async function apiGetTeacherPreferences(request, token, type) {
  const response = await request.get(`/api/teacher/preferences?type=${encodeURIComponent(type)}`, {
    headers: bearerHeaders(token)
  });
  await expectOkResponse(response);
  return response.json();
}

async function savePreferencesViaUi(page, expectedCount) {
  const responsePromise = page.waitForResponse(
    response => response.url().includes("/api/teacher/preferences") && response.request().method() === "POST"
  );
  await page.getByTestId("preferences-save").click();
  const response = await responsePromise;
  await expectOkResponse(response);
  expect(await response.json()).toHaveLength(expectedCount);
  await expect(page.getByTestId("preferences-success")).toBeVisible();
}

async function fillSemesterPreference(page, { subject, groups }) {
  await page.getByTestId("preference-0-subject").fill(subject);
  await page.getByTestId("preference-0-groups").fill(groups);
  await page.getByTestId("preference-0-day-0").check();
  await page.getByTestId("preference-0-days-priority").selectOption("5");
  await page.getByTestId("preference-0-times").fill("09:00-12:00");
  await page.getByTestId("preference-0-times-priority").selectOption("4");
}

async function fillSessionPreference(page, { subject, groups }) {
  await page.getByTestId("preference-0-subject").fill(subject);
  await page.getByTestId("preference-0-groups").fill(groups);
  await page.getByTestId("preference-0-preferred-dates").fill("10.01, 12.01");
  await page.getByTestId("preference-0-preferred-dates-priority").selectOption("5");
  await page.getByTestId("preference-0-avoid-dates").fill("15.01");
  await page.getByTestId("preference-0-avoid-dates-priority").selectOption("4");
}

module.exports = {
  TEACHER_PASSWORD,
  uniqueLogin,
  expectOkResponse,
  loginViaUi,
  registerTeacherViaUi,
  apiRegisterTeacher,
  apiLogin,
  apiCreateTeacherAndToken,
  apiSavePreferences,
  apiGetTeacherPreferences,
  savePreferencesViaUi,
  fillSemesterPreference,
  fillSessionPreference
};
