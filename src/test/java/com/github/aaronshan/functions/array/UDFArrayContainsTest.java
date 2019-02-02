package com.github.aaronshan.functions.array;

import com.google.common.collect.ImmutableList;
import java.util.List;
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
 * @time 20:29
 */
public class UDFArrayContainsTest {
    @Test
    public void testArrayContains() throws Exception {
        UDFArrayContains udf = new UDFArrayContains();

        ObjectInspector arrayOI = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        ObjectInspector valueOI = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        ObjectInspector[] arguments = {arrayOI, valueOI};

        udf.initialize(arguments);
        List<String> array = ImmutableList.of("a", "b", "c");
        DeferredObject arrayObj = new DeferredJavaObject(array);
        DeferredObject valueObj = new DeferredJavaObject("a");
        DeferredObject[] args = {arrayObj, valueObj};
        BooleanWritable output = (BooleanWritable) udf.evaluate(args);

        assertEquals("array_contains() test", new BooleanWritable(true).get(), output.get());

        // Try with null args
        DeferredObject[] nullArgs = { new DeferredJavaObject(null), new DeferredJavaObject(null) };
        output = (BooleanWritable) udf.evaluate(nullArgs);
        assertEquals("array_contains() test", new BooleanWritable(false).get(), output.get());
    }
}