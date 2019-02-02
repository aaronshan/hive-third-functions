package com.github.aaronshan.functions.utils.json;

/**
 * @author ruifeng.shan
 * @date 2016-07-25
 * @time 15:15
 */
public class JsonPath {
    private final JsonExtract.JsonExtractor<String> scalarExtractor;
    private final JsonExtract.JsonExtractor<String> objectExtractor;
    private final JsonExtract.JsonExtractor<Long> sizeExtractor;

    public JsonPath(String pattern) {
        scalarExtractor = JsonExtract.generateExtractor(pattern, new JsonExtract.ScalarValueJsonExtractor());
        objectExtractor = JsonExtract.generateExtractor(pattern, new JsonExtract.JsonValueJsonExtractor());
        sizeExtractor = JsonExtract.generateExtractor(pattern, new JsonExtract.JsonSizeExtractor());
    }

    public JsonExtract.JsonExtractor<String> getScalarExtractor() {
        return scalarExtractor;
    }

    public JsonExtract.JsonExtractor<String> getObjectExtractor() {
        return objectExtractor;
    }

    public JsonExtract.JsonExtractor<Long> getSizeExtractor() {
        return sizeExtractor;
    }
}
