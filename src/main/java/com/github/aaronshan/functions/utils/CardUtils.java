package com.github.aaronshan.functions.utils;

import com.github.aaronshan.functions.model.ChinaIdArea;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Map;

/**
 * @author ruifeng.shan
 * @date 2016-07-25
 * @time 19:35
 */
public class CardUtils {
    private static final Map<String, ChinaIdArea> chinaIdAreaMap = ConfigUtils.getIdCardMap();
    private static int[] weight = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};    //十七位数字本体码权重
    private static char[] validate = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};    //mod11,对应校验码字符值

    private static ChinaIdArea getCardValue(String card) {
        if (card == null) {
            return null;
        }

        int cardLength = card.length();
        //身份证只有15位或18位
        if (cardLength != 15 && cardLength != 18) {
            return null;
        }

        String cardPrefix = card.substring(0, 6);
        if (chinaIdAreaMap.containsKey(cardPrefix)) {
            return chinaIdAreaMap.get(cardPrefix);
        }

        return null;
    }

    public static String getIdCardProvince(String card) {
        ChinaIdArea chinaIdArea = getCardValue(card);
        if (chinaIdArea != null) {
            return chinaIdArea.getProvince();
        }

        return null;
    }

    public static String getIdCardCity(String card) {
        ChinaIdArea chinaIdArea = getCardValue(card);
        if (chinaIdArea != null) {
            return chinaIdArea.getCity();
        }

        return null;
    }

    public static String getIdCardArea(String card) {
        ChinaIdArea chinaIdArea = getCardValue(card);
        if (chinaIdArea != null) {
            return chinaIdArea.getArea();
        }

        return null;
    }

    public static String getIdCardBirthday(String card) {
        if (isValidIdCard(card)) {
            int cardLength = card.length();
            if (cardLength == 15) {
                return "19" + card.substring(6, 12);
            } else {
                return card.substring(6, 14);
            }
        }

        return null;
    }

    public static String getIdCardGender(String card) {
        if (isValidIdCard(card)) {
            int cardLength = card.length();
            int genderValue;
            if (cardLength == 15) {
                genderValue = card.charAt(15) - 48;
            } else {
                genderValue = card.charAt(17) - 48;
            }

            if (genderValue % 2 == 0) {
                return "女";
            } else {
                return "男";
            }
        }

        return null;
    }

    /**
     * 判断是否是正确的身份证号
     *
     * @param card
     * @return
     */
    public static boolean isValidIdCard(String card) {
        if (!Strings.isNullOrEmpty(card)) {
            int cardLength = card.length();
            //身份证只有15位或18位
            if (cardLength == 18) {
                String card17 = card.substring(0, 17);
                // 前17位必需都是数字
                if (!card17.matches("[0-9]+")) {
                    return false;
                }
                char validateCode = getValidateCode(card17);
                if (validateCode == card.charAt(17)) {
                    return true;
                }
            } else if (cardLength == 15) {
                if (!card.matches("[0-9]+") || getCardValue(card) == null) {
                    return false;
                }
                return true;
            }
        }

        return false;
    }

    /**
     * 获取正确的校验码
     *
     * @param card17 18位身份证前17位
     * @return
     */
    private static char getValidateCode(String card17) {
        int sum = 0, mode = 0;
        for (int i = 0; i < card17.length(); i++) {
            sum = sum + (card17.charAt(i) - 48) * weight[i];
        }
        mode = sum % 11;
        return validate[mode];
    }

    public static String getJsonOfChinaIdCard(String card) {
        try {
            Map<String, Object> map = Maps.newHashMap();
            ChinaIdArea chinaIdArea = getCardValue(card);
            if (chinaIdArea == null) {
                map.put("province", null);
                map.put("city", null);
                map.put("area", null);
            } else {
                map.put("province", chinaIdArea.getProvince());
                map.put("city", chinaIdArea.getCity());
                map.put("area", chinaIdArea.getArea());
            }

            map.put("gender", getIdCardGender(card));
            map.put("valid", isValidIdCard(card));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
