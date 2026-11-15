# 25 WebSocket 消息类型完整定义

> **规范对齐**: 11 种消息类型与 `08-IM 核心需求明细.md §2.1` 完全一致
> `TEXT` / `IMAGE` / `FILE` / `VOICE` / `VIDEO` / `LINK` / `LOCATION` / `GEOTAG` / `DING` / `SYSTEM` / `RECALL`

> **定位**：WsFrame 协议中所有 `type` × `action` 组合 + payload schema。
> **前置**：`06-WebSocket 实时协议.md`、`23-共享子模块架构设计.md`
> **承诺**：前端 WS 处理只围绕本文档的 type/action 枚举扩展，**不新增未登记的类型**。

---

## 一、WsFrame 帧结构

```jsonc
{
  "type":    "MESSAGE | EVENT | CALL | NOTIFY | AUTH | ACK | PING | PONG",
  "action":  "具体动作枚举",
  "requestId": "UUID v4",        // 请求-响应关联
  "data":    { /* payload */ },
  "timestamp": 1730000000000     // 毫秒时间戳（服务端发出时间）
}
```

### 1.1 type 枚举

| type | 说明 | 发送方 |
|------|------|--------|
| `AUTH` | 认证与连接管理 | 双向 |
| `MESSAGE` | 消息收发双向 | 双向 |
| `EVENT` | 事件（typing / presence / online / read receipt）| 双向 |
| `CALL` | 音视频通话信令 | 双向 |
| `NOTIFY` | 通知推送（DING / 系统通知 / 机器人）| 服务端 → 客户端 |
| `ACK` | 确认帧 | 双向 |
| `PING` / `PONG` | 心跳 | 双向 |

### 1.2 action 枚举（完整）

| action | type | 方向 | 说明 |
|--------|------|------|------|
| `AUTH_LOGIN` | AUTH | C→S | 携带 Token 认证 |
| `AUTH_RESPONSE` | AUTH | S→C | 认证结果（成功/失败+错误码）|
| `AUTH_LOGOUT` | AUTH | C→S | 主动断开 |
| `MSG_SEND` | MESSAGE | C→S | 发送消息 |
| `MSG_SEND_ACK` | MESSAGE | S→C | 服务端确认已收到（分配 msgId）|
| `MSG_RECEIVE` | MESSAGE | S→C | 新消息到达 |
| `MSG_DELIVERED` | MESSAGE | S→C | 消息已送达对方 |
| `MSG_READ` | MESSAGE | S→C | 对方已读 |
| `MSG_RECALL` | MESSAGE | C→S → S→C | 撤回消息 |
| `MSG_RECALL_ACK` | MESSAGE | S→C | 撤回结果 |
| `MSG_EDIT` | MESSAGE | C→S → S→C | 编辑消息 |
| `MSG_EDIT_ACK` | MESSAGE | S→C | 编辑结果 |
| `MSG_DELETE` | MESSAGE | C→S → S→C | 删除消息 |
| `MSG_REACTION` | MESSAGE | C→S → S→C | 消息表情回复 |
| `EVENT_TYPING` | EVENT | C→S → S→C | 正在输入 |
| `EVENT_PRESENCE` | EVENT | S→C | 在线状态变更 |
| `EVENT_ONLINE` | EVENT | S→C | 用户上线 |
| `EVENT_OFFLINE` | EVENT | S→C | 用户下线 |
| `EVENT_READ_RECEIPT` | EVENT | S→C | 已读回执 |
| `EVENT_SESSION_UPDATE` | EVENT | S→C | 会话列表变更（置顶/免打扰/未读）|
`EVENT_CONTACT_CHANGE` | EVENT | S→C | 联系人变更 |
| `EVENT_GROUP_CHANGE` | EVENT | S→C | 群组变更（成员/群名/群公告）|
| `CALL_INVITE` | CALL | C→S → S→C | 发起通话邀请 |
| `CALL_ACCEPT` | CALL | C→S → S→C | 接受邀请 |
| `CALL_REJECT` | CALL | C→S → S→C | 拒绝邀请 |
| `CALL_END` | CALL | C→S → S→C | 挂断 |
| `CALL_CANCEL` | CALL | C→S → S→C | 取消呼叫 |
| `CALL_TIMEOUT` | CALL | S→C | 对方未接听超时 |
| `CALL_SDP` | CALL | 双向 | SDP Offer/Answer |
| `CALL_ICE` | CALL | 双向 | ICE Candidate |
| `CALL_ERROR` | CALL | S→C | 通话错误 |
| `GROUP_CALL_INVITE` | CALL | C→S → S→C | 发起群会议 |
| `GROUP_CALL_JOIN` | CALL | C→S → S→C | 加入群会议 |
| `GROUP_CALL_LEAVE` | CALL | C→S → S→C | 离开群会议 |
| `GROUP_CALL_END` | CALL | S→C | 群会议结束 |
| `NOTIFY_DING` | NOTIFY | S→C | DING 强提醒 |
| `NOTIFY_SYSTEM` | NOTIFY | S→C | 系统通知 |
| `NOTIFY_BOT` | NOTIFY | S→C | 机器人推送 |
| `NOTIFY_RED_DOT` | NOTIFY | S→C | 红点/角标通知 |
| `PING` | PING | C→S | 客户端心跳 |
| `PONG` | PONG | S→C | 服务端心跳回应 |

