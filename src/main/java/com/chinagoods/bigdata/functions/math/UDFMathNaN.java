package com.chinagoods.bigdata.functions.math;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.DoubleWritable;

/**
 * @author ruifeng.shan
 * date: 18-7-23
 */
@Description(name = "NaN"
        , value = "_FUNC_() - constant representing not-a-number."
        , extended = "Example:\n > select _FUNC_() from src;")
public class UDFMathNaN extends UDF {
    /**
     * A constant holding a Not-a-Number (NaN) value of type
     * {@code double}. It is equivalent to the value returned by
     * {@code Double.longBitsToDouble(0x7ff8000000000000L)}.
     */
    public static final double NaN = 0.0d / 0.0;

    private DoubleWritable result = new DoubleWritable();

    public UDFMathNaN() {
    }

    public DoubleWritable evaluate() {
        result.set(NaN);
        return result;
    }
}