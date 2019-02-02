package com.github.aaronshan.functions.json;

import com.github.aaronshan.functions.utils.json.JsonUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-25
 * @time 14:57
 */
@Description(name = "json_array_length", value = "_FUNC_(json, json_path) - Returns the array length of json (a string containing a JSON array). "
        , extended = "Example:\n"
        + "  > SELECT _FUNC_(json_array, json_path) FROM src LIMIT 1;")
public class UDFJsonArrayLength extends UDF {
    private LongWritable result = new LongWritable();

    public UDFJsonArrayLength() {
    }

    public LongWritable evaluate(Text text) {
        try {
            result.set(JsonUtils.jsonArrayLength(text.toString()));
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
