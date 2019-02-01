package cc.shanruifeng.functions.string;

import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import static cc.shanruifeng.functions.utils.Failures.checkCondition;
import static io.airlift.slice.SliceUtf8.getCodePointAt;
import static io.airlift.slice.SliceUtf8.countCodePoints;

/**
 * @author ruifeng.shan
 * @date 2018-07-26
 * @time 23:23
 */
@Description(name = "codepoint"
        , value = "_FUNC_(string) - returns Unicode code point of a single character string."
        , extended = "Example:\n > select _FUNC_(string) from src;")
public class UDFCodePoint extends UDF {
    private LongWritable result = new LongWritable();

    public UDFCodePoint() {
    }

    /**
     * codepoint.
     *
     * @param text
     * @return
     */
    public LongWritable evaluate(Text text) throws HiveException {
        if (text == null) {
            return null;
        }

        Slice slice = Slices.utf8Slice(text.toString());
        checkCondition(countCodePoints(slice) == 1, "Input string must be a single character string");

        result.set(getCodePointAt(slice, 0));
        return result;
    }
}