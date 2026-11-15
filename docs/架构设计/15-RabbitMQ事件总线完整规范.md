# 15 RabbitMQ 事件总线完整规范

> **定位**：定义所有微服务间异步事件的 Exchange、routing key、消息结构、消费者和幂等策略。大模型生成 RabbitMQ 配置和消费者代码时直接参照。
> **更新时间**：2026-10-30

---

## 一、Exchange 与 Queue 命名规范

| Exchange | 类型 | 用途 | 绑定 Queue |
|----------|------|------|-----------|
| `im.msg` | topic | 消息相关事件 | `im.msg.store` / `im.msg.push` / `im.msg.notify` |
| `im.event` | fanout | 在线状态广播 | `im.event.presence` / `im.event.group` |
| `im.delay` | x-delayed-message | 延迟任务 (提醒/回执超时) | `im.delay.reminder` / `im.delay.call-timeout` |
| `im.search` | topic | 搜索索引更新 | `im.search.message` / `im.search.doc` |
| `im.notify` | topic | 推送通知 | `im.notify.push` / `im.notify.email` / `im.notify.sms` |
| `im.file` | topic | 文件处理 | `im.file.thumbnail` / `im.file.transcode` |
| `im.dlx` | direct | 死信队列 | `im.dlq.all` |

---

## 二、事件目录 (按服务分组)

### 2.1 消息事件 (im.msg)

#### `message.created`

| 项 | 值 |
|----|-----|
| **Exchange** | `im.msg` |
| **Routing Key** | `message.created` |
| **消费方** | `im.msg.store` / `im.msg.push` |
| **重试策略** | 3 次, 指数退避 (1s/2s/4s) |
| **DLQ** | `im.dlq.all` |
| **幂等键** | `messageId` |

```json
{
  "eventId": "1780006000001",
  "eventType": "message.created",
  "timestamp": 1730000000000,
  "sourceService": "im-message",
  "data": {
    "messageId": "1780001000040",
    "sessionId": "1780000000001",
    "senderId": 111,
    "senderName": "张三",
    "type": "TEXT",
    "content": "明天下午3点开会",
    "serverSeq": 40,
    "clientSeq": 12345,
    "clientMsgId": "client-uuid-xx-001",
    "mentionIds": [112, 113],
    "replyTo": null,
    "createdAt": "2026-10-30T14:30:00.000"
  }
}
```

**消费者行为:**
- `im.msg.store`: 异步写入分库分表 (二期)、更新会话摘要、更新 Redis 缓存
- `im.msg.push`: 查询接收者是否在线 → 通过 WS 推送 / 写离线表

#### `message.edited`

| 项 | 值 |
|----|-----|
| **Routing Key** | `message.edited` |
| **消费方** | `im.msg.notify` / `im.search.message` |
| **幂等键** | `messageId` + `editVersion` |

```json
{
  "eventId": "1780006000002",
  "eventType": "message.edited",
  "timestamp": 1730000000100,
  "sourceService": "im-message",
  "data": {
    "messageId": "1780001000040",
    "sessionId": "1780000000001",
    "editorId": 111,
    "oldContent": "明天下午3点开会",
    "newContent": "明天下午3点开会 (地点: 会议室A301)",
    "editVersion": 1,
    "editedAt": "2026-10-30T14:31:00.000"
  }
}
```

#### `message.recalled`

```json
{
  "eventType": "message.recalled",
  "data": {
    "messageId": "1780001000040",
    "sessionId": "1780000000001",
    "recallerId": 111,
    "originalContent": "明天下午3点开会",
    "recalledAt": "2026-10-30T14:31:30.000"
  }
}
```

#### `message.reaction_updated`

```json
{
  "eventType": "message.reaction_updated",
  "data": {
    "messageId": "1780001000040",
    "sessionId": "1780000000001",
    "emoji": "👍",
    "action": "ADD",
    "userId": 112,
    "totalCount": 4
  }
}
```

