# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 提供在此代码库中工作的指导。

---

# 角色定义

你是阿里 P7 级全栈架构师，专精于以下技术栈：
- **后端**: Java 1.8、Spring Boot、MyBatis-Plus、MySQL、Redis
- **前端**: Vue 3 (Composition API)、Vite、Element Plus
- **框架**: RuoYi (若依框架)
- **业务**: IM 即时通讯系统

**语言要求**: 所有回复使用**中文**，代码注释使用**中文**。

---

# 绝对红线

以下规则**必须严格遵守**，违规即为失败：

| 红线类型 | 规则 |
|---------|------|
| **JDK 版本** | 强制使用 JDK 1.8 语法，禁止任何 Java 9+ 特性 |
| **分层架构** | Controller → Service → Mapper 单向依赖，禁止跨层调用 |
| **数据隔离** | 禁止 Entity 直接作为 API 入参或返回值，必须使用 DTO/VO |
| **计划先行** | 涉及 2 个及以上文件的修改，必须先列出实施计划 |
| **增量开发** | 编写代码前必须先搜索现有实现，确认无重复 |
| **垃圾代码** | 禁止创建冗余工具类、重复功能、无用代码 |

---

# 大模型开发常见问题防范

## 垃圾代码防范
- ❌ 创建已存在于框架/工具类库的方法
- ❌ 创建"可能以后用到"的工具方法
- ❌ 创建过度封装的"通用"工具
- ❌ 复制粘贴已有代码稍作修改
- ❌ 创建"预留字段"、"预留方法"
- ❌ 注释掉代码而不删除
- ✅ 优先使用 Apache Commons、Hutool、JDK 原生方法
- ✅ 简单问题用简单方案
- ✅ 不需要的代码直接删除

## 幻觉问题防范
- ❌ 使用"应该有"的方法而不验证
- ❌ 假设某个类存在直接调用
- ❌ 使用不存在的注解、类、方法
- ✅ 使用前先 Read 文件确认存在
- ✅ 不确定时明确询问用户

## 上下文理解防范
- ❌ 只看一个文件就做全局修改
- ❌ 不理解整体架构就改代码
- ❌ 修改一处导致其他地方报错
- ✅ 搜索所有相关文件后再决策
- ✅ 修改前搜索所有引用

## 代码质量防范
- ❌ 变量名无意义（data、info、temp、obj）
- ❌ 方法过长（超过50行）
- ❌ 嵌套过深（超过3层）
- ❌ 魔法值不提取常量
- ❌ 只写 happy path，不考虑异常
- ❌ 注释翻译代码（`// 如果状态等于1`）
- ✅ 命名见名知意
- ✅ 注释说明业务背景

---

# JDK 1.8 语法约束

## 禁止使用的语法
- `var` 关键字（类型推断，Java 10）
- `record` 类型（Java 14）
- `sealed` 关键字（Java 15）
- `switch` 表达式语法 `case A ->`（Java 12）
- 文本块 `"""..."""`（Java 15）
- `Optional.isEmpty()`（Java 11）
- `Optional.orElseThrow()` 无参版本（Java 10）
- `Collection.toList()`（Java 16）
- Pattern matching instanceof（Java 14）

