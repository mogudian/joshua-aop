package com.mogudiandian.aop.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 重写insert into
 * @author sunbo
 */
@Slf4j
@Component
@Aspect
public class RewriteInsertIntoAspect {

    /**
     * 是否已初始化
     */
    private final AtomicBoolean initStatus = new AtomicBoolean(false);

    /**
     * 开关所有者
     */
    private static ThreadLocal<RewriteInsertIntoInterceptor.RewriteTo> switchHolder = new ThreadLocal<>();

    @Autowired(required = false)
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @PostConstruct
    public void registerInterceptor() {
        if (CollectionUtils.isNotEmpty(sqlSessionFactoryList)) {
            if (initStatus.compareAndSet(false, true)) {
                RewriteInsertIntoInterceptor interceptor = new RewriteInsertIntoInterceptor(switchHolder);
                for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
                    org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
                    if (!isInterceptorExists(configuration, interceptor)) {
                        configuration.addInterceptor(interceptor);
                    }
                }
            }
        }
    }

    /**
     * 是否已经存在指定的拦截器
     * @param configuration mybatis的配置
     * @param interceptor 指定拦截器
     * @return 存在/不存在
     */
    private static boolean isInterceptorExists(org.apache.ibatis.session.Configuration configuration, Interceptor interceptor) {
        try {
            return configuration.getInterceptors().contains(interceptor);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 拦截 @InsertIgnoreInto
     * @param joinPoint 连接点
     * @return 方法返回
     * @throws Throwable 方法抛出的异常
     */
    @Around("@annotation(insertIgnoreInto)")
    public Object around(ProceedingJoinPoint joinPoint, InsertIgnoreInto insertIgnoreInto) throws Throwable {
        return doAround(joinPoint, RewriteInsertIntoInterceptor.RewriteTo.IGNORE);
    }

    /**
     * 拦截 @ReplaceInto
     * @param joinPoint 连接点
     * @return 方法返回
     * @throws Throwable 方法抛出的异常
     */
    @Around("@annotation(replaceInto)")
    public Object around(ProceedingJoinPoint joinPoint, ReplaceInto replaceInto) throws Throwable {
        return doAround(joinPoint, RewriteInsertIntoInterceptor.RewriteTo.REPLACE);
    }

    /**
     * 真正的方法
     * @param joinPoint 连接点
     * @param rewriteTo 重写为
     * @return 被代理方法执行
     * @throws Throwable 被代理方法的异常
     */
    private Object doAround(ProceedingJoinPoint joinPoint, RewriteInsertIntoInterceptor.RewriteTo rewriteTo) throws Throwable {
        // 没有初始化 说明mybatis配置有问题
        if (!initStatus.get()) {
            throw new RuntimeException("No SqlSessionFactory Bean(s) Found in Context");
        }

        // 设置要重写为什么
        switchHolder.set(rewriteTo);

        try {
            return joinPoint.proceed();
        } finally {
            // 清除上下文 避免内存溢出
            switchHolder.remove();
        }
    }

}