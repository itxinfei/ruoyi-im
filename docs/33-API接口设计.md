# 33 API接口设计文档

> 用途：定义前后端API接口规范
> 更新时间：2026-03-21
> 基础路径：/api

---

## 一、接口规范

### 1.1 通用规范

**请求头**：
```text
Content-Type: application/json
Authorization: Bearer {token}
X-Device-Id: {设备ID}
X-Client-Version: {客户端版本}
```

**响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1710921600000
}
```

**状态码规范**：
| 状态码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

### 1.2 分页参数

**请求参数**：
| 参数 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| pageNum | int | 1 | 页码 |
| pageSize | int | 20 | 每页数量 |
| orderBy | string | create_time | 排序字段 |
| orderDirection | string | DESC | 排序方向 |

**响应格式**：
```json
{
  "code": 200,
  "data": {
    "list": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 5
  }
}
```

---

## 二、用户模块 API

### 2.1 用户认证

#### POST /api/auth/login
用户登录

**请求体**：
```json
{
  "username": "string",
  "password": "string",
  "deviceType": "web|mobile|pc"
}
```

**响应体**：
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "zhangsan",
      "nickname": "张三",
      "avatar": "https://...",
      "department": "技术部",
      "position": "工程师"
    }
  }
}
```

#### POST /api/auth/logout
用户登出

#### POST /api/auth/refresh
刷新Token

### 2.2 用户信息

#### GET /api/user/info
获取当前用户信息

#### PUT /api/user/info
更新用户信息

**请求体**：
```json
{
  "nickname": "string",
  "avatar": "string",
  "signature": "string",
  "gender": "string (0=未知,1=男,2=女)"
}
```

#### GET /api/user/search
搜索用户

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| keyword | string | 是 | 搜索关键词 |
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

#### GET /api/user/online
获取在线用户列表

---

## 三、会话模块 API

### 3.1 会话管理

#### GET /api/conversation/list
获取会话列表

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| type | string | 否 | PRIVATE/GROUP |
| isPinned | int | 否 | 是否置顶 |
| isArchived | int | 否 | 是否归档 |

**响应体**：
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "type": "PRIVATE",
        "targetId": 2,
        "name": "张三",
        "avatar": "https://...",
        "lastMessage": {
          "id": 100,
          "content": "你好",
          "messageType": "TEXT",
          "senderId": 2,
          "senderName": "张三",
          "createTime": "2026-03-20 10:00:00"
        },
        "unreadCount": 5,
        "isPinned": true,
        "isMuted": false,
        "pinnedTime": "2026-03-19 10:00:00",
        "updateTime": "2026-03-20 10:00:00"
      }
    ]
  }
}
```

#### POST /api/conversation/create
创建会话

**请求体**：
```json
{
  "type": "PRIVATE|GROUP",
  "targetId": 1,
  "name": "string",
  "memberIds": [1, 2, 3]
}
```

#### PUT /api/conversation/{id}/pin
置顶会话

**请求体**：
```json
{
  "isPinned": true
}
```

#### PUT /api/conversation/{id}/mute
会话免打扰

**请求体**：
```json
{
  "isMuted": true
}
```

#### DELETE /api/conversation/{id}
删除会话

### 3.2 会话设置

#### GET /api/conversation/{id}/settings
获取会话设置

#### PUT /api/conversation/{id}/settings
更新会话设置

**请求体**：
```json
{
  "isPinned": true,
  "isMuted": false,
  "isArchived": false,
  "draftContent": "草稿内容"
}
```

---

## 四、消息模块 API

### 4.1 消息发送

#### POST /api/message/send
发送消息

**请求体**：
```json
{
  "conversationId": 1,
  "messageType": "TEXT|IMAGE|FILE|VOICE|VIDEO|LINK",
  "content": "消息内容",
  "clientMsgId": "uuid",
  "replyToMessageId": null,
  "fileUrl": null,
  "fileName": null,
  "fileSize": null
}
```

**响应体**：
```json
{
  "code": 200,
  "data": {
    "id": 100,
    "clientMsgId": "uuid",
    "sendStatus": "SENT",
    "createTime": "2026-03-20 10:00:00"
  }
}
```

### 4.2 消息查询

#### GET /api/message/list
获取消息列表

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| conversationId | long | 是 | 会话ID |
| lastMessageId | long | 否 | 最后消息ID（用于加载更多） |
| pageSize | int | 否 | 每页数量 |

**响应体**：
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 100,
        "conversationId": 1,
        "senderId": 2,
        "senderName": "张三",
        "senderAvatar": "https://...",
        "messageType": "TEXT",
        "content": "你好",
        "createTime": "2026-03-20 10:00:00",
        "isRevoked": false,
        "isEdited": false,
        "replyToMessageId": null,
        "reactions": [
          {
            "type": "LIKE",
            "emoji": "👍",
            "count": 5,
            "userReacted": true
          }
        ]
      }
    ],
    "hasMore": true
  }
}
```