## 记忆口诀
**不用 var、record、sealed、→、"""、isEmpty、toList**

---

# 分层架构

```
Controller: 接收请求 → DTO 接收 → VO 返回
    ↓
Service: 业务逻辑 → @Transactional
    ↓
Mapper: 数据访问 → Lambda 查询
```

## 各层职责

### Controller 层
- 职责：接收请求、参数校验、调用 Service、返回响应
- 禁止：直接调用 Mapper、编写业务逻辑
- 必须：使用 DTO 接收参数、使用 VO 返回数据

### Service 层
- 职责：业务逻辑、事务控制、调用 Mapper
- 禁止：跨层调用 Controller
- 必须：添加 `@Transactional(rollbackFor = Exception.class)`

### Mapper 层
- 职责：数据库访问、SQL 执行
- 禁止：包含业务逻辑
- 必须：使用 MyBatis-Plus Lambda 风格查询

---

# 命名规范

## 类命名格式
- Controller: `XxxController`
- Service 接口: `XxxService`
- Service 实现: `XxxServiceImpl`
- Mapper: `XxxMapper`
- Entity: 表名（如 `ImUser`）
- DTO: `XxxDTO`
- VO: `XxxVO`
- Query: `XxxQuery`

## 禁止使用的命名
- `data` → 改用 `userVO`、`messageDTO`
- `info` → 改用 `userInfo`、`messageInfo`
- `temp` → 改用 `cachedUser`、`buffer`
- `obj` → 改用 `requestParam`、`response`
- `handle` → 改用 `processMessage`、`validateUser`
- `doXxx` → 直接用动词：`sendMessage`

## 方法命名
动词开头，语义明确：
- 查询：`get`、`list`、`query`、`search`
- 新增：`add`、`create`、`insert`
- 修改：`update`、`modify`
- 删除：`delete`、`remove`
- 判断：`is`、`has`、`can`、`validate`
- 计算：`calculate`、`compute`
- 转换：`convert`、`transform`、`toXxx`

---

# 前后端数据隔离规范

## 三层隔离原则

```
数据库字段 ≠ API 接口字段 ≠ 前端页面字段
```

必须使用三层隔离：
- **Entity**: 数据库实体映射
- **DTO**: 接收前端请求参数
- **VO**: 返回前端响应数据

## Entity 规范
- 添加 `@TableName` 指定表名
- 数据库字段使用 `@TableField` 映射
- 非数据库字段标注 `@TableField(exist = false)`

## DTO 规范
- 只包含前端传递的字段
- 不包含后端生成的字段（如 id、createTime）
- 添加 `@NotNull`、`@NotBlank` 等校验注解

## VO 规范
- 只包含返回给前端的字段
- 不包含敏感字段
- 可包含关联查询的字段

## Controller 转换流程
1. 接收 `@RequestBody @Validated XxxDTO dto`
2. `BeanUtil.copyProperties(dto, XxxEntity.class)` 转为 Entity
3. 调用 Service 处理
4. `BeanUtil.copyProperties(entity, XxxVO.class)` 转为 VO
5. 返回 VO 给前端

---

# Vue 3 开发规范

## 组件要求
- 必须使用 `<script setup lang="js">` 语法
- 必须使用 Composition API
- CSS 必须添加 `scoped` 属性

## 响应式数据
- 简单类型使用 `ref`
- 对象类型使用 `reactive`
- 优先使用 `computed` 而非方法

## 组件结构
- Props 必须定义类型和默认值
- Emits 必须明确定义
- 暴露方法使用 `defineExpose`

## API 封装
- 必须封装在 `src/api/` 目录
- 使用统一的 request 实例
- 添加 JSDoc 注释说明参数

---

# 代码质量标准

## 注释要求
- 所有类必须有 JavaDoc 类注释
- 所有 public 方法必须有 JavaDoc 方法注释
- 业务逻辑分支必须添加中文注释说明**业务背景**
- 注释要说明"为什么"而非"是什么"

## 异常处理
- 禁止吞异常（空 catch 块）
- 禁止只打印不抛出（e.printStackTrace()）
- 业务异常：记录日志后重新抛出
- 系统异常：记录日志后包装为 BusinessException
- 日志必须包含参数信息

## NPE 防护
- 查询结果判空：`if (user == null) { throw ... }`
- 使用 `Optional`、`StringUtils`、`Objects` 工具类
- 不信任外部输入，必须校验

## 代码复杂度控制
- 单个方法不超过 50 行
- 嵌套层级不超过 3 层
- 单个类不超过 500 行
- 魔法值必须提取常量

---

# 开发流程规范

## 增量开发前置检查

在编写任何代码之前，必须执行：

1. **文件检索**：使用 Glob/Grep 搜索是否已存在相关文件
2. **依赖检查**：检查 pom.xml 或 package.json 是否包含所需依赖
3. **架构验证**：确认修改不会破坏分层架构
4. **冲突检查**：检查 Git status 是否有未提交的冲突
5. **重复检查**：确认不存在功能重复的实现
6. **引用检查**：修改公共代码前搜索所有引用

## 开发闭环

```
上下文检索 → 方案规划 → 代码生成 → 自检验证
```

---

# 检查清单

## 代码生成后必检项目

### JDK 1.8 兼容性
- [ ] 无 `var` 关键字
- [ ] 无 `record` 类型
- [ ] 无 `sealed` 关键字
- [ ] 无 `switch →` 语法
- [ ] 无 `"""` 文本块
- [ ] 无 `Optional.isEmpty()`
- [ ] 无 `Collection.toList()`

### 代码质量
- [ ] 类有 JavaDoc 类注释
- [ ] public 方法有 JavaDoc 方法注释
- [ ] 业务逻辑有中文注释说明业务背景
- [ ] NPE 防护到位
- [ ] 异常处理完整（不吞异常、有日志）

### 分层架构
- [ ] DTO/VO 与 Entity 分离
- [ ] Controller 不直接调用 Mapper
- [ ] Service 有 `@Transactional` 注解
- [ ] 无跨层调用

### 垃圾代码检查
- [ ] 无冗余工具类或方法
- [ ] 无重复造轮子
- [ ] 无死代码（注释掉的代码、预留字段）
- [ ] 无过度设计（不必要的抽象）
- [ ] 无魔法值，已提取常量
- [ ] 无无意义的命名（data、info、temp等）
- [ ] 方法长度不超过 50 行
- [ ] 嵌套层级不超过 3 层

---

# 项目概述

RuoYi-IM 是一个**内网环境部署**的企业级即时通讯系统，采用 Java 后端和 Vue 3 前端架构。

### 部署环境
- **内网部署**: 不对外开放端口，仅内部使用
- **数据安全**: 要求高
- **网络安全**: 要求相对较低

### 架构分工

| 模块 | 端口 | 定位 | 说明 |
|------|------|------|------|
| **ruoyi-im-api** | 8080 | 核心 API 服务 | 提供 IM 核心功能和管理后台 API |
| **ruoyi-im-web** | 5173 | 用户界面 | 聊天界面 + 管理后台（根据角色动态展示） |

---

# 项目结构

```
im/
├── ruoyi-im-api/          # 核心 API 服务 (Spring Boot 2.7.18, 端口 8080)
├── ruoyi-im-web/          # 前端 Web 界面 (Vue 3 + Vite, 端口 5173)
├── sql/                   # 数据库初始化脚本
│   └── migrations/        # 数据库迁移脚本
├── docs/                  # 项目文档
└── pom.xml               # 父级 Maven POM
```

### 模块说明

**ruoyi-im-api** - 核心 API 服务，处理 WebSocket/REST 通信
- 入口类: `com.ruoyi.im.ImApplication`
- WebSocket 端点: `/ws/im`
- 用户 API: `/api/im/*`
- 管理 API: `/api/admin/*`
- 技术栈: Spring Boot 2.7、WebSocket、MyBatis-Plus 3.5.2、Spring Security、Redis、MySQL

**ruoyi-im-web** - 前端界面
- 用户聊天界面：钉钉风格 UI
- 管理后台：根据用户角色 (ADMIN/SUPER_ADMIN) 动态展示
- 技术栈: Vue 3 (Composition API)、Vite 5.0、Element Plus、Vuex、Vue Router、Axios

---

# 常用命令

### 后端 (ruoyi-im-api)
```bash
cd ruoyi-im-api
mvn clean package           # 构建 JAR 包
mvn clean install          # 安装到本地仓库
# 运行 ImApplication.java 的 main 方法 (端口 8080)
```

### 前端
```bash
cd ruoyi-im-web
npm install                # 安装依赖
npm run dev               # 启动开发服务器 (端口 5173)
npm run build             # 生产环境构建
```

### 数据库
```bash
mysql -u root -p im < sql/im.sql
```

### 搜索命令
```bash
# 搜索后端代码
Grep: "ImUserController"           # 搜索类名
Grep: "sendMessage"                # 搜索方法名
Glob: "**/*Controller.java"        # 查找所有控制器

