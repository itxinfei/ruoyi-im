# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 提供在此代码库中工作的指导。

## 项目概述

RuoYi-IM 是一个**内网环境部署**的企业级即时通讯系统，采用 Java 后端和 Vue 3 前端架构。

### 部署环境
- **内网部署**: 不对外开放端口，仅内部使用
- **数据安全**: 要求高
- **网络安全**: 要求相对较低

### 架构分工

| 模块 | 端口 | 定位 | 要求 |
|------|------|------|------|
| **ruoyi-im-admin** | 8081 | 后台管理系统 | 管理功能好用、页面使用方便，要求相对较低 |
| **ruoyi-im-api** | 8080 | 核心通讯服务 | 高性能、高可靠的 IM 核心服务（独立项目不合ruoyi-im-admin关联偶合） |
| **ruoyi-im-web** | 5173 | 用户聊天界面 | **高要求**，钉钉风格 UI，用户体验优先 |

**语言要求**: 所有对用户的回复必须使用**中文**。

## 项目结构

```
im/
├── ruoyi-im-api/          # 核心 API 服务 (Spring Boot 2.7.18, 端口 8080)
├── ruoyi-im-admin/        # 后台管理系统 (RuoYi v4.8.0, 端口 8081)
├── ruoyi-im-web/          # 前端 Web 界面 (Vue 3 + Vite, 端口 5173)
├── sql/                   # 数据库初始化脚本
├── docs/                  # 项目文档
└── pom.xml               # 父级 Maven POM
```

### 模块说明

**ruoyi-im-api** - 核心 API 服务，处理 WebSocket/REST 通信
- 入口类: `com.ruoyi.im.ImApplication`
- WebSocket 端点: `/ws/im`
- REST API 基础路径: `/api/im`
- 技术栈: Spring Boot 2.7、WebSocket、MyBatis-Plus 3.5.2、Spring Security、Redis、MySQL

**ruoyi-im-admin** - 后台管理系统 (多模块 Maven 项目)
- 子模块: ruoyi-admin、ruoyi-framework、ruoyi-system、ruoyi-generator、ruoyi-common
- 入口类: `com.ruoyi.RuoYiApplication`
- 技术栈: RuoYi v4.8.0、Apache Shiro、Thymeleaf

**ruoyi-im-web** - 前端聊天界面
- 技术栈: Vue 3 (Composition API)、Vite 5.0、Element Plus、Vuex、Vue Router、Axios

## 常用命令

### 后端 (ruoyi-im-api)
```bash
cd ruoyi-im-api
mvn clean package           # 构建 JAR 包
mvn clean install          # 安装到本地仓库
# 运行 ImApplication.java 的 main 方法 (端口 8080)
```

