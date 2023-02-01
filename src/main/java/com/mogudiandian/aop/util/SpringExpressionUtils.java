package com.mogudiandian.aop.util;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * SpEL的工具类
 *
 * @author sunbo
 */
public class SpringExpressionUtils {

    /**
     * 解析SpEL表达式
     * @param spel 表达式
     * @param method 方法
     * @param args 参数
     * @param clazz 需要返回的类型
     * @return 执行表达式后的结果
     */
    public static <T> T parse(String spel, Method method, Object[] args, Class<T> clazz) {
        // 表达式的context
        EvaluationContext context = new StandardEvaluationContext();

        if (args != null) {
            String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(method);
            if (parameterNames != null) {
                for (int i = 0, len = args.length; i < len; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
            }
        }

        // 解析表达式
        SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
        Expression expression = spelExpressionParser.parseExpression(spel);

        return expression.getValue(context, clazz);
    }

    /**
     * 解析SpEL的模板
     * @param template 模板
     * @param object 参数对象
     * @return 执行后的字符串
     */
    public static String parseTemplate(String template, Object object) {
        TemplateParserContext templateParserContext = new TemplateParserContext();
        ExpressionParser parser = new SpelExpressionParser();
        // new PropertySourcesPropertyResolver
        Expression expression = parser.parseExpression(template, templateParserContext);
        return expression.getValue(object, String.class);
    }
}