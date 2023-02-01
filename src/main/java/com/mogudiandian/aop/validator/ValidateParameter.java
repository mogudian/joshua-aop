package com.mogudiandian.aop.validator;

import java.lang.annotation.*;

/**
 * 需要校验参数
 * @author sunbo
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidateParameter {
}
