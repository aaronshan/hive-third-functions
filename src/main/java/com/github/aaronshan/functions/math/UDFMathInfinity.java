package cc.shanruifeng.functions.math;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.DoubleWritable;

/**
 * @author ruifeng.shan
 * @date 18-7-23
 */
@Description(name = "infinity"
        , value = "_FUNC_() - Infinity."
        , extended = "Example:\n > select _FUNC_() from src;")
public class UDFMathInfinity extends UDF {
    private DoubleWritable result = new DoubleWritable();

    public UDFMathInfinity() {
    }

    public DoubleWritable evaluate() {
        result.set(Double.POSITIVE_INFINITY);
        return result;
    }
}