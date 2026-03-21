package schedule.etl.transform;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;
import schedule.etl.model.SchedulePreferenceDto;
import schedule.etl.model.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Transform: нормализация строк и многоуровневая валидация (Bean Validation + бизнес-правила).
 */
public final class ScheduleTransform {

    private final Validator validator;

    public ScheduleTransform(Validator validator) {
        this.validator = validator;
    }

    /**
     * Нормализует и валидирует строки. Возвращает список ошибок с привязкой к строке и полю.
     * Номер строки — 1-based (строка 1 = заголовок, строка 2 = первая запись).
     */
    public List<ValidationError> validate(List<SchedulePreferenceDto> rows) {
        List<ValidationError> errors = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            SchedulePreferenceDto dto = rows.get(i);
            normalize(dto);
            int rowNum = i + 2; // в Excel: строка 1 — заголовок
            Set<ConstraintViolation<SchedulePreferenceDto>> violations = validator.validate(dto);
            for (ConstraintViolation<SchedulePreferenceDto> v : violations) {
                var annotation = v.getConstraintDescriptor().getAnnotation();
                ValidationError.Severity severity =
                        (annotation instanceof Size) ? ValidationError.Severity.WARNING : ValidationError.Severity.ERROR;
                errors.add(new ValidationError(rowNum, v.getPropertyPath().toString(), v.getMessage(), severity));
            }
            // ФИО без пробела (например «Синицын») — не ошибка, а предупреждение; строка остаётся в данных
            String name = dto.getTeacherName();
            if (name != null && !name.isBlank() && !name.contains(" ")) {
                errors.add(new ValidationError(rowNum, "Преподаватель (ФИО)",
                        "Рекомендуется указать в формате «Фамилия И.О.» (с пробелом). Сейчас указано одно слово (например, только фамилия).",
                        ValidationError.Severity.WARNING));
            }
        }
        return errors;
    }

    private void normalize(SchedulePreferenceDto dto) {
        if (dto.getTeacherName() != null) dto.setTeacherName(dto.getTeacherName().trim());
        if (dto.getTeacherLogin() != null) dto.setTeacherLogin(dto.getTeacherLogin().trim());
        if (dto.getSubject() != null) dto.setSubject(dto.getSubject().trim());
        if (dto.getGroups() != null) dto.setGroups(dto.getGroups().trim());
        if (dto.getTimes() != null) dto.setTimes(dto.getTimes().trim());
        if (dto.getPreferredDates() != null) dto.setPreferredDates(dto.getPreferredDates().trim());
        if (dto.getAvoidDates() != null) dto.setAvoidDates(dto.getAvoidDates().trim());
        if (dto.getNewYearPref() != null) dto.setNewYearPref(dto.getNewYearPref().trim());
        if (dto.getBuildingRoom() != null) dto.setBuildingRoom(dto.getBuildingRoom().trim());
        if (dto.getComments() != null) dto.setComments(dto.getComments().trim());
        if (dto.getDays() != null) {
            dto.setDays(dto.getDays().stream().map(String::trim).filter(s -> !s.isEmpty()).distinct().toList());
        }
        if (dto.getComputers() != null) {
            dto.setComputers(dto.getComputers().stream().map(String::trim).filter(s -> !s.isEmpty()).toList());
        }
    }
}
