package com.mogudiandian.aop.caching;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 缓存的提供者
 * @author sunbo
 */
public class GuavaCacheProvider implements CacheProvider {

    private static final ConcurrentHashMap<Long, Cache<String, Optional<Object>>> expireMillisToCache = new ConcurrentHashMap<>();

    @Override
    public <T> void put(String key, T value, long expireMillis) {
        Cache<String, Optional<Object>> cache = expireMillisToCache.get(expireMillis);
        if (cache == null) {
            cache = expireMillisToCache.computeIfAbsent(expireMillis, k -> CacheBuilder.newBuilder()
                                                                                       .expireAfterWrite(k, TimeUnit.MILLISECONDS)
                                                                                       .build());
        }
        cache.put(key, Optional.ofNullable(value));
    }

    @Override
    public <T> T get(String key, long expireMillis) {
        Cache<String, Optional<Object>> cache = expireMillisToCache.get(expireMillis);
        if (cache == null) {
            return null;
        }
        Optional<Object> optional = cache.getIfPresent(key);
        if (optional == null || !optional.isPresent()) {
            return null;
        }
        return (T) optional.get();
    }

}
