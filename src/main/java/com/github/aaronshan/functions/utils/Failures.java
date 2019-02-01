package com.github.aaronshan.functions.utils;

import org.apache.hadoop.hive.ql.metadata.HiveException;

import static com.google.common.collect.Sets.newIdentityHashSet;
import static java.lang.String.format;

public class Failures {
    private Failures() {}

    public static void checkCondition(boolean condition, String formatString, Object... args) throws HiveException {
        if (!condition) {
            throw new HiveException(format(formatString, args));
        }
    }
}
