# Bug 修复记录

## 2026-01-09

### 文档更新
**更新内容：**
- 修复 `需求与代码差距分析报告.md` 中的重大错误
- 更新 `项目进度.md`，补充完整的Controller清单（30个）
- 更新数据库表统计（52张核心业务表）
- 修正各模块完成度：整体从55%提升到80%

**主要修正：**
1. **DING消息模块** - 从"0%完成"修正为"90%完成"
   - 已有完整的数据表：im_ding_message, im_ding_receipt, im_ding_template
   - 已有完整的Controller：ImDingMessageController（10个API接口）

2. **消息收藏功能** - 从"完全缺失"修正为"已完整实现"
   - 已有数据表：im_message_favorite
   - 已有Controller：ImMessageFavoriteController（7个API接口）

3. **日程管理** - 从"完全缺失"修正为"已完整实现"
   - 已有数据表：im_schedule_event, im_schedule_participant, im_schedule_reminder
   - 已有Controller：ImScheduleEventController（9个API接口）

4. **工作报告** - 从"完全缺失"修正为"已完整实现"
   - 已有数据表：im_work_report, im_work_report_comment, im_work_report_like
   - 已有Controller：ImWorkReportController（13个API接口）

5. **消息编辑** - 从"完全缺失"修正为"已完整实现"
   - 已有数据表：im_message_edit_history

6. **外部联系人** - 从"需要补充"修正为"已完整实现"
   - 已有数据表：im_external_contact, im_external_contact_group
   - 已有Controller：ImExternalContactController

**修改文件：**
- `docs/项目进度.md`
- `docs/需求与代码差距分析报告.md`

---

## 2026-01-08

### 1. ImFriendMapper.xml 列名不匹配

**问题描述：**
```
Error querying database. Cause: java.sql.SQLSyntaxErrorException: Unknown column 'friend_user_id' in 'field list'
```

**原因：**
- Mapper XML 使用了 `friend_user_id` 列名
- 实际数据库表 `im_friend` 的列名是 `friend_id`
- 实体类 `ImFriend` 正确使用了 `@TableField("friend_id")`

**修复内容：**
- `ImFriendMapper.xml` 中所有 `friend_user_id` → `friend_id`
- 移除了 `selectImFriendVo` 中的 `status` 字段（status 在实体类中标记为 `@TableField(exist = false)`，非数据库字段）
- 更新了 insert/update 语句中的列名

**修改文件：**
- `ruoyi-im-api/src/main/resources/mapper/ImFriendMapper.xml`

---

### 2. 前端参数名不匹配导致 undefined 错误

**问题描述：**
```
NumberFormatException: For input string: "undefined" - /api/im/message/list/undefined
```

**原因：**
- 后端 API: `GET /api/im/message/list/{conversationId}`
- 前端 store 调用 `listMessage({ sessionId, ... })`
- 前端 API 函数期望 `params.conversationId`
- 导致 `conversationId` 为 `undefined`，字符串 "undefined" 被传到后端

**修复内容：**
- `store/modules/im.js` 的 `loadMessages` action 中：
  ```javascript
  // 修复前
  const response = await listMessage({ sessionId, page, pageSize })

  // 修复后
  const response = await listMessage({ conversationId: sessionId, page, pageSize })
  ```

**修改文件：**
- `ruoyi-im-web/src/store/modules/im.js`

**注意：**
- 前端使用 `sessionId` (会话ID) 概念
- 后端使用 `conversationId` (对话ID) 概念
- 两者指向同一实体，需要在调用 API 时进行参数名映射

---

### 3. ImTodoItemMapper.xml 字段不匹配

**问题描述：**
```
GET /api/im/workbench/overview 返回 500 错误
GET /api/im/workbench/todos 返回 500 错误
```

**原因：**
- Mapper XML 使用了 `is_completed` 字段
- Mapper XML 引用了不存在的 `type` 和 `related_id` 字段
- 实际数据库表使用 `status` 字段 (PENDING/IN_PROGRESS/COMPLETED/CANCELLED)

**修复内容：**
- resultMap 中 `is_completed` → `status`
- selectImTodoItemVo 中移除 `type` 和 `related_id`
- 查询条件 `is_completed = false` → `status = 'PENDING'`
- 排序条件移除 `is_completed`
- insert/update 语句移除 `type` 和 `related_id`
- markAsCompleted 语句更新为 `status = 'COMPLETED'`

**修改文件：**
- `ruoyi-im-api/src/main/resources/mapper/ImTodoItemMapper.xml`

---

### 4. ImGroupServiceImpl 使用非数据库字段

**问题描述：**
```
GET /api/im/group/list 返回 500 错误
```

