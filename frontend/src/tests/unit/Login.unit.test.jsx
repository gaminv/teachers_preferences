import React from "react";
import { MemoryRouter } from "react-router-dom";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import Login from "../../components/Login";

vi.mock("../../api", () => ({
  login: vi.fn()
}));

const mockedNavigate = vi.fn();
vi.mock("react-router-dom", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => mockedNavigate
  };
});

import { login as apiLogin } from "../../api";

describe("Login unit", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  const renderPage = () =>
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

  test.each([
    ["", "", "Логин обязателен"],
    ["user", "", "Пароль обязателен"],
    ["user", "123", "Пароль минимум 6 символов"]
  ])("validation %#", async (login, pwd, errorText) => {
    renderPage();
    fireEvent.change(screen.getByPlaceholderText("Введите ваш логин"), { target: { value: login } });
    fireEvent.change(screen.getByPlaceholderText("••••••••"), { target: { value: pwd } });
    fireEvent.click(screen.getByRole("button", { name: "Войти" }));
    expect(await screen.findByText(errorText)).toBeInTheDocument();
  });

  test.each([
    ["ADMIN", "/admin"],
    ["TEACHER", "/teacher"],
    ["OTHER", "/teacher"]
  ])("successful navigation for role %s", async (role, expectedPath) => {
    apiLogin.mockResolvedValueOnce({ token: "token", fullName: "John", role });
    renderPage();
    fireEvent.change(screen.getByPlaceholderText("Введите ваш логин"), { target: { value: "john" } });
    fireEvent.change(screen.getByPlaceholderText("••••••••"), { target: { value: "123456" } });
    fireEvent.click(screen.getByRole("button", { name: "Войти" }));
    await waitFor(() => expect(mockedNavigate).toHaveBeenCalledWith(expectedPath, { replace: true }));
  });

  test("shows api error", async () => {
    apiLogin.mockRejectedValueOnce(new Error("Неверные данные"));
    renderPage();
    fireEvent.change(screen.getByPlaceholderText("Введите ваш логин"), { target: { value: "john" } });
    fireEvent.change(screen.getByPlaceholderText("••••••••"), { target: { value: "123456" } });
    fireEvent.click(screen.getByRole("button", { name: "Войти" }));
    expect(await screen.findByText("Неверные данные")).toBeInTheDocument();
  });

  test("toggles password visibility", () => {
    renderPage();
    const input = screen.getByPlaceholderText("••••••••");
    expect(input).toHaveAttribute("type", "password");
    fireEvent.click(screen.getByLabelText("Показать пароль"));
    expect(input).toHaveAttribute("type", "text");
  });
});
