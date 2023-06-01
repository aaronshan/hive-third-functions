package com.chinagoods.bigdata.functions.url;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chinagoods.bigdata.functions.json.UDFJsonArray;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ruifeng.shan
 * date: 2016-07-27
 * time: 16:04
 */
@Description(name = "url_decode"
        , value = "_FUNC_(value) - Unescape the URL encoded value. This function is the inverse of url_encode()"
        , extended = "Example:\n > select _FUNC_(value) from src;")
public class UDFUrlDecode extends UDF {
    public static final Logger logger = LoggerFactory.getLogger(UDFUrlDecode.class);

    private Text result = new Text();
    /**
     * 正则表达式，匹配结尾为%\d或%$字符
     **/
    public static final String INCOMPLETE_PATTERN_STR = "%[\\w+\\\\]?$";

    // 创建 Pattern 对象
    public static final Pattern INCOMPLETE_PATTERN = Pattern.compile(INCOMPLETE_PATTERN_STR);

    public Text evaluate(String value) {
        if (value == null) {
            return null;
        }
        try {
            Matcher m = INCOMPLETE_PATTERN.matcher(value);
            if (m.find( )) {
                value = value.replaceAll(INCOMPLETE_PATTERN_STR, "");
            }

            result.set(URLDecoder.decode(value, "UTF-8"));
            return result;
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            logger.error("url_decode转码失败，此字符串无法转码: {}", value, e);
            result.set(value);
            return result;
//            throw new AssertionError(e);
        }
    }

    public static void main(String[] args) {
        String url = "http://m.chinagoods.com/callApp/buyer?code=AI#mCHMv5C%dqMJ0";
        System.out.println((new UDFUrlDecode()).evaluate(url));
    }
}
