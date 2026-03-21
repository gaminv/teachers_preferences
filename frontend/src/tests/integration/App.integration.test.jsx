import React from "react";
import { render, screen } from "@testing-library/react";

vi.mock("../../components/Login", () => ({ default: () => <div>LoginPage</div> }));
vi.mock("../../components/Register", () => ({ default: () => <div>RegisterPage</div> }));
vi.mock("../../components/AdminDashboard", () => ({ default: () => <div>AdminDashboardPage</div> }));
vi.mock("../../components/TeacherDashboard", () => ({ default: () => <div>TeacherDashboardPage</div> }));
vi.mock("../../components/PreferencesForm", () => ({ default: () => <div>PreferencesFormPage</div> }));

describe("App integration routes", () => {
  test.each([
    ["/login", "LoginPage"],
    ["/register", "RegisterPage"],
    ["/admin", "AdminDashboardPage"],
    ["/teacher", "TeacherDashboardPage"],
    ["/teacher/semester", "PreferencesFormPage"],
    ["/teacher/session", "PreferencesFormPage"],
    ["/not-found", "Страница не найдена"]
  ])("route %s renders correct page", async (path, expected) => {
    window.history.pushState({}, "", path);
    const { default: App } = await import("../../App");
    render(<App />);
    expect(await screen.findByText(expected)).toBeInTheDocument();
  });
});
