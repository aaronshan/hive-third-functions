package com.chinagoods.bigdata.functions.risk;

import com.chinagoods.bigdata.functions.utils.MysqlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author xiaowei.song
 * date: 2022-03-23
 * time: 14:48
 */
@Description(name = "risk_keywords", value = "_FUNC_(str) - Determines whether the keyword is a prohibited word, if it returns comma string, otherwise returns null. "
        , extended = "Example:\n"
        + "  > SELECT _FUNC_(key_word) FROM src LIMIT 1;")
public class UDFRiskKeywords extends GenericUDF {

    private static final Logger logger = LoggerFactory.getLogger(UDFRiskKeywords.class);

    public static final String DB_URL = "jdbc:mysql://rm-uf6wr9aa537v0tesf3o.mysql.rds.aliyuncs.com:3306/source?characterEncoding=UTF-8&useSSL=false";
    public static final String DB_USER = "datax";
    public static final String DB_PASSWORD = "oRvmRrVJeOCl8XsY";
    public static final String SELECT_RISK_KEYWORDS_SQL = "select t.key_word\n" +
            "from risk_control_keywords t\n" +
            "inner join (\n" +
            "\tselect key_word\n" +
            "\t,max(create_time) max_create_time\n" +
            "\tfrom risk_control_keywords\n" +
            "\tgroup by key_word\n" +
            ") nt on t.create_time =  nt.max_create_time and t.key_word = nt.key_word\n" +
            "where t.is_deleted = '否'";

    private ObjectInspectorConverters.Converter[] converters;

    /**
     * Number of arguments to this UDF
     **/
    private static final int ARG_COUNT = 1;

    /**
     * 搜索关键词禁用表
     **/
    public Set<String> riskKeywordsSet = new HashSet<String>();

    public UDFRiskKeywords() {
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function search_keywords_sensitive(key_word) takes exactly " + ARG_COUNT + " arguments.");
        }

        converters = new ObjectInspectorConverters.Converter[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            converters[i] = ObjectInspectorConverters.getConverter(arguments[i],
                    PrimitiveObjectInspectorFactory.writableStringObjectInspector);
        }

        // 查询现有搜索引擎禁用词
        MysqlUtil mysqlUtil = new MysqlUtil(DB_URL, DB_USER, DB_PASSWORD);
        try {
            riskKeywordsSet = mysqlUtil.getKeywords(SELECT_RISK_KEYWORDS_SQL);
        } catch (SQLException e) {
            throw new UDFArgumentException(String.format("Failed to query the set of prohibited words in the risk database, the error details are: %s", e));
        }

        return PrimitiveObjectInspectorFactory.javaStringObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        assert (arguments.length == ARG_COUNT);

        if (arguments[0].get() == null || StringUtils.isBlank(arguments[0].get().toString())) {
            return null;
        }
        Set<String> hitKeywordSet = new TreeSet<>();

        String goodName;
        try {
            goodName = converters[0].convert(arguments[0].get()).toString();
        } catch (Exception e) {
            logger.error("Type conversion failed", e);
            return null;
        }

        for (String keyword : riskKeywordsSet) {
            if (StringUtils.contains(goodName, keyword)) {
                hitKeywordSet.add(keyword);
            }
        }
        return StringUtils.join(hitKeywordSet, ",");
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "risk_keywords(" + strings[0] + ")";
    }
}
