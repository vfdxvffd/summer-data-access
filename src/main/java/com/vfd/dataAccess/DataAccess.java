package com.vfd.dataAccess;

import com.vfd.dataAccess.annotation.Data;
import com.vfd.dataAccess.config.Config;
import com.vfd.dataAccess.proxy.ProxyFactory;
import com.vfd.summer.applicationContext.impl.SummerAnnotationConfigApplicationContext;
import com.vfd.summer.extension.Extension;
import com.vfd.summer.ioc.bean.BeanDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * @PackageName: com.vfd.dataAccess
 * @ClassName: DataAccess
 * @Description:
 * @Author: vfdxvffd
 * @date: 2021/11/14 21:55
 */
public class DataAccess implements Extension {
    @Override
    public void doOperation0(SummerAnnotationConfigApplicationContext context) throws Exception {
        Config.propertyFile = context.getPropertyFile();
    }

    @Override
    public void doOperation1(SummerAnnotationConfigApplicationContext context) throws Exception {

        // 遍历所有标注了@Data注解的接口类
        for (Class<?> clazz : context.getAnnotationType2Clazz().getOrDefault(Data.class, new ArrayList<>())) {
            // 首先确保是接口类型
            if (clazz.isInterface()) {
                Object proxyInstance = new ProxyFactory(clazz).getProxyInstance();       // 代理对象
                // 将代理对象加入到一级缓存中
                String className = clazz.getName().replaceAll(clazz.getPackage().getName() + ".", "");
                className = className.substring(0, 1).toLowerCase() + className.substring(1);
                context.getIocByName().put(className, proxyInstance);

                // 将beanType和beanName的对应关系保存起来
                context.getBeanTypeAndName().put(clazz, new HashSet<>(Collections.singletonList(className)));
            }
        }
    }

    @Override
    public void doOperation2(SummerAnnotationConfigApplicationContext summerAnnotationConfigApplicationContext) throws Exception {

    }

    @Override
    public void doOperation3(SummerAnnotationConfigApplicationContext summerAnnotationConfigApplicationContext) throws Exception {

    }

    @Override
    public void doOperation4(SummerAnnotationConfigApplicationContext summerAnnotationConfigApplicationContext) throws Exception {

    }

    @Override
    public void doOperation5(SummerAnnotationConfigApplicationContext summerAnnotationConfigApplicationContext, BeanDefinition beanDefinition) throws Exception {

    }

    @Override
    public void doOperation6(SummerAnnotationConfigApplicationContext summerAnnotationConfigApplicationContext, Object o) throws Exception {

    }

    @Override
    public void doOperation7(SummerAnnotationConfigApplicationContext summerAnnotationConfigApplicationContext, Object o) throws Exception {

    }

    @Override
    public void doOperation8(SummerAnnotationConfigApplicationContext summerAnnotationConfigApplicationContext, Object o) throws Exception {

    }

    @Override
    public void doOperation9(SummerAnnotationConfigApplicationContext summerAnnotationConfigApplicationContext) throws Exception {

    }
}
