package com.github.aaronshan.functions.math;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.DoubleWritable;

/**
 * @author ruifeng.shan
 * @date 18-7-23
 */
@Description(name = "is_nan"
        , value = "_FUNC_(double) - test if value is nan."
        , extended = "Example:\n > select _FUNC_(double) from src;")
public class UDFMathIsNaN extends UDF {
    BooleanWritable result = new BooleanWritable();

    public UDFMathIsNaN() {
    }

    public BooleanWritable evaluate(DoubleWritable num) {
        if (num == null) {
            result.set(false);
        } else {
            result.set(Double.isNaN(num.get()));
        }
        return result;
    }
}