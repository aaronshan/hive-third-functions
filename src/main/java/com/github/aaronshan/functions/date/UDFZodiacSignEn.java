package com.github.aaronshan.functions.date;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by ruifengshan on 16/3/18.
 */
@Description(name = "zodiac_en"
        , value = "_FUNC_(date) - from the input date string or separate month and day arguments, returns the sing of the Zodiac."
        , extended = "Example:\n > select _FUNC_(date_string) from src;\n > select _FUNC_(month, day) from src;")
public class UDFZodiacSignEn extends UDF {
    public final static DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    private Text result = new Text();

    public UDFZodiacSignEn() {
    }

    public Text evaluate(String birthday) {
        if (birthday == null) {
            return null;
        }
        DateTime dateTime = null;
        try {
            dateTime = DateTime.parse(birthday, DEFAULT_DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }

        return evaluate(dateTime.toDate());
    }

    public Text evaluate(Date birthday) {
        if (birthday == null) {
            return null;
        }
        DateTime dateTime = new DateTime(birthday);
        return evaluate(new IntWritable(dateTime.getMonthOfYear()), new IntWritable(dateTime.getDayOfMonth()));
    }

    public Text evaluate(IntWritable month, IntWritable day) {
        if (month == null || day == null) {
            return null;
        }
        result.set(getZodiac(month.get(), day.get()));
        return result;
    }

    private String getZodiac(int month, int day) {
        String[] zodiacArray = {"Capricorn", "Aquarius", "Pisces", "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius"};
        int[] splitDay = {19, 18, 20, 20, 20, 21, 22, 22, 22, 22, 21, 21}; // split day
        int index = month;
        if (day <= splitDay[month - 1]) {
            index = index - 1;
        } else if (month == 12) {
            index = 0;
        }
        return zodiacArray[index];
    }
}