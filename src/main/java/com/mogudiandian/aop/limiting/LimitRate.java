package com.mogudiandian.aop.limiting;

import java.lang.annotation.*;

/**
 * 需要限速
 * @author sunbo
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LimitRate {

    /**
     * 限速的key表达式 多个方法可共用同一个key来共享限制
     */
    String spel();

    /**
     * 方法的QPS 默认为200(经验值)
     */
    int qps() default 200;

}
