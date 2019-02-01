package com.github.aaronshan.functions.string;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.Text;

import java.text.Normalizer;

/**
 * @author ruifeng.shan
 * @date 2018-07-27 下午12:37
 */
@Description(name = "normalize"
        , value = "_FUNC_(string, string) - transforms the string to normalized form."
        , extended = "Example:\n > select _FUNC_(string, form_str) from src;")
public class UDFStringNormalize extends UDF {
    private Text result = new Text();

    public UDFStringNormalize() {
    }

    public Text evaluate(Text text, Text form) throws HiveException {
        if (text == null) {
            return null;
        }

        Normalizer.Form targetForm;
        try {
            targetForm = Normalizer.Form.valueOf(form.toString());
        }
        catch (IllegalArgumentException e) {
            throw new HiveException("Normalization form must be one of [NFD, NFC, NFKD, NFKC]");
        }

        result.set(Normalizer.normalize(text.toString(), targetForm));
        return result;
    }
}