#### `message.read_receipt`

```json
{
  "eventType": "message.read_receipt",
  "data": {
    "sessionId": "1780000000001",
    "userId": 112,
    "lastReadSeq": 40,
    "lastReadMsgId": "1780001000040",
    "readAt": "2026-10-30T14:35:00.000"
  }
}
```

#### `message.batch_read`

```json
{
  "eventType": "message.batch_read",
  "data": {
    "sessionId": "1780000000001",
    "readerId": 112,
    "messageIds": ["1780001000038", "1780001000039", "1780001000040"],
    "lastReadSeq": 40,
    "readAt": "2026-10-30T14:36:00.000"
  }
}
```

---

### 2.2 会话事件 (im.msg 路由)

#### `session.created`

```json
{
  "eventType": "session.created",
  "data": {
    "sessionId": "1780000000099",
    "type": "P2P",
    "targetId": 120,
    "creatorId": 111,
    "createTime": "2026-10-30T15:00:00",
    "firstMessage": "你好, 关于明天的会议"
  }
}
```

#### `session.last_msg_updated`

| 项 | 值 |
|----|-----|
| **用途** | 更新 Redis 最近消息缓存、搜索索引 |
| **消费方** | `im.msg.store` / `im.search.message` |

```json
{
  "eventType": "session.last_msg_updated",
  "data": {
    "sessionId": "1780000000001",
    "lastMessageId": "1780001000040",
    "lastMessageType": "TEXT",
    "lastMessageContent": "明天下午3点开会",
    "lastMessageSender": 111,
    "lastMessageTime": "2026-10-30T14:30:00",
    "activeTime": "2026-10-30T14:30:00"
  }
}
```

#### `session.pin_toggled`

```json
{
  "eventType": "session.pin_toggled",
  "data": {
    "sessionId": "1780000000001",
    "userId": 111,
    "pinned": true,
    "pinnedAt": "2026-10-30T14:40:00"
  }
}
```

#### `session.mute_toggled`

```json
{
  "eventType": "session.mute_toggled",
  "data": {
    "sessionId": "1780000000001",
    "userId": 111,
    "muted": false,
    "mutedUntil": null
  }
}
```

---

### 2.3 群组事件 (im.event)

#### `group.created`

| 项 | 值 |
|----|-----|
| **Exchange** | `im.event` (fanout) |
| **消费方** | `im.event.group` / `im.notify.push` |

```json
{
  "eventType": "group.created",
  "data": {
    "groupId": 2005,
    "name": "AI 研究小组",
    "ownerId": 111,
    "memberIds": [111, 112, 113, 114, 115],
    "avatar": "http://minio:9000/im-group/2005/icon.jpg",
    "createTime": "2026-10-30T15:10:00"
  }
}
```

#### `group.member_joined`

```json
{
  "eventType": "group.member_joined",
  "data": {
    "groupId": 2001,
    "userId": 118,
    "inviterId": 111,
    "joinType": "INVITED",
    "joinTime": "2026-10-30T15:12:00",
    "memberCount": 16
  }
}
```

#### `group.member_left`

```json
{
  "eventType": "group.member_left",
  "data": {
    "groupId": 2001,
    "userId": 115,
    "leaveType": "EXITED",
    "operatorId": 115,
    "leftAt": "2026-10-30T15:15:00",
    "memberCount": 14
  }
}
```

#### `group.member_kicked`

```json
{
  "eventType": "group.member_kicked",
  "data": {
    "groupId": 2001,
    "userId": 116,
    "operatorId": 111,
    "reason": "违规内容",
    "kickedAt": "2026-10-30T15:18:00",
    "memberCount": 13
  }
}
```

#### `group.owner_transferred`

```json
{
  "eventType": "group.owner_transferred",
  "data": {
    "groupId": 2001,
    "oldOwnerId": 111,
    "newOwnerId": 112,
    "transferredAt": "2026-10-30T15:20:00"
  }
}
```

