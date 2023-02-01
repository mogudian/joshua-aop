package com.mogudiandian.aop.mybatis;

import lombok.SneakyThrows;
import org.apache.ibatis.mapping.BoundSql;

import java.lang.reflect.Field;

/**
 * Mybatis拦截器的基类
 *
 * @author sunbo
 */
public class MybatisBaseInterceptor {

    /**
     * BoundSql类的sql字段
     */
    private static Field sqlFieldOfBoundSql;

    static {
        // 需要反射的字段
        try {
            sqlFieldOfBoundSql = BoundSql.class.getDeclaredField("sql");
            sqlFieldOfBoundSql.setAccessible(true);
        } catch (Exception e) {
            // throw new RuntimeException(e);
        }
    }

    /**
     * 替换BoundSql对象中的sql
     * @param boundSql BoundSql对象
     * @param sql 新的SQL
     */
    @SneakyThrows
    protected void setSql(BoundSql boundSql, CharSequence sql) {
        sqlFieldOfBoundSql.set(boundSql, sql.toString());
    }

}
