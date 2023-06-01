package com.chinagoods.bigdata.functions.url;

import com.chinagoods.bigdata.functions.utils.MysqlUtil;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UDFStandardUrlFormatTest {
    private static final Logger logger = LoggerFactory.getLogger(UDFStandardUrlFormat.class);
    private static final String DB_URL = "jdbc:mysql://rm-uf6wr9aa537v0tesf3o.mysql.rds.aliyuncs.com:3306/source?characterEncoding=UTF-8&useSSL=false";
    private static final String DB_USER = "datax";
    private static final String DB_PASSWORD = "oRvmRrVJeOCl8XsY";


    @Test
    public void testUrlEncode() throws Exception {
        UDFStandardUrlFormat udf = new UDFStandardUrlFormat();
        ObjectInspector platform_type = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        ObjectInspector sc_url = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        ObjectInspector[] arguments = {platform_type, sc_url};
        udf.initialize(arguments);

        ArrayList<Text> reslist = null;
        GenericUDF.DeferredObject sourceObj = new GenericUDF.DeferredJavaObject("pc");
        GenericUDF.DeferredObject patternObj = new GenericUDF.DeferredJavaObject("https://news.chinagoods.com/information/11/search/减免租金");
        GenericUDF.DeferredObject[] args = {sourceObj, patternObj};
        reslist = udf.evaluate(args);
        System.out.println(reslist);

//        String RULE_SQL = "select platform_type,sc_url from test2 where" +
//                 " sc_url not like 'http://localhost%' " +
//                "and sc_url not like 'https://localhost%' " +
//                "and sc_url not like 'http://%.%.%.%' " +
//                "and sc_url not like 'https://%.%.%.%' " +
//                "and sc_url not like '%cgb.chinagoods.com%'";

//        String RULE_SQL = "select platform_type,sc_url from standard_rule_url where regex is not null";
//        MysqlUtil mysqlUtil = new MysqlUtil(DB_URL, DB_USER, DB_PASSWORD);
//        List<List<String>> list = mysqlUtil.getLists(RULE_SQL);
//        int a = 0;
//        for (List<String> r:list) {
//            ArrayList<Text> reslist1 = new ArrayList<>();
//            List<String> array = null;
//            try {
//                GenericUDF.DeferredObject sourceObj1 = new GenericUDF.DeferredJavaObject(r.get(0));
//                GenericUDF.DeferredObject patternObj1 = new GenericUDF.DeferredJavaObject(r.get(1));
//                GenericUDF.DeferredObject[] args1 = {sourceObj1, patternObj1};
//                reslist1 = udf.evaluate(args1);
//            } catch (Exception e) {
//                e.printStackTrace();
//                System.out.println(array);
//            }
//            if (reslist1.size() != 4 || reslist1.get(0).toString().equals("0000")) {
//                a++;
//                System.out.println(a+"************************");
//                System.out.println(r.get(0) + "-----" + r.get(1) + "-----" + reslist1);
//            }
//        }

    }
}
