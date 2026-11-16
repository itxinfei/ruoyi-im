# 06 RESTful API 设计

> **定位**：定义 12 个微服务的完整 RESTful API 清单、请求/响应结构、错误码、分页规范。
> **更新时间**：2026-10-30
> **统一前缀**：`/im/`（所有微服务），Gateway 路由到对应服务端口

---

## 一、通用规范

### 1.1 请求规范

| 项 | 约定 |
|----|------|
| Base URL | `https://{host}/im/` |
| 认证 | `Authorization: Bearer {token}` |
| Content-Type | `application/json;charset=UTF-8` |
| 时间格式 | ISO-8601 `yyyy-MM-ddTHH:mm:ss` (UTC+8) |
| 分页参数 | `page`(1-based) / `size`(default 20, max 100) |

### 1.2 统一响应体

```json
{
  "code": 200,
  "msg": "success",
  "data": { },
  "timestamp": 1730000000000
}
```

### 1.3 分页响应体

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [],
    "total": 1234,
    "page": 1,
    "size": 20,
    "hasNext": true
  }
}
```

### 1.4 错误码规范

| 区间 | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 / Token 失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务端异常 |
| 1xxxx | IM 业务错误 |
| 2xxxx | 用户/通讯录业务错误 |
| 3xxxx | 协同业务错误 |

| 错误码 | 说明 |
|--------|------|
| 10001 | 消息不存在 |
| 10002 | 消息无法撤回 (超时) |
| 10003 | 不是会话成员 |
| 10004 | 消息内容过长 |
| 10005 | 敏感词拦截 |
| 10006 | 对象不存在 |
| 20001 | 用户不存在 |
| 20002 | 已是好友 |
| 20003 | 部门不存在 |
| 30001 | 审批模板不存在 |
| 30002 | 审批记录不存在 |
| 30003 | 日程冲突 |

---

## 二、im-message 服务 API (8081)

### 2.1 消息管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/message/send` | 发送消息（HTTP 备用通道，主要走 WS） |
| POST | `/im/message/{msgId}/recall` | 撤回消息 |
| PUT | `/im/message/{msgId}/edit` | 编辑消息 |
| DELETE | `/im/message/{msgId}` | 删除消息（本地） |
| GET | `/im/message/history` | 历史消息（游标分页） |
| GET | `/im/message/{msgId}` | 查询单条消息 |
| POST | `/im/message/react/{msgId}` | 添加表情反应 |
| DELETE | `/im/message/react/{msgId}` | 移除表情反应 |

### 2.2 消息操作 (IM-M 撤回/编辑/转发/已读)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/message/{msgId}/recall` | 撤回消息（2min 内有效） |
| PUT  | `/im/message/{msgId}/edit` | 编辑消息（5min 内有效，仅 TEXT/IMAGE/FILE 描述） |
| POST | `/im/message/forward` | 转发消息到会话/群组 |
| GET  | `/im/message/read-status/{msgId}` | 查询消息已读状态（群聊返回已读人员列表） |

### 2.3 @提及 (IM-D-011) + 链接预览 (IM-D-006)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/im/message/mentions` | 获取@我的未读消息列表 |
| PUT | `/im/message/mentions/read` | 标记所有@提及已读 |
| POST | `/im/url/preview` | 解析 URL → 链接预览卡片（30天缓存） |

### 2.4 请求示例

```jsonc
// POST /im/message/send
{
  "conversationId": 123456789,
  "msgType": "TEXT",
  "content": "你好!",
  "clientSeq": "uuid-001",
  "quotedMsgId": null,
  "extra": null
}
```

```jsonc
// Response
{
  "code": 200,
  "data": {
    "messageId": 987654321,
    "serverSeq": 42,
    "status": "SENT",
    "createTime": "2026-10-30T10:30:00"
  }
}
```

```text
// GET /im/message/history?convId=123456789&beforeSeq=100&limit=50
// Response: { "data": { "list": [...], "hasNext": true, "oldestSeq": 50 } }
```

> **请求限制**: TEXT 单条消息最长 5000 字符；IMAGE/FILE 类无字符限制，文件上限见 §9。

---

