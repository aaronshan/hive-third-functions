package com.chinagoods.bigdata.functions.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * @author xiaowei.song
 */
final public class MysqlUtil {
    private static final Logger logger = LoggerFactory.getLogger(MysqlUtil.class);
    private Connection connection;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    /**
     * MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
     **/
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    public MysqlUtil(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        connection = getConnection();
    }

    public Connection getConnection(){
        Connection conn = null;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(this.dbUrl, this.dbUser, this.dbPassword);
        }catch (Exception e) {
            logger.error("获取mysql连接失败, dbUrl: {}, username: {}, password: {}", dbUrl, dbUser, dbPassword, e);
            System.exit(1);
        }
        return conn;
    }

    /**
     * 查询表中关键词
     * @return keywords sensitive set违禁词集合
     * @throws SQLException 查詢異常
     */
     public Set<String> getKeywords(String sql) throws SQLException {
        Set<String> keywordsSet = new HashSet<>(3000);
        // 重建mysql连接信息
        if (connection == null || connection.isClosed()) {
             connection = getConnection();
         }
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        // 展开结果集数据库
        while(rs.next()) {
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                keywordsSet.add((String) rs.getObject(i + 1));
            }
        }
        // 完成后关闭
        rs.close();
        stmt.close();
        return keywordsSet;
    }

    public Set<String> getSet(String sql) throws SQLException {
        Set<String> set = new HashSet<>();
        // 重建mysql连接信息
        if (connection == null || connection.isClosed()) {
            connection = getConnection();
        }
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        // 展开结果集数据库
        while(rs.next()) {
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                set.add((String) rs.getObject(i + 1));
            }
        }
        // 完成后关闭
        rs.close();
        stmt.close();
        return set;
    }
    
    public List<List<String>> getLists(String sql) throws SQLException {
        List<List<String>> resultlist = new ArrayList<>();
        // 重建mysql连接信息
        if (connection == null || connection.isClosed()) {
            connection = getConnection();
        }
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        // 展开结果集数据库
        while(rs.next()) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                list.add((String) rs.getObject(i + 1));
            }
            resultlist.add(list);
        }
        // 完成后关闭
        rs.close();
        stmt.close();
        return resultlist;
    }

    public Map<String,String> getMap(String sql) throws SQLException {
        Map<String,String> map = new HashMap<>();
        // 重建mysql连接信息
        if (connection == null || connection.isClosed()) {
            connection = getConnection();
        }
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        // 展开结果集数据库
        while(rs.next()) {
            map.put((String) rs.getObject(1),(String) rs.getObject(2));
        }
        // 完成后关闭
        rs.close();
        stmt.close();
        return map;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static void main(String[] args) throws SQLException {
        MysqlUtil mysqlUtil = new MysqlUtil("jdbc:mysql://rm-uf6wr9aa537v0tesf3o.mysql.rds.aliyuncs.com:3306/source?characterEncoding=UTF-8&useSSL=false",
                "datax",
                "oRvmRrVJeOCl8XsY");
        System.out.println(mysqlUtil.getKeywords("select t.key_word\n" +
                "from risk_control_keywords t\n" +
                "inner join (\n" +
                "\tselect key_word\n" +
                "\t,max(create_time) max_create_time\n" +
                "\tfrom risk_control_keywords\n" +
                "\tgroup by key_word\n" +
                ") nt on t.create_time =  nt.max_create_time and t.key_word = nt.key_word\n" +
                "where t.is_deleted = '否'"));
    }
}
