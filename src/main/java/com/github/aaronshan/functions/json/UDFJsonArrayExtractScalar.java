package com.github.aaronshan.functions.json;

import com.github.aaronshan.functions.utils.json.JsonExtract;
import com.github.aaronshan.functions.utils.json.JsonPath;
import com.github.aaronshan.functions.utils.json.JsonUtils;
import java.util.ArrayList;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-25
 * @time 15:33
 */
@Description(name = "json_array_extract_scalar", value = "_FUNC_(json, json_path) - extract json array by given jsonPath. but returns the result value as a string (as opposed to being encoded as JSON)."
        , extended = "Example:\n"
        + "  > SELECT _FUNC_(json_array, json_path) FROM src LIMIT 1;")
public class UDFJsonArrayExtractScalar extends GenericUDF {
    private ObjectInspectorConverters.Converter[] converters;

    public UDFJsonArrayExtractScalar() {
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        if (arguments.length != 2) {
            throw new UDFArgumentLengthException(
                    "The function json_array_extract_scalar(json, json_path) takes exactly 2 arguments.");
        }

        converters = new ObjectInspectorConverters.Converter[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            converters[i] = ObjectInspectorConverters.getConverter(arguments[i],
                    PrimitiveObjectInspectorFactory.writableStringObjectInspector);
        }

        return ObjectInspectorFactory
                .getStandardListObjectInspector(PrimitiveObjectInspectorFactory
                        .writableStringObjectInspector);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        assert (arguments.length == 2);

        if (arguments[0].get() == null || arguments[1].get() == null) {
            return null;
        }

        try {
            Text jsonText = (Text) converters[0].convert(arguments[0].get());
            Text pathText = (Text) converters[1].convert(arguments[1].get());
            String json = jsonText.toString();

            Long length = JsonUtils.jsonArrayLength(json);
            if (length == null) {
                return null;
            }
            ArrayList<Text> ret = new ArrayList<Text>(length.intValue());
            JsonPath jsonPath = new JsonPath(pathText.toString());
            ret.clear();
            for (int i = 0; i < length; i++) {
                String content = JsonUtils.jsonArrayGet(json, i);
                String result = JsonExtract.extract(content, jsonPath.getScalarExtractor());
                ret.add(new Text(result));
            }
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == 2);
        return "json_array_extract_scalar(" + strings[0] + ", " + strings[1] + ")";
    }
}
