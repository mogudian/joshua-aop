package com.mogudiandian.aop.mybatis;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Mybatis拦截器
 * 将insert into 重写为 insert ignore into
 *
 * @author sunbo
 */
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class RewriteInsertIntoInterceptor extends MybatisBaseInterceptor implements Interceptor {

    private static final String INSERT_INTO = "insert into";

    /**
     * 重写为
     */
    public enum RewriteTo {

        /**
         * insert ignore into
         */
        IGNORE(INSERT_INTO, "insert ignore into"),

        /**
         * replace into
         */
        REPLACE(INSERT_INTO, "replace into");

        private String source;

        private String target;

        RewriteTo(String source, String target) {
            this.source = source;
            this.target = target;
        }
    }

    /**
     * 重写子串和新串要执行的函数集合
     */
    private static List<Function<String, String>> rewriteSearcherFns = new ArrayList<>();

    static {
        // 要查找原SQL中insert into的大小写
        rewriteSearcherFns.add(String::toLowerCase);
        rewriteSearcherFns.add(String::toUpperCase);
    }


    /**
     * 切换开关的标记
     */
    private final ThreadLocal<RewriteTo> switchHolder;

    public RewriteInsertIntoInterceptor(ThreadLocal<RewriteTo> switchHolder) {
        if (switchHolder == null) {
            throw new IllegalArgumentException();
        }
        this.switchHolder = switchHolder;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 查看开关
        RewriteTo rewriteTo = switchHolder.get();

        // 开关有值时才处理
        if (rewriteTo != null) {

            StatementHandler handler = (StatementHandler) invocation.getTarget();
            BoundSql boundSql = handler.getBoundSql();
            String sql = boundSql.getSql();

            // SQL存在时处理
            if (StringUtils.isNotBlank(sql)) {

                StringBuilder stringBuilder = new StringBuilder(sql);
                boolean updated = false;

                for (Function<String, String> searcherFn : rewriteSearcherFns) {
                    String searcher = searcherFn.apply(rewriteTo.source);
                    for (int index = 0; (index = stringBuilder.indexOf(searcher, index)) >= 0; ) {
                        String newValue = searcherFn.apply(rewriteTo.target);
                        stringBuilder.replace(index, index + searcher.length(), newValue);
                        index += newValue.length();
                        updated = true;
                    }
                }

                // 如果有更新（原始SQL是insert）
                if (updated) {
                    // 替换BoundSql对象中的sql
                    setSql(boundSql, stringBuilder);
                }
            }
        }

        return invocation.proceed();
    }
}