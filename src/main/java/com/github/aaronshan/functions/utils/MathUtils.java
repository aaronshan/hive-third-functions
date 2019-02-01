package cc.shanruifeng.functions.utils;

import org.apache.hadoop.hive.ql.metadata.HiveException;

import static cc.shanruifeng.functions.utils.Failures.checkCondition;
import static java.lang.Character.MAX_RADIX;
import static java.lang.Character.MIN_RADIX;

public class MathUtils {
    public static void checkRadix(long radix) throws HiveException {
        checkCondition(radix >= MIN_RADIX && radix <= MAX_RADIX, "Radix must be between %d and %d", MIN_RADIX, MAX_RADIX);
    }

}
