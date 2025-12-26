# RuoYi-IM 需求说明（内网高保密版）

本说明面向“催收公司/涉密场景”的仅 Web 页面内部通讯软件，强调合规可控、留痕审计与安全边界。本文档聚焦需求，去除安装、架构与实现细节。

## 项目定位与边界
- 定位：内网部署、仅企业内部使用的即时通讯系统，支持私聊与群聊，满足合规留痕与审计要求。
- 通信：浏览器 WebSocket 实时消息；HTTP 用于管理与历史查询。
- 边界：不对公网开放；不含移动端、第三方推送、WebRTC 音视频等功能。

## 角色与权限
- 普通员工（坐席/业务员）：内部沟通、文件受控分享、消息基础操作。
- 组长/主管：群管理、公告、禁言、违规上报、较长时间窗口的撤回。
- 审计员/合规岗：全量留痕检索、敏感词告警处置、导出取证（需审批）。
- 系统管理员：组织与账号、权限与策略、归档与备份、系统监控。

## 核心功能需求
- 账号与组织
  - 登录鉴权、强密码策略；组织/角色/权限管理（最小权限原则）。
  - 登录后下发令牌用于 WebSocket 鉴权；支持单点踢出与会话控制（可选）。
- 联系人与好友
  - 好友申请/通过/备注/黑名单；支持组织结构浏览与人员搜索。
- 会话与消息
  - 私聊与群聊；在线状态、输入中、已读/未读；消息撤回（时间窗口可配）。
  - 消息类型：文本、表情、引用/回复、转发、收藏、置顶；支持消息搜索与定位。
- 群组管理
  - 创建/解散/重命名/公告；群主/管理员/成员角色；禁言；入群审批（可选）。
- 文件收发（受控）
  - 白名单类型与大小限制；MD5 校验与秒传；下载鉴权与审计。
  - 预览水印（含用户/时间/IP），可配置禁止外链、时效与次数控制。
- 通知与提醒
  - 桌面提醒、@ 指定人提醒、免打扰时段；系统公告。
- 审计与归档
  - 敏感词（多库/分级）检测，命中记录与告警；违规会话快速定位。
  - 全量会话归档；归档规则（按人群/会话类型/保留时长/WORM 可选）。
  - 审计检索与导出：需审批流；导出留痕（申请人、审批人、用途、时间）。
- 管理后台
  - 策略配置：敏感词、撤回窗口、文件白名单、导出与群创建审批、保留周期。
  - 监控与告警：在线人数、消息失败/重试、WebSocket 心跳、Redis/MySQL 指标。

## 安全与合规要求
- 通信安全
  - 全站 HTTPS/WSS；WebSocket 连接携带令牌并校验，心跳与重放防护。
  - 关闭跨域或严格白名单；校验 Referer/Origin。
- 访问控制
  - RBAC 最小权限；关键操作二次确认与审批（导出、群创建、公告）。
  - IP/设备白名单（可选）；并发登录/多端限制（可选）。
- 数据安全
  - 存储加密（字段级 AES-GCM 可选）；密钥托管与轮换建议。
  - 敏感字段脱敏展示；附件下载短链、时效/次数限制与审计。
- 审计留痕
  - 用户操作日志、消息留痕、敏感词告警；导出全流程追踪与责任到人。
- 保留与合规
  - 数据保留周期（如 6/12/36 个月）策略化；Legal Hold 冻结指定会话（可选）。
  - 导出合规：去标识化/脱敏模板；审批与留痕必须。

## 非功能性要求
- 可用性：内网单体可用性 ≥ 99.5%；支持扩展到多实例（无状态 Web + Redis 会话）。
- 性能：≥ 1K 并发在线，峰值 ≥ 200 msg/s；消息落库 p95 < 100ms（内网）。
- 可靠性：消息投递至少一次；失败重试与补偿机制；失败消息可追踪。
- 可观测性：链路日志、指标、审计报表；异常报警（断连、积压、慢查询）。
- 备份与恢复：DB/文件定期备份（加密），演练恢复流程。

