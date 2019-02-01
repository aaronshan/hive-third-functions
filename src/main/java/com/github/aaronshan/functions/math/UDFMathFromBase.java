package cc.shanruifeng.functions.math;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import static cc.shanruifeng.functions.utils.MathUtils.checkRadix;
import static java.lang.String.format;

/**
 * @author ruifeng.shan
 * @date 18-7-23
 */
@Description(name = "from_base"
        , value = "_FUNC_(string, long) - convert a number to a string in the given base."
        , extended = "Example:\n > select _FUNC_(string, long) from src;")
public class UDFMathFromBase extends UDF {
    private LongWritable result = new LongWritable();

    public UDFMathFromBase() {
    }

    public LongWritable evaluate(Text value, LongWritable radix) throws HiveException {
        if (value == null || radix == null) {
            return null;
        }

        checkRadix(radix.get());
        try {
            result.set(Long.parseLong(value.toString(), (int) radix.get()));
        }
        catch (NumberFormatException e) {
            throw new HiveException(format("Not a valid base-%d number: %s", radix, value.toString()), e);
        }
        return result;
    }
}
