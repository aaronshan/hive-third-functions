package com.github.aaronshan.functions.string;

import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import static com.github.aaronshan.functions.utils.Failures.checkCondition;
import static io.airlift.slice.SliceUtf8.lengthOfCodePoint;
import static io.airlift.slice.SliceUtf8.tryGetCodePointAt;

/**
 * @author ruifeng.shan
 * @date 2018-07-26
 * @time 23:43
 */
@Description(name = "hamming_distance"
        , value = "_FUNC_(string, string) - computes Hamming distance between two strings."
        , extended = "Example:\n > select _FUNC_(string, string) from src;")
public class UDFStringHammingDistance extends UDF {
    private LongWritable result = new LongWritable(0);

    public UDFStringHammingDistance() {
    }

    /**
     * hamming distance.
     *
     * @param leftText
     * @param rightText
     * @return
     */
    public LongWritable evaluate(Text leftText, Text rightText) throws HiveException {
        if (leftText == null || rightText == null) {
            return result;
        }

        Slice left = Slices.utf8Slice(leftText.toString());
        Slice right = Slices.utf8Slice(rightText.toString());
        int distance = 0;
        int leftPosition = 0;
        int rightPosition = 0;
        while (leftPosition < left.length() && rightPosition < right.length()) {
            int codePointLeft = tryGetCodePointAt(left, leftPosition);
            int codePointRight = tryGetCodePointAt(right, rightPosition);

            // if both code points are invalid, we do not care if they are equal
            // the following code treats them as equal if they happen to be of the same length
            if (codePointLeft != codePointRight) {
                distance++;
            }

            leftPosition += codePointLeft > 0 ? lengthOfCodePoint(codePointLeft) : -codePointLeft;
            rightPosition += codePointRight > 0 ? lengthOfCodePoint(codePointRight) : -codePointRight;
        }

        checkCondition(leftPosition == left.length() && rightPosition == right.length(),
                "The input strings to hamming_distance function must have the same length");
        result.set(distance);

        return result;
    }
}