package com.mogudiandian.aop.limiting;

/**
 * 限流异常
 * @author sunbo
 */
public class RateLimitedException extends RuntimeException {

    public RateLimitedException(String message) {
        super(message);
    }
}