#### GET /api/message/{id}
获取消息详情

### 4.3 消息操作

#### PUT /api/message/{id}/revoke
撤回消息

#### PUT /api/message/{id}/edit
编辑消息

**请求体**：
```json
{
  "content": "编辑后的内容"
}
```

#### DELETE /api/message/{id}
删除消息

#### POST /api/message/{id}/reaction
消息表情回应

**请求体**：
```json
{
  "reactionType": "LIKE|HEART|LAUGH|CRY|SURPRISE",
  "emoji": "👍"
}
```

#### DELETE /api/message/{id}/reaction
取消消息表情回应

### 4.4 消息已读

#### POST /api/message/read
标记消息已读

**请求体**：
```json
{
  "conversationId": 1,
  "lastReadMessageId": 100
}
```

#### GET /api/message/read-status/{messageId}
获取消息已读状态

**响应体**：
```json
{
  "code": 200,
  "data": {
    "messageId": 100,
    "totalCount": 10,
    "readCount": 8,
    "readUsers": [
      {
        "userId": 1,
        "userName": "张三",
        "avatar": "https://...",
        "readTime": "2026-03-20 10:00:00"
      }
    ],
    "unreadUsers": [
      {
        "userId": 2,
        "userName": "李四",
        "avatar": "https://..."
      }
    ]
  }
}
```

### 4.5 消息转发

#### POST /api/message/forward
转发消息

**请求体**：
```json
{
  "messageIds": [100, 101],
  "targetConversationIds": [1, 2],
  "forwardType": "SINGLE|COMBINE"
}
```

### 4.6 消息收藏

#### POST /api/message/favorite
收藏消息

**请求体**：
```json
{
  "messageId": 100,
  "remark": "备注",
  "tags": "标签1,标签2"
}
```

#### GET /api/message/favorite/list
获取收藏消息列表

#### DELETE /api/message/favorite/{id}
取消收藏

### 4.7 链接解析 (Link Metadata)

#### GET /api/im/url/parse
解析 URL 元数据 (C-010)

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| url | string | 是 | 需要解析的完整 URL |

**响应体**：
```json
{
  "code": 200,
  "data": {
    "title": "网页标题",
    "description": "内容摘要",
    "image": "缩略图URL",
    "siteName": "站点名称"
  }
}
```

---

## 五、群组模块 API

### 5.1 群组管理

#### POST /api/group/create
创建群组

**请求体**：
```json
{
  "name": "群名称",
  "avatar": "https://...",
  "memberIds": [1, 2, 3],
  "description": "群描述"
}
```

#### GET /api/group/list
获取群组列表

#### GET /api/group/{id}
获取群组详情

