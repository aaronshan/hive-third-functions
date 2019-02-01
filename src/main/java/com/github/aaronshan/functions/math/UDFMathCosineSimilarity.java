package cc.shanruifeng.functions.math;

import org.apache.hadoop.hive.ql.exec.*;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.io.DoubleWritable;
import org.apache.hadoop.hive.serde2.objectinspector.MapObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.util.Map;

/**
 * @author ruifeng.shan
 * @date 18-7-23
 */
@Description(name = "cosine_similarity"
        , value = "_FUNC_(map(varchar,double), map(varchar,double)) - cosine similarity between the given sparse vectors."
        , extended = "Example:\n > select _FUNC_(map(varchar,double), map(varchar,double)) from src;")
public class UDFMathCosineSimilarity extends GenericUDF {
    private static final int ARG_COUNT = 2; // Number of arguments to this UDF
    private transient MapObjectInspector leftMapOI;
    private transient MapObjectInspector rightMapOI;

    public UDFMathCosineSimilarity() {
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function cosine_similarity(map, map) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if two argument is of category LIST
        for (int i = 0; i < 2; i++) {
            if (!arguments[i].getCategory().equals(ObjectInspector.Category.MAP)) {
                throw new UDFArgumentTypeException(i,
                        "\"" + serdeConstants.MAP_TYPE_NAME + "\" "
                                + "expected at function cosine_similarity, but "
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
                            + " expected at function cosine_similarity key, but "
                            + "\"" + rightMapKeyOI.getTypeName() + "\""
                            + " is found");
        }

        if (!ObjectInspectorUtils.compareTypes(PrimitiveObjectInspectorFactory.javaStringObjectInspector, leftMapKeyOI)) {
            throw new UDFArgumentTypeException(1,
                    "\"" + PrimitiveObjectInspectorFactory.javaStringObjectInspector.getTypeName() + "\""
                            + " expected at function cosine_similarity key, but "
                            + "\"" + leftMapKeyOI.getTypeName() + "\""
                            + " is found");
        }

        if (!ObjectInspectorUtils.compareTypes(leftMapValueOI, rightMapValueOI)) {
            throw new UDFArgumentTypeException(1,
                    "\"" + leftMapValueOI.getTypeName() + "\""
                            + " expected at function cosine_similarity value, but "
                            + "\"" + rightMapValueOI.getTypeName() + "\""
                            + " is found");
        }

        if (!ObjectInspectorUtils.compareTypes(PrimitiveObjectInspectorFactory.javaDoubleObjectInspector, leftMapValueOI)) {
            throw new UDFArgumentTypeException(1,
                    "\"" + PrimitiveObjectInspectorFactory.javaDoubleObjectInspector.getTypeName() + "\""
                            + " expected at function cosine_similarity value, but "
                            + "\"" + leftMapValueOI.getTypeName() + "\""
                            + " is found");
        }

        return ObjectInspectorFactory.getStandardMapObjectInspector(leftMapKeyOI, leftMapValueOI);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object leftMapObj = arguments[0].get();
        Object rightMapObj = arguments[1].get();

        if (leftMapObj == null || rightMapObj == null) {
            return null;
        }

        Map<?,?> leftMap = leftMapOI.getMap(leftMapObj);
        Map<?,?> rightMap = leftMapOI.getMap(rightMapObj);

        Double normLeftMap = mapL2Norm(leftMap);
        Double normRightMap = mapL2Norm(rightMap);

        if (normLeftMap == null || normRightMap == null) {
            return null;
        }

        double dotProduct = mapDotProduct(leftMap, rightMap);
        return new DoubleWritable(dotProduct / (normLeftMap * normRightMap));
    }

    private double mapDotProduct(Map<?,?> leftMap, Map<?,?> rightMap) {
        double result = 0.0;

        for (Map.Entry<?, ?> entry : rightMap.entrySet()) {
            if (leftMap.containsKey(entry.getKey())) {
                Double leftValue = (Double) leftMap.get(entry.getKey());
                Double rightValue = (Double) entry.getValue();
                result += leftValue * rightValue;
            }
        }

        return result;
    }

    private Double mapL2Norm(Map<?,?> map) {
        double norm = 0.0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                return null;
            }

            Double value = (Double) entry.getValue();
            norm += value * value;
        }

        return Math.sqrt(norm);
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "cosine_similarity(" + strings[0] + ", "
                + strings[1] + ")";
    }
}
