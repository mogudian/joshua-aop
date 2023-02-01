package com.mogudiandian.aop.caching;

import java.lang.annotation.*;


/**
 * 缓存
 * @author sunbo
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Caching {

    /**
     * 缓存的key表达式
     */
    String value();

    /**
     * 缓存的过期时间(毫秒) 默认为30秒
     */
    long expireMillis() default 30_000;

    /**
     * 是否缓存null值 默认不缓存
     */
    boolean cacheIfNull() default false;

}