**响应体**：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "技术交流群",
    "avatar": "https://...",
    "ownerId": 1,
    "ownerName": "张三",
    "memberCount": 50,
    "maxMembers": 500,
    "description": "群描述",
    "allMuted": false,
    "allowUpload": true,
    "createTime": "2026-01-01 00:00:00",
    "myRole": "MEMBER"
  }
}
```

#### PUT /api/group/{id}
更新群组信息

#### DELETE /api/group/{id}
解散群组

### 5.2 群成员管理

#### GET /api/group/{id}/members
获取群成员列表

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| groupId | long | 是 | 群组ID |
| role | string | 否 | OWNER/ADMIN/MEMBER |
| keyword | string | 否 | 搜索关键词 |

#### POST /api/group/{id}/members
添加群成员

**请求体**：
```json
{
  "userIds": [1, 2, 3]
}
```

#### DELETE /api/group/{id}/members/{userId}
移除群成员

#### PUT /api/group/{id}/members/{userId}/role
设置群成员角色

**请求体**：
```json
{
  "role": "ADMIN|MEMBER"
}
```

#### POST /api/group/{id}/quit
退出群组

### 5.3 群公告

#### GET /api/group/{id}/announcements
获取群公告列表

#### POST /api/group/{id}/announcement
发布群公告

**请求体**：
```json
{
  "content": "公告内容",
  "isPinned": true
}
```

### 5.4 群文件

#### GET /api/group/{id}/files
获取群文件列表

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| category | string | 否 | document/image/video/audio/other |
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

## 六、好友模块 API

### 6.1 好友管理

#### GET /api/friend/list
获取好友列表

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| groupName | string | 否 | 分组名称 |

**响应体**：
```json
{
  "code": 200,
  "data": {
    "groups": [
      {
        "name": "我的好友",
        "friends": [
          {
            "id": 1,
            "userId": 2,
            "nickname": "张三",
            "avatar": "https://...",
            "remark": "备注名",
            "status": "ONLINE",
            "department": "技术部"
          }
        ]
      }
    ]
  }
}
```

#### POST /api/friend/add
添加好友

**请求体**：
```json
{
  "userId": 2,
  "message": "我是张三，想加你为好友",
  "groupName": "我的好友"
}
```

#### PUT /api/friend/{friendId}
更新好友信息

**请求体**：
```json
{
  "remark": "备注名",
  "groupName": "分组名",
  "tags": "标签1,标签2"
}
```

#### DELETE /api/friend/{friendId}
删除好友

### 6.2 好友申请

#### GET /api/friend/request/list
获取好友申请列表

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| type | string | 否 | received/sent |

#### PUT /api/friend/request/{id}
处理好友申请

**请求体**：
```json
{
  "status": "APPROVED|REJECTED",
  "groupName": "我的好友"
}
```

---

## 七、文件模块 API

### 7.1 文件上传

#### POST /api/file/upload
上传文件

**请求体**：multipart/form-data

| 字段 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| file | file | 是 | 文件 |
| conversationId | long | 否 | 会话ID |

**响应体**：
```json
{
  "code": 200,
  "data": {
    "fileId": 1,
    "fileUrl": "https://...",
    "fileName": "document.pdf",
    "fileSize": 102400,
    "fileType": "document"
  }
}
```

### 7.2 分片上传

#### POST /api/file/chunk/init
初始化分片上传

**请求体**：
```json
{
  "fileName": "video.mp4",
  "fileSize": 104857600,
  "fileMd5": "md5值",
  "chunkSize": 5242880
}
```

**响应体**：
```json
{
  "code": 200,
  "data": {
    "uploadId": "uuid",
    "totalChunks": 20,
    "uploadedChunks": []
  }
}
```

#### POST /api/file/chunk/upload
上传分片

**请求体**：multipart/form-data

| 字段 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| uploadId | string | 是 | 上传ID |
| chunkNumber | int | 是 | 分片序号 |
| chunkMd5 | string | 是 | 分片MD5 |
| file | file | 是 | 分片文件 |

#### POST /api/file/chunk/complete
完成分片上传

**请求体**：
```json
{
  "uploadId": "uuid"
}
```

### 7.3 文件下载

#### GET /api/file/download/{id}
下载文件

#### GET /api/file/preview/{id}
预览文件

---

## 八、搜索模块 API

### 8.1 全局搜索

#### GET /api/search
全局搜索

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| keyword | string | 是 | 搜索关键词 |
| type | string | 否 | all/message/user/group/file |
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

**响应体**：
```json
{
  "code": 200,
  "data": {
    "messages": {
      "list": [],
      "total": 100
    },
    "users": {
      "list": [],
      "total": 10
    },
    "groups": {
      "list": [],
      "total": 5
    },
    "files": {
      "list": [],
      "total": 20
    }
  }
}
```

### 8.2 消息搜索

#### GET /api/search/message
搜索消息

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| keyword | string | 是 | 搜索关键词 |
| conversationId | long | 否 | 会话ID |
| senderId | long | 否 | 发送者ID |
| startTime | datetime | 否 | 开始时间 |
| endTime | datetime | 否 | 结束时间 |
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

## 九、通知模块 API

### 9.1 系统通知

#### GET /api/notification/list
获取通知列表

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| type | string | 否 | SYSTEM/APPROVAL/MESSAGE/GROUP/FRIEND |
| isRead | int | 否 | 是否已读 |

#### PUT /api/notification/{id}/read
标记通知已读

#### PUT /api/notification/read-all
标记全部已读

#### GET /api/notification/unread-count
获取未读通知数量

---

## 十、设置模块 API

### 10.1 用户设置

#### GET /api/user/settings
获取用户设置

#### PUT /api/user/settings
更新用户设置

**请求体**：
```json
{
  "notification": {
    "enabled": true,
    "desktopNotification": true,
    "soundEnabled": true,
    "showPreview": true,
    "dndEnabled": false,
    "dndStartTime": "22:00",
    "dndEndTime": "08:00"
  },
  "privacy": {
    "showOnlineStatus": true,
    "allowSearch": true,
    "allowAddFriend": true,
    "receiptEnabled": true,
    "clearUnreadMode": 0
  }
}
```

**字段说明**：
- `receiptEnabled`: 已读回执开关 (C-002)
- `clearUnreadMode`: 未读清零规则: 0进入即清零 1已读回执清零 (C-003)

---

## 十一、通话模块 API

### 11.1 通话管理

#### POST /api/call/initiate
发起通话

**请求体**：
```json
{
  "targetId": 1,
  "targetType": "USER|GROUP",
  "callType": "VOICE|VIDEO"
}
```

**响应体**：
```json
{
  "code": 200,
  "data": {
    "callId": "uuid",
    "status": "RINGING",
    "createTime": "2026-03-20 10:00:00"
  }
}
```

#### POST /api/call/answer
接听通话

**请求体**：
```json
{
  "callId": "uuid"
}
```

#### POST /api/call/reject
拒绝通话

**请求体**：
```json
{
  "callId": "uuid"
}
```

#### POST /api/call/end
结束通话

**请求体**：
```json
{
  "callId": "uuid"
}
```

### 11.2 通话记录

#### GET /api/call/records
获取通话记录

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| type | string | 否 | VOICE/VIDEO |
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

**响应体**：
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "callId": "uuid",
        "callerId": 1,
        "callerName": "张三",
        "calleeId": 2,
        "calleeName": "李四",
        "callType": "VOICE",
        "status": "COMPLETED|MISSED|REJECTED",
        "duration": 120,
        "createTime": "2026-03-20 10:00:00"
      }
    ]
  }
}
```

