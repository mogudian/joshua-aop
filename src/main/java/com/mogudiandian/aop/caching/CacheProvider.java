package com.mogudiandian.aop.caching;

/**
 * 缓存的提供者
 * @author sunbo
 */
public interface CacheProvider {

    /**
     * 存储
     * @param key 键
     * @param value 值
     * @param expireMillis 过期时长
     * @param <T> 值类型
     */
    <T> void put(String key, T value, long expireMillis);

    /**
     * 获取
     * @param key 键
     * @param expireMillis 过期时长
     * @param <T> 值类型
     * @return 值
     */
    <T> T get(String key, long expireMillis);

}
