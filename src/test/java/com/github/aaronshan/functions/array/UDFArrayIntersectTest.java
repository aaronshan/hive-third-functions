package com.github.aaronshan.functions.array;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author ruifeng.shan
 * @date 2018-07-18
 * @time 13:00
 */
public class UDFArrayIntersectTest {
    @Test
    public void testArrayIntersect() throws HiveException {
        UDFArrayIntersect udf = new UDFArrayIntersect();

        ObjectInspector leftArrayOI = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
        ObjectInspector rightArrayOI = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
        ObjectInspector[] arguments = {leftArrayOI, rightArrayOI};

        udf.initialize(arguments);

        assertTrue(Iterables.elementsEqual(ImmutableList.of(1,2,5), evaluate(ImmutableList.of(0,1,2,3,4,5), ImmutableList.of(1,1,2,2,5,5), udf)));
        assertTrue(Iterables.elementsEqual(ImmutableList.of(1,2,3,4), evaluate(ImmutableList.of(0,1,2,3,4,4), ImmutableList.of(1,1,2,2,3,4), udf)));
        assertTrue(Iterables.elementsEqual(ImmutableList.of(1,2,3,4), evaluate(ImmutableList.of(0,1,1,2,3,4,4), ImmutableList.of(1,1,2,2,3,4), udf)));
    }

    private ArrayList<Object> evaluate(List<Integer> leftArray, List<Integer> rightArray, UDFArrayIntersect udf) throws HiveException {
        GenericUDF.DeferredObject leftArrayObj = new GenericUDF.DeferredJavaObject(leftArray);
        GenericUDF.DeferredObject rightArrayObj = new GenericUDF.DeferredJavaObject(rightArray);
        GenericUDF.DeferredObject[] args = {leftArrayObj, rightArrayObj};
        ArrayList<Object> output = (ArrayList<Object>) udf.evaluate(args);
        return output;
    }
}