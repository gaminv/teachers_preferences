package schedule.etl.api;

import schedule.etl.model.SchedulePreferenceDto;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;

/**
 * Extract: reads schedule template Excel and returns raw rows.
 */
public interface ScheduleExcelReader {

    /**
     * Reads Excel (XLS/XLSX) and returns list of preference rows.
     * Sheet name: "Пожелания" or first sheet.
     */
    List<SchedulePreferenceDto> read(InputStream excelInput) throws IOException;
}
