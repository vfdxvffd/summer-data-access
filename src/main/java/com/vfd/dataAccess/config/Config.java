package com.vfd.dataAccess.config;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.vfd.dataAccess.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @PackageName: com.vfd.dataAccess.config
 * @ClassName: Config
 * @Description:
 * @Author: vfdxvffd
 * @date: 2021/11/15 14:26
 */
public abstract class Config {
    static Properties properties;
    public static String propertyFile = "/application.properties";
    public static final String PREFIX_DATASOURCE_CONFIG = "summer.datasource.druid.";

    static {
        try (InputStream in = Config.class.getResourceAsStream(propertyFile)) {
            properties = new Properties();
            properties.load(in);

            // 遍历所有的配置，将所有有关druid的提取出来
            Map<String, Object> property = new HashMap<>();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String key = entry.getKey().toString();
                if (key.startsWith(PREFIX_DATASOURCE_CONFIG)) {
                    property.put(key.substring(PREFIX_DATASOURCE_CONFIG.length()), entry.getValue());
                }
            }
            DBUtil.dataSource = DruidDataSourceFactory.createDataSource(property);

            LoggerFactory.getLogger(Config.class).info("加载配置文件: [" + propertyFile + "]，数据库连接池初始化完成");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
