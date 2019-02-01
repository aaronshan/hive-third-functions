package com.github.aaronshan.functions.card;

import com.github.aaronshan.functions.utils.CardUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * Created by ruifengshan on 16/3/22.
 */

//身份证->json
@Description(name = "id_card_info"
        , value = "_FUNC_(string) - get all info by given china id card, output is json string."
        , extended = "Example:\n > select _FUNC_(string) from src;")
public class UDFChinaIdCardInfo extends UDF {
    private Text result = new Text();

    public UDFChinaIdCardInfo() {
    }

    public Text evaluate(Text idCard) {
        if (idCard == null) {
            return null;
        }
        result.set(CardUtils.getJsonOfChinaIdCard(idCard.toString()));
        return result;
    }
}
