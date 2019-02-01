package com.github.aaronshan.functions.array;

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
import org.apache.hadoop.io.IntWritable;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 10:09
 */
@Description(name = "array_element_at"
        , value = "_FUNC_(array<E>, index) - returns element of array at given index. If index < 0, element_at accesses elements from the last to the first."
        , extended = "Example:\n > select _FUNC_(array, index) from src;")
public class UDFArrayElementAt extends GenericUDF {

    private static final int ARRAY_IDX = 0;
    private static final int INDEX_IDX = 1;
    private static final int ARG_COUNT = 2; // Number of arguments to this UDF
    private transient ObjectInspector indexOI;
    private transient ListObjectInspector arrayOI;
    private transient ObjectInspector arrayElementOI;

    public UDFArrayElementAt() {

    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function array_element_at(array, index) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if ARRAY_IDX argument is of category LIST
        if (!arguments[ARRAY_IDX].getCategory().equals(ObjectInspector.Category.LIST)) {
            throw new UDFArgumentTypeException(ARRAY_IDX,
                    "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                            + "expected at function array_element_at, but "
                            + "\"" + arguments[ARRAY_IDX].getTypeName() + "\" "
                            + "is found");
        }

        arrayOI = (ListObjectInspector) arguments[ARRAY_IDX];
        arrayElementOI = arrayOI.getListElementObjectInspector();
        indexOI = arguments[INDEX_IDX];

        ObjectInspector expectOI = PrimitiveObjectInspectorFactory.writableIntObjectInspector;

        // Check if index and expect are of same type
        if (!ObjectInspectorUtils.compareTypes(expectOI, indexOI)) {
            throw new UDFArgumentTypeException(INDEX_IDX,
                    "\"" + expectOI.getTypeName() + "\""
                            + " expected at function array_element_at, but "
                            + "\"" + indexOI.getTypeName() + "\""
                            + " is found");
        }

        return arrayElementOI;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object array = arguments[ARRAY_IDX].get();
        IntWritable index = (IntWritable) arguments[INDEX_IDX].get();

        int arrayLength = arrayOI.getListLength(array);

        // Check if array is null or empty or index is null
        if (index == null || arrayLength <= 0) {
            return null;
        }

        if (index.get() < 0) {
            int idx = arrayLength + index.get();
            if (idx >= 0) {
                return arrayOI.getListElement(array, idx);
            }
        } else {
            if (index.get() < arrayLength) {
                return arrayOI.getListElement(array, index.get());
            }
        }

        return null;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "array_element_at(" + strings[ARRAY_IDX] + ", "
                + strings[INDEX_IDX] + ")";
    }
}