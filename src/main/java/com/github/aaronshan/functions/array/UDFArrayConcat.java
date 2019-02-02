package com.github.aaronshan.functions.array;

import java.util.ArrayList;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;

/**
 * @author ruifeng.shan
 * @date 2016-07-26
 * @time 17:30
 */
@Description(name = "array_concat"
        , value = "_FUNC_(array, array) - concatenates two arrays."
        , extended = "Example:\n > select _FUNC_(array, array) from src;")
public class UDFArrayConcat extends GenericUDF {
    private static final int ARG_COUNT = 2; // Number of arguments to this UDF
    private transient ListObjectInspector leftArrayOI;
    private transient ListObjectInspector rightArrayOI;
    private transient ObjectInspector leftArrayElementOI;
    private transient ObjectInspector rightArrayElementOI;

    private transient ArrayList<Object> result = new ArrayList<Object>();
    private transient ObjectInspectorConverters.Converter converter;

    public UDFArrayConcat() {
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function array_concat(array, array) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if two argument is of category LIST
        for (int i = 0; i < 2; i++) {
            if (!arguments[i].getCategory().equals(ObjectInspector.Category.LIST)) {
                throw new UDFArgumentTypeException(i,
                        "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                                + "expected at function array_concat, but "
                                + "\"" + arguments[i].getTypeName() + "\" "
                                + "is found");
            }
        }

        leftArrayOI = (ListObjectInspector) arguments[0];
        rightArrayOI = (ListObjectInspector) arguments[1];

        leftArrayElementOI = leftArrayOI.getListElementObjectInspector();
        rightArrayElementOI = rightArrayOI.getListElementObjectInspector();

        // Check if two array are of same type
        if (!ObjectInspectorUtils.compareTypes(leftArrayElementOI, rightArrayElementOI)) {
            throw new UDFArgumentTypeException(1,
                    "\"" + leftArrayElementOI.getTypeName() + "\""
                            + " expected at function array_concat, but "
                            + "\"" + rightArrayElementOI.getTypeName() + "\""
                            + " is found");
        }

        // Check if the comparison is supported for this type
        if (!ObjectInspectorUtils.compareSupported(leftArrayElementOI)) {
            throw new UDFArgumentException("The function array_concat"
                    + " does not support comparison for "
                    + "\"" + leftArrayElementOI.getTypeName() + "\""
                    + " types");
        }

        converter = ObjectInspectorConverters.getConverter(leftArrayElementOI, leftArrayElementOI);

        return ObjectInspectorFactory.getStandardListObjectInspector(leftArrayElementOI);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object leftArray = arguments[0].get();
        Object rightArray = arguments[1].get();

        int leftArrayLength = leftArrayOI.getListLength(leftArray);
        int rightArrayLength = rightArrayOI.getListLength(rightArray);

        // Check if array is null or empty
        if (leftArray == null || rightArray == null || leftArrayLength < 0 || rightArrayLength < 0) {
            return null;
        }

        if (leftArrayLength == 0) {
            return rightArray;
        }

        if (rightArrayLength == 0) {
            return leftArray;
        }

        result.clear();

        for (int i = 0; i < leftArrayLength; i++) {
            Object arrayElement = leftArrayOI.getListElement(leftArray, i);
            result.add(converter.convert(arrayElement));
        }

        for (int i = 0; i < rightArrayLength; i++) {
            Object arrayElement = rightArrayOI.getListElement(rightArray, i);
            result.add(converter.convert(arrayElement));
        }

        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "array_concat(" + strings[0] + ", "
                + strings[1] + ")";
    }
}
