package com.github.jfcloud.multidatasouce.config;

/**
 * @author zj
 * @date 2021-06-08
 * 数据库匹配错误
 */
public class DynamicDataSouceErrorException extends RuntimeException {
    private static final long serialVersionUID = 6218087628673198390L;

    public DynamicDataSouceErrorException(String msg) {
        super(msg);

    }
}
