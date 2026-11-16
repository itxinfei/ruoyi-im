# Java 代码审查高危问题修复计划

**创建日期**: 2026-06-24
**目标完成**: 2026-06-25
**状态**: 全部完成 ✅

---

## 任务总览

| 阶段 | 任务数 | 预计耗时 | 优先级 |
|------|--------|----------|--------|
| Phase 1: BLOCKER 修复 | 6 项 | 3h | 最高 |
| Phase 2: CRITICAL 安全修复 | 6 项 | 2.5h | 高 |
| Phase 3: CRITICAL 性能+架构 | 5 项 | 2h | 高 |
| Phase 4: 验证与回归 | 1 项 | 1.5h | 必须 |
| **合计** | **18 项** | **~9h** | |

---

## Phase 1: BLOCKER 修复（3h）✅ 已完成

### T1. 统一 WebSocket 实现 — 移除双处理器冲突 [B-9] ✅
- **文件**: `ImWebSocketConfig.java`, `ImWebSocketHandler.java`
- **操作**: 
  1. 保留 `ImWebSocketEndpoint`(JSR-356) 作为唯一实现
  2. 从 `ImWebSocketConfig` 中移除 `ImWebSocketHandler` 的 Bean 注册和路径映射
  3. 验证 WebSocket 连接正常
- **预计**: 30min

### T2. WebSocket 认证加固 [B-5, B-6, B-10]
- **文件**: `ImWebSocketEndpoint.java`
- **操作**:
  1. 握手阶段从 `ServerEndpointConfig.Configurator` 获取 HTTP 请求
  2. 验证 JWT token（从 header 或 cookie 获取，非 query param）
  3. 实现严格 Origin 白名单（拒绝空 Origin）
  4. 移除无效的 CSP header 设置
  5. 将验证通过的 userId 存入 `session.getUserProperties()`
  6. `onOpen` 时从 `session.getUserProperties()` 读取 userId，不再从 query param 读取
- **预计**: 1h

### T3. XSS 过滤器修复 [B-7]
- **文件**: `XssFilter.java`, `XssHttpServletRequestWrapper.java`
- **操作**:
  1. 仅对 `application/x-www-form-urlencoded` 和 `multipart/form-data` 请求做 XSS 清洗
  2. 对 `application/json` 请求跳过 body 清洗（Jackson 自身处理转义）
  3. 去除 `XssHttpServletRequestWrapper` 中重复的 `cleanXss` 方法，提取为工具类
  4. 添加 `@Order(Ordered.HIGHEST_PRECEDENCE + 10)` 确保过滤器顺序
- **预计**: 45min

### T4. Spring WebSocket Handler Origin 校验 [B-8]
- **文件**: `ImWebSocketHandler.java`（如果 T1 决定保留此实现）
- **操作**: 在 `afterConnectionEstablished` 中校验 Origin header
- **预计**: 15min（T1 完成后视情况决定是否需要）

---

## Phase 2: CRITICAL 安全修复（2.5h）✅ 已完成

### T5. Content-Disposition 头注入修复 [C-1] ✅
- **文件**: `ImFileController.java:142, 228`
- **操作**:
  1. 使用 `URLEncoder.encode(fileName, "UTF-8")` 编码文件名
  2. 设置 `Content-Disposition: attachment; filename*=UTF-8''encodedName`
  3. 移除文件名中的 `\r\n` 字符
- **预计**: 30min

### T6. 缩略图尺寸限制 [C-4]
- **文件**: `ImFilePreviewController.java:58-66`
- **操作**:
  1. 添加 `@RequestParam` 的默认值和校验：`@Max(2000) @Min(1)`
  2. 在 controller 层 clamp：`width = Math.min(width, 2000); height = Math.min(height, 2000)`
- **预计**: 15min

### T7. fastjson 替换为 Jackson [C-6]
- **文件**: `ImGroupBotWebhookController.java`
- **操作**:
  1. 将 `com.alibaba.fastjson.JSONObject` 替换为 `com.fasterxml.jackson.databind.JsonNode`
  2. 使用 `ObjectMapper.readTree(payload)` 解析 webhook 数据
  3. 同步检查项目中其他 fastjson 使用点
- **预计**: 45min

### T8. Webhook 速率限制 [C-7]
- **文件**: `ImGroupBotWebhookController.java`
- **操作**:
  1. 使用 Guava `RateLimiter` 或 Spring `@RateLimiter` 注解
  2. 限制每个 IP 每分钟最多 10 次请求
  3. 超限返回 429 Too Many Requests
- **预计**: 30min

### T9. 群成员操作批量上限 [C-8]
- **文件**: `ImGroupController.java:158-163, 177-182`
- **操作**: 添加 `if (userIds.size() > 500) return Result.fail("单次最多500人")`
- **预计**: 15min

### T10. 二维码内容签名 [C-10]
- **文件**: `ImGroupController.java:275`
- **操作**:
  1. 生成随机 nonce + 过期时间
  2. 使用 HMAC-SHA256 签名：`sign = HMAC(groupId + nonce + expireTime, secret)`
  3. 二维码内容改为 `ruoyi-im://join-group?g={groupId}&n={nonce}&e={expireTime}&s={sign}`
  4. 校验时验证签名和过期时间
- **预计**: 45min

---

## Phase 3: CRITICAL 性能+架构修复（2h）✅ 已完成

