package com.github.aaronshan.functions.map;

import com.github.aaronshan.functions.utils.MapUtils;
import com.github.aaronshan.functions.utils.MapUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredJavaObject;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 23:06
 */
public class UDFMapConcatTest {
    @Test
    public void testMapConcat() throws Exception {
        UDFMapConcat udf = new UDFMapConcat();
        ObjectInspector leftMapOI = ObjectInspectorFactory.getStandardMapObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector, PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        ObjectInspector rightMapOI = ObjectInspectorFactory.getStandardMapObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector, PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        ObjectInspector[] arguments = {leftMapOI, rightMapOI};
        udf.initialize(arguments);

        LinkedHashMap<String, String> leftMap = Maps.newLinkedHashMap();
        leftMap.putAll(ImmutableMap.<String, String>of("key1", "11", "key2", "12", "key3", "13"));
        LinkedHashMap<String, String> rightMap = Maps.newLinkedHashMap();
        rightMap.putAll(ImmutableMap.<String, String>of("key3", "21", "key4", "22", "key5", "23"));

        DeferredObject leftMapObj = new DeferredJavaObject(leftMap);
        DeferredObject rightMapObj = new DeferredJavaObject(rightMap);
        DeferredObject[] args = {leftMapObj, rightMapObj};
        LinkedHashMap<String, String> output = (LinkedHashMap<String, String>) udf.evaluate(args);
        LinkedHashMap<String, String> expect = Maps.newLinkedHashMap();
        expect.putAll(ImmutableMap.<String, String>of("key1", "11", "key2", "12", "key3", "21", "key4", "22", "key5", "23"));

        Assert.assertEquals("map_concat() test", true, MapUtils.mapEquals(output, expect));
    }
}