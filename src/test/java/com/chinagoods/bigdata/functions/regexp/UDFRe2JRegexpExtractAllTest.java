package com.chinagoods.bigdata.functions.regexp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class UDFRe2JRegexpExtractAllTest {
    @Test
    public void testUDFRe2JRegexpExtractAll() throws HiveException {
        UDFRe2JRegexpExtractAll udf = new UDFRe2JRegexpExtractAll();

        ObjectInspector source = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        ObjectInspector pattern = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        ObjectInspector[] arguments = {source, pattern};

        udf.initialize(arguments);

        GenericUDF.DeferredObject sourceObj = new GenericUDF.DeferredJavaObject("1a 2b 3c 6f");
        GenericUDF.DeferredObject patternObj = new GenericUDF.DeferredJavaObject("\\d+");
        GenericUDF.DeferredObject[] args = {sourceObj, patternObj};

        ArrayList<Object> output = (ArrayList<Object>) udf.evaluate(args);
        assertTrue(Iterables.elementsEqual(ImmutableList.of("1", "2", "3", "6"), output));
    }
}