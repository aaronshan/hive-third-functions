package com.chinagoods.bigdata.functions.string;

/**
 * @author xiaowei.song
 * @version v1.0.0
 * @description TODO
 * @date 2021/5/7 16:04
 */
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

@Description(
        name = "full2half",
        value = "_FUNC_(full_string) - this is a 全角转化半角 util,若为空，则返回空字符串",
        extended = "Example:\n > select _FUNC_(full_string) from src;"
)
public class UDFFull2Half extends UDF {

    public UDFFull2Half() {
    }

    public String evaluate(String fullStr) throws Exception {
        if (StringUtils.isBlank(fullStr)) {
            return "";
        }

        StringBuilder outStrBuf = new StringBuilder("");
        String tStr = "";
        byte[] b = null;
        for (int i = 0; i < fullStr.length(); i++) {
            tStr = fullStr.substring(i, i + 1);
            // 全角空格转换成半角空格
            if (tStr.equals("　")) {
                outStrBuf.append(" ");
                continue;
            }

            b = tStr.getBytes("unicode");
            // 得到 unicode 字节数据
            if (b[2] == -1) { // 表示全角？
                b[3] = (byte) (b[3] + 32);
                b[2] = 0;
                outStrBuf.append(new String(b, "unicode"));
            } else {
                outStrBuf.append(tStr);
            }
        }
        // end for.
        return outStrBuf.toString();
    }

}