package com.github.aaronshan.functions.array;

import com.github.aaronshan.functions.fastuitl.ints.IntArrays;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters.Converter;

import static com.github.aaronshan.functions.utils.ArrayUtils.IntArrayCompare;

/**
 * @author ruifeng.shan
 * @date 2016-07-26
 * @time 11:57
 */
@Description(name = "array_intersect"
        , value = "_FUNC_(array, array) - returns the two array's intersection, without duplicates."
        , extended = "Example:\n > select _FUNC_(array, array) from src;")
public class UDFArrayIntersect extends GenericUDF {
    private static final int INITIAL_SIZE = 128;
    private static final int ARG_COUNT = 2; // Number of arguments to this UDF
    private int[] leftPositions = new int[INITIAL_SIZE];
    private int[] rightPositions = new int[INITIAL_SIZE];
    private transient ListObjectInspector leftArrayOI;
    private transient ListObjectInspector rightArrayOI;
    private transient ObjectInspector leftArrayElementOI;
    private transient ObjectInspector rightArrayElementOI;

    private transient ArrayList<Object> result = new ArrayList<Object>();
    private transient Converter converter;

    public UDFArrayIntersect() {
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function array_intersect(array, array) takes exactly " + ARG_COUNT + "arguments.");
        }

        // Check if two argument is of category LIST
        for (int i = 0; i < 2; i++) {
            if (!arguments[i].getCategory().equals(ObjectInspector.Category.LIST)) {
                throw new UDFArgumentTypeException(i,
                        "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                                + "expected at function array_intersect, but "
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
                            + " expected at function array_intersect, but "
                            + "\"" + rightArrayElementOI.getTypeName() + "\""
                            + " is found");
        }

        // Check if the comparison is supported for this type
        if (!ObjectInspectorUtils.compareSupported(leftArrayElementOI)) {
            throw new UDFArgumentException("The function array_intersect"
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
            return leftArray;
        }
        if (rightArrayLength == 0) {
            return rightArray;
        }

        if (leftPositions.length < leftArrayLength) {
            leftPositions = new int[leftArrayLength];
        }

        if (rightPositions.length < rightArrayLength) {
            rightPositions = new int[rightArrayLength];
        }

        for (int i = 0; i < leftArrayLength; i++) {
            leftPositions[i] = i;
        }
        for (int i = 0; i < rightArrayLength; i++) {
            rightPositions[i] = i;
        }

        IntArrays.quickSort(leftPositions, 0, leftArrayLength, IntArrayCompare(leftArray, leftArrayOI));
        IntArrays.quickSort(rightPositions, 0, rightArrayLength, IntArrayCompare(rightArray, rightArrayOI));

        result.clear();
        int leftCurrentPosition = 0;
        int rightCurrentPosition = 0;
        int leftBasePosition;
        int rightBasePosition;

        while (leftCurrentPosition < leftArrayLength && rightCurrentPosition < rightArrayLength) {
            leftBasePosition = leftCurrentPosition;
            rightBasePosition = rightCurrentPosition;
            Object leftArrayElement = leftArrayOI.getListElement(leftArray, leftPositions[leftCurrentPosition]);
            Object rightArrayElement = rightArrayOI.getListElement(rightArray, rightPositions[rightCurrentPosition]);
            int compareValue = ObjectInspectorUtils.compare(leftArrayElement, leftArrayElementOI, rightArrayElement, rightArrayElementOI);
            if (compareValue > 0) {
                rightCurrentPosition++;
            } else if (compareValue < 0) {
                leftCurrentPosition++;
            } else {
                result.add(converter.convert(leftArrayOI.getListElement(leftArray, leftPositions[leftCurrentPosition])));
                leftCurrentPosition++;
                rightCurrentPosition++;

                while (leftCurrentPosition < leftArrayLength && compare(leftArrayOI, leftArray, leftBasePosition, leftCurrentPosition) == 0) {
                    leftCurrentPosition++;
                }
                while (rightCurrentPosition < rightArrayLength && compare(rightArrayOI, rightArray, rightBasePosition, rightCurrentPosition) == 0) {
                    rightCurrentPosition++;
                }
            }
        }

        return result;
    }

    private int compare(ListObjectInspector arrayOI, Object array, int position1, int position2) {
        ObjectInspector arrayElementOI = arrayOI.getListElementObjectInspector();
        Object arrayElementTmp1 = arrayOI.getListElement(array, leftPositions[position1]);
        Object arrayElementTmp2 = arrayOI.getListElement(array, leftPositions[position2]);
        return ObjectInspectorUtils.compare(arrayElementTmp1, arrayElementOI, arrayElementTmp2, arrayElementOI);
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "array_intersect(" + strings[0] + ", "
                + strings[1] + ")";
    }
}