#### `group.dismissed`

```json
{
  "eventType": "group.dismissed",
  "data": {
    "groupId": 2001,
    "dismissedBy": 112,
    "memberCountAtDismiss": 13,
    "dismissedAt": "2026-10-30T15:25:00"
  }
}
```

#### `group.profile_updated`

```json
{
  "eventType": "group.profile_updated",
  "data": {
    "groupId": 2001,
    "updatedFields": ["name", "avatar", "description"],
    "oldValues": { "name": "开发二群" },
    "newValues": { "name": "开发二群 V2" },
    "operatorId": 111,
    "updatedAt": "2026-10-30T15:22:00"
  }
}
```

#### `group.announcement_published`

```json
{
  "eventType": "group.announcement_published",
  "data": {
    "groupId": 2001,
    "announcementId": 5001,
    "title": "本周工作安排",
    "content": "...",
    "publisherId": 111,
    "pinned": true,
    "mentionAll": false,
    "publishedAt": "2026-10-30T15:30:00"
  }
}
```

---

### 2.4 在线状态事件 (im.event)

#### `user.presence_changed`

| 项 | 值 |
|----|-----|
| **Exchange** | `im.event` (fanout) |
| **消费方** | `im.event.presence` |
| **QOS** | At most once (可丢失, 心跳会覆盖) |

```json
{
  "eventType": "user.presence_changed",
  "data": {
    "userId": 112,
    "oldStatus": "ONLINE",
    "newStatus": "OFFLINE",
    "reason": "WS_DISCONNECT",
    "changedAt": "2026-10-30T15:40:00",
    "deviceInfo": {
      "web": "OFFLINE",
      "desktop": "ONLINE"
    }
  }
}
```

#### `user.typing`

| 项 | 值 |
|----|-----|
| **用途** | 通过 WS 转发 (MQ 不持久化) |

```json
{
  "eventType": "user.typing",
  "data": {
    "sessionId": "1780000000001",
    "userId": 112,
    "isTyping": true,
    "timestamp": 1730000000000
  }
}
```

---

### 2.5 通话事件 (im.event → im-real-time)

#### `call.invite`

```json
{
  "eventType": "call.invite",
  "data": {
    "callId": "1780003000010",
    "roomName": "call_1780003000010",
    "type": "VIDEO",
    "initiatorId": 111,
    "sessionId": "1780000000001",
    "participantIds": [112, 113],
    "invitedAt": "2026-10-30T16:00:00"
  }
}
```

#### `call.accepted`

```json
{
  "eventType": "call.accepted",
  "data": {
    "callId": "1780003000010",
    "userId": 112,
    "acceptedAt": "2026-10-30T16:01:00"
  }
}
```

#### `call.rejected`

```json
{
  "eventType": "call.rejected",
  "data": {
    "callId": "1780003000010",
    "userId": 113,
    "reason": "BUSY",
    "rejectedAt": "2026-10-30T16:02:00"
  }
}
```

#### `call.ended`

```json
{
  "eventType": "call.ended",
  "data": {
    "callId": "1780003000010",
    "endedBy": 111,
    "reason": "HANGUP",
    "duration": 1865,
    "endedAt": "2026-10-30T16:31:05",
    "participants": [
      { "userId": 111, "duration": 1865 },
      { "userId": 112, "duration": 1710 }
    ]
  }
}
```

---

### 2.6 文件处理事件 (im.file)

#### `file.uploaded`

| 项 | 值 |
|----|-----|
| **Exchange** | `im.file` |
| **消费方** | `im.file.thumbnail` / `im.file.transcode` |

```json
{
  "eventType": "file.uploaded",
  "data": {
    "fileId": "1780002000005",
    "bucket": "im-chat-image",
    "objectKey": "1780002000005.png",
    "fileName": "whiteboard-photo.png",
    "fileSize": 1567890,
    "mimeType": "image/png",
    "uploaderId": 111,
    "sessionId": "1780000000001",
    "uploadTime": "2026-10-30T14:35:00"
  }
}
```

