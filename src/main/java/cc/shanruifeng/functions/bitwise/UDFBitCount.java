package cc.shanruifeng.functions.bitwise;

import org.apache.hadoop.hive.ql.exec.Description;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 15:49
 */
@Description(name = "bit_count"
        , value = "_FUNC_(x, bits) - count the number of bits set in x (treated as bits-bit signed integer) in 2’s complement representation."
        , extended = "Example:\n > select _FUNC_(9, 64) from src;")
public class UDFBitCount {
}