## 范围外（不提供）
- 第三方 IM 集成（钉钉/企业微信等）、移动端推送、WebRTC 音视频。
- 对公网开放与跨组织互通。

## 里程碑（建议）
- M1（上线门槛）：私聊/群聊、文件白名单、敏感词检测、撤回/已读、归档检索、审计导出（审批）、监控。
- M2：导出脱敏模板、群治理（创建/公告审批）、水印与下载时效控制、保留周期策略化。
- M3：字段级存储加密与密钥轮换、Legal Hold、端到端加密（需权衡与审计）。

## 当前实现状态 ✅

### 已完成功能
- ✅ **WebSocket 协议**：路径 `/websocket/{token}`，支持 token 鉴权
- ✅ **REST API 规范**：消息发送、撤回、已读等接口符合需求文档
- ✅ **敏感词检测**：集成敏感词检测，支持 WARN/BLOCK 级别处理
- ✅ **实体类修复**：解决了 Lombok 冲突和字段不匹配问题
- ✅ **编译通过**：所有代码可以正常编译

### 待完善功能
- 🔄 **配置项管理**：添加 im.* 配置项
- 🔄 **审计导出**：实现导出申请和审批流程
- 🔄 **文件处理**：水印、下载控制、白名单验证
- 🔄 **JWT 验证**：集成真正的 token 验证
- 🔄 **错误码标准化**：实现 IM- 前缀错误码

## 领域模型（约束 AI 生成的实体与表结构）
- User（复用 RuoYi 系统用户）
  - userId(Long)、username(String)、deptId(Long)、status、createdAt、updatedAt
- Friend
  - id(Long)、userId(Long)、friendUserId(Long)、alias(String)、remark(String)、status(ENUM: NORMAL|BLOCKED)、createdAt
- FriendRequest
  - id(Long)、fromUserId(Long)、toUserId(Long)、message(String)、status(ENUM: PENDING|APPROVED|REJECTED)、createdAt、handledAt
- Group
  - id(Long)、name(String 2..50)、ownerId(Long)、announcement(String 0..2000)、status(ENUM: NORMAL|DISMISSED)、createdAt、updatedAt
- GroupMember
  - id(Long)、groupId(Long)、userId(Long)、role(ENUM: OWNER|ADMIN|MEMBER)、muteUntil(Nullable Timestamp)、joinedAt
- Conversation
  - id(Long)、type(ENUM: PRIVATE|GROUP)、targetId(Long: friendUserId or groupId)、lastMessageId(Long)、updatedAt、createdAt
- ConversationMember
  - id(Long)、conversationId(Long)、userId(Long)、lastReadMessageId(Long)、pinned(Boolean)、muted(Boolean)
- Message
  - id(Long Snowflake)、conversationId(Long)、senderId(Long)、type(ENUM: TEXT|FILE|NOTICE|RECALL|REPLY|FORWARD)、
  - content(Text JSON for rich types)、replyToMessageId(Nullable Long)、
  - status(ENUM: SENT|DELIVERED|READ|REVOKED)、sensitiveLevel(ENUM: NONE|WARN|BLOCK)、
  - createdAt、revokedAt(Nullable)
- FileAsset
  - id(Long)、uploaderId(Long)、filename(String)、ext(String)、mime(String)、size(Long)、md5(String)、storagePath(String)、watermark(Boolean)、createdAt
- SensitiveWord
  - id(Long)、word(String)、level(ENUM: WARN|BLOCK)、category(String)、enabled(Boolean)、createdAt、updatedAt
- SensitiveEvent
  - id(Long)、userId(Long)、messageId(Long)、wordId(Long)、level、createdAt
- AuditExportRequest
  - id(Long)、requestedBy(Long)、reason(String)、scope(JSON: users/groups/time/keywords)、
  - status(ENUM: PENDING|APPROVED|REJECTED|DONE)、filePath(String)、createdAt、approvedBy(Nullable Long)、approvedAt(Nullable)

