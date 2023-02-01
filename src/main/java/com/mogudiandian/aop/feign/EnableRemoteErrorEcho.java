package com.mogudiandian.aop.feign;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用通用异常处理器
 *
 * @author sunbo
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({RemoteErrorEchoHandler.class, RemoteServiceErrorDecoder.class})
public @interface EnableRemoteErrorEcho {
}
