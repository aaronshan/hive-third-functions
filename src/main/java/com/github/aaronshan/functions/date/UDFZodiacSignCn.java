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
@Description(name = "zodiac_cn"
        , value = "_FUNC_(date) - from the input date string or separate month and day arguments, returns the sing of the Zodiac."
        , extended = "Example:\n > select _FUNC_(date_string) from src;\n > select _FUNC_(month, day) from src;")
public class UDFZodiacSignCn extends UDF {
    public final static DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    private Text result = new Text();

    public UDFZodiacSignCn() {
    }

    public Text evaluate(Text birthday) {
        if (birthday == null) {
            return null;
        }
        DateTime dateTime = null;
        try {
            dateTime = DateTime.parse(birthday.toString(), DEFAULT_DATE_FORMATTER);
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
        String[] zodiacArray = {"魔羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座"};
        int[] splitDay = {19, 18, 20, 20, 20, 21, 22, 22, 22, 22, 21, 21}; // 两个星座分割日
        int index = month;
        // 所查询日期在分割日之前，索引-1，否则不变
        if (day <= splitDay[month - 1]) {
            index = index - 1;
        } else if (month == 12) {
            index = 0;
        }
        // 返回索引指向的星座string
        return zodiacArray[index];
    }
}
