package com.github.aaronshan.functions.geo;

import org.apache.hadoop.io.Text;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author ruifeng.shan
 * @date 2016-07-28
 * @time 17:39
 */
public class UDFGeoBdToGcjTest {
    protected void runTest(Text expect, double lat, double lng, UDFGeoBdToGcj udf) {
        Text res = udf.evaluate(lat, lng);
        Assert.assertNotNull(res);
        Assert.assertEquals("bd_to_gcj test", expect.toString(), res.toString());
    }

    @Test
    public void testBdToGcj() throws Exception {
        runTest(new Text("{\"lng\":116.39762729119315,\"lat\":39.90865673957631}"), 39.915, 116.404, new UDFGeoBdToGcj());
    }
}