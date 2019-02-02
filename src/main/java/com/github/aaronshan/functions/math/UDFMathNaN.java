package com.github.aaronshan.functions.math;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.DoubleWritable;

/**
 * @author ruifeng.shan
 * @date 18-7-23
 */
@Description(name = "NaN"
        , value = "_FUNC_() - constant representing not-a-number."
        , extended = "Example:\n > select _FUNC_() from src;")
public class UDFMathNaN extends UDF {
    private DoubleWritable result = new DoubleWritable();

    public UDFMathNaN() {
    }

    public DoubleWritable evaluate() {
        result.set(Double.NaN);
        return result;
    }
}