## 三、im-session 服务 API (8082)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/im/session/list` | 会话列表（带 lastMsg） |
| GET | `/im/session/{convId}` | 会话详情 |
| POST | `/im/session` | 创建会话 |
| PUT | `/im/session/{convId}` | 更新会话 |
| DELETE | `/im/session/{convId}` | 删除会话 |
| POST | `/im/session/{convId}/read` | 标记全部已读 |
| GET | `/im/session/{convId}/members` | 会话成员列表 |
| PUT | `/im/session/{convId}/pin` | 置顶/取消置顶 |
| PUT | `/im/session/{convId}/mute` | 免打扰设置 |
| GET | `/im/session/draft/{convId}` | 获取草稿 |
| PUT | `/im/session/draft/{convId}` | 保存草稿 |

### 3.1 会话分组 (IM-S-009)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/im/session/groups` | 获取当前用户的所有会话分组 |
| POST | `/im/session/groups` | 创建会话分组 |
| PUT | `/im/session/groups/{groupId}` | 更新分组名称/排序 |
| DELETE | `/im/session/groups/{groupId}` | 删除分组（不影响会话） |
| PUT | `/im/session/groups/{groupId}/sessions` | 批量移动会话到分组 |
| PUT | `/im/session/{convId}/group` | 设置/取消会话分组归属 |

### 3.2 会话批量操作 (IM-S-010)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/session/batch/read` | 批量标记已读 |
| POST | `/im/session/batch/delete` | 批量删除会话（不删聊天记录） |
| POST | `/im/session/batch/pin` | 批量置顶/取消置顶 |
| POST | `/im/session/batch/mute` | 批量设置免打扰 |

### 3.3 消息收藏 (IM-S-001~005)

> **实现服务**: im-message (与消息同服务)
> **设计思路**: 收藏不复制消息内容，仅保存引用；通过 JOIN 查询获取原始消息。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/favorite` | 收藏消息（传入 `msgId`） |
| DELETE | `/im/favorite/{favoriteId}` | 取消收藏 |
| GET | `/im/favorite/list` | 收藏列表（分页，按收藏时间倒序） |
| GET | `/im/favorite/search` | 搜索已收藏内容（关键词） |

---

## 四、im-group 服务 API (8083)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/group` | 创建群组 |
| GET | `/im/group/{groupId}` | 群信息 |
| PUT | `/im/group/{groupId}` | 更新群信息 |
| DELETE | `/im/group/{groupId}` | 解散群组 |
| GET | `/im/group/{groupId}/members` | 成员列表 |
| POST | `/im/group/{groupId}/members` | 添加成员 |
| DELETE | `/im/group/{groupId}/members/{userId}` | 移除成员 |
| PUT | `/im/group/{groupId}/members/{userId}/role` | 设置成员角色 |
| PUT | `/im/group/{groupId}/members/{userId}/mute` | 禁言 |
| PUT | `/im/group/{groupId}/transfer` | 转让群主 |
| POST | `/im/group/{groupId}/announcement` | 发布公告 |
| PUT | `/im/group/{groupId}/announcement/{announcementId}` | 编辑群公告 |
| DELETE | `/im/group/{groupId}/announcement/{announcementId}` | 删除群公告 |
| GET | `/im/group/{groupId}/announcements` | 公告列表 |
| PUT | `/im/group/{groupId}/members/{userId}/nickname` | 设置/修改群昵称（个人备注，仅自己可见） |
| GET | `/im/group/{groupId}/files` | 群文件列表 |
| POST | `/im/group/{groupId}/files` | 上传文件到群（通用上传后关联群） |
| GET | `/im/group/{groupId}/files/search` | 搜索群文件（按文件名） |
| DELETE | `/im/group/{groupId}/files/{fileId}` | 删除群文件（群主/管理员） |
| POST | `/im/group/{groupId}/files/batch-delete` | 批量删除群文件 |
| POST | `/im/group/join` | 申请入群 |
| PUT | `/im/group/{groupId}/approve` | 审批入群 |
| DELETE | `/im/group/{groupId}/exit` | 退群 |

---

## 五、im-real-time 服务 API (8084)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/im/rt/health` | 健康检查 |
| GET | `/im/rt/online/{userId}` | 查询用户是否在线 |
| POST | `/im/rt/push` | 内部调用：推送消息 |
| GET | `/im/rt/stats` | 连接统计 |

