---
stepsCompleted: [1, 2, 3, 4, 5]
inputDocuments:
  - docs/需求文档.md
  - docs/项目总规划-钉钉对齐V1.md
  - docs/P0执行任务卡-第一批.md
  - docs/plans/2026-03-04-P0-执行排期与里程碑.md
  - docs/钉钉聊天功能差距分析.md
workflowType: 'architecture'
project_name: 'im'
user_name: 'Itxinfei'
date: '2026-03-04'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**

核心功能域：
1. **消息域** - 会话列表与筛选、消息收发（文本/图片/文件/语音/视频）、消息操作（撤回/编辑/转发/回复/收藏）、二级菜单筛选、三级弹窗交互
2. **通讯录域** - 组织架构、我的好友、群组管理、最近联系人
3. **工作台域** - 待办、审批、日程、公告、文档入口

**Non-Functional Requirements:**

| 类别 | 要求 |
|------|------|
| 性能 | 100并发（峰值500），消息延迟P95 ≤ 300ms，首屏加载 ≤ 3s |
| 安全 | JWT鉴权、RBAC权限、敏感词过滤、AES-256-GCM消息加密 |
| 稳定性 | WebSocket断线重连 ≥ 99%，发送失败重试 ≥ 95% |
| 部署 | 内网部署、HTTPS/WSS、操作审计 |

**Scale & Complexity:**

- Primary domain: 全栈Web应用（前端Vue3 + 后端Spring Boot）
- Complexity level: 中等偏高
- Estimated architectural components: 
  - 前端：~50个Vue组件、~10个Store模块
  - 后端：~20个Controller、~30个Service、~40个Mapper

### Technical Constraints & Dependencies

**强制约束：**
- JDK 1.8（禁止Java 9+语法）
- Vue 3 + Composition API
- MySQL 5.7
- Redis 6+
- WebRTC（浏览器标准能力）
- 开发环境：Windows 11
- 生产环境：CentOS 7

**依赖服务：**
- 文件存储服务
- WebSocket服务
- WebRTC信令服务
- 敏感词过滤服务

### Cross-Cutting Concerns Identified

1. **消息状态管理** - 统一的状态机设计，覆盖发送/已读/撤回等状态
2. **实时通讯** - WebSocket连接池管理、心跳检测、断线重连
3. **权限控制** - RBAC模型、组织架构权限继承、群组权限
4. **错误处理** - 统一异常处理、失败重试策略、用户友好提示
5. **审计日志** - 操作追踪、安全审计、合规要求
6. **数据一致性** - 前后端字段映射、数据库与Mapper一致性

---

## UI/UX Architecture Decisions

### 1. 左侧导航栏设计规范

**设计目标：** 对齐钉钉桌面端左侧导航栏设计语言

**视觉规范：**

| 属性 | 钉钉标准 | 说明 |
|------|----------|------|
| 宽度 | 64px | 从当前52px调整，符合人体工学 |
| 菜单项高度 | 48px | 触摸友好，避免误触 |
| 图标尺寸 | 24×24px | 清晰可辨 |
| 图标线宽 | 1.5px | 精致现代感 |
| 选中背景 | #1677FF | 品牌蓝色 |
| 默认图标色 | #646A73 | 中性灰色 |
| 悬停背景 | #E8E8E8 | 微妙交互反馈 |
| 导航背景 | #F5F6F7 | 浅灰底色 |

**菜单项设计：**

```
┌────────────────┐
│                │
│   💬 消息      │ ← 选中态（蓝色背景+白色图标）
│                │
│   👥 通讯录    │
│                │
│   📋 工作台    │
│                │
│   📅 日程      │
│                │
│   ────────     │
│                │
│   👤 头像      │ ← 底部用户区域
│   ⚙️ 设置      │
│                │
└────────────────┘
```

**交互规范：**
- 选中态：蓝色圆角矩形背景（border-radius: 8px）+ 白色图标
- 悬停态：浅灰背景 + Tooltip 显示菜单名称
- 未读提示：消息图标右上角红色徽章（数字或圆点）
- 在线状态：用户头像右下角绿色圆点（8px）

**组件架构：**

```
src/components/ImSideNav/
├── index.vue              # 主容器
├── NavItem.vue            # 菜单项组件
├── NavIcon/               # SVG图标组件集
│   ├── MessageIcon.vue
│   ├── ContactsIcon.vue
│   ├── WorkbenchIcon.vue
│   ├── CalendarIcon.vue
│   └── SettingsIcon.vue
└── UserArea.vue           # 底部用户区域
```

