package com.github.aaronshan.functions.map;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.objectinspector.*;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 15:40
 */
@Description(name = "map_concat"
        , value = "_FUNC_(x<K, V>, y<K, V>) - returns the union of two maps. If a key is found in both x and y, that key’s value in the resulting map comes from y."
        , extended = "Example:\n > select _FUNC_(mapX, mapY) from src;")
public class UDFMapConcat extends GenericUDF {
    private static final int ARG_COUNT = 2; // Number of arguments to this UDF
    LinkedHashMap<Object, Object> result = new LinkedHashMap<Object, Object>();
    private transient MapObjectInspector leftMapOI;
    private transient MapObjectInspector rightMapOI;

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function map_concat(map, map) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if two argument is of category LIST
        for (int i = 0; i < 2; i++) {
            if (!arguments[i].getCategory().equals(ObjectInspector.Category.MAP)) {
                throw new UDFArgumentTypeException(i,
                        "\"" + serdeConstants.MAP_TYPE_NAME + "\" "
                                + "expected at function map_concat, but "
                                + "\"" + arguments[i].getTypeName() + "\" "
                                + "is found");
            }
        }

        leftMapOI = (MapObjectInspector) arguments[0];
        rightMapOI = (MapObjectInspector) arguments[1];

        ObjectInspector leftMapKeyOI = leftMapOI.getMapKeyObjectInspector();
        ObjectInspector leftMapValueOI = leftMapOI.getMapValueObjectInspector();
        ObjectInspector rightMapKeyOI = rightMapOI.getMapKeyObjectInspector();
        ObjectInspector rightMapValueOI = rightMapOI.getMapValueObjectInspector();

        // Check if two map are of same key and value type
        if (!ObjectInspectorUtils.compareTypes(leftMapKeyOI, rightMapKeyOI)) {
            throw new UDFArgumentTypeException(1,
                    "\"" + leftMapKeyOI.getTypeName() + "\""
                            + " expected at function map_concat key, but "
                            + "\"" + rightMapKeyOI.getTypeName() + "\""
                            + " is found");
        }

        if (!ObjectInspectorUtils.compareTypes(leftMapValueOI, rightMapValueOI)) {
            throw new UDFArgumentTypeException(1,
                    "\"" + leftMapValueOI.getTypeName() + "\""
                            + " expected at function map_concat value, but "
                            + "\"" + rightMapValueOI.getTypeName() + "\""
                            + " is found");
        }

        return ObjectInspectorFactory.getStandardMapObjectInspector(leftMapKeyOI, leftMapValueOI);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        result.clear();
        Object leftMapObj = arguments[0].get();
        Object rightMapObj = arguments[1].get();

        Map<?,?> leftMap = leftMapOI.getMap(leftMapObj);
        Map<?,?> rightMap = leftMapOI.getMap(rightMapObj);

        if (leftMap == null) {
            if (rightMap == null) {
                return null;
            }
            return rightMap;
        } else {
            if (rightMap == null) {
                return leftMap;
            }
        }

        result.putAll(leftMap);
        result.putAll(rightMap);

        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "map_concat(" + strings[0] + ", "
                + strings[1] + ")";
    }
}
