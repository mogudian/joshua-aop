package com.mogudiandian.aop.feign;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Feign的远程服务错误解码器
 * @author sunbo
 */
@Configuration
public class RemoteServiceErrorDecoder implements ErrorDecoder, Constants {

    /**
     * 默认的解码器
     */
    private ErrorDecoder defaultErrorDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            try {
                String message = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                JSONObject jsonObject = JSON.parseObject(message);
                if (jsonObject != null && jsonObject.getBooleanValue(REMOTE_ERROR_MARK)) {
                    ThrowableDTO throwableDTO = jsonObject.getObject(REMOTE_ERROR_OBJECT_KEY, ThrowableDTO.class);
                    return new RemoteServiceException("Caught remote service [" + methodKey + "] exception", throwableDTO.toThrowable());
                }
            } catch (IOException e) {
                return e;
            }
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}