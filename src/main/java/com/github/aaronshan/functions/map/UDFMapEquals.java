package com.github.aaronshan.functions.map;

import com.github.aaronshan.functions.utils.MapUtils;
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
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.BooleanWritable;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 23:31
 */
@Description(name = "map_equals"
        , value = "_FUNC_(x<K, V>, y<K, V>) - whether map x equals with map y or not."
        , extended = "Example:\n > select _FUNC_(mapX, mapY) from src;")
public class UDFMapEquals extends GenericUDF {
    private static final int ARG_COUNT = 2; // Number of arguments to this UDF
    private transient MapObjectInspector leftMapOI;
    private transient MapObjectInspector rightMapOI;
    private BooleanWritable result;

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function map_equals(map, map) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if two argument is of category LIST
        for (int i = 0; i < 2; i++) {
            if (!arguments[i].getCategory().equals(ObjectInspector.Category.MAP)) {
                throw new UDFArgumentTypeException(i,
                        "\"" + serdeConstants.MAP_TYPE_NAME + "\" "
                                + "expected at function map_equals, but "
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
                            + " expected at function map_equals key, but "
                            + "\"" + rightMapKeyOI.getTypeName() + "\""
                            + " is found");
        }

        if (!ObjectInspectorUtils.compareTypes(leftMapValueOI, rightMapValueOI)) {
            throw new UDFArgumentTypeException(1,
                    "\"" + leftMapValueOI.getTypeName() + "\""
                            + " expected at function map_equals value, but "
                            + "\"" + rightMapValueOI.getTypeName() + "\""
                            + " is found");
        }

        // Check if the comparison is supported for this type
        if (!ObjectInspectorUtils.compareSupported(leftMapValueOI)) {
            throw new UDFArgumentException("The function map_equals"
                    + " does not support comparison for "
                    + "\"" + leftMapValueOI.getTypeName() + "\""
                    + " types");
        }

        result = new BooleanWritable(false);
        return PrimitiveObjectInspectorFactory.writableBooleanObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        result.set(false);
        Object leftMapObj = arguments[0].get();
        Object rightMapObj = arguments[1].get();

        Map<?,?> leftMap = leftMapOI.getMap(leftMapObj);
        Map<?,?> rightMap = leftMapOI.getMap(rightMapObj);

        boolean ret = MapUtils.mapEquals(leftMap, rightMap, leftMapOI.getMapValueObjectInspector());
        result.set(ret);

        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "map_equals(" + strings[0] + ", "
                + strings[1] + ")";
    }
}