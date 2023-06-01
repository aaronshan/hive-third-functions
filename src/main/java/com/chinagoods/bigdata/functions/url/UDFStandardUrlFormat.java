package com.chinagoods.bigdata.functions.url;

import com.chinagoods.bigdata.functions.utils.MysqlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zyl
 * date: 2022-09-24
 * time: 16:30
 * describe: 此函数从基础规则表standard_rule_url，特殊url规则表standard_special_url查询相关规则进行匹配
 * 上述规则数据基于k-v映射表standard_url_param_maping动态生成固定url相关数据，参考见数据库触发器standard_url_param_maping_trigger（依赖于market_industry，sys_map，category，venue，active_meeting，cms_category，cms_article表数据）
 * standard_rule_url配置：1.无参url（包含参数在url中，不在问号后的）不用配置正则 2.有参url按照正则替换，正则可多个，用/&/分割，params参数也用/&/分割，空使用NULL字符串代替，其他使用0000代替 3.正则中\\使用\ 4.规则配置时，如果原始url符合特殊url表standard_special_url中规则，则正则表达式字段不能为空，否则处理不了
 * standard_special_url配置：1、url,unit,sub_unit必填 2.前置条件param_type为1代表参数替换；fixed_identity代表url包含这个字符串就是按照次规则匹配 3.fixed_param字段参数包含0000，用mapping_key的key中---后的部分替换0000部分；值为fixed_param字符串的，则使用mapping_key的key中---后的部分直接替换fixed_param，如果url_param_keys有值，则按照顺序拼接作为参数直接替换fixed_param（如code=112&active_code=112&id=3）3.前置条件param_type为2代表参数替换；用mapping_key的key中---后的部分替换fixed_param；4.mapping_key的标识必须和standard_url_param_maping中的key_desc的前缀保持一致
 */
@Description(name = "standard_url_format"
        , value = "_FUNC_(string,string) - Normalize the url according to the rule, returning an array of processed urls and a tertiary directory name if present, or an array of default values if absent"
        , extended = "Example:\n> SELECT _FUNC_(platform_type,sc_url) FROM src;")
