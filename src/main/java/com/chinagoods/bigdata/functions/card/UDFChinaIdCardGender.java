package com.chinagoods.bigdata.functions.card;

import com.chinagoods.bigdata.functions.utils.CardUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * date: 2016-07-25
 * time: 20:14
 */
@Description(name = "id_card_gender"
        , value = "_FUNC_(string) - get gender by given china id card."
        , extended = "Example:\n > select _FUNC_(string) from src;")
public class UDFChinaIdCardGender extends UDF {
    private Text result = new Text();

    public UDFChinaIdCardGender() {
    }

    public Text evaluate(Text idCard) {
        if (idCard == null) {
            return null;
        }
        String gender = CardUtils.getIdCardGender(idCard.toString());
        if (gender == null) {
            return null;
        }
        result.set(gender);

        return result;
    }
}