### 后端 (ruoyi-im-admin)
```bash
cd ruoyi-im-admin/ruoyi-admin
mvn clean package
# 运行 RuoYiApplication.java 的 main 方法 (端口 8081)
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

## 开发环境

- **JDK**: 1.8
- **Maven**: 3.6+
- **MySQL**: 5.7+ (字符集: utf8mb4)
- **Redis**: 3.0+
- **Node.js**: 16+ (推荐 18+)

## 配置文件

| 文件 | 用途 |
|------|------|
| `ruoyi-im-api/src/main/resources/application.yml` | API 配置 (数据库、Redis、WebSocket) |
| `ruoyi-im-admin/ruoyi-admin/src/main/resources/application.yml` | 后台管理配置 |
| `ruoyi-im-admin/ruoyi-admin/src/main/resources/application-druid.yml` | 数据库连接池配置 |
| `ruoyi-im-web/vite.config.js` | 前端构建/开发配置 |

## 测试环境配置

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

## 系统架构

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

| Controller | 路径 | 功能 |
|------------|------|------|
| `ImMessageController` | `/api/im/message` | 消息发送/查询/撤回 |
| `ImConversationController` | `/api/im/conversation` | 会话管理 |
| `ImContactController` | `/api/im/contact` | 好友/联系人管理 |
| `ImUserController` | `/api/im/user` | 用户管理 |
| `ImGroupController` | `/api/im/group` | 群组管理 |
| `ImSessionController` | `/api/im/session` | 会话列表 |

### 核心前端结构

```
ruoyi-im-web/src/
├── api/im/                      # API 客户端 (message.js、conversation.js 等)
├── views/im/
│   ├── ImChatLayoutOptimized.vue  # 主聊天界面 (钉钉风格)
│   ├── chat/ChatContainer.vue     # 聊天容器组件
│   ├── contacts/                  # 联系人模块
│   └── workbench/                 # 工作台模块
├── store/modules/im.js            # IM 状态管理的 Vuex Store
├── composables/useImWebSocket.js  # WebSocket 组合式函数
└── utils/websocket/imWebSocket.js # WebSocket 单例实现
```

### Vuex Store (im.js)

**State**:
- `currentSession` - 当前活跃会话
- `sessions` - 会话列表
- `messageList` - 按 sessionId 分组的消息列表
- `contacts` - 联系人列表
- `groups` - 群组列表
- `wsConnected` - WebSocket 连接状态

**Actions**:
- `loadSessions()` - 加载会话列表
- `loadMessages({ sessionId, lastId, pageSize })` - 加载消息
- `sendMessage({ sessionId, type, content })` - 发送消息
- `receiveMessage(message)` - 接收消息

## 数据库设计

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

## 代码规范

### 编码规范（强制执行）

**遵循阿里巴巴开发手册**:
- 《阿里巴巴 Java 开发手册》（最新版）
- 《阿里巴巴前端开发手册》（Vue 专项）
- 《阿里巴巴数据库设计规范》

### 开发流程（三步法）

**第一步：搜索现有代码**
- 使用 `Grep` 或 `Glob` 搜索是否已存在相关功能
- 检查是否有可复用的组件、工具类、方法
- 查看现有实现方式和命名规范

**第二步：复用优先**
- 找到现有代码 → 直接使用或扩展
- 找到类似组件 → 修改复用，不新建
- 确认不存在 → 再开发新功能

**第三步：质量把关**
- 禁止重复造轮子
- 禁止创建冗余组件
- 禁止写垃圾代码（不可维护、无注释、逻辑混乱）

### Java 代码标准（阿里巴巴规范）

**命名规范**:
- 类名：大驼峰 `ImUserController`
- 方法名：小驼峰 `sendMessage`
- 常量：全大写下划线 `MAX_MESSAGE_SIZE`
- 变量：小驼峰 `userName`
- 包名：全小写点分隔 `com.ruoyi.im.controller`

**注释规范**:
- 所有类必须添加 JavaDoc 注释（说明类的功能、作者、日期）
- 所有 public 方法必须添加 JavaDoc 注释（说明参数、返回值、异常）
- 核心业务逻辑必须添加行内中文注释
- 复杂算法必须添加注释说明思路

**代码质量**:
- 严格分层: controller → service → mapper（禁止跨层调用）
- 仅使用 JDK 1.8 语法（禁止 Java 9+ 特性）
- 命名清晰 - 禁止使用: `data`、`info`、`temp`、`obj`、`handle`、`process`
- 避免炫技式 Stream/lambda - 复杂逻辑必须可断点调试
- `common` 包为受控区域 - 禁止堆放业务代码
- 一个方法只做一件事 - 方法长度不超过 50 行
- 避免深层嵌套 - 嵌套层级不超过 3 层
- 使用常量替代魔法值

**失败条件**:
- 未搜索就开发 = 代码必须重写
- 重复开发已有功能 = 代码必须删除
- 分层违例、命名不规范、缺少注释 = 代码必须重写

### 前端代码标准（阿里巴巴 Vue 规范）

**组件规范**:
- 组件名：多单词大驼峰 `ChatContainer.vue`
- 使用 Composition API 和 `<script setup>` 语法
- 组件 Props 必须定义类型和默认值
- 组件事件使用 kebab-case `@send-message`

**代码质量**:
- 避免 `this` 丢失，使用箭头函数
- 响应式数据使用 `ref` / `reactive`
- 计算属性优先于方法调用
- 合理拆分组件 - 单文件不超过 300 行
- 合理使用 Vuex - 避免过度全局状态

### 数据库规范（阿里巴巴规范）

**表设计**:
- 表名：小写下划线 `im_message`
- 字段名：小写下划线 `conversation_id`
- 必须有主键 `id` (BIGINT)
- 必须有 `create_time`、`update_time`
- 逻辑删除字段 `del_flag` (0=正常 1=删除)
- 字符集：utf8mb4，排序规则：utf8mb4_general_ci

**SQL 规范**:
- 禁止 `SELECT *` - 明确指定字段
- 禁止在 WHERE 子句中对字段进行函数操作
- 模糊查询使用前缀匹配 `LIKE 'keyword%'`
- 避免 COUNT(*) - 使用 COUNT(id)
- 大表操作需分页，单页不超过 1000 条

### 搜索命令参考

```bash
# 搜索后端代码
Grep: "ImUserController"           # 搜索类名
Grep: "sendMessage"                # 搜索方法名
Glob: "**/*Controller.java"        # 查找所有控制器