public class UDFStandardUrlFormat extends GenericUDF {
    private static final Logger logger = LoggerFactory.getLogger(UDFStandardUrlFormat.class);
//    private static final String DB_URL = "jdbc:mysql://rm-uf6wr9aa537v0tesf3o.mysql.rds.aliyuncs.com:3306/source?characterEncoding=UTF-8&useSSL=false";
//    private static final String DB_USER = "datax";
//    private static final String DB_PASSWORD = "oRvmRrVJeOCl8XsY";
    private static final String DB_URL = "jdbc:mysql://172.18.5.10:23307/source?characterEncoding=UTF-8&useSSL=false";
    private static final String DB_USER = "source";
    private static final String DB_PASSWORD = "jP8*dKw,bRjBVos=";
    /**
     * 菜单映射信息,该表是mysql触发器逻辑生成的数据，触发器名称 standard_url_param_maping_trigger
     */
    private static final String MENU_MAPING_SQL = "select key_desc,value_desc from standard_url_param_maping";
    /**
     * 特殊URL
     */
    private static final String STANDARD_SPECIAL_URL_SQL = "select url,fixed_identity,fixed_param,mapping_key,unit,sub_unit,url_param_keys,param_type,page_link_name from standard_special_url where url_type=%s";
    /**
     * 标准url匹配规则信息
     */
    private static final String RULE_SQL = "select platform_type,case when standard_url like '%://h%' then 'Y' else 'N' end is_h5,standard_url, regex, unit,sub_unit,page_name,params from standard_rule_url " +
            "where lang = 'zh' and unit is not null and sub_unit is not null and page_name is not null and platform_type is not null and sc_url!='' and regex is not null and regex!=''";
    /**
     * 静态URL信息
     */
    private static final String STATIC_URL_SQL = "select standard_url, concat(unit,'---',sub_unit,'---',page_name) url_name from standard_rule_url where lang = 'zh' and (regex is null or regex = '') and standard_url is not null";
    /**
     * 特殊URL信息
     **/
    private List<List<String>> menuUrlList = new ArrayList<>();
    private List<List<String>> specialUrlList = new ArrayList<>();
    /**
     * 参数k-v映射信息
     **/
    private Map<String, String> paramKvMap = new HashMap<>();
    /**
     * 标准化URL规则
     **/
    private List<List<String>> standardUrlList = new ArrayList<>();
    /**
     * 汇总规则
     */
    private Map<String, List<List<String>>> allRuleMap = new HashMap<String, List<List<String>>>();
    private Map<String, String> staticUrlMap = new HashMap<String, String>();
    private ObjectInspectorConverters.Converter[] converters;
    private static final int ARG_COUNT = 2;
    private static final String HTTP_PREFIX = "http:";
    private static final String HTTPS_PREFIX = "https:";
    private static final String H5_PREFIX = "://h";
    private static final String FLAG = "Y";
    private static final String ONE = "1";
    private static final String TWO = "2";
    private static final String EMPTY = "";
    //约定空字符用NULL替换
    private static final String PARAM_NULL = "NULL";
    private static final String SEPARATOR = "---";
    private static final String KV_SEPARATOR = "--";
    private static final String KV_VALUE_SEPARATOR = "-";
    private static final String CONNECTOR_SEPARATOR = "?";
    private static final String REGEX_OR_PARAM_SEPARATOR = "/&/";
    private static final String BACKSLASH = "/";
    private static final String PARAM_SEPARATOR = "&";
    private static final String EQ = "=";
    private static final String COMMA = ",";
    /**
     * 固定参数
     */
    private static final String T = "T--";
    /*导航菜单商品分类中的分类编码*/
    private static final String C = "C--";
    /*导航菜单市场区块中的市场编码*/
    private static final String Z = "Z--";
    /*导航菜单商品分类中的市场编码*/
    private static final String M = "M--";
    /*商品列表实力商家筛选按钮*/
    private static final String EY = "EY--";
    /*商品列表甄选商家筛选按钮*/
    private static final String VL = "VL--";
    /*商品列表生产厂家筛选按钮*/
    private static final String SF = "SF--";
    /*指定的市场id*/
    private static final String LT = "LT--";
    /*商品列表综合，销售、价格、最新、有视频排序按钮*/
    private static final String S = "S--";
    private static final String IMT = "IMT--";
    /*商品列表当前页数*/
    private static final String P = "P--";
    /*商品列表当前页条数*/
    private static final String I = "I--";
    /*商品列表当前页条数*/
    private static final String HR = "HR--";
    /*是否展开全部*/
    private static final String EP = "EP--";

    private static final String STANDARD_ZERO = "0000";
    private static final String H5 = "h5";
    private static final String FIXED_PARAM = "fixed_param";
    /**
     * 返回结果list元素
     */
    private String platFormType = null;
    private String standardUrl = null;
    private String unit = null;
    private String subUnit = null;
    private String pageName = null;
    private String regex = null;
    private String params = null;
    private ArrayList<Text> resultPageNameList = null;
    private String multipleUrl="search/categoryProduct";

    public UDFStandardUrlFormat() {
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentException(
                    "The function standard_url_format takes exactly " + ARG_COUNT + " arguments.");
        }
        converters = new ObjectInspectorConverters.Converter[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            converters[i] = ObjectInspectorConverters.getConverter(arguments[i],
                    PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        }
        initRules();
        return ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
    }

