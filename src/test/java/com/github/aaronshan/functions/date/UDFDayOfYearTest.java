package com.github.aaronshan.functions.date;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 20:20
 */
public class UDFDayOfYearTest {
    @Test
    public void testDayOfYear() throws Exception {
        Text dateString = new Text("2016-01-01");
        UDFDayOfYear udf = new UDFDayOfYear();

        runTest(dateString, new IntWritable(1), udf);
    }

    protected void runTest(Text dateString, IntWritable exp, UDFDayOfYear udf) {
        IntWritable res = udf.evaluate(dateString);
        if (exp == null) {
            Assert.assertNull(res);
        } else {
            Assert.assertNotNull(res);
            Assert.assertEquals("day_of_year test", exp.get(), res.get());
        }
    }
}