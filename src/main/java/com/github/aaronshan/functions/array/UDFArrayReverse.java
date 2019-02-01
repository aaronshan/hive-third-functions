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
 * @time 18:03
 */
@Description(name = "array_reverse"
        , value = "_FUNC_(array) - reverse the array element."
        , extended = "Example:\n > select _FUNC_(array) from src;")
public class UDFArrayReverse extends GenericUDF {
    private static final int ARG_COUNT = 1; // Number of arguments to this UDF
    private transient ListObjectInspector arrayOI;
    private transient ObjectInspector arrayElementOI;

    private transient ObjectInspectorConverters.Converter converter;
    private transient ArrayList<Object> result = new ArrayList<Object>();

    public UDFArrayReverse() {
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function array_reverse(array) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if two argument is of category LIST
        if (!arguments[0].getCategory().equals(ObjectInspector.Category.LIST)) {
            throw new UDFArgumentTypeException(0,
                    "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                            + "expected at function array_reverse, but "
                            + "\"" + arguments[0].getTypeName() + "\" "
                            + "is found");
        }

        arrayOI = (ListObjectInspector) arguments[0];
        arrayElementOI = arrayOI.getListElementObjectInspector();

        // Check if the comparison is supported for this type
        if (!ObjectInspectorUtils.compareSupported(arrayElementOI)) {
            throw new UDFArgumentException("The function array_reverse"
                    + " does not support comparison for "
                    + "\"" + arrayElementOI.getTypeName() + "\""
                    + " types");
        }

        converter = ObjectInspectorConverters.getConverter(arrayElementOI, arrayElementOI);

        return ObjectInspectorFactory.getStandardListObjectInspector(arrayElementOI);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object array = arguments[0].get();
        int arrayLength = arrayOI.getListLength(array);

        // Check if array is null or empty
        if (array == null || arrayLength <= 0) {
            return null;
        }

        if (arrayLength == 1) {
            return array;
        }

        result.clear();
        for (int i = arrayLength - 1; i >= 0; i--) {
            Object arrayElement = arrayOI.getListElement(array, i);
            result.add(arrayElement);
        }
        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "array_reverse(" + strings[0] + ")";
    }
}