# 34 WebSocket协议规范

> **用途**：定义即时通讯、在线状态、系统通知的实时推送协议。
> **基础路径**：`ws://{server}/ws/im`

---

## 1. 协议基础

### 1.1 数据帧格式 (JSON)
```json
{
  "type": "MESSAGE",
  "action": "SEND",
  "requestId": "uuid",
  "data": {},
  "timestamp": 1710921600000
}
```

### 1.2 核心消息类型 (`type`)
- `AUTH`: 鉴权与心跳
- `MESSAGE`: IM 聊天消息
- `NOTIFY`: 业务通知 (红点、提醒)
- `EVENT`: 状态事件 (输入中、在线状态)
- `CALL`: 音视频通话指令

---

## 2. IM 聊天消息 (`MESSAGE`)

### 2.1 序列号机制 (Sequence ID - 核心卓越项)
为确保内网环境下不丢消息、不重消息，必须引入 **`seq`**：
- **定义**：每个会话 (`conversationId`) 拥有一个从 1 开始、单调连续递增的序列号。
- **作用**：客户端收到消息后，检查 `seq` 是否等于 `localLastSeq + 1`。
    - 若 `seq > localLastSeq + 1`，说明产生 **"消息空隙 (Gap)"**，立即触发 `SYNC_REQ`。
    - 若 `seq <= localLastSeq`，说明是 **"重复消息"**，直接丢弃。

### 2.2 发送与接收流程 (Reliability Cycle)
1. **上行 (SEND)**：客户端发送消息，必须带上 `requestId` (UUID)。
2. **服务端确认 (SEND_ACK)**：服务端入库成功后，立即返回 ACK。
```json
{
  "type": "MESSAGE",
  "action": "SEND_ACK",
  "requestId": "uuid-from-client",
  "data": {
    "msgId": "12345",
    "seq": 105,
    "timestamp": 1710921600000
  }
}
```
3. **下行推送 (RECEIVE)**：接收方收到的推送包必须包含 `seq`：
```json
{
  "type": "MESSAGE",
  "action": "RECEIVE",
  "data": {
    "msgId": "12345",
    "seq": 105,
    "conversationId": "789",
    "content": "...",
    "senderId": "101"
  }
}
```

4. **送达确认 (DELIVERED_ACK)**：消息成功推送到接收方后，服务端通知发送方消息已送达。
```json
{
  "type": "MESSAGE",
  "action": "DELIVERED_ACK",
  "data": {
    "msgId": "12345",
    "deliveredTime": 1710921600000
  }
}
```

> **消息状态与 WS 帧对应关系**：
> - `SEND_ACK`：对应 `SENT` 状态（消息已存储）
> - `DELIVERED_ACK`：对应 `DELIVERED` 状态（消息已推送到接收方）
> - `READ_RECEIPT`：对应已读状态

---

## 3. 消息撤回与编辑 (Update Logic)
撤回与编辑同样占位一个 **`seq`**，以确保所有变更动作在时间线上是可追溯且连续的。

### 3.1 审批提醒 (APPROVAL)
- **场景**：当用户有新的审批待处理时推送。
- **Payload**:
```javascript
{
  "type": "NOTIFY",
  "action": "APPROVAL_PENDING",
  "data": {
    "instanceId": "123",
    "title": "张三的请假申请",
    "applicant": "张三",
    "createTime": "2026-03-20 10:00:00"
  }
}
```

### 3.2 待办提醒 (TASK)
- **场景**：待办任务即将到期或被指派时推送。
- **Payload**:
```json
{
  "type": "NOTIFY",
  "action": "TASK_REMIND",
  "data": {
    "taskId": "456",
    "content": "提交周报",
    "dueTime": "2026-03-20 18:00:00"
  }
}
```

---

## 4. 状态与事件 (`EVENT`)

### 4.1 输入中状态 (TYPING)
- **场景**：对方正在输入时，上方标题显示“正在输入...”。
- **Payload**:
```json
{
  "type": "EVENT",
  "action": "TYPING",
  "data": {
    "conversationId": "789",
    "senderId": "101"
  }
}
```

