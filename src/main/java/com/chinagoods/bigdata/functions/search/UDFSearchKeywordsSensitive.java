package com.chinagoods.bigdata.functions.search;

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

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author xiaowei.song
 * date: 2022-03-07
 * time: 14:48
 */
@Description(name = "search_keywords_sensitive", value = "_FUNC_(str) - Determines whether the keyword is a prohibited word, if it returns 1, otherwise returns 0. "
        , extended = "Example:\n"
        + "  > SELECT _FUNC_(key_word) FROM src LIMIT 1;")
public class UDFSearchKeywordsSensitive extends GenericUDF {

    public static final String DB_URL = "jdbc:mysql://172.18.7.7:3306/cg_search?characterEncoding=UTF-8&useSSL=false";
    public static final String DB_USER = "cg_search";
    public static final String DB_PASSWORD = "GPuBoTWz3UiMwwLz";
    public static final String SELECT_SENSITIVE_KEYWORDS_SQL = "select words key_word\n" +
            "from lexicon_sensitive ls\n" +
            "where 1=1\n" +
            "and status = 0\n" +
            "and is_deleted = 0";

    private ObjectInspectorConverters.Converter[] converters;

    /**
     * Number of arguments to this UDF
     **/
    private static final int ARG_COUNT = 1;

    /**
     * 搜索关键词禁用表
     **/
    public Set<String> keywordsSensitive = new HashSet<String>();

    public UDFSearchKeywordsSensitive() {
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
            keywordsSensitive = mysqlUtil.getKeywords(SELECT_SENSITIVE_KEYWORDS_SQL);
        } catch (SQLException e) {
            throw new UDFArgumentException(String.format("Failed to query the set of prohibited words in the search engine database, the error details are: %s", e));
        }

        return PrimitiveObjectInspectorFactory
                .javaIntObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        assert (arguments.length == ARG_COUNT);

        if (arguments[0].get() == null || StringUtils.isBlank(arguments[0].get().toString())) {
            return 0;
        }

        try {
            String keywords = converters[0].convert(arguments[0].get()).toString();
            for (String sensitiveKeyword : keywordsSensitive) {
                if (StringUtils.contains(keywords, sensitiveKeyword)) {
                    return 1;
                }
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "search_keywords_sensitive(" + strings[0] + ")";
    }
}
