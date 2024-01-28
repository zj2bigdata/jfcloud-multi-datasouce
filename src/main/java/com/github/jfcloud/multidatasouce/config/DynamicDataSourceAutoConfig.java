package com.github.jfcloud.multidatasouce.config;

import com.github.jfcloud.multidatasouce.utils.SpringContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源自动配置类
 * @author zj
 * @date 2022/5/31
 */
@Configuration
@ComponentScan("com.github.jfcloud.multidatasouce.factoty")
@Import(value = {SpringContextUtil.class,DynamicDataSourceInit.class,DynamicDataSourceAspect.class})
public class DynamicDataSourceAutoConfig {

    @Bean("dynamicDataSource")
    public DynamicDataSource dynamicDataSource(DataSource defaultSource) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(CommonVariable.DEFAULT_DATASOURCE_BEAN_NAME, defaultSource);
        // 将 defaultSource 数据源作为默认指定的数据源
        dynamicDataSource.setDefaultDataSource(defaultSource);
        // 将 defaultSource  数据源作为指定的数据源
        dynamicDataSource.setDataSources(dataSourceMap);
        return dynamicDataSource;
    }


}
