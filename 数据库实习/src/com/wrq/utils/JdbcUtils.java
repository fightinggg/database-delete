package com.wrq.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class JdbcUtils {
    private Connection conn = null;
    private PreparedStatement ps = null;
    private ResultSet rs = null;

    static {
        try {
            String name = "oracle.jdbc.driver.OracleDriver";
            Class.forName(name);
        } catch (ClassNotFoundException e) {
            System.out.println("驱动加载失败原因为" + e.getMessage());
        }
    }

    public JdbcUtils() {
        getConn();
    }

    public Connection getConn() {
        try {
            String password = "wsx";
            String user = "wsx";
            String url = "jdbc:oracle:thin:@localhost:1521:helowin";
            conn = (conn == null || conn.isClosed()) ? DriverManager.getConnection(url, user, password) : conn;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public int update(String sql, Object[] params) {
        int result = 0;
        try {
            System.out.println(sql+ Arrays.toString(params));
            ps = getConn().prepareStatement(sql);
            setParams(params);
            result = ps.executeUpdate();
        } catch (SQLException e) {
            result = -1;
            System.out.println("数据更新失败,可能是你的sql语句写错了：" + e.getMessage());
        } finally {
            close();
        }

        return result;
    }

    private ResultSet query1(String sql, Object[] params) {
        try {
            ps = getConn().prepareStatement(sql);
            setParams(params);
            return ps.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("sql写错了！");
        }
        return null;
    }

    public int count(String sql, Object[] params) {
        String count_sql = "select count(*) records from(" + sql + ") temp";
        rs = query1(count_sql, params);
        try {
            if (rs.next()) {
                return rs.getInt("records");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    // 查询
    public List<Map<String, Object>> query(String sql, Object[] params) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            ps = getConn().prepareStatement(sql);
            setParams(params);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                // 所有字段名
                ResultSetMetaData rsmd = rs.getMetaData();
                // 字段的数量
                int columnCount = rsmd.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    // 获得字段名
                    String columnName = rsmd.getColumnLabel(i + 1);
                    map.put(columnName, rs.getObject(columnName));
                }
                list.add(map);
            }
        } catch (SQLException e) {
            System.out.println("查询失败，数据更新失败,可能是你的sql语句写错了：" + e.getMessage());
        } finally {
            close();
        }
        return list;
    }

    // 分页查询
    public List<Map<String, Object>> listForPage(String sql, int page, int records, Object[] params) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String pageSql = "SELECT * FROM (" + sql + ") where rn> " + (page - 1) * records + " and rn <=" + page*records;
        System.out.println("--->>"+pageSql);
        String columnName = null;
        try {
            ps = getConn().prepareStatement(pageSql);
            setParams(params);
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    columnName = rsmd.getColumnLabel(i + 1);
                    map.put(columnName, rs.getObject(columnName));
                }
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    // 查询一条
    public Map<String, Object> queryById(String sql, int user_id) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            ps = getConn().prepareStatement(sql);
            ps.setObject(1, user_id);
            rs = ps.executeQuery();
            while (rs.next()) {
                // 所有字段名
                ResultSetMetaData rsmd = rs.getMetaData();
                // 字段的数量
                int columnCount = rsmd.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    // 获得字段名
                    String columnName = rsmd.getColumnLabel(i + 1);
                    map.put(columnName, rs.getObject(columnName));
                }
            }
        } catch (SQLException e) {
            System.out.println("查询一条失败，数据更新失败,可能是你的sql语句写错了：" + e.getMessage());
        } finally {
            close();
        }
        return map;
    }

    // 登录
    public Map<String, Object> login(String sql, String account, String password) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            ps = getConn().prepareStatement(sql);
            ps.setObject(1, account);
            ps.setObject(2, password);
            rs = ps.executeQuery();
            while (rs.next()) {
                // 所有字段名
                ResultSetMetaData rsmd = rs.getMetaData();
                // 字段的数量
                int columnCount = rsmd.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    // 获得字段名
                    String columnName = rsmd.getColumnLabel(i + 1);
                    map.put(columnName, rs.getObject(columnName));
                }
            }
        } catch (SQLException e) {
            System.out.println("登陆失败，数据更新失败,可能是你的sql语句写错了：" + e.getMessage());
        } finally {
            close();
        }
        return map;
    }

    public void setParams(Object[] params) {
        try {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void close() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
