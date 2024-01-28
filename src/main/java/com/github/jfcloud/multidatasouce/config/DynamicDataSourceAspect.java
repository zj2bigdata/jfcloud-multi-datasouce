package com.github.jfcloud.multidatasouce.config;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.github.jfcloud.multidatasouce.annotation.AutowiredDataSource;
import com.github.jfcloud.multidatasouce.handler.ITenantInfoHandler;
import com.github.jfcloud.multidatasouce.utils.MethodTenantParam;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;

/**
 * 动态数据源切面拦截
 *
 * @author zj
 * @date 2022/5/19 00:29
 */
@Slf4j
@Aspect
@Component
@Order(1) // 请注意：这里order一定要小于tx:annotation-driven的order，即先执行DynamicDataSourceAspectAdvice切面，再执行事务切面，才能获取到最终的数据源
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class DynamicDataSourceAspect {

    @Autowired
    ITenantInfoHandler iTenantInfoHandler;

    @Value("${multitenant.request.url.tenant:TENANT_ID}")
    String requestTenantParam;

    @Value("${multitenant.request.session.tenant:TENANT_ID}")
    String requestSessionTenantParam;

    @Value("${multitenant.request.head.tenant:TENANT-ID}")
    String requestHeadTenantParam;

    /**
     * 多库
     */
    @Value("#{'${multitenant.dynamic.dbs}'.split(',')}")
    String[] dbs;

    @Value("${multitenant.dynamic.defaultid}")
    String defaultTenantId;

    @Pointcut("@within(com.github.jfcloud.multidatasouce.annotation.AutowiredDataSource) || @annotation(com.github.jfcloud.multidatasouce.annotation.AutowiredDataSource)")
    public void distributeDataDource() {

    }

    @Around("distributeDataDource()")
    public Object doAround(ProceedingJoinPoint jp) throws Throwable {
        Object result;
        try {
            // 获取操作
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) jp.getSignature();
            // 获取切入点所在的方法
            Method method = signature.getMethod();
            AutowiredDataSource autowiredDataSource = method.getAnnotation(AutowiredDataSource.class);
            if (autowiredDataSource == null) {
                autowiredDataSource = jp.getTarget().getClass().getAnnotation(AutowiredDataSource.class);
            }
            String tenantId = "";
            String database = "";
            String changeDataSource = CommonVariable.DEFAULT_DATASOURCE_BEAN_NAME;
            //注解 tenanid 优先级最高
            if (StrUtil.isNotBlank(autowiredDataSource.tenandId())) {
                tenantId = autowiredDataSource.tenandId();
            }
            //方法参数 tenanid 优先级其次，从方法参数获取
            if (StrUtil.isBlank(tenantId)) {
                Object[] args = jp.getArgs();
                if (args.length != 0 && args[0] instanceof MethodTenantParam) {
                    MethodTenantParam params = (MethodTenantParam) args[0];
                    if (StrUtil.isNotBlank(params.getTenantId())) {
                        tenantId = params.getTenantId();
                    }
                }
            }

            //业务自定义 tenanid 优先级其次，从token获取租户信息
            if (StrUtil.isBlank(tenantId)) {
                tenantId = iTenantInfoHandler.dynamicTenantId();
            }
            //请求参数 tenanid 优先级其次，从接口请求获取tenantid
            if (StrUtil.isBlank(tenantId)) {
                //从接口请求获取租户信息
                ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (sra != null) {
                    HttpServletRequest request = sra.getRequest();
                    //从session取
                    HttpSession session = sra.getRequest().getSession(true);
                    tenantId = (String) session.getAttribute(requestSessionTenantParam);
                    //从参数取
                    if (StrUtil.isEmpty(tenantId)) {
                        tenantId = request.getParameter(requestTenantParam);
                    }
                    //从请求头取
                    if (StrUtil.isEmpty(tenantId)) {
                        tenantId = request.getHeader(requestHeadTenantParam);
                    }
                }
            }

            //注解获取库名
            if (StrUtil.isNotBlank(autowiredDataSource.database())) {
                database = autowiredDataSource.database();
            }
            //方法参数获取库名
            if (StrUtil.isBlank(database)) {
                Object[] args = jp.getArgs();
                if (args.length != 0 && args[0] instanceof MethodTenantParam) {
                    MethodTenantParam params = (MethodTenantParam) args[0];
                    if (StrUtil.isNotBlank(params.getDatabase())) {
                        database = params.getDatabase();
                    }
                }
            }
            //空则使用默认库
            if(StrUtil.isBlank(database) || !ArrayUtil.contains(dbs, database)){
                database = dbs[0];
            }
            if (StrUtil.isNotBlank(tenantId) && StrUtil.isNotBlank(database)) {
                if(StrUtil.equals(tenantId,defaultTenantId)){
                    changeDataSource = database;
                }else{
                    changeDataSource = database + "_" + tenantId;
                }
            }
            log.info("当前租户Id={},当前线程使用库={}", tenantId, database);
            DynamicDataSourceContextHolder.setDataSourceKey(changeDataSource);
            result = jp.proceed();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
        return result;
    }

}
