# 16 Spring Cloud Gateway 网关路由配置

> **定位**：定义 im-gateway 的完整路由规则、过滤器链、限流策略和跨域配置。
> **更新时间**：2026-10-30

---

## 一、完整 application.yml

```yaml
# im-gateway/src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: im-gateway
  profiles:
    active: ${NAMESPACE:dev}
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER:nacos:8848}
        namespace: ${NAMESPACE:dev}
        group: IM_GROUP

    gateway:
      discovery:
        locator:
          enabled: false   # 不用自动路由, 全部手工指定

      # 全局超时
      httpclient:
        connect-timeout: 5000
        response-timeout: 30s
        pool:
          type: FIXED
          max-connections: 500
          acquire-timeout: 2000
          max-idle-time: 10s

      # —— 路由规则 ——
      routes:
        # 1. 认证服务 (公开,合并至 platform-user)
        - id: platform-user-public
          uri: lb://platform-user
          predicates:
            - Path=/im/auth/**
          filters:
            - StripPrefix=0
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 20   # 每秒 20 令牌
                redis-rate-limiter.burstCapacity: 40   # 突发 40
                key-resolver: "#{@ipKeyResolver}"       # 按 IP

        # 2. 用户服务
        - id: platform-user
          uri: lb://platform-user
          predicates:
            - Path=/im/user/**,/im/contact/**,/im/dept/**,/im/auth/**,/im/friend/**
          filters:
            - name: JwtAuthenticationFilter    # 自定义 JWT 校验
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 30
                redis-rate-limiter.burstCapacity: 60

        # 3. 会话服务
        - id: im-session
          uri: lb://im-session
          predicates:
            - Path=/im/session/**
          filters:
            - name: JwtAuthenticationFilter
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 50
                redis-rate-limiter.burstCapacity: 100

        # 4. 消息服务 (核心, 最高吞吐)
        - id: im-message
          uri: lb://im-message
          predicates:
            - Path=/im/message/**
          filters:
            - name: JwtAuthenticationFilter
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 80
                redis-rate-limiter.burstCapacity: 160

        # 4a. AI 对话 (platform-ai, 8091)
        - id: platform-ai
          uri: lb://platform-ai
          predicates:
            - Path=/im/ai/**
          filters:
            - name: JwtAuthenticationFilter
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20

        # 4b. 全局搜索 (platform-search, 8092)
        - id: platform-search
          uri: lb://platform-search
          predicates:
            - Path=/im/search/**
          filters:
            - name: JwtAuthenticationFilter
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20

        # 5. 群组服务
        - id: im-group
          uri: lb://im-group
          predicates:
            - Path=/im/group/**
          filters:
            - name: JwtAuthenticationFilter

        # 6. 通话服务 (LiveKit VoIP, WS 信令走 im-real-time)
        - id: im-rtc
          uri: lb://im-real-time
          predicates:
            - Path=/im/call/**
          filters:
            - name: JwtAuthenticationFilter

        # 7a. 任务与审批 (collab-task)
        - id: collab-task
          uri: lb://collab-task
          predicates:
            - Path=/im/todo/**,/im/approval/**,/im/report/**
          filters:
            - name: JwtAuthenticationFilter

        # 7b. 日历 (collab-calendar)
        - id: collab-calendar
          uri: lb://collab-calendar
          predicates:
            - Path=/im/calendar/**
          filters:
            - name: JwtAuthenticationFilter

        # 8. 文档协作 (collab-doc + ONLYOFFICE)
        - id: collab-doc
          uri: lb://collab-doc
          predicates:
            - Path=/im/doc/**
          filters:
            - name: JwtAuthenticationFilter

        # 9. 文件上传 (file-storage, 单独限流保护带宽)
        - id: file-storage-upload
          uri: lb://file-storage
          predicates:
            - Path=/im/file/upload,/im/file/chunk/**
          filters:
            - name: JwtAuthenticationFilter
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 5     # 上传限制更严
                redis-rate-limiter.burstCapacity: 10
                key-resolver: "#{@userKeyResolver}"       # 按用户 ID 限流

        # 11. 文件下载 (公开预签名 URL, 不校验 JWT)
        - id: file-storage-download
          uri: http://minio:9000
          predicates:
            - Path=/im/file/download/**
          filters:
            - StripPrefix=3   # 去掉 /im/file/download

        # 10. 管理后台 (admin-system, 需 ADMIN 角色)
        - id: admin-system
          uri: lb://admin-system
          predicates:
            - Path=/im/admin/**
          filters:
            - name: JwtAuthenticationFilter
              args:
                requiredRole: "ADMIN"

        # 13. ONLYOFFICE 回调 (内部, IP 白名单)
        - id: onlyoffice-callback
          uri: lb://collab-doc
          predicates:
            - Path=/im/doc/office/callback
          filters:
            - name: IpWhitelistFilter
              args:
                whitelist: "172.28.0.0/16,127.0.0.1"

        # 默认 404
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin
        - name: CircuitBreaker
          args:
            name: default-cb
            fallbackUri: forward:/fallback/error

      # 全局 CORS
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "${CORS_ORIGINS:http://localhost:3000,https://*.ruoyi.com}"
            allowedMethods: "*"
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600

# Resilience4j 熔断配置
resilience4j:
  circuitbreaker:
    instances:
      default-cb:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
    configs:
      default:
        registerHealthIndicator: true

# Redis (限流 + IP 白名单)
spring:
  data:
    redis:
      host: ${REDIS_HOST:redis}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PWD:redis_pwd_123}
      lettuce:
        pool:
          max-active: 16

# 限流 Key 解析器
gateway:
  rate-limit:
    ip-header: X-Real-IP   # 从 Nginx 获取真实 IP
    whitelist-ips: "127.0.0.1,172.28.0.1"

# JWT
jwt:
  public-key: ${JWT_PUBLIC_KEY:classpath:public.pem}

# 健康检查
management:
  endpoints:
    web:
      exposure:
        include: gateway,health,info,prometheus
  endpoint:
    gateway:
      enabled: true

logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: INFO

---
# 路由优先级顺序 (排在前面的优先匹配)
# 注意: Path 路由从上到下匹配, 第一个匹配到的就停止
```