    @Override
    public ArrayList<Text> evaluate(DeferredObject[] arguments) throws HiveException {
        assert (arguments.length == ARG_COUNT);
        initParam();
        String scUrl = null;
        if (arguments == null || arguments.length < 2) {
            return resultPageNameList;
        } else {
            platFormType = converters[0].convert(arguments[0].get()).toString();
            scUrl = converters[0].convert(arguments[1].get()).toString();
            if (StringUtils.isBlank(scUrl) || StringUtils.isBlank(platFormType)) {
                return resultPageNameList;
            }
            //标准化url
            if (scUrl.contains(HTTP_PREFIX)) {
                scUrl = scUrl.replace(HTTP_PREFIX, HTTPS_PREFIX);
            }
            if (scUrl.contains(HTTPS_PREFIX)) {
                if (scUrl.contains(CONNECTOR_SEPARATOR) && !scUrl.contains(EQ + HTTP_PREFIX) && !scUrl.contains(EQ + HTTPS_PREFIX)) {
                    String requestUrl = scUrl.substring(0, scUrl.indexOf(CONNECTOR_SEPARATOR));
                    String requestUrlParam = scUrl.substring(scUrl.indexOf(CONNECTOR_SEPARATOR), scUrl.length());
                    scUrl = requestUrl.lastIndexOf(BACKSLASH) + 1 == requestUrl.length() ? scUrl : requestUrl + BACKSLASH + requestUrlParam;
                } else if (scUrl.contains(CONNECTOR_SEPARATOR) && (scUrl.contains(EQ + HTTP_PREFIX) || scUrl.contains(EQ + HTTPS_PREFIX))) {
                    scUrl = scUrl + BACKSLASH;
                } else {
                    scUrl = scUrl.lastIndexOf(BACKSLASH) + 1 == scUrl.length() ? scUrl : scUrl + BACKSLASH;
                }
            }
        }
        meunDealUrl(scUrl);
        resultPageNameList = specialParamDealUrl(scUrl);
        if (resultPageNameList.get(0).toString().equals(STANDARD_ZERO)) {
            resultPageNameList = regexDealUrl(scUrl);
        }
        return resultPageNameList;
    }

    /**
     * 初始化规则信息
     *
     * @throws UDFArgumentException
     */
    public void initRules() throws UDFArgumentException {
        // 配置信息
        MysqlUtil mysqlUtil = new MysqlUtil(DB_URL, DB_USER, DB_PASSWORD);
        try {
            paramKvMap = mysqlUtil.getMap(MENU_MAPING_SQL);
            menuUrlList = mysqlUtil.getLists(String.format(STANDARD_SPECIAL_URL_SQL, ONE));
            mysqlUtil.getLists(RULE_SQL).stream().forEach(rules -> {
                platFormType = rules.get(0);
                String h5Key = rules.get(1).equals(FLAG) ? H5 : EMPTY;
                if (allRuleMap.get(platFormType + h5Key) == null) {
                    standardUrlList = new ArrayList<>();
                    standardUrlList.add(rules);
                    allRuleMap.put(platFormType + h5Key, standardUrlList);
                } else {
                    allRuleMap.get(platFormType + h5Key).add(rules);
                    allRuleMap.put(platFormType + h5Key, allRuleMap.get(platFormType + h5Key));
                }
            });
            staticUrlMap = mysqlUtil.getMap(STATIC_URL_SQL);
            //特殊url动态生成
            specialUrlList = mysqlUtil.getLists(String.format(STANDARD_SPECIAL_URL_SQL, TWO));
            specialUrlList.forEach(urlList -> {
                //固定参数
                String fixedParam = urlList.get(2);
                //参数对应的枚举key前缀
                String mappingKey = urlList.get(3);
                unit = urlList.get(4);
                subUnit = urlList.get(5);
                String paramType = urlList.get(7);
                paramKvMap.forEach((key, value) -> {
                    String url = urlList.get(0);
                    String paramKeyPrex = key.split(KV_SEPARATOR)[0];
                    String paramValue = key.split(KV_SEPARATOR)[1];
                    if (paramType.equals(ONE)) {
                        if (mappingKey.equals(paramKeyPrex) && !fixedParam.contains(FIXED_PARAM)) {
                            url = url.replace(fixedParam, fixedParam.replace(STANDARD_ZERO, paramValue));
                        }
                        if (mappingKey.equals(paramKeyPrex) && fixedParam.contains(FIXED_PARAM)) {
                            url = url.replace(fixedParam, fixedParam.replace(fixedParam, paramValue));
                        }
                        if (!url.contains(STANDARD_ZERO) && !url.contains(FIXED_PARAM)) {
                            staticUrlMap.put(url, String.join(SEPARATOR, unit, subUnit, value));
                        }
                    } else if (paramType.equals(TWO)) {
                        staticUrlMap.put(paramValue, value);
                    }
                });
            });
            //统一转小写
            toLowerMap(staticUrlMap);
            toLowerMap(paramKvMap);
        } catch (Exception e) {
            logger.error("Failed to query the standard rule. Procedure, the error details are: ", e);
            throw new UDFArgumentException(String.format("Failed to query the standard rule. Procedure, the error details are: %s", e));
        }
    }

