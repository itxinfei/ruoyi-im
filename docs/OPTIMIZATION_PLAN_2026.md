# 优化方案说明书 (Optimization Plan)

**日期**: 2026-02-10  
**目标**: 提升系统性能（RT 降低 30%，吞吐量提升 50%）与可扩展性

## 1. 核心性能优化策略

### 1.1 消息发送流程异步化 (Asynchronous Processing)
**问题**: 当前发送群消息时，同步执行了“保存消息”、“更新会话”、“循环更新成员未读数”、“推送 WebSocket”、“记录审计日志”等操作。
**方案**:
引入消息队列 (MQ)，将非核心路径异步化。
- **同步路径**: 
    1. 鉴权 & 参数校验
    2. 保存消息到 DB (`im_message`)
    3. 返回成功响应给前端
- **异步路径 (MQ 消费者)**:
    1. 更新会话最后一条消息 (`im_conversation`)
    2. 批量更新群成员未读数 (Batch Update)
    3. WebSocket 广播推送
    4. 写入审计日志
    5. 触发机器人/回调

### 1.2 数据库优化 (Database Optimization)
**问题**: `im_message` 表随时间推移将变得巨大。
**方案**:
1.  **冷热分离**: 将 3 个月前的历史消息归档到历史表 (`im_message_history`) 或 NoSQL (MongoDB/HBase)。
2.  **分表策略**: 按 `conversation_id` 模值分表，确保同一会话的消息在同一张表，便于查询上下文。
3.  **索引优化**: 确保 `(conversation_id, create_time)` 联合索引存在，加速历史消息拉取。

### 1.3 缓存策略 (Caching Strategy)
**方案**:
1.  **用户信息缓存**: `ImUser` 对象高频读取，必须缓存 (TTL 1小时)。
2.  **群成员列表缓存**: `ImGroupMember` 列表在发送消息时频繁读取，建议缓存 `List<Long> memberIds` (Redis Set/List)。
3.  **多级缓存**: 本地缓存 (Caffeine) + 分布式缓存 (Redis) 存储热点配置。

## 2. 架构重构策略

### 2.1 解决 "God Class" 问题
**目标**: 拆分 `ImMessageServiceImpl`。
**方案**:
- **Handler 模式**: 引入 `MessageHandler` 接口。
    - `TextMessageHandler`
    - `ImageMessageHandler`
    - `FileMessageHandler`
- **Service 拆分**:
    - `MessageSenderService`: 专注消息发送流程。
    - `MessageQueryService`: 专注消息列表查询。
    - `MessageActionService`: 专注撤回、回复、反应。

### 2.2 废弃冗余代码
**动作**:
- 标记 `ImSessionController` 为 `@Deprecated`。
- 将 `ImSessionController` 独有的接口迁移至 `ImConversationController` (如果存在)。

## 3. 安全加固策略

1.  **限流升级**: 当前使用 `@RateLimit` (Redis实现)。建议在网关层 (Nginx/Gateway) 增加 IP 级别的限流，防止 DDoS。
2.  **内容安全**: 集成第三方内容安全 API (阿里云/腾讯云)，对图片和文本进行自动审核（涉黄、涉政）。

## 4. 实施路线图

1.  **Phase 1 (本周)**: 废弃冗余代码，增加索引，代码层面小幅重构。
2.  **Phase 2 (下周)**: 引入 MQ，实现消息发送异步化。
3.  **Phase 3 (下月)**: 实施分库分表与冷热分离。
