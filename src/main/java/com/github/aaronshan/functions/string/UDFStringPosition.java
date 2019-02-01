package cc.shanruifeng.functions.string;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2018-07-27 下午12:08
 */
@Description(name = "strpos"
        , value = "_FUNC_(string, substring) - returns index of first occurrence of a substring (or 0 if not found)."
        , extended = "Example:\n > select _FUNC_(string, substring) from src;")
public class UDFStringPosition extends UDF {
    private LongWritable result = new LongWritable(0);

    public UDFStringPosition() {
    }

    public LongWritable evaluate(Text text, Text subText) {
        if (text == null || subText == null) {
            return result;
        }

        if (subText.getLength() == 1) {
            result.set(1);
            return result;
        }

        int index = text.toString().indexOf(subText.toString());
        if (index < 0) {
            return result;
        }

        result.set(index + 1);
        return result;
    }
}
