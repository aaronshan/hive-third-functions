package com.github.aaronshan.functions.array;

import com.github.aaronshan.functions.fastuitl.ints.IntArrays;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;

import static com.github.aaronshan.functions.utils.ArrayUtils.IntArrayCompare;

/**
 * @author ruifeng.shan
 * @date 2016-07-26
 * @time 17:32
 */
@Description(name = "array_min"
        , value = "_FUNC_(array) - returns the minimum value of input array."
        , extended = "Example:\n > select _FUNC_(array) from src;")
public class UDFArrayMin extends GenericUDF {
    private static final int INITIAL_SIZE = 128;
    private static final int ARG_COUNT = 1; // Number of arguments to this UDF
    private int[] positions = new int[INITIAL_SIZE];
    private transient ListObjectInspector arrayOI;
    private transient ObjectInspector arrayElementOI;

    public UDFArrayMin() {
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function array_min(array) takes exactly " + ARG_COUNT + "arguments.");
        }

        // Check if two argument is of category LIST
        if (!arguments[0].getCategory().equals(ObjectInspector.Category.LIST)) {
            throw new UDFArgumentTypeException(0,
                    "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                            + "expected at function array_min, but "
                            + "\"" + arguments[0].getTypeName() + "\" "
                            + "is found");
        }

        arrayOI = (ListObjectInspector) arguments[0];
        arrayElementOI = arrayOI.getListElementObjectInspector();

        // Check if the comparison is supported for this type
        if (!ObjectInspectorUtils.compareSupported(arrayElementOI)) {
            throw new UDFArgumentException("The function array_min"
                    + " does not support comparison for "
                    + "\"" + arrayElementOI.getTypeName() + "\""
                    + " types");
        }

        return arrayElementOI;
    }

    @Override
    public Object evaluate(GenericUDF.DeferredObject[] arguments) throws HiveException {
        Object array = arguments[0].get();
        int arrayLength = arrayOI.getListLength(array);

        // Check if array is null or empty
        if (array == null || arrayLength <= 0) {
            return null;
        }

        if (arrayLength == 1) {
            return arrayOI.getListElement(array, positions[0]);
        }

        if (positions.length < arrayLength) {
            positions = new int[arrayLength];
        }

        for (int i = 0; i < arrayLength; i++) {
            positions[i] = i;
        }

        IntArrays.quickSort(positions, 0, arrayLength, IntArrayCompare(array, arrayOI));
        Object minArrayElement = arrayOI.getListElement(array, positions[0]);
        return minArrayElement;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "array_min(" + strings[0] + ")";
    }
}
