package com.github.aaronshan.functions.string;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-25
 * @time 14:29
 */
@Description(name = "sha256"
        , value = "_FUNC_(string) - get sha256 hash code by given input string."
        , extended = "Example:\n > select _FUNC_(string) from src;")
public class UDFSha256 extends UDF {
    private Text result = new Text();

    public UDFSha256() {
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

        result.set(DigestUtils.sha256Hex((text.toString())));
        return result;
    }
}
