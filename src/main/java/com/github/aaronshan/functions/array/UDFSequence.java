package cc.shanruifeng.functions.array;

import com.google.common.collect.Lists;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import static java.lang.Math.toIntExact;
import static cc.shanruifeng.functions.utils.Failures.checkCondition;
/**
 * @author aaron02
 * @date 2018-08-18 上午9:23
 */
@Description(name = "sequence"
        , value = "_FUNC_(start, stop) - Generate a sequence of integers from start to stop.\n" +
        "_FUNC_(start, stop, step) - Generate a sequence of integers from start to stop, incrementing by step."
        , extended = "Example:\n > select _FUNC_(1, 5) from src;\n > select _FUNC_(1, 9, 4) from src;\n" +
        " > select _FUNC_('2016-04-12', '2016-04-14') from src;")
public class UDFSequence extends UDF {
    public final static DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final long MAX_RESULT_ENTRIES = 10_000;

    public UDFSequence() {

    }

    public Object evaluate(LongWritable start, LongWritable stop) throws HiveException {
        return fixedWidthSequence(start.get(), stop.get(), stop.get() >= start.get() ? 1 : -1, Long.class);
    }

    public Object evaluate(LongWritable start, LongWritable stop, LongWritable step) throws HiveException {
        return fixedWidthSequence(start.get(), stop.get(), step.get(), Long.class);
    }

    public Object evaluate(Text start, Text stop, long step) throws HiveException {
        long startMillis = DateTime.parse(start.toString(), DEFAULT_DATE_FORMATTER).getMillis();
        long stopMillis = DateTime.parse(stop.toString(), DEFAULT_DATE_FORMATTER).getMillis();
        return fixedWidthSequence(startMillis, stopMillis, step, String.class);
    }

    private static Object fixedWidthSequence(long start, long stop, long step, Class type) throws HiveException {
        checkValidStep(start, stop, step);

        int length = toIntExact((stop - start) / step + 1L);
        checkMaxEntry(length);

        if (type == long.class || type == Long.class) {
            List<Long> result = Lists.newArrayList();
            for (long i = 0, value = start; i < length; ++i, value += step) {
                result.add(value);
            }
            return result;
        } else if (type == String.class){
            List<String> result = Lists.newArrayList();
            for (long i = 0, value = start; i < length; ++i, value += step) {
                DateTime dateTime = new DateTime(value);
                result.add(dateTime.toString(DEFAULT_DATE_FORMATTER));
            }
            return result;
        } else {
            throw new HiveException("Don't support this class type!" + type);
        }
    }

    private static void checkValidStep(long start, long stop, long step) throws HiveException {
        checkCondition(
                step != 0,
                "step must not be zero");
        checkCondition(
                step > 0 ? stop >= start : stop <= start,
                "sequence stop value should be greater than or equal to start value if step is greater than zero otherwise stop should be less than or equal to start");
    }

    private static void checkMaxEntry(int length) throws HiveException {
        checkCondition(
                length <= MAX_RESULT_ENTRIES,
                "result of sequence function must not have more than 10000 entries");
    }
}
