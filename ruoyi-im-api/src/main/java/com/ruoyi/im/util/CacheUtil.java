package com.ruoyi.im.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存工具类
 * <p>
 * 封装常用的Redis操作，减少重复代码
 * </p>
 *
 * @author ruoyi
 */
public class CacheUtil {

    /**
     * 缓存基本的对象
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     * @param <T>   值类型
     */
    public static <T> void set(String key, T value) {
        set(key, value, 0);
    }

    /**
     * 缓存基本的对象，带过期时间
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  过期时间（秒）
     * @param <T>      值类型
     */
    public static <T> void set(String key, T value, long timeout) {
        set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 缓存基本的对象
     *
     * @param key     缓存的键值
     * @param value   缓存的值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @param <T>     值类型
     */
    public static <T> void set(String key, T value, long timeout, TimeUnit unit) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        if (timeout > 0) {
            operation.set(key, value, timeout, unit);
        } else {
            operation.set(key, value);
        }
    }

    /**
     * 设置带条件判断的缓存
     *
     * @param key     缓存的键值
     * @param value   缓存的值
     * @param timeout 过期时间（秒）
     * @param <T>     值类型
     */
    public static <T> void setIfNotNull(String key, T value, long timeout) {
        if (value != null) {
            set(key, value, timeout);
        }
    }

    /**
     * 获取缓存的基本对象
     *
     * @param key 缓存键值
     * @param <T> 值类型
     * @return 缓存键值对应的数据
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 获取缓存的基本对象，如果不存在则返回默认值
     *
     * @param key          缓存键值
     * @param defaultValue 默认值
     * @param <T>          值类型
     * @return 缓存键值对应的数据或默认值
     */
    public static <T> T getOrDefault(String key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 删除单个对象
     *
     * @param key 缓存键值
     */
    public static void delete(String key) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param keys 多个缓存键值
     */
    public static void delete(Collection<String> keys) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        redisTemplate.delete(keys);
    }

    /**
     * 判断缓存是否存在
     *
     * @param key 缓存键值
     * @return true-存在，false-不存在
     */
    public static boolean exists(String key) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置过期时间
     *
     * @param key     缓存键值
     * @param timeout 过期时间（秒）
     */
    public static void expire(String key, long timeout) {
        expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置过期时间
     *
     * @param key     缓存键值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public static void expire(String key, long timeout, TimeUnit unit) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间
     *
     * @param key 缓存键值
     * @return 过期时间（秒），-1表示永不过期，-2表示不存在
     */
    public static long getExpire(String key) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    // ============================ Hash缓存操作 ============================

    /**
     * 获取Hash中的数据
     *
     * @param key  Redis键
     * @param hash Hash键
     * @param <T>  值类型
     * @return Hash中的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T hGet(String key, String hash) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        HashOperations<String, String, T> hashOperations = redisTemplate.opsForHash();
        return hashOperations.get(key, hash);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  Redis键
     * @param hash Hash键
     * @param <T>  值类型
     * @return Hash中的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T hGet(String key, Object hash) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        HashOperations<String, Object, Object> hashOperations = (HashOperations<String, Object, Object>) redisTemplate.opsForHash();
        Object value = hashOperations.get(key, hash);
        return value != null ? (T) value : null;
    }

    /**
     * 设置Hash
     *
     * @param key   Redis键
     * @param hash  Hash键
     * @param value 值
     * @param <T>   值类型
     */
    @SuppressWarnings("unchecked")
    public static <T> void hSet(String key, String hash, T value) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        HashOperations<String, String, T> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(key, hash, value);
    }

