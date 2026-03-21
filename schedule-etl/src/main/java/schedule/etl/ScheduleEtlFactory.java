package schedule.etl;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import schedule.etl.api.ScheduleExcelReader;
import schedule.etl.api.ScheduleExcelWriter;
import schedule.etl.extract.PoiScheduleExcelReader;
import schedule.etl.transform.ScheduleTransform;
import schedule.etl.write.PoiScheduleExcelWriter;

/**
 * Factory for ETL components (no DI container). Use this to build ScheduleEtlService.
 */
public final class ScheduleEtlFactory {

    private ScheduleEtlFactory() {}

    public static ScheduleExcelReader createReader() {
        return new PoiScheduleExcelReader();
    }

    public static ScheduleExcelWriter createWriter() {
        return new PoiScheduleExcelWriter();
    }

    public static ScheduleTransform createTransform() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        return new ScheduleTransform(validator);
    }

    public static ScheduleEtlService createService() {
        return new ScheduleEtlService(createReader(), createTransform(), createWriter());
    }
}
