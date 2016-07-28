package cc.shanruifeng.functions.map;

import java.util.Map;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.objectinspector.MapObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 15:38
 */
@Description(name = "map_element_at"
        , value = "_FUNC_(x<K, V>, key) - returns value for given key, or NULL if the key is not contained in the map."
        , extended = "Example:\n > select _FUNC_(map, key) from src;")
public class UDFMapElementAt extends GenericUDF {
    private static final int ARG_COUNT = 2; // Number of arguments to this UDF
    private transient MapObjectInspector mapOI;
    private transient ObjectInspector keyOI;

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function map_element_at(map, key) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if two argument is of category LIST
            if (!arguments[0].getCategory().equals(ObjectInspector.Category.MAP)) {
                throw new UDFArgumentTypeException(0,
                        "\"" + serdeConstants.MAP_TYPE_NAME + "\" "
                                + "expected at function map_element_at, but "
                                + "\"" + arguments[0].getTypeName() + "\" "
                                + "is found");
            }

        mapOI = (MapObjectInspector) arguments[0];
        keyOI = arguments[1];

        ObjectInspector mapKeyOI = mapOI.getMapKeyObjectInspector();
        ObjectInspector mapValueOI = mapOI.getMapValueObjectInspector();

        // Check if map value type are of same value type
        if (!ObjectInspectorUtils.compareTypes(mapKeyOI, keyOI)) {
            throw new UDFArgumentTypeException(1,
                    "\"" + mapKeyOI.getTypeName() + "\""
                            + " expected at function map_element_at key, but "
                            + "\"" + keyOI.getTypeName() + "\""
                            + " is found");
        }

        return mapValueOI;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object mapObj = arguments[0].get();
        Object keyObj = arguments[1].get();

        Map<?,?> map = mapOI.getMap(mapObj);
        if (map == null) {
            return null;
        }

        return map.get(keyObj);
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "map_element_at(" + strings[0] + ", "
                + strings[1] + ")";
    }
}
