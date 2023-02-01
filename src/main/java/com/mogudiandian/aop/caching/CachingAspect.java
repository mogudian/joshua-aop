package com.mogudiandian.aop.caching;

import com.mogudiandian.aop.util.SpringExpressionUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 缓存
 * @author sunbo
 */
@Slf4j
@Component
@Aspect
public class CachingAspect {

    @Autowired(required = false)
    private CacheProvider cacheProvider;

    /**
     * 缓存
     * @param joinPoint 连接点
     * @return 方法返回
     * @throws Throwable 方法抛出的异常
     */
    @Around("@annotation(caching)")
    public Object around(ProceedingJoinPoint joinPoint, Caching caching) throws Throwable {
        if (cacheProvider == null) {
            return joinPoint.proceed();
        }

        Object value = SpringExpressionUtils.parse(caching.value(), ((MethodSignature) joinPoint.getSignature()).getMethod(), joinPoint.getArgs(), Object.class);

        if (value == null) {
            throw new NullPointerException("parse caching key expression result is null");
        }

        String key = value.toString();

        // 先从缓存获取
        Object obj = cacheProvider.get(key, caching.expireMillis());

        if (obj == null) {
            log.debug("caching key {} missed!", key);
            obj = joinPoint.proceed();
            if (obj != null || caching.cacheIfNull()) {
                cacheProvider.put(key, obj, caching.expireMillis());
            }
        } else {
            log.debug("caching key {} hit!", key);
        }

        return obj;
    }



}