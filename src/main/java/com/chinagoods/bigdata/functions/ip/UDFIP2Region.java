package com.chinagoods.bigdata.functions.ip;

import com.chinagoods.bigdata.functions.utils.ConfigUtils;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.lionsoul.ip2region.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

/**
 * @author xiaowei.song
 * date: 2020-12-7
 * time: 17:02
 */
@Description(name = "ip2region"
        , value = "_FUNC_(ip, pos) - Convert ip to region. returns a map created using the country|area|province|city|isp|city_id|region_content|data_ptr."
        , extended = "Example:\n > select _FUNC_(ip, pos) from src;")
public class UDFIP2Region extends UDF {
    public static final Logger logger = LoggerFactory.getLogger(UDFIP2Region.class);
    // 初始化结果值
    private Text result = new Text();

    public static DbConfig config;
    public static final String IP2REGION_DB_PATH = "/ip2region.db";
//    public static final String IP2REGION_DB_PATH = "/user/hive/warehouse/resource/ip2region.db";
    public volatile byte[] IP2REGION_DB_BYTES;
    static {
        try {
            config = new DbConfig();
        } catch (DbMakerConfigException e) {
            logger.error("ip2region maker config error", e);
            System.exit(-1);
        }
    }

    public static final Map<Integer, String> DEFAULT_IP_REGION_FILED_MAP = ImmutableMap.<Integer, String>builder()
            .put(0, "0")
            .put(1, "0")
            .put(2, "0")
            .put(3, "0")
            .put(4, "0")
            .put(5, "0")
            .put(6, "0|0|0|0|0")
            .put(7, "0")
            .build();

    public DbSearcher dbSearcher = null;

    public UDFIP2Region() {
    }

    public Text evaluate(String ip, int pos) throws HiveException {
        // 清理原始数据
        result.clear();

        ip = StringUtils.trim(ip);
        if(pos < 0 || pos > 7) {
            logger.error("pos value range 0-7");
            throw new HiveException("pos value range 0-7");
        }

        if (IP2REGION_DB_BYTES == null) {
            // 避免load就占用内存，使用才加载，否则不加载
            try {
                IP2REGION_DB_BYTES = ConfigUtils.loadBinFile(IP2REGION_DB_PATH);
            } catch (IOException e) {
                logger.error("loading ip2region.db error", e);
                throw new HiveException("loading ip2region.db error", e);
            }
            dbSearcher = new DbSearcher(config, IP2REGION_DB_BYTES);
            logger.info("load ip2region.db success, path={}, bytes length={}!!!", IP2REGION_DB_PATH, IP2REGION_DB_BYTES.length);
        }
        if (StringUtils.isBlank(ip) || StringUtils.equals("0000", ip)  ||
                StringUtils.equals("-", ip)) {
            result.set(DEFAULT_IP_REGION_FILED_MAP.get(pos));
            return result;
        }
        logger.debug("ip is not empty: {}", ip);
        // 若是非合法ip地址，返回null
        if (!Util.isIpAddress(ip)) {
            result.set(DEFAULT_IP_REGION_FILED_MAP.get(pos));
            return result;
        }

        logger.debug("ip passed legality check, ip: {}", ip);
        DataBlock db = null;
        try {
            db = dbSearcher.memorySearch(ip);
        } catch (IOException e) {
            logger.error("IO exception，details error：", e);
            throw new HiveException("IO exception，details error：", e);
        }
        if (db == null) {
            logger.error("Not exists ip: {}", ip);
            result.set(DEFAULT_IP_REGION_FILED_MAP.get(pos));
            return result;
        }
        logger.debug("search ip:{} return DataBlock: {}.", ip, db);
        String region = db.getRegion();
        String[] arr = StringUtils.split(region, '|');
        if (pos < 5) {
            result.set(arr[pos]);
        } else {
            if (pos == 5) {
                result.set(String.valueOf(db.getCityId()));
            } else if (pos == 6) {
                result.set(db.getRegion());
            } else {
                result.set(String.valueOf(db.getDataPtr()));
            }
        }
        return result;
    }

    public Text evaluate(Text ipT, IntWritable posI) throws HiveException {
        String ip = ipT.toString();
        int pos = posI.get();
        return new Text(evaluate(ip, pos));
    }

    public Text evaluate(Text ipT, Text posT) throws HiveException {
        String ip = ipT.toString();
        int pos = Integer.parseInt(posT.toString());
        return new Text(evaluate(ip, pos));
    }

    public Text evaluate(Text ipT, LongWritable posI) throws HiveException {
        String ip = ipT.toString();
        int pos = (int) posI.get();
        return new Text(evaluate(ip, pos));
    }

    public Text evaluate(Text ipT) throws HiveException {
        String ip = ipT.toString();
        return new Text(evaluate(ip, 6));
    }

    public static void main(String[] args) throws HiveException {
        UDFIP2Region udfip2Region = new UDFIP2Region();
        System.out.println(udfip2Region.evaluate("39.183.144.171", 0));

    }
}
