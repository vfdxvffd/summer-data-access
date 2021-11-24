package com.vfd.dataAccess.proxy;

import com.vfd.dataAccess.DBUtil;
import com.vfd.dataAccess.annotation.*;
import com.vfd.dataAccess.exception.EmptySqlException;
import com.vfd.dataAccess.exception.NotImplementMethodException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @PackageName: com.vfd.dataAccess.proxy
 * @ClassName: ProxyFactory
 * @Description:
 * @Author: vfdxvffd
 * @date: 2021/11/14 19:33
 */
public class ProxyFactory {

    private final Class<?> interfacee;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ProxyFactory(Class<?> interfacee) {
        this.interfacee = interfacee;
    }

    @SuppressWarnings("all")
    public Object getProxyInstance() {
        // 实现动态代理
        return Proxy.newProxyInstance(interfacee.getClassLoader(), new Class[]{interfacee},
                (proxy, method, args) -> {
                    DBUtil dbUtil = new DBUtil();

                    String sql = "";
                    sql = method.getAnnotation(Insert.class) == null ? "" : method.getAnnotation(Insert.class).sql();
                    sql = method.getAnnotation(Delete.class) == null ? sql : method.getAnnotation(Delete.class).sql();
                    sql = method.getAnnotation(Update.class) == null ? sql : method.getAnnotation(Update.class).sql();
                    if (!"".equals(sql)) {
                        return dbUtil.executeUpdate(sql, args);
                    }

                    Select select = method.getAnnotation(Select.class);
                    sql = select == null ? "" : select.sql();
                    if (!"".equals(sql)) {
                        dbUtil.executeQuery(sql, args);
                        return getSelectResult(method, dbUtil, select.resultType());
                    }

                    SelectMaps selectMaps = method.getAnnotation(SelectMaps.class);
                    sql = selectMaps == null ? "" : selectMaps.sql();
                    if (!"".equals(sql)) {
                        dbUtil.executeQuery(sql, args);
                        return getSelectMapsResult(dbUtil);
                    }

                    SelectPojo selectPojo = method.getAnnotation(SelectPojo.class);
                    sql = selectPojo == null ? "" : selectPojo.sql();
                    if (!"".equals(sql)) {
                        dbUtil.executeQuery(sql, args);
                        return getSelectPojo(dbUtil, selectPojo.pojo(), method);
                    }

                    boolean isSqlMethod = method.getAnnotation(Insert.class) != null
                            || method.getAnnotation(Delete.class) != null
                            || method.getAnnotation(Update.class) != null
                            || select != null || selectMaps != null || selectPojo != null;
                    if (isSqlMethod) {
                        throw new EmptySqlException();
                    }
                    if (method.isDefault()) {           // 如果是接口的默认方法
                        // 参考网上大佬的写法
                        // https://www.jianshu.com/p/63691220f81f
                        // https://stackoverflow.com/questions/22614746/how-do-i-invoke-java-8-default-methods-reflectively
                        Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                                .getDeclaredConstructor(Class.class, int.class);
                        constructor.setAccessible(true);

                        Class<?> declaringClass = method.getDeclaringClass();
                        int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE;

                        return constructor.newInstance(declaringClass, allModes)
                                .unreflectSpecial(method, declaringClass)
                                .bindTo(proxy)
                                .invokeWithArguments(args);
                    }

                    logger.error("@Data标注的接口中出现非数据库访问且非默认方法");
                    throw new NotImplementMethodException(method, interfacee);
                });
    }

    @SuppressWarnings("all")
    public <T> Object getSelectResult (Method method, DBUtil dbUtil, Class<?> resultType) throws Exception {
        ResultSet rs = dbUtil.rs;
        String columnName = rs.getMetaData().getColumnName(1);
        if (List.class.equals(method.getReturnType())) {            // 如果返回值是以List接收的
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add((T) rs.getObject(columnName, resultType));
            }
            dbUtil.closeAll();
            return result;
        } else {         // 如果返回值直接收单条数据则返回第一条或者抛出异常
            Object result;
            if (rs.next()) {
                result = rs.getObject(columnName, resultType);
            } else {
                dbUtil.closeAll();
                throw new Exception("");
            }
            dbUtil.closeAll();
            return result;
        }
    }

    public List<Map<String, Object>> getSelectMapsResult (DBUtil dbUtil) throws SQLException {
        ResultSet rs = dbUtil.rs;
        List<Map<String, Object>> result = new ArrayList<>();
        int columnCount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            Map<String, Object> res = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                res.put(columnName, rs.getObject(columnName));
            }
            result.add(res);
        }
        dbUtil.closeAll();
        return result;
    }

    @SuppressWarnings("all")
    public <T> Object getSelectPojo (DBUtil dbUtil, Class<?> pojo, Method method) throws Exception {
        ResultSet rs = dbUtil.rs;
        if (List.class.equals(method.getReturnType())) {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add((T) getPojoInstance(pojo, rs));
            }
            dbUtil.closeAll();
            return result;
        } else {
            if (rs.next()) {
                Object instance = getPojoInstance(pojo, rs);
                dbUtil.closeAll();
                return instance;
            } else {
                dbUtil.closeAll();
                return null;
            }
        }
    }

    private Object getPojoInstance(Class<?> pojo, ResultSet rs) throws InstantiationException, IllegalAccessException {
        Object instance = pojo.newInstance();
        for (Field field : pojo.getDeclaredFields()) {
            field.setAccessible(true);
            Mapping mapping = field.getAnnotation(Mapping.class);
            String from = mapping.from();
            Object object;
            try {
                object = rs.getObject(from);
            } catch (Exception e) {
                object = null;
            }
            field.set(instance, object);
        }
        return instance;
    }
}
