package com.chinagoods.bigdata.functions.array;

import com.chinagoods.bigdata.functions.utils.Failures;
import com.google.common.collect.Lists;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.LongWritable;

import java.util.ArrayList;

/**
 * @author aaron02
 * date: 2018-08-18 上午9:23
 */
@Description(name = "sequence"
        , value = "_FUNC_(start, stop) - Generate a sequence of integers from start to stop.\n" +
        "_FUNC_(start, stop, step) - Generate a sequence of integers from start to stop, incrementing by step."
        , extended = "Example:\n > select _FUNC_(1, 5) from src;\n > select _FUNC_(1, 9, 4) from src;\n")
public class UDFSequence extends UDF {
    private static final long MAX_RESULT_ENTRIES = 10000;

    public UDFSequence() {

    }

    public ArrayList<Long> evaluate(LongWritable start, LongWritable stop) throws HiveException {
        return fixedWidthSequence(start.get(), stop.get(), stop.get() >= start.get() ? 1 : -1);
    }

    public ArrayList<Long> evaluate(LongWritable start, LongWritable stop, LongWritable step) throws HiveException {
        return fixedWidthSequence(start.get(), stop.get(), step.get());
    }

    public static int toIntExact(long value) {
        if ((int)value != value) {
            throw new ArithmeticException("integer overflow");
        }
        return (int)value;
    }

    private static ArrayList<Long> fixedWidthSequence(long start, long stop, long step) throws HiveException {
        checkValidStep(start, stop, step);

        int length = toIntExact((stop - start) / step + 1L);
        checkMaxEntry(length);
        ArrayList<Long> result = Lists.newArrayList();
        for (long i = 0, value = start; i < length; ++i, value += step) {
            result.add(value);
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
}
