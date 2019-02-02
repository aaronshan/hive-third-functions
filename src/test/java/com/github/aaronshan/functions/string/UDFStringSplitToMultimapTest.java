package com.github.aaronshan.functions.string;

import com.github.aaronshan.functions.utils.MapUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class UDFStringSplitToMultimapTest {
    @Test
    public void testStringSplitToMultimap() throws Exception {
        UDFStringSplitToMultimap udf = new UDFStringSplitToMultimap();

        GenericUDF.DeferredObject string = new GenericUDF.DeferredJavaObject("a=123,b=0.4,a=124");
        GenericUDF.DeferredObject entryDelimiter = new GenericUDF.DeferredJavaObject(",");
        GenericUDF.DeferredObject keyValueDelimiter = new GenericUDF.DeferredJavaObject("=");
        GenericUDF.DeferredObject[] args = {string, entryDelimiter, keyValueDelimiter};

        HashMap<String, List<String>> output = (HashMap<String, List<String>>) udf.evaluate(args);

        HashMap<String, List<String>> expect = Maps.newHashMap();
        expect.putAll(ImmutableMap.<String, List<String>>of("a", ImmutableList.<String>of("123", "124"), "b", ImmutableList.<String>of("0.4")));

        Assert.assertEquals("split_to_multimap() test", true, MapUtils.mapEquals(output, expect));
    }
}