> 主要业务通过 WebSocket，REST 仅用于健康检查和内部推送。

### 5.1 通话信令 (IM-C-001~007)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/call/token` | 获取 LiveKit 接入 JWT（房间号 + 用户ID） |
| POST | `/im/call/room` | 创建 LiveKit 房间（群聊通话） |
| DELETE | `/im/call/room/{roomName}` | 销毁房间 |

### 5.2 通话记录 (IM-C-009)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/im/call/history` | 通话记录列表（分页，按时间倒序） |
| GET | `/im/call/history/{callId}` | 通话详情（含时长 / 房间 / 录制文件） |
| DELETE | `/im/call/history/{callId}` | 删除通话记录 |

### 5.3 录制管理 (IM-C-010)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/call/recording/start` | 启动 LiveKit Egress 录制 |
| DELETE | `/im/call/recording/{recordingId}` | 停止录制 + 触发 MinIO 上传 |

### 5.4 DING 消息限流查询

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/im/ding/limit` | 查询当日剩余可发条数 |

> **限流规则** (doc 08 补充 Redis key):
> 每人每天最多 10 条: Redis Key = `im:ding:limit:{userId}:{yyyyMMdd}`，TTL=24h
> 超过限流返回 HTTP 429，错误码 10007

---

## 六、DING 消息 API (im-message, 8081)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/ding` | 发送 DING 消息 |
| GET  | `/im/ding/received` | 收到的 DING 列表 |
| GET  | `/im/ding/sent` | 我发送的 DING 列表 |
| GET  | `/im/ding/{dingId}/receipts` | DING 确认进度（已读/未读人数明细） |
| PUT  | `/im/ding/{dingId}/ack` | 确认 DING |
| POST | `/im/ding/{dingId}/ack` | 确认 DING (REST 版本，备用) |
| GET  | `/im/ding/limit` | 查询当日剩余可发条数 |

> **限流规则** (doc 08 补充 Redis key):
> 每人每天最多 10 条: Redis Key = `im:ding:limit:{userId}:{yyyyMMdd}`，TTL=24h
> 超过限流返回 HTTP 429，错误码 10007

---

## 七、collab-task 服务 API (8085)

### 6.0 待办扩展 API (PL-T 补充)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/todo/from-message` | 消息转待办（专用端点，携带 `msgId` + 预填内容） |
| POST | `/im/todo/batch/complete` | 批量完成待办（传 `todoIds: Long[]`） |
| POST | `/im/todo/batch/delete` | 批量删除待办 |
| POST | `/im/todo/batch/update-priority` | 批量修改优先级 |
| GET  | `/im/todo/categories` | 获取待办分类统计计数 |

### 6.1 待办

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/im/todo/list` | 待办列表 |
| POST | `/im/todo` | 创建待办 |
| PUT | `/im/todo/{todoId}` | 更新待办 |
| DELETE | `/im/todo/{todoId}` | 删除待办 |
| PUT | `/im/todo/{todoId}/finish` | 完成待办 |

> **筛选参数**: `GET /im/todo/list` 支持可选参数 `?category=WORK&status=PENDING&priority=1&sort=due_time&order=asc`

### 6.2 审批

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/approval` | 发起审批 |
| GET | `/im/approval/list` | 审批列表（待办/已发起/已审批） |
| GET | `/im/approval/{approvalId}` | 审批详情 |
| PUT | `/im/approval/{approvalId}/approve` | 审批通过 |
| PUT | `/im/approval/{approvalId}/reject` | 审批驳回 |
| PUT | `/im/approval/{approvalId}/transfer` | 转交 |
| PUT | `/im/approval/{approvalId}/cancel` | 撤回审批 |

### 6.2.1 审批模板 (PL-A 补充)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/im/approval/templates` | 审批模板列表 |
| GET | `/im/approval/templates/{id}` | 审批模板详情 (含 form_schema + flow_schema) |
| POST | `/im/approval/templates` | 创建审批模板 |
| PUT | `/im/approval/templates/{id}` | 更新审批模板 |
| DELETE | `/im/approval/templates/{id}` | 停用审批模板 |
| POST | `/im/approval/templates/{id}/publish` | 模板发布（版本+1） |

