package com.mogudiandian.aop.feign;

/**
 * 远程服务异常
 * @author sunbo
 */
public class RemoteServiceException extends RuntimeException {

    public RemoteServiceException(String message, Throwable cause) {
        super(message);
        // 本来应该调用super(message, cause)
        // 但是由于SpringBoot集成的tomcat中打印异常会层层找归因，导致一个异常除了cause均不能被打印
        // 详见 org.apache.catalina.core.StandardWrapper#getRootCause(ServletException)
        // 因此设置为suppressed避免被吞掉
        if (cause != null) {
            addSuppressed(cause);
        }
    }
}
