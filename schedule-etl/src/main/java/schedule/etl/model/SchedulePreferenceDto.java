package schedule.etl.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Одна строка шаблона пожеланий расписания (базовая модель данных).
 * Используется после преобразования (Transform) и для JSON при обмене с алгоритмическим ядром.
 * Поля соответствуют столбцам листа «Пожелания» в шаблоне Excel.
 */
public class SchedulePreferenceDto {

    @NotBlank(message = "ФИО преподавателя обязательно")
    @Size(max = 500)
    private String teacherName;

    @Size(max = 100)
    private String teacherLogin;

    @Pattern(regexp = "semester|session", message = "Тип: допустимо только semester или session (в Excel: «Семестр»/«Сессия»). Получено: ${validatedValue}.")
    private String type;

    /** Дисциплина / предмет (столбец «Предмет»). */
    @Size(max = 300)
    private String subject;

    @Size(max = 500)
    private String groups;

    private List<String> days;

    @Min(value = 0, message = "Приоритет дней: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    @Max(value = 5, message = "Приоритет дней: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    private Integer daysPriority;

    @Size(max = 200)
    private String times;

    @Min(value = 0, message = "Приоритет времени: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    @Max(value = 5, message = "Приоритет времени: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    private Integer timesPriority;

    @Size(max = 500)
    private String preferredDates;

    @Size(max = 500)
    private String avoidDates;

    @Size(max = 500)
    private String newYearPref;

    @Size(max = 100)
    private String loadType;

    @Min(value = 0, message = "Приоритет нагрузки: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    @Max(value = 5, message = "Приоритет нагрузки: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    private Integer loadTypePriority;

    @Size(max = 300)
    private String buildingRoom;

    @Min(value = 0, message = "Приоритет корпуса/аудитории: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    @Max(value = 5, message = "Приоритет корпуса/аудитории: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    private Integer buildingRoomPriority;

    @Size(max = 100)
    private String boardType;

    @Min(value = 0, message = "Приоритет доски: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    @Max(value = 5, message = "Приоритет доски: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    private Integer boardTypePriority;

    private List<String> computers;

    @Min(value = 0, message = "Приоритет компьютеров: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    @Max(value = 5, message = "Приоритет компьютеров: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    private Integer computersPriority;

    @Size(max = 100)
    private String format;

    @Min(value = 0, message = "Приоритет формата: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    @Max(value = 5, message = "Приоритет формата: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    private Integer formatPriority;

    @Size(max = 1000)
    private String comments;

    @Min(value = 0, message = "Приоритет комментария: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    @Max(value = 5, message = "Приоритет комментария: допустимый диапазон 0–5. Получено: ${validatedValue}.")
    private Integer commentsPriority;

    public SchedulePreferenceDto() {}

    // --- Getters and setters (required for Bean Validation and Jackson) ---

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getTeacherLogin() { return teacherLogin; }
    public void setTeacherLogin(String teacherLogin) { this.teacherLogin = teacherLogin; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getGroups() { return groups; }
    public void setGroups(String groups) { this.groups = groups; }

    public List<String> getDays() { return days; }
    public void setDays(List<String> days) { this.days = days; }

    public Integer getDaysPriority() { return daysPriority; }
    public void setDaysPriority(Integer daysPriority) { this.daysPriority = daysPriority; }

    public String getTimes() { return times; }
    public void setTimes(String times) { this.times = times; }

    public Integer getTimesPriority() { return timesPriority; }
    public void setTimesPriority(Integer timesPriority) { this.timesPriority = timesPriority; }

    public String getPreferredDates() { return preferredDates; }
    public void setPreferredDates(String preferredDates) { this.preferredDates = preferredDates; }

    public String getAvoidDates() { return avoidDates; }
    public void setAvoidDates(String avoidDates) { this.avoidDates = avoidDates; }

    public String getNewYearPref() { return newYearPref; }
    public void setNewYearPref(String newYearPref) { this.newYearPref = newYearPref; }

    public String getLoadType() { return loadType; }
    public void setLoadType(String loadType) { this.loadType = loadType; }

    public Integer getLoadTypePriority() { return loadTypePriority; }
    public void setLoadTypePriority(Integer loadTypePriority) { this.loadTypePriority = loadTypePriority; }

    public String getBuildingRoom() { return buildingRoom; }
    public void setBuildingRoom(String buildingRoom) { this.buildingRoom = buildingRoom; }

    public Integer getBuildingRoomPriority() { return buildingRoomPriority; }
    public void setBuildingRoomPriority(Integer buildingRoomPriority) { this.buildingRoomPriority = buildingRoomPriority; }

    public String getBoardType() { return boardType; }
    public void setBoardType(String boardType) { this.boardType = boardType; }

    public Integer getBoardTypePriority() { return boardTypePriority; }
    public void setBoardTypePriority(Integer boardTypePriority) { this.boardTypePriority = boardTypePriority; }

    public List<String> getComputers() { return computers; }
    public void setComputers(List<String> computers) { this.computers = computers; }

    public Integer getComputersPriority() { return computersPriority; }
    public void setComputersPriority(Integer computersPriority) { this.computersPriority = computersPriority; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public Integer getFormatPriority() { return formatPriority; }
    public void setFormatPriority(Integer formatPriority) { this.formatPriority = formatPriority; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Integer getCommentsPriority() { return commentsPriority; }
    public void setCommentsPriority(Integer commentsPriority) { this.commentsPriority = commentsPriority; }
}
