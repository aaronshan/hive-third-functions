package com.github.aaronshan.functions.utils;

import com.github.aaronshan.functions.fastuitl.ints.AbstractIntComparator;
import com.github.aaronshan.functions.fastuitl.ints.IntComparator;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;

/**
 * @author ruifeng.shan
 * @date 2016-07-26
 * @time 17:35
 */
public class ArrayUtils {
    public static IntComparator IntArrayCompare(final Object array, final ListObjectInspector arrayOI) {
        return new AbstractIntComparator() {
            @Override
            public int compare(int left, int right) {
                ObjectInspector arrayElementOI = arrayOI.getListElementObjectInspector();
                Object leftArrayElement = arrayOI.getListElement(array, left);
                Object rightArrayElement = arrayOI.getListElement(array, right);
                if (leftArrayElement == null && rightArrayElement == null) {
                    return 0;
                }
                if (leftArrayElement == null) {
                    return -1;
                }
                if (rightArrayElement == null) {
                    return 1;
                }
                int result = ObjectInspectorUtils.compare(leftArrayElement, arrayElementOI, rightArrayElement, arrayElementOI);

                return result;
            }
        };
    }

    public static boolean arrayEquals(Object left, Object right, ListObjectInspector arrayOI) {
        if (left == null || right == null) {
            if (left == null && right == null) {
                return true;
            }
            return false;
        }

        int leftArrayLength = arrayOI.getListLength(left);
        int rightArrayLength = arrayOI.getListLength(right);

        if (leftArrayLength != rightArrayLength) {
            return false;
        }

        ObjectInspector arrayElementOI = arrayOI.getListElementObjectInspector();
        for (int i = 0; i < leftArrayLength; i++) {
            Object leftArrayElement = arrayOI.getListElement(left, i);
            Object rightArrayElement = arrayOI.getListElement(right, i);
            int compareValue = ObjectInspectorUtils.compare(leftArrayElement, arrayElementOI, rightArrayElement, arrayElementOI);
            if (compareValue != 0) {
                return false;
            }
        }

        return true;
    }
}