---

## 十二、日历日程模块 API

### 12.1 日程管理

#### GET /api/schedule/list
获取日程列表

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| startDate | date | 否 | 开始日期 |
| endDate | date | 否 | 结束日期 |
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

**响应体**：
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "title": "项目会议",
        "content": "讨论项目进度",
        "startTime": "2026-03-20 10:00:00",
        "endTime": "2026-03-20 11:00:00",
        "isAllDay": false,
        "location": "会议室A",
        "remindTime": 15,
        "remindUnit": "MINUTE",
        "repeatType": "NONE|DAILY|WEEKLY|MONTHLY",
        "participants": [
          {"userId": 1, "userName": "张三"}
        ],
        "createTime": "2026-03-19 10:00:00"
      }
    ]
  }
}
```

#### POST /api/schedule/create
创建日程

**请求体**：
```json
{
  "title": "项目会议",
  "content": "讨论项目进度",
  "startTime": "2026-03-20 10:00:00",
  "endTime": "2026-03-20 11:00:00",
  "isAllDay": false,
  "location": "会议室A",
  "remindTime": 15,
  "remindUnit": "MINUTE",
  "repeatType": "NONE",
  "participantIds": [1, 2, 3]
}
```

#### PUT /api/schedule/{id}
更新日程

#### DELETE /api/schedule/{id}
删除日程

### 12.2 日历视图

#### GET /api/schedule/calendar
获取日历视图数据

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| year | int | 是 | 年份 |
| month | int | 是 | 月份 |

---

## 十三、待办任务模块 API

### 13.1 待办管理

#### GET /api/todo/list
获取待办列表

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| status | string | 否 | PENDING/COMPLETED/EXPIRED |
| priority | string | 否 | HIGH/MEDIUM/LOW |
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

**响应体**：
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "title": "完成项目报告",
        "content": "提交本周项目进度报告",
        "priority": "HIGH",
        "status": "PENDING",
        "dueTime": "2026-03-20 18:00:00",
        "remindTime": "2026-03-20 17:00:00",
        "tags": ["工作", "报告"],
        "createTime": "2026-03-19 10:00:00"
      }
    ]
  }
}
```

