package com.github.aaronshan.functions.math;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.DoubleWritable;

/**
 * @author ruifeng.shan
 * @date 18-7-23
 */
@Description(name = "is_finite"
        , value = "_FUNC_(double) - test if value is finite."
        , extended = "Example:\n > select _FUNC_(double) from src;")
public class UDFMathIsFinite extends UDF {
    BooleanWritable result = new BooleanWritable();

    public UDFMathIsFinite() {
    }

    public BooleanWritable evaluate(DoubleWritable num) {
        if (num == null) {
            result.set(false);
        } else {
            result.set(Double.isFinite(num.get()));
        }
        return result;
    }
}