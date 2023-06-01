package com.chinagoods.bigdata.functions.utils;

import com.chinagoods.bigdata.functions.model.ChinaIdArea;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ruifeng.shan
 * date: 2016-07-07
 * time: 16:21
 */
public class ConfigUtils {
    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    public static Configuration conf = null;

    public static List<String> loadFile(String fileName) throws IOException {
        ArrayList<String> strings = Lists.newArrayList();
        Closer closer = Closer.create();
        try {
            InputStream inputStream = ConfigUtils.class.getResourceAsStream(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
            closer.register(bufferedReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (Strings.isNullOrEmpty(line) || line.startsWith("#")) {
                    continue;
                }
                strings.add(line);
            }
        } catch (IOException e) {
            logger.error("loadFile {} error. error is {}.", fileName, e);
            throw e;
        } finally {
            closer.close();
        }

        return strings;
    }

    public static Configuration getHDFSConf(){
        if (conf == null){
            //这里的路径是在hdfs上的存放路径，但是事先需将hdfs-site.xml文件放在工程的source文件下，这样才能找到hdfs
            conf = new Configuration();
        }
        return conf;
    }

    public static byte[] loadBinFile(String fileName) throws IOException {
//        ByteBuffer bbf = ByteBuffer.allocateDirect(8_733_094 + 1024);
//        byte[] bytes = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Closer closer = Closer.create();
        try {
//            // 一次性载入，速度最快
//            InputStream is = ConfigUtils.class.getResourceAsStream(fileName);
//            closer.register(is);
//            bytes = new byte[is.available()];
//            is.read(bytes);

//            FileSystem fs = FileSystem.newInstance(getHDFSConf());
//            Path remotePath = new Path(fileName);
//            FSDataInputStream in = fs.open(remotePath);
//            closer.register(in);
//            byte[] buffer = new byte[4096];
//            int n = 0;
//            while (-1 != (n = in.read(buffer))) {
//                output.write(buffer, 0, n);
//            }
//            bytes = new byte[in.available()];
//            in.read(bytes);

            // 设置缓冲器大小, 4k
            final int buffSize = 4096;
            byte[] bf = new byte[buffSize];
            InputStream in = ConfigUtils.class.getResourceAsStream(fileName);
            closer.register(in);
            byte[] buffer = new byte[buffSize];
            int n = 0;
            while (-1 != (n = in.read(buffer))) {
                output.write(buffer, 0, n);
            }
        } catch (IOException e) {
            logger.error("loadFile {} error. error is {}.", fileName, e);
            throw e;
        } finally {
            closer.close();
        }
        // 转换模式
//        bbf.flip();
//        byte[] bytes = new byte[bbf.remaining()];
//        bbf.get(bytes);
        return output.toByteArray();
    }

    public static Map<String, ChinaIdArea> getIdCardMap() {
        String fileName = "/china_p_c_a.config";
        Map<String, ChinaIdArea> map = Maps.newHashMap();
        try {
            List<String> list = loadFile(fileName);
            for (String line : list) {
                String[] results = line.split("\t", 4);
                map.put(results[0], new ChinaIdArea(results[1], results[2], results[3]));
            }
        } catch (IOException e) {
            logger.error("get china id card map error. error is {}.", e);
            return map;
        }

        return map;
    }

    public static Map<String, String> getDayMap() {
        String fileName = "/china_day_type.config";
        Map<String, String> map = Maps.newHashMap();
        try {
            List<String> list = loadFile(fileName);
            for (String line : list) {
                String[] results = line.split("\t", 2);
                map.put(results[0], results[1]);
            }
        } catch (IOException e) {
            logger.error("get day map error. error is {}.", e);
            return map;
        }

        return map;
    }
}
