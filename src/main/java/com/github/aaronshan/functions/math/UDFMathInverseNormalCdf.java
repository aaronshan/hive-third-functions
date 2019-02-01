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
 * @time 23:04
 */
@Description(name = "inverse_normal_cdf"
        , value = "_FUNC_(mean, sd, p) - inverse of normal cdf given a mean, std, and probability."
        , extended = "Example:\n > select _FUNC_(mean, sd, p) from src;")
public class UDFMathInverseNormalCdf extends UDF {
    private DoubleWritable result = new DoubleWritable();

    public UDFMathInverseNormalCdf() {
    }

    public DoubleWritable evaluate(double mean, double sd, double p) throws HiveException {
        checkCondition(p > 0 && p < 1, "p must be 0 > p > 1");
        checkCondition(sd > 0, "sd must > 0");

        result.set(mean + sd * 1.4142135623730951 * Erf.erfInv(2 * p - 1));
        return result;
    }
}