package com.chinagoods.bigdata.functions.array;

import com.chinagoods.bigdata.functions.fastuitl.ints.IntArrays;
import java.util.ArrayList;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters.Converter;

import static com.chinagoods.bigdata.functions.utils.ArrayUtils.IntArrayCompare;

/**
 * @author ruifeng.shan
 * date: 2016-07-26
 * time: 11:57
 */
@Description(name = "array_intersect"
        , value = "_FUNC_(array, array) - returns the two array's intersection, without duplicates."
        , extended = "Example:\n > select _FUNC_(array, array) from src;")
public class UDFArrayIntersect extends GenericUDF {
    private static final int INITIAL_SIZE = 128;
    private static final int ARG_COUNT = 2; // Number of arguments to this UDF
    private int[] leftPositions = new int[INITIAL_SIZE];
    private int[] rightPositions = new int[INITIAL_SIZE];
    private transient ListObjectInspector leftArrayOi;
    private transient ListObjectInspector rightArrayOi;
    private transient ObjectInspector leftArrayElementOi;
    private transient ObjectInspector rightArrayElementOi;

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

        leftArrayOi = (ListObjectInspector) arguments[0];
        rightArrayOi = (ListObjectInspector) arguments[1];

        leftArrayElementOi = leftArrayOi.getListElementObjectInspector();
        rightArrayElementOi = rightArrayOi.getListElementObjectInspector();

        // Check if two array are of same type
        if (!ObjectInspectorUtils.compareTypes(leftArrayElementOi, rightArrayElementOi)) {
            throw new UDFArgumentTypeException(1,
                    "\"" + leftArrayElementOi.getTypeName() + "\""
                            + " expected at function array_intersect, but "
                            + "\"" + rightArrayElementOi.getTypeName() + "\""
                            + " is found");
        }

        // Check if the comparison is supported for this type
        if (!ObjectInspectorUtils.compareSupported(leftArrayElementOi)) {
            throw new UDFArgumentException("The function array_intersect"
                    + " does not support comparison for "
                    + "\"" + leftArrayElementOi.getTypeName() + "\""
                    + " types");
        }

        converter = ObjectInspectorConverters.getConverter(leftArrayElementOi, leftArrayElementOi);

        return ObjectInspectorFactory.getStandardListObjectInspector(leftArrayElementOi);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object leftArray = arguments[0].get();
        Object rightArray = arguments[1].get();

        int leftArrayLength = leftArrayOi.getListLength(leftArray);
        int rightArrayLength = rightArrayOi.getListLength(rightArray);

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

        IntArrays.quickSort(leftPositions, 0, leftArrayLength, IntArrayCompare(leftArray, leftArrayOi));
        IntArrays.quickSort(rightPositions, 0, rightArrayLength, IntArrayCompare(rightArray, rightArrayOi));

        result.clear();
        int leftCurrentPosition = 0, rightCurrentPosition = 0;
        int leftBasePosition = 0, rightBasePosition = 0;

        while (leftCurrentPosition < leftArrayLength && rightCurrentPosition < rightArrayLength) {
            leftBasePosition = leftCurrentPosition;
            rightBasePosition = rightCurrentPosition;
            Object leftArrayElement = leftArrayOi.getListElement(leftArray, leftPositions[leftCurrentPosition]);
            Object rightArrayElement = rightArrayOi.getListElement(rightArray, rightPositions[rightCurrentPosition]);
            int compareValue = ObjectInspectorUtils.compare(leftArrayElement, leftArrayElementOi, rightArrayElement, rightArrayElementOi);
            if (compareValue > 0) {
                rightCurrentPosition++;
            } else if (compareValue < 0) {
                leftCurrentPosition++;
            } else {
                result.add(converter.convert(leftArrayOi.getListElement(leftArray, leftPositions[leftCurrentPosition])));
                leftCurrentPosition++;
                rightCurrentPosition++;

                while (leftCurrentPosition < leftArrayLength && compare(leftArrayOi, leftArray, leftPositions[leftBasePosition], leftPositions[leftCurrentPosition]) == 0) {
                    leftCurrentPosition++;
                }
                while (rightCurrentPosition < rightArrayLength && compare(rightArrayOi, rightArray, rightPositions[rightBasePosition], rightPositions[rightCurrentPosition]) == 0) {
                    rightCurrentPosition++;
                }
            }
        }

        return result;
    }

    private int compare(ListObjectInspector arrayOi, Object array, int position1, int position2) {
        ObjectInspector arrayElementOi = arrayOi.getListElementObjectInspector();
        Object arrayElementTmp1 = arrayOi.getListElement(array, position1);
        Object arrayElementTmp2 = arrayOi.getListElement(array, position2);
        return ObjectInspectorUtils.compare(arrayElementTmp1, arrayElementOi, arrayElementTmp2, arrayElementOi);
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "array_intersect(" + strings[0] + ", "
                + strings[1] + ")";
    }
}
