package com.github.aaronshan.functions.json;

import com.github.aaronshan.functions.utils.json.JsonExtract;
import com.github.aaronshan.functions.utils.json.JsonPath;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-25
 * @time 16:26
 */
@Description(name = "json_extract_scalar", value = "_FUNC_(json, json_path) - like json_extract, but returns the result value as a string (as opposed to being encoded as JSON). "
        , extended = "Example:\n"
        + "  > SELECT _FUNC_(json_array, json_path) FROM src LIMIT 1;")
public class UDFJsonExtractScalar extends UDF {
    private Text result = new Text();

    public UDFJsonExtractScalar() {
    }

    public Text evaluate(Text json, Text path) {
        try {
            JsonPath jsonPath = new JsonPath(path.toString());
            String content = JsonExtract.extract(json.toString(), jsonPath.getScalarExtractor());
            result.set(content);
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
