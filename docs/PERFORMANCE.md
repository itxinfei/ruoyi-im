# RuoYi-IM 性能优化方案

## 1. 数据库优化

### 1.1 消息表分区策略
```sql
-- 按月分区消息表
ALTER TABLE im_message PARTITION BY RANGE (YEAR(create_time) * 100 + MONTH(create_time)) (
    PARTITION p202301 VALUES LESS THAN (202302),
    PARTITION p202302 VALUES LESS THAN (202303),
    -- ...
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 历史消息归档（超过6个月的消息迁移到历史表）
CREATE TABLE im_message_history LIKE im_message;

-- 定时归档任务
INSERT INTO im_message_history SELECT * FROM im_message
WHERE create_time < DATE_SUB(NOW(), INTERVAL 6 MONTH);
DELETE FROM im_message WHERE create_time < DATE_SUB(NOW(), INTERVAL 6 MONTH);
```

### 1.2 索引优化
```sql
-- 覆盖索引优化常用查询
CREATE INDEX idx_msg_conversation_time
ON im_message(conversation_id, create_time DESC, id);

-- 复合索引优化未读消息统计
CREATE INDEX idx_member_unread
ON im_conversation_member(user_id, unread_count, is_pinned);

-- 消息搜索索引（全文索引）
ALTER TABLE im_message ADD FULLTEXT INDEX ft_content(content);
```

### 1.3 读写分离配置
```yaml
spring:
  datasource:
    master:
      url: jdbc:mysql://master-db:3306/im
    slave:
      urls:
        - jdbc:mysql://slave-db1:3306/im
        - jdbc:mysql://slave-db2:3306/im
```

## 2. 缓存策略优化

### 2.1 多级缓存架构
```java
/**
 * 多级缓存管理器
 * L1: 本地缓存（Caffeine）- 减少网络开销
 * L2: Redis缓存 - 分布式共享
 */
@Component
public class MultiLevelCacheManager {

    private final Cache<String, Object> localCache;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取缓存（先本地后Redis）
     */
    public Object get(String key) {
        // 1. 查询本地缓存
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }

        // 2. 查询Redis缓存
        value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            // 回写到本地缓存
            localCache.put(key, value);
        }
        return value;
    }

    /**
     * 更新缓存（同时更新本地和Redis）
     */
    public void put(String key, Object value, long ttl) {
        localCache.put(key, value);
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
    }
}
```

### 2.2 缓存预热
```java
/**
 * 缓存预热服务
 * 应用启动时加载热点数据
 */
@Component
public class CacheWarmUpService {

    @Autowired
    private ImUserService userService;

    @Autowired
    private ImConversationService conversationService;

    /**
     * 应用启动后执行预热
     */
    @PostConstruct
    public void warmUp() {
        // 预热活跃用户信息
        List<Long> activeUserIds = getActiveUserIds();
        activeUserIds.forEach(userId -> {
            userService.getUserById(userId); // 触发缓存
        });

        // 预热活跃会话
        List<Long> activeConversationIds = getActiveConversationIds();
        activeConversationIds.forEach(convId -> {
            conversationService.getConversationById(convId);
        });
    }
}
```

### 2.3 缓存更新策略
```java
/**
 * Cache Aside模式实现
 */
@Service
public class CacheAsideService {

    /**
     * 更新数据时先更新数据库，再删除缓存
     * 延迟双删策略防止并发问题
     */
    @Transactional
    public void updateWithCache(Long id, Object data) {
        // 1. 更新数据库
        updateToDb(id, data);

        // 2. 删除缓存
        deleteCache(id);

        // 3. 延迟删除（防止并发时缓存回填脏数据）
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(500); // 延迟500ms
                deleteCache(id);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
```

## 3. 连接管理优化

### 3.1 数据库连接池优化
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50        # 最大连接数
      minimum-idle: 10             # 最小空闲连接
      connection-timeout: 30000    # 连接超时（毫秒）
      idle-timeout: 600000         # 空闲连接超时（10分钟）
      max-lifetime: 1800000        # 连接最大生命周期（30分钟）
      connection-test-query: SELECT 1
```

### 3.2 Redis连接池优化
```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 5
        max-wait: 3000ms
      cluster:
        max-redirects: 3
