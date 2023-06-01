package com.chinagoods.bigdata.functions.string;

/**
 * @author xiaowei.song
 * 乱码恢复
 */
import com.chinagoods.bigdata.functions.utils.CgStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@Description(
        name = "messy_code_recover",
        value = "_FUNC_(messy_code_str) - this is a 乱码恢复方法",
        extended = "Example:\n > select _FUNC_(messy_code_str) from src;"
)
public class UDFMessyCodeRecover extends UDF {
    public static final Logger logger = LoggerFactory.getLogger(UDFMessyCodeRecover.class);

    private Text result = new Text();

    public Text evaluate(String messy_code_str) throws Exception {
        if (StringUtils.isBlank(messy_code_str)) {
            return null;
        }

        // 探测字符串值编码
        String charsetName = CgStringUtils.getEncoding(messy_code_str);
        String value = messy_code_str;
        switch (charsetName) {
            case CgStringUtils.ISO_8859_1:
                value = new String(messy_code_str.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                break;
            case CgStringUtils.GBK:
                value = new String(messy_code_str.getBytes(CgStringUtils.GBK), StandardCharsets.UTF_8);
        }
        result.set(value);
        return result;
    }

    public static void main(String[] args) throws Exception {
//                String str = "515æ´»å\u008A¨";
                String str = "515活动";
//        System.out.println(newStr);
        System.out.println((new UDFMessyCodeRecover ()).evaluate(str));

    }

}