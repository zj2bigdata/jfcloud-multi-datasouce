package com.github.jfcloud.multidatasouce.handler;

import java.util.List;

/**
 * 租户信息处理器
 * @author zj
 * @date 2022/5/31
 */
public interface ITenantInfoHandler {

    /**
     * 获取数据源配置
     */
    List<TenantDataSourceProperties> list();

    /**
     * 动态租户信息（可配置为当前登陆租户）
     *
     * @return {@link String }
     * @author zj
     */
    String dynamicTenantId();
}
