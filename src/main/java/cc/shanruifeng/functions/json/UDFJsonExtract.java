package cc.shanruifeng.functions.json;

import cc.shanruifeng.functions.utils.json.JsonExtract;
import cc.shanruifeng.functions.utils.json.JsonPath;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-25
 * @time 16:26
 */
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