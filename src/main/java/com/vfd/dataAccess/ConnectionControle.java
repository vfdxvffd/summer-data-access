package com.vfd.dataAccess;

import java.sql.Connection;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @PackageName: com.vfd.dataAccess
 * @ClassName: ConnectionControle
 * @Description:
 * @Author: vfdxvffd
 * @date: 2021/11/18 13:55
 */
public class ConnectionControle {

    public static Map<Long, Stack<Connection>> threadId2Connections = new ConcurrentHashMap<>();
}
