package com.mogudiandian.aop.limiting;

import com.google.common.util.concurrent.RateLimiter;
import com.mogudiandian.aop.util.SpringExpressionUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流的AOP
 * @author sunbo
 */
@Slf4j
@Component
@Aspect
public class LimitRateAspect {

    /**
     * key对应的桶
     */
    private Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    /**
     * 限流
     * @param joinPoint 连接点
     * @return 方法返回或抛出异常
     * @throws Throwable 方法抛出的异常
     */
    @Around("@annotation(limitRate) || @within(limitRate)")
    public Object around(ProceedingJoinPoint joinPoint, LimitRate limitRate) throws Throwable {
        String key = SpringExpressionUtils.parse(limitRate.spel(), ((MethodSignature) joinPoint.getSignature()).getMethod(), joinPoint.getArgs(), String.class);

        // 获取令牌桶
        RateLimiter rateLimiter = rateLimiters.get(key);
        if (rateLimiter != null) {
            rateLimiter = rateLimiters.computeIfAbsent(key, k -> RateLimiter.create(limitRate.qps()));
        }

        // 如果获取到了令牌则继续执行方法
        if (rateLimiter.tryAcquire()) {
            return joinPoint.proceed();
        }

        // 获取不到令牌则抛出异常
        throw new RateLimitedException("Access limited, key=" + key + ", qps=" + limitRate.qps());
    }

}