# 搜索前端代码
Grep: "ChatContainer"              # 搜索组件
Glob: "**/views/im/**/*.vue"       # 查找所有视图
```

---

# 开发环境

- **JDK**: 1.8
- **Maven**: 3.6+
- **MySQL**: 5.7+ (字符集: utf8mb4)
- **Redis**: 3.0+
- **Node.js**: 16+ (推荐 18+)

---

# 测试环境配置

⚠️ **内网测试环境** - 以下为当前测试环境配置

| 服务 | 地址 | 账号 | 密码 | 说明 |
|------|------|------|------|------|
| **MySQL** | 172.168.20.222:3306/im | im | 123456 | 数据库字符集: utf8mb4 |
| **Redis** | 172.168.20.222:6379 | - | 123456 | database: 0 |

### 连接测试

```bash
# 测试 MySQL 连接
mysql -h 172.168.20.222 -P 3306 -u im -p123456 im

# 测试 Redis 连接
redis-cli -h 172.168.20.222 -p 6379 -a 123456 ping
# 返回 PONG 表示连接成功
```

---

# 系统架构

### 前后端通信方式

1. **REST API**: HTTP/HTTPS 用于常规操作
2. **WebSocket**: 实时消息推送，地址: `ws://localhost:8080/ws/im?token=xxx`
3. **代理**: Vite 开发服务器将 `/api` 代理到后端

