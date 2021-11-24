package com.vfd.dataAccess;

import com.vfd.dataAccess.annotation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;

/**
 * @PackageName: com.vfd.dataAccess
 * @ClassName: SetTransactionMethod
 * @Description:
 * @Author: vfdxvffd
 * @date: 2021/11/18 13:45
 */
public class SetTransactionMethod {

    Connection connection = null;
    boolean isOpenConn = false;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void beforeMethodgetNewConn (Method realMethod) throws SQLException {
        boolean withAnotherConn = realMethod.getAnnotation(Transaction.class).withAnotherConn();
        if (withAnotherConn || isNoUseableConn()) {
            connection = DBUtil.dataSource.getConnection();
            connection.setAutoCommit(false);
            long threadId = Thread.currentThread().getId();
            Stack<Connection> connectionStack = ConnectionControle.threadId2Connections.getOrDefault(threadId, new Stack<>());
            connectionStack.push(connection);
            ConnectionControle.threadId2Connections.put(threadId, connectionStack);
            isOpenConn = true;
            logger.info ("add conn: " + connection + ", threadId: " + threadId);
        } else {
            System.out.println("not create new Connection, beacause withNewConn = false and isNoUseableConn = " + isNoUseableConn());
        }
    }

    public void returnMethodCommit() throws SQLException {
        if (isOpenConn) {
            connection.commit();
            logger.info ("conn commit: " + connection + ", threadId: " + Thread.currentThread().getId());
        }
    }

    public void throwMethodRollBack() throws SQLException {
        if (isOpenConn) {
            connection.rollback();
            logger.info ("conn rollback: " + connection + ", threadId" + Thread.currentThread().getId());
        }
    }

    public void afterMethodCloseConn() throws SQLException {
        if (isOpenConn) {
            long threadId = Thread.currentThread().getId();
            Stack<Connection> connectionStack = ConnectionControle.threadId2Connections.getOrDefault(threadId, new Stack<>());
            Connection conn = connectionStack.pop();
            if (conn.equals(connection)) {
                connection.close();
                logger.info ("conn close: " + connection + "threadId: " + threadId);
            }
        }
    }

    private boolean isNoUseableConn () {
        return ConnectionControle.threadId2Connections.getOrDefault(Thread.currentThread().getId(), new Stack<>()).isEmpty();
    }
}
