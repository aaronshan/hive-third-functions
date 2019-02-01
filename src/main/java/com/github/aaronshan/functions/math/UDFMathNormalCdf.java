package com.github.aaronshan.functions.math;

import org.apache.commons.math3.special.Erf;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.DoubleWritable;

import static com.github.aaronshan.functions.utils.Failures.checkCondition;

/**
 * @author ruifeng.shan
 * @date 2018-07-26
 * @time 23:03
 */
@Description(name = "normal_cdf"
        , value = "_FUNC_(mean, sd, v) - normal cdf given a mean, standard deviation, and value."
        , extended = "Example:\n > select _FUNC_(mean, sd, v) from src;")
public class UDFMathNormalCdf extends UDF {
    private DoubleWritable result = new DoubleWritable();

    public UDFMathNormalCdf() {
    }

    public DoubleWritable evaluate(double mean, double standardDeviation, double value) throws HiveException {
        checkCondition(standardDeviation > 0, "standardDeviation must > 0");
        result.set(0.5 * (1 + Erf.erf((value - mean) / (standardDeviation * Math.sqrt(2)))));
        return result;
    }
}