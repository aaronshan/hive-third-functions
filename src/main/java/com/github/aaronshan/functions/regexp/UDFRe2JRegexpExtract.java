package com.github.aaronshan.functions.regexp;

import io.airlift.slice.Slices;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2018-07-27
 * @time 22:38
 */
@Description(name = "regexp_extract"
        , value = "_FUNC_(string, string) - returns substrings matching a regular expression."
        , extended = "Example:\n > select _FUNC_(string, pattern) from src;")
public class UDFRe2JRegexpExtract extends UDF {
    private static Re2JRegexp re2JRegexp;
    private Text result = new Text();

    public UDFRe2JRegexpExtract() {

    }

    public Text evaluate(Text source, Text pattern) throws HiveException {
        return evaluate(source, pattern, new LongWritable(0));
    }

    public Text evaluate(Text source, Text pattern, LongWritable groupIndex) throws HiveException {
        if (source == null) {
            return null;
        }

        if (re2JRegexp == null) {
            re2JRegexp = new Re2JRegexp(Integer.MAX_VALUE, 5, Slices.utf8Slice(pattern.toString()));
        }

        result.set(re2JRegexp.extract(Slices.utf8Slice(source.toString()), groupIndex.get()).toStringUtf8());
        return result;
    }
}
