package com.vfd.dataAccess;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Stack;

/**
 * @PackageName: com.vfd.dataAccess
 * @ClassName: DBUtil
 * @Description: JDBC的帮助类，封装了Java访问MySql的一些常用接口供项目调用，减少开发成本
 * @Author: vfdxvffd
 * @date: 2021/11/15 14:58
 */
public class DBUtil {

    public Connection connection = null;
    public PreparedStatement pstmt = null;
    public ResultSet rs = null;
    public static DataSource dataSource = null;

    /**
     * 获取Connection
     * @return 返回connection
     */
    private Connection getConnection() throws Exception {
        connection = dataSource.getConnection();
        return connection;
    }


    /**
     * 获取PreparedStatement
     *
     * @param sql    需要执行的sql语句
     * @param params sql中占位符代表的参数列表
     * @return 返回绑定完字段的pstmt
     */
    private PreparedStatement createPreparedStatement(Connection connection, String sql, Object[] params) throws Exception {
        pstmt = connection.prepareStatement(sql);
        if (params != null) {
            for (int i = 1; i <= params.length; i++) {
                pstmt.setObject(i, params[i - 1]);
            }
        }
        return pstmt;
    }


    /**
     * 关闭所有的资源
     */
    public void closeAll() throws SQLException {
        if (rs != null) {
            rs.close();
        }
        if (pstmt != null) {
            pstmt.close();
        }
        if (connection != null) {
            connection.close();
        }
    }


    /**
     * 通用的增删改操作
     *
     * @param sql    sql语句，用PreparedStatement操作，防止sql注入风险
     * @param params 代替?的参数列表，是一个Object数组
     * @return 返回成功与否
     */
    private int executeUpdateWithNewConn(String sql, Object[] params) throws Exception {
        int count = -1;
        try {
            pstmt = createPreparedStatement(getConnection(), sql, params);
            count = pstmt.executeUpdate();
            return count;
        } finally {
            closeAll();
        }
    }

    public int executeUpdate (String sql, Object[] params) throws Exception {
        int count = -1;
        long threadId = Thread.currentThread().getId();
        if (ConnectionControle.threadId2Connections.getOrDefault(threadId, new Stack<>()).isEmpty()) {    // 没有缓存的conn，说明要重新开一个conn
            return executeUpdateWithNewConn(sql, params);
        } else {
            // conn 不关闭
            Connection conn = ConnectionControle.threadId2Connections.get(threadId).peek();
            PreparedStatement pstmt = createPreparedStatement(conn, sql, params);
            count = pstmt.executeUpdate();
            pstmt.close();
            return count;
        }
    }


    /**
     * 通用的查操作，此方法未关闭connection、pstmt、rs，需要调用者自行关闭
     *
     * @param sql    查的sql语句
     * @param params 参数列表，代替?占位符
     */
    public void executeQueryWithNewConn(String sql, Object[] params) throws Exception {
        pstmt = createPreparedStatement(getConnection(), sql, params);
        rs = pstmt.executeQuery();
    }

    public void executeQuery(String sql, Object[] params) throws Exception {
        long threadId = Thread.currentThread().getId();
        if (ConnectionControle.threadId2Connections.getOrDefault(threadId, new Stack<>()).isEmpty()) {    // 没有缓存的conn，说明要重新开一个conn
            executeQueryWithNewConn(sql, params);
        } else {
            // conn 不关闭
            Connection conn = ConnectionControle.threadId2Connections.get(threadId).peek();
            PreparedStatement pstmt = createPreparedStatement(conn, sql, params);
            rs = pstmt.executeQuery();
        }
    }

    private int executeBatchWithNewConn (String sql, Object[][] params) throws Exception {
        try {
            pstmt = getConnection().prepareStatement(sql);
            return batchTasks(params, pstmt);
        } finally {
            closeAll();
        }
    }

    public int executeBacth (String sql, Object[][] params) throws Exception {
        int count = -1;
        long threadId = Thread.currentThread().getId();
        if (ConnectionControle.threadId2Connections.getOrDefault(threadId, new Stack<>()).isEmpty()) {    // 没有缓存的conn，说明要重新开一个conn
            return executeBatchWithNewConn(sql, params);
        } else {
            // conn 不关闭
            Connection conn = ConnectionControle.threadId2Connections.get(threadId).peek();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            count = batchTasks(params, pstmt);
            pstmt.close();
            return count;
        }
    }

    private int batchTasks(Object[][] params, PreparedStatement pstmt) throws SQLException {
        for (Object[] param : params) {
            if (param != null) {
                for (int i = 1; i <= param.length; i++) {
                    pstmt.setObject(i, param[i - 1]);
                }
                pstmt.addBatch();
            }
        }
        int[] batch = pstmt.executeBatch();
        return batch.length;
    }
}
