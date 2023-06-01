package com.chinagoods.bigdata.functions.card;

import com.chinagoods.bigdata.functions.utils.CardUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * date: 2016-07-25
 * time: 20:12
 */
@Description(name = "id_card_area"
        , value = "_FUNC_(string) - get area by given china id card."
        , extended = "Example:\n > select _FUNC_(string) from src;")
public class UDFChinaIdCardArea extends UDF {
    private Text result = new Text();

    public UDFChinaIdCardArea() {
    }

    public Text evaluate(Text idCard) {
        if (idCard == null) {
            return null;
        }
        String area = CardUtils.getIdCardArea(idCard.toString());
        if (area == null) {
            return null;
        }
        result.set(area);
        return result;
    }
}
