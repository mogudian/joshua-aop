package com.mogudiandian.aop.validator;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * 参数校验的切面
 * @author sunbo
 */
@Component
@Aspect
public class RequestParamAspect {

    /**
     * 切入点 在类或方法上有该注解时切入
     */
    @Pointcut("@annotation(com.mogudiandian.aop.validator.ValidateParameter) || @within(com.mogudiandian.aop.validator.ValidateParameter)")
    public void pointcut() {
    }
 
    /**
     * 方法执行前校验参数合法性
     * @param joinPoint 连接点
     */
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        Arrays.stream(joinPoint.getArgs())
              .filter(Objects::nonNull)
              .filter(x -> BindingResult.class.isAssignableFrom(x.getClass()))
              .map(x -> (BindingResult) x)
              .filter(Errors::hasErrors)
              .map(Errors::getFieldErrors)
              .flatMap(Collection::stream)
              .findFirst()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .ifPresent(x -> {
                  throw new ParameterValidateException(x);
              });
    }
}