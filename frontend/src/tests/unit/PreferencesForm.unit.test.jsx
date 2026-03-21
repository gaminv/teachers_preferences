import React from "react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import PreferencesForm from "../../components/PreferencesForm";

const mockGetTeacherPreferences = vi.fn();
const mockSaveTeacherPreferences = vi.fn();
const mockLogout = vi.fn();
const mockedNavigate = vi.fn();

vi.mock("../../api", () => ({
  getTeacherPreferences: (...args) => mockGetTeacherPreferences(...args),
  saveTeacherPreferences: (...args) => mockSaveTeacherPreferences(...args),
  logout: (...args) => mockLogout(...args)
}));

vi.mock("react-router-dom", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => mockedNavigate
  };
});

const renderForPath = (path) =>
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/teacher/:type" element={<PreferencesForm />} />
      </Routes>
    </MemoryRouter>
  );

describe("PreferencesForm unit", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetTeacherPreferences.mockReset();
    mockSaveTeacherPreferences.mockReset();
    mockLogout.mockReset();
    mockedNavigate.mockReset();
    localStorage.clear();
  });

  test("semester mode renders and saves mapped numeric priorities", async () => {
    localStorage.setItem("adminConfig", JSON.stringify({ semesterButtonText: "Sem Btn" }));
    mockGetTeacherPreferences.mockResolvedValueOnce([]);
    mockSaveTeacherPreferences.mockResolvedValueOnce([
      {
        subject: "Math",
        groups: "A1",
        type: "semester",
        days: ["Пн"],
        daysPriority: 5,
        computers: [],
        showRooms: false
      }
    ]);

    renderForPath("/teacher/semester");

    expect(await screen.findByText("Sem Btn")).toBeInTheDocument();
    const textInputs = screen.getAllByRole("textbox");
    fireEvent.change(textInputs[0], { target: { value: "Math" } });
    fireEvent.change(textInputs[1], { target: { value: "A1" } });
    fireEvent.click(screen.getByLabelText("Пн"));

    const prioritySelects = screen.getAllByRole("combobox");
    fireEvent.change(prioritySelects[0], { target: { value: "5" } });

    fireEvent.click(screen.getByRole("button", { name: "Сохранить" }));

    await waitFor(() => expect(mockSaveTeacherPreferences).toHaveBeenCalledTimes(1));
    const [typeArg, payload] = mockSaveTeacherPreferences.mock.calls[0];
    expect(typeArg).toBe("semester");
    expect(payload[0].daysPriority).toBe(5);
    expect(await screen.findByText("✅ Ваши пожелания успешно сохранены")).toBeInTheDocument();
  });

  test("session mode renders session fields branch", async () => {
    localStorage.setItem("adminConfig", JSON.stringify({ sessionButtonText: "Session Btn" }));
    mockGetTeacherPreferences.mockResolvedValueOnce([]);
    mockSaveTeacherPreferences.mockResolvedValueOnce([]);
    renderForPath("/teacher/session");
    expect(await screen.findByText("Session Btn")).toBeInTheDocument();
    expect(screen.getByText("Предпочтительные даты")).toBeInTheDocument();
    expect(screen.getByText("Даты, в которые НЕ ставить")).toBeInTheDocument();
    expect(screen.getByText("Пожелания до/после Нового года")).toBeInTheDocument();
  });

  test("add/copy/remove entry buttons work", async () => {
    mockGetTeacherPreferences.mockResolvedValueOnce([]);
    renderForPath("/teacher/semester");
    await screen.findByText("Пожелания к семестру");

    fireEvent.click(screen.getByRole("button", { name: "Добавить ещё" }));
    expect(screen.getByText("Копировать предыдущий")).toBeInTheDocument();
    fireEvent.click(screen.getByText("Копировать предыдущий"));
    fireEvent.click(screen.getAllByText("✕")[0]);
  });

  test("initial load 401 triggers logout and redirect", async () => {
    mockGetTeacherPreferences.mockRejectedValueOnce(new Error("401 Unauthorized"));
    renderForPath("/teacher/semester");
    await waitFor(() => expect(mockLogout).toHaveBeenCalledTimes(1));
    expect(mockedNavigate).toHaveBeenCalledWith("/login", { replace: true });
  });

  test("save 401 triggers logout and redirect", async () => {
    mockGetTeacherPreferences.mockResolvedValueOnce([]);
    mockSaveTeacherPreferences.mockRejectedValueOnce(new Error("401 Unauthorized"));
    renderForPath("/teacher/semester");
    await screen.findByText("Пожелания к семестру");
    fireEvent.click(screen.getByRole("button", { name: "Сохранить" }));
    await waitFor(() => expect(mockLogout).toHaveBeenCalledTimes(1));
    expect(mockedNavigate).toHaveBeenCalledWith("/login", { replace: true });
  });
});
