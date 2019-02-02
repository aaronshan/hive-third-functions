package com.github.aaronshan.functions.date;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.serde2.io.TimestampWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;

/**
 * @author ruifeng.shan
 * @date 15-8-31
 */
@Description(name = "day_of_week"
        , value = "_FUNC_(date) - day of week. if monday, return 1, tuesday return 2 ... sunday return 7."
        , extended = "Example:\n > select _FUNC_(date_string) from src;\n > select _FUNC_(date) from src;")
public class UDFDayOfWeek extends UDF {
    public final static DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    private IntWritable result = new IntWritable();

    public UDFDayOfWeek() {

    }

    /**
     * Get the day of week from a date string.
     *
     * @param dateString the dateString in the format of "yyyy-MM-dd".
     * @return an int from 0 to 6(0 = Monday, 1 = Tuesday, … 6 = Sunday). null if the dateString is not a valid date
     * string.
     */
    public IntWritable evaluate(Text dateString) {
        if (dateString == null) {
            return null;
        }

        try {
            LocalDate date = LocalDate.parse(dateString.toString(), DEFAULT_DATE_FORMATTER);

            result.set(date.getDayOfWeek());
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
        result.set(date.getDayOfWeek());
        return result;
    }

}