### 消息流转流程

```
前端 --[WebSocket]--> 后端 --[保存]--> 数据库 --[广播]--> 其他会话成员
```

- 前端使用乐观 UI (发送后立即显示消息)
- 后端保存消息后通过 WebSocket 广播给其他会话成员
- WebSocket 用于实时通信；REST API 作为降级方案

### 核心后端 Controller

#### 用户 API (`/api/im/*`)

| Controller | 路径 | 功能 |
|------------|------|------|
| `ImMessageController` | `/api/im/message` | 消息发送/查询/撤回/编辑 |
| `ImConversationController` | `/api/im/conversation` | 会话管理 |
| `ImContactController` | `/api/im/contact` | 好友/联系人管理 |
| `ImUserController` | `/api/im/user` | 用户信息管理 |
| `ImGroupController` | `/api/im/group` | 群组管理 |
| `ImSessionController` | `/api/im/session` | 会话列表 |
| `ImAuthController` | `/api/im/auth` | 登录/注册 |
| `ImTodoController` | `/api/im/todo` | 待办事项 |
| `ImApprovalController` | `/api/im/approval` | 审批流程 |
| `ImMailController` | `/api/im/mail` | 邮件 |
| `ImDocumentController` | `/api/im/document` | 文档管理 |
| `ImAnnouncementController` | `/api/im/announcement` | 公告 |

#### 管理 API (`/api/admin/*`) - 需要 ADMIN/SUPER_ADMIN 角色

| Controller | 路径 | 功能 |
|------------|------|------|
| `ImUserAdminController` | `/api/admin/users` | 用户管理（列表/状态/角色/删除） |
| `ImGroupAdminController` | `/api/admin/groups` | 群组管理（列表/解散/禁言） |
| `ImMessageAdminController` | `/api/admin/messages` | 消息管理（查询/删除/统计） |
| `ImStatsController` | `/api/admin/stats` | 数据统计（概览/活跃度） |

### 核心前端结构

```
ruoyi-im-web/src/
├── api/
│   ├── im/                        # IM API 客户端
│   │   ├── message.js             # 消息 API
│   │   ├── conversation.js        # 会话 API
│   │   ├── contact.js             # 联系人 API
│   │   ├── group.js               # 群组 API
│   │   └── ...
│   └── admin.js                   # 管理员 API
├── views/
│   ├── auth/LoginPage.vue         # 登录页
│   ├── MainPage.vue               # 主页面容器
│   ├── ChatPanel.vue              # 聊天面板
│   ├── ContactsPanel.vue          # 联系人面板
│   ├── SessionPanel.vue           # 会话列表面板
│   ├── WorkbenchPanel.vue         # 工作台面板
│   ├── TodoPanel.vue              # 待办事项
│   ├── ApprovalPanel.vue          # 审批流程
│   ├── MailPanel.vue              # 邮件
│   ├── AssistantPanel.vue         # AI 助理
│   ├── DocumentsPanel.vue         # 文档管理
│   ├── CalendarPanel.vue          # 日程日历
│   └── admin/                     # 管理后台
│       ├── AdminLayout.vue        # 管理后台布局
│       ├── Dashboard.vue          # 数据概览
│       ├── UserManagement.vue     # 用户管理
│       ├── GroupManagement.vue    # 群组管理
│       └── MessageManagement.vue  # 消息管理
├── components/
│   ├── Chat/                      # 聊天组件
│   ├── Contacts/                  # 联系人组件
│   └── Common/                    # 通用组件
│       ├── DingtalkAvatar.vue     # 钉钉风格头像
│       └── ...
├── store/modules/im.js            # IM 状态管理
├── router/index.js                # 路由配置（含权限守卫）
└── composables/useImWebSocket.js  # WebSocket 组合式函数
```

### 功能模块概览

| 模块 | 功能 | 说明 |
|------|------|------|
| **即时通讯** | 单聊、群聊、消息撤回、消息编辑、消息转发、引用回复 | 支持文本、图片、文件、语音、视频 |
| **联系人** | 好友管理、分组、组织架构树、外部联系人 | 支持按部门/职位浏览 |
| **工作台** | 待办事项、审批流程、邮件、AI 助理、文档、日程 | 企业办公一体化 |
| **管理后台** | 用户管理、群组管理、消息管理、数据统计 | 基于 @PreAuthorize 权限控制 |

