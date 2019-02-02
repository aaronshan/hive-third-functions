package com.github.aaronshan.functions.math;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import static com.github.aaronshan.functions.utils.MathUtils.checkRadix;

/**
 * @author ruifeng.shan
 * @date 18-7-23
 */
@Description(name = "to_base"
        , value = "_FUNC_(long, long) - convert a number to a string in the given base."
        , extended = "Example:\n > select _FUNC_(long, long) from src;")
public class UDFMathToBase extends UDF {
    private Text result = new Text();

    public UDFMathToBase() {
    }

    public Text evaluate(LongWritable value, LongWritable radix) throws HiveException {
        if (value == null || radix == null) {
            return null;
        }

        checkRadix(radix.get());
        result.set(Long.toString(value.get(), (int) radix.get()));
        return result;
    }
}
