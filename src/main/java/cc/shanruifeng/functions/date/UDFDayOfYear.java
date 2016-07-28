package cc.shanruifeng.functions.date;

import java.util.Calendar;
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
 * @date 2016-07-27
 * @time 15:58
 */
@Description(name = "day_of_year"
        , value = "_FUNC_(date) - returns the day of the year from x. The value ranges from 1 to 366."
        , extended = "Example:\n > select _FUNC_(date_string) from src;\n > select _FUNC_(date) from src;")
public class UDFDayOfYear extends UDF {
    public final static DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    private IntWritable result = new IntWritable();

    public UDFDayOfYear() {

    }

    /**
     * Get the day of week from a date string.
     *
     * @param dateString the dateString in the format of "yyyy-MM-dd".
     * @return an int from 1 to 366
     * string.
     */
    public IntWritable evaluate(Text dateString) {
        if (dateString == null) {
            return null;
        }

        try {
            LocalDate date = LocalDate.parse(dateString.toString(), DEFAULT_DATE_FORMATTER);

            result.set(date.getDayOfYear());
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public IntWritable evaluate(TimestampWritable t) {
        if (t == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(t.getTimestamp());
        LocalDate date = LocalDate.fromCalendarFields(calendar);
        result.set(date.getDayOfYear());
        return result;
    }
}