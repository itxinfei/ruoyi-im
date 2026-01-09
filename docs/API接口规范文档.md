# IM系统API接口规范文档

> 文档版本: v1.0
> 创建日期: 2026-01-09
> 适用范围: RuoYi-IM 前后端开发

---

## 一、命名规范

### 1.1 核心概念统一

| 概念 | 中文名称 | 变量名 | 数据库字段 | 说明 |
|------|----------|--------|------------|------|
| 会话 | 会话/对话 | `conversationId` | `conversation_id` | **统一使用conversation，禁止使用session** |
| 消息 | 消息 | `messageId` | `id` | 消息主键 |
| 用户 | 用户 | `userId` | `user_id` / `sender_id` | 用户ID |
| 群组 | 群组 | `groupId` | `group_id` | 群组ID |

### 1.2 命名约定

- **前端**: 统一使用 `conversationId`，废弃 `sessionId`
- **后端**: 统一使用 `conversationId`，API参数、DTO、VO全部一致
- **WebSocket**: 同时兼容 `conversationId` 和 `sessionId`（向前兼容）

---

## 二、RESTful API规范

### 2.1 通用响应格式

```typescript
// 成功响应
interface Result<T> {
  code: number      // 状态码，200表示成功
  msg: string       // 响应消息
  data: T           // 响应数据
}

// 分页响应
interface PageResult<T> {
  code: number
  msg: string
  rows: T[]         // 数据列表
  total: number     // 总记录数
}

// 错误响应
interface ErrorResult {
  code: number      // 错误码
  msg: string       // 错误信息
  data: null
}
```

### 2.2 通用请求头

```
Authorization: Bearer {jwt_token}
Content-Type: application/json
userId: {user_id}  // 开发环境使用，生产环境从Token解析
```

### 2.3 状态码规范

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 三、会话(Conversation) API

### 3.1 获取会话列表

**请求**
```
GET /api/im/conversation/list
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "type": "PRIVATE",           // PRIVATE=单聊, GROUP=群聊
      "name": "张三",
      "avatar": "http://...",
      "unreadCount": 5,
      "lastMessage": {
        "id": 123,
        "content": "你好",
        "senderId": 2,
        "senderName": "张三",
        "messageType": "TEXT",
        "createTime": "2026-01-09 10:00:00"
      },
      "pinned": false,              // 是否置顶
      "muted": false,               // 是否免打扰
      "createTime": "2026-01-01 10:00:00",
      "updateTime": "2026-01-09 10:00:00"
    }
  ]
}
```

### 3.2 获取会话详情

**请求**
```
GET /api/im/conversation/{id}
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "type": "PRIVATE",
    "name": "张三",
    "avatar": "http://...",
    "memberCount": 2,
    "members": [...],
    "pinned": false,
    "muted": false
  }
}
```

### 3.3 创建会话

**请求**
```
POST /api/im/conversation/create
Content-Type: application/json

{
  "type": "PRIVATE",              // PRIVATE 或 GROUP
  "name": "会话名称",              // 群聊必填
  "memberIds": [2, 3, 4],         // 成员ID列表（群聊）
  "peerUserId": 2                 // 对方用户ID（私聊）
}
```

**响应**
```json
{
  "code": 200,
  "msg": "创建成功",
  "data": 1                       // 新会话ID
}
```

### 3.4 置顶/取消置顶

**请求**
```
PUT /api/im/conversation/{id}/pinned?pinned=true
```

**响应**
```json
{
  "code": 200,
  "msg": "置顶成功"
}
```

### 3.5 免打扰/取消免打扰

**请求**
```
PUT /api/im/conversation/{id}/muted?muted=true
```

**响应**
```json
{
  "code": 200,
  "msg": "设置成功"
}
```

### 3.6 删除会话

**请求**
```
DELETE /api/im/conversation/{id}
```

**响应**
```json
{
  "code": 200,
  "msg": "删除成功"
}
```

### 3.7 标记会话已读

**请求**
```
PUT /api/im/conversation/{id}/markAsRead
```

**响应**
```json
{
  "code": 200,
  "msg": "标记成功"
}
```

### 3.8 获取未读消息总数

**请求**
```
GET /api/im/conversation/unreadCount
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": 10
}
```

---

## 四、消息(Message) API

### 4.1 发送消息

**请求**
```
POST /api/im/message/send
Content-Type: application/json

{
  "conversationId": 1,            // 会话ID（必填）
  "type": "TEXT",                 // 消息类型（必填）
  "content": "消息内容",           // 消息内容（必填）
  "replyToMessageId": 123,        // 回复的消息ID（可选）
  "clientMsgId": "temp_xxx",      // 客户端消息ID（可选，用于去重）
  "receiverId": 2,                // 接收者ID（私聊且会话ID为空时使用）
  "mentionInfo": {                // @提及信息（可选）
    "userIds": [2, 3],            // 被@的用户ID列表
    "mentionAll": false           // 是否@所有人
  }
}
```

