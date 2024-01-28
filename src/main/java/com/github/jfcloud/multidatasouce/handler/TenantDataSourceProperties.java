package com.github.jfcloud.multidatasouce.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author Lij
 * @since 2020-05-19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TenantDataSourceProperties implements Serializable {

    private static final long serialVersionUID = 7635406453475841170L;
    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 数据源url
     */
    private String datasourceUrl;

    /**
     * 数据源用户名
     */
    private String datasourceUsername;

    /**
     * 数据源密码
     */
    private String datasourcePassword;

    /**
     * 数据源驱动
     */
    private String datasourceDriver;

    /**
     * 数据源class
     */
    private String dataSourceClass;

}
