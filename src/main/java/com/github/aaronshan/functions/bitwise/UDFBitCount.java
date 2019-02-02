package com.github.aaronshan.functions.bitwise;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.LongWritable;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 15:49
 */
@Description(name = "bit_count"
        , value = "_FUNC_(x, bits) - count the number of bits set in x (treated as bits-bit signed integer) in 2’s complement representation."
        , extended = "Example:\n > select _FUNC_(9, 64) from src;")
public class UDFBitCount extends UDF {
    private LongWritable result = new LongWritable();

    public LongWritable evaluate(long num, long bits) throws HiveException {
        if (bits == 64) {
            result.set(Long.bitCount(num));
            return result;
        }
        if (bits <= 1 || bits > 64) {
            throw new HiveException("Bits specified in bit_count must be between 2 and 64, got " + bits);
        }
        long lowBitsMask = (1 << (bits - 1)) - 1; // set the least (bits - 1) bits
        if (num > lowBitsMask || num < ~lowBitsMask) {
            throw new HiveException("Number must be representable with the bits specified. " + num + " can not be represented with " + bits + " bits");
        }
        long mask = lowBitsMask | 0x8000000000000000L; // set the least (bits - 1) bits and the sign bit
        result.set(Long.bitCount(num & mask));
        return result;
    }
}