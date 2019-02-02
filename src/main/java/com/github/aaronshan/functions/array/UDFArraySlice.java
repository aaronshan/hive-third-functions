package com.github.aaronshan.functions.array;

import java.util.ArrayList;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.IntWritable;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 10:24
 */
@Description(name = "array_slice"
        , value = "_FUNC_(array<E>, start, length) - subsets array starting from index start (or starting from the end if start is negative) with a length of length."
        , extended = "Example:\n > select _FUNC_(array, start, length) from src;")
public class UDFArraySlice extends GenericUDF {

    private static final int ARRAY_IDX = 0;
    private static final int START_IDX = 1;
    private static final int LENGTH_IDX = 2;
    private static final int ARG_COUNT = 3;
    private transient ListObjectInspector arrayOI;
    private transient ObjectInspector arrayElementOI;
    private transient ArrayList<Object> result = new ArrayList<Object>();

    public UDFArraySlice() {

    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function array_slice(array, start, length) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if ARRAY_IDX argument is of category LIST
        if (!arguments[ARRAY_IDX].getCategory().equals(ObjectInspector.Category.LIST)) {
            throw new UDFArgumentTypeException(ARRAY_IDX,
                    "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                            + "expected at function array_slice, but "
                            + "\"" + arguments[ARRAY_IDX].getTypeName() + "\" "
                            + "is found");
        }

        arrayOI = (ListObjectInspector) arguments[ARRAY_IDX];
        arrayElementOI = arrayOI.getListElementObjectInspector();

        ObjectInspector expectOI = PrimitiveObjectInspectorFactory.writableIntObjectInspector;

        // Check if value and expect are of same type
        for (int i = 1; i < 3; i++) {
            if (!ObjectInspectorUtils.compareTypes(expectOI, arguments[i])) {
                throw new UDFArgumentTypeException(i,
                        "\"" + expectOI.getTypeName() + "\""
                                + " expected at function array_slice, but "
                                + "\"" + arguments[i].getTypeName() + "\""
                                + " is found");
            }
        }

        return ObjectInspectorFactory.getStandardListObjectInspector(arrayElementOI);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object array = arguments[ARRAY_IDX].get();
        IntWritable start = (IntWritable) arguments[START_IDX].get();
        IntWritable length = (IntWritable) arguments[LENGTH_IDX].get();

        int arrayLength = arrayOI.getListLength(array);

        if (start == null || length == null || length.get() < 0) {
            return null;
        }

        if (arrayLength <= 0) {
            return array;
        }

        result.clear();
        if (start.get() < 0) {
            int idx = arrayLength + start.get();
            if (idx < 0) {
                idx = 0;
            }
            for (int i = idx, j = 1; j <= length.get() && i < arrayLength; i++, j++) {
                result.add(arrayOI.getListElement(array, i));
            }
            return result;
        } else {
            for (int i = start.get(); i <= length.get() && i < arrayLength; i++) {
                result.add(arrayOI.getListElement(array, i));
            }
            return result;
        }
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "array_slice(" + strings[ARRAY_IDX] + ", "
                + strings[START_IDX] + ","
                + strings[LENGTH_IDX] + ")";
    }
}