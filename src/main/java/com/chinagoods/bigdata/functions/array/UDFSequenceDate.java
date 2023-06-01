package com.chinagoods.bigdata.functions.array;

import com.chinagoods.bigdata.functions.utils.Failures;
import com.google.common.collect.Lists;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.Text;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

/**
 * @author xiaowei.song
 * date: 2021-09-04 上午9:23
 */
@Description(name = "sequence_date"
        , value = "_FUNC_(start, stop, step) - Generate a sequence of integers from start to stop, incrementing by step."
        , extended = "Example:\n > select _FUNC_('2016-04-12', '2016-04-14') from src; \n" +
        " > select _FUNC_('2016-04-12 00:00:00', '2016-04-14 00:00:00', 86400000) from src;")
public class UDFSequenceDate extends UDF {
    public final static DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public final static DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final long MAX_RESULT_ENTRIES = 10000;
    private static final long MAX_1_DAY_MS = 24 * 3600 * 1000;

    public UDFSequenceDate() {

    }

    public ArrayList<Text> evaluate(Text start, Text stop) throws HiveException {
        DateTimeFormatter format = exactFormat(start, stop);
        long startMillis = DateTime.parse(start.toString(), format).getMillis();
        long stopMillis = DateTime.parse(stop.toString(), format).getMillis();
        return fixedWidthSequence(startMillis, stopMillis, MAX_1_DAY_MS, format);
    }

    public ArrayList<Text> evaluate(Text start, Text stop, long step) throws HiveException {
        DateTimeFormatter format = exactFormat(start, stop);
        long startMillis = DateTime.parse(start.toString(), format).getMillis();
        long stopMillis = DateTime.parse(stop.toString(), format).getMillis();
        return fixedWidthSequence(startMillis, stopMillis, step, format);
    }

    public static DateTimeFormatter exactFormat(Text start, Text stop) throws HiveException {
        Failures.checkCondition(
                start.toString().length() == stop.toString().length(),
                "The two input formats are inconsistent, please check whether the two inputs are consistent.");
        DateTimeFormatter format = UDFSequenceDate.DEFAULT_DATE_TIME_FORMATTER;
        if (start.toString().length() == 10) {
            format = UDFSequenceDate.DEFAULT_DATE_FORMATTER;
        }
        return format;
    }

    public static int toIntExact(long value) {
        if ((int)value != value) {
            throw new ArithmeticException("integer overflow");
        }
        return (int)value;
    }

    private static ArrayList<Text> fixedWidthSequence(long start, long stop, long step, DateTimeFormatter format) throws HiveException {
        checkValidStep(start, stop, step);

        int length = toIntExact((stop - start) / step + 1L);
        checkMaxEntry(length);

        ArrayList<Text> result = Lists.newArrayList();
        for (long i = 0, value = start; i < length; ++i, value += step) {
            DateTime dateTime = new DateTime(value);
            result.add(new Text(dateTime.toString(format)));
        }
        return result;
    }

    private static void checkValidStep(long start, long stop, long step) throws HiveException {
        Failures.checkCondition(
                step != 0,
                "step must not be zero");
        Failures.checkCondition(
                step > 0 ? stop >= start : stop <= start,
                "sequence stop value should be greater than or equal to start value if step is greater than zero otherwise stop should be less than or equal to start");
    }

    private static void checkMaxEntry(int length) throws HiveException {
        Failures.checkCondition(
                length <= MAX_RESULT_ENTRIES,
                "result of sequence function must not have more than 10000 entries");
    }

    public static void main(String[] args) throws HiveException {
        UDFSequenceDate sequence = new UDFSequenceDate();
//        System.out.println(sequence.evaluate(new Text("2016-04-12 00:00:00"), new Text("2016-04-14 00:00:00"), 86400000));
        System.out.println(sequence.evaluate(new Text("2016-04-12"), new Text("2016-04-14")));
    }
}
