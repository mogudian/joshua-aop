package com.mogudiandian.aop.log;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 记录执行耗时
 * @author sunbo
 */
@Slf4j
@Component
@Aspect
public class LogExecutionElapsedAspect {

    /**
     * 记录执行耗时
     * @param joinPoint 连接点
     * @return 方法返回
     * @throws Throwable 方法抛出的异常
     */
    @Around("@annotation(logExecutionElapsed) || @within(logExecutionElapsed)")
    public Object around(ProceedingJoinPoint joinPoint, LogExecutionElapsed logExecutionElapsed) throws Throwable {
        /*String spanName = MDC.get("spanName");
        if (StringUtils.isBlank(spanName)) {
            spanName = String.format("%s#%s", joinPoint.getTarget().getClass().getSimpleName(), ((MethodSignature) joinPoint.getSignature()).getMethod().getName());
            MDC.put("spanName", spanName);
        }*/
        String spanName = String.format("%s#%s", joinPoint.getTarget().getClass().getSimpleName(), ((MethodSignature) joinPoint.getSignature()).getMethod().getName());
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            Object result = joinPoint.proceed();
            long elapsed = stopwatch.elapsed(logExecutionElapsed.timeUnit());
            // MDC.put("elapsed", String.valueOf(elapsed));
            log.info("{} elapsed {} {}", spanName, elapsed, logExecutionElapsed.timeUnit());
            return result;
        } catch (Throwable t) {
            // long elapsed = stopwatch.elapsed(logExecutionElapsed.timeUnit());
            // MDC.put("elapsed", String.valueOf(elapsed));
            log.error("{} throws ", spanName, t);
            throw t;
        } finally {
            // MDC.remove("elapsed");
        }
    }

}