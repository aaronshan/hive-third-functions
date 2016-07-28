package cc.shanruifeng.functions.url;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 16:04
 */
@Description(name = "url_encode"
        , value = "_FUNC_(value) - string\n Escapes value by encoding it so that it can be safely included in URL query parameter names and values:\n"
        + "* Alphanumeric characters are not encoded.\n"
        + "* The characters characters ., -, * and _ are not encoded.\n"
        + "* The ASCII space character is encoded as +.\n"
        + "* All other characters are converted to UTF-8 and the bytes are encoded as the string %XX where XX is the uppercase hexadecimal value of the UTF-8 byte."
        , extended = "Example:\n > select _FUNC_(value) from src;")
public class UDFUrlEncode extends UDF {
    private Text result = new Text();

    public Text evaluate(String value) {
        Escaper escaper = UrlEscapers.urlFormParameterEscaper();
        result.set(escaper.escape(value));
        return result;
    }
}
