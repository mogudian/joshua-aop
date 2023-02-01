package com.mogudiandian.aop.feign;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

/**
 * 异常DTO
 * @author sunbo
 */
@Getter
@Setter
public class ThrowableDTO {

    /**
     * 异常类名
     */
    private String className;

    /**
     * 异常消息
     */
    private String message;

    /**
     * 异常栈元素数组
     */
    private StackTraceElementDTO[] stackTraceElements;

    /**
     * 原始的异常数组
     */
    private ThrowableDTO[] suppressed;

    /**
     * 原因
     */
    private ThrowableDTO cause;

    public ThrowableDTO() {
        super();
    }

    public ThrowableDTO(Throwable e) {
        this.className = e.getClass().getName();
        this.message = e.getMessage();

        this.stackTraceElements = Arrays.stream(e.getStackTrace())
                                        .map(StackTraceElementDTO::new)
                                        .toArray(StackTraceElementDTO[]::new);

        if (e.getSuppressed() != null) {
            this.suppressed = Arrays.stream(e.getSuppressed()).filter(Objects::nonNull)
                                    .filter(x -> x != e)
                                    .map(ThrowableDTO::new)
                                    .toArray(ThrowableDTO[]::new);
        }

        if (e.getCause() != null && e.getCause() != e) {
            this.cause = new ThrowableDTO(e.getCause());
        }
    }

    /**
     * 内部的栈轨迹数组转换为异常的
     * @return 异常的栈轨迹数组
     */
    private StackTraceElement[] toStackTrace() {
        return Arrays.stream(stackTraceElements)
                     .map(StackTraceElementDTO::toStackTraceElement)
                     .toArray(StackTraceElement[]::new);
    }

    /**
     * 转换为Java标准的异常
     * @return
     */
    public Throwable toThrowable() {
        Class<?> clazz = null;
        try {
            // forName在当前classloader下找类，如果找到了则继续，找不到会抛异常
            clazz = Class.forName(this.className);
        } catch (ClassNotFoundException ignored) {
            // 在当前classloader下找不到类的话默认使用RuntimeException
        }

        Throwable t = null;

        if (clazz != null) {
            // 优先找message+cause的构造器，这里cause不等于自己是为了防止死递归
            try {
                Constructor<?> constructor = clazz.getConstructor(String.class, Throwable.class);
                if (this.cause != null && this.cause != this) {
                    t = (Throwable) constructor.newInstance(this.message, this.cause.toThrowable());
                } else {
                    t = (Throwable) constructor.newInstance(this.message, null);
                }
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ignored) {
            }

            // 如果有message没cause并且没找到message+cause的构造器，尝试找message的，这个最省资源，也最符合实际场景（比如未登录、参数校验失败等只有提示信息）
            if (t == null && this.message != null && (this.cause == null || this.cause == this)) {
                try {
                    Constructor<?> constructor = clazz.getConstructor(String.class);
                    t = (Throwable) constructor.newInstance(this.message);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ignored) {
                }
            }
            // 如果没message有cause并且没找到message+cause的构造器，找cause的
            if (t == null && this.message == null && this.cause != null && this.cause != this) {
                try {
                    Constructor<?> constructor = clazz.getConstructor(Throwable.class);
                    t = (Throwable) constructor.newInstance(this.cause.toThrowable());
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ignored) {
                }
            }
            // 如果没有message也没有cause并且没有message|cause的任意构造器，使用默认构造器
            if (t == null) {
                try {
                    Constructor<?> constructor = clazz.getConstructor();
                    t = (Throwable) constructor.newInstance();
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ignored) {
                }
            }
        }

        // 找不到类或无法实例化该类时，默认使用RuntimeException
        if (t == null) {
            t = new RuntimeException(message, this.cause != null && this.cause != this ? this.cause.toThrowable() : null);
        }

        // 设置trace
        t.setStackTrace(this.toStackTrace());

        // 设置原始异常
        if (this.suppressed != null) {
            Arrays.stream(this.suppressed)
                  .filter(Objects::nonNull)
                  .filter(x -> x != this)
                  .map(ThrowableDTO::toThrowable)
                  .forEach(t::addSuppressed);
        }

        return t;
    }

    /**
     * 异常栈轨迹DTO
     * @author sunbo
     */
    @Getter
    @Setter
    public static class StackTraceElementDTO {
        private String declaringClass;
        private String methodName;
        private String fileName;
        private int lineNumber;

        public StackTraceElementDTO() {
            super();
        }

        public StackTraceElementDTO(StackTraceElement stackTraceElement) {
            this.declaringClass = stackTraceElement.getClassName();
            this.methodName = stackTraceElement.getMethodName();
            this.fileName = stackTraceElement.getFileName();
            this.lineNumber = stackTraceElement.getLineNumber();
        }

        public StackTraceElement toStackTraceElement() {
            return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
        }
    }

    public static void main(String[] args) {
        for (Scanner sc; ; ) {
            System.out.println("请输入数字");
            sc = new Scanner(System.in);
            String line = sc.nextLine();
            if ("exit".equals(line.trim())) {
                break;
            }
            try {
                try {
                    line.substring(5);
                    int number = Integer.parseInt(line);
                    System.out.println(number);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                ThrowableDTO dto = new ThrowableDTO(e);
                // System.out.println(JSON.toJSONString(dto, true));
                dto.toThrowable().printStackTrace();
            }
        }
    }

}