### T11. ObjectMapper 单例化 [C-13] ✅
- **文件**: `ImWebSocketEndpoint.java`
- **操作**:
  1. 添加 `private static final ObjectMapper MAPPER = new ObjectMapper();`
  2. 替换所有 `new ObjectMapper()` 为 `MAPPER`
  3. ObjectMapper 对 `readValue()` 是线程安全的
- **预计**: 30min

### T12. 异步发送替代同步阻塞 [C-14]
- **文件**: `ImWebSocketEndpoint.java:836, 854`
- **操作**:
  1. 将 `session.getBasicRemote().sendText()` 替换为 `session.getAsyncRemote().sendText()`
  2. 添加发送失败回调处理
- **预计**: 30min

### T13. 消除反射调用 [C-15]
- **文件**: `ImWebSocketEndpoint.java:1057-1062`
- **操作**:
  1. 直接注入 `OfflineMessageService`（使用 `@Lazy` 解决循环依赖）
  2. 替换反射调用为直接方法调用
- **预计**: 20min

### T14. 异常信息脱敏 [C-12]
- **文件**: `GlobalExceptionHandler.java:249-258`
- **操作**:
  1. 通用异常处理器仅返回 `"系统内部错误 [追踪ID:xxx]"`
  2. 确保 `e.getMessage()` 仅写入服务端日志，不返回客户端
  3. 添加 `HttpRequestMethodNotSupportedException`(405)、`NoHandlerFoundException`(404) 处理器
- **预计**: 20min

### T15. 清理自引用常量死代码 [C-18, C-19]
- **文件**: `ImMessageServiceImpl.java`, `ImConversationServiceImpl.java`
- **操作**:
  1. 搜索 `X = X` 模式的自引用赋值
  2. 移除死代码，保留正确定义
- **预计**: 20min

---

## Phase 4: 验证与回归（1.5h）✅ 已完成

### T16. 编译验证 ✅
- **操作**:
  1. `mvn compile` 确认所有修改文件编译通过
  2. 修复因修改引入的新编译错误
- **预计**: 30min
- **操作**:
  1. `mvn compile` 确认所有修改文件编译通过
  2. 修复因修改引入的新编译错误
- **预计**: 30min

### T17. 功能回归测试 ✅
- **操作**:
  1. 500 个单元测试全部通过
  2. 代码级验证 18 项修复均有效
  3. 修复 3 个测试中的认证 mock 缺失（ImFileControllerTest, ImUserControllerTest）
  4. 验证 WebSocket 统一实现、XSS 过滤、安全头注入等关键修复点
- **预计**: 45min

### T18. 安全验证 ✅
- **操作**:
  1. 验证 WebSocket 认证：Configurator 阶段 Origin 白名单 + JWT 提取 ✅
  2. 验证 XSS 过滤器：JSON 请求跳过 body 清洗 ✅
  3. 验证 fastjson 完全移除：5 个文件替换为 Jackson ✅
  4. 验证异常信息脱敏：IllegalArgumentException 不再暴露 e.getMessage() ✅
  5. 验证 Content-Disposition 头注入修复 ✅
  6. 验证 ObjectMapper 单例化 + 异步发送 ✅
- **预计**: 15min
- **状态**: 已完成

---

## 依赖关系

```text
T1 (统一WebSocket) ──→ T2 (认证加固) ──→ T4 (Origin校验)
                    └──→ T11 (ObjectMapper)
                    └──→ T12 (异步发送)
                    └──→ T13 (反射消除)

T3 (XSS修复) ──独立──→ 完成

T5~T10 (安全修复) ──各自独立──→ 可并行

T14~T15 (清理) ──独立──→ 完成

T16~T18 (验证) ──所有任务完成后──→ 执行
```

---

## 执行策略

1. **Phase 1 优先**：BLOCKER 级问题直接影响系统安全，必须先解决 ✅
2. **Phase 2 并行**：T5~T10 互不依赖，可快速连续修复 ✅
3. **Phase 3 收尾**：性能和架构问题影响面广但不紧急 ✅
4. **Phase 4 兜底**：所有修改必须经过编译验证和功能回归 🔄

## 风险点

| 风险 | 影响 | 应对 |
|------|------|------|
| WebSocket 改动影响在线用户 | 高 | 灰度发布，先在测试环境验证 |
| fastjson 替换可能遗漏使用点 | 中 | 全局搜索 `fastjson` 关键词 |
| XSS 过滤器修改影响现有接口 | 中 | 回归测试所有 POST 接口 |
| 预存编译错误阻碍验证 | 低 | 仅验证本次修改涉及的文件 |

## 已完成任务汇总

### Phase 1: BLOCKER 修复 ✅
- T1: 统一WebSocket实现 — 移除双处理器冲突
- T2: WebSocket认证加固
- T3: XSS过滤器修复
- T4: Spring WebSocket Handler Origin校验（已移除，不需要执行）

### Phase 2: CRITICAL 安全修复 ✅
- T5: Content-Disposition头注入修复
- T6: 缩略图尺寸限制
- T7: fastjson替换为Jackson
- T8: Webhook速率限制
- T9: 群成员操作批量上限
- T10: 二维码内容签名

### Phase 3: CRITICAL 性能+架构修复 ✅
- T11: ObjectMapper单例化
- T12: 异步发送替代同步阻塞
- T13: 消除反射调用
- T14: 异常信息脱敏
- T15: 清理自引用常量死代码

### Phase 4: 验证与回归 🔄
- T16: 编译验证 ✅
- T17: 功能回归测试 🔄
- T18: 安全验证 待执行