### 4.2 在线状态变更 (PRESENCE)
- **Payload**:
```json
{
  "type": "EVENT",
  "action": "PRESENCE_CHANGE",
  "data": {
    "userId": "101",
    "status": "ONLINE|BUSY|OFFLINE"
  }
}
```

---

## 5. 离线消息与同步逻辑 (Reliability)

### 5.1 重连握手 (Sync Handshake)
当 WebSocket 重连成功后，客户端必须立即发送 `SYNC_REQ`：
```json
{
  "type": "MESSAGE",
  "action": "SYNC_REQ",
  "data": {
    "lastKnownMsgId": "1234567890",
    "conversationId": "all"
  }
}
```
### 5.2 服务端推送 (Sync Response) 与解耦总线
- **分页拉取**：服务端根据 `lastKnownMsgId` 检索数据库中更大的 ID，通过 `SYNC_PUSH` 批量推送。前后端开发时必须使用 **"时间窗口+限定拉取条数"**（如每次拉取最前方的几十条）实施流式分页补偿，防止断网且长期失效重连造成的内存与带宽爆表。
- **业务总线解耦 (EventBus)**：由于重连/发送动作触发极快且频繁，所有推向客户端的 WS 下行广播不可直接挂接进落库的同步线程。Java 后端应该使用 Spring `ApplicationEvent` 监听 `MessageSendEvent` 等进行剥离式异步分发。

---

## 6. 心跳与熔断 (Keep-Alive)
- **心跳间隔**：30s 客户端 PING，服务端 5s 内必须回 PONG。
- **自动断开**：连续 3 次 PING 无响应，服务端必须强制清理该 Session 的 Redis 状态。

---

## 7. 通话信令 (`CALL`)

### 7.1 发起通话 (INVITE)
- **场景**：用户发起语音/视频通话
- **Payload**:
```json
{
  "type": "CALL",
  "action": "INVITE",
  "data": {
    "callId": "uuid",
    "callerId": 1,
    "callerName": "张三",
    "callerAvatar": "https://...",
    "targetId": 2,
    "targetType": "USER|GROUP",
    "callType": "VOICE|VIDEO",
    "createTime": "2026-03-20 10:00:00"
  }
}
```

### 7.2 响铃中 (RINGING)
- **场景**：被叫方收到呼叫邀请
- **Payload**:
```json
{
  "type": "CALL",
  "action": "RINGING",
  "data": {
    "callId": "uuid"
  }
}
```

### 7.3 接听通话 (ANSWER)
- **场景**：被叫方接听
- **Payload**:
```json
{
  "type": "CALL",
  "action": "ANSWER",
  "data": {
    "callId": "uuid",
    "answer": true
  }
}
```

### 7.4 拒绝通话 (REJECT)
- **场景**：被叫方拒绝
- **Payload**:
```json
{
  "type": "CALL",
  "action": "REJECT",
  "data": {
    "callId": "uuid",
    "reason": "BUSY|DECLINED"
  }
}
```

### 7.5 结束通话 (END)
- **场景**：任一方挂断
- **Payload**:
```json
{
  "type": "CALL",
  "action": "END",
  "data": {
    "callId": "uuid",
    "enderId": 1,
    "duration": 120
  }
}
```

### 7.6 ICE 候选 (ICE_CANDIDATE)
- **场景**：WebRTC ICE 候选交换
- **Payload**:
```json
{
  "type": "CALL",
  "action": "ICE_CANDIDATE",
  "data": {
    "callId": "uuid",
    "candidate": {
      "candidate": "...",
      "sdpMid": "...",
      "sdpMLineIndex": 0
    }
  }
}
```

### 7.7 SDP 会话描述 (SDP)
- **场景**：WebRTC SDP 交换
- **Payload**:
```json
{
  "type": "CALL",
  "action": "OFFER|ANSWER",
  "data": {
    "callId": "uuid",
    "sdp": "v=0\r\n..."
  }
}
```

---

## 8. 消息已读回执 (`MESSAGE`)

### 8.1 已读回执 (READ_RECEIPT)
- **场景**：消息被阅读后推送
- **Payload**:
```json
{
  "type": "MESSAGE",
  "action": "READ_RECEIPT",
  "data": {
    "conversationId": 1,
    "messageId": 100,
    "readerId": 2,
    "readerName": "李四",
    "readTime": "2026-03-20 10:00:00"
  }
}
```

