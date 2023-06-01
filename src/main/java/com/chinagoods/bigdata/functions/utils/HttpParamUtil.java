package com.chinagoods.bigdata.functions.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaowei.song
 */
public class HttpParamUtil {
    public static final Logger logger = LoggerFactory.getLogger(HttpParamUtil.class);

    /**
     * 获得url中参数
     * @param url url链接
     * @return url中参数map
     */
    public static Map<String, Object> getParameter(String url) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            url = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            logger.error("url解码异常: {}", url, e);
        }
        if (url.indexOf('?') != -1) {
            final String contents = url.substring(url.indexOf('?') + 1);
            String[] keyValues = contents.split("[&#]");
            for (int i = 0; i < keyValues.length; i++) {
                // url结尾为/非标准k=v格式，跳过
                if (!keyValues[i].contains("=")) {
                    continue;
                }
                String key = keyValues[i].substring(0, keyValues[i].indexOf("="));
                String value = keyValues[i].substring(keyValues[i].indexOf("=") + 1);
                // value去除特殊字符
                value = value.replaceAll("[.、']", "");
                map.put(key, value);
            }
        }
        return map;
    }
    /**
     * 测试
     * @param args main方法参数列表
     */
    public static void main(String[] args) {
        String url = "https://www.chinagoods.com/activies/special?code=202107131418516200819335&active_code=202107131418516200819335&id=545?utm_campaign=????&utm_source=??&utm_medium=cpc&utm_content=??-????WAP-????#/";
        Map<String, Object> map = getParameter(url);
        System.out.println(map);
    }
}