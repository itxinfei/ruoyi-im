# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 提供项目指导，所有对话使用中文。

## 项目概述

RuoYi-IM 是一个全栈企业级即时通讯系统：
- **后端**: Java 8, Spring Boot 2.7, WebSocket, MyBatis Plus, MySQL, Redis
- **前端**: Vue 3 (Composition API), Vite, Element Plus, Vuex, Vue Router
- **管理后台**: RuoYi v4.8.0，使用 Thymeleaf 和 Shiro

## 项目结构

```
im/
├── ruoyi-im-api/          # 核心API服务 (Spring Boot, 端口 8080)
│   └── src/main/java/com/ruoyi/im/
│       ├── controller/    # REST API 接口
│       ├── service/       # 业务逻辑
│       ├── mapper/        # MyBatis-Plus 数据访问层
│       ├── domain/        # 实体类
│       ├── dto/           # 数据传输对象
│       ├── vo/            # 视图对象
│       ├── config/        # Spring 配置 (WebSocket, Security, Redis)
│       ├── websocket/     # WebSocket 端点
│       ├── utils/         # 工具类
│       └── ImApplication.java  # 主入口
├── ruoyi-im-web/          # 前端聊天界面 (Vue 3 + Vite, 端口 3000)
│   └── src/
│       ├── api/           # API 客户端调用
│       ├── components/    # Vue 组件
│       ├── composables/   # 组合式函数
│       ├── views/         # 页面视图
│       ├── store/         # Vuex 状态管理
│       ├── router/        # Vue Router 配置
│       ├── utils/         # 工具函数
│       └── main.js        # Vue 应用入口
├── ruoyi-im-admin/        # 管理后台 (RuoYi 框架, 端口 8081)
├── sql/                   # 数据库结构文件
├── docs/                  # 项目文档
└── pom.xml               # 父级 Maven POM
```

## 开发命令

### 前端 (ruoyi-im-web)
```bash
cd ruoyi-im-web
npm install              # 安装依赖
npm run dev             # 启动开发服务器 (端口 3000)
npm run build           # 生产环境构建
```

### 后端 (ruoyi-im-api)
```bash
cd ruoyi-im-api
mvn clean package       # 构建 JAR 文件
# 运行 ImApplication.java 的 main 方法启动服务器 (端口 8080)
```

## IM 系统架构

### 消息发送流程

```
┌─────────┐     WebSocket      ┌─────────┐
│         │ ───────────────────> │         │
│  前端   │                      │  后端   │
│         │ <─────────────────── │         │
└─────────┘     实时推送         └─────────┘
     │                               │
     │ 1. 用户发送消息               │
     │                               │
     ├─> 乐观UI更新 (立即显示)        │
     │                               │
     ├─> WebSocket.send()            │
     │                               │
     │                          ┌────▼─────┐
     │                          │保存到DB  │
     │                          └────┬─────┘
     │                               │
     │                          ┌────▼─────┐
     │                          │推送给其他 │
     │                          │成员(WS)  │
     │                          └──────────┘
```

**关键点**：
- 前端优先使用 WebSocket 发送，失败时降级到 REST API
- 采用乐观 UI 更新，发送后立即显示消息
- 后端保存消息后通过 WebSocket 推送给其他会话成员（不推送给发送者自己）

### 消息接收流程

```
┌─────────────────────────────────────────────┐
│                  Vuex Store                   │
│  ┌─────────────────────────────────────┐   │
│  │  messageList: {                      │   │
│  │    sessionId1: [msg1, msg2, ...],   │   │
│  │    sessionId2: [msg3, msg4, ...]    │   │
│  │  }                                   │   │
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
                    ▲
                    │ WebSocket.onmessage
                    │
┌─────────────────┴─────────────────────────┐
│          WebSocket 客户端                   │
└─────────────────┬─────────────────────────┘
                  │
    ┌─────────────┴─────────────┐
    │                           │
┌───▼────┐                  ┌────▼────┐
│ 后端   │                  │  Redis  │
│WebSocket│◄───────────────►│ Pub/Sub │
└───┬────┘                  └─────────┘
    │
    │ 消息广播
    ▼
┌─────────────┐
│ 会话成员表  │
└─────────────┘
```

### 会话管理

**会话类型**：
- `PRIVATE` - 私聊会话（一对一）
- `GROUP` - 群聊会话

**会话成员表 (im_conversation_member)**：
- 存储用户与会话的关系
- 记录未读数、置顶、免打扰等状态
- 唯一约束：`(conversation_id, user_id)`

**去重机制**：
- 后端 `getUserConversations()` 按 `peerUserId` 去重私聊会话
- 前端 Vuex `SET_SESSIONS` 双重去重（按 id 和 peerId）

### 在线状态管理

**WebSocket 心跳机制**：
- 前端每 30 秒发送 `ping` 消息
- 后端响应 `pong` 消息
- 连接断开时自动重连（指数退避）

**在线状态**：
- 连接 WebSocket = 在线
- 断开 WebSocket = 离线
- 最后在线时间存储在 `im_user.last_online_time`

## 核心模块说明

### 后端 Controller

