package com.chinagoods.bigdata.functions.math;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.DoubleWritable;

/**
 * @author ruifeng.shan
 * date: 18-7-23
 */
@Description(name = "is_infinite"
        , value = "_FUNC_(double) - test if value is infinite."
        , extended = "Example:\n > select _FUNC_(double) from src;")
public class UDFMathIsInfinite extends UDF {
    /**
     * A constant holding the positive infinity of type
     * {@code double}. It is equal to the value returned by
     * {@code Double.longBitsToDouble(0x7ff0000000000000L)}.
     */
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;

    /**
     * A constant holding the negative infinity of type
     * {@code double}. It is equal to the value returned by
     * {@code Double.longBitsToDouble(0xfff0000000000000L)}.
     */
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

    BooleanWritable result = new BooleanWritable();

    public UDFMathIsInfinite() {
    }

    public BooleanWritable evaluate(DoubleWritable num) {
        if (num == null) {
            result.set(false);
        } else {
            result.set(isInfinite(num.get()));
        }
        return result;
    }

    private boolean isInfinite(double v) {
        return (v == POSITIVE_INFINITY) || (v == NEGATIVE_INFINITY);
    }
}
