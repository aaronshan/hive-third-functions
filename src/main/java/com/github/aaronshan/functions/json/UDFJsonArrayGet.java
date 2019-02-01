package com.github.aaronshan.functions.json;

import com.github.aaronshan.functions.utils.json.JsonUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-25
 * @time 15:29
 */
@Description(name = "json_array_get", value = "_FUNC_(json, json_path) - returns the element at the specified index into the json_array. The index is zero-based.. "
        , extended = "Example:\n"
        + "  > SELECT _FUNC_(json_array, json_path) FROM src LIMIT 1;")
public class UDFJsonArrayGet extends UDF {
    private Text result = new Text();

    public UDFJsonArrayGet() {
    }

    public Text evaluate(Text json, long index) {
        try {
            result.set(JsonUtils.jsonArrayGet(json.toString(), index));
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
