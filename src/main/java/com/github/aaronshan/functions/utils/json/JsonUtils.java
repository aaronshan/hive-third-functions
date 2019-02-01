package com.github.aaronshan.functions.utils.json;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;

import static com.fasterxml.jackson.core.JsonFactory.Feature.CANONICALIZE_FIELD_NAMES;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * @author ruifeng.shan
 * @date 2016-07-25
 * @time 14:47
 */
public class JsonUtils {
    private static final JsonFactory JSON_FACTORY = new JsonFactory()
            .disable(CANONICALIZE_FIELD_NAMES);

    private static final JsonFactory MAPPING_JSON_FACTORY = new MappingJsonFactory()
            .disable(CANONICALIZE_FIELD_NAMES);

    public static Long jsonArrayLength(String jsonString) {
        try {
            JsonParser parser = JSON_FACTORY.createParser(jsonString);
            if (parser.nextToken() != START_ARRAY) {
                return null;
            }
            long length = 0;
            while (true) {
                JsonToken token = parser.nextToken();
                if (token == null) {
                    return null;
                }
                if (token == END_ARRAY) {
                    return length;
                }
                parser.skipChildren();

                length++;
            }
        } catch (IOException e) {
            return null;
        }
    }

    public static String jsonArrayGet(String json, long index) {
        try {
            JsonParser parser = MAPPING_JSON_FACTORY.createParser(json);

            if (parser.nextToken() != START_ARRAY) {
                return null;
            }

            List<String> tokens = null;
            if (index < 0) {
                tokens = Lists.newArrayList();
            }

            long count = 0;
            while (true) {
                JsonToken token = parser.nextToken();
                if (token == null) {
                    return null;
                }
                if (token == END_ARRAY) {
                    if (tokens != null && count >= index * -1) {
                        return tokens.get(0);
                    }

                    return null;
                }

                String arrayElement;
                if (token == START_OBJECT || token == START_ARRAY) {
                    arrayElement = parser.readValueAsTree().toString();
                } else {
                    arrayElement = parser.getValueAsString();
                }

                if (count == index) {
                    return arrayElement == null ? null : arrayElement;
                }

                if (tokens != null) {
                    tokens.add(arrayElement);

                    if (count >= index * -1) {
                        tokens.remove(0);
                    }
                }

                count++;
            }
        } catch (IOException e) {
            return null;
        }
    }
}
