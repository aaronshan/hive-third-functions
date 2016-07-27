package cc.shanruifeng.functions.map;

import org.apache.hadoop.hive.ql.exec.Description;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 15:40
 */
@Description(name = "map_concat"
        , value = "_FUNC_(x<K, V>, y<K, V>) - returns the union of two maps. If a key is found in both x and y, that key’s value in the resulting map comes from y."
        , extended = "Example:\n > select _FUNC_(mapX, mapY) from src;")
public class UDFMapConcat {
}
