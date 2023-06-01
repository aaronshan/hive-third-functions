package com.chinagoods.bigdata.functions.url;

import com.chinagoods.bigdata.functions.json.UDFJsonArray;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author xiaowei.song
 * date: 2021-01-04
 */
@Description(name = "nginx_log_decode"
        , value = "_FUNC_(value) - Unescape the nginx log encoded value."
        , extended = "Example:\n > select _FUNC_(value) from src;")
public class UDFNginxLogDecode extends UDF {
    public static final Logger logger = LoggerFactory.getLogger(UDFNginxLogDecode.class);

    public static final String REPLACE_X_STR = "\\x";
    public static final String REPLACE_PERCENTAGE= "%";

    private Text result = new Text();

    public Text evaluate(String value) {
        if (value == null) {
            return null;
        }
        String targetStr = value.replace("%", "%25").replace(REPLACE_X_STR, REPLACE_PERCENTAGE);
        try {
            result.set(URLDecoder.decode(targetStr, StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            logger.error("source value: {}, url decoder error: ", value, e);
//            throw new AssertionError(e);
            // TODO (解析失败)
            // 替换%22为"
            result.set(targetStr.replace("%22", "\""));
        }
        return result;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
//        String str = "{\\x22page\\x22:3,\\x22sort\\x22:11,\\x22q\\x22:\\x22\\xE8\\x8A\\xB1\\xE8\\xBE\\xB9\\xE8\\xBE\\x85\\xE6\\x96\\x99\\x22,\\x22page_size\\x22:20}";
        String str = "{\\x22x_grandpa_product_type_id\\x22:10944,\\x22x_parent_product_type_id\\x22:null,\\x22merchant_type\\x22:\\x22regular\\x22,\\x22page\\x22:1,\\x22page_size\\x22:30,\\x22sort\\x22:1,\\x22q\\x22:\\x22\\x22}";
        String replaceStr = str.replace("\\x", "%");
        System.out.println(URLDecoder.decode(replaceStr, "UTF-8"));
    }
}
