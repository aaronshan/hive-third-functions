package cc.shanruifeng.functions.regexp;

import io.airlift.slice.Slices;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2018-07-27
 * @time 22:36
 */
@Description(name = "regexp_like"
        , value = "_FUNC_(string, string) - returns substrings matching a regular expression."
        , extended = "Example:\n > select _FUNC_(string, pattern) from src;")
public class UDFRe2JRegexpLike extends UDF {
    private static Re2JRegexp re2JRegexp;

    public UDFRe2JRegexpLike() {

    }

    public boolean evaluate(Text text, Text pattern) {
        if (text == null) {
            return false;
        }

        if (re2JRegexp == null) {
            re2JRegexp = new Re2JRegexp(Integer.MAX_VALUE, 5, Slices.utf8Slice(pattern.toString()));
        }

        return re2JRegexp.matches(Slices.utf8Slice(text.toString()));
    }

}