## REST API 规范（示例，全部走 JSON）
- 统一
  - 鉴权：JWT/Token 通过 Header `Authorization: Bearer <token>`；所有 /im/** 需要登录
  - 分页：`pageNum`、`pageSize`；时间范围参数：`beginTime`、`endTime` ISO8601
  - 返回：`{ code: 200|4xx|5xx, msg: string, data: any }`

- 联系人/好友
  - POST /im/friend/request/{friendId}
    - 返回：请求详情；幂等（重复申请返回相同 PENDING）
  - PUT /im/friend/request/{requestId}
    - Body: `{ action: "APPROVE"|"REJECT", remark?: string }`
  - DELETE /im/friend/{friendId}
  - GET /im/friend/list?keyword=
  - GET /im/friend/block/list

- 群组
  - POST /im/group  Body: `{ name, memberIds[], announcement? }`
  - PUT /im/group    Body: `{ id, name?, announcement? }`
  - DELETE /im/group/{id}
  - POST /im/group/member/{groupId} Body: `{ userIds[] }`
  - DELETE /im/group/member/{groupId} Body: `{ userIds[] }`
  - PUT /im/group/mute/{groupId} Body: `{ userId, muteUntil }`

- 会话与消息
  - GET /im/conversation/list
  - POST /im/message/send Body:
    ```json
    {
      "conversationId": 123,
      "type": "TEXT|FILE|REPLY|FORWARD",
      "content": {"text": "hi"},
      "replyToMessageId": null,
      "clientMsgId": "uuid-..."
    }
    ```
  - GET /im/message/{conversationId}/list?pageNum=1&pageSize=50&keyword=
  - PUT /im/message/recall/{messageId}
  - PUT /im/message/read/{conversationId} Body: `{ lastReadMessageId }`

- 文件
  - POST /im/file/upload  表单或分片策略；服务端校验白名单与大小
  - GET /im/file/download/{fileId}  鉴权短链、可配时效/次数
  - GET /im/file/preview/{fileId}   支持水印

- 审计/归档
  - GET /im/audit/search?userId=&groupId=&keyword=&beginTime=&endTime=
  - POST /im/audit/export  Body: `{ scope, reason }`（触发审批流）
  - PUT /im/audit/export/{id} Body: `{ action: APPROVE|REJECT }`
  - GET /im/audit/export/{id}/download

## WebSocket 协议（/websocket/{token}）
- 收发统一 Envelope
  ```json
  {
    "action": "message/send|message/read|typing/start|typing/stop|presence/subscribe|heartbeat",
    "payload": {"...": "..."},
    "clientMsgId": "uuid-optional",
    "serverMsgId": "snowflake-optional",
    "ts": 1710000000000
  }
  ```
- 关键动作
  - message/send: 与 REST `message/send` 一致，服务端回 `DELIVERED` 并下行给订阅者
  - message/read: 标记已读并广播给对端/群成员
  - typing/start|stop: 输入状态提示（限时 5s 自动过期）
  - presence/subscribe: 订阅在线状态
  - heartbeat: 客户端 30~60s 心跳；服务端空闲超时断开

## RBAC 权限矩阵（示例）
- EMPLOYEE: chat:view, chat:send, file:upload(white-list), msg:recall(self, within window)
- SUPERVISOR: group:manage, msg:recall(extended window), notice:publish
- AUDITOR: audit:search, audit:export:request, audit:export:download(approved)
- ADMIN: user:manage, policy:manage, system:monitor, audit:approve

## 策略与配置项（application.yml 建议）
```yaml
im:
  message:
    recallWindowSeconds: 120
  file:
    maxSizeMb: 20
    allowExt: [pdf, docx, xlsx, png, jpg, txt]
    enablePreviewWatermark: true
  sensitive:
    enabled: true
    onBlock: REJECT   # REJECT | MASK | WARN
  export:
    requireApproval: true
    linkExpireMinutes: 60
  security:
    corsAllowOrigins: []    # 为空代表不跨域
    ipWhitelist: []         # 可选
```

## 错误码（前缀 IM-）
- IM-401-01 未认证或令牌失效
- IM-403-01 无权限访问
- IM-400-01 非法文件类型或大小超限
- IM-409-01 敏感词拦截（BLOCK）
- IM-404-01 资源不存在（会话/消息/群）

## 验收标准（抽样）
- 发送文本消息后：
  - WebSocket 下行 200ms 内到达同会话在线成员；消息持久化成功且可在历史查询
- 撤回：在配置窗口内，撤回成功并对所有成员下行 RECALL 事件
- 敏感词：BLOCK 级别消息被拒绝，产生 SensitiveEvent
- 文件：非白名单/超限上传被拒；下载需鉴权且生成审计日志
- 审计导出：提交申请后进入 PENDING；审批通过后生成一次性下载链接并过期

## 前端页面与交互（与 `ruoyi-im-web` 对齐）
- 路由
  - `/im/chat` 会话与消息列表，支持搜索、高亮定位
  - `/im/group` 群管理（创建/成员/公告/禁言）
  - `/im/file` 文件列表与预览（水印）
  - `/im/audit` 审计检索与导出申请
  - `/im/archive` 归档与规则
  - `/im/settings` 策略配置（管理员）
- 组件要求
  - 输入框：@ 提醒、引用/回复、粘贴图片至文件上传
  - 消息项：状态徽标（已读/撤回/敏感告警）
  - 文件预览：叠加水印（用户名/时间/IP）

---

### 可复制给 AI 编程的提示词
```text
你是资深全栈工程师。请在现有仓库基础上实现“内网高保密 IM（仅 Web）”，严格遵守下列约束：

[技术/目录]
- 后端：Java Spring Boot + MyBatis-Plus + Redis + MySQL，遵循 RuoYi 体系；模块在 `ruoyi-im-server/ruoyi-im/`
- 前端：基于 `ruoyi-im-web`（Vue + Element UI）扩展页面与 API 调用
- 所有 IM 接口前缀 `/im`，返回 `{code,msg,data}`

[领域模型]
- 使用本文档“领域模型”部分的实体与字段；Message.id 使用 Snowflake；Conversation.type=PRIVATE|GROUP

[REST API]
- 按“REST API 规范”实现所列接口与请求/响应；分页参数 `pageNum/pageSize`
- 所有接口需登录；Header `Authorization: Bearer <token>`

[WebSocket]
- 路径 `/websocket/{token}`，统一 Envelope：`{action,payload,clientMsgId,serverMsgId,ts}`
- 支持动作：message/send, message/read, typing/start|stop, presence/subscribe, heartbeat
- 服务端需要广播、已读同步与心跳超时处理

[安全与策略]
- 文件白名单与大小限制；敏感词检测：WARN 记录、BLOCK 拒绝
- 撤回窗口 `im.message.recallWindowSeconds`；导出需审批，链接过期 `im.export.linkExpireMinutes`
- 关闭跨域或严格白名单；所有下载生成审计日志

[RBAC]
- 角色 EMPLOYEE/SUPERVISOR/AUDITOR/ADMIN，按“RBAC 权限矩阵”控制接口与按钮

[前端]
- 路由：/im/chat, /im/group, /im/file, /im/audit, /im/archive, /im/settings
- 组件：聊天输入支持回复/转发/@，消息状态徽标，文件预览水印

[错误码]
- 使用“错误码”章节；BLOCK 场景返回 IM-409-01

[验收]
- 实现“验收标准”用例（含 WebSocket 广播、撤回、敏感词、文件、导出审批）

输出要求：
1）后端 Controller/Service/Mapper 与实体/表结构；2）前端视图与调用；3）配置项与示例；4）关键单元/集成测试；5）必要迁移脚本（如缺失字段）
```