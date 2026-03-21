package schedule.etl.util;

import schedule.etl.model.SchedulePreferenceDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Генератор тестовых данных пожеланий расписания для проверки импорта/экспорта и тестов на сайте.
 */
public final class SampleDataGenerator {

    private static final String[] SURNAMES = {
            "Иванов", "Петрова", "Сидоров", "Козлова", "Новиков", "Морозова", "Волков", "Соколова", "Лебедев", "Кузнецова",
            "Смирнов", "Федорова", "Попов", "Михайлов", "Васильева", "Семёнов", "Егорова", "Павлов", "Козлов", "Степанова",
            "Николаев", "Орлова", "Андреев", "Макарова", "Никитин", "Захарова", "Зайцев", "Соловьёва", "Борисов", "Яковлева"
    };
    private static final String[] NAMES = {
            "Иван", "Мария", "Алексей", "Елена", "Дмитрий", "Анна", "Сергей", "Ольга", "Андрей", "Наталья",
            "Пётр", "Татьяна", "Николай", "Ирина", "Владимир", "Светлана", "Михаил", "Екатерина", "Виктор", "Юлия",
            "Павел", "Ольга", "Александр", "Лариса", "Максим", "Надежда", "Артём", "Вероника", "Игорь", "Дарья"
    };
    private static final String[] PATRONYMICS = {
            "Иванович", "Петровна", "Алексеевич", "Сергеевна", "Дмитриевич", "Александровна", "Сергеевич", "Ивановна", "Андреевич", "Михайловна",
            "Николаевич", "Владимировна", "Петрович", "Сергеевна", "Викторович", "Андреевна", "Михайлович", "Дмитриевна", "Павлович", "Алексеевна"
    };
    private static final String[] SUBJECTS = {
            "Математический анализ", "Программирование", "Физика", "Базы данных", "Алгоритмы", "Линейная алгебра", "Иностранный язык", "Физкультура",
            "Сети и телекоммуникации", "ОПРПП", "СУБД", "ТРКПО", "Нейронные сети", "Защита информации", "МФАЦИ", "Разработка ПО"
    };
    private static final String[] GROUPS_PREFIX = { "М-", "ИВТ-", "ПИ-", "ФИЗ-", "ЭК-" };
    /** Номера групп в формате направления/подгруппа (условные, не реальные). */
    private static final String[] GROUP_CODES = { "5140101/10101", "5140101/10102", "5140102/10201", "5140102/10202", "5140201/20101", "5140201/20102", "5130901/90101", "5130901/90102", "5130902/90201" };
    private static final String[] DAYS = { "Пн", "Вт", "Ср", "Чт", "Пт", "Сб" };
    private static final String[] TIMES = { "до 14:00", "после 14:00", "вторая половина дня", "утро", "любое" };
    private static final String[] LOAD_TYPES = { "compact", "even" };
    private static final String[] BOARD_TYPES = { "marker", "chalk", "digital" };
    private static final String[] FORMATS = { "in-person", "remote" };
    /** Варианты для столбца «Компьютеры» — как на сайте (ОС/тип ПК). */
    private static final String[][] COMPUTER_OPTIONS = {
            { "Windows" },
            { "Linux" },
            { "Windows", "Linux" },
            { "ПК в аудитории" }
    };
    /** Предпочтительные даты / исключить даты / новогодние — в основном для пожеланий по сессии. */
    private static final String[] PREF_DATES = { "до 20 января", "первая неделя сессии", "после 15 января" };
    private static final String[] AVOID_DATES = { "31 декабря", "1 января", "последний день сессии" };
    private static final String[] NEW_YEAR_PREF = { "без занятий 2–3 января", "перенос на другую неделю" };

    private SampleDataGenerator() {}