### 8.2 批量已读 (READ_BATCH)
... (保持原有内容)

### 8.3 多端已读同步 (SELF_READ_SYNC)
- **场景**：用户在设备 A 阅读了消息，系统需通知该用户的设备 B、C 同步消除未读。
- **Payload**:
```json
{
  "type": "MESSAGE",
  "action": "SELF_READ_SYNC",
  "data": {
    "conversationId": 1,
    "lastReadMessageId": 100,
    "lastReadTime": 1710921600000
  }
}
```

---

## 9. 消息编辑通知 (`MESSAGE`)

### 9.1 消息编辑 (EDIT)
- **场景**：消息被编辑后通知
- **Payload**:
```json
{
  "type": "MESSAGE",
  "action": "EDIT",
  "data": {
    "msgId": 100,
    "conversationId": 1,
    "content": "编辑后的内容",
    "editorId": 1,
    "editTime": "2026-03-20 10:00:00"
  }
}
```

---

## 10. 群组事件 (`EVENT`)

### 10.1 群成员变更 (GROUP_MEMBER_CHANGE)
- **场景**：群成员加入/退出/被移除
- **Payload**:
```json
{
  "type": "EVENT",
  "action": "GROUP_MEMBER_CHANGE",
  "data": {
    "groupId": 1,
    "changeType": "JOIN|LEAVE|REMOVE",
    "memberId": 2,
    "memberName": "李四",
    "operatorId": 1,
    "operatorName": "张三"
  }
}
```

### 10.2 群信息变更 (GROUP_INFO_CHANGE)
- **场景**：群名称/公告/设置变更
- **Payload**:
```json
{
  "type": "EVENT",
  "action": "GROUP_INFO_CHANGE",
  "data": {
    "groupId": 1,
    "changeField": "NAME|ANNOUNCEMENT|AVATAR",
    "oldValue": "旧群名",
    "newValue": "新群名",
    "operatorId": 1
  }
}
```

### 10.3 群解散 (GROUP_DISMISS)
- **场景**：群被解散
- **Payload**:
```json
{
  "type": "EVENT",
  "action": "GROUP_DISMISS",
  "data": {
    "groupId": 1,
    "groupName": "技术交流群",
    "dismisserId": 1
  }
}
```

---

## 11. 文件上传进度 (`EVENT`)

### 11.1 上传进度 (UPLOAD_PROGRESS)
- **场景**：大文件分片上传进度通知
- **Payload**:
```json
{
  "type": "EVENT",
  "action": "UPLOAD_PROGRESS",
  "data": {
    "uploadId": "uuid",
    "fileName": "video.mp4",
    "totalSize": 104857600,
    "uploadedSize": 52428800,
    "progress": 50,
    "status": "UPLOADING|COMPLETED|FAILED"
  }
}
```

---

## 12. 完整消息类型汇总

### 12.1 消息类型 (`type`)
| 类型 | 说明 | 子动作 |
|-----|------|-------|
| AUTH | 鉴权与心跳 | PING, PONG |
| MESSAGE | IM 消息 | SEND, RECEIVE, REVOKE, EDIT, READ_RECEIPT, READ_BATCH |
| NOTIFY | 业务通知 | APPROVAL_PENDING, TASK_REMIND, SYSTEM_NOTICE |
| EVENT | 状态事件 | TYPING, PRESENCE_CHANGE, GROUP_MEMBER_CHANGE, GROUP_INFO_CHANGE, GROUP_DISMISS, UPLOAD_PROGRESS |
| CALL | 通话信令 | INVITE, RINGING, ANSWER, REJECT, END, ICE_CANDIDATE, OFFER, ANSWER |

### 12.2 消息状态码
| 状态码 | 说明 |
|-------|------|
| 0 | 成功 |
| 1001 | 未授权 |
| 1002 | Token 过期 |
| 2001 | 消息发送失败 |
| 3001 | 通话已结束 |
| 3002 | 对方忙线中 |
| 3003 | 对方无响应 |

---

## 13. 变更记录

| 日期 | 变更内容 |
|-----|---------|
| 2026-03-20 | 补充通话信令、已读回执、群组事件、文件上传进度 |
| 2026-03-20 | 初始版本创建 |