# 搜索前端代码
Grep: "ChatContainer"              # 搜索组件
Glob: "**/views/im/**/*.vue"       # 查找所有视图
```

## WebSocket 消息类型

| 类型 | 方向 | 描述 |
|------|------|------|
| `auth` | 客户端→服务端 | 身份认证 |
| `message` | 双向 | 聊天消息 |
| `ping/pong` | 双向 | 心跳检测 (30秒间隔) |
| `read` | 客户端→服务端 | 已读回执 |
| `typing` | 客户端→服务端 | 输入状态提示 |

## 已知问题与解决方案

### 1. 会话重复问题
**问题**: 同一对用户之间可能存在多条会话记录
**解决**: 后端按 `peerUserId` 去重，前端 Vuex 按 `id` 和 `peerId` 双重去重

### 2. 好友重复问题
**问题**: 好友关系表中可能存在重复记录
**解决**: SQL 子查询去重、service 检查已存在关系、前端按 `friendId` 去重

### 3. 字段命名不一致
**问题**: `sessionId` 与 `conversationId` 混用
**解决**: 前端内部统一使用 `sessionId`，调用后端 API 时转换为 `conversationId`

## 重要约束

1. **数据库变更**需要同步更新 Entity、Mapper XML 和 API 接口
2. Entity 类使用 Lombok 注解
3. 避免使用 `SELECT *` 查询
4. 使用统一的 WebSocket 消息格式
5. 前端使用 Composition API 和 `<script setup>` 语法

## 前后端 API 对齐规范（避免参数不对齐）

### 核心原则

**数据库字段 ≠ API 接口字段 ≠ 前端页面字段**

必须使用三层隔离：
- **Entity**: 数据库实体映射（直接对应数据库）
- **DTO/VO**: 数据传输对象（API 接口使用）
- **Frontend Model**: 前端数据模型（前端组件使用）

### 开发规范

#### 1. 后端使用 DTO/VO 隔离

**禁止直接使用 Entity 作为 API 入参/返回值**

❌ **错误做法**:
```java
// 直接使用 Entity，前端多传任何字段都会报错
@PostMapping("/send")
public ImMessage sendMessage(@RequestBody ImMessage message) {
    // 前端传了 extraField，但数据库没有这个字段 → 报错
}
```

✅ **正确做法**:
```java
// 使用 DTO 接收参数
@PostMapping("/send")
public ImMessageVO sendMessage(@RequestBody ImMessageDTO dto) {
    // DTO 只包含需要的字段，前端多传的字段会被忽略
    ImMessage message = BeanUtil.copyProperties(dto, ImMessage.class);
    return BeanUtil.copyProperties(message, ImMessageVO.class);
}
```

#### 2. 分层定义数据对象

```
com.ruoyi.im.domain/
├── entity/        # Entity - 数据库实体
│   └── ImMessage.java
├── dto/           # DTO - 接收请求参数
│   └── ImMessageDTO.java
└── vo/            # VO - 返回响应数据
    └── ImMessageVO.java
```

**Entity (数据库实体)**:
```java
@Data
@TableName("im_message")
public class ImMessage {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String messageType;
    private String content;
    // 所有数据库字段
}
```

**DTO (请求参数)**:
```java
/**
 * 发送消息请求 DTO
 * 只包含前端需要传递的字段
 */
@Data
public class ImMessageDTO {
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    @NotNull(message = "消息类型不能为空")
    private String messageType;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    // 注意：不包含 id、senderId、createTime 等由后端生成的字段
}
```

**VO (响应数据)**:
```java
/**
 * 消息响应 VO
 * 只包含返回给前端需要的字段
 */
@Data
public class ImMessageVO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String messageType;
    private String content;
    private LocalDateTime createTime;

    // 注意：不包含敏感字段或不需要返回的字段
}
```

#### 3. Controller 层转换

```java
@RestController
@RequestMapping("/api/im/message")
public class ImMessageController {

    @Autowired
    private ImMessageService messageService;

