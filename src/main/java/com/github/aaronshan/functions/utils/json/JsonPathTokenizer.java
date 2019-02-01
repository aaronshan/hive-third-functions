package com.github.aaronshan.functions.utils.json;

import com.google.common.collect.AbstractIterator;

import static java.lang.Character.isLetterOrDigit;
import static java.lang.String.format;

/**
 * @author ruifeng.shan
 * @date 2016-07-25
 * @time 15:06
 */
public class JsonPathTokenizer extends AbstractIterator<String> {
    private static final char QUOTE = '\"';
    private static final char DOT = '.';
    private static final char OPEN_BRACKET = '[';
    private static final char CLOSE_BRACKET = ']';
    private static final char UNICODE_CARET = '\u2038';

    private final String path;
    private int index;

    public JsonPathTokenizer(String path) {
        if (path == null) {
            throw new NullPointerException("path is null");
        }
        this.path = path;

        if (path.isEmpty()) {
            throw invalidJsonPath();
        }

        // skip the start token
        match('$');
    }

    private static boolean isUnquotedPathCharacter(char c) {
        return c == ':' || isUnquotedSubscriptCharacter(c);
    }

    private static boolean isUnquotedSubscriptCharacter(char c) {
        return c == '_' || isLetterOrDigit(c);
    }

    @Override
    protected String computeNext() {
        if (!hasNextCharacter()) {
            return endOfData();
        }

        if (tryMatch(DOT)) {
            return matchPathSegment();
        }

        if (tryMatch(OPEN_BRACKET)) {
            String token = tryMatch(QUOTE) ? matchQuotedSubscript() : matchUnquotedSubscript();

            match(CLOSE_BRACKET);
            return token;
        }

        throw invalidJsonPath();
    }

    private String matchPathSegment() {
        // seek until we see a special character or whitespace
        int start = index;
        while (hasNextCharacter() && isUnquotedPathCharacter(peekCharacter())) {
            nextCharacter();
        }
        int end = index;

        String token = path.substring(start, end);

        // an empty unquoted token is not allowed
        if (token.isEmpty()) {
            throw invalidJsonPath();
        }

        return token;
    }

    private String matchUnquotedSubscript() {
        // seek until we see a special character or whitespace
        int start = index;
        while (hasNextCharacter() && isUnquotedSubscriptCharacter(peekCharacter())) {
            nextCharacter();
        }
        int end = index;

        String token = path.substring(start, end);

        // an empty unquoted token is not allowed
        if (token.isEmpty()) {
            throw invalidJsonPath();
        }

        return token;
    }

    private String matchQuotedSubscript() {
        // quote has already been matched

        // seek until we see the close quote
        int start = index;
        while (hasNextCharacter() && peekCharacter() != QUOTE) {
            nextCharacter();
        }
        int end = index;

        String token = path.substring(start, end);

        match(QUOTE);
        return token;
    }

    private boolean hasNextCharacter() {
        return index < path.length();
    }

    private void match(char expected) {
        if (!tryMatch(expected)) {
            throw invalidJsonPath();
        }
    }

    private boolean tryMatch(char expected) {
        if (peekCharacter() != expected) {
            return false;
        }
        index++;
        return true;
    }

    private void nextCharacter() {
        index++;
    }

    private char peekCharacter() {
        return path.charAt(index);
    }

    private RuntimeException invalidJsonPath() {
        return new RuntimeException(format("Invalid JSON path: '%s'", path));
    }

    @Override
    public String toString() {
        return path.substring(0, index) + UNICODE_CARET + path.substring(index);
    }
}