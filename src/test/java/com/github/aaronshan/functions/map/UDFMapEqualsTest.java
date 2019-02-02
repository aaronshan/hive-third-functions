package com.github.aaronshan.functions.map;

import com.github.aaronshan.functions.utils.MapUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredJavaObject;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.BooleanWritable;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 23:42
 */
public class UDFMapEqualsTest {
    @Test
    public void testMapEquals() throws Exception {
        UDFMapEquals udf = new UDFMapEquals();
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
        BooleanWritable output = (BooleanWritable) udf.evaluate(args);

        assertEquals("map_concat() test", false, output.get());

        rightMap = Maps.newLinkedHashMap();
        rightMap.putAll(ImmutableMap.<String, String>of("key1", "11", "key2", "12", "key3", "13"));
        rightMapObj = new DeferredJavaObject(rightMap);
        DeferredObject[] args1 = {leftMapObj, rightMapObj};
        output = (BooleanWritable) udf.evaluate(args1);

        assertEquals("map_concat() test", true, output.get());
    }
}