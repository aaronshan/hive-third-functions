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
import org.apache.hadoop.io.LongWritable;

/**
 * @author ruifeng.shan
 * @date 2016-07-26
 * @time 17:32
 */
@Description(name = "array_position"
        , value = "_FUNC_(array<E>, E) - returns the position of the first occurrence of the element in array (or 0 if not found)."
        , extended = "Example:\n > select _FUNC_(array, value) from src;")
public class UDFArrayPosition extends GenericUDF {

    private static final int ARRAY_IDX = 0;
    private static final int VALUE_IDX = 1;
    private static final int ARG_COUNT = 2; // Number of arguments to this UDF
    private transient ObjectInspector valueOI;
    private transient ListObjectInspector arrayOI;
    private transient ObjectInspector arrayElementOI;
    private LongWritable result;

    public UDFArrayPosition() {

    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function array_position(array, value) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if ARRAY_IDX argument is of category LIST
        if (!arguments[ARRAY_IDX].getCategory().equals(ObjectInspector.Category.LIST)) {
            throw new UDFArgumentTypeException(ARRAY_IDX,
                    "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                            + "expected at function array_position, but "
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
                            + " expected at function array_position, but "
                            + "\"" + valueOI.getTypeName() + "\""
                            + " is found");
        }

        // Check if the comparison is supported for this type
        if (!ObjectInspectorUtils.compareSupported(valueOI)) {
            throw new UDFArgumentException("The function array_position"
                    + " does not support comparison for "
                    + "\"" + valueOI.getTypeName() + "\""
                    + " types");
        }

        result = new LongWritable(0L);

        return PrimitiveObjectInspectorFactory.writableLongObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        result.set(0L);

        Object array = arguments[ARRAY_IDX].get();
        Object value = arguments[VALUE_IDX].get();

        int arrayLength = arrayOI.getListLength(array);

        // Check if array is null or empty or value is null
        if (value == null || arrayLength <= 0) {
            return result;
        }

        // Compare the value to each element of array until a match is found
        for (int i = 0; i < arrayLength; ++i) {
            Object listElement = arrayOI.getListElement(array, i);
            if (ObjectInspectorUtils.compare(value, valueOI, listElement, arrayElementOI) == 0) {
                result.set(i + 1);
                break;
            }
        }

        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "array_position(" + strings[ARRAY_IDX] + ", "
                + strings[VALUE_IDX] + ")";
    }
}
