import React from "react";
import { MemoryRouter } from "react-router-dom";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import AdminDashboard from "../../components/AdminDashboard";

const mockedNavigate = vi.fn();
const mockGetAllPreferences = vi.fn();
const mockExportPreferencesToExcel = vi.fn();
const mockLogout = vi.fn();
const mockRegister = vi.fn();

vi.mock("../../api", () => ({
  getAllPreferences: (...args) => mockGetAllPreferences(...args),
  exportPreferencesToExcel: (...args) => mockExportPreferencesToExcel(...args),
  logout: (...args) => mockLogout(...args),
  register: (...args) => mockRegister(...args)
}));

vi.mock("react-router-dom", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => mockedNavigate
  };
});

describe("AdminDashboard unit", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    window.alert = vi.fn();
  });

  test("redirects to login when token is absent", async () => {
    render(
      <MemoryRouter>
        <AdminDashboard />
      </MemoryRouter>
    );
    expect(mockedNavigate).toHaveBeenCalledWith("/login", { replace: true });
  });

  test("loads and displays semester/session preferences", async () => {
    localStorage.setItem("token", "token");
    mockGetAllPreferences.mockResolvedValueOnce([
      { teacherName: "A", subject: "Math", groups: "G1", type: "semester" },
      { teacherName: "B", subject: "Physics", groups: "G2", type: "session" }
    ]);

    render(
      <MemoryRouter>
        <AdminDashboard />
      </MemoryRouter>
    );

    expect(await screen.findByText("Панель администратора")).toBeInTheDocument();
    expect(screen.getByText(/A/)).toBeInTheDocument();
    expect(screen.getByText(/B/)).toBeInTheDocument();
  });

  test("saves config and shows alert", async () => {
    localStorage.setItem("token", "token");
    mockGetAllPreferences.mockResolvedValueOnce([]);

    render(
      <MemoryRouter>
        <AdminDashboard />
      </MemoryRouter>
    );

    await screen.findByText("Панель администратора");
    fireEvent.click(screen.getByText("Сохранить настройки"));
    expect(window.alert).toHaveBeenCalledWith("Настройки сохранены");
    expect(localStorage.getItem("adminConfig")).toContain("semesterButtonText");
  });

  test("downloads excel and handles download error", async () => {
    localStorage.setItem("token", "token");
    mockGetAllPreferences.mockResolvedValue([]);
    mockExportPreferencesToExcel.mockRejectedValueOnce(new Error("export failed"));

    render(
      <MemoryRouter>
        <AdminDashboard />
      </MemoryRouter>
    );
    await screen.findByText("Панель администратора");
    fireEvent.click(screen.getByText("Скачать все в Excel"));
    await waitFor(() => expect(window.alert).toHaveBeenCalledWith("Не удалось скачать: export failed"));
  });

  test("register success flow", async () => {
    localStorage.setItem("token", "token");
    mockGetAllPreferences.mockResolvedValue([]);
    mockRegister.mockResolvedValue({});

    render(
      <MemoryRouter>
        <AdminDashboard />
      </MemoryRouter>
    );
    await screen.findByText("Регистрация нового пользователя");

    fireEvent.change(screen.getByPlaceholderText("ФИО"), { target: { value: "Иван Иванов" } });
    fireEvent.change(screen.getByPlaceholderText("Логин"), { target: { value: "ivan" } });
    fireEvent.change(screen.getByPlaceholderText("Пароль"), { target: { value: "123456" } });
    fireEvent.click(screen.getByRole("button", { name: "Создать" }));

    expect(await screen.findByText("Пользователь «ivan» успешно создан")).toBeInTheDocument();
  });

  test("logout button calls api logout and navigate", async () => {
    localStorage.setItem("token", "token");
    mockGetAllPreferences.mockResolvedValue([]);
    render(
      <MemoryRouter>
        <AdminDashboard />
      </MemoryRouter>
    );
    await screen.findByText("Панель администратора");
    fireEvent.click(screen.getByText("Выйти"));
    expect(mockLogout).toHaveBeenCalledTimes(1);
    expect(mockedNavigate).toHaveBeenCalledWith("/login", { replace: true });
  });
});
