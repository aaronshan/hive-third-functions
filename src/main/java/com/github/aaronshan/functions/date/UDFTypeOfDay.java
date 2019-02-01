package com.github.aaronshan.functions.date;

import com.github.aaronshan.functions.utils.ConfigUtils;
import java.util.Calendar;
import java.util.Map;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.serde2.io.TimestampWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author ruifeng.shan
 * @date 15-9-1
 */
@Description(name = "type_of_day"
        , value = "_FUNC_(date) - get type of day in china. if normal festival, return 1; if weekend, return 2, if workday return 3, if weekend or festival but work, return 4; if error, return null."
        , extended = "Example:\n > select _FUNC_(date_string) from src;\n > select _FUNC_(date) from src;")
public class UDFTypeOfDay extends UDF {
    public final static DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    public final static Map<String, String> dayMap = ConfigUtils.getDayMap();
    private IntWritable result = new IntWritable();

    public UDFTypeOfDay() {

    }

    /**
     * Get whether is holiday or not.
     *
     * @param dateString the dateString in the format of "yyyyMMdd".
     * @return 1: 法定节假日, 2: 正常周末, 3: 正常工作日 4:攒假的工作日
     */
    public IntWritable evaluate(Text dateString) {
        if (dateString == null) {
            return null;
        }

        try {
            String value = dayMap.get(dateString.toString());
            if (DayType.HOLIDAY.getCode().equalsIgnoreCase(value)) {
                result.set(1);
            } else if (DayType.WORKDAY.getCode().equalsIgnoreCase(value)) {
                result.set(4);
            } else {
                LocalDate date = LocalDate.parse(dateString.toString(), DEFAULT_DATE_FORMATTER);
                if (date.getDayOfWeek() < 6) {
                    result.set(3);
                } else {
                    result.set(2);
                }
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public IntWritable evaluate(TimestampWritable t) {
        if (t == null) {
            return null;
        }

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(t.getTimestamp());
            LocalDate date = LocalDate.fromCalendarFields(calendar);
            String dateString = date.toString(DEFAULT_DATE_FORMATTER);

            return evaluate(new Text(dateString));
        } catch (Exception e) {
            return null;
        }
    }

    private enum DayType {
        HOLIDAY("holiday"), WORKDAY("workday");

        private String code;

        private DayType(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }
    }
}
