package cc.shanruifeng.functions.utils;

import java.util.Map;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 22:33
 */
public final class MapUtils {
    public static<K, V> boolean mapEquals(Map<K,V> left, Map<K, V> right) {
        if (left == null || right == null) {
            if (left == null && right == null) {
                return true;
            }
            return false;
        }

        if (left.size() != right.size()) {
            return false;
        }

        for (K key : left.keySet()) {
            if (!left.get(key).equals(right.get(key))) {
                return false;
            }
        }

        return true;
    }

    public static boolean mapEquals(Map left, Map right, ObjectInspector valueOI) {
        if (left == null || right == null) {
            if (left == null && right == null) {
                return true;
            }
            return false;
        }

        if (left.size() != right.size()) {
            return false;
        }

        for (Object key : left.keySet()) {
            if (ObjectInspectorUtils.compare(left.get(key), valueOI, right.get(key), valueOI) != 0) {
                return false;
            }
        }

        return true;
    }
}
