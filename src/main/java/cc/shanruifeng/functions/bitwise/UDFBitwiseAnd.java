package cc.shanruifeng.functions.bitwise;

import org.apache.hadoop.hive.ql.exec.Description;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 15:49
 */
@Description(name = "bitwise_and"
        , value = "_FUNC_(x, y) - returns the bitwise AND of x and y in 2’s complement arithmetic."
        , extended = "Example:\n > select _FUNC_(x, y) from src;")
public class UDFBitwiseAnd {
}
