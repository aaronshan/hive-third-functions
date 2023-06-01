package com.chinagoods.bigdata.functions.map;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredJavaObject;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.objectinspector.MapObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaowei.song
 * date: 2016-07-27
 * time: 15:39
 */
@Description(name = "map2sort_key_array"
        , value = "_FUNC_(map<K,V>) - Returns the value array of map keys in positive order."
        , extended = "Example:\n > select _FUNC_(map) from src;")
public class UDFMap2SortKeyArray extends GenericUDF {
    /**
     * Number of arguments to this UDF
     **/
    private static final int ARG_COUNT = 1;
    private transient MapObjectInspector mapOi;
    List<Object> result = new ArrayList<>();

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function map2sort_key_array(map<K, V>) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if one argument is of category Map
        for (int i = 0; i < 1; i++) {
            if (!arguments[i].getCategory().equals(ObjectInspector.Category.MAP)) {
                throw new UDFArgumentTypeException(i,
                        "\"" + serdeConstants.MAP_TYPE_NAME + "\" "
                                + "expected at function map2sort_key_array, but "
                                + "\"" + arguments[i].getTypeName() + "\" "
                                + "is found");
            }
        }

        mapOi = (MapObjectInspector) arguments[0];

        ObjectInspector mapValueOI = mapOi.getMapValueObjectInspector();

        return ObjectInspectorFactory.getStandardListObjectInspector(mapValueOI);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Map<?, ?> mObj = mapOi.getMap(arguments[0].get());

        if (mObj == null || mObj.size() <= 0) {
            return null;
        }
        result.clear();
        Map<String, Object> mTrans = new HashMap<>(mObj.size());
        mObj.forEach((k, v) -> {
            mTrans.put(k.toString(), v);
        });
        Set<?> mKeySet = mObj.keySet();
        List<String> mKeyList = mKeySet.stream().map(Object::toString).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        for (String k : mKeyList) {
            result.add(mTrans.get(k));
        }
        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "map2sort_key_array(" + strings[0] + ")";
    }

    public static void main(String[] args) throws HiveException {
        UDFMap2SortKeyArray map2SortKeyArray = new UDFMap2SortKeyArray();
        Map<String, String> sourceMap = new HashMap<String, String>(){
            {
                put("2021-04-05", "2");
                put("2021-05-04", "1");
                put("2021-06-03", "8");
                put("2021-01-01", "100");
            }
        };
        DeferredObject[] arr = new DeferredObject[1];
        arr[0] = new GenericUDF.DeferredJavaObject(sourceMap);
        ObjectInspector[] objArr = new ObjectInspector[1];
        objArr[0] = ObjectInspectorFactory.getStandardMapObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector, PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        map2SortKeyArray.initialize(objArr);
        System.out.println(map2SortKeyArray.evaluate(arr));
    }
}
