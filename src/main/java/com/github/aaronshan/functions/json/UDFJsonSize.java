package com.github.aaronshan.functions.json;

import com.github.aaronshan.functions.utils.json.JsonExtract;
import com.github.aaronshan.functions.utils.json.JsonPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-25
 * @time 16:33
 */
@Description(name = "json_size", value = "_FUNC_(json, json_path) - like json_extract, but returns the size of the value. For objects or arrays, the size is the number of members, and the size of a scalar value is zero. "
        , extended = "Example:\n"
        + "  > SELECT _FUNC_(json_array, json_path) FROM src LIMIT 1;")
public class UDFJsonSize extends UDF{
    private LongWritable result = new LongWritable();

    public UDFJsonSize() {
    }

    public LongWritable evaluate(Text json, Text path) {
        try {
            JsonPath jsonPath = new JsonPath(path.toString());
            Long size = JsonExtract.extract(json.toString(), jsonPath.getSizeExtractor());
            result.set(size);
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
