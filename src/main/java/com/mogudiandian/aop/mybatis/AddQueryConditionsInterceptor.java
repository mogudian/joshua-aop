package com.mogudiandian.aop.mybatis;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Mybatis拦截器
 * 动态添加多个where条件
 *
 * @author sunbo
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class AddQueryConditionsInterceptor extends MybatisBaseInterceptor implements Interceptor {

    /**
     * select语句的正则
     */
    private static final Pattern selectPattern = Pattern.compile("^\\s*?select\\s", Pattern.CASE_INSENSITIVE);

    /**
     * 要加入的条件的指针
     */
    private final ThreadLocal<List<String>> conditionsHolder;

    public AddQueryConditionsInterceptor(ThreadLocal<List<String>> conditionsHolder) {
        if (conditionsHolder == null) {
            throw new IllegalArgumentException();
        }
        this.conditionsHolder = conditionsHolder;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 查看开关
        List<String> conditions = conditionsHolder.get();

        // 开关有值时才处理
        if (CollectionUtils.isNotEmpty(conditions)) {
            Object[] args = invocation.getArgs();
            MappedStatement mappedStatement = (MappedStatement) args[0];
            Object parameter = args[1];
            RowBounds rowBounds = (RowBounds) args[2];
            ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];

            Executor executor = (Executor) invocation.getTarget();
            CacheKey cacheKey = null;
            BoundSql boundSql = null;

            // 拦截到的是4个参数的query方法
            if (args.length == 4) {
                boundSql = mappedStatement.getBoundSql(parameter);
                cacheKey = executor.createCacheKey(mappedStatement, parameter, rowBounds, boundSql);
            } else if (args.length == 6) {
                // 拦截到的是6个参数的query方法
                cacheKey = (CacheKey) args[4];
                boundSql = (BoundSql) args[5];
            }

            String sql = boundSql.getSql();

            // SQL如果形似select语句时处理
            if (sql != null && selectPattern.matcher(sql).find()) {
                SQLSelectStatement selectStatement = null;

                // 解析SQL并新增where条件
                try {
                    SQLStatementParser parser = new MySqlStatementParser(sql);
                    SQLStatement statement = parser.parseStatement();
                    if (statement instanceof SQLSelectStatement) {
                        selectStatement = (SQLSelectStatement) statement;
                    }
                } catch (Exception ignored) {
                }

                // SQL是否有更新
                boolean updated = false;

                // 判断SQL到底是不是select语句
                if (selectStatement != null) {
                    SQLSelectQuery query = selectStatement.getSelect().getQuery();
                    SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) query;

                    for (String condition : conditions) {
                        // 需要追加的条件
                        SQLExpr appendCondition = SQLUtils.toMySqlExpr(condition);
                        // 新条件（老条件+追加的条件）
                        SQLExpr newWhere;
                        if (queryBlock.getWhere() == null) {
                            newWhere = appendCondition;
                        } else {
                            newWhere = new SQLBinaryOpExpr(queryBlock.getWhere(), SQLBinaryOperator.BooleanAnd, appendCondition);
                        }
                        queryBlock.setWhere(newWhere);
                    }

                    // 将AST转成字符串
                    sql = SQLUtils.toMySqlString(selectStatement, new SQLUtils.FormatOption(false, false));
                    updated = true;
                }

                // 如果有更新（原始SQL是insert）
                if (updated) {
                    // 替换BoundSql对象中的sql
                    // setSql(boundSql, sql);
                    boundSql = new BoundSql(mappedStatement.getConfiguration(), sql, boundSql.getParameterMappings(), boundSql.getParameterObject());
                    return executor.query(mappedStatement, parameter, rowBounds, resultHandler, cacheKey, boundSql);
                }
            }
        }

        return invocation.proceed();
    }

}