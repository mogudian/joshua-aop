package com.mogudiandian.aop.feign;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * 远程错误回显
 * @author sunbo
 */
@RestControllerAdvice
public class RemoteErrorEchoHandler implements Constants {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public JSONObject handleThrowable(Throwable e, HttpServletRequest httpServletRequest) throws Throwable {
        // 通过request获取处理器 一般是Controller#method(params)
        HandlerExecutionChain chain = requestMappingHandlerMapping.getHandler(httpServletRequest);
        if (chain != null) {
            Object handler = chain.getHandler();
            // 如果是HandlerMethod(普通方法)才处理，也有可能是Function等，待研究
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                // 获取方法签名上的返回值
                Class<?> methodReturnValueType = handlerMethod.getReturnType().getParameterType();
                // 如果值不是BaseApiResponse类型才处理，BaseApiResponse则直接抛出异常，面向UI的Controller应该自己来把控自己的异常处理，这里不去处理
                if (!ViewResponse.class.isAssignableFrom(methodReturnValueType)) {
                    JSONObject jsonObject = new JSONObject(new HashMap<>(2, 1));
                    jsonObject.put(REMOTE_ERROR_MARK, Boolean.TRUE);
                    jsonObject.put(REMOTE_ERROR_OBJECT_KEY, new ThrowableDTO(e));
                    return jsonObject;
                }
            }
        }
        throw e;
    }

}