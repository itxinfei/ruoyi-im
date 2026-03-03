# 日志记录最佳实践指南

**改进日期**: 2026-03-02
**改进状态**: 进行中

---

## 🎯 日志记录最佳实践

### 1. 正确使用日志级别

| 级别 | 用途 | 示例 |
|------|------|------|
| **TRACE** | 非常详细的调试信息 | `log.trace("方法入口: userId={}", userId)` |
| **DEBUG** | 调试信息，生产环境关闭 | `log.debug("查询参数: {}", params)` |
| **INFO** | 重要的业务流程信息 | `log.info("用户登录成功: userId={}", userId)` |
| **WARN** | 警告信息，需要关注但不是错误 | `log.warn("Token 即将过期: userId={}", userId)` |
| **ERROR** | 错误信息，需要立即处理 | `log.error("数据库连接失败", e)` |

### 2. 日志记录格式

**✅ 推荐格式**:
```java
// 包含上下文信息
log.info("用户登录成功: userId={}, username={}, ip={}, device={}", 
    userId, username, request.getRemoteAddr(), deviceId);

// 包含关键业务数据
log.info("发送消息成功: messageId={}, conversationId={}, receiverId={}", 
    messageId, conversationId, receiverId);

// 包含性能信息
log.info("查询完成: sql={}, 耗时={}ms, 返回={}条", sql, time, count);
```

**❌ 避免格式**:
```java
// 缺少关键信息
log.info("登录成功");

// 过于冗余
log.info("用户" + user.getUsername() + "在" + new Date() + "成功登录了系统");
```

### 3. 记录异常的完整信息

**✅ 推荐格式**:
```java
log.error("保存用户失败: userId={}, username={}", userId, username, e);
log.error("数据库操作失败: sql={}, params={}", sql, params, e);
log.error("远程调用失败: url={}, timeout={}ms", url, timeout, e);
```

**❌ 避免格式**:
```java
log.error("错误: {}", e.getMessage());  // 缺少异常堆栈
log.error("失败", e);  // 缺少错误信息
```

### 4. 关键业务操作必须记录

**必须记录的场景**:
- 用户登录/登出
- 权限变更
- 数据修改（增删改）
- 支付交易
- 系统配置变更
- 批量操作
- 异常和错误
- 性能关键操作

**示例**:
```java
// 用户登录
log.info("用户登录: userId={}, username={}, ip={}, device={}", 
    userId, username, request.getRemoteAddr(), deviceId);

// 发送消息
log.info("发送消息: messageId={}, senderId={}, receiverId={}, type={}", 
    messageId, senderId, receiverId, messageType);

// 删除消息
log.warn("删除消息: messageId={}, operatorId={}, reason={}", 
    messageId, userId, reason);

// 系统错误
log.error("系统异常: module={}, action={}, error={}", 
    "MessageService", "sendMessage", error, e);
```

### 5. 避免敏感信息记录

**❌ 避免记录**:
- 用户密码
- Token 完整内容
- 信用卡号
- 个人身份信息（PII）

**✅ 推荐做法**:
```java
// 记录密码长度而非密码本身
log.info("用户登录: userId={}, passwordLength={}", userId, password.length());

// 记录 Token 而非完整内容
log.info("Token 验证: userId={}, tokenPrefix={}", userId, token.substring(0, 10) + "...");

// 脱敏处理
log.info("用户注册: userId={}, phone={}", userId, maskPhone(phone));
```

### 6. 性能关键操作记录耗时

**✅ 推荐格式**:
```java
long startTime = System.currentTimeMillis();

// 执行操作
List<ImMessage> messages = messageService.getMessages(conversationId, userId, lastId, limit);

long duration = System.currentTimeMillis() - startTime;
log.info("查询消息完成: conversationId={}, 耗时={}ms, 返回={}条", 
    conversationId, duration, messages.size());

// 记录慢查询
if (duration > 1000) {
    log.warn("慢查询: sql={}, 耗时={}ms", sql, duration);
}
```

---

## 🔍 当前代码中的日志问题

### 发现的问题

1. **日志级别使用不当**
   - 部分正常业务流程使用 `log.warn`
   - 部分错误信息使用 `log.info`

2. **日志信息不完整**
   - 缺少关键上下文信息
   - 缺少关键业务数据

3. **异常日志不完整**
   - 部分异常日志缺少堆栈信息
   - 部分异常日志缺少上下文

---

## 📋 日志改进建议

### 1. WebSocket 日志改进

**当前问题**:
```java
log.warn("Token 解析失败：{}", e.getMessage());
```

**改进建议**:
```java
log.warn("WebSocket Token 解析失败: sessionId={}, userId={}, error={}", 
    session.getId(), userId, e.getMessage(), e);
```

### 2. 消息服务日志改进

**当前问题**:
```java
log.info("发送消息: userId={}, messageId={}", userId, messageId);
```

**改进建议**:
```java
log.info("发送消息: messageId={}, senderId={}, conversationId={}, type={}, receiverId={}, contentLength={}", 
    messageId, senderId, conversationId, messageType, receiverId, content != null ? content.length() : 0);
```

### 3. 用户服务日志改进

**当前问题**:
```java
log.info("用户登录: userId={}", userId);
```

**改进建议**:
```java
log.info("用户登录: userId={}, username={}, ip={}, device={}, deviceType={}, os={}", 
    userId, username, request.getRemoteAddr(), deviceId, deviceType, os);
```

### 4. 数据库操作日志改进

**当前问题**:
```java
log.error("数据库操作失败", e);
```

**改进建议**:
```java
log.error("数据库操作失败: table={}, operation={}, params={}, error={}", 
    tableName, operation, params, e.getMessage(), e);
```

---

## 🎯 日志记录清单

### 必须记录的场景

- [ ] 用户认证和授权（登录、登出、权限变更）
- [ ] 关键业务操作（发送消息、删除消息、转发消息）
- [ ] 数据修改操作（创建、更新、删除）
- [ ] 文件操作（上传、下载、删除）
- [ ] 系统配置变更
- [ ] 批量操作
- [ ] 异常和错误
- [ ] 性能关键操作（耗时 > 100ms）
- [ ] 安全事件（权限拒绝、登录失败）

### 可选记录的场景

- [ ] 正常的业务流程（DEBUG 级别）
- [ ] 性能统计信息（INFO 级别）
- [ ] 系统健康检查（INFO 级别）

---

## 📊 日志级别配置建议

### 开发环境
```yaml
logging:
  level:
    root: INFO
    com.ruoyi.im: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
```

### 测试环境
```yaml
logging:
  level:
    root: INFO
    com.ruoyi.im: INFO
    org.springframework.web: INFO
```

### 生产环境
```yaml
logging:
  level:
    root: WARN
    com.ruoyi.im: INFO
    org.springframework.web: WARN
    org.springframework.security: WARN
```

---

## ✅ 改进清单

- [ ] 调整 WebSocket 日志级别和信息
- [ ] 改进消息服务日志记录
- [ ] 改进用户服务日志记录
- [ ] 改进数据库操作日志记录
- [ ] 添加性能日志记录
- [ ] 添加安全事件日志记录
- [ ] 完善异常日志记录

---

## 🎯 预期效果

1. **更好的问题排查能力**: 详细的日志信息便于快速定位问题
2. **更好的系统监控**: 通过日志了解系统运行状态
3. **更好的审计能力**: 记录关键操作满足审计要求
4. **更好的性能分析**: 通过性能日志发现性能瓶颈

---

**改进完成时间**: 待定
**改进人员**: AI Code Reviewer