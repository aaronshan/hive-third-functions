package com.github.aaronshan.functions.string;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: ruifengshan
 * Date: 23/03/2015
 */
@Description(name = "pinyin"
        , value = "_FUNC_(string) - get pinyin by given chinese."
        , extended = "Example:\n > select _FUNC_(string) from src;")
public class UDFChineseToPinYin extends UDF {
    private Text result = new Text();

    public UDFChineseToPinYin() {

    }

    /**
     * convert chinese han zi to pinyin.
     *
     * @param chinese
     * @return
     */
    public Text evaluate(Text chinese) {
        if (chinese == null) {
            return null;
        }

        result.set(ConvertToPinyin(chinese.toString()));
        return result;
    }

    //convert chinese to pinyin.
    public String ConvertToPinyin(String name) {
        HanyuPinyinOutputFormat pyFormat = new HanyuPinyinOutputFormat();
        pyFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        pyFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        pyFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

        String result = null;
        try {
            result = PinyinHelper.toHanyuPinyinString(name, pyFormat, "");
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            return null;
        }

        return result;
    }

}
