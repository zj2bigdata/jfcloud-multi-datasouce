package com.github.jfcloud.multidatasouce.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zj
 * @date 2022-8-1
 */
@Data
public class MethodTenantParam implements Serializable {
    private static final long serialVersionUID = 4005500665838199815L;
    private String tenantId;
    private String database;

    public MethodTenantParam() {
    }

    public MethodTenantParam(String tenantId) {
        this.tenantId = tenantId;
        this.database="";
    }

    public MethodTenantParam(String tenantId, String database) {
        this.tenantId = tenantId;
        this.database = database;
    }
}
