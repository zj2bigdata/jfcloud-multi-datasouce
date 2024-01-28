package com.github.jfcloud.multidatasouce.factoty.impl;

import cn.hutool.core.util.StrUtil;
import com.github.jfcloud.multidatasouce.config.DynamicDataSouceErrorException;
import com.github.jfcloud.multidatasouce.factoty.MultiDatasourceFactory;
import com.github.jfcloud.multidatasouce.handler.TenantDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author zj
 * @date 2022/5/31
 */
@Slf4j
@Component
public class MultiHikariDataSourceFactory implements MultiDatasourceFactory {
    @Override
    public Object initDataSource(TenantDataSourceProperties dataSourceProperties) {
        Object dataSource;
        try {
            Class<?> dataSourceClass = ClassLoader.getSystemClassLoader().loadClass(dataSourceProperties.getDataSourceClass());
            dataSource = dataSourceClass.newInstance();
            // 获取属性并设置
            Field driverClassName = dataSourceClass.getSuperclass().getDeclaredField("driverClassName");
            driverClassName.setAccessible(true);
            Method setDriverClassName = dataSourceClass.getSuperclass().getDeclaredMethod("setDriverClassName", driverClassName.getType());
            setDriverClassName.invoke(dataSource, dataSourceProperties.getDatasourceDriver());

            // 获取属性并设置
            Field jdbcUrl = dataSourceClass.getSuperclass().getDeclaredField("jdbcUrl");
            jdbcUrl.setAccessible(true);
            Method setJdbcUrl = dataSourceClass.getSuperclass().getDeclaredMethod("setJdbcUrl", jdbcUrl.getType());
            setJdbcUrl.invoke(dataSource, dataSourceProperties.getDatasourceUrl());

            // 获取属性并设置
            Field username = dataSourceClass.getSuperclass().getDeclaredField("username");
            username.setAccessible(true);
            Method setUsername = dataSourceClass.getSuperclass().getDeclaredMethod("setUsername", username.getType());
            setUsername.invoke(dataSource, dataSourceProperties.getDatasourceUsername());

            // 获取属性并设置
            Field password = dataSourceClass.getSuperclass().getDeclaredField("password");
            password.setAccessible(true);
            Method setPassword = dataSourceClass.getSuperclass().getDeclaredMethod("setPassword", password.getType());
            setPassword.invoke(dataSource, dataSourceProperties.getDatasourcePassword());
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            log.error("反射数据驱动类出错", e);
            throw new DynamicDataSouceErrorException("反射数据源出错");
        }
        return dataSource;
    }

    @Override
    public boolean match(String dataSourceClass) {
        return StrUtil.equals(dataSourceClass, "com.zaxxer.hikari.HikariDataSource");
    }
}