    /**
     * Генерирует список из {@code count} валидных записей с разнообразным заполнением полей.
     */
    public static List<SchedulePreferenceDto> generate(int count) {
        List<SchedulePreferenceDto> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(generateOne(i));
        }
        return list;
    }

    /**
     * Генерирует записи с минимальным заполнением: только обязательные поля (ФИО, тип) и по желанию логин.
     * Остальные поля пустые — для проверки импорта/экспорта «минимального» шаблона.
     */
    public static List<SchedulePreferenceDto> generateMinimal(int count) {
        List<SchedulePreferenceDto> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            SchedulePreferenceDto dto = new SchedulePreferenceDto();
            String surname = SURNAMES[i % SURNAMES.length];
            String name = NAMES[i % NAMES.length];
            String patronymic = PATRONYMICS[i % PATRONYMICS.length];
            dto.setTeacherName(surname + " " + name + " " + patronymic);
            dto.setTeacherLogin((surname + i).toLowerCase().replace("ё", "e"));
            dto.setType(i % 2 == 0 ? "semester" : "session");
            list.add(dto);
        }
        return list;
    }

    /**
     * Генерирует {@code totalCount} записей: часть полностью заполнены, у {@code warningCount} — ФИО одним словом
     * (при импорте будет предупреждение, строка остаётся в данных). Все записи валидны по типу и приоритетам.
     */
    public static List<SchedulePreferenceDto> generateWithWarnings(int totalCount, int warningCount) {
        if (warningCount > totalCount || warningCount < 0) {
            throw new IllegalArgumentException("warningCount должно быть от 0 до totalCount");
        }
        List<SchedulePreferenceDto> list = new ArrayList<>(totalCount);
        for (int i = 0; i < totalCount - warningCount; i++) {
            list.add(generateOne(i));
        }
        for (int i = 0; i < warningCount; i++) {
            SchedulePreferenceDto dto = generateOne(totalCount + i);
            dto.setTeacherName(SURNAMES[i % SURNAMES.length]); // одно слово — предупреждение
            list.add(dto);
        }
        return list;
    }

    /** Типы намеренных ошибок для отчёта (только проверки по обязательным/валидируемым полям). */
    private enum InvalidType {
        /** Недопустимый тип (semester|session). */
        TYPE_INVALID,
        /** Приоритет дней вне диапазона 0–5. */
        DAYS_PRIORITY_OUT_OF_RANGE,
        /** Приоритет времени отрицательный. */
        TIMES_PRIORITY_NEGATIVE,
        /** Приоритет нагрузки вне диапазона 0–5. */
        LOAD_PRIORITY_OUT_OF_RANGE,
        /** Приоритет доски вне диапазона 0–5. */
        BOARD_PRIORITY_OUT_OF_RANGE,
        /** Приоритет формата вне диапазона 0–5. */
        FORMAT_PRIORITY_OUT_OF_RANGE
    }

    /**
     * Генерирует {@code totalCount} записей: первые (totalCount - invalidCount) валидные,
     * последние {@code invalidCount} — с разными типами ошибок валидации (ФИО, тип, приоритеты).
     * Необязательные поля не заполняются ошибочно — в отчёт попадают только нарушения правил.
     */
    public static List<SchedulePreferenceDto> generateWithErrors(int totalCount, int invalidCount) {
        if (invalidCount >= totalCount || invalidCount < 1) {
            throw new IllegalArgumentException("invalidCount должно быть от 1 до totalCount-1");
        }
        List<SchedulePreferenceDto> list = new ArrayList<>(totalCount);
        int validCount = totalCount - invalidCount;
        for (int i = 0; i < validCount; i++) {
            list.add(generateOne(i));
        }
        InvalidType[] types = InvalidType.values();
        for (int i = 0; i < invalidCount; i++) {
            list.add(generateOneInvalid(validCount + i, types[i % types.length]));
        }
        return list;
    }

    private static SchedulePreferenceDto generateOneInvalid(int index, InvalidType invalidType) {
        SchedulePreferenceDto dto = generateOne(index);
        switch (invalidType) {
            case TYPE_INVALID -> {
                // Ошибка по типу + (дополнительно) ошибка по приоритету корпуса и предупреждение по ФИО.
                dto.setTeacherName("ОшибкаФИО" + index);
                dto.setType("year");
                dto.setBuildingRoomPriority(10);
            }
            case DAYS_PRIORITY_OUT_OF_RANGE -> {
                // Ошибка по приоритету дней + ошибка по приоритету комментария.
                dto.setDays(List.of("Пн"));
                dto.setDaysPriority(10); // вне шкалы 0–5
                dto.setComments("Тестовый комментарий");
                dto.setCommentsPriority(10);
            }
            case TIMES_PRIORITY_NEGATIVE -> {
                // Ошибка по приоритету времени + ошибка по приоритету компьютеров.
                dto.setTimes("до 14:00");
                dto.setTimesPriority(-1);
                dto.setComputers(List.of("Windows"));
                dto.setComputersPriority(10);
            }
            case LOAD_PRIORITY_OUT_OF_RANGE -> {
                dto.setLoadType("compact");
                dto.setLoadTypePriority(10);
            }
            case BOARD_PRIORITY_OUT_OF_RANGE -> {
                dto.setBoardType("marker");
                dto.setBoardTypePriority(10);
            }
            case FORMAT_PRIORITY_OUT_OF_RANGE -> {
                // Ошибка по приоритету формата + предупреждение по ФИО.
                dto.setTeacherName("ОшибкаФИО" + index);
                dto.setFormat("in-person");
                dto.setFormatPriority(10);
            }
        }
        return dto;
    }

    private static SchedulePreferenceDto generateOne(int index) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        SchedulePreferenceDto dto = new SchedulePreferenceDto();
        String surname = SURNAMES[index % SURNAMES.length];
        String name = NAMES[index % NAMES.length];
        String patronymic = PATRONYMICS[index % PATRONYMICS.length];
        dto.setTeacherName(surname + " " + name + " " + patronymic);
        dto.setTeacherLogin((surname + index).toLowerCase().replace("ё", "e"));
        boolean isSession = r.nextBoolean();
        dto.setType(isSession ? "session" : "semester");
        dto.setSubject(SUBJECTS[index % SUBJECTS.length]);
        // Группы в основном в формате 5140101/10102, 5140102/10201
        if (r.nextInt(5) != 0) {
            String g1 = GROUP_CODES[index % GROUP_CODES.length];
            String g2 = GROUP_CODES[(index + 1) % GROUP_CODES.length];
            dto.setGroups(g1 + ", " + g2);
        } else {
            String prefix = GROUPS_PREFIX[index % GROUPS_PREFIX.length];
            dto.setGroups(prefix + (100 + index % 20) + ", " + prefix + (101 + index % 20));
        }
        if (r.nextInt(3) != 0) {
            int n = 1 + r.nextInt(3);
            List<String> days = new ArrayList<>();
            for (int j = 0; j < n; j++) days.add(DAYS[r.nextInt(DAYS.length)]);
            dto.setDays(days);
            dto.setDaysPriority(r.nextInt(2) == 0 ? r.nextInt(6) : null); // 0–5
        }
        if (r.nextInt(2) != 0) {
            dto.setTimes(TIMES[r.nextInt(TIMES.length)]);
            dto.setTimesPriority(r.nextInt(2) == 0 ? r.nextInt(6) : null); // 0–5
        }
        dto.setLoadType(LOAD_TYPES[r.nextInt(LOAD_TYPES.length)]);
        dto.setLoadTypePriority(r.nextInt(2) == 0 ? r.nextInt(6) : null); // 0–5
        // Корпус от 1 до 11, как на сайте
        int buildingNum = 1 + index % 11;
        dto.setBuildingRoom("Корпус " + buildingNum + ", ауд. " + (100 + index % 50));
        dto.setBoardType(BOARD_TYPES[r.nextInt(BOARD_TYPES.length)]);
        // Компьютеры — как на сайте: Windows, Linux, Windows, Linux и т.д.
        if (r.nextInt(2) != 0) {
            String[] opts = COMPUTER_OPTIONS[index % COMPUTER_OPTIONS.length];
            dto.setComputers(Arrays.asList(opts));
            dto.setComputersPriority(r.nextInt(2) == 0 ? r.nextInt(6) : null); // 0–5
        }
        dto.setFormat(FORMATS[r.nextInt(FORMATS.length)]);
        // Предпочтительные даты, исключить даты, новогодние — в основном для сессии
        if (isSession && r.nextInt(2) != 0) {
            dto.setPreferredDates(PREF_DATES[index % PREF_DATES.length]);
            if (r.nextBoolean()) dto.setAvoidDates(AVOID_DATES[index % AVOID_DATES.length]);
            if (r.nextBoolean()) dto.setNewYearPref(NEW_YEAR_PREF[index % NEW_YEAR_PREF.length]);
        }
        if (r.nextInt(3) == 0) {
            dto.setComments("Комментарий для записи " + (index + 1));
            dto.setCommentsPriority(r.nextInt(2) == 0 ? r.nextInt(6) : null); // 0–5
        }
        return dto;
    }
}
