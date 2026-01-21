package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * IM分布式锁工具类
 * 基于Redis实现分布式锁，用于保护关键操作
 *
 * @author ruoyi
 */
@Component
public class ImDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(ImDistributedLock.class);

    private static final String LOCK_PREFIX = "im:lock:";
    private static final String LOCK_VALUE_PREFIX = "lock:";
    private static final long DEFAULT_EXPIRE_TIME = 30; // 默认锁过期时间30秒

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 尝试获取锁
     *
     * @param lockKey   锁的key
     * @param expireTime 锁过期时间（秒）
     * @return 锁对象，如果获取失败返回null
     */
    public Lock tryLock(String lockKey, long expireTime) {
        String fullKey = LOCK_PREFIX + lockKey;
        String lockValue = LOCK_VALUE_PREFIX + UUID.randomUUID().toString();

        Boolean success = redisTemplate.opsForValue().setIfAbsent(fullKey, lockValue, expireTime, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(success)) {
            log.debug("获取分布式锁成功: key={}", fullKey);
            return new Lock(fullKey, lockValue);
        } else {
            log.debug("获取分布式锁失败: key={}", fullKey);
            return null;
        }
    }

    /**
     * 尝试获取锁（使用默认过期时间）
     *
     * @param lockKey 锁的key
     * @return 锁对象，如果获取失败返回null
     */
    public Lock tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 释放锁
     *
     * @param lock 锁对象
     */
    public void unlock(Lock lock) {
        if (lock == null) {
            return;
        }

        try {
            // 使用Lua脚本确保只有锁的持有者才能释放锁
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            redisTemplate.execute(new org.springframework.data.redis.core.script.DefaultRedisScript<>(
                    script, Long.class), java.util.Collections.singletonList(lock.getKey()), lock.getValue());

            log.debug("释放分布式锁成功: key={}", lock.getKey());
        } catch (Exception e) {
            log.error("释放分布式锁失败: key={}", lock.getKey(), e);
        }
    }

    /**
     * 执行带锁的操作
     *
     * @param lockKey   锁的key
     * @param operation 要执行的操作
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, LockOperation<T> operation) {
        return executeWithLock(lockKey, DEFAULT_EXPIRE_TIME, operation);
    }

    /**
     * 执行带锁的操作
     *
     * @param lockKey    锁的key
     * @param expireTime 锁过期时间（秒）
     * @param operation  要执行的操作
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, long expireTime, LockOperation<T> operation) {
        Lock lock = null;
        try {
            lock = tryLock(lockKey, expireTime);
            if (lock == null) {
                throw new RuntimeException("获取锁失败: " + lockKey);
            }
            return operation.execute();
        } finally {
            if (lock != null) {
                unlock(lock);
            }
        }
    }

    /**
     * 尝试执行带锁的操作（如果获取锁失败则返回null）
     *
     * @param lockKey   锁的key
     * @param operation 要执行的操作
     * @return 操作结果，如果获取锁失败返回null
     */
    public <T> T tryExecuteWithLock(String lockKey, LockOperation<T> operation) {
        return tryExecuteWithLock(lockKey, DEFAULT_EXPIRE_TIME, operation);
    }

    /**
     * 尝试执行带锁的操作（如果获取锁失败则返回null）
     *
     * @param lockKey    锁的key
     * @param expireTime 锁过期时间（秒）
     * @param operation  要执行的操作
     * @return 操作结果，如果获取锁失败返回null
     */
    public <T> T tryExecuteWithLock(String lockKey, long expireTime, LockOperation<T> operation) {
        Lock lock = tryLock(lockKey, expireTime);
        if (lock == null) {
            return null;
        }

        try {
            return operation.execute();
        } finally {
            unlock(lock);
        }
    }

    /**
     * 锁对象
     */
    public static class Lock {
        private final String key;
        private final String value;

        public Lock(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 锁操作接口
     */
    @FunctionalInterface
    public interface LockOperation<T> {
        T execute();
    }

    /**
     * 预定义的锁Key常量
     */
    public static class LockKeys {
        /** 用户会话锁 */
        public static final String USER_CONVERSATION = "user:conversation:";

        /** 发送消息锁 */
        public static final String SEND_MESSAGE = "send:message:";

        /** 好友关系锁 */
        public static final String FRIEND_RELATION = "friend:relation:";

        /** 群组操作锁 */
        public static final String GROUP_OPERATION = "group:operation:";

        /**
         * 生成用户会话锁Key
         */
        public static String userConversationKey(Long userId) {
            return USER_CONVERSATION + userId;
        }

        /**
         * 生成发送消息锁Key
         */
        public static String sendMessageKey(Long conversationId) {
            return SEND_MESSAGE + conversationId;
        }

        /**
         * 生成好友关系锁Key
         */
        public static String friendRelationKey(Long userId, Long friendId) {
            long min = Math.min(userId, friendId);
            long max = Math.max(userId, friendId);
            return FRIEND_RELATION + min + ":" + max;
        }

        /**
         * 生成群组操作锁Key
         */
        public static String groupOperationKey(Long groupId) {
            return GROUP_OPERATION + groupId;
        }
    }
}
