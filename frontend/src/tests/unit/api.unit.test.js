import {
  register,
  login,
  logout,
  getTeacherPreferences,
  saveTeacherPreferences,
  getAllPreferences
} from "../../api";

describe("api unit", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    localStorage.clear();
    global.fetch = vi.fn();
  });

  test.each([
    [{ fullName: "A", login: "a", password: "123456" }],
    [{ fullName: "B", login: "b", password: "abcdef" }],
    [{ fullName: "User", login: "user", password: "qwerty" }],
    [{ fullName: "Тест", login: "test", password: "пароль12" }],
    [{ fullName: "Jane Doe", login: "jane", password: "pwdpwd" }]
  ])("register success %o", async (payload) => {
    fetch.mockResolvedValueOnce({ ok: true, json: async () => ({ message: "ok" }) });
    const res = await register(payload);
    expect(res.message).toBe("ok");
    expect(fetch).toHaveBeenCalledTimes(1);
  });

  test.each([
    [{ message: "bad" }, "bad"],
    [{}, "Ошибка регистрации"]
  ])("register error fallback %#", async (errBody, expected) => {
    fetch.mockResolvedValueOnce({
      ok: false,
      json: async () => errBody
    });
    await expect(register({ fullName: "A", login: "a", password: "123456" })).rejects.toThrow(expected);
  });

  test.each([
    [{ login: "a", password: "123456" }],
    [{ login: "b", password: "abcdef" }],
    [{ login: "c", password: "zzzzzz" }],
    [{ login: "d", password: "111111" }],
    [{ login: "e", password: "222222" }]
  ])("login success %#", async (payload) => {
    fetch.mockResolvedValueOnce({ ok: true, json: async () => ({ token: "t" }) });
    const res = await login(payload);
    expect(res.token).toBe("t");
  });

  test.each([
    [{ message: "unauthorized" }, "unauthorized"],
    [{}, "Ошибка входа"]
  ])("login error %#", async (errBody, expected) => {
    fetch.mockResolvedValueOnce({ ok: false, json: async () => errBody });
    await expect(login({ login: "a", password: "b" })).rejects.toThrow(expected);
  });

  test("logout clears local storage", () => {
    localStorage.setItem("token", "abc");
    localStorage.setItem("user", "x");
    logout();
    expect(localStorage.getItem("token")).toBeNull();
    expect(localStorage.getItem("user")).toBeNull();
  });

  test.each(["semester", "session", "other", "sem-1", "s 2"])("getTeacherPreferences ok for %s", async (type) => {
    fetch.mockResolvedValueOnce({ ok: true, status: 200, json: async () => [{ type }] });
    const res = await getTeacherPreferences(type);
    expect(Array.isArray(res)).toBe(true);
  });

  test.each([204, 403])("getTeacherPreferences returns [] for status %i", async (status) => {
    fetch.mockResolvedValueOnce({ ok: false, status, json: async () => ({}) });
    const res = await getTeacherPreferences("semester");
    expect(res).toEqual([]);
  });

  test.each([
    [{ message: "boom" }, "boom"],
    [{}, "Bad Request"]
  ])("getTeacherPreferences error fallback %#", async (errBody, expected) => {
    fetch.mockResolvedValueOnce({
      ok: false,
      status: 400,
      statusText: "Bad Request",
      json: async () => errBody
    });
    await expect(getTeacherPreferences("semester")).rejects.toThrow(expected);
  });

  test.each([
    ["semester", [{ subject: "A" }]],
    ["session", [{ subject: "B" }, { subject: "C" }]],
    ["semester", []],
    ["session", [{ comments: "x" }]],
    ["semester", [{ days: ["Пн"] }]]
  ])("saveTeacherPreferences success %#", async (type, dtos) => {
    fetch.mockResolvedValueOnce({ ok: true, json: async () => dtos });
    const res = await saveTeacherPreferences(type, dtos);
    expect(res).toEqual(dtos);
  });

  test.each([
    [{ message: "save failed" }, "save failed"],
    [{}, "Bad Request"]
  ])("saveTeacherPreferences errors %#", async (errBody, expected) => {
    fetch.mockResolvedValueOnce({
      ok: false,
      statusText: "Bad Request",
      json: async () => errBody
    });
    await expect(saveTeacherPreferences("semester", [{ subject: "x" }])).rejects.toThrow(expected);
  });

  test.each([
    [[{ id: 1 }]],
    [[{ id: 1 }, { id: 2 }]],
    [[]]
  ])("getAllPreferences success %#", async (payload) => {
    fetch.mockResolvedValueOnce({ ok: true, json: async () => payload });
    const res = await getAllPreferences();
    expect(res).toEqual(payload);
  });
});
