package com.chinagoods.bigdata.functions.card;

import com.chinagoods.bigdata.functions.utils.CardUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author ruifeng.shan
 * date: 2016-07-25
 * time: 20:15
 */
@Description(name = "id_card_15to18"
        , value = "_FUNC_(string) - Convert 15-digit ID card number into 18-digit"
        , extended = "Example:\n > select _FUNC_(string) from src;")
public class UDFChinaIdCard15to18 extends UDF {
    private static final Logger log = LoggerFactory.getLogger(UDFChinaIdCard15to18.class);

    private Text result = new Text();

    // 每位加权因子
    private static final int[] power = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

    public UDFChinaIdCard15to18() {
    }

    public Text evaluate(Text idCard) {
        if (idCard == null) {
            return null;
        }
        result.set(convertIdCardBy15bit(idCard.toString()));
        return result;
    }

    /**
     * 将15位的身份证转成18位身份证
     *
     * @param idCard 身份证号码
     * @return idCard 转换完成的身份证号码
     */
    public static String convertIdCardBy15bit(String idCard) {
        String idCard17 = null;
        // 非15位身份证
        if (idCard.length() != 15) {
            return idCard;
        }

        if (isDigital(idCard)) {
            // 获取出生年月日
            String birthday = idCard.substring(6, 12);
            Date birthDate = null;
            try {
                birthDate = new SimpleDateFormat("yyMMdd").parse(birthday);
            } catch (ParseException e) {
                log.error("解析身份证位数失败，身份证号码错误, {}", idCard, e);
                return "";
            }
            Calendar cDay = Calendar.getInstance();
            cDay.setTime(birthDate);
            String year = String.valueOf(cDay.get(Calendar.YEAR));

            idCard17 = idCard.substring(0, 6) + year + idCard.substring(8);

            char[] c = idCard17.toCharArray();
            String checkCode = "";

            int[] bit;

            // 将字符数组转为整型数组
            bit = convertCharToInt(c);
            int sum17 = 0;

            sum17 = getPowerSum(bit);

            // 获取和值与11取模得到余数进行校验码
            checkCode = getCheckCodeBySum(sum17);
            // 获取不到校验位
            if (null == checkCode) {
                return "";
            }

            // 将前17位与第18位校验码拼接
            idCard17 += checkCode;
        } else {
            // 身份证包含数字
            log.warn("身份证号码非法! 号码: {}", idCard);
            return "";
        }
        return idCard17;
    }

    /**
     * 数字验证
     *
     * @param str
     * @return
     */
    public static boolean isDigital(String str) {
        return str != null && !"".equals(str) && str.matches("^[0-9]*$");
    }

    /**
     * 将字符数组转为整型数组
     *
     * @param c
     * @return
     * @throws NumberFormatException
     */
    public static int[] convertCharToInt(char[] c) throws NumberFormatException {
        int[] a = new int[c.length];
        int k = 0;
        for (char temp : c) {
            a[k++] = Integer.parseInt(String.valueOf(temp));
        }
        return a;
    }


    /**
     * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
     *
     * @param bit
     * @return
     */
    public static int getPowerSum(int[] bit) {

        int sum = 0;

        if (power.length != bit.length) {
            return sum;
        }

        for (int i = 0; i < bit.length; i++) {
            for (int j = 0; j < power.length; j++) {
                if (i == j) {
                    sum = sum + bit[i] * power[j];
                }
            }
        }
        return sum;
    }

    /**
     * 将和值与11取模得到余数进行校验码判断
     *
     * @param sum17
     * @return 校验位
     */
    public static String getCheckCodeBySum(int sum17) {
        String checkCode = null;
        switch (sum17 % 11) {
            case 10:
                checkCode = "2";
                break;
            case 9:
                checkCode = "3";
                break;
            case 8:
                checkCode = "4";
                break;
            case 7:
                checkCode = "5";
                break;
            case 6:
                checkCode = "6";
                break;
            case 5:
                checkCode = "7";
                break;
            case 4:
                checkCode = "8";
                break;
            case 3:
                checkCode = "9";
                break;
            case 2:
                checkCode = "X";
                break;
            case 1:
                checkCode = "0";
                break;
            case 0:
                checkCode = "1";
                break;
        }
        return checkCode;
    }

    public static void main(String[] args) {
        UDFChinaIdCard15to18 udf = new UDFChinaIdCard15to18();
        System.out.println(udf.evaluate(new Text("330621690111306")));
    }

}
