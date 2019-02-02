package com.github.aaronshan.functions.utils.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.StringWriter;

import static com.fasterxml.jackson.core.JsonFactory.Feature.CANONICALIZE_FIELD_NAMES;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * @author ruifeng.shan
 * @date 2016-07-25
 * @time 15:02
 */
public class JsonExtract {
    private static final JsonFactory JSON_FACTORY = new JsonFactory()
            .disable(CANONICALIZE_FIELD_NAMES);

    private JsonExtract() {
    }

    public static <T> T extract(String jsonInput, JsonExtractor<T> jsonExtractor) {
        if (jsonInput == null) {
            throw new NullPointerException("jsonInput is null");
        }

        try {
            JsonParser jsonParser = JSON_FACTORY.createParser(jsonInput);
            // Initialize by advancing to first token and make sure it exists
            if (jsonParser.nextToken() == null) {
                return null;
            }

            return jsonExtractor.extract(jsonParser);
        } catch (JsonParseException e) {
            // Return null if we failed to parse something
            return null;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static <T> JsonExtractor<T> generateExtractor(String path, JsonExtractor<T> rootExtractor) {
        return generateExtractor(path, rootExtractor, false);
    }

    public static <T> JsonExtractor<T> generateExtractor(String path, JsonExtractor<T> rootExtractor, boolean exceptionOnOutOfBounds) {
        ImmutableList<String> tokens = ImmutableList.copyOf(new JsonPathTokenizer(path));

        JsonExtractor<T> jsonExtractor = rootExtractor;
        for (String token : tokens.reverse()) {
            jsonExtractor = new ObjectFieldJsonExtractor(token, jsonExtractor, exceptionOnOutOfBounds);
        }
        return jsonExtractor;
    }

    private static int tryParseInt(String fieldName, int defaultValue) {
        int index = defaultValue;
        try {
            index = Integer.parseInt(fieldName);
        } catch (NumberFormatException ignored) {
        }
        return index;
    }

    public interface JsonExtractor<T> {
        /**
         * Executes the extraction on the existing content of the JsonParser and outputs the match.
         * <p/>
         * Notes:
         * <ul>
         * <li>JsonParser must be on the FIRST token of the value to be processed when extract is called</li>
         * <li>INVARIANT: when extract() returns, the current token of the parser will be the LAST token of the value</li>
         * </ul>
         *
         * @return the value, or null if not applicable
         */
        T extract(JsonParser jsonParser)
                throws IOException;
    }

    public static class ObjectFieldJsonExtractor<T>
            implements JsonExtractor<T> {
        private final SerializedString fieldName;
        private final JsonExtractor<? extends T> delegate;
        private final int index;
        private final boolean exceptionOnOutOfBounds;

        public ObjectFieldJsonExtractor(String fieldName, JsonExtractor<? extends T> delegate) {
            this(fieldName, delegate, false);
        }

        public ObjectFieldJsonExtractor(String fieldName, JsonExtractor<? extends T> delegate, boolean exceptionOnOutOfBounds) {
            if (fieldName == null) {
                throw new NullPointerException("jsonInput is null");
            }

            if (delegate == null) {
                throw new NullPointerException("delegate is null");
            }
            this.fieldName = new SerializedString(fieldName);
            this.delegate = delegate;
            this.exceptionOnOutOfBounds = exceptionOnOutOfBounds;
            this.index = tryParseInt(fieldName, -1);
        }

        public T extract(JsonParser jsonParser)
                throws IOException {
            if (jsonParser.getCurrentToken() == START_OBJECT) {
                return processJsonObject(jsonParser);
            }

            if (jsonParser.getCurrentToken() == START_ARRAY) {
                return processJsonArray(jsonParser);
            }

            throw new JsonParseException("Expected a JSON object or array", jsonParser.getCurrentLocation());
        }

        public T processJsonObject(JsonParser jsonParser)
                throws IOException {
            while (!jsonParser.nextFieldName(fieldName)) {
                if (!jsonParser.hasCurrentToken()) {
                    throw new JsonParseException("Unexpected end of object", jsonParser.getCurrentLocation());
                }
                if (jsonParser.getCurrentToken() == END_OBJECT) {
                    // Unable to find matching field
                    return null;
                }
                jsonParser.skipChildren(); // Skip nested structure if currently at the start of one
            }

            jsonParser.nextToken(); // Shift to first token of the value

            return delegate.extract(jsonParser);
        }

        public T processJsonArray(JsonParser jsonParser) throws IOException {
            int currentIndex = 0;
            while (true) {
                JsonToken token = jsonParser.nextToken();
                if (token == null) {
                    throw new JsonParseException("Unexpected end of array", jsonParser.getCurrentLocation());
                }
                if (token == END_ARRAY) {
                    // Index out of bounds
                    if (exceptionOnOutOfBounds) {
                        throw new RuntimeException("Index out of bounds");
                    }
                    return null;
                }
                if (currentIndex == index) {
                    break;
                }
                currentIndex++;
                jsonParser.skipChildren(); // Skip nested structure if currently at the start of one
            }

            return delegate.extract(jsonParser);
        }
    }

    public static class ScalarValueJsonExtractor
            implements JsonExtractor<String> {
        public String extract(JsonParser jsonParser)
                throws IOException {
            JsonToken token = jsonParser.getCurrentToken();
            if (token == null) {
                throw new JsonParseException("Unexpected end of value", jsonParser.getCurrentLocation());
            }
            if (!token.isScalarValue() || token == VALUE_NULL) {
                return null;
            }
            return jsonParser.getText();
        }
    }

    public static class JsonValueJsonExtractor
            implements JsonExtractor<String> {
        public String extract(JsonParser jsonParser)
                throws IOException {
            if (!jsonParser.hasCurrentToken()) {
                throw new JsonParseException("Unexpected end of value", jsonParser.getCurrentLocation());
            }

            StringWriter stringWriter = new StringWriter();
            try {
                JsonGenerator jsonGenerator = JSON_FACTORY.createGenerator(stringWriter);
                jsonGenerator.copyCurrentStructure(jsonParser);
                jsonGenerator.flush();
                jsonGenerator.close();
            } catch (IOException e) {
                return null;
            }
            return stringWriter.toString();
        }
    }

    public static class JsonSizeExtractor
            implements JsonExtractor<Long> {
        public Long extract(JsonParser jsonParser)
                throws IOException {
            if (!jsonParser.hasCurrentToken()) {
                throw new JsonParseException("Unexpected end of value", jsonParser.getCurrentLocation());
            }

            if (jsonParser.getCurrentToken() == START_ARRAY) {
                long length = 0;
                while (true) {
                    JsonToken token = jsonParser.nextToken();
                    if (token == null) {
                        return null;
                    }
                    if (token == END_ARRAY) {
                        return length;
                    }
                    jsonParser.skipChildren();

                    length++;
                }
            }

            if (jsonParser.getCurrentToken() == START_OBJECT) {
                long length = 0;
                while (true) {
                    JsonToken token = jsonParser.nextToken();
                    if (token == null) {
                        return null;
                    }
                    if (token == END_OBJECT) {
                        return length;
                    }

                    if (token == FIELD_NAME) {
                        length++;
                    } else {
                        jsonParser.skipChildren();
                    }
                }
            }

            return 0L;
        }
    }
}
