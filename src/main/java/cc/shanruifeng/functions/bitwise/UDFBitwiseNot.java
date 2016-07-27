package cc.shanruifeng.functions.bitwise;

import org.apache.hadoop.hive.ql.exec.Description;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 15:50
 */
@Description(name = "bitwise_not"
        , value = "_FUNC_(x) - returns the bitwise NOT of x in 2’s complement arithmetic."
        , extended = "Example:\n > select _FUNC_(9) from src;")
public class UDFBitwiseNot {
}
