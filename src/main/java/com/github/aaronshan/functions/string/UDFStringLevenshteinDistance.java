package cc.shanruifeng.functions.string;

import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import static cc.shanruifeng.functions.utils.Failures.checkCondition;
import static io.airlift.slice.SliceUtf8.getCodePointAt;
import static io.airlift.slice.SliceUtf8.lengthOfCodePoint;
import static io.airlift.slice.SliceUtf8.tryGetCodePointAt;

/**
 * @author ruifeng.shan
 * @date 2018-07-26
 * @time 23:53
 */
@Description(name = "levenshtein_distance"
        , value = "_FUNC_(string, string) - computes Levenshtein distance between two strings."
        , extended = "Example:\n > select _FUNC_(string, string) from src;")
public class UDFStringLevenshteinDistance extends UDF {
    private LongWritable result = new LongWritable(0);

    public UDFStringLevenshteinDistance() {
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
            return null;
        }

        Slice left = Slices.utf8Slice(leftText.toString());
        Slice right = Slices.utf8Slice(rightText.toString());
        int[] leftCodePoints = castToCodePoints(left);
        int[] rightCodePoints = castToCodePoints(right);

        if (leftCodePoints.length < rightCodePoints.length) {
            int[] tempCodePoints = leftCodePoints;
            leftCodePoints = rightCodePoints;
            rightCodePoints = tempCodePoints;
        }

        if (rightCodePoints.length == 0) {
            result.set(leftCodePoints.length);
            return result;
        }

        checkCondition((leftCodePoints.length * (rightCodePoints.length - 1)) <= 1_000_000,
                "The combined inputs for Levenshtein distance are too large");

        int[] distances = new int[rightCodePoints.length];
        for (int i = 0; i < rightCodePoints.length; i++) {
            distances[i] = i + 1;
        }

        for (int i = 0; i < leftCodePoints.length; i++) {
            int leftUpDistance = distances[0];
            if (leftCodePoints[i] == rightCodePoints[0]) {
                distances[0] = i;
            }
            else {
                distances[0] = Math.min(i, distances[0]) + 1;
            }
            for (int j = 1; j < rightCodePoints.length; j++) {
                int leftUpDistanceNext = distances[j];
                if (leftCodePoints[i] == rightCodePoints[j]) {
                    distances[j] = leftUpDistance;
                }
                else {
                    distances[j] = Math.min(distances[j - 1], Math.min(leftUpDistance, distances[j])) + 1;
                }
                leftUpDistance = leftUpDistanceNext;
            }
        }

        result.set(distances[rightCodePoints.length - 1]);

        return result;
    }

    private static int[] castToCodePoints(Slice slice) throws HiveException {
        int[] codePoints = new int[safeCountCodePoints(slice)];
        int position = 0;
        for (int index = 0; index < codePoints.length; index++) {
            codePoints[index] = getCodePointAt(slice, position);
            position += lengthOfCodePoint(slice, position);
        }
        return codePoints;
    }

    private static int safeCountCodePoints(Slice slice) throws HiveException {
        int codePoints = 0;
        for (int position = 0; position < slice.length(); ) {
            int codePoint = tryGetCodePointAt(slice, position);
            if (codePoint < 0) {
                throw new HiveException("Invalid UTF-8 encoding in characters: " + slice.toStringUtf8());
            }
            position += lengthOfCodePoint(codePoint);
            codePoints++;
        }
        return codePoints;
    }
}