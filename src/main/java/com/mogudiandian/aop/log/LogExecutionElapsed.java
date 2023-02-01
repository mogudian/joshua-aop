package com.mogudiandian.aop.log;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 记录执行耗时
 * @author sunbo
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogExecutionElapsed {

    /**
     * 耗时的时间级别
     * @return 时间单元 默认为毫秒
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}
