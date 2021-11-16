package com.vfd.dataAccess.exception;

/**
 * @PackageName: com.vfd.dataAccess
 * @ClassName: EmptySqlException
 * @Description:
 * @Author: vfdxvffd
 * @date: 2021/11/16 14:57
 */
public class EmptySqlException extends Exception {

    @Override
    public void printStackTrace() {
        System.err.println("空的sql语句，请检查注解的sql属性");
    }

}