---

### 2. 消息功能模块设计规范

#### 2.1 会话列表（SessionPanel）

**二级菜单筛选：**

```
┌─────────────────────────────────────────────────┐
│  消息                                           │
├─────────────────────────────────────────────────┤
│  [全部] [未读] [置顶] [免打扰] [群聊] [文件链接] │
└─────────────────────────────────────────────────┘
```

| 筛选项 | 筛选逻辑 | 徽章显示 |
|--------|----------|----------|
| 全部 | 显示所有会话 | 无 |
| 未读 | 仅显示有未读消息的会话 | 显示未读总数 |
| 置顶 | 仅显示置顶会话 | 无 |
| 免打扰 | 仅显示免打扰会话 | 无 |
| 群聊 | 仅显示群组会话 | 无 |
| 文件链接 | 显示包含文件/链接的会话 | 无 |

**会话卡片设计：**

```
┌─────────────────────────────────────────────┐
│  ┌────┐  会话名称              14:30       │
│  │头像│  [消息预览内容...]      🔴 3       │
│  └────┘  📌 🔇                          │
└─────────────────────────────────────────────┘

尺寸规范：
• 头像：40×40px，圆角8px
• 名称：15px 粗体，#1F2329
• 预览：14px 常规，#646A73，单行截断
• 时间：12px 常规，#8F959E，右对齐
• 未读徽章：红色圆点或数字
• 状态图标：置顶📌 免打扰🔇
```

**群组头像拼接算法：**

| 成员数 | 布局 | 说明 |
|--------|------|------|
| 1人 | 单头像 | 显示对方头像 |
| 2人 | 左右各1 | 两个头像左右排列 |
| 3人 | 左1右2 | 左侧1个，右侧上下2个 |
| 4人 | 2×2四宫格 | 四个头像均匀分布 |
| >4人 | 默认群图标 | 显示群组默认图标 |

**排序规则：**
1. 置顶会话优先（按置顶时间倒序）
2. 未读会话次之（按最后消息时间倒序）
3. 普通会话最后（按最后消息时间倒序）

#### 2.2 聊天区域（ChatPanel）

**三段式布局：**

```
┌─────────────────────────────────────────────┐
│  ChatHeader                                 │
│  会话名称        📞 📹 🔍 ⋯               │
│  成员数/在线状态                             │
├─────────────────────────────────────────────┤
│  MessageList                                │
│  ── 今天 ──                                 │
│                                             │
│  ┌────┐ 你好！                              │
│  │头像│ 10:30                               │
│  └────┘                                     │
│                                             │
│              收到了！  ┌────┐               │
│              10:31    │头像│               │
│                       └────┘               │
├─────────────────────────────────────────────┤
│  MessageInput                               │
│  😀 📎 📷 🎤 📁 @                          │
│  ┌─────────────────────────────────────┐   │
│  │ 输入消息...                          │   │
│  └─────────────────────────────────────┘   │
│                              [发送]         │
└─────────────────────────────────────────────┘
```

**ChatHeader 功能：**

| 功能 | 图标 | 行为 |
|------|------|------|
| 语音通话 | 📞 | 发起语音通话 |
| 视频通话 | 📹 | 发起视频通话 |
| 搜索 | 🔍 | 搜索会话内消息 |
| 更多 | ⋯ | 下拉菜单：置顶、免打扰、清空消息、删除会话 |

**MessageBubble 设计：**

```
对方消息气泡：                    自己消息气泡：
┌──────────────────┐              ┌──────────────────┐
│ 消息内容         │              │ 消息内容         │
│                  │              │                  │
└──────────────────┘              └──────────────────┘ ✓✓
圆角：2px 12px 12px 2px           圆角：12px 12px 2px 12px
背景：#F5F6F7                     背景：#1677FF（蓝色）
文字：#1F2329                     文字：#FFFFFF（白色）

悬停操作栏：
[回复] [转发] [收藏] [多选] [撤回/删除]
```

**消息状态：**

| 状态 | 图标 | 说明 |
|------|------|------|
| 发送中 | ⏳ 旋转动画 | 消息正在发送 |
| 发送成功 | ✓ | 单灰勾，已送达服务器 |
| 已读 | ✓✓ | 双蓝勾，对方已阅读 |
| 发送失败 | ❌ | 红色感叹号，点击重试 |

