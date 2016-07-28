package cc.shanruifeng.functions.array;

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
 * @date 2015-3-23
 */
@Description(name = "array_contains"
        , value = "_FUNC_(array<E>, E) - whether array contains value or not."
        , extended = "Example:\n > select _FUNC_(array, value) from src;")
public class UDFArrayContains extends GenericUDF {

    private static final int ARRAY_IDX = 0;
    private static final int VALUE_IDX = 1;
    private static final int ARG_COUNT = 2; // Number of arguments to this UDF
    private transient ObjectInspector valueOI;
    private transient ListObjectInspector arrayOI;
    private transient ObjectInspector arrayElementOI;
    private BooleanWritable result;

    public UDFArrayContains() {

    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function array_contains(array, value) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if ARRAY_IDX argument is of category LIST
        if (!arguments[ARRAY_IDX].getCategory().equals(ObjectInspector.Category.LIST)) {
            throw new UDFArgumentTypeException(ARRAY_IDX,
                    "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                            + "expected at function array_contains, but "
                            + "\"" + arguments[ARRAY_IDX].getTypeName() + "\" "
                            + "is found");
        }

        arrayOI = (ListObjectInspector) arguments[ARRAY_IDX];
        arrayElementOI = arrayOI.getListElementObjectInspector();

        valueOI = arguments[VALUE_IDX];

        // Check if list element and value are of same type
        if (!ObjectInspectorUtils.compareTypes(arrayElementOI, valueOI)) {
            throw new UDFArgumentTypeException(VALUE_IDX,
                    "\"" + arrayElementOI.getTypeName() + "\""
                            + " expected at function array_contains, but "
                            + "\"" + valueOI.getTypeName() + "\""
                            + " is found");
        }

        // Check if the comparison is supported for this type
        if (!ObjectInspectorUtils.compareSupported(valueOI)) {
            throw new UDFArgumentException("The function array_contains"
                    + " does not support comparison for "
                    + "\"" + valueOI.getTypeName() + "\""
                    + " types");
        }

        result = new BooleanWritable(false);

        return PrimitiveObjectInspectorFactory.writableBooleanObjectInspector;
    }

    @Override
    // Returns <tt>true</tt> if this array contains the specified element.
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        result.set(false);

        Object array = arguments[ARRAY_IDX].get();
        Object value = arguments[VALUE_IDX].get();

        int arrayLength = arrayOI.getListLength(array);

        // Check if array is null or empty or value is null
        if (array == null) {
            return result;
        }

        if (value == null || arrayLength <= 0) {
            return result;
        }

        // Compare the value to each element of array until a match is found
        for (int i = 0; i < arrayLength; ++i) {
            Object listElement = arrayOI.getListElement(array, i);
            if (listElement != null) {
                if (ObjectInspectorUtils.compare(value, valueOI, listElement, arrayElementOI) == 0) {
                    result.set(true);
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "array_contains(" + strings[ARRAY_IDX] + ", "
                + strings[VALUE_IDX] + ")";
    }
}
