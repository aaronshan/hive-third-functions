package com.chinagoods.bigdata.functions.url;

import com.chinagoods.bigdata.functions.utils.HttpParamUtil;
import com.chinagoods.bigdata.functions.utils.CgStringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * @author ruifeng.shan
 * date: 2016-07-27
 * time: 16:04
 */
@Description(name = "ad_url_format"
        , value = "_FUNC_(value) - Advertising link formatting, Include only advertising parameters"
        , extended = "Example:\n > select ad_url_format(value) from src;")
public class UDFAdUrlFormat extends UDF {
    public static final Logger logger = LoggerFactory.getLogger(UDFAdUrlFormat.class);

    public static final List<String> AD_PARAM_LIST = Collections.unmodifiableList(Arrays.asList("utm_campaign",
            "utm_source",
            "utm_medium",
            "utm_content"
            ));

    public String evaluate(String value) throws MalformedURLException {
        if (value == null) {
            return null;
        }
        logger.debug("输入的URL为: {}", value);
        // 转义，避免乱码
        value = StringEscapeUtils.unescapeJava(value);
        logger.debug("转义之后的URL为: {}", value);


//        URI adUri = URI.create(value);
        URL adUri = null;
        try {
            adUri = new URL(value);
        } catch (Exception e) {
            logger.error("解析ad url失败，{}", value, e);
        }
        Map<String, Object> paramsMap = HttpParamUtil.getParameter(value);

        // Fixed me。 若广告解析为null，则返回null
        if (adUri == null) {
            return null;
        }

        return String.format("%s://%s%s?utm_campaign=%s&utm_source=%s&utm_medium=%s&utm_content=%s", adUri.getProtocol(), adUri.getAuthority(),
                adUri.getPath(),
                paramsMap.getOrDefault("utm_campaign", ""),
                paramsMap.getOrDefault("utm_source", ""),
                paramsMap.getOrDefault("utm_medium", ""),
                paramsMap.getOrDefault("utm_content", "")
        );
    }

    public static void main(String[] args) throws UnsupportedEncodingException, MalformedURLException {
        String url = "https://m.chinagoods.com/shop/8002418?utm_campaign=%E58%95%3E&utm_source=4a&utm_medium=cpc&utm_content=4a-F%CE%WAP-%E58%95%3E&custom_ua=novel_webview&fp=a_fake_fp&version_code=7.1.5&tma_jssdk_version=1.31.1.2&app_name=news_article_lite&vid=D98F40E9-DB7D-4678-B4A7-F998DA4A9404&device_id=36725067842&channel=App%20Store&resolution=1242*2208&aid=35&ab_version=1859936,668908,2756108,668907,2756104,668905,2756072,668906,2756080,668904,2756063,668903,2756098,2571776&ab_feature=79452";
        System.out.println((new UDFAdUrlFormat()).evaluate(url));

//        String str = "515æ´»å\u008A¨";
//        String str = "515活动";
//        String newStr = new String(str.getBytes("ISO8859-1"),"UTF-8");
////        System.out.println(newStr);
//        System.out.println(CgStringUtils.getEncoding(str));
    }
}