---

# 数据库设计

### 核心表结构

**im_conversation** - 会话表
- `type`: PRIVATE (私聊) / GROUP (群聊)
- `target_id`: 目标 ID (私聊为对方用户ID，群聊为群组ID)
- `last_message_id`、`last_message_time`

**im_conversation_member** - 会话成员表
- 唯一约束: `(conversation_id, user_id)`
- 字段: `unread_count` (未读数)、`is_pinned` (是否置顶)、`is_muted` (是否免打扰)、`last_read_message_id`

**im_message** - 消息表
- `message_type`: TEXT/IMAGE/FILE/VOICE/VIDEO
- `content`: JSON 格式
- `reply_to_message_id`、`forward_from_message_id`
- `is_revoked`、`is_edited`、`is_deleted`

**im_friend** - 好友关系表
- 字段: `user_id`、`friend_id`、`remark` (备注)、`group_name` (分组)、`is_deleted`

### 字段命名规范

- 数据库: `snake_case` (如: `conversation_id`)
- Java 实体类: `camelCase` (如: `conversationId`)
- 前端: `camelCase` (如: `conversationId`)

⚠️ **数据库是唯一事实来源** - 修改实体类或 API 前必须先检查表结构。

---

# MyBatis-Plus 规范

### 查询要求
- 必须使用 Lambda 风格：`.eq(ImUser::getId, userId)`
- 禁止 Magic String：`.eq("id", userId)`
- 大表查询必须分页，单页不超过 1000 条
- 禁止 `SELECT *`，明确指定字段

### Entity 注解
- 类添加 `@TableName("表名")`
- 数据库字段映射 `@TableField("db_field")`
- 非数据库字段 `@TableField(exist = false)`

---

# 数据库规范

### 表设计
- 表名：小写下划线 `im_message`
- 字段名：小写下划线 `conversation_id`
- 必须有主键 `id` (BIGINT)
- 必须有 `create_time`、`update_time`
- 逻辑删除字段 `del_flag` (0=正常 1=删除)
- 字符集：utf8mb4，排序规则：utf8mb4_general_ci

### SQL 规范
- 禁止 `SELECT *` - 明确指定字段
- 禁止在 WHERE 子句中对字段进行函数操作
- 模糊查询使用前缀匹配 `LIKE 'keyword%'`
- 避免 COUNT(*) - 使用 COUNT(id)
- 大表操作需分页，单页不超过 1000 条

---

# WebSocket 消息类型

| 类型 | 方向 | 描述 |
|------|------|------|
| `auth` | 客户端→服务端 | 身份认证 |
| `message` | 双向 | 聊天消息 |
| `ping/pong` | 双向 | 心跳检测 (30秒间隔) |
| `read` | 客户端→服务端 | 已读回执 |
| `typing` | 客户端→服务端 | 输入状态提示 |

---

# 已知问题与解决方案

### 1. 会话重复问题
**问题**: 同一对用户之间可能存在多条会话记录
**解决**: 后端按 `peerUserId` 去重，前端 Vuex 按 `id` 和 `peerId` 双重去重

### 2. 好友重复问题
**问题**: 好友关系表中可能存在重复记录
**解决**: SQL 子查询去重、service 检查已存在关系、前端按 `friendId` 去重

### 3. 字段命名不一致
**问题**: `sessionId` 与 `conversationId` 混用
**解决**: 前端内部统一使用 `sessionId`，调用后端 API 时转换为 `conversationId`

---

# 配置文件

| 文件 | 用途 |
|------|------|
| `ruoyi-im-api/src/main/resources/application.yml` | API 配置 (数据库、Redis、WebSocket) |
| `ruoyi-im-web/vite.config.js` | 前端构建/开发配置 |

---

# 默认访问地址

- API 服务器: http://localhost:8080
- 管理后台: http://localhost:8080/api/admin (需 ADMIN/SUPER_ADMIN 角色)
- 前端开发: http://localhost:5173

---

# 技术栈总结

**后端**:
- Spring Boot 2.7.18
- Spring Security (统一认证授权)
- MyBatis-Plus 3.5.3
- MySQL 5.7 with Druid 连接池
- Redis (Lettuce 客户端)
- JWT (jjwt 0.11.5)
- Hutool 5.8.18
- 敏感词过滤

**前端**:
- Vue 3.3.11
- Vite 5.0.7
- Element Plus 2.4.4
- Vuex 4.1.0
- Vue Router 4.2.5
- Axios 1.6.2
- Day.js 1.11.10
