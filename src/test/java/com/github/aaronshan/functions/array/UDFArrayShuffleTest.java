package com.github.aaronshan.functions.array;

import com.github.aaronshan.functions.array.UDFArrayShuffle;
import com.google.common.collect.ImmutableList;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Test;

import java.util.List;

/**
 * @author aaronshan
 * @date 2018-08-18 上午8:59
 */
public class UDFArrayShuffleTest {
    @Test
    public void testArrayShuffle() throws HiveException {
        UDFArrayShuffle udf = new UDFArrayShuffle();

        ObjectInspector arrayOI = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
        ObjectInspector[] arguments = {arrayOI};

        udf.initialize(arguments);

        List<Integer> array = ImmutableList.of(1,2,5,6);
        GenericUDF.DeferredObject arrayObj = new GenericUDF.DeferredJavaObject(array);
        GenericUDF.DeferredObject[] args = {arrayObj};
        System.out.println(udf.evaluate(args));
    }
}
