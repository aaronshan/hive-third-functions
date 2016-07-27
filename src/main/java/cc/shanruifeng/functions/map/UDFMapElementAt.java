package cc.shanruifeng.functions.map;

import org.apache.hadoop.hive.ql.exec.Description;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 15:38
 */
@Description(name = "map_element_at"
        , value = "_FUNC_(x<K, V>, key) - returns value for given key, or NULL if the key is not contained in the map."
        , extended = "Example:\n > select _FUNC_(map, key) from src;")
public class UDFMapElementAt {
}
