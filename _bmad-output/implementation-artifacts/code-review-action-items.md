# 代码审查待办事项

**审查日期：** 2026-02-26
**审查范围：** Git工作目录中的所有修改文件
**问题总数：** 19个（8高、7中、4低）

---

## 🔴 高优先级待办 (HIGH)

- [ ] [HIGH][安全] 修复WebSocket认证绕过漏洞 - `ImWebSocketEndpoint.java:573-598` - 非生产模式下允许从payload直接获取userId，应强制要求token验证
- [ ] [HIGH][安全] 添加SSRF防护 - `ImLinkPreviewController.java:38-50` - 为URL预览功能添加白名单验证和内网IP过滤
- [ ] [HIGH][性能] 优化未读DING计数查询 - `ImDingMessageController.java:177-187` - 使用数据库聚合查询替代内存遍历
- [ ] [HIGH][安全] 移除密码相关日志 - `ImUserServiceImpl.java:91-95` - 删除登录时记录密码长度的日志
- [ ] [HIGH][健壮] 添加批量删除验证 - `ImFileAdminController.java:66-70` - 验证输入列表非空且ID有效
- [ ] [HIGH][健壮] 添加路径变量验证 - `ImDingMessageController.java`, `ImScheduledMessageController.java` - 为ID路径变量添加@Positive注解
- [ ] [HIGH][性能] 导出功能添加分页限制 - `ImUserServiceImpl.java:764-765` - 使用流式导出或分批处理大数据量
- [ ] [HIGH][正确性] 修复用户在线状态 - `ImUserServiceImpl.java:263` - 检查实际在线状态而非硬编码true

---

## 🟡 中优先级待办 (MEDIUM)

- [ ] [MEDIUM][健壮] 记录批量操作失败 - `ImDingMessageController.java:139-143` - batchReadDing应记录失败日志
- [ ] [MEDIUM][安全] 添加限流保护 - `ImDingMessageController.java`, `ImScheduledMessageController.java` - 为发送和创建操作添加@RateLimit
- [ ] [MEDIUM][安全] 隐藏内部错误信息 - `ImMessageSyncController.java:59` - 返回通用错误消息，避免暴露内部实现
- [ ] [MEDIUM][前端] 修复潜在内存泄漏 - `useChatSend.js:50-54` - 使用onUnmounted清理setTimeout
- [ ] [MEDIUM][安全] WebSocket消息速率限制 - `ImWebSocketEndpoint.java` - 添加消息处理速率限制防止DoS
- [ ] [MEDIUM][代码] 统一URL映射 - `ImScheduledMessageController.java:62-63` - 移除重复的URL路径映射
- [ ] [MEDIUM][并发] 优化并发策略 - `ImWebSocketEndpoint.java:53-60` - 统一使用ConcurrentHashMap或synchronized

---

## 🟢 低优先级待办 (LOW)

- [ ] [LOW][配置] 提取硬编码配置 - 多处 - 将超时时间、重试次数等提取到配置文件
- [ ] [LOW][文档] 补充API文档 - 部分Controller方法 - 添加@Operation注解描述
- [ ] [LOW][文档] 添加代码注释 - 多个Service实现类 - 为复杂业务逻辑添加说明
- [ ] [LOW][代码] 消除魔法数字 - `useChatSend.js:30`, `ImUserServiceImpl.java:764` - 定义常量替代硬编码数值

---

## 建议处理顺序

1. **立即处理：** 安全相关的高优先级问题（认证绕过、SSRF、日志敏感信息）
2. **近期处理：** 性能和健壮性问题
3. **低优先级：** 代码质量改进

---

## 相关文件清单

**后端文件：**
- `ruoyi-im-api/src/main/java/com/ruoyi/im/websocket/ImWebSocketEndpoint.java`
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImLinkPreviewController.java`
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImDingMessageController.java`
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImScheduledMessageController.java`
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImMessageSyncController.java`
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/admin/ImFileAdminController.java`
- `ruoyi-im-api/src/main/java/com/ruoyi/im/service/impl/ImUserServiceImpl.java`

**前端文件：**
- `ruoyi-im-web/src/composables/useChat/useChatSend.js`