### 6.3 工作报告

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/report` | 提交报告 |
| GET | `/im/report/list` | 报告列表 |

---

## 八、collab-calendar 服务 API (8086)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/calendar/event` | 创建事件 |
| GET | `/im/calendar/event/{eventId}` | 事件详情 |
| PUT | `/im/calendar/event/{eventId}` | 更新事件 |
| DELETE | `/im/calendar/event/{eventId}` | 删除事件 |
| GET | `/im/calendar/events` | 日程列表（时间范围） |
| GET | `/im/calendar/events/today` | 今日日程 |
| POST | `/im/calendar/event/{eventId}/participants` | 添加参与者 |
| PUT | `/im/calendar/event/{eventId}/respond` | 响应邀请 |

---

## 九、collab-doc 服务 API (8087)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/doc` | 创建文档 |
| GET | `/im/doc/{docId}` | 文档详情 |
| PUT | `/im/doc/{docId}` | 更新文档 |
| DELETE | `/im/doc/{docId}` | 删除文档 |
| GET | `/im/doc/list` | 文档列表 |
| GET | `/im/doc/{docId}/versions` | 版本历史 |
| POST | `/im/doc/{docId}/versions` | 创建新版本 |
| POST | `/im/doc/{docId}/share` | 生成分享链接 |
| POST | `/im/doc/{docId}/collaborators` | 添加协作者 |
| GET | `/im/doc/share/{shareCode}` | 通过分享码获取文档 |

---

## 十、file-storage 服务 API (8088)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/file/upload` | 上传文件（小文件） |
| POST | `/im/file/chunk/init` | 初始化分片上传 |
| POST | `/im/file/chunk/{uploadId}` | 上传分片 |
| POST | `/im/file/chunk/{uploadId}/complete` | 合并分片 |
| GET | `/im/file/{fileId}` | 文件元数据 |
| GET | `/im/file/{fileId}/download` | 下载文件（返回预签名URL） |
| DELETE | `/im/file/{fileId}` | 删除文件 |
| GET | `/im/file/list` | 文件列表 |

---

## 十一、platform-user 服务 API (8089)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/auth/login` | 登录 |
| POST | `/im/auth/logout` | 登出 |
| POST | `/im/auth/refresh` | 刷新 Token |
| GET | `/im/user/info` | 当前用户信息 |
| GET | `/im/user/info/{userId}` | 指定用户信息 |
| PUT | `/im/user/info` | 更新用户信息 |
| GET | `/im/user/search` | 搜索用户 |
| GET | `/im/dept/tree` | 组织架构树 |
| GET | `/im/dept/{deptId}/users` | 部门用户列表 |
| POST | `/im/friend/apply` | 申请好友 |
| PUT | `/im/friend/{requestId}/accept` | 通过好友 |
| PUT | `/im/friend/{requestId}/reject` | 拒绝好友 |
| GET | `/im/friend/list` | 好友列表 |
| PUT | `/im/friend/{friendId}/remark` | 修改好友备注 |
| DELETE | `/im/friend/{friendId}` | 删除好友 |
| GET | `/im/qrcode/generate` | 生成二维码 |

---

## 十二、admin-system 服务 API (8090)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/im/admin/dashboard` | 数据概览 |
| GET | `/im/admin/users` | 用户列表 |
| PUT | `/im/admin/users/{userId}/status` | 修改用户状态 |
| DELETE | `/im/admin/users/{userId}` | 删除用户 |
| GET | `/im/admin/groups` | 群组列表 |
| DELETE | `/im/admin/groups/{groupId}` | 解散群组 |
| GET | `/im/admin/messages` | 消息审计 |
| GET | `/im/admin/audit-log` | 操作审计日志 |
| GET | `/im/admin/config` | 系统配置 |
| PUT | `/im/admin/config` | 更新配置 |

---

## 十三、platform-ai 服务 API (8091)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/im/ai/chat` | AI 对话 |
| POST | `/im/ai/chat/stream` | AI 流式对话 (SSE) |
| GET | `/im/ai/models` | 可用模型列表 |
| GET | `/im/ai/status` | AI 服务状态 |