**原因：**
- `ImGroup` 实体的 `status` 字段标记为 `@TableField(exist = false)`
- 服务代码尝试检查 `group.getStatus() == "NORMAL"`，该字段始终为 null
- 实际的状态字段是 `is_deleted` (0=正常, 1=已删除)

**修复内容：**
- `getUserGroups()` 方法：`"NORMAL".equals(group.getStatus())` → `group.getIsDeleted() == 0`
- `dismissGroup()` 方法：
  - `group.setStatus("DISMISSED")` → `group.setIsDeleted(1)`
  - 添加 `group.setDeletedTime(LocalDateTime.now())`

**修改文件：**
- `ruoyi-im-api/src/main/java/com/ruoyi/im/service/impl/ImGroupServiceImpl.java`

---

### 5. ImMessageServiceImpl 使用非数据库字段

**问题描述：**
```
POST /api/im/message/send 返回 500 错误
```

**原因：**
- `ImMessage` 实体存在以下非数据库字段（`@TableField(exist = false)`）：
  - `type` (实际数据库字段是 `message_type`)
  - `status`
  - `sendTime` (实际数据库字段是 `create_time`)
  - `revokeTime` (实际数据库字段是 `revoked_time`)
  - `parentId`
- 服务代码错误地使用 `setType()` 而非 `setMessageType()`，导致消息类型未被持久化
- `setStatus()`, `setSendTime()`, `setRevokeTime()` 设置的字段不会保存到数据库

**修复内容：**
- `sendMessage()` 方法：
  - `message.setType(...)` → `message.setMessageType(...)`
  - 移除 `message.setStatus(1)`
  - 移除 `message.setSendTime(...)`
- `recallMessage()` 方法：
  - `message.getSendTime()` → `message.getCreateTime()`
  - `message.setRevokeTime(...)` → `message.setRevokedTime(...)`
  - 移除 `message.setStatus(5)`
- `markAsRead()` 方法：移除 `message.setStatus(3)`
- `forwardMessage()` 方法：
  - `message.setType("forward")` → `message.setMessageType(originalMessage.getMessageType())`
  - `message.setParentId(...)` → `message.setForwardFromMessageId(...)`
  - 移除 `message.setStatus(2)`, `message.setSendTime(...)`
- `replyMessage()` 方法：
  - `message.setType("reply")` → `message.setMessageType("TEXT")`
  - 移除 `message.setParentId(...)`, `message.setStatus(3)`, `message.setSendTime(...)`
- `buildQuotedMessage()` 方法：
  - `originalMessage.getType()` → `originalMessage.getMessageType()`
  - `originalMessage.getSendTime()` → `originalMessage.getCreateTime()`
- `searchMessages()` 方法：`message.getType()` → `message.getMessageType()`

**修改文件：**
- `ruoyi-im-api/src/main/java/com/ruoyi/im/service/impl/ImMessageServiceImpl.java`

---

### 6. WebMvcConfig 静态资源配置

**问题描述：**
```
GET http://localhost:8080/avatar/1.jpg 返回 404 错误
```

**原因：**
- 缺少静态资源映射配置
- Spring Security 虽然允许 `/avatar/**` 访问，但没有 URL 到文件系统的映射

**修复内容：**
- 创建 `WebMvcConfig.java` 配置类
- 添加 `/avatar/**` → `classpath:/static/avatar/` 映射
- 添加 `/uploads/**` → `file:uploads/` 映射
- 添加 `/profile/**` → `classpath:/static/profile/` 映射

**修改文件：**
- `ruoyi-im-api/src/main/java/com/ruoyi/im/config/WebMvcConfig.java` (新建)

---

### 7. ImMessageSendRequest 缺少 replyToMessageId 字段

**问题描述：**
```
POST http://localhost:8080/api/im/message/send 返回 400 错误
```

**原因：**
- 前端发送 `replyToMessageId` 和 `clientMsgId` 字段
- 后端 DTO `ImMessageSendRequest` 没有这些字段
- 虽然未知字段通常会被忽略，但可能导致反序列化问题

**修复内容：**
- `ImMessageSendRequest.java` 添加 `replyToMessageId` 字段（Long类型）
- `ImMessageSendRequest.java` 添加 `clientMsgId` 字段（String类型）
- `ImMessageServiceImpl.sendMessage()` 设置 `message.setReplyToMessageId(request.getReplyToMessageId())`

**修改文件：**
- `ruoyi-im-api/src/main/java/com/ruoyi/im/dto/message/ImMessageSendRequest.java`
- `ruoyi-im-api/src/main/java/com/ruoyi/im/service/impl/ImMessageServiceImpl.java`

---

### 8. WebSocket 重连消息频繁提示

**问题描述：**
```
连接断开，正在重新连接... 提示太频繁，很烦
```

**原因：**
- 每次连接断开后重连时都会显示提示
- `reconnectAttempts` 在连接成功后重置为0，导致每次新的重连周期都会显示消息

