package com.github.aaronshan.functions.string;

import com.github.aaronshan.functions.utils.MapUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class UDFStringSplitToMapTest {
    @Test
    public void testStringSplitToMap() throws Exception {
        UDFStringSplitToMap udf = new UDFStringSplitToMap();

        GenericUDF.DeferredObject string = new GenericUDF.DeferredJavaObject("a=123,b=0.4");
        GenericUDF.DeferredObject entryDelimiter = new GenericUDF.DeferredJavaObject(",");
        GenericUDF.DeferredObject keyValueDelimiter = new GenericUDF.DeferredJavaObject("=");
        GenericUDF.DeferredObject[] args = {string, entryDelimiter, keyValueDelimiter};

        HashMap<String, String> output = (HashMap<String, String>) udf.evaluate(args);

        HashMap<String, String> expect = Maps.newHashMap();
        expect.putAll(ImmutableMap.<String, String>of("a", "123", "b", "0.4"));

        Assert.assertEquals("split_to_map() test", true, MapUtils.mapEquals(output, expect));
    }
}