#### POST /api/todo/create
创建待办

**请求体**：
```json
{
  "title": "完成项目报告",
  "content": "提交本周项目进度报告",
  "priority": "HIGH",
  "dueTime": "2026-03-20 18:00:00",
  "remindTime": "2026-03-20 17:00:00",
  "tags": "工作,报告"
}
```

#### PUT /api/todo/{id}
更新待办

#### PUT /api/todo/{id}/complete
完成待办

#### DELETE /api/todo/{id}
删除待办

---

## 十四、审批流程模块 API

### 14.1 审批申请

#### GET /api/approval/template/list
获取审批模板列表

**响应体**：
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "name": "请假申请",
        "icon": "leave",
        "description": "员工请假审批",
        "fields": [
          {"name": "leaveType", "label": "请假类型", "type": "select", "options": ["事假", "病假", "年假"]},
          {"name": "startDate", "label": "开始日期", "type": "date"},
          {"name": "endDate", "label": "结束日期", "type": "date"},
          {"name": "reason", "label": "请假原因", "type": "textarea"}
        ]
      }
    ]
  }
}
```

#### POST /api/approval/submit
提交审批申请

**请求体**：
```json
{
  "templateId": 1,
  "title": "请假申请",
  "formData": {
    "leaveType": "事假",
    "startDate": "2026-03-21",
    "endDate": "2026-03-22",
    "reason": "个人事务"
  },
  "approverIds": [1, 2]
}
```

#### GET /api/approval/my/list
获取我的申请列表

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| status | string | 否 | PENDING/APPROVED/REJECTED |

#### GET /api/approval/pending/list
获取待我审批列表

### 14.2 审批处理

#### GET /api/approval/{id}
获取审批详情

#### POST /api/approval/{id}/approve
审批通过

**请求体**：
```json
{
  "comment": "同意"
}
```

#### POST /api/approval/{id}/reject
审批拒绝

**请求体**：
```json
{
  "comment": "拒绝原因"
}
```

---

## 十五、AI助手模块 API

### 15.1 AI对话

#### POST /api/ai/chat
AI对话

**请求体**：
```json
{
  "conversationId": "uuid",
  "message": "帮我总结一下今天的会议内容",
  "context": {
    "messageIds": [100, 101, 102]
  }
}
```

**响应体**：
```json
{
  "code": 200,
  "data": {
    "reply": "根据会议记录，今天讨论了以下内容...",
    "conversationId": "uuid"
  }
}
```

#### GET /api/ai/conversations
获取AI对话历史

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

### 15.2 AI功能

#### POST /api/ai/summarize
消息摘要

**请求体**：
```json
{
  "messageIds": [100, 101, 102]
}
```

#### POST /api/ai/translate
消息翻译

**请求体**：
```json
{
  "content": "你好，世界",
  "targetLang": "en"
}
```

**响应体**：
```json
{
  "code": 200,
  "data": {
    "translated": "Hello, World"
  }
}
```

---

## 十六、DING消息模块 API

### 16.1 DING消息管理

#### POST /api/ding/send
发送DING消息

**请求体**：
```json
{
  "content": "紧急会议通知",
  "recipientIds": [1, 2, 3],
  "notifyType": "APP|SMS|PHONE"
}
```

**响应体**：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "sentCount": 3,
    "createTime": "2026-03-20 10:00:00"
  }
}
```

#### GET /api/ding/received
获取收到的DING消息

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| status | string | 否 | PENDING/CONFIRMED |
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

#### GET /api/ding/sent
获取发送的DING消息

#### PUT /api/ding/{id}/confirm
确认DING消息

#### GET /api/ding/{id}/stats
获取DING统计

**响应体**：
```json
{
  "code": 200,
  "data": {
    "totalCount": 10,
    "confirmedCount": 8,
    "unconfirmedUsers": [
      {"userId": 1, "userName": "张三"}
    ]
  }
}
```

---