---

## 十四、工作台 API (PL-W, collab-task 8085)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/im/workbench/summary` | 工作台首页汇总 (待办数/审批待办/日程) |
| GET | `/im/workbench/schedule` | 工作台日程 (按日聚合审批/待办/日历事件) |
| GET | `/im/workbench/quick-apps` | 快捷应用入口配置 |
| PUT | `/im/workbench/quick-apps` | 编辑快捷应用配置 |

## 十五、platform-search 服务 API (8092)

### 15.1 搜索入口 (IM-SE-001~004)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/im/search/global` | 全局搜索（联系人+群+消息+文件聚合返回） |
| GET | `/im/search/message` | 消息搜索（会话内 or 跨会话） |
| GET | `/im/search/contact` | 联系人搜索（姓名/手机号/邮箱） |
| GET | `/im/search/doc` | 文档搜索（文件名/内容） |

### 15.2 搜索参数 (IM-SE-005~008)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | String | ✅ | 搜索关键词（ngram 分词） |
| `convId` | Long | ❌ | 指定会话内搜索（空=全局） |
| `msgType` | String | ❌ | 按消息类型筛选（TEXT/IMAGE/FILE 等） |
| `senderId` | Long | ❌ | 按发送人筛选 |
| `startTime` | DateTime | ❌ | 时间范围起 |
| `endTime` | DateTime | ❌ | 时间范围止 |
| `pageNum` | Int | ❌ | 页码（默认 1） |
| `pageSize` | Int | ❌ | 每页条数（默认 20，最大 50） |

---

## 十六、Gateway 路由规则

```yaml
# Spring Cloud Gateway 配置
spring:
  cloud:
    gateway:
      routes:
        - id: im-message
          uri: http://localhost:8081
          predicates:
            - Path=/im/message/**,/im/conversation/**
        - id: im-session
          uri: http://localhost:8082
          predicates:
            - Path=/im/session/**
        - id: im-group
          uri: http://localhost:8083
          predicates:
            - Path=/im/group/**
        - id: collab-task
          uri: http://localhost:8085
          predicates:
            - Path=/im/todo/**,/im/approval/**,/im/report/**
        - id: collab-calendar
          uri: http://localhost:8086
          predicates:
            - Path=/im/calendar/**
        - id: collab-doc
          uri: http://localhost:8087
          predicates:
            - Path=/im/doc/**
        - id: file-storage
          uri: http://localhost:8088
          predicates:
            - Path=/im/file/**
        - id: platform-user
          uri: http://localhost:8089
          predicates:
            - Path=/im/auth/**,/im/user/**,/im/dept/**,/im/friend/**
        - id: admin-system
          uri: http://localhost:8090
          predicates:
            - Path=/im/admin/**
        - id: platform-ai
          uri: http://localhost:8091
          predicates:
            - Path=/im/ai/**
        - id: platform-search
          uri: http://localhost:8092
          predicates:
            - Path=/im/search/**
```

---

## 十七、AI SSE 协议规范

> **对应审查报告**: 27-辅助功能审查 §问题 2 (P1)
> **端点**: `POST /im/ai/chat/stream` (platform-ai 8091)
> **Content-Type**: `text/event-stream;charset=UTF-8`

### 16.1 SSE 帧格式

服务端以 SSE 协议推送流式 token，每帧格式如下：

