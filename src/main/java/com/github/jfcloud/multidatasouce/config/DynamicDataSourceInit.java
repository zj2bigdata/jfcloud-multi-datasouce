package com.github.jfcloud.multidatasouce.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.jfcloud.multidatasouce.factoty.MultiDatasourceFactory;
import com.github.jfcloud.multidatasouce.handler.ITenantInfoHandler;
import com.github.jfcloud.multidatasouce.handler.TenantDataSourceProperties;
import com.github.jfcloud.multidatasouce.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态数据源初始化
 *
 * @author zj
 * @date 2022/5/19 00:08
 */
@Slf4j
@Configuration
public class DynamicDataSourceInit {

    @Autowired
    private ITenantInfoHandler tenantInfoService;

    @Autowired
    private List<MultiDatasourceFactory> multiDatasourceFactories;

    @Value("#{'${multitenant.dynamic.dbs}'.split(',')}")
    String[] dbs;

    @Value("${multitenant.dynamic.dbhost}")
    String dbhost;

    //指定租户的数据库是否带后缀 当租户id=该值，则创建的数据库连接不带租户后缀，根据数据库名配置
    @Value("${multitenant.dynamic.defaultid}")
    String defaultTenantId;

    /**
     * 初始化数据源
     *
     * @author zj
     */
    @Bean
    public void initDataSource() {
        if (ArrayUtil.isEmpty(dbs)) {
            throw new DynamicDataSouceErrorException("请在yml配置dynamic.dbs属性值");
        }
        DynamicDataSource dynamicDataSource = (DynamicDataSource) SpringContextUtil.getBean(CommonVariable.DYNAMIC_DATASOURCE_BEAN_NAME);
        DataSource defaultSource = SpringContextUtil.getBean(CommonVariable.DEFAULT_DATASOURCE_BEAN_NAME);
        log.info("======初始化动态数据源 开始=====");
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(CommonVariable.DEFAULT_DATASOURCE_BEAN_NAME, defaultSource);
        List<TenantDataSourceProperties> tenantList = tenantInfoService.list();
        Object dataSource = null;
        for (String db : dbs) {
            for (TenantDataSourceProperties tenantInfo : tenantList) {
                if(StrUtil.isBlank(tenantInfo.getDatasourceUrl()) || StrUtil.isBlank(tenantInfo.getDatasourceDriver()) || StrUtil.isBlank(tenantInfo.getDatasourceUsername()) || StrUtil.isBlank(tenantInfo.getDatasourcePassword())){
                    continue;
                }
                TenantDataSourceProperties dataSourceProperties = ObjectUtil.cloneByStream(tenantInfo);
                if(StrUtil.equals(tenantInfo.getTenantId(),defaultTenantId)){
                    dataSourceProperties.setDatasourceUrl(dataSourceProperties.getDatasourceUrl()
                            .replace("{dbhost}",dbhost)
                            .replace("{db}", db));
                }else{
                    dataSourceProperties.setDatasourceUrl(dataSourceProperties.getDatasourceUrl()
                            .replace("{dbhost}",dbhost)
                            .replace("{db}", db + "_" + tenantInfo.getTenantId()));
                }
                log.info("======初始化动态数据源 =====");
                if(StrUtil.isBlank(tenantInfo.getDatasourceUrl())){
                    log.error("异常租户数据源:key={},tenant={}", db, dataSourceProperties.toString());
                }else{
                    log.info("key={},tenant={}", db, dataSourceProperties.toString());
                }
                for (MultiDatasourceFactory multiDatasourceFactory : multiDatasourceFactories) {
                    if (multiDatasourceFactory.match(tenantInfo.getDataSourceClass())) {
                        dataSource = multiDatasourceFactory.initDataSource(dataSourceProperties);
                        break;
                    }
                }
                if (dataSource == null) {
                    throw new DynamicDataSouceErrorException("找不到对应数据库");
                }
                dataSourceMap.put(db + (StrUtil.equals(tenantInfo.getTenantId(),defaultTenantId)?"": "_" + dataSourceProperties.getTenantId()), dataSource);
            }
        }
        // 设置数据源
        dynamicDataSource.setDataSources(dataSourceMap);
        /**
         * 必须执行此操作，才会重新初始化AbstractRoutingDataSource 中的 resolvedDataSources，也只有这样，动态切换才会起效
         */
        dynamicDataSource.afterPropertiesSet();
        log.info("======初始化动态数据源 结束=====");
    }
}
