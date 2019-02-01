package cc.shanruifeng.functions.regexp;

import io.airlift.slice.Slices;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2018-07-27
 * @time 22:37
 */
@Description(name = "regexp_replace"
        , value = "_FUNC_(string, string) - removes substrings matching a regular expression\n" +
        "_FUNC_(string, string, string) - replaces substrings matching a regular expression by given string."
        , extended = "Example:\n > select _FUNC_(string, pattern) from src;\n" +
        "select _FUNC_(string, pattern, replacement) from src;")
public class UDFRe2JRegexpReplace extends UDF {
    private static Re2JRegexp re2JRegexp;
    private Text result = new Text();

    public UDFRe2JRegexpReplace() {

    }

    public Text evaluate(Text source, Text pattern) throws HiveException {
        return evaluate(source, pattern, new Text(Slices.EMPTY_SLICE.toStringUtf8()));
    }

    public Text evaluate(Text source, Text pattern, Text replacement) throws HiveException {
        if (source == null) {
            return null;
        }

        if (re2JRegexp == null) {
            re2JRegexp = new Re2JRegexp(Integer.MAX_VALUE, 5, Slices.utf8Slice(pattern.toString()));
        }

        result.set(re2JRegexp.replace(Slices.utf8Slice(source.toString()), Slices.utf8Slice(replacement.toString())).toStringUtf8());
        return result;
    }

}