**消息类型枚举**
```typescript
type MessageType =
  | "TEXT"       // 文本消息
  | "IMAGE"      // 图片消息
  | "FILE"       // 文件消息
  | "VOICE"      // 语音消息
  | "VIDEO"      // 视频消息
  | "LOCATION"   // 位置消息
  | "CARD"       // 名片消息
```

**响应**
```json
{
  "code": 200,
  "msg": "发送成功",
  "data": 123                      // 消息ID
}
```

### 4.2 获取消息列表

**请求**
```
GET /api/im/message/list/{conversationId}?lastId=100&limit=20
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conversationId | Long | 是 | 会话ID（路径参数） |
| lastId | Long | 否 | 上一页最后一条消息ID，用于分页 |
| limit | Integer | 否 | 每页数量，默认20 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 123,
      "conversationId": 1,
      "senderId": 2,
      "senderName": "张三",
      "senderAvatar": "http://...",
      "messageType": "TEXT",
      "content": "你好",
      "isSelf": false,              // 是否为当前用户发送
      "isRevoked": false,           // 是否已撤回
      "isEdited": false,            // 是否已编辑
      "replyToMessageId": null,     // 回复的消息ID
      "quotedMessage": null,        // 被回复的消息内容
      "createTime": "2026-01-09 10:00:00",
      "editTime": null,             // 编辑时间
      "revokedTime": null           // 撤回时间
    }
  ]
}
```

### 4.3 撤回消息

**请求**
```
DELETE /api/im/message/{messageId}/recall
```

**限制**
- 只能撤回自己发送的消息
- 消息发送时间不超过2分钟

**响应**
```json
{
  "code": 200,
  "msg": "消息已撤回"
}
```

### 4.4 编辑消息

**请求**
```
PUT /api/im/message/{messageId}/edit
Content-Type: application/json

{
  "newContent": "修改后的内容"
}
```

**限制**
- 只能编辑自己发送的消息
- 只能编辑文本消息
- 消息发送时间不超过15分钟

**响应**
```json
{
  "code": 200,
  "msg": "消息已编辑"
}
```

### 4.5 转发消息

**请求**
```
POST /api/im/message/forward
Content-Type: application/json

{
  "messageId": 123,                // 原消息ID
  "toConversationId": 5,           // 目标会话ID
  "toUserId": 2,                   // 目标用户ID（私聊使用）
  "content": "转发附言"             // 转发附言（可选）
}
```

**响应**
```json
{
  "code": 200,
  "msg": "转发成功",
  "data": 124                      // 新消息ID
}
```

### 4.6 回复消息

**请求**
```
POST /api/im/message/reply
Content-Type: application/json

{
  "messageId": 123,                // 被回复的消息ID
  "content": "回复内容"
}
```

**响应**
```json
{
  "code": 200,
  "msg": "回复成功",
  "data": 124                      // 新消息ID
}
```

### 4.7 标记消息已读

**请求**
```
PUT /api/im/message/read
Content-Type: application/json

{
  "conversationId": 1,
  "messageIds": [123, 124, 125]    // 消息ID列表
}
```

**响应**
```json
{
  "code": 200,
  "msg": "已标记为已读"
}
```

### 4.8 搜索消息

**请求**
```
POST /api/im/message/search
Content-Type: application/json

{
  "keyword": "搜索关键词",
  "conversationId": 1,             // 限定会话（可选）
  "messageType": "TEXT",           // 消息类型（可选）
  "senderId": 2,                   // 发送者ID（可选）
  "startTime": "2026-01-01",       // 开始时间（可选）
  "endTime": "2026-01-09",         // 结束时间（可选）
  "pageNum": 1,
  "pageSize": 20,
  "includeRevoked": false,         // 是否包含已撤回消息
  "exactMatch": false              // 是否精确匹配
}
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 20,
    "totalPages": 3,
    "items": [...]
  }
}
```

### 4.9 删除消息

**请求**
```
DELETE /api/im/message/{messageId}
```

**响应**
```json
{
  "code": 200,
  "msg": "删除成功"
}
```

### 4.10 获取未读消息数量

**请求**
```
GET /api/im/message/unread/count
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": 10
}
```

---

## 五、WebSocket消息规范

### 5.1 连接地址

```
ws://localhost:8080/ws/im?token={jwt_token}
```

### 5.2 消息格式

**通用消息结构**
```typescript
interface WsMessage {
  type: string                    // 消息类型
  payload?: any                   // 消息数据
  timestamp?: number              // 时间戳
}
```

### 5.3 客户端发送的消息类型

#### 5.3.1 发送聊天消息

```typescript
{
  type: "message",
  payload: {
    conversationId: 1,            // 会话ID（必填）
    messageType: "TEXT",          // 消息类型
    content: "消息内容",
    replyToMessageId: 123,        // 可选
    clientMsgId: "temp_xxx"       // 可选
  }
}
```

#### 5.3.2 正在输入状态

```typescript
{
  type: "typing",
  payload: {
    conversationId: 1,
    isTyping: true
  }
}
```

#### 5.3.3 心跳

```typescript
{
  type: "ping"
}
```

#### 5.3.4 消息已读

```typescript
{
  type: "read",
  payload: {
    conversationId: 1,
    messageIds: [123, 124]
  }
}
```

### 5.4 服务端推送的消息类型

#### 5.4.1 连接成功

```typescript
{
  type: "connected",
  userId: 1,
  data: {
    connected: true
  },
  timestamp: 1234567890
}
```

#### 5.4.2 新消息

```typescript
{
  type: "new_message",
  payload: {
    id: 123,
    conversationId: 1,
    senderId: 2,
    senderName: "张三",
    senderAvatar: "http://...",
    messageType: "TEXT",
    content: "你好",
    replyToMessageId: null,
    quotedMessage: null,
    createTime: "2026-01-09 10:00:00"
  }
}
```

#### 5.4.3 消息撤回

```typescript
{
  type: "message_recalled",
  payload: {
    messageId: 123,
    conversationId: 1
  }
}
```

#### 5.4.4 消息已读回执

```typescript
{
  type: "read_receipt",
  payload: {
    conversationId: 1,
    userId: 2,
    messageIds: [123, 124]
  },
  timestamp: 1234567890
}
```

#### 5.4.5 正在输入状态

```typescript
{
  type: "typing",
  conversationId: 1,
  userId: 2,
  isTyping: true
}
```

#### 5.4.6 用户上线/离线

```typescript
{
  type: "online",
  userId: 2,
  data: true,                     // true=上线, false=离线
  timestamp: 1234567890
}
```

#### 5.4.7 心跳响应

```typescript
{
  type: "pong",
  timestamp: 1234567890
}
```

---

## 六、数据模型规范

### 6.1 会话(Conversation)

```typescript
interface Conversation {
  id: number                      // 会话ID
  type: 'PRIVATE' | 'GROUP'       // 会话类型
  name: string                    // 会话名称
  avatar: string                  // 会话头像
  unreadCount: number             // 未读消息数
  pinned: boolean                 // 是否置顶
  muted: boolean                  // 是否免打扰
  createTime: string              // 创建时间
  updateTime: string              // 更新时间
}
```

### 6.2 消息(Message)

```typescript
interface Message {
  id: number                      // 消息ID
  conversationId: number          // 会话ID
  senderId: number                // 发送者ID
  senderName: string              // 发送者名称
  senderAvatar: string            // 发送者头像
  messageType: MessageType        // 消息类型
  content: string                 // 消息内容
  isSelf: boolean                 // 是否为自己发送
  isRevoked: boolean              // 是否已撤回
  isEdited: boolean               // 是否已编辑
  replyToMessageId?: number       // 回复的消息ID
  quotedMessage?: QuotedMessage   // 被回复的消息
  createTime: string              // 创建时间
  editTime?: string               // 编辑时间
  revokedTime?: string            // 撤回时间
}
```

### 6.3 引用消息(QuotedMessage)

```typescript
interface QuotedMessage {
  id: number
  type: string
  content: string
  senderName: string
  sendTime: string
  isFile: boolean
}
```

---

## 七、错误码规范

| 错误码 | 说明 |
|--------|------|
| 10001 | 会话不存在 |
| 10002 | 用户不在会话中 |
| 10003 | 消息不存在 |
| 10004 | 无权操作该消息 |
| 10005 | 消息撤回超时 |
| 10006 | 消息编辑超时 |
| 10007 | 发送者不存在 |
| 10008 | 接收者不存在 |
| 10009 | 会话ID和接收者ID不能同时为空 |
| 10010 | 消息内容不能为空 |

---

## 八、前端API调用规范

### 8.1 API模块组织

```
src/api/im/
├── index.js           # 统一导出
├── conversation.js    # 会话相关API
├── message.js         # 消息相关API
├── group.js           # 群组相关API
├── contact.js         # 联系人相关API
└── file.js            # 文件相关API
```

### 8.2 API函数命名规范

```javascript
// 查询列表: list{Resource}
export function listConversations(params)

// 查询详情: get{Resource}
export function getConversation(id)

// 创建: create{Resource}
export function createConversation(data)

// 更新: update{Resource}
export function updateConversation(id, data)

// 删除: delete{Resource}
export function deleteConversation(id)

// 操作: {action}{Resource}
export function sendMessage(data)
export function recallMessage(id)
```

---

## 九、版本控制

- 本文档版本: **v1.0**
- API版本: **v1**
- WebSocket协议版本: **v1**

**变更记录**:
| 版本 | 日期 | 变更内容 |
|------|------|----------|
| v1.0 | 2026-01-09 | 初始版本，定义核心API规范 |

---

**文档维护**: 后端负责人 + 前端负责人
**更新流程**: API变更 → 更新文档 → 双方确认 → 代码实现
