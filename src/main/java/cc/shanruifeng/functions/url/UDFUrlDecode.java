package cc.shanruifeng.functions.url;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 16:04
 */
@Description(name = "url_decode"
        , value = "_FUNC_(value) - Unescape the URL encoded value. This function is the inverse of url_encode()"
        , extended = "Example:\n > select _FUNC_(value) from src;")
public class UDFUrlDecode extends UDF {
    private Text result = new Text();

    public Text evaluate(String value) {
        try {
            result.set(URLDecoder.decode(value, "UTF-8"));
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
}
