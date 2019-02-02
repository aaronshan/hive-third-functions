package com.github.aaronshan.functions.url;

import org.apache.hadoop.io.Text;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author ruifeng.shan
 * @date 2016-07-28
 * @time 17:57
 */
public class UDFUrlDecodeTest {
    protected void runTest(String value, Text exp, UDFUrlDecode udf) {
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
        runTest("http%3A%2F%2Fshanruifeng.cc%2F", new Text("http://shanruifeng.cc/"), new UDFUrlDecode());
    }
}