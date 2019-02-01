package com.github.aaronshan.functions.url;

import com.github.aaronshan.functions.date.UDFDayOfYear;
import org.apache.hadoop.io.Text;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author ruifeng.shan
 * @date 2016-07-28
 * @time 17:51
 */
public class UDFUrlEncodeTest {
    protected void runTest(String value, Text exp, UDFUrlEncode udf) {
        Text res = udf.evaluate(value);
        if (exp == null) {
            Assert.assertNull(res);
        } else {
            Assert.assertNotNull(res);
            Assert.assertEquals("url_encode test", exp.toString(), res.toString());
        }
    }

    @Test
    public void testUrlEncode() throws Exception {
        runTest("http://shanruifeng.cc/", new Text("http%3A%2F%2Fshanruifeng.cc%2F"), new UDFUrlEncode());
    }
}