**MessageInput 工具栏：**

| 工具 | 图标 | 功能 |
|------|------|------|
| 表情 | 😀 | 表情选择器 |
| 附件 | 📎 | 文件选择上传 |
| 图片 | 📷 | 图片选择上传 |
| 语音 | 🎤 | 语音消息录制 |
| 文件 | 📁 | 文件选择上传 |
| @成员 | @ | 群聊@成员选择 |

**输入行为：**
- Enter 发送消息
- Shift+Enter 换行
- 支持拖拽文件到输入区上传
- 支持 Ctrl+V 粘贴图片上传

#### 2.3 状态管理架构

**消息状态机：**

```
┌─────────┐    发送    ┌─────────┐    成功    ┌─────────┐
│  编辑中  │ ────────> │  发送中  │ ────────> │  已发送  │
└─────────┘           └─────────┘           └─────────┘
                           │                     │
                           │ 失败                │ 对方已读
                           ↓                     ↓
                      ┌─────────┐           ┌─────────┐
                      │  失败   │           │  已读   │
                      └─────────┘           └─────────┘
                           │
                           │ 重试
                           ↓
                      返回发送中
```

**Store 模块设计：**

```javascript
// store/modules/im-session.js
state: {
  sessions: [],           // 会话列表
  activeSessionId: null,  // 当前激活会话
  filters: {
    type: 'all',          // 筛选类型
    keyword: ''           // 搜索关键词
  }
}

// store/modules/im-message.js
state: {
  messages: {},           // 消息字典 { sessionId: [messages] }
  sendingQueue: [],       // 发送中队列
  failedQueue: []         // 失败队列（待重试）
}
```

---

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (已确定):**
- 后端框架：Spring Boot 2.7.18 + JDK 1.8
- 前端框架：Vue 3.3.11 + Composition API
- 数据库：MySQL 8.0.33
- 缓存：Redis 6+
- ORM：MyBatis Plus 3.5.2
- UI组件库：Element Plus 2.4.4
- 状态管理：Vuex 4.1.0
- 构建工具：Vite 5.0.7

**Important Decisions (基于现有代码):**
- 认证方案：JWT (jjwt 0.11.5)
- 实时通讯：WebSocket (Spring WebSocket)
- 连接池：Druid 1.2.23
- 敏感词过滤：sensitive-word 0.25.0
- API文档：SpringDoc OpenAPI 1.7.0

---

### Data Architecture

**数据库设计规范：**

| 表名 | 用途 | 关键字段 |
|------|------|----------|
| `im_conversation` | 会话表 | id, type, target_id, name, avatar |
| `im_message` | 消息表 | id, conversation_id, sender_id, message_type, content |
| `im_user` | 用户表 | id, username, password, avatar, status |
| `im_user_session` | 用户会话状态 | user_id, online_status, last_active_time |
| `im_group` | 群组表 | id, name, avatar, owner_id |
| `im_group_member` | 群成员表 | group_id, user_id, role |
| `im_friend` | 好友关系表 | user_id, friend_id, status |

**消息类型枚举：**

```java
public enum MessageType {
    TEXT,       // 文本消息
    IMAGE,      // 图片消息
    FILE,       // 文件消息
    VOICE,      // 语音消息
    VIDEO,      // 视频消息
    SYSTEM,     // 系统消息
    LINK        // 链接卡片
}
```

**消息状态枚举：**

```java
public enum MessageStatus {
    PENDING(0),     // 发送中
    DELIVERED(1),   // 已送达
    READ(2),        // 已读
    FAILED(3),      // 发送失败
    REVOKED(5)      // 已撤回
}
```

**缓存策略：**

| 缓存类型 | Redis Key | 过期时间 | 说明 |
|----------|-----------|----------|------|
| 在线用户 | `im:online:{userId}` | 5分钟 | 用户在线状态 |
| 会话信息 | `im:conversation:{id}` | 30分钟 | 会话详情缓存 |
| 用户信息 | `im:user:{id}` | 1小时 | 用户基本信息 |
| 未读消息 | `im:unread:{userId}` | 永久 | 未读消息计数 |

---

### Authentication & Security

**认证流程：**

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   登录请求   │ ──> │  验证用户   │ ──> │  生成JWT    │
└─────────────┘     └─────────────┘     └─────────────┘
                                              │
                                              ↓
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   访问资源   │ <── │  携带Token  │ <── │  返回Token  │
└─────────────┘     └─────────────┘     └─────────────┘
       │
       ↓
