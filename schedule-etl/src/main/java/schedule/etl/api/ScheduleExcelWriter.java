package schedule.etl.api;

import schedule.etl.model.SchedulePreferenceDto;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Writes schedule preferences to Excel (reverse of Extract).
 */
public interface ScheduleExcelWriter {

    /**
     * Writes data to Excel (XLSX) onto the given output stream.
     */
    void write(List<SchedulePreferenceDto> data, OutputStream output) throws IOException;
}
