# RuoYi-IM 安全增强方案

## 1. JWT认证安全增强

### 1.1 当前问题
- 密钥硬编码在配置文件
- 缺少Token刷新机制
- 没有Token黑名单机制

### 1.2 增强方案

#### 密钥管理
```java
/**
 * JWT密钥管理服务
 * 支持密钥轮换和动态更新
 */
@Service
public class JwtKeyManager {
    private volatile String currentKey;
    private volatile String previousKey;
    private long keyRotationTime;

    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点轮换
    public void rotateKey() {
        previousKey = currentKey;
        currentKey = generateSecureKey();
        keyRotationTime = System.currentTimeMillis();
    }

    public String getCurrentKey() {
        return currentKey;
    }

    // 验证时同时使用当前密钥和上一轮密钥
    public boolean validateToken(String token) {
        // 先用当前密钥验证
        // 失败则用上一轮密钥验证
    }
}
```

#### Token刷新机制
```java
/**
 * Token刷新策略
 * Access Token有效期：2小时
 * Refresh Token有效期：30天
 */
public class TokenRefreshStrategy {
    // 当Access Token剩余有效期少于15分钟时自动刷新
    public boolean shouldRefresh(String accessToken) {
        Date expiration = getExpirationFromToken(accessToken);
        long minutesLeft = ChronoUnit.MINUTES.between(
            Instant.now(), expiration.toInstant());
        return minutesLeft < 15;
    }
}
```

#### Token黑名单
```java
/**
 * Token黑名单实现
 * 用于主动撤销Token（如用户退出登录）
 */
@Service
public class TokenBlacklist {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "im:token:blacklist:";

    /**
     * 将Token加入黑名单
     * 有效期设置为Token的剩余有效期
     */
    public void addToBlacklist(String token, long expireSeconds) {
        String jti = getJtiFromToken(token);
        redisTemplate.opsForValue().set(
            BLACKLIST_PREFIX + jti,
            "revoked",
            expireSeconds,
            TimeUnit.SECONDS
        );
    }

    /**
     * 检查Token是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        String jti = getJtiFromToken(token);
        return Boolean.TRUE.equals(
            redisTemplate.hasKey(BLACKLIST_PREFIX + jti)
        );
    }
}
```

## 2. 消息加密传输

### 2.1 端到端加密方案
```java
/**
 * 端到端加密服务
 * 每个会话使用独立的加密密钥
 */
@Service
public class E2EEncryptionService {

    /**
     * 生成会话加密密钥
     * 使用ECDH密钥交换协议
     */
    public String generateSessionKey(Long conversationId, List<Long> memberIds) {
        // 1. 为每个会话成员生成公私钥对
        // 2. 使用会话成员公钥加密会话密钥
        // 3. 将加密后的会话密钥分发给各成员
    }

    /**
     * 加密消息内容
     */
    public String encryptMessage(String content, String sessionKey) {
        // 使用AES-256-GCM加密
        // 每条消息使用不同的IV
    }
}
```

### 2.2 消息完整性校验
```java
/**
 * 消息签名服务
 * 确保消息未被篡改
 */
@Service
public class MessageSignatureService {

    /**
     * 生成消息签名
     */
    public String signMessage(Long messageId, String content, Long senderId) {
        String data = messageId + ":" + content + ":" + senderId;
        byte[] signature = hmacSHA256(data, getSigningKey(senderId));
        return Base64.encode(signature);
    }

    /**
     * 验证消息签名
     */
    public boolean verifySignature(Long messageId, String content,
                                   Long senderId, String signature) {
        String expected = signMessage(messageId, content, senderId);
        return MessageDigest.isEqual(expected, signature);
    }
}
```

## 3. 防重放攻击

### 3.1 时间戳验证
```java
/**
 * 请求时间戳验证器
 * 防止重放攻击
 */
@Component
public class TimestampValidator {

    private static final long ALLOWED_DELAY = 300_000; // 5分钟

    public boolean isValidTimestamp(long timestamp) {
        long now = System.currentTimeMillis();
        return Math.abs(now - timestamp) <= ALLOWED_DELAY;
    }
}
```

### 3.2 请求签名验证
```java
/**
 * API请求签名验证
 */
@Component
public class RequestSignatureValidator {

    public boolean verifySignature(HttpServletRequest request) {
        // 1. 获取请求参数
        // 2. 按规则拼接签名字符串
        // 3. 计算签名并比对
    }
}
```

## 4. 敏感信息过滤

### 4.1 敏感词过滤增强
```java
/**
 * 敏感词过滤器（DFA算法）
 */
@Service
public class SensitiveWordFilter {

    private TrieNode root = new TrieNode();

    /**
     * 初始化敏感词库
     */
    @PostConstruct
    public void init() {
        // 从数据库或配置文件加载敏感词
        // 构建DFA树
    }

    /**
     * 过滤敏感词
     * @return 过滤后的文本和被过滤的词列表
     */
    public FilterResult filter(String text) {
        // 使用DFA算法高效匹配
    }
}
```

### 4.2 数据脱敏
```java
/**
 * 数据脱敏注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {
    SensitiveType type() default SensitiveType.DEFAULT;
}

/**
 * 脱敏类型
 */
public enum SensitiveType {
    DEFAULT,       // 默认：显示前3后4
    PHONE,         // 手机号：138****1234
    ID_CARD,       // 身份证：110101********1234
    EMAIL,         // 邮箱：a***@example.com
    BANK_CARD      // 银行卡：6222************1234
}
```

## 5. 访问控制增强

### 5.1 基于角色的访问控制（RBAC）
```java
/**
 * 权限注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String value();
    LogicalType logical() default LogicalType.AND;
}

/**
 * 权限拦截器
 */
@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint,
                                RequirePermission requirePermission) {
        // 1. 获取当前用户
        // 2. 查询用户权限
        // 3. 验证是否有权限访问
    }
}
```

### 5.2 接口限流
```java
/**
 * 接口限流注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int value() default 100;  // 每秒请求数
    int duration() default 1; // 时间窗口（秒）
}

/**
 * Redis实现的限流器
 */
@Component
public class RateLimiter {

    public boolean tryAcquire(String key, int limit, int duration) {
        // 使用Redis的滑动窗口算法
    }
}
```