┌─────────────┐     ┌─────────────┐
│  JWT验证    │ ──> │  权限检查   │
└─────────────┘     └─────────────┘
```

**JWT配置：**

```yaml
# application.yml
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 86400000  # 24小时
  header: Authorization
  prefix: "Bearer "
```

**权限角色：**

| 角色 | 权限范围 |
|------|----------|
| `ROLE_USER` | 普通用户，访问IM功能 |
| `ROLE_ADMIN` | 管理员，访问管理接口 |
| `ROLE_SUPER_ADMIN` | 超级管理员，全部权限 |

**安全配置要点：**

1. **密码加密**：BCryptPasswordEncoder
2. **CSRF保护**：禁用（前后端分离）
3. **CORS配置**：允许指定域名跨域
4. **接口权限**：
   - `/api/auth/**` - 公开访问
   - `/api/im/**` - 需要认证
   - `/api/admin/**` - 需要管理员权限

---

### API & Communication Patterns

**REST API 设计规范：**

```
基础路径: /api/im

会话接口:
GET    /conversation/list           # 获取会话列表
GET    /conversation/{id}           # 获取会话详情
PUT    /conversation/{id}/pin       # 置顶会话
PUT    /conversation/{id}/mute      # 免打扰
DELETE /conversation/{id}           # 删除会话

消息接口:
GET    /message/list/{conversationId}  # 获取消息列表
POST   /message/send                 # 发送消息
PUT    /message/{id}/recall          # 撤回消息
PUT    /message/{id}/edit            # 编辑消息
DELETE /message/{id}                 # 删除消息

用户接口:
GET    /user/info                   # 获取当前用户信息
PUT    /user/info                   # 更新用户信息
GET    /user/online/{userId}        # 获取用户在线状态
```

**统一响应格式：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { },
  "timestamp": 1709529600000
}
```

**WebSocket 消息协议：**

```json
// 客户端 -> 服务端
{
  "type": "message",
  "data": {
    "conversationId": 123,
    "type": "TEXT",
    "content": "你好",
    "clientMsgId": "uuid-xxx"
  }
}

// 服务端 -> 客户端 (ACK)
{
  "type": "message_ack",
  "clientMsgId": "uuid-xxx",
  "messageId": 456,
  "timestamp": 1709529600000
}

// 心跳
{
  "type": "ping"
}
// 响应
{
  "type": "pong"
}
```

**WebSocket 消息类型：**

| 类型 | 方向 | 说明 |
|------|------|------|
| `auth` | C→S | 认证消息 |
| `message` | C→S | 发送消息 |
| `message_ack` | S→C | 消息确认 |
| `message_error` | S→C | 消息错误 |
| `typing` | 双向 | 输入状态 |
| `read` | C→S | 已读回执 |
| `call` | 双向 | 通话信令 |
| `ping/pong` | 双向 | 心跳 |

---

### Frontend Architecture

**状态管理架构：**

```javascript
// Vuex Store 模块划分
store/
├── index.js              # Store 入口
└── modules/
    ├── user.js           # 用户状态（登录信息、个人资料）
    ├── im-session.js     # 会话状态（会话列表、当前会话、筛选）
    ├── im-message.js     # 消息状态（消息列表、发送状态）
    ├── im-contact.js     # 通讯录状态（联系人、群组）
    └── im.js             # IM全局状态（WebSocket连接）
```

**核心 Store 状态：**

```javascript
// im-session.js
state: {
  sessions: [],           // 会话列表
  currentSession: null,   // 当前选中会话
  totalUnreadCount: 0,    // 未读总数
  currentFilter: 'all'    // 当前筛选类型
}

// im-message.js
state: {
  messages: {},           // 消息字典 { sessionId: [messages] }
  sendingQueue: [],       // 发送中队列
  failedQueue: []         // 失败队列
}
```

**组件通信模式：**

```
┌─────────────────────────────────────────────────────┐
│                    MainPage.vue                     │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │
│  │ ImSideNav    │  │ SessionPanel │  │ ChatPanel │ │
│  │              │  │              │  │           │ │
│  │  菜单切换    │  │  会话选择    │  │  消息收发 │ │
│  │    ↓         │  │    ↓         │  │    ↓      │ │
│  └──────────────┘  └──────────────┘  └───────────┘ │
│         │                 │                │       │
│         └─────────────────┼────────────────┘       │
│                           ↓                        │
│                    Vuex Store                       │
│              (im-session, im-message)              │
└─────────────────────────────────────────────────────┘
```

**API 请求封装：**

```javascript
// api/request.js
// Axios 实例配置
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

// 请求拦截器：自动添加 Token 和 userId
service.interceptors.request.use(config => {
  const token = localStorage.getItem('im_token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理
service.interceptors.response.use(
  response => response.data,
  error => {
    // 401: 跳转登录
    // 其他错误: 显示提示
  }
)
```

---

### Infrastructure & Deployment

**部署架构：**

```
┌─────────────────────────────────────────────────────┐
│                    生产环境                          │
│                    CentOS 7                          │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─────────────┐     ┌─────────────┐               │
│  │   Nginx     │────>│ Spring Boot │               │
│  │  (反向代理)  │     │   (8080)    │               │
│  └─────────────┘     └─────────────┘               │
│        │                    │                       │
│        │              ┌─────┴─────┐                 │
│        │              │           │                 │
│        │         ┌────┴────┐ ┌────┴────┐           │
│        │         │  MySQL  │ │  Redis  │           │
│        │         │  (3306) │ │ (6379)  │           │
│        │         └─────────┘ └─────────┘           │
│        │                                           │
│        └──────> 静态资源 (Vue构建产物)              │
│                                                     │
└─────────────────────────────────────────────────────┘
```

**环境配置：**

```yaml
# application-prod.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/im?useSSL=false
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}

# 安全配置
app:
  security:
    enabled: true
```

**CI/CD 建议：**

```yaml
# .gitlab-ci.yml 或 GitHub Actions
stages:
  - build
  - test
  - deploy

build-frontend:
  stage: build
  script:
    - cd ruoyi-im-web
    - npm install
    - npm run build
  artifacts:
    paths:
      - ruoyi-im-web/dist/

build-backend:
  stage: build
  script:
    - cd ruoyi-im-api
    - mvn clean package -DskipTests
  artifacts:
    paths:
      - ruoyi-im-api/target/*.jar

deploy:
  stage: deploy
  script:
    - scp ruoyi-im-api/target/ruoyi-im-api.jar user@server:/app/
    - scp -r ruoyi-im-web/dist/* user@server:/var/www/html/
    - ssh user@server "systemctl restart im-api"
```

---

### Decision Impact Analysis

**实现顺序：**

1. **数据层** - 数据库表结构、Mapper、Entity
2. **服务层** - Service 接口与实现
3. **控制层** - Controller、API 接口
4. **前端状态** - Vuex Store 模块
5. **前端组件** - Vue 组件实现
6. **实时通讯** - WebSocket 集成
7. **测试验证** - 单元测试、集成测试

**跨组件依赖：**

```
数据库表结构
    ↓
Entity/Domain
    ↓
Mapper (MyBatis Plus)
    ↓
Service
    ↓
Controller (REST API)
    ↓
前端 API 封装
    ↓
Vuex Store
    ↓
Vue 组件
```

---

## Implementation Patterns & Consistency Rules

### Pattern Categories Defined

**Critical Conflict Points Identified:** 6个主要冲突区域

---

### Naming Patterns

#### Database Naming Conventions

| 类型 | 规则 | 示例 |
|------|------|------|
| 表名 | `im_` 前缀 + snake_case | `im_conversation`, `im_message`, `im_user` |
| 列名 | snake_case | `user_id`, `conversation_id`, `message_type` |
| 主键 | `id` (自增) | `id BIGINT AUTO_INCREMENT` |
| 外键 | `{table}_id` | `conversation_id`, `sender_id` |
| 索引 | `idx_{table}_{columns}` | `idx_message_conversation_time` |
| 布尔字段 | `is_{action}` | `is_deleted`, `is_revoked`, `is_pinned` |

#### API Naming Conventions

| 类型 | 规则 | 示例 |
|------|------|------|
| REST路径 | 复数名词 | `/api/im/conversations`, `/api/im/messages` |
| 路径参数 | camelCase | `/{conversationId}`, `/{messageId}` |
| 查询参数 | camelCase | `?lastId=xxx&limit=20` |
| HTTP方法 | 标准REST | GET查询, POST创建, PUT更新, DELETE删除 |

**API路径规范：**

```
# 正确示例
GET    /api/im/conversations           # 获取会话列表
GET    /api/im/conversations/{id}      # 获取单个会话
POST   /api/im/messages                # 发送消息
PUT    /api/im/messages/{id}/recall    # 撤回消息

# 错误示例
GET    /api/im/getConversationList     # ❌ 不使用动词前缀
GET    /api/im/conversation            # ❌ 使用复数
POST   /api/im/message/send            # ❌ 资源路径不含动词
```

#### Code Naming Conventions

| 类型 | 规则 | 示例 |
|------|------|------|
| Java类 | PascalCase | `ImConversationService`, `ImMessageController` |
| Java方法 | camelCase | `getConversationById()`, `sendMessage()` |
| Java常量 | UPPER_SNAKE | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| Vue组件 | PascalCase | `ChatPanel.vue`, `MessageBubble.vue` |
| Vue文件 | PascalCase | `ChatHeader.vue`, `SessionItem.vue` |
| Vuex模块 | kebab-case | `im-session.js`, `im-message.js` |
| CSS类 | kebab-case | `.chat-panel`, `.message-bubble` |
| 变量 | camelCase | `conversationId`, `messageList` |

---

### Structure Patterns

#### Project Organization

**后端项目结构：**

```
ruoyi-im-api/
├── src/main/java/com/ruoyi/im/
│   ├── controller/           # REST控制器
│   │   └── admin/            # 管理端控制器
│   ├── service/              # 业务服务接口
│   │   └── impl/             # 服务实现
│   ├── mapper/               # MyBatis Mapper接口
│   ├── domain/               # 实体类（数据库映射）
│   ├── vo/                   # 视图对象（响应）
│   ├── dto/                  # 数据传输对象（请求）
│   ├── config/               # 配置类
│   ├── security/             # 安全相关
│   ├── websocket/            # WebSocket端点
│   ├── util/                 # 工具类
│   ├── constant/             # 常量定义
│   └── aspect/               # AOP切面
│
├── src/main/resources/
│   ├── mapper/               # MyBatis XML映射文件
│   ├── application.yml       # 主配置
│   └── application-prod.yml  # 生产配置
│
└── src/test/                 # 测试代码
```

**前端项目结构：**

```
ruoyi-im-web/
├── src/
│   ├── views/                # 页面组件
│   ├── components/           # 可复用组件
│   │   ├── Chat/             # 聊天相关组件
│   │   ├── Contacts/         # 通讯录组件
│   │   └── Common/           # 通用组件
│   ├── store/                # Vuex状态管理
│   │   └── modules/          # 状态模块
│   ├── api/                  # API请求封装
│   │   └── im/               # IM相关API
│   ├── router/               # 路由配置
│   ├── utils/                # 工具函数
│   ├── styles/               # 全局样式
│   └── assets/               # 静态资源
│
└── public/                   # 公共资源
```

#### File Naming Rules

| 文件类型 | 命名规则 | 示例 |
|----------|----------|------|
| Vue组件 | PascalCase | `ChatPanel.vue` |
| Vuex模块 | kebab-case | `im-session.js` |
| API模块 | camelCase | `conversation.js` |
| 工具函数 | camelCase | `message.js` |
| 样式文件 | kebab-case | `chat-panel.scss` |
| 测试文件 | 同名+.test | `ChatPanel.test.js` |

---

### Format Patterns

#### API Response Format

**统一响应结构：**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { },
  "timestamp": 1709529600000
}
```

**状态码定义：**

| code | 含义 | 使用场景 |
|------|------|----------|
| 200 | 成功 | 请求成功 |
| 400 | 参数错误 | 请求参数验证失败 |
| 401 | 未授权 | Token无效或过期 |
| 403 | 禁止访问 | 无权限 |
| 404 | 未找到 | 资源不存在 |
| 500 | 服务器错误 | 内部异常 |

**错误响应格式：**

```json
{
  "code": 400,
  "msg": "参数验证失败",
  "data": {
    "errors": [
      { "field": "content", "message": "消息内容不能为空" }
    ]
  },
  "timestamp": 1709529600000
}
```

#### Data Exchange Formats

**日期时间格式：**

| 场景 | 格式 | 示例 |
|------|------|------|
| 后端存储 | LocalDateTime | `2026-03-04T14:30:00` |
| API传输 | 字符串 | `"2026-03-04 14:30:00"` |
| 前端显示 | 格式化 | `今天 14:30` |
| 时间戳 | 毫秒 | `1709529600000` |

**JSON字段命名：**

| 场景 | 规则 | 示例 |
|------|------|------|
| API请求/响应 | camelCase | `conversationId`, `messageType` |
| 数据库映射 | snake_case | `conversation_id`, `message_type` |
| WebSocket消息 | camelCase | `clientMsgId`, `sendTime` |

**布尔值表示：**

| 场景 | 表示方式 | 示例 |
|------|----------|------|
| 数据库 | TINYINT(0/1) | `is_deleted = 1` |
| Java | Boolean | `Boolean isDeleted` |
| JSON | boolean | `"isDeleted": true` |

---

### Communication Patterns

#### WebSocket Message Format

**消息结构：**

```json
{
  "type": "message",
  "data": {
    "conversationId": 123,
    "type": "TEXT",
    "content": "你好",
    "clientMsgId": "uuid-xxx"
  }
}
```

**消息类型定义：**

| type | 方向 | 用途 |
|------|------|------|
| `auth` | C→S | 认证 |
| `message` | C→S | 发送消息 |
| `message_ack` | S→C | 消息确认 |
| `message_error` | S→C | 消息错误 |
| `typing` | 双向 | 输入状态 |
| `read` | C→S | 已读回执 |
| `call` | 双向 | 通话信令 |
| `ping/pong` | 双向 | 心跳 |

#### State Management Patterns

**Vuex State更新规则：**

```javascript
// ✅ 正确：使用 mutations 更新状态
commit('UPDATE_SESSION', { id: sessionId, unreadCount: 0 })

// ❌ 错误：直接修改状态
state.sessions[0].unreadCount = 0
```

**Action命名规范：**

```javascript
// ✅ 正确：动词开头，描述清晰
async loadSessions({ commit }) { }
async selectSession({ commit }, session) { }
async markSessionAsRead({ commit }, sessionId) { }

// ❌ 错误：命名不清晰
async get({ commit }) { }
async doSomething({ commit }) { }
```

**Getter命名规范：**

```javascript
// ✅ 正确：名词或形容词
getters: {
  sortedSessions(state) { },
  currentSessionUnread(state) { },
  sessionById: (state) => (id) => { }
}
```

---

### Process Patterns

#### Error Handling Patterns

**后端异常处理：**

```java
// 统一异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
```

**前端错误处理：**

```javascript
// API请求错误处理
service.interceptors.response.use(
  response => response.data,
  error => {
    const { status, data } = error.response || {}
    
    switch (status) {
      case 401:
        // 清除token，跳转登录
        localStorage.removeItem('im_token')
        window.location.href = '/login'
        break
      case 500:
        ElMessage.error('服务器错误')
        break
      default:
        ElMessage.error(data?.msg || '请求失败')
    }
    
    return Promise.reject(error)
  }
)
```

#### Loading State Patterns

**加载状态管理：**

```javascript
// Vuex State
state: {
  loading: false,
  sessions: []
}

// Actions
async loadSessions({ commit }) {
  commit('SET_LOADING', true)
  try {
    const res = await getConversations()
    commit('SET_SESSIONS', res.data)
  } finally {
    commit('SET_LOADING', false)
  }
}
```

**组件中使用：**

```vue
<template>
  <div v-loading="loading">
    <!-- 内容 -->
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const loading = computed(() => store.state.imSession.loading)
</script>
```

---

### Enforcement Guidelines

**All AI Agents MUST:**

1. **遵循命名规范** - 数据库用snake_case，代码用camelCase/PascalCase
2. **使用统一响应格式** - 所有API返回 `{code, msg, data}` 结构
3. **通过Vuex管理状态** - 禁止直接修改state，必须通过mutations
4. **处理错误和加载状态** - 每个异步操作都要处理loading和error
5. **添加适当的日志** - 关键操作记录日志，便于调试
6. **编写可读代码** - 优先可读性，避免复杂表达式链

**Java代码强制规则：**

```java
// ✅ 正确
if (user != null && user.isActive()) {
    return user.getName();
}

// ❌ 错误：复杂表达式链
return Optional.ofNullable(user).filter(User::isActive).map(User::getName).orElse(null);
```

**Vue代码强制规则：**

```vue
<!-- ✅ 正确：使用v-if/v-else清晰表达 -->
<div v-if="loading">加载中...</div>
<div v-else-if="sessions.length === 0">暂无会话</div>
<div v-else>
  <SessionItem v-for="session in sessions" :key="session.id" />
</div>

<!-- ❌ 错误：复杂三元表达式 -->
<div>{{ loading ? '加载中...' : sessions.length === 0 ? '暂无会话' : '' }}</div>
```

---

### Pattern Examples

#### Good Examples

**API接口定义：**

```java
@RestController
@RequestMapping("/api/im/conversations")
public class ImConversationController {
    
    @GetMapping
    public Result<List<ImConversationVO>> list(
            @RequestParam(defaultValue = "all") String filter) {
        return Result.success(conversationService.listConversations(filter));
    }
    
    @PutMapping("/{id}/pin")
    public Result<Void> pin(@PathVariable Long id, @RequestParam Boolean pinned) {
        conversationService.pinConversation(id, pinned);
        return Result.success();
    }
}
```

**Vuex模块定义：**

```javascript
export default {
  namespaced: true,
  
  state: () => ({
    sessions: [],
    currentSession: null,
    loading: false
  }),
  
  mutations: {
    SET_SESSIONS(state, sessions) {
      state.sessions = sessions
    },
    SET_LOADING(state, loading) {
      state.loading = loading
    }
  },
  
  actions: {
    async loadSessions({ commit }, filter = 'all') {
      commit('SET_LOADING', true)
      try {
        const res = await getConversations(filter)
        commit('SET_SESSIONS', res.data)
      } finally {
        commit('SET_LOADING', false)
      }
    }
  }
}
```

#### Anti-Patterns

**避免的做法：**

```java
// ❌ 魔法值
if (status == 1) { }

// ✅ 使用常量或枚举
if (status == MessageStatus.DELIVERED.getCode()) { }
```

```javascript
// ❌ 直接修改state
state.sessions.push(newSession)

// ✅ 通过mutation
commit('ADD_SESSION', newSession)
```

```java
// ❌ 复杂表达式链
users.stream().filter(u -> u.getStatus() == 1).map(User::getName).collect(Collectors.toList());

// ✅ 清晰的代码
List<String> activeUserNames = new ArrayList<>();
for (User user : users) {
    if (user.getStatus() == UserStatus.ACTIVE.getCode()) {
        activeUserNames.add(user.getName());
    }
}
```

---

## Component Architecture

### 前端组件结构

```
src/
├── components/
│   ├── ImSideNav/              # 左侧导航
│   │   ├── index.vue
│   │   ├── NavItem.vue
│   │   ├── NavIcon/
│   │   └── UserArea.vue
│   │
│   ├── Chat/                   # 聊天模块
│   │   ├── SessionPanel.vue    # 会话列表
│   │   ├── SessionItem.vue     # 会话卡片
│   │   ├── GroupAvatar.vue     # 群组头像
│   │   ├── ChatPanel.vue       # 聊天主区
│   │   ├── ChatHeader.vue      # 聊天头部
│   │   ├── MessageList.vue     # 消息流
│   │   ├── MessageBubble.vue   # 消息气泡
│   │   ├── MessageInput.vue    # 输入区
│   │   └── CallDialog.vue      # 通话弹窗
│   │
│   └── common/                 # 通用组件
│       ├── Avatar.vue
│       └── Badge.vue
│
├── store/modules/
│   ├── im-session.js           # 会话状态
│   ├── im-message.js           # 消息状态
│   └── im-user.js              # 用户状态
│
├── api/im/
│   ├── conversation.js         # 会话API
│   ├── message.js              # 消息API
│   └── user.js                 # 用户API
│
└── utils/websocket/
    └── imWebSocket.js          # WebSocket管理
```

### 后端模块结构

```
com.ruoyi.im/
├── controller/
│   ├── ImConversationController.java
│   ├── ImMessageController.java
│   └── ImUserController.java
│
├── service/
│   ├── ImConversationService.java
│   ├── ImMessageService.java
│   └── ImWebSocketService.java
│
├── mapper/
│   ├── ImConversationMapper.java
│   ├── ImMessageMapper.java
│   └── ImUserSessionMapper.java
│
├── domain/
│   ├── ImConversation.java
│   ├── ImMessage.java
│   └── ImUserSession.java
│
└── vo/
    ├── conversation/
    │   └── ImConversationVO.java
    └── message/
        └── ImMessageVO.java
```