    /**
     * 处理菜单URL
     *
     * @param scUrl
     * @return
     */
    public ArrayList<Text> meunDealUrl(String scUrl) throws HiveException {
        try {
            //菜单固定参数
            if (StringUtils.contains(scUrl, Z) || StringUtils.contains(scUrl, T) || StringUtils.contains(scUrl, M) || StringUtils.contains(scUrl, S) || StringUtils.contains(scUrl, C)) {
                //遍历菜单URL
                for (List<String> ls : menuUrlList) {
                    //当URL时菜单路径
                    if (StringUtils.contains(scUrl, ls.get(0))) {
                        unit = ls.get(4);
                        subUnit = ls.get(5);
                        String pageLinkName = ls.get(8);
                        //当URL包含多个key
                        int indexStart = scUrl.lastIndexOf(ls.get(0)) + ls.get(0).length();
                        int indexEnd = scUrl.lastIndexOf(BACKSLASH);
                        String paramUrl = scUrl.substring(indexStart, indexEnd).toLowerCase();
                        if (paramUrl.contains(SEPARATOR)) {
                            List<String> nameList = new ArrayList<>();
                            //search搜索存在搜索后筛选的情况，给固定格式
                            if (paramUrl.contains(BACKSLASH)) {
                                nameList.add(paramUrl.substring(0, paramUrl.indexOf(BACKSLASH)));
                                paramUrl = paramUrl.substring(paramUrl.indexOf(BACKSLASH) + 1, paramUrl.length());
                            }
                            String[] keysArr = paramUrl.split(SEPARATOR);
                            for (String key : keysArr) {
                                String upKey=key.toUpperCase();
                                if(upKey.contains(EY) || upKey.contains(VL) || upKey.contains(SF) || upKey.contains(LT)
                                    || upKey.contains(S) || upKey.contains(IMT) || upKey.contains(P) || upKey.contains(I)
                                    || upKey.contains(HR) || upKey.contains(EP)){
                                    continue;
                                }
                                if((key.contains(C.toLowerCase()) || key.contains(M.toLowerCase())) && scUrl.contains(multipleUrl)){
                                    String mOrC=key.split(KV_SEPARATOR)[0];
                                    String value=EMPTY;
                                    if(key.contains(C.toLowerCase())){
                                        value=key.replace(C.toLowerCase(),EMPTY);
                                    }
                                    if(key.contains(M.toLowerCase())){
                                        value=key.replace(M.toLowerCase(),EMPTY);
                                    }
                                    if(value.contains(KV_VALUE_SEPARATOR)){
                                        String[]arr=value.split(KV_VALUE_SEPARATOR);
                                        if(arr!=null && arr.length>0) {
                                            for (String v : arr) {
                                                String res=paramKvMap.get(mOrC + KV_SEPARATOR + v);
                                                if(StringUtils.isNotBlank(res)){
                                                    nameList.add(res);
                                                }
                                            }
                                        }
                                        continue;
                                    }
                                }
                                nameList.add(paramKvMap.get(key));
                            }
                            pageName = String.join(SEPARATOR, nameList);
                        } else {
                            pageName = paramKvMap.get(paramUrl);
                        }
                        setListValue(scUrl, unit, subUnit, pageName, pageLinkName);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Menu URL handling error,url is {},error is ", scUrl, e);
            throw new HiveException("Menu URL handling error,url is " + scUrl + ",error is ", e);
        }
        return resultPageNameList;
    }

    /**
     * 处理特殊固定参数的URL和无正则url处理
     *
     * @param scUrl
     * @return
     */
    public ArrayList<Text> specialParamDealUrl(String scUrl) throws HiveException {
        try {
            //区分是否特殊URL
            boolean isSpecial = false;
            for (List<String> rowList : specialUrlList) {
                if (scUrl.contains(rowList.get(1))) {
                    isSpecial = true;
                }
            }
            //无正则url处理
            if (!isSpecial && scUrl.contains(CONNECTOR_SEPARATOR)) {
                getStaticUrl(scUrl.substring(0, scUrl.indexOf(CONNECTOR_SEPARATOR)), null);
                return resultPageNameList;
            } else if (!isSpecial || !scUrl.contains(CONNECTOR_SEPARATOR)) {
                getStaticUrl(scUrl, null);
                return resultPageNameList;
            }
            //特殊url处理
            if (isSpecial) {
                for (List<String> urlList : specialUrlList) {
                    String url = urlList.get(0);
                    String fixedIdentity = urlList.get(1);
                    String paramType = urlList.get(7);
                    String pageLinkName = urlList.get(8);
                    URL urls = new URL(scUrl);
                    String host = urls.getHost();
                    if (scUrl.contains(fixedIdentity) && url.contains(host)) {
                        if (paramType.equals(ONE)) {
                            String urlParamKeys = urlList.get(6);
                            if (StringUtils.isNotBlank(urlParamKeys)) {
                                String[] urlParamKeysArray = urlParamKeys.split(COMMA);
                                Map<String, List<String>> map = getUrlparameter(scUrl, urlParamKeysArray);
                                map.forEach((k, v) -> {
                                    standardUrl = String.join(CONNECTOR_SEPARATOR, k, String.join(PARAM_SEPARATOR, v));
                                });
                            }else if(!url.contains(CONNECTOR_SEPARATOR)){
                                standardUrl = scUrl.substring(0,scUrl.indexOf(CONNECTOR_SEPARATOR));
                            }
                        } else if (paramType.equals(TWO)) {
                            if (!url.contains(CONNECTOR_SEPARATOR)) {
                                standardUrl = scUrl.substring(0, scUrl.indexOf(CONNECTOR_SEPARATOR));
                            }
                        }
                        if (StringUtils.isBlank(standardUrl)) {
                            standardUrl = scUrl;
                        }
                        if (StringUtils.isNotBlank(standardUrl)) {
                            //静态url获取三级名称
                            getStaticUrl(standardUrl, pageLinkName);
                            return resultPageNameList;
                        }
                    }
                }

            }

        } catch (Exception e) {
            logger.error("Error handling special parameter URL,url is {},error is ", scUrl, e);
            throw new HiveException("Error handling special parameter URL ,url is " + scUrl + ",error is ", e);
        }
        return resultPageNameList;
    }

    /**
     * 处理正则匹配的URL
     *
     * @param scUrl
     * @return
     */
    public ArrayList<Text> regexDealUrl(String scUrl) throws HiveException {
        String joinKey = scUrl.contains(H5_PREFIX) ? H5 : EMPTY;
        try {
            List<List<String>> platFormRules = allRuleMap.get(platFormType + joinKey);
            if (platFormRules != null) {
                for (List<String> rules : platFormRules) {
                    String newScUrl = scUrl;
                    standardUrl = rules.get(2);
                    regex = rules.get(3);
                    unit = rules.get(4);
                    subUnit = rules.get(5);
                    pageName = rules.get(6);
                    params = rules.get(7);
                    //多正则匹配
                    if (StringUtils.isNotBlank(regex)) {
                        String[] regexArray = regex.split(REGEX_OR_PARAM_SEPARATOR);
                        String[] ruleParamsArray = params.split(REGEX_OR_PARAM_SEPARATOR);
                        for (int i = 0; i < regexArray.length; i++) {
                            newScUrl = newScUrl.replaceAll(regexArray[i], ruleParamsArray[i].replace(PARAM_NULL, ""));
                        }
                    } else {
                        newScUrl = scUrl.replaceAll(regex, STANDARD_ZERO);
                    }
                    newScUrl = newScUrl.lastIndexOf(BACKSLASH) + 1 == newScUrl.length() ? newScUrl : newScUrl + BACKSLASH;
                    if (newScUrl.equals(standardUrl)) {
                        setListValue(standardUrl, unit, subUnit, pageName, null);
                        return resultPageNameList;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Regular expression rule URL processing error,url is {},error is ", scUrl, e);
            throw new HiveException("Regular expression rule URL processing error ,url is " + scUrl + ",error is ", e);
        }
        return resultPageNameList;
    }

    /**
     * 从静态url中获取pagename
     *
     * @param standardUrl
     */
    public void getStaticUrl(String standardUrl, String pageLinkName) {
        String pageSeparatorName = staticUrlMap.get(standardUrl.toLowerCase());
        if (!StringUtils.isBlank(pageSeparatorName)) {
            String[] pageNameArr = pageSeparatorName.split(SEPARATOR);
            setListValue(standardUrl, pageNameArr[0], pageNameArr[1], pageNameArr[2], pageLinkName);
        }
    }

    /**
     * 初始化变量
     */
    public void initParam() {
        platFormType = null;
        standardUrl = null;
        unit = null;
        subUnit = null;
        pageName = null;
        regex = null;
        params = null;
        resultPageNameList = new ArrayList<>(4);
        resultPageNameList.add(new Text("0000"));
        resultPageNameList.add(new Text("0000"));
        resultPageNameList.add(new Text("0000"));
        resultPageNameList.add(new Text("0000"));
    }

    /**
     * 返回结果赋值
     *
     * @param standardUrl
     * @param unit
     * @param subUnit
     * @param pageName
     * @return
     */
    public void setListValue(String standardUrl, String unit, String subUnit, String pageName, String pageLinkName) {
        if (StringUtils.isNotBlank(standardUrl) && StringUtils.isNotBlank(unit) && StringUtils.isNotBlank(subUnit)
                && StringUtils.isNotBlank(pageName)) {
            resultPageNameList.set(0, new Text(standardUrl));
            resultPageNameList.set(1, new Text(unit));
            resultPageNameList.set(2, new Text(subUnit));
            resultPageNameList.set(3, new Text(pageName + (pageLinkName == null ? "" : pageLinkName)));
        }
    }

    /**
     * url转小写获取
     *
     * @param map
     * @return
     */
    public void toLowerMap(Map<String, String> map) {
        Map<String, String> lowerMap = new HashMap<String, String>();
        map.forEach((key, value) -> {
            lowerMap.put(key.toLowerCase(), value);
        });
        map.clear();
        map.putAll(lowerMap);
    }

    /**
     * 提取URL指定参数和请求域名
     *
     * @param url
     * @param keyArr
     * @return
     */
    public static Map<String, List<String>> getUrlparameter(String url, String[] keyArr) {
        List<String> list = new ArrayList<>();
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        String domainUrl = null;
        if (StringUtils.isNotBlank(url) && keyArr.length > 0) {
            domainUrl = url.substring(0, url.indexOf(CONNECTOR_SEPARATOR));
            for (int i = 0; i < keyArr.length; i++) {
                Pattern pattern = Pattern.compile(keyArr[i] + "=([^&]*)");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    String value = matcher.group(0).split("=")[1].replace(PARAM_SEPARATOR, EMPTY) == null ? null : matcher.group(0).split("=")[1].replace(PARAM_SEPARATOR, EMPTY);
                    list.add(keyArr[i] + EQ + value);
                }
            }
        }
        map.put(domainUrl, list);
        return map;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "standard_url_format(" + strings[0] + ", " + strings[1] + ")";
    }
}