```text
data: {"token":"你","finish":false,"model":"deepseek-chat"}

data: {"token":"好","finish":false,"model":"deepseek-chat"}

data: {"token":"","finish":true,"model":"deepseek-chat","usage":{"prompt_tokens":12,"completion_tokens=3"}}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| token | String | 增量 token 文本（可能为空字符串） |
| finish | Boolean | true 表示流结束 |
| model | String | 使用的模型 ID |
| usage | Token 结束时出现 | prompt_tokens / completion_tokens |

### 16.2 错误帧

当首 token 推理失败或模型服务不可用时，服务端在 SSE 首帧返回 error 事件：

```text
event: error
data: {"code":5001,"msg":"模型服务不可用","retryable":true}
```

| 错误码 | 含义 | 客户端行为 |
|--------|------|-----------|
| 5001 | 模型服务不可达 | 展示黄条降级态，3s 后重试 |
| 5002 | 上下文超 maxTokens | 截断历史消息后重试（减半） |
| 5003 | 限流 (RPM 超限) | 指数退避重试，上限 30s |
| 5004 | Token 过期 | 触发 refresh 后重新建连 |
| 5005 | 内容审核拦截 | 展示审核提示，不重试 |

### 16.3 超时与重试

| 参数 | 默认值 | 说明 |
|------|--------|------|
| 首 token 超时 | 30s | 握手成功后 30s 内未收到首帧 → 前端判定失败 |
| 帧间超时 | 15s | 两个 data 帧间隔超过 15s → 客户端 AbortController 中止 |
| 最大重试次数 | 3 次 | 含首次请求共 4 次尝试（含抖动退避） |
| 重试退避 | 1s→2s→4s + random(0,500ms) | 递增指数退避 |

### 16.4 客户端重连机制 (Last-Event-Id)

```text
首次请求: POST /im/ai/chat/stream (无 Last-Event-Id)
中断重连: POST /im/ai/chat/stream + Header: Last-Event-Id: <最后收到的 messageId>
```

服务端从 Last-Event-Id + 1 续传 token（需服务端缓存最近 200 条流式 messageId → 续传偏移量映射表，TTL=5min）。

### 16.5 降级策略 (doc 13 §11.6 对齐)

| 场景 | 降级行为 |
|------|---------|
| platform-ai 服务不可达 | 顶部黄条提示 (doc 13 §11.6)，轮询 /im/ai/status 每 30s |
| 轮询间隔未配置 | 默认 30s（Nacos 配置 `im.ai.status-poll-interval`） |
| 用户中途关闭页面 | AbortController.abort() → 服务端检测到 cancelled 取消推理内联任务 |

---

## 十八、通讯录扩展 API（CO-C-007/008）

| 方法 | 路径 | 服务 | 说明 |
|------|------|------|------|
| GET | /im/contact/frequent | im-user 8087 | 常用联系人排序（按最近 30 天联系频次） |
| GET | /im/external-contact | im-user 8087 | 外部联系人列表（owner_id=当前用户） |
| POST | /im/external-contact | im-user 8087 | 新建外部联系人 |
| PUT | /im/external-contact/{id} | im-user 8087 | 更新外部联系人 |
| DELETE | /im/external-contact/{id} | im-user 8087 | 删除外部联系人 |

## 十九、群管理补充 API（CO-G-006/010）

| 方法 | 路径 | 服务 | 说明 |
|------|------|------|------|
| PUT | /im/group/{groupId}/announcement/{announcementId} | im-group 8083 | 编辑群公告 |
| DELETE | /im/group/{groupId}/announcement/{announcementId} | im-group 8083 | 删除群公告 |
| PUT | /im/group/{groupId}/members/{userId}/nickname | im-group 8083 | 修改群内昵称 |
| GET | /im/friend/requests | im-user 8087 | 待处理好友申请列表 |

## 二十、日历补充 API（PL-CA-005/006）

| 方法 | 路径 | 服务 | 说明 |
|------|------|------|------|
| GET | /im/calendar/freebusy | collab-calendar 8086 | 查询多用户空忙状态（userIds + startTime + endTime） |
| POST | /im/calendar/{eventId}/cancel | collab-calendar 8086 | 取消日程（软删除 + 通知参与者） |

---

## 二十一、变更记录

| 日期 | 变更 |
|------|------|
| 2026-10-30 | 定义 12 个微服务完整 API 清单（87 个端点）、统一响应格式、错误码、Gateway 路由规则 |
| 2026-10-30 | 新增 六、DING 消息 API 章节 (im-message, 8081): 7 个 REST 端点 + 限流规则；后续章节序号顺延 |
| 2026-05-07 | 新增 十七 AI SSE 协议规范: SSE 帧格式、5 项错误码 (5001-5005)、超时/重试参数、Last-Event-Id 重连、降级策略（对齐 27-辅助功能审查 §问题 2 P1） |
| 2026-10-30 | 新增 十九~二十一章：通讯录扩展 API、群管理补充 API、日历补充 API（由 doc 24 迁移） |
