package com.chinagoods.bigdata.functions.json;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * @author ruifeng.shan
 * date: 2016-07-25
 * time: 16:26
 */
@Description(name = "json_array", value = "_FUNC_(json) - Convert a string of JSON-encoded array to a Hive array of strings."
        , extended = "Example:\n"
        + "  > SELECT _FUNC_(json_array) FROM src LIMIT 1;")
public class UDFJsonArray extends UDF {
    public static final Logger logger = LoggerFactory.getLogger(UDFJsonArray.class);
    public ArrayList<String> evaluate(String jsonString) {
        if (jsonString == null) {
            logger.error("传入json字符串为空");
            return null;
        }
        try {
            JSONArray extractObject = new JSONArray(jsonString);
            ArrayList<String> result = new ArrayList<>();
            for (int ii = 0; ii < extractObject.length(); ++ii) {
                result.add(extractObject.get(ii).toString());
            }
            return result;
        } catch (JSONException | NumberFormatException e) {
            logger.error("传入json字符串解析异常，\njsonString: {}", jsonString, e);
            return null;
        }
    }
}
