package com.github.aaronshan.functions.map;

import java.util.LinkedHashMap;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 15:39
 */
@Description(name = "map_build"
        , value = "_FUNC_(array<K>, array<V>) - returns a map created using the given key/value arrays."
        , extended = "Example:\n > select _FUNC_(array, array) from src;")
public class UDFMapBuild extends GenericUDF {
    private static final int ARG_COUNT = 2; // Number of arguments to this UDF
    LinkedHashMap<Object, Object> result = new LinkedHashMap<Object, Object>();
    private transient ListObjectInspector keyArrayOI;
    private transient ListObjectInspector valueArrayOI;

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function map_build(array, array) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if two argument is of category LIST
        for (int i = 0; i < 2; i++) {
            if (!arguments[i].getCategory().equals(ObjectInspector.Category.LIST)) {
                throw new UDFArgumentTypeException(i,
                        "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                                + "expected at function map_build, but "
                                + "\"" + arguments[i].getTypeName() + "\" "
                                + "is found");
            }
        }

        keyArrayOI = (ListObjectInspector) arguments[0];
        valueArrayOI = (ListObjectInspector) arguments[1];

        ObjectInspector mapKeyOI = keyArrayOI.getListElementObjectInspector();
        ObjectInspector mapValueOI = valueArrayOI.getListElementObjectInspector();

        return ObjectInspectorFactory.getStandardMapObjectInspector(mapKeyOI, mapValueOI);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object keyArray = arguments[0].get();
        Object valueArray = arguments[1].get();

        int keyArrayLength = keyArrayOI.getListLength(keyArray);
        int valueArrayLength = valueArrayOI.getListLength(valueArray);

        if (keyArray == null || valueArray == null || keyArrayLength <= 0 || valueArrayLength <= 0) {
            return null;
        }

        if (keyArrayLength != valueArrayLength) {
            throw new HiveException("key array length not equals value array length!");
        }

        result.clear();
        for (int i = 0; i < keyArrayLength; i++) {
            result.put(keyArrayOI.getListElement(keyArray, i), valueArrayOI.getListElement(valueArray, i));
        }

        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "map_build(" + strings[0] + ", "
                + strings[1] + ")";
    }
}
