package com.github.aaronshan.functions.bitwise;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.LongWritable;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author ruifeng.shan
 * @date 2016-07-28
 * @time 14:15
 */
public class UDFBitCountTest {
    protected void runTest(long num, long bits, LongWritable exp, UDFBitCount udf) throws HiveException {
        LongWritable res = udf.evaluate(num, bits);
        if (exp == null) {
            Assert.assertNull(res);
        } else {
            Assert.assertNotNull(res);
            Assert.assertEquals("bit_count test", exp.get(), res.get());
        }
    }

    @Test
    public void testBitCount() throws Exception {
        runTest(9, 64, new LongWritable(2), new UDFBitCount());
        runTest(9, 8, new LongWritable(2), new UDFBitCount());
        runTest(-7, 64, new LongWritable(62), new UDFBitCount());
        runTest(-7, 8, new LongWritable(6), new UDFBitCount());
    }
}