package com.chinagoods.bigdata.functions.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Jackson builder mapper
 */
public final class JacksonBuilder {
    public static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 配置jackson配置
     **/
    static {
        // 该特性决定了当遇到未知属性（没有映射到属性，没有任何setter或者任何可以处理它的handler），是否应该抛出一个JsonMappingException异常
        mapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
        //在序列化时日期格式默认为 yyyy-MM-dd'T'HH:mm:ss.SSSZ
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //在序列化时忽略值为 null 的属性
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        //忽略值为默认值的属性
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS);
        //设置JSON时间格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        // 声明一个简单Module 对象
        SimpleModule httpCodeEnumModule = new SimpleModule();
        mapper.registerModule(httpCodeEnumModule);
    }

    public static void main(String[] args) throws IOException {
        String requestBody = "{\\x22unitSystem\\x22:\\x22iOS\\x22,\\x22type\\x22:1,\\x22token\\x22:\\x22031f88c99c601d614633c6ba84825d5e763c\\x22,\\x22unitType\\x22:\\x22iPhone 7 Plus\\x22,\\x22unitName\\x22:\\x22iPhone\\x22}";
        requestBody = StringEscapeUtils.unescapeJava(requestBody.replaceAll("\\\\x", "\\\\u00"));
        JsonNode jsonNode = JacksonBuilder.mapper.readTree(requestBody);
        System.out.println(jsonNode);
    }
}
