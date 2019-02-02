package com.github.aaronshan.functions.string;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * Created by ruifengshan on 16/3/18.
 */
@Description(name = "md5"
        , value = "_FUNC_(string) - get md5 hash code by given input string."
        , extended = "Example:\n > select _FUNC_(string) from src;")
public class UDFMd5 extends UDF {
    private Text result = new Text();

    public UDFMd5() {
    }

    /**
     * md5 hash.
     *
     * @param text
     * @return
     */
    public Text evaluate(Text text) {
        if (text == null) {
            return null;
        }

        result.set(DigestUtils.md5Hex((text.toString())));
        return result;
    }
}