#### `file.thumbnail_generated`

```json
{
  "eventType": "file.thumbnail_generated",
  "data": {
    "fileId": "1780002000005",
    "thumbnailKey": "thumb/1780002000005.jpg",
    "width": 360,
    "height": 202,
    "generatedAt": "2026-10-30T14:35:03"
  }
}
```

#### `file.virus_scanned`

```json
{
  "eventType": "file.virus_scanned",
  "data": {
    "fileId": "1780002000006",
    "scanResult": "CLEAN",
    "engine": "ClamAV 1.3.1",
    "scanTime": "2026-10-30T14:36:00"
  }
}
```

---

### 2.7 日历/审批事件 (im.notify)

#### `calendar.reminder_due`

| 项 | 值 |
|----|-----|
| **Exchange** | `im.delay` |
| **TTL** | 根据用户设定 (15min/30min/1h) |

```json
{
  "eventType": "calendar.reminder_due",
  "data": {
    "eventId": 5001,
    "userId": 111,
    "title": "产品评审会",
    "startTime": "2026-11-01T14:00:00",
    "reminderType": "NOTIFICATION",
    "triggeredAt": "2026-11-01T13:45:00"
  }
}
```

#### `approval.status_changed`

```json
{
  "eventType": "approval.status_changed",
  "data": {
    "auditId": "1780004000001",
    "applicantId": 111,
    "auditorId": 112,
    "action": "APPROVE",
    "result": "APPROVED",
    "comment": "同意",
    "auditedAt": "2026-10-30T16:00:00",
    "nextAuditorId": null
  }
}
```

#### `approval.passed_level`

```json
{
  "eventType": "approval.passed_level",
  "data": {
    "auditId": "1780004000001",
    "templateName": "请假申请",
    "passedLevel": 1,
    "nextLevel": 2,
    "nextAuditorId": 100,
    "notifiedAt": "2026-10-30T16:02:00"
  }
}
```

---

## 三、Spring AMQP 配置

```java
@Configuration
public class RabbitConfig {

    // ========== Exchange ==========
    @Bean
    public TopicExchange msgExchange() {
        return new TopicExchange("im.msg", true, false);
    }

    @Bean
    public FanoutExchange eventExchange() {
        return new FanoutExchange("im.event", true, false);
    }

    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "topic");
        return new CustomExchange("im.delay", "x-delayed-message", true, false, args);
    }

    @Bean
    public TopicExchange searchExchange() {
        return new TopicExchange("im.search", true, false);
    }

    @Bean
    public TopicExchange notifyExchange() {
        return new TopicExchange("im.notify", true, false);
    }

    @Bean
    public TopicExchange fileExchange() {
        return new TopicExchange("im.file", true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange("im.dlx", true, false);
    }

    // ========== Queue ==========
    @Bean
    public Queue msgStoreQueue() {
        return QueueBuilder.durable("im.msg.store")
            .withArgument("x-dead-letter-exchange", "im.dlx")
            .withArgument("x-dead-letter-routing-key", "im.dlq.all")
            .build();
    }

    @Bean
    public Queue msgPushQueue() {
        return QueueBuilder.durable("im.msg.push")
            .withArgument("x-dead-letter-exchange", "im.dlx")
            .withArgument("x-dead-letter-routing-key", "im.dlq.all")
            .build();
    }

    @Bean
    public Queue presenceQueue() {
        return QueueBuilder.durable("im.event.presence")
            .withArgument("x-message-ttl", 60000)  // 60s 过期, 不要积压
            .build();
    }

    @Bean
    public Queue groupEventQueue() {
        return QueueBuilder.durable("im.event.group")
            .withArgument("x-dead-letter-exchange", "im.dlx")
            .build();
    }

    @Bean
    public Queue searchMessageQueue() {
        return QueueBuilder.durable("im.search.message")
            .build();
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable("im.dlq.all").build();
    }

    // ========== Binding ==========
    @Bean
    public Binding msgStoreBinding() {
        return BindingBuilder.bind(msgStoreQueue()).to(msgExchange()).with("message.*");
    }

    @Bean
    public Binding msgPushBinding() {
        return BindingBuilder.bind(msgPushQueue()).to(msgExchange()).with("message.*");
    }

    @Bean
    public Binding lastMsgBinding() {
        return BindingBuilder.bind(msgStoreQueue()).to(msgExchange()).with("session.last_msg_updated");
    }

    @Bean
    public Binding presenceBinding() {
        return BindingBuilder.bind(presenceQueue()).to(eventExchange());
    }

    @Bean
    public Binding groupEventBinding() {
        return BindingBuilder.bind(groupEventQueue()).to(eventExchange());
    }

    @Bean
    public Binding searchMessageBinding() {
        return BindingBuilder.bind(searchMessageQueue()).to(searchExchange()).with("message.*");
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue()).to(dlxExchange()).with("im.dlq.all");
    }

    // ========== 延迟消息插件配置 ==========
    @RabbitListener(queues = "im.delay.reminder")
    public void onReminderDue(CalendarReminderEvent event, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            // 发送提醒通知
            notificationService.sendReminder(event);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            channel.basicNack(tag, false, true);
        }
    }

    // ========== 死信队列监控 (告警) ==========
    @RabbitListener(queues = "im.dlq.all")
    public void onDeadLetter(Object message, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        // 记录告警, 通知开发
        alertService.fire("MQ 死信消费", message.toString());
        channel.basicAck(tag, false);
    }
}
```