    /**
     * 删除Hash
     *
     * @param key  Redis键
     * @param hash Hash键
     */
    public static void hDelete(String key, Object... hash) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        HashOperations<String, Object, Object> hashOperations = (HashOperations<String, Object, Object>) redisTemplate.opsForHash();
        hashOperations.delete(key, hash);
    }

    /**
     * 判断Hash中是否存在该hashKey
     *
     * @param key     Redis键
     * @param hashKey Hash键
     * @return true-存在，false-不存在
     */
    public static boolean hExists(String key, String hashKey) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        HashOperations<String, Object, Object> hashOperations = (HashOperations<String, Object, Object>) redisTemplate.opsForHash();
        return hashOperations.hasKey(key, hashKey);
    }

    // ============================ Set缓存操作 ============================

    /**
     * 向Set中添加值
     *
     * @param key    Redis键
     * @param values 值集合
     * @param <T>    值类型
     * @return 添加成功的数量
     */
    @SuppressWarnings("unchecked")
    public static <T> long sAdd(String key, T... values) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        SetOperations<String, T> setOperations = redisTemplate.opsForSet();
        return setOperations.add(key, values);
    }

    /**
     * 获取Set中的所有值
     *
     * @param key Redis键
     * @param <T> 值类型
     * @return Set集合
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> sMembers(String key) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        SetOperations<String, T> setOperations = redisTemplate.opsForSet();
        return setOperations.members(key);
    }

    /**
     * 判断Set中是否存在该值
     *
     * @param key   Redis键
     * @param value 值
     * @param <T>   值类型
     * @return true-存在，false-不存在
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean sIsMember(String key, T value) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        SetOperations<String, T> setOperations = redisTemplate.opsForSet();
        return Boolean.TRUE.equals(setOperations.isMember(key, value));
    }

    /**
     * 从Set中移除值
     *
     * @param key    Redis键
     * @param values 值集合
     * @param <T>    值类型
     * @return 移除成功的数量
     */
    @SuppressWarnings("unchecked")
    public static <T> long sRemove(String key, T... values) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        SetOperations<String, T> setOperations = redisTemplate.opsForSet();
        return setOperations.remove(key, values);
    }

    // ============================ List缓存操作 ============================

    /**
     * 向List左侧添加值
     *
     * @param key   Redis键
     * @param value 值
     * @param <T>   值类型
     * @return 添加后的List长度
     */
    @SuppressWarnings("unchecked")
    public static <T> long lLeftPush(String key, T value) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        ListOperations<String, T> listOperations = redisTemplate.opsForList();
        return listOperations.leftPush(key, value);
    }

    /**
     * 向List右侧添加值
     *
     * @param key   Redis键
     * @param value 值
     * @param <T>   值类型
     * @return 添加后的List长度
     */
    @SuppressWarnings("unchecked")
    public static <T> long lRightPush(String key, T value) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        ListOperations<String, T> listOperations = redisTemplate.opsForList();
        return listOperations.rightPush(key, value);
    }

    /**
     * 获取List中的值
     *
     * @param key   Redis键
     * @param start 开始位置
     * @param end   结束位置
     * @param <T>   值类型
     * @return List集合
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> lRange(String key, long start, long end) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        ListOperations<String, T> listOperations = redisTemplate.opsForList();
        return listOperations.range(key, start, end);
    }

    /**
     * 获取List长度
     *
     * @param key Redis键
     * @return List长度
     */
    @SuppressWarnings("unchecked")
    public static long lSize(String key) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        ListOperations<String, Object> listOperations = redisTemplate.opsForList();
        return listOperations.size(key);
    }

    // ============================ ZSet缓存操作 ============================

    /**
     * 向ZSet中添加值
     *
     * @param key   Redis键
     * @param value 值
     * @param score 分数
     * @param <T>   值类型
     * @return 添加成功的数量
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean zAdd(String key, T value, double score) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        ZSetOperations<String, T> zSetOperations = redisTemplate.opsForZSet();
        return Boolean.TRUE.equals(zSetOperations.add(key, value, score));
    }

    /**
     * 获取ZSet中指定范围的值
     *
     * @param key   Redis键
     * @param start 开始位置
     * @param end   结束位置
     * @param <T>   值类型
     * @return Set集合
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> zRange(String key, long start, long end) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        ZSetOperations<String, T> zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.range(key, start, end);
    }

    /**
     * 获取ZSet中指定范围的值（按分数排序）
     *
     * @param key   Redis键
     * @param min   最小分数
     * @param max   最大分数
     * @param <T>   值类型
     * @return Set集合
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> zRangeByScore(String key, double min, double max) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        ZSetOperations<String, T> zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.rangeByScore(key, min, max);
    }

    /**
     * 移除ZSet中的值
     *
     * @param key    Redis键
     * @param values 值集合
     * @param <T>    值类型
     * @return 移除成功的数量
     */
    @SuppressWarnings("unchecked")
    public static <T> long zRemove(String key, T... values) {
        RedisTemplate<String, T> redisTemplate = getRedisTemplate();
        ZSetOperations<String, T> zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.remove(key, values);
    }

    // ============================ 通用操作 ============================

    /**
     * 获取RedisTemplate
     */
    @SuppressWarnings("unchecked")
    private static <T> RedisTemplate<String, T> getRedisTemplate() {
        return SpringContextUtils.getBean("redisTemplate");
    }

    /**
     * 增量操作
     *
     * @param key   Redis键
     * @param delta 增量值
     * @return 增量后的值
     */
    public static long increment(String key, long delta) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 增量操作
     *
     * @param key   Redis键
     * @param delta 增量值
     * @return 增量后的值
     */
    public static double increment(String key, double delta) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        return redisTemplate.opsForValue().increment(key, delta);
    }

    private CacheUtil() {
        // 工具类禁止实例化
    }
}
