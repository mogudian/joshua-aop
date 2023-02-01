package com.mogudiandian.aop.mybatis;

import java.lang.annotation.*;


/**
 * insert ignore into
 * @author sunbo
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InsertIgnoreInto {

}
