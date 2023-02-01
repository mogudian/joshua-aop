package com.mogudiandian.aop;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * AOP启动类
 * @author sunbo
 */
@Configuration
@ComponentScan(basePackages = "com.mogudiandian.aop")
public class AopConfiguration {

}