package com.mogudiandian.aop.validator;

/**
 * 参数校验异常
 * @author sunbo
 */
public class ParameterValidateException extends RuntimeException {

    public ParameterValidateException(String message) {
        super(message);
    }
}
