package cc.shanruifeng.functions.utils;

import cc.shanruifeng.functions.fastuitl.ints.AbstractIntComparator;
import cc.shanruifeng.functions.fastuitl.ints.IntComparator;
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
}
