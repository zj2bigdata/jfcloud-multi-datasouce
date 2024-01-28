package com.github.jfcloud.multidatasouce.config;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 动态数据源上下文切换
 *
 * @author zj
 * @date 2022/5/18 23:33
 */
public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>() {
        /**
         * 将 defaultSource 数据源的 key作为默认数据源的 key
         */
        @Override
        protected String initialValue() {
            return CommonVariable.DEFAULT_DATASOURCE_BEAN_NAME;
        }
    };

    /**
     * 数据源的 key集合，用于切换时判断数据源是否存在
     */
    public static List<Object> dataSourceKeys = new ArrayList<>();

    /**
     * 切换数据源
     * @param key  数据源
     */
    public static void setDataSourceKey(String key) {
        if (!StrUtil.isEmpty(key)) {
            contextHolder.set(key);
        }
    }

    /**
     * 获取数据源
     * @return
     */
    public static String getDataSourceKey() {
        return contextHolder.get();
    }

    /**
     * 重置数据源
     */
    public static void clearDataSourceKey() {
        contextHolder.remove();
    }

    /**
     * 判断是否包含数据源
     * @param key   数据源
     * @return
     */
    public static boolean containDataSourceKey(String key) {
        return dataSourceKeys.contains(key);
    }

    /**
     * 添加数据源Keys
     * @param keys
     * @return
     */
    public static boolean addDataSourceKeys(Collection<? extends Object> keys) {
        return dataSourceKeys.addAll(keys);
    }
}
