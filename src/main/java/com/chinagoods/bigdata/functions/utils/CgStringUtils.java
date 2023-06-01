package com.chinagoods.bigdata.functions.utils;

import java.nio.charset.StandardCharsets;

/**
 * @author xiaowei.song
 * 字符串编码探测，返回相应编码
 */
public class CgStringUtils {
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String GB2312 = "GB2312";
    public static final String GBK = "GBK";

    /**
     * 探测字符串编码
     **/
    public static String getEncoding(String str) {
        String encode = GB2312;
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s = encode;
                return s;
            }
        } catch (Exception exception) {
        }
        encode = ISO_8859_1;
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = StandardCharsets.UTF_8.name();
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = GBK;
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        return "";
    }
}
