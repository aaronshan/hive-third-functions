package com.github.aaronshan.functions.map;

import com.github.aaronshan.functions.utils.MapUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredJavaObject;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 23:23
 */
public class UDFMapElementAtTest {
    @Test
    public void testMapElementAt() throws Exception {
        UDFMapElementAt udf = new UDFMapElementAt();
        ObjectInspector mapOI = ObjectInspectorFactory.getStandardMapObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector, PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        ObjectInspector keyOI = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        ObjectInspector[] arguments = {mapOI, keyOI};
        udf.initialize(arguments);

        LinkedHashMap<String, String> map = Maps.newLinkedHashMap();
        map.putAll(ImmutableMap.<String, String>of("key1", "11", "key2", "12", "key3", "13"));
        DeferredObject mapObj = new DeferredJavaObject(map);
        DeferredObject keyObj = new DeferredJavaObject("key1");
        DeferredObject[] args = {mapObj, keyObj};
        assertEquals("map_concat() test", "11", udf.evaluate(args));

        keyObj = new DeferredJavaObject("key4");
        DeferredObject[] args1 = {mapObj, keyObj};
        assertEquals("map_concat() test", null, udf.evaluate(args1));
    }
}