---

## 二、Payload Schema 逐动作定义

### 2.1 AUTH 类

#### `AUTH_LOGIN`（C→S）

```typescript
{
  token: string              // JWT Token（Authorization: Bearer xxx 之外的 WS 认证方式）
  deviceId: string           // 设备唯一标识（UUID）
  deviceType: 'desktop' | 'mobile' | 'web'
  platform: 'win32' | 'darwin' | 'linux' | 'ios' | 'android'
  appVersion: string         // 客户端版本号
}
```

#### `AUTH_RESPONSE`（S→C）

```typescript
{
  success: boolean
  userId?: number
  errorCode?: string         // 'TOKEN_EXPIRED' | 'INVALID_TOKEN' | 'DEVICE_LIMIT'
  errorMsg?: string
  serverTime: number         // 服务端时间戳（用于时钟校准）
}
```

#### `AUTH_LOGOUT`（C→S）

```typescript
{
  reason: 'USER_LOGOUT' | 'SWITCH_DEVICE' | 'APP_CLOSE'
}
```

### 2.2 MESSAGE 类

#### `MSG_SEND`（C→S）

```typescript
{
  conversationId: number
  messageType: 'TEXT' | 'IMAGE' | 'FILE' | 'VOICE' | 'VIDEO' | 'LINK' | 'LOCATION' | 'GEOTAG' | 'DING' | 'SYSTEM' | 'RECALL'
  content: string            // TEXT=文本内容，其他=JSON序列化的文件元信息
  clientMsgId: string        // 客户端 UUID，幂等去重 key
  replyToMessageId?: number  // 回复哪条消息
  mentionType?: 'NONE' | 'ALL' | 'SPECIFIC'
  mentionUserIds?: number[]
  sensitiveLevel?: 'NORMAL' | 'CONFIDENTIAL'
}
```

#### `MSG_SEND_ACK`（S→C）

```typescript
{
  clientMsgId: string        // 回传客户端发送时的 UUID
  messageId: number          // 服务端分配的雪花 ID
  conversationId: number
  serverSequence: number     // 服务端会话内序号（用于排序）
  sendTime: string           // ISO 8601
  status: 'ACCEPTED' | 'REJECTED'
  errorCode?: string         // REJECTED 时的原因
}
```

#### `MSG_RECEIVE`（S→C）

