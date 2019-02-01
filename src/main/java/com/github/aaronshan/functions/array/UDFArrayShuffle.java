package cc.shanruifeng.functions.array;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author aaron02
 * @date 2018-08-18 上午8:52
 */
@Description(name = "array_shuffle"
        , value = "_FUNC_(array<E>) - Generates a random permutation of the given array."
        , extended = "Example:\n > select _FUNC_(array) from src;")
public class UDFArrayShuffle extends GenericUDF {
    private static final int ARG_COUNT = 1; // Number of arguments to this UDF
    private transient ListObjectInspector arrayOI;
    private transient ObjectInspector arrayElementOI;

    private transient ObjectInspectorConverters.Converter converter;
    private transient ArrayList<Object> result = new ArrayList<Object>();

    private static final int INITIAL_LENGTH = 128;
    private int[] positions = new int[INITIAL_LENGTH];

    public UDFArrayShuffle() {
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function array_shuffle(array) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if two argument is of category LIST
        if (!arguments[0].getCategory().equals(ObjectInspector.Category.LIST)) {
            throw new UDFArgumentTypeException(0,
                    "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                            + "expected at function array_shuffle, but "
                            + "\"" + arguments[0].getTypeName() + "\" "
                            + "is found");
        }

        arrayOI = (ListObjectInspector) arguments[0];
        arrayElementOI = arrayOI.getListElementObjectInspector();

        // Check if the comparison is supported for this type
        if (!ObjectInspectorUtils.compareSupported(arrayElementOI)) {
            throw new UDFArgumentException("The function array_shuffle"
                    + " does not support comparison for "
                    + "\"" + arrayElementOI.getTypeName() + "\""
                    + " types");
        }

        converter = ObjectInspectorConverters.getConverter(arrayElementOI, arrayElementOI);

        return ObjectInspectorFactory.getStandardListObjectInspector(arrayElementOI);
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
            return array;
        }

        result.clear();

        if (positions.length < arrayLength) {
            positions = new int[arrayLength];
        }
        for (int i = 0; i < arrayLength; i++) {
            positions[i] = i;
        }

        // Fisher-Yates shuffle
        // Randomly swap a pair of positions
        for (int i = arrayLength - 1; i > 0; i--) {
            int index = ThreadLocalRandom.current().nextInt(i + 1);
            int swap = positions[i];
            positions[i] = positions[index];
            positions[index] = swap;
        }

        for (int i = 0; i < arrayLength; i++) {
            Object arrayElement = arrayOI.getListElement(array, positions[i]);
            result.add(arrayElement);
        }
        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "array_shuffle(" + strings[0] + ")";
    }
}