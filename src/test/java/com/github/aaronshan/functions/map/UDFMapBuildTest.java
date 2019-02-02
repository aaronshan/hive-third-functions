package com.github.aaronshan.functions.map;

import com.github.aaronshan.functions.utils.MapUtils;
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
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 22:23
 */
public class UDFMapBuildTest {
    @Test
    public void testMapBuild() throws Exception {
        UDFMapBuild udf = new UDFMapBuild();
        ObjectInspector keyArrayOI = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        ObjectInspector valueArrayOI = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        ObjectInspector[] arguments = {keyArrayOI, valueArrayOI};
        udf.initialize(arguments);

        List<String> keyArray = ImmutableList.of("key1", "key2", "key3");
        List<String> valueArray = ImmutableList.of("value1", "value2", "value3");
        DeferredObject keyArrayObj = new DeferredJavaObject(keyArray);
        DeferredObject valueArrayObj = new DeferredJavaObject(valueArray);
        DeferredObject[] args = {keyArrayObj, valueArrayObj};
        LinkedHashMap<String, String> output = (LinkedHashMap<String, String>) udf.evaluate(args);
        LinkedHashMap<String, String> expect = Maps.newLinkedHashMap();
        expect.putAll(ImmutableMap.<String, String>of("key1", "value1", "key2", "value2", "key3", "value3"));

        Assert.assertEquals("map_build() test", true, MapUtils.mapEquals(output, expect));
    }
}