---

## 四、延迟消息发送示例

```java
// 日历提醒 —— 在事件开始前 15 分钟触发
@Service
public class CalendarReminderSender {

    @Autowired private RabbitTemplate rabbitTemplate;

    public void scheduleReminder(Long eventId, LocalDateTime eventTime, List<Integer> reminderMinutes) {
        for (int minutes : reminderMinutes) {
            long delayMs = Duration.between(LocalDateTime.now(), eventTime).toMinutes() - minutes;
            if (delayMs <= 0) continue;  // 已过, 不发

            rabbitTemplate.convertAndSend("im.delay", "calendar.reminder_due",
                new CalendarReminderEvent(eventId, minutes),
                msg -> {
                    msg.getMessageProperties().setDelay((int) (delayMs * 60 * 1000));
                    msg.getMessageProperties().setHeader("x-delay", (int) (delayMs * 60 * 1000));
                    return msg;
                });
        }
    }
}
```

---

## 五、幂等消费通用抽象

```java
// 幂等消费者基类
public abstract class IdempotentConsumer<T> {

    private StringRedisTemplate redis;

    private static final String IDEMPOTENT_KEY_PREFIX = "mq:idempotent:";
    private static final Duration IDEMPOTENT_TTL = Duration.ofHours(24);

    public void consume(T event, Channel channel, Message message) throws IOException {
        String idempotentKey = IDEMPOTENT_KEY_PREFIX + extractEventId(event);

        Boolean isNew = redis.opsForValue().setIfAbsent(idempotentKey, "1", IDEMPOTENT_TTL);
        if (isNew == null || !isNew) {
            // 重复消费, 直接 ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        try {
            handle(event);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (RetryableException e) {
            // 可重试: 退回队列
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        } catch (Exception e) {
            // 不可重试: 进 DLQ
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    protected abstract String extractEventId(T event);
    protected abstract void handle(T event);
}
```

---

## 六、变更记录

| 日期 | 变更 |
|------|------|
| 2026-10-30 | 完成全部 RabbitMQ 事件规范: 7 个 Exchange / 30+ 事件类型 / 含完整 JSON schema / 延迟消息示例 / 幂等消费模板 |
