package schedule.etl.config;

/**
 * Константы формата шаблона Excel (лист «Пожелания»).
 * Единый источник для reader и writer — совместимость с экспортом веб-сайта.
 */
public final class ExcelTemplateConstants {

    private ExcelTemplateConstants() {}

    /** Имя листа с пожеланиями. */
    public static final String SHEET_NAME = "Пожелания";

    /** Заголовки столбцов (порядок определяет индексы). */
    public static final String[] HEADERS = {
            "Преподаватель",      // 0
            "Логин",              // 1
            "Тип",                // 2
            "Предмет",            // 3
            "Группы",             // 4
            "Нежелательные дни",  // 5
            "Время",              // 6
            "Предпочтительные даты",  // 7
            "Исключить даты",     // 8
            "Новогодние пожелания",  // 9
            "Нагрузка",           // 10
            "Корпус/аудитория",   // 11
            "Доска",              // 12
            "Компьютеры",         // 13
            "Формат",             // 14
            "Комментарии"         // 15
    };

    public static final int COL_TEACHER = 0;
    public static final int COL_LOGIN = 1;
    public static final int COL_TYPE = 2;
    public static final int COL_SUBJECT = 3;
    public static final int COL_GROUPS = 4;
    public static final int COL_DAYS = 5;
    public static final int COL_TIMES = 6;
    public static final int COL_PREF_DATES = 7;
    public static final int COL_AVOID_DATES = 8;
    public static final int COL_NEW_YEAR = 9;
    public static final int COL_LOAD = 10;
    public static final int COL_BUILDING = 11;
    public static final int COL_BOARD = 12;
    public static final int COL_COMPUTERS = 13;
    public static final int COL_FORMAT = 14;
    public static final int COL_COMMENTS = 15;
}