| Controller | 路径 | 功能 |
|-----------|------|------|
| `ImMessageController` | `/api/im/message` | 消息发送、查询、撤回、编辑 |
| `ImConversationController` | `/api/im/conversation` | 会话管理 |
| `ImContactController` | `/api/im/contact` | 好友/联系人管理 |
| `ImUserController` | `/api/im/user` | 用户管理 |
| `ImGroupController` | `/api/im/group` | 群组管理 |
| `ImSessionController` | `/api/im/session` | 会话列表 |

### 后端 Service

| Service | 功能 |
|---------|------|
| `ImMessageService` | 消息业务逻辑 |
| `ImConversationService` | 会话业务逻辑 |
| `ImFriendService` | 好友关系管理 |
| `ImGroupService` | 群组管理 |
| `ImUserService` | 用户管理 |

### 前端 Vuex Store

| Module | State | 功能 |
|--------|-------|------|
| `im` | `sessions`, `messageList`, `currentSession` | 会话和消息管理 |
| `user` | `userInfo`, `token` | 用户认证信息 |
| `websocket` | `isConnected` | WebSocket 连接状态 |

### WebSocket 消息类型

| 类型 | 方向 | 说明 |
|------|------|------|
| `auth` | 客户端→服务端 | 认证 |
| `message` | 双向 | 聊天消息 |
| `ping/pong` | 双向 | 心跳 |
| `read` | 客户端→服务端 | 已读回执 |
| `typing` | 客户端→服务端 | 输入状态 |

## 数据库设计

### 核心表结构

**im_conversation (会话表)**
```sql
id              BIGINT          主键
type            VARCHAR(20)     类型: PRIVATE/GROUP
target_id       BIGINT          目标ID (私聊=对方用户ID, 群聊=群组ID)
name            VARCHAR(100)    会话名称
last_message_id BIGINT          最后消息ID
last_message_time DATETIME      最后消息时间
```

**im_conversation_member (会话成员表)**
```sql
conversation_id BIGINT          会话ID
user_id         BIGINT          用户ID
role            VARCHAR(20)     角色: OWNER/MEMBER
unread_count    INT             未读数
is_pinned       TINYINT         是否置顶
is_muted        TINYINT         是否免打扰
last_read_message_id BIGINT     最后已读消息ID
-- 唯一约束: uk_conversation_user (conversation_id, user_id)
```

**im_message (消息表)**
```sql
id                  BIGINT      主键
conversation_id     BIGINT      会话ID
sender_id           BIGINT      发送者ID
message_type        VARCHAR(20) 消息类型: TEXT/IMAGE/FILE/VOICE/VIDEO
content             TEXT        消息内容 (JSON格式)
reply_to_message_id BIGINT      回复的消息ID
forward_from_message_id BIGINT  转发来源消息ID
is_revoked          TINYINT     是否撤回
is_edited           TINYINT     是否已编辑
is_deleted          TINYINT     是否删除
```

**im_friend (好友关系表)**
```sql
id              BIGINT      主键
user_id         BIGINT      用户ID
friend_id       BIGINT      好友ID
remark          VARCHAR(100) 备注
group_name      VARCHAR(50) 分组名称
is_deleted      TINYINT     是否删除 (软删除)
```

### 字段命名规范

- 数据库: `snake_case` (如: `conversation_id`)
- Java Entity: `camelCase` (如: `conversationId`)
- 前端 API: `camelCase` (如: `conversationId`)
- 前端组件: 混合使用 `sessionId` 和 `conversationId` (需要统一)

## 已知问题与解决方案

### 1. 会话重复问题

**问题**: 同一对用户之间可能存在多条会话记录

**解决方案**:
- 后端: `ImConversationServiceImpl.getUserConversations()` 按 `peerUserId` 去重
- 前端: Vuex `SET_SESSIONS` mutation 双重去重

### 2. 好友重复问题

**问题**: 好友关系表中可能存在重复记录

**解决方案**:
- SQL: `ImFriendMapper.xml` 使用子查询去重
- Service: `handleFriendRequest()` 检查已存在关系
- 前端: `loadFriends()` 按 `friendId` 去重

### 3. 消息重复发送

**问题**: WebSocket 和 REST API 并行发送可能导致重复

**解决方案**:
- 前端: 统一使用 WebSocket 发送，失败时降级到 REST API
- 后端: 只通过 WebSocket 端点广播消息

### 4. 字段命名不一致

**问题**: `sessionId` vs `conversationId` 混用

**解决方案**:
- 统一使用 `conversationId` 表示会话ID
- 前端组件中做兼容处理

## 重要约束

### 数据库字段变更规则
⚠️ **数据库结构是单一事实来源，任何字段变更必须先确认数据库表结构**

1. 修改表结构前，必须先查看数据库表结构
2. 变更数据库后，需要同步更新:
   - Entity 实体类 (`domain/*.java`)
   - Mapper XML (`resources/mapper/*.xml`)
   - API 接口返回字段
3. 所有字段命名遵循: 数据库 `snake_case` ↔ Java `camelCase` 自动转换

### 代码规范
- Entity 类使用 Lombok 注解
- 避免使用 `select *`
- WebSocket 消息使用统一格式
- 前端使用 Composition API 和 `<script setup>` 语法