```

### 3.3 WebSocket连接管理
```java
/**
 * WebSocket连接池管理
 */
@Component
public class WebSocketConnectionPool {

    private final ConcurrentHashMap<Long, Session> connections = new ConcurrentHashMap<>();
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    /**
     * 限制单服务器最大连接数
     */
    private static final int MAX_CONNECTIONS = 10000;

    /**
     * 添加连接（带限流）
     */
    public boolean addConnection(Long userId, Session session) {
        if (connectionCount.get() >= MAX_CONNECTIONS) {
            return false;
        }
        connections.put(userId, session);
        connectionCount.incrementAndGet();
        return true;
    }

    /**
     * 定期清理无效连接
     */
    @Scheduled(fixedRate = 60000) // 每分钟
    public void cleanInactiveConnections() {
        connections.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            if (!session.isOpen()) {
                connectionCount.decrementAndGet();
                return true;
            }
            return false;
        });
    }
}
```

## 4. 消息队列使用

### 4.1 异步消息处理
```java
/**
 * 消息发送服务（异步化）
 */
@Service
public class AsyncMessageService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 异步发送消息
     */
    public void sendMessageAsync(ImMessage message) {
        // 1. 保存消息到数据库（同步）
        saveMessage(message);

        // 2. 发送到消息队列（异步）
        rabbitTemplate.convertAndSend("im.message.exchange",
            "im.message.routing.key", message);
    }

    /**
     * 消费消息队列（推送给在线用户）
     */
    @RabbitListener(queues = "im.message.queue")
    public void consumeMessage(ImMessage message) {
        // 1. 获取会话在线成员
        // 2. 批量推送消息
        // 3. 更新未读数
    }
}
```

### 4.2 离线消息处理
```java
/**
 * 离线消息存储服务
 */
@Service
public class OfflineMessageService {

    /**
     * 用户离线时存储消息
     */
    public void storeOfflineMessage(Long userId, ImMessage message) {
        // 使用Redis List存储离线消息
        String key = "im:offline:" + userId;
        redisTemplate.opsForList().leftPush(key, message);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    /**
     * 用户上线时推送离线消息
     */
    public void pushOfflineMessages(Long userId, Session session) {
        String key = "im:offline:" + userId;
        List<ImMessage> messages = redisTemplate.opsForList().range(key, 0, -1);
        messages.forEach(msg -> {
            sendToSession(session, msg);
        });
        redisTemplate.delete(key);
    }
}
```

## 5. 性能监控

### 5.1 指标收集
```java
/**
 * 性能指标收集
 */
@Component
public class PerformanceMonitor {

    private final MeterRegistry meterRegistry;

    /**
     * 记录消息发送耗时
     */
    public void recordMessageSendTime(long millis) {
        Timer.builder("im.message.send.time")
            .description("消息发送耗时")
            .register(meterRegistry)
            .record(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录WebSocket连接数
     */
    public void recordConnectionCount(int count) {
        Gauge.builder("im.websocket.connections", () -> count)
            .description("WebSocket连接数")
            .register(meterRegistry);
    }
}
```

### 5.2 慢查询监控
```java
/**
 * 慢查询拦截器
 */
@Aspect
@Component
public class SlowQueryInterceptor {

    private static final long SLOW_QUERY_THRESHOLD = 1000; // 1秒

    @Around("execution(* com.ruoyi.im.mapper.*.*(..))")
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        if (duration > SLOW_QUERY_THRESHOLD) {
            log.warn("慢查询: {} 耗时: {}ms",
                joinPoint.getSignature().getName(), duration);
            // 发送告警
        }
        return result;
    }
}
```

## 6. 性能基准目标

| 指标 | 目标值 | 当前值 | 优化建议 |
|-----|-------|-------|---------|
| 单机并发连接数 | 10,000 | ~5,000 | WebSocket连接池优化 |
| 消息发送QPS | 50,000 | ~10,000 | 异步消息队列 |
| 消息存储延迟 | <50ms | ~100ms | 数据库优化 |
| 消息推送延迟 | <100ms | ~200ms | 批量推送优化 |
| 历史消息查询 | <500ms | ~1s | 索引优化+分区 |
| 缓存命中率 | >80% | ~60% | 多级缓存+预热 |
