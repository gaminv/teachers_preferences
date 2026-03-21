import React from "react";
import { MemoryRouter } from "react-router-dom";
import { fireEvent, render, screen } from "@testing-library/react";
import TeacherDashboard from "../../components/TeacherDashboard";

const mockedNavigate = vi.fn();
vi.mock("react-router-dom", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => mockedNavigate
  };
});

describe("TeacherDashboard unit", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    window.alert = vi.fn();
  });

  test("redirects to login without stored user", () => {
    render(
      <MemoryRouter>
        <TeacherDashboard />
      </MemoryRouter>
    );
    expect(mockedNavigate).toHaveBeenCalledWith("/login", { replace: true });
  });

  test.each([
    [{ fullName: "John Doe" }, "Здравствуйте, John Doe!"],
    [{ fullName: "Иван Иванов" }, "Здравствуйте, Иван Иванов!"]
  ])("renders greeting %#", (user, expectedText) => {
    localStorage.setItem("user", JSON.stringify(user));
    render(
      <MemoryRouter>
        <TeacherDashboard />
      </MemoryRouter>
    );
    expect(screen.getByText(expectedText)).toBeInTheDocument();
  });

  test("logout clears storage and redirects", () => {
    localStorage.setItem("user", JSON.stringify({ fullName: "John" }));
    localStorage.setItem("token", "t");
    render(
      <MemoryRouter>
        <TeacherDashboard />
      </MemoryRouter>
    );
    fireEvent.click(screen.getByText("Выйти"));
    expect(localStorage.getItem("token")).toBeNull();
    expect(localStorage.getItem("user")).toBeNull();
    expect(mockedNavigate).toHaveBeenCalledWith("/login", { replace: true });
  });
});
