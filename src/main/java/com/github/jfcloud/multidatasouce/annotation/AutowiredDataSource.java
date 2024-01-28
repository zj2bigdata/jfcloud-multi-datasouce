package com.github.jfcloud.multidatasouce.annotation;

import java.lang.annotation.*;

/**
 * 租户数据库隔离注解
 * @author zj
 */

@Target({ElementType.METHOD,ElementType.TYPE}) //注解放置的目标位置,METHOD是可注解在方法级别上
@Retention(RetentionPolicy.RUNTIME) //注解在哪个阶段执行
@Inherited
@Documented
public @interface AutowiredDataSource {

    /**
     * 指定租户
     */
    String tenandId() default "";

    /**
     * 指定库
     */
    String database() default "";
}
