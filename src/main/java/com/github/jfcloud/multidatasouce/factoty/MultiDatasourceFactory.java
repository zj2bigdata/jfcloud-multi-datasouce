package com.github.jfcloud.multidatasouce.factoty;

import com.github.jfcloud.multidatasouce.handler.TenantDataSourceProperties;

/**
 * 数据源初始化工具
 * @author zj
 * @date 2022/5/31
 */
public interface MultiDatasourceFactory {

    /**
     * init 数据源
     *
     * @return {@link Object }
     * @author zj
     */
    Object initDataSource(TenantDataSourceProperties dataSourceProperties);

    /**
     * 匹配数据源类型
     *
     * @return boolean
     * @author zj
     */
    boolean match(String dataSourceClass);

}