```typescript
{
  messageId: number
  conversationId: number
  senderId: number
  messageType: 'TEXT' | 'IMAGE' | 'FILE' | 'VOICE' | 'VIDEO' | 'LINK' | 'LOCATION' | 'GEOTAG' | 'DING' | 'SYSTEM' | 'RECALL'
  content: string
  serverSequence: number
  sendTime: string           // ISO 8601
  replyToMessageId?: number
  mentionType?: 'NONE' | 'ALL' | 'SPECIFIC'
  mentionUserIds?: number[]
  sender: {                  // 内联用户摘要，避免客户端再查
    userId: number
    nickname: string
    avatar: string
  }
}
```

#### `MSG_RECALL` / `MSG_EDIT`（C→S → S→C）

```typescript
{
  messageId: number
  conversationId: number
  // MSG_EDIT 时额外字段：
  newContent?: string
}
```

#### `MSG_REACTION`（C→S → S→C）

```typescript
{
  messageId: number
  conversationId: number
  userId: number
  reactionType: 'LIKE' | 'LOVE' | 'LAUGH' | 'WOW' | 'SAD' | 'ANGRY' | 'THANK'
  isAdd: boolean             // true=添加反应, false=取消
}
```

### 2.3 EVENT 类

#### `EVENT_TYPING`（C→S → S→C）

```typescript
{
  conversationId: number
  userId: number
  typingType: 'TEXT' | 'VOICE'
  isTyping: boolean          // true=开始输入, false=停止/清空
}
```

#### `EVENT_PRESENCE`（S→C）

```typescript
{
  userId: number
  status: 'ONLINE' | 'OFFLINE' | 'AWAY'
  deviceSummary: Array<{
    deviceType: 'desktop' | 'mobile' | 'web'
    isOnline: boolean
  }>
  lastSeenTime?: string     // OFFLINE 时返回
}
```

#### `EVENT_READ_RECEIPT`（S→C）

```typescript
{
  conversationId: number
  userId: number             // 谁已读
  lastReadMessageId: number  // 读到哪条
  readTime: string
  readCount: number          // 群聊返回累计已读数
  totalMemberCount: number   // 群聊返回群总人数
}
```

### 2.4 CALL 类

#### `CALL_INVITE`（C→S → S→C）

```typescript
{
  callId: number             // 服务端生成的通话 ID
  callerId: number
  calleeId: number           // 单聊被叫者；群会议时为 0
  callType: 'AUDIO' | 'VIDEO'
  isGroupCall: boolean
  groupId?: number           // 群会议时必填
}
```

#### `CALL_SDP`（双向）

```typescript
{
  callId: number
  targetUserId: number       // 发给谁
  sdpType: 'offer' | 'answer'
  sdp: string                // JSON_stringified RTCSessionDescriptionInit
}
```

#### `CALL_ICE`（双向）

```typescript
{
  callId: number
  targetUserId: number
  candidate: string          // JSON_stringified RTCIceCandidateInit
  sdpMid?: string
  sdpMLineIndex?: number
}
```

### 2.5 NOTIFY 类

#### `NOTIFY_DING`（S→C）

```typescript
{
  dingId: number
  senderId: number
  senderName: string
  conversationId: number
  content: string
  priority: 'MEDIUM' | 'HIGH' | 'URGENT'
  createTime: string
}
```

#### `NOTIFY_RED_DOT`（S→C）

```typescript
{
  module: 'CHAT' | 'CONTACTS' | 'DING' | 'APPROVAL' | 'MAIL'
  count: number               // 该模块未读总数
}
```

---

## 三、前端处理模板

