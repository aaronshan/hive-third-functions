package cc.shanruifeng.functions.date;

import org.apache.hadoop.hive.ql.exec.Description;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 15:58
 */
@Description(name = "day_of_year"
        , value = "_FUNC_(date) - returns the day of the year from x. The value ranges from 1 to 366.."
        , extended = "Example:\n > select _FUNC_(date_string) from src;\n > select _FUNC_(date) from src;")
public class UDFDayOfYear {
}
