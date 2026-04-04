import React from "react";
import { MemoryRouter } from "react-router-dom";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import Register from "../../components/Register";

vi.mock("../../api", () => ({
  register: vi.fn()
}));

import { register as registerApi } from "../../api";

describe("Register integration flows", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const fillAndSubmit = (fullName, login, password) => {
    fireEvent.change(screen.getByTestId("register-full-name"), { target: { value: fullName } });
    fireEvent.change(screen.getByTestId("register-login"), { target: { value: login } });
    fireEvent.change(screen.getByTestId("register-password"), { target: { value: password } });
    fireEvent.click(screen.getByTestId("register-submit"));
  };

  test.each([
    ["Иван Иванов", "ivan", "123456"],
    ["John Doe", "john", "abcdef"],
    ["Teacher One", "teach1", "qwerty"],
    ["Teacher Two", "teach2", "zxcvbn"],
    ["Teacher Three", "teach3", "pass12"]
  ])("successful registration flow %#", async (fullName, login, password) => {
    registerApi.mockResolvedValueOnce({ message: "ok" });
    render(
      <MemoryRouter>
        <Register />
      </MemoryRouter>
    );
    fillAndSubmit(fullName, login, password);
    await waitFor(() => expect(screen.getByTestId("register-success")).toBeInTheDocument());
  });

  test.each([
    ["Ошибка регистрации"],
    ["login is already registered"],
    ["Server error"],
    ["Network failed"],
    ["Validation failed"]
  ])("registration negative flow %#", async (message) => {
    registerApi.mockRejectedValueOnce(new Error(message));
    render(
      <MemoryRouter>
        <Register />
      </MemoryRouter>
    );
    fillAndSubmit("Иван", "ivan", "123456");
    await waitFor(() => expect(screen.getByTestId("register-error")).toHaveTextContent(message));
  });
});
