package com.github.aaronshan.functions.math;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.io.DoubleWritable;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class UDFMathCosineSimilarityTest {

    @Test
    public void testCosineSimilarity() throws HiveException {
        Double result = getResult(ImmutableMap.<String, Double>of("a", 1.0, "b", 2.0), ImmutableMap.<String, Double>of("c", 1.0, "b", 3.0));
        assertEquals(result, 2 * 3 / (Math.sqrt(5) * Math.sqrt(10)), 0.0);
        result = getResult(ImmutableMap.<String, Double>of("a", 1.0, "b", 2.0, "c", -1.0), ImmutableMap.<String, Double>of("c", 1.0, "b", 3.0));
        assertEquals(result, (2 * 3 + (-1) * 1) / (Math.sqrt(1 + 4 + 1) * Math.sqrt(1 + 9)), 0.0);
        result = getResult(ImmutableMap.<String, Double>of("a", 1.0, "b", 2.0, "c", -1.0), ImmutableMap.<String, Double>of("d", 1.0, "e", 3.0));
        assertEquals(result, 0.0, 0.0);
        result = getResult(null, ImmutableMap.<String, Double>of("c", 1.0, "b", 3.0));
        assertEquals(result, null);
        LinkedHashMap<String, Double> leftMap = Maps.newLinkedHashMap();
        leftMap.put("a", 1.0);
        leftMap.put("b", null);
        result = getResult(leftMap, ImmutableMap.<String, Double>of("c", 1.0, "b", 3.0));
        assertEquals(result, null);
    }

    public Double getResult(Map<String, Double> leftMap, Map<String, Double> rightMap) throws HiveException {
        UDFMathCosineSimilarity udf = new UDFMathCosineSimilarity();

        ObjectInspector leftMapOI = ObjectInspectorFactory.getStandardMapObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector, PrimitiveObjectInspectorFactory.javaDoubleObjectInspector);
        ObjectInspector rightMapOI = ObjectInspectorFactory.getStandardMapObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector, PrimitiveObjectInspectorFactory.javaDoubleObjectInspector);
        ObjectInspector[] arguments = {leftMapOI, rightMapOI};
        udf.initialize(arguments);

        GenericUDF.DeferredObject leftMapObj = new GenericUDF.DeferredJavaObject(leftMap);
        GenericUDF.DeferredObject rightMapObj = new GenericUDF.DeferredJavaObject(rightMap);
        GenericUDF.DeferredObject[] args = {leftMapObj, rightMapObj};
        DoubleWritable output = (DoubleWritable) udf.evaluate(args);
        return output == null ? null : output.get();
    }
}