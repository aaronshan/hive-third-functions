package com.github.aaronshan.functions.array;

import com.google.common.collect.ImmutableList;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.LongWritable;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class UDFArrayValueCountTest {
    @Test
    public void testArrayValueCount() throws Exception {
        UDFArrayValueCount udf = new UDFArrayValueCount();

        ObjectInspector arrayOI = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        ObjectInspector valueOI = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        ObjectInspector[] arguments = {arrayOI, valueOI};

        udf.initialize(arguments);
        List<String> array = ImmutableList.of("a", "b", "c", "a");
        GenericUDF.DeferredObject arrayObj = new GenericUDF.DeferredJavaObject(array);
        GenericUDF.DeferredObject valueObj = new GenericUDF.DeferredJavaObject("a");
        GenericUDF.DeferredObject[] args = {arrayObj, valueObj};
        LongWritable output = (LongWritable) udf.evaluate(args);

        assertEquals("array_value_count() test", new LongWritable(2).get(), output.get());

        // Try with null args
        GenericUDF.DeferredObject[] nullArgs = { new GenericUDF.DeferredJavaObject(null), new GenericUDF.DeferredJavaObject(null) };
        output = (LongWritable) udf.evaluate(nullArgs);
        assertEquals("array_value_count() test", new LongWritable(0).get(), output.get());
    }
}