```typescript
// WS 事件处理标准模板（在 Vue 组件中使用）
const handleWsMessage = (frame: WsFrame) => {
  switch (frame.type) {
    case 'MESSAGE':
      switch (frame.action) {
        case 'MSG_RECEIVE':
          store.dispatch('im/message/receiveMessage', frame.data)
          break
        case 'MSG_SEND_ACK':
          store.dispatch('im/message/handleSendAck', frame.data)
          break
        case 'MSG_DELIVERED':
          store.dispatch('im/message/markDelivered', frame.data)
          break
      }
      break

    case 'EVENT':
      switch (frame.action) {
        case 'EVENT_TYPING':
          store.dispatch('im/session/handleTyping', frame.data)
          break
        case 'EVENT_PRESENCE':
          store.dispatch('im/session/handlePresence', frame.data)
          break
        case 'EVENT_READ_RECEIPT':
          store.dispatch('im/message/handleReadReceipt', frame.data)
          break
        case 'EVENT_SESSION_UPDATE':
          store.dispatch('im/session/refresh', frame.data)
          break
      }
      break

    case 'CALL':
      switch (frame.action) {
        case 'CALL_INVITE':
          store.dispatch('im/call/handleInvite', frame.data)
          break
        case 'CALL_SDP':
          store.dispatch('im/call/handleSdp', frame.data)
          break
        case 'CALL_ICE':
          store.dispatch('im/call/handleIce', frame.data)
          break
      }
      break

    case 'NOTIFY':
      switch (frame.action) {
        case 'NOTIFY_DING':
          store.dispatch('im/ding/receive', frame.data)
          // 桌面端同步触发系统通知
          if (window.electronAPI?.showNotification) {
            window.electronAPI.showNotification('DING', frame.data.content)
          }
          break
        case 'NOTIFY_RED_DOT':
          store.dispatch('im/session/updateRedDot', frame.data)
          // 同步托盘 badge
          if (window.electronAPI?.setBadgeCount) {
            window.electronAPI.setBadgeCount(frame.data.count)
          }
          break
      }
      break
  }
}
```

---

## 四、心跳与重连规则

| 规则 | 值 |
|------|-----|
| 客户端 PING 间隔 | 30s |
| 服务端 PONG 超时 | 收到 PING 必须 3s 内回复 PONG |
| 服务端主动断连判定 | 连续 3 次没收到 PING（90s）主动断开 + 清理状态 |
| 客户端重连策略 | 首次立即 → 等 2s → 等 4s → 等 8s → 等 16s → 等 30s（最大间隔）+ 随机 0-5s 抖动 |
| 重连成功后的动作 | AUTH_LOGIN → 同步离线消息（游标拉取）|
| 重连彻底失败判定 | 重连 5 次全部失败 → 触发 `reconnect-failed` 事件 |

---

## 五、消息可靠性机制（6 条强制）

| # | 机制 | 说明 |
|---|------|------|
| 1 | **服务端分配序号** | 客户端按 `serverSequence` 排序，**禁止**按 `createTime` 排序 |
| 2 | **游标分页** | `WHERE conversation_id=? AND server_sequence > ? ORDER BY server_sequence LIMIT 50`，禁 OFFSET |
| 3 | **缺口检测** | 收到的 `serverSequence` 不连续时主动补拉 |
| 4 | **应用层 ACK** | TCP 到达 ≠ 业务收到；服务端必须发 `MSG_SEND_ACK` |
| 5 | **抖动重连** | 重连间隔加随机 jitter（Slack 2021 教训）|
| 6 | **消息幂等** | 按 `clientMsgId` 去重 |

---

## 六、变更控制

| 操作 | 流程 |
|------|------|
| 新增 type | Owner → 修改本文档 + `23-共享子模块架构设计.md` 枚举 → 前端 + 后端同时更新 |
| 新增 action | Owner → 修改本文档 → 前端 switch 增加 case + 后端枚举新增 |
| 修改 payload 字段 | Owner → 标注版本号 → 双端同步 |
| 删除字段 | 标记 deprecated → 保留 2 个版本周期 → 彻底删除 → 修改本文档 |

---

## 七、变更记录

| 日期 | 变更 |
|------|------|
| 2026-10-28 | 输出完整 WS 消息类型定义：type 8 种、action 40+ 种、payload schema 20+、前端处理模板、心跳/重连规则、消息可靠性 6 条强制 |
