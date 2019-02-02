package com.github.aaronshan.functions.regexp;

import com.google.common.collect.Lists;
import com.google.re2j.Matcher;
import com.google.re2j.Options;
import com.google.re2j.Pattern;
import io.airlift.slice.Slice;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.re2j.Options.Algorithm.DFA_FALLBACK_TO_NFA;
import static java.lang.String.format;

/**
 * @author ruifeng.shan
 * @date 2018-07-27
 * @time 22:20
 */
public final class Re2JRegexp {
    private static final Logger log = LoggerFactory.getLogger(Re2JRegexp.class);

    private static final java.util.regex.Pattern DOT_STAR_PREFIX_PATTERN = java.util.regex.Pattern.compile("(?s)^(\\.\\*\\??)?(.*)");
    private static final int CORE_PATTERN_INDEX = 2;

    public final int dfaStatesLimit;
    public final int dfaRetries;

    public final Pattern re2jPattern;
    public final Pattern re2jPatternWithoutDotStartPrefix;

    public Re2JRegexp(int dfaStatesLimit, int dfaRetries, Slice pattern) {
        this.dfaStatesLimit = dfaStatesLimit;
        this.dfaRetries = dfaRetries;

        Options options = Options.builder()
                .setAlgorithm(DFA_FALLBACK_TO_NFA)
                .setMaximumNumberOfDFAStates(dfaStatesLimit)
                .setNumberOfDFARetries(dfaRetries)
                .setEventsListener(new RE2JEventsListener())
                .build();

        String patternString = pattern.toStringUtf8();
        re2jPattern = Pattern.compile(patternString, options);

        // Remove .*? prefix. DFA has optimization which does fast lookup for first byte of a potential match.
        // When pattern is prefixed with .*? this optimization doesn't work in Pattern.find() function.
        java.util.regex.Matcher dotStarPrefixMatcher = DOT_STAR_PREFIX_PATTERN.matcher(patternString);
        checkState(dotStarPrefixMatcher.matches());
        String patternStringWithoutDotStartPrefix = dotStarPrefixMatcher.group(CORE_PATTERN_INDEX);

        if (!patternStringWithoutDotStartPrefix.equals(patternString)) {
            re2jPatternWithoutDotStartPrefix = Pattern.compile(patternStringWithoutDotStartPrefix, options);
        } else {
            re2jPatternWithoutDotStartPrefix = re2jPattern;
        }
    }

    private static void validateGroup(int group, int groupCount) throws HiveException {
        if (group < 0) {
            throw new HiveException("Group cannot be negative");
        }
        if (group > groupCount) {
            throw new HiveException(format("Pattern has %d groups. Cannot access group %d", groupCount, group));
        }
    }

    public boolean matches(Slice source) {
        return re2jPatternWithoutDotStartPrefix.find(source);
    }

    public Slice replace(Slice source, Slice replacement) throws HiveException {
        Matcher matcher = re2jPattern.matcher(source);
        try {
            return matcher.replaceAll(replacement);
        } catch (IndexOutOfBoundsException e) {
            throw new HiveException("Illegal replacement sequence: " + replacement.toStringUtf8());
        } catch (IllegalArgumentException e) {
            throw new HiveException("Illegal replacement sequence: " + replacement.toStringUtf8());
        }
    }

    public static int toIntExact(long value) {
        if ((int)value != value) {
            throw new ArithmeticException("integer overflow");
        }
        return (int)value;
    }

    public List<Object> extractAll(Slice source, long groupIndex) throws HiveException {
        Matcher matcher = re2jPattern.matcher(source);
        int group = (int)(groupIndex);
        validateGroup(group, matcher.groupCount());

        List<Object> list = Lists.newArrayList();
        while (true) {
            if (!matcher.find()) {
                break;
            }

            Slice searchedGroup = matcher.group(group);
            if (searchedGroup == null) {
                list.add(null);
                continue;
            }
            list.add(searchedGroup.toString());
        }
        return list;
    }

    public Slice extract(Slice source, long groupIndex) throws HiveException {
        Matcher matcher = re2jPattern.matcher(source);
        int group = (int)(groupIndex);
        validateGroup(group, matcher.groupCount());

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(group);
    }

    public List<Object> split(Slice source) {
        Matcher matcher = re2jPattern.matcher(source);
        List<Object> list = Lists.newArrayList();

        int lastEnd = 0;
        while (matcher.find()) {
            Slice slice = source.slice(lastEnd, matcher.start() - lastEnd);
            lastEnd = matcher.end();
            list.add(slice.toString());
        }

        list.add(source.slice(lastEnd, source.length() - lastEnd).toString());
        return list;
    }

    private class RE2JEventsListener
            implements Options.EventsListener {
        @Override
        public void fallbackToNFA() {
            log.debug("Fallback to NFA, pattern: %s, DFA states limit: %d, DFA retries: %d", re2jPattern.pattern(), dfaStatesLimit, dfaRetries);
        }
    }
}