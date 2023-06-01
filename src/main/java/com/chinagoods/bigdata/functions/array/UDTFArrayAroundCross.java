package com.chinagoods.bigdata.functions.array;

import com.chinagoods.bigdata.functions.fastuitl.ints.IntArrays;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters.Converter;

import java.util.ArrayList;
import java.util.Arrays;

import static com.chinagoods.bigdata.functions.utils.ArrayUtils.IntArrayCompare;

/**
 * @author xiaowei.song
 * date: 2022-10-26
 * time: 09:39
 */
@Description(name = "array_around_cross"
        , value = "_FUNC_(array) - Returns the cartesian product function in a collection."
        , extended = "Example:\n > select _FUNC_(array) from src;")
public class UDTFArrayAroundCross extends GenericUDTF {
    private static final int INITIAL_SIZE = 128;
    /**
     * Number of arguments to this UDF
     **/
    private static final int ARG_COUNT = 1;
    private int[] leftPositions = new int[INITIAL_SIZE];
    private transient ListObjectInspector arrayOi;
    private transient ObjectInspector arrayElementOi;

    private transient Converter converter;

    public UDTFArrayAroundCross() {
    }

    @Override
    public StructObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function array_around_cross(array) takes exactly " + ARG_COUNT + "arguments.");
        }

        // Check if two argument is of category LIST
        for (int i = 0; i < ARG_COUNT; i++) {
            if (!arguments[i].getCategory().equals(ObjectInspector.Category.LIST)) {
                throw new UDFArgumentTypeException(i,
                        "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                                + "expected at function array_around_cross, but "
                                + "\"" + arguments[i].getTypeName() + "\" "
                                + "is found");
            }
        }
        arrayOi = (ListObjectInspector) arguments[0];
        arrayElementOi = arrayOi.getListElementObjectInspector();
        converter = ObjectInspectorConverters.getConverter(arrayElementOi, arrayElementOi);

        // 有2列，col1、col2
        return ObjectInspectorFactory.getStandardStructObjectInspector(
                Arrays.asList("col1", "col2"),
                Arrays.asList(arrayElementOi
                        ,arrayElementOi
                )
        );
    }

    @Override
    public void process(Object[] arguments) throws HiveException {
        Object srcArray = arguments[0];
        int srcArrayLength = arrayOi.getListLength(srcArray);

        // Check if array is null or empty
        if (srcArray == null || srcArrayLength < 0) {
            return;
        }

        // 若原始内容小于等于1，则返回null
        if (srcArrayLength <= 1) {
            return;
        }

        if (leftPositions.length < srcArrayLength) {
            leftPositions = new int[srcArrayLength];
        }

        for (int i = 0; i < srcArrayLength; i++) {
            leftPositions[i] = i;
        }

        IntArrays.quickSort(leftPositions, 0, srcArrayLength, IntArrayCompare(srcArray, arrayOi));

        Object leftArrayElement;
        Object rightArrayElement;


        ArrayList<Object> tuple2Arr;
        for (int i = 0; i < srcArrayLength; i++) {
            leftArrayElement = arrayOi.getListElement(srcArray, leftPositions[i]);
            for (int j = i + 1; j < srcArrayLength; j++) {
                tuple2Arr = new ArrayList<>(2);
                rightArrayElement = arrayOi.getListElement(srcArray, leftPositions[j]);
                tuple2Arr.add(leftArrayElement);
                tuple2Arr.add(rightArrayElement);
                forward(tuple2Arr);
            }
        }
    }

    @Override
    public void close() throws HiveException {

    }

    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "array_around_cross(" + strings[0] + ")";
    }
}