### 搜索路由补充

| 路径 | 方法 | 目标服务 | 说明 |
|------|------|---------|------|
| /im/search/** | GET | platform-search 8092 | 全局消息搜索（会话内+跨会话） |
| /im/search/sessions | GET | platform-search 8092 | 会话名搜索 |
| /im/search/contacts | GET | platform-search 8092 | 联系人搜索 |
| /im/ai/** | GET/POST | platform-ai 8091 | AI 对话流式端点（chat/stream/models/status） |

**限流**: 全局搜索 10 次/分钟（对齐 09-协同办公需求 §F10 搜索配额）

---

## 二、JWT 认证过滤器

```java
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired private JwtUtils jwtUtils;
    @Autowired private ReactiveRedisTemplate<String, String> redisTemplate;

    // 公开路径 (不需要 JWT)
    private static final List<String> WHITE_LIST = List.of(
        "/im/auth/login",
        "/im/auth/register",
        "/im/auth/refresh",
        "/im/file/download/",
        "/im/doc/office/callback",
        "/im/actuator/"
    );

    // 需要 ADMIN 角色的路径前缀
    private static final List<String> ADMIN_PATHS = List.of("/im/admin/");

    private String requiredRole;   // 由 Config 传入

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 1. 白名单直接放行
            if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }

            // 2. 提取 Token
            String token = extractToken(request);
            if (token == null) {
                return unauthorized(exchange, "未提供认证令牌");
            }

            // 3. 校验 Token 有效性
            if (!jwtUtils.isValid(token)) {
                return unauthorized(exchange, "令牌无效");
            }

            // 4. 吊销检查 (Redis 黑名单)
            String jti = jwtUtils.getJti(token);
            return redisTemplate.hasKey("blacklist:" + jti)
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        return unauthorized(exchange, "令牌已吊销");
                    }

                    Long userId = jwtUtils.getUserId(token);

                    // 5. 角色检查 (ADMIN 路径)
                    if (ADMIN_PATHS.stream().anyMatch(path::startsWith)) {
                        if (!jwtUtils.hasRole(token, "ADMIN")) {
                            return forbidden(exchange, "需要 ADMIN 角色");
                        }
                    }

                    // 6. 将 userId 注入到请求头, 下游服务直接读取
                    ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Name", jwtUtils.getUsername(token))
                        .header("X-User-Roles", String.join(",", jwtUtils.getRoles(token)))
                        .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
        };
    }

    private String extractToken(ServerHttpRequest request) {
        String bearer = request.getHeaders().getFirst("Authorization");
        if (StrUtil.isNotBlank(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        // 检查 Query Param (WS 无法带 Header)
        return request.getQueryParams().getFirst("token");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = JSONUtil.toJsonStr(Result.fail(30003, msg));
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = JSONUtil.toJsonStr(Result.fail(30004, msg));
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }

    @Data
    public static class Config {
        private String requiredRole;
    }
}
```

---

## 三、限流 Key 解析器

```java
@Configuration
public class RateLimitConfig {

    // 按 IP 限流 (匿名)
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
            if (StrUtil.isBlank(ip)) {
                ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }
            return Mono.just("rate:ip:" + ip);
        };
    }

    // 按用户 ID 限流 (已登录)
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (StrUtil.isBlank(userId)) {
                return Mono.just("rate:user:anonymous");
            }
            return Mono.just("rate:user:" + userId);
        };
    }
}
```

---

## 四、限流超限响应

```java
@Component
public class RateLimitHandler {

    @Bean
    public CustomizedExceptionHandler customizedExceptionHandler() {
        return new CustomizedExceptionHandler();
    }

    public static class CustomizedExceptionHandler implements ErrorWebExceptionHandler {

        @Override
        public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
            if (ex instanceof ResponseStatusException e &&
                e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {

                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> body = Map.of(
                    "code", 10003,
                    "msg", "请求过于频繁, 请稍后再试",
                    "data", Map.of("retryAfter", 30),
                    "timestamp", System.currentTimeMillis()
                );

                byte[] bytes = JSONUtil.toJsonStr(body).getBytes();
                return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
                );
            }
            return Mono.error(ex);
        }
    }
}
```

---

## 五、IP 白名单过滤器 (ONLYOFFICE 回调)

```java
@Component
public class IpWhitelistFilter extends AbstractGatewayFilterFactory<IpWhitelistFilter.Config> {

    private String[] whitelist;

    public IpWhitelistFilter() { super(Config.class); }

    @Override
    public GatewayFilter apply(Config config) {
        this.whitelist = config.getWhitelist().split(",");
        return (exchange, chain) -> {
            String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            boolean allowed = Arrays.stream(whitelist)
                .anyMatch(cidr -> IpUtils.isInRange(clientIp, cidr));

            if (!allowed) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        };
    }

    @Data
    public static class Config {
        private String whitelist;
    }
}
```

---

## 六、降级响应

```java
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/error")
    public Mono<Result<String>> fallback() {
        return Mono.just(Result.fail(10002, "服务暂时不可用, 请稍后重试"));

    @RequestMapping(value = "/{service}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Result<String>> serviceFallback(@PathVariable String service) {
        return Mono.just(Result.fail(10002, "服务 [" + service + "] 不可用"));
    }
}
```

---

## 七、Gateway 监控端点

```bash
# 查看所有路由
GET /actuator/gateway/routes

# 刷新路由
POST /actuator/gateway/refresh

# 查看过滤器
GET /actuator/gateway/globalfilters

# 查看指定路由
GET /actuator/gateway/routes/im-message
```

---

## 八、变更记录

| 日期 | 变更 |
|------|------|
| 2026-10-30 | 完成 im-gateway 全部路由规则/YAML 配置/JWT 过滤器/限流配置/熔断配置/降级响应 |
| 2026-11-06 | §4a 新增 platform-ai (8091) 路由；§4b 新增 platform-search (8092) 路由；搜索路由表同步修正（此前 /im/search/** 错误指向 im-message 8081） |
