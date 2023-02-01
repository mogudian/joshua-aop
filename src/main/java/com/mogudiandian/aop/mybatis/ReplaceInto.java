package com.mogudiandian.aop.mybatis;

import java.lang.annotation.*;


/**
 * replace into
 * @author sunbo
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReplaceInto {

}
