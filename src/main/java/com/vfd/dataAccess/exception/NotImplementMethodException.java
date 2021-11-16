package com.vfd.dataAccess.exception;

import java.lang.reflect.Method;

/**
 * @PackageName: com.vfd.dataAccess.exception
 * @ClassName: NotImplementMethodException
 * @Description:
 * @Author: vfdxvffd
 * @date: 2021/11/16 15:09
 */
public class NotImplementMethodException extends Exception {

    private final Method method;

    private final Class<?> clazz;

    public NotImplementMethodException (Method method, Class<?> clazz) {
        this.method = method;
        this.clazz = clazz;
    }

    @Override
    public void printStackTrace() {
        System.err.println("接口/类: [" + clazz + "] 的方法: [" + method + "]未实现");
    }
}
