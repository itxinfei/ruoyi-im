# RuoYi-IM API 接口文档

## 概述

本文档描述 RuoYi-IM 系统的 REST API 接口规范。

### 基础信息

| 项目 | 值 |
|-----|---|
| 基础URL | `/api/im` |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| 认证方式 | JWT Token |

### 请求头

| 头部 | 类型 | 必填 | 说明 |
|-----|------|-----|-----|
| Authorization | String | 是 | JWT Token，格式：`Bearer {token}` |
| Content-Type | String | 是 | 固定值：`application/json` |

### 通用响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|-----|------|-----|
| code | Integer | 状态码，200表示成功 |
| msg | String | 响应消息 |
| data | Object | 响应数据 |

### 错误响应格式

```json
{
  "code": 500,
  "msg": "操作失败 [追踪ID: a1b2c3d4e5f6g7h8]",
  "data": null
}
```

---

## 消息模块

### 1. 发送消息

**接口**：`POST /api/im/message/send`

**请求参数**：

```json
{
  "conversationId": 123,
  "type": "TEXT",
  "content": "你好",
  "replyToMessageId": null,
  "clientMsgId": "20231215123456_abc123"
}
```

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|-----|
| conversationId | Long | 是 | 会话ID |
| type | String | 是 | 消息类型：TEXT/IMAGE/FILE/VOICE/VIDEO |
| content | String | 是 | 消息内容 |
| replyToMessageId | Long | 否 | 回复的消息ID |
| clientMsgId | String | 否 | 客户端消息ID（用于去重） |

**响应**：

```json
{
  "code": 200,
  "msg": "发送成功",
  "data": 456
}
```

### 2. 查询消息列表

**接口**：`GET /api/im/message/list`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|-----|
| conversationId | Long | 是 | 会话ID |
| lastId | Long | 否 | 最后一条消息ID（用于分页） |
| limit | Integer | 否 | 每页数量，默认20 |

**响应**：

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "id": 456,
      "conversationId": 123,
      "senderId": 1,
      "type": "text",
      "content": "你好",
      "createTime": "2023-12-15 12:34:56"
    }
  ],
  "total": 100
}
```

### 3. 撤回消息

**接口**：`DELETE /api/im/message/{messageId}/recall`

**路径参数**：

| 参数 | 类型 | 说明 |
|-----|------|-----|
| messageId | Long | 消息ID |

**响应**：

```json
{
  "code": 200,
  "msg": "撤回成功"
}
```

### 4. 搜索消息

**接口**：`GET /api/im/message/search`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|-----|
| keyword | String | 是 | 搜索关键词 |
| conversationId | Long | 否 | 会话ID（不传则搜索全部） |
| messageType | String | 否 | 消息类型过滤 |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认20 |

---

## 会话模块

### 1. 获取用户会话列表

**接口**：`GET /api/im/conversation/list`

**响应**：

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "id": 123,
      "type": "PRIVATE",
      "peerName": "张三",
      "peerAvatar": "/avatar/user1.png",
      "unreadCount": 5,
      "lastMessage": {
        "id": 456,
        "content": "你好",
        "createTime": "2023-12-15 12:34:56"
      },
      "isPinned": false,
      "isMuted": false
    }
  ]
}
```

### 2. 创建私聊会话

**接口**：`POST /api/im/conversation/private`

**请求参数**：

```json
{
  "peerUserId": 2
}
```

**响应**：

```json
{
  "code": 200,
  "msg": "创建成功",
  "data": 123
}
```

### 3. 创建群聊会话

**接口**：`POST /api/im/conversation/group`

**请求参数**：

```json
{
  "name": "技术交流群",
  "memberIds": [2, 3, 4]
}
```

### 4. 删除会话

**接口**：`DELETE /api/im/conversation/{conversationId}`

### 5. 置顶/取消置顶会话

**接口**：`PUT /api/im/conversation/{conversationId}/pin`

**请求参数**：

```json
{
  "pinned": true
}
```

---

## 好友模块

### 1. 获取好友列表

**接口**：`GET /api/im/contact/list`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|-----|
| groupId | String | 否 | 分组ID |
| keyword | String | 否 | 搜索关键词 |

