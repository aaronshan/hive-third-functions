package cc.shanruifeng.functions.map;

import org.apache.hadoop.hive.ql.exec.Description;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 15:39
 */
@Description(name = "map_build"
        , value = "_FUNC_(array<K>, array<V>) - returns a map created using the given key/value arrays."
        , extended = "Example:\n > select _FUNC_(array, array) from src;")
public class UDFMapBuild {
}