    /**
     * 发送消息
     * @param dto 前端传递的参数（DTO）
     * @return 返回给前端的数据（VO）
     */
    @PostMapping("/send")
    public AjaxResult sendMessage(@RequestBody @Validated ImMessageDTO dto) {
        // 1. DTO → Entity
        ImMessage message = BeanUtil.copyProperties(dto, ImMessage.class);

        // 2. 调用 Service
        ImMessage savedMessage = messageService.sendMessage(message);

        // 3. Entity → VO
        ImMessageVO vo = BeanUtil.copyProperties(savedMessage, ImMessageVO.class);

        return AjaxResult.success(vo);
    }
}
```

#### 4. 前端 API 定义

```javascript
// api/im/message.js

/**
 * 发送消息
 * @param {Object} params - 参数对象
 * @param {number} params.conversationId - 会话ID
 * @param {string} params.messageType - 消息类型 TEXT/IMAGE/FILE
 * @param {string} params.content - 消息内容
 */
export function sendMessage(params) {
  return request({
    url: '/api/im/message/send',
    method: 'post',
    data: params
  })
}
```

### AI 开发提示技巧

当使用 AI 开发 API 时，使用以下提示词模板：

```
【开发任务】开发 XXX 功能的 API 接口

【要求】
1. 创建 XxxDTO.java 接收前端参数，只包含必要字段
2. 创建 XxxVO.java 返回响应数据，只包含需要展示的字段
3. Entity 只用于数据库映射，不要直接暴露给 API
4. Controller 使用 @RequestBody 接收 DTO，返回 VO
5. 前端 API 调用时，严格按照 DTO 定义传递参数

【参数定义】
- 前端传递: {字段1}: {类型}, {字段2}: {类型}
- 后端返回: {字段1}: {类型}, {字段2}: {类型}

【数据库表】
- 表名: im_xxx
- 字段: xxx

请按照阿里巴巴规范和项目结构生成代码。
```

### 常见问题解决

#### 问题 1: 前端多传字段导致报错

**原因**: 使用 Entity 接收参数，MyBatis-Plus 尝试映射所有字段

**解决**:
```java
// 方案 1: 使用 DTO 接收（推荐）
@PostMapping("/xxx")
public AjaxResult xxx(@RequestBody XxxDTO dto) {
    // DTO 中不存在的字段会被忽略
}

// 方案 2: 在 Entity 上添加 @JsonIgnoreProperties
@JsonIgnoreProperties(ignoreUnknown = true)
public class XxxEntity {
}

// 方案 3: 全局配置（application.yml）
spring:
  jackson:
    deserialization:
      fail-on-unknown-properties: false  # 忽略未知字段
```

#### 问题 2: 字段命名不一致导致映射失败

**原因**: 前端使用 camelCase，后端使用其他命名

**解决**: 统一使用 camelCase
```java
// 统一命名规范
private Long conversationId;  // ✅ 前后端一致
private Long conversation_id; // ❌ 不一致
```

#### 问题 3: 数据库字段与 API 字段混淆

**解决**: 使用 MyBatis-Plus 字段映射
```java
@Data
@TableName("im_message")
public class ImMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("conversation_id")  // 数据库字段名
    private Long conversationId;    // Java 字段名

    @TableField(exist = false)     // 不存在于数据库
    private String extraField;
}
```

### 开发流程

**开发新功能时**:
1. **先定义 DTO/VO** - 明确接口需要哪些参数
2. **再实现 Controller** - 使用 DTO 接收，返回 VO
3. **最后实现 Service** - 处理业务逻辑
4. **前端对接** - 严格按照 DTO 定义传参

**修改现有功能时**:
1. **检查 Entity** - 确认数据库字段
2. **检查 DTO/VO** - 确认接口字段
3. **前后端同步** - 同时修改前端 API 调用
4. **测试验证** - 确保参数对齐

### 快速检查清单

开发完成后，检查以下项目：

- [ ] Controller 是否使用 DTO/VO，而不是 Entity
- [ ] DTO 是否添加了 @Validated 注解
- [ ] 前端 API 调用参数是否与 DTO 定义一致
- [ ] 是否有字段命名不一致的情况
- [ ] 是否有多余字段未做 @TableField(exist = false) 处理

## 默认访问地址

- API 服务器: http://localhost:8080
- 后台管理: http://localhost:8081 (账号: admin / 密码: 123456)
- 前端开发: http://localhost:5173

## 技术栈总结

**后端**:
- Spring Boot 2.7.18
- Spring Security (API) / Apache Shiro (后台)
- MyBatis-Plus 3.5.2
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
