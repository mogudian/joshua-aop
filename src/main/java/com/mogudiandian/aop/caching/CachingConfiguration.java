package com.mogudiandian.aop.caching;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置
 * @author sunbo
 */
@Configuration
public class CachingConfiguration {

    @ConditionalOnMissingBean(CacheProvider.class)
    @Bean
    public CacheProvider cacheProvider() {
        return new GuavaCacheProvider();
    }

}