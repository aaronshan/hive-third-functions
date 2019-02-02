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
@Description(name = "json_extract", value = "_FUNC_(json, json_path) - extract json by given jsonPath. "
        , extended = "Example:\n"
        + "  > SELECT _FUNC_(json_array, json_path) FROM src LIMIT 1;")
public class UDFJsonExtract extends UDF {
    private Text result = new Text();

    public UDFJsonExtract() {
    }

    public Text evaluate(Text json, Text path) {
        try {
            JsonPath jsonPath = new JsonPath(path.toString());
            String content = JsonExtract.extract(json.toString(), jsonPath.getObjectExtractor());
            result.set(content);
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