## 十七、管理后台扩展 API

### 17.1 部门管理

#### GET /api/admin/department/tree
获取部门树

**响应体**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "总公司",
      "parentId": 0,
      "sort": 1,
      "leader": "张三",
      "phone": "138****8888",
      "children": [
        {
          "id": 2,
          "name": "技术部",
          "parentId": 1,
          "sort": 1,
          "leader": "李四",
          "children": []
        }
      ]
    }
  ]
}
```

#### POST /api/admin/department
创建部门

**请求体**：
```json
{
  "name": "新部门",
  "parentId": 1,
  "sort": 1,
  "leader": "张三",
  "phone": "138****8888",
  "email": "dept@company.com",
  "status": 1
}
```

#### PUT /api/admin/department/{id}
更新部门

#### DELETE /api/admin/department/{id}
删除部门

**说明**：存在子部门或成员时不允许删除

#### GET /api/admin/department/{id}/members
获取部门成员

### 17.2 角色管理

#### GET /api/admin/role/list
获取角色列表

**响应体**：
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "roleName": "超级管理员",
        "roleKey": "SUPER_ADMIN",
        "sort": 1,
        "status": 1,
        "userCount": 1,
        "createTime": "2026-01-01 00:00:00"
      }
    ]
  }
}
```

#### POST /api/admin/role
创建角色

**请求体**：
```json
{
  "roleName": "部门管理员",
  "roleKey": "DEPT_ADMIN",
  "sort": 2,
  "status": 1,
  "menuIds": [1, 2, 3],
  "dataScope": "DEPT"
}
```

#### PUT /api/admin/role/{id}
更新角色

#### DELETE /api/admin/role/{id}
删除角色

**说明**：已分配用户的角色不允许删除

#### GET /api/admin/role/{id}/permissions
获取角色权限

#### PUT /api/admin/role/{id}/permissions
更新角色权限

**请求体**：
```json
{
  "menuIds": [1, 2, 3, 4, 5],
  "dataScope": "DEPT|ALL|SELF"
}
```

### 17.3 数据统计

#### GET /api/admin/stats/overview
获取概览统计

**响应体**：
```json
{
  "code": 200,
  "data": {
    "totalUsers": 1000,
    "activeUsers": 800,
    "totalGroups": 50,
    "todayMessages": 5000,
    "userGrowth": 5.2,
    "messageGrowth": 12.3
  }
}
```

#### GET /api/admin/stats/message-trend
获取消息趋势

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| days | int | 否 | 统计天数，默认7天 |

#### GET /api/admin/stats/user-active
获取用户活跃度

#### GET /api/admin/stats/group-active
获取群组活跃度

---

## 十七、业务错误码字典 (Error Taxonomy)

系统采用 5 位数原子化错误码，AI 必须严格按此返回，禁止自行发明。

| 错误码 | 模块 | 场景描述 | 推荐前端操作 |
|-------|-----|---------|-------------|
| **10001** | 认证 | 用户名或密码错误 | 提示文案并清空密码框 |
| **10003** | 认证 | Token 已过期 | 自动触发 Refresh Token 或退回登录 |
| **20001** | 消息 | 消息撤回已超过 2 分钟 | 提示"超过时间限制，无法撤回" |
| **20002** | 消息 | 消息内容包含敏感词 | 气泡旁显示红色感叹号 |
| **20003** | 消息 | 发送频率过快 (限流) | 临时禁用发送按钮 3s |
| **30001** | 群组 | 您已被移出该群组 | 自动关闭聊天窗口并移除会话 |
| **30005** | 群组 | 全员禁言中 | 输入框显示"禁言中"占位符 |
| **40001** | 文件 | 文件大小超过 100MB | 拦截上传并提示限制 |
| **40003** | 文件 | 文件 MD5 校验失败 | 提示"文件传输损坏，请重试" |
| **50001** | 通话 | 对方设备暂不支持音视频 | 提示"对方版本过低，无法呼叫" |

---

## 十八、变更记录


| 日期 | 变更内容 | 变更人 |
|-----|---------|-------|
| 2026-03-21 | 新增管理后台扩展API（部门管理、角色管理、数据统计） | AI |
| 2026-03-21 | 新增DING消息模块API接口 | AI |
| 2026-03-20 | 初始版本创建 | AI |