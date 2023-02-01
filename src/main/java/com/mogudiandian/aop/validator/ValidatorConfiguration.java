package com.mogudiandian.aop.validator;

import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validation;
import javax.validation.Validator;

/**
 * 校验器配置
 * @author sunbo
 */
@ConditionalOnClass(Validator.class)
@Configuration
public class ValidatorConfiguration {

    @Bean
    @ConditionalOnMissingBean(Validator.class)
    public Validator joshuaValidator() {
        return Validation.byProvider(HibernateValidator.class)
                         .configure()
                         .failFast(false)
                         .buildValidatorFactory()
                         .getValidator();
    }

}
