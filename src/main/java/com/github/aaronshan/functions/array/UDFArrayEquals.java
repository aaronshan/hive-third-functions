package com.github.aaronshan.functions.array;

import com.github.aaronshan.functions.utils.ArrayUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.BooleanWritable;

/**
 * @author ruifeng.shan
 * @date 2016-09-08
 * @time 16:03
 */
@Description(name = "array_equals"
        , value = "_FUNC_(array, array) - whether two arrays equals or not."
        , extended = "Example:\n > select _FUNC_(array, array) from src;")
public class UDFArrayEquals extends GenericUDF {
    private static final int ARG_COUNT = 2; // Number of arguments to this UDF
    private transient ListObjectInspector leftArrayOI;
    private transient ListObjectInspector rightArrayOI;
    private transient ObjectInspector leftArrayElementOI;
    private transient ObjectInspector rightArrayElementOI;

    private BooleanWritable result;

    public UDFArrayEquals() {
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function array_equals(array, array) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if two argument is of category LIST
        for (int i = 0; i < 2; i++) {
            if (!arguments[i].getCategory().equals(ObjectInspector.Category.LIST)) {
                throw new UDFArgumentTypeException(i,
                        "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                                + "expected at function array_equals, but "
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
                            + " expected at function array_equals, but "
                            + "\"" + rightArrayElementOI.getTypeName() + "\""
                            + " is found");
        }

        // Check if the comparison is supported for this type
        if (!ObjectInspectorUtils.compareSupported(leftArrayElementOI)) {
            throw new UDFArgumentException("The function array_equals"
                    + " does not support comparison for "
                    + "\"" + leftArrayElementOI.getTypeName() + "\""
                    + " types");
        }

        result = new BooleanWritable(false);
        return PrimitiveObjectInspectorFactory.writableBooleanObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object leftArray = arguments[0].get();
        Object rightArray = arguments[1].get();

        boolean ret = ArrayUtils.arrayEquals(leftArray, rightArray, leftArrayOI);
        result.set(ret);

        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "array_equals(" + strings[0] + ", "
                + strings[1] + ")";
    }
}