**响应**：

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "id": 1,
      "friendId": 2,
      "nickname": "李四",
      "avatar": "/avatar/user2.png",
      "onlineStatus": 1,
      "groupName": "同事"
    }
  ]
}
```

### 2. 发送好友申请

**接口**：`POST /api/im/contact/request/send`

**请求参数**：

```json
{
  "userId": 2,
  "reason": "我是张三，想加你为好友"
}
```

### 3. 处理好友申请

**接口**：`POST /api/im/contact/request/{requestId}/handle`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|-----|
| approved | Boolean | 是 | 是否同意 |

### 4. 删除好友

**接口**：`DELETE /api/im/contact/{friendId}`

### 5. 更新好友备注

**接口**：`PUT /api/im/contact/{friendId}`

**请求参数**：

```json
{
  "remark": "备注名"
}
```

---

## 群组模块

### 1. 获取群组列表

**接口**：`GET /api/im/group/list`

### 2. 创建群组

**接口**：`POST /api/im/group/create`

**请求参数**：

```json
{
  "name": "技术交流群",
  "description": "技术讨论群组",
  "avatar": "/avatar/group1.png",
  "memberIds": [2, 3, 4]
}
```

### 3. 获取群组成员

**接口**：`GET /api/im/group/{groupId}/members`

### 4. 邀请成员入群

**接口**：`POST /api/im/group/{groupId}/members`

**请求参数**：

```json
{
  "memberIds": [5, 6, 7]
}
```

### 5. 移除群成员

**接口**：`DELETE /api/im/group/{groupId}/members/{userId}`

### 6. 退出群组

**接口**：`DELETE /api/im/group/{groupId}/leave`

---

## 用户模块

### 1. 获取用户信息

**接口**：`GET /api/im/user/info`

**响应**：

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "avatar": "/avatar/user1.png",
    "email": "zhangsan@example.com",
    "status": 1
  }
}
```

### 2. 更新用户信息

**接口**：`PUT /api/im/user/profile`

**请求参数**：

```json
{
  "nickname": "张三",
  "signature": "这个人很懒",
  "gender": 1
}
```

### 3. 上传头像

**接口**：`POST /api/im/user/avatar`

**请求类型**：`multipart/form-data`

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|-----|
| file | File | 是 | 头像文件 |

---

## 文件模块

### 1. 上传文件

**接口**：`POST /api/im/file/upload`

**请求类型**：`multipart/form-data`

**响应**：

```json
{
  "code": 200,
  "msg": "上传成功",
  "data": {
    "id": 123,
    "fileName": "document.pdf",
    "fileSize": 1024000,
    "fileUrl": "/files/2023/12/15/abc123.pdf"
  }
}
```

### 2. 下载文件

**接口**：`GET /api/im/file/download`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|-----|
| fileId | Long | 是 | 文件ID |

**响应类型**：`application/octet-stream`

### 3. 获取文件预览URL

**接口**：`GET /api/im/file/preview/{fileId}`

---

## WebSocket 接口

### 连接地址

```
ws://localhost:8080/ws/im?token={JWT_TOKEN}
```

### 消息格式

#### 客户端 -> 服务端

```json
{
  "type": "message",
  "payload": {
    "conversationId": 123,
    "type": "TEXT",
    "content": "你好",
    "clientMsgId": "20231215123456_abc123"
  }
}
```

#### 服务端 -> 客户端

```json
{
  "type": "message",
  "payload": {
    "id": 456,
    "conversationId": 123,
    "senderId": 2,
    "senderName": "李四",
    "type": "text",
    "content": "你好",
    "createTime": "2023-12-15 12:34:56"
  }
}
```

### 消息类型

| type | 说明 |
|------|-----|
| auth | 认证消息 |
| message | 聊天消息 |
| typing | 正在输入 |
| read | 已读回执 |
| ping/pong | 心跳 |
| online/offline | 在线状态 |
| incoming_call | 来电通知 |
| webrtc_signal | WebRTC信令 |

---

## 错误码说明

| 错误码 | 说明 |
|-------|-----|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| 错误码 | 说明 |
|-------|-----|
| 1001 | 用户不存在 |
| 1002 | 用户已存在 |
| 1003 | 密码错误 |
| 2001 | 会话不存在 |
| 2002 | 消息不存在 |
| 2003 | 消息撤回超时 |
| 3001 | 群组不存在 |
| 3002 | 不在群组中 |
| 4001 | 好友不存在 |
| 4002 | 好友申请已存在 |