**修复内容：**
- 添加 `hasShownReconnectMessage` 标记，整个会话期间只显示一次重连提示
- 将 `if (this.reconnectAttempts === 1)` 改为 `if (!this.hasShownReconnectMessage)`

**修改文件：**
- `ruoyi-im-web/src/utils/websocket/imWebSocket.js`

---

## 待修复问题

### 1. 登录接口返回 500 (用户不存在)
- **状态**: 非Bug，正常业务逻辑
- **说明**: 当用户不存在时，服务正确返回 500 状态码，错误信息为 "用户不存在 [USER_NOT_EXIST]"
- **建议**: 可优化为返回 400 或 404 状态码，但当前实现符合业务逻辑

### 2. 注册接口返回 500
- **状态**: 待排查
- **说明**: 注册用户时返回 500 错误，需要进一步排查具体原因

---

## 需要重新编译的项目

修改了 Mapper XML 文件和 Java 源文件，需要重新编译：

```bash
cd D:\MyCode\im\ruoyi-im-api
mvn clean compile
```

然后重启应用服务器。

---

## 优化方案

### 1. 代码优化建议

#### 1.1 用户ID获取方式优化
**问题：**
当前代码中大量使用 `@RequestHeader(value = "userId", required = false) Long userId` 并设置默认值：
```java
if (userId == null) {
    userId = 1L;
}
```

**建议：**
1. 统一使用Spring Security或JWT拦截器从Token中解析用户ID
2. 创建一个 `@CurrentUserId` 注解简化代码
3. 移除硬编码的默认值（1L、2L等）

#### 1.2 异常处理统一化
**问题：**
当前异常处理分散在各个Controller中

**建议：**
1. 创建全局异常处理器 `@ControllerAdvice`
2. 统一异常响应格式
3. 定义业务异常枚举

#### 1.3 日志记录优化
**建议：**
1. 使用AOP记录所有API调用日志
2. 敏感操作（登录、删除、修改权限）增加详细日志
3. 使用日志级别（DEBUG/INFO/WARN/ERROR）合理分类

### 2. 性能优化建议

#### 2.1 数据库索引优化
**建议添加的索引：**
```sql
-- 消息表查询优化
CREATE INDEX idx_msg_conversation_time ON im_message(conversation_id, create_time DESC);
CREATE INDEX idx_msg_sender ON im_message(sender_id);

-- 会话成员未读数查询
CREATE INDEX idx_conv_member_user_unread ON im_conversation_member(user_id, unread_count);

-- DING消息回执查询
CREATE INDEX idx_ding_receipt_user ON im_ding_receipt(ding_id, receiver_id);
```

#### 2.2 Redis缓存应用
**建议缓存的数据：**
1. 用户基本信息（User ID -> User Info）
2. 用户在线状态
3. 群组成员列表
4. 系统配置项
5. 敏感词列表

#### 2.3 分页查询优化
**建议：**
1. 使用MyBatis-Plus分页插件统一分页
2. 大数据量查询使用游标分页而非offset分页

### 3. 安全优化建议

#### 3.1 接口鉴权优化
**建议：**
1. 统一使用JWT Token鉴权
2. 敏感接口添加权限注解验证
3. 接口限流防刷

#### 3.2 敏感信息加密
**建议：**
1. 用户手机号、邮箱加密存储
2. 聊天记录可选择性加密
3. 文件上传增加病毒扫描

### 4. 前端优化建议

#### 4.1 状态管理优化
**建议：**
1. 使用Pinia替代Vuex（Vue3推荐）
2. 优化WebSocket消息分发机制
3. 添加消息队列处理高并发消息

#### 4.2 UI交互优化
**建议：**
1. 实现左侧导航栏图标化（68px宽）
2. 添加消息右键菜单
3. 完善消息输入框工具栏
4. 添加语音消息录制组件

### 5. 部署优化建议

#### 5.1 配置外部化
**建议：**
1. 使用Nacos或Apollo配置中心
2. 敏感配置（数据库密码）使用Jasypt加密
3. 环境隔离（dev/test/prod）

#### 5.2 监控告警
**建议：**
1. 集成Prometheus + Grafana监控
2. 添加健康检查接口 `/actuator/health`
3. 关键业务指标告警

### 6. 待实现功能清单

| 优先级 | 功能 | 说明 |
|--------|------|------|
| P0 | 在线文档 | 集成OnlyOffice或金山文档 |
| P0 | 语音消息 | 前端录音组件+后端存储 |
| P1 | 邮箱模块 | IMAP/SMTP集成 |
| P1 | DING额度 | 企业DING消息额度管理 |
| P1 | 白板协作 | 在线白板功能 |
| P2 | 视频会议 | WebRTC集成 |
| P2 | 机器人对接 | 聊天机器人接入 |
