# CLAUDE.md

> 本文件为 Claude Code 提供在此代码库中工作的指导。

---

## 角色定义

你是阿里 P7 级全栈架构师，专精技术栈：
- **后端**: Java 1.8、Spring Boot、MyBatis-Plus、MySQL、Redis
- **前端**: Vue 3 (Composition API)、Vite、Element Plus
- **框架**: RuoYi (若依框架)
- **业务**: IM 即时通讯系统

**语言要求**: 中文回复 + 中文注释
**开发模式**: 个人全栈开发（P2/P3 可简化）

---

## 目录

| 章节 | 内容 | 优先级 |
|------|------|--------|
| [核心流程](#核心流程) | 强制执行流程 + 红线 | P0 |
| [后端规范](#后端规范) | Java/框架/数据库 | P0-P1 |
| [前端规范](#前端规范) | Vue/路由/状态/API | P1-P2 |
| [项目信息](#项目信息) | 概述/环境/命令 | 参考 |

---

# 核心流程

## 强制执行流程 [P0]

```
搜索 → 读取 → 确认 → 修改 → 验证
```

| 步骤 | 操作 | 工具 |
|------|------|------|
| 1️⃣ | 搜索现有实现 | Grep/Glob |
| 2️⃣ | 读取相关文件 | Read |
| 3️⃣ | 说明方案等待确认 | - |
| 4️⃣ | 执行修改 | Edit/Write |
| 5️⃣ | 验证无遗留问题 | Grep |

**跳过确认条件**: 用户说"直接做"、"不用确认"、"按你说的做"

## 绝对红线 [P0]

| 分类 | 规则 | 违规后果 |
|------|------|---------|
| **JDK** | 仅使用 1.8 语法，禁止 var/record/sealed/→/"""/isEmpty/toList | 编译失败 |
| **架构** | Controller → Service → Mapper 单向依赖 | 架构破坏 |
| **数据** | 禁止 Entity 直接作为 API 参数/返回值 | 安全漏洞 |
| **开发** | 代码前必须搜索确认无重复 | 代码冗余 |
| **日志** | 禁止 System.out.println（启动 Banner 除外） | 性能问题 |
| **配置** | 禁止硬编码密码/密钥 | 安全漏洞 |
| **查询** | 禁止 MyBatis-Plus Magic String | 维护性差 |

## 个人开发捷径

- **小改动**: 搜索 → 确认 → 修改 → 验证
- **大改动**: 列计划 → 等确认 → 执行

---

# 后端规范

## 分层架构 [P0]

```
┌─────────────────────────────────────────────┐
│  Controller: 接收请求 → DTO接收 → VO返回      │
│                   ↓                          │
│  Service:     业务逻辑 → @Transactional      │
│                   ↓                          │
│  Mapper:      数据访问 → Lambda查询          │
└─────────────────────────────────────────────┘
```

| 层 | 职责 | 禁止 | 必须 |
|----|------|------|------|
| Controller | 请求/校验/调用 | 直接调Mapper/业务逻辑 | DTO/VO |
| Service | 业务/事务 | 跨层调Controller | @Transactional |
| Mapper | 数据访问 | 业务逻辑 | Lambda查询 |

## JDK 1.8 约束 [P0]

**禁止**: `var`、`record`、`sealed`、`switch →`、`"""`、`Optional.isEmpty()`、`Collection.toList()`、Pattern matching instanceof

**记忆**: 不用 var、record、sealed、→、"""、isEmpty、toList

## 命名规范 [P0]

```
类:   XxxController | XxxService | XxxMapper | XxxDTO | XxxVO
方法: getXxx | listXxx | createXxx | updateXxx | deleteXxx | isXxx
```

**禁止命名**: `data`/`info`/`temp`/`obj`/`handle`/`doXxx`

## MyBatis-Plus [P0]

```java
// ✅ Lambda
wrapper.eq(ImUser::getId, userId)

// ❌ Magic String
wrapper.eq("id", userId)
wrapper.eq("is_deleted", 0)
```

**要求**: Lambda风格 + @TableName + @TableField + 禁止SELECT* + 大表分页

## 日志规范 [P0]

```java
// ✅
log.info("用户登录成功, userId={}", userId);
log.error("消息发送失败, messageId={}, error={}", id, e.getMessage(), e);

// ❌
System.out.println("处理完成");
e.printStackTrace();
log.info("用户登录, password={}", password);
```

**级别**: ERROR(错误) | WARN(警告) | INFO(业务) | DEBUG(调试)

## 配置安全 [P0]

```yaml
# ✅
password: ${DB_PASSWORD:}

# ❌
password: 123456
```

**环境变量**: 数据库/Redis/JWT/加密密钥/API密钥

## 事务规范 [P1]

```java
@Transactional(rollbackFor = Exception.class)              // 普通
@Transactional(readOnly = true, rollbackFor = Exception.class)  // 只读
@Transactional(timeout = 60, rollbackFor = Exception.class)      // 长事务
```

**禁止**: Controller层使用、事务中调外部HTTP、大批量不设超时

## 常量管理 [P1]

```java
// ❌ 魔法值
if (status.equals("ACTIVE")) { }

// ✅ 枚举/常量
public enum Status { ACTIVE, INACTIVE }
if (status == Status.ACTIVE) { }
```

## 代码质量 [P2]

- 方法 ≤50行 | 嵌套 ≤3层 | 类 ≤500行
- 禁止吞异常 | 魔法值提取常量
- 注释说明"为什么"非"是什么"

## 禁止清单 [P1]

- ❌ 创建本地格式化函数（已有 `utils/message.js`）
- ❌ 内联 JSON 解析（已有 `parseMessageContent`）
- ❌ 本地动画定义（已有 `animations.scss`）
- ❌ 临时测试文件/备份文件
- ❌ 使用魔法值/Magic String

---

# 前端规范

## Vue 3 基础 [P1]

**必须**: `<script setup>` | PascalCase 文件名 | `scoped` 样式 | `v-for` 指定 `:key`

**禁止**: Options API | 直接修改 props | `v-html` 处理用户输入

## 组件结构 [P1]

```vue
<template>...</template>

<script setup>
// 1.导入 2.Props 3.Emits 4.响应式 5.计算 6.方法 7.生命周期 8.监听
</script>

<style lang="scss" scoped>/* 仅组件特有样式 */</style>
```

## 响应式数据 [P1]

```javascript
// ✅
const count = ref(0)           // 简单类型
const user = reactive({...})    // 对象
const { name } = toRefs(user)   // 解构

// ❌
const { name } = user          // 失去响应性
```

## API 封装 [P1]

```javascript
// ✅ 统一封装
export function getUserList(params) {
  return request({ url: '/api/im/user/list', method: 'get', params })
}

// ❌ 组件内直接用 axios
```

**目录**: `src/api/` | request.js + 模块文件

## 路由规范 [P1]

```javascript
// ✅ 使用路由名称
router.push({ name: 'UserProfile' })

// ❌ 硬编码路径
router.push('/user-profile')
```

**命名**: 大驼峰 `UserProfile`，禁止 `user-profile`

## 状态管理 [P2]

```javascript
// ✅
store.getters['user/isLoggedIn']
store.dispatch('user/login', {...})

// ❌ 直接修改
store.state.user.token = 'xxx'
```

**存储**: 全局共享(Vuex) | 模块共享(Module) | 组件私有(ref)

## 存储规范 [P1]

```javascript
// ✅ 统一封装
import { getToken, setToken } from '@/utils/storage'

// ❌ 直接使用
localStorage.setItem('im_token', token)
```

## 样式约束 [P1]

**禁止**: 全局动画(@keyframes) | 重复滚动条样式 | 与 global.scss 重复变量

---

# 数据库规范

## 表设计 [P0]

- 表名 `snake_case` | 主键 `id(BIGINT)`
- `create_time`/`update_time` | 逻辑删除 `del_flag`
- 字符集 `utf8mb4`

## SQL 规范 [P0]

- ❌ `SELECT *` | WHERE函数操作 | `COUNT(*)`
- ✅ 前缀匹配 `LIKE 'keyword%'` | 大表分页

## 修改约束 [P0]

```
创建迁移脚本 → 更新Entity → 更新Mapper
```

**位置**: `sql/migrations/YYYYMMDD_描述.sql` | 可重复执行(IF EXISTS)

## 核心表

| 表名 | 说明 |
|------|------|
| im_conversation | 会话 |
| im_conversation_member | 会话成员 |
| im_message | 消息 |
| im_user | 用户 |
| im_group | 群组 |
| im_friend | 好友 |

**字段命名**: 数据库 `snake_case` → Java/前端 `camelCase`

---

# 项目信息

## 概述

RuoYi-IM 是内网企业级即时通讯系统。

| 模块 | 端口 | 技术栈 |
|------|------|--------|
| ruoyi-im-api | 8080 | Spring Boot 2.7、WebSocket |
| ruoyi-im-web | 5173 | Vue 3、Element Plus |

**核心功能**: 即时通讯、联系人、工作台、视频会议、云盘、考勤、文档协作

## 开发环境

```
JDK 1.8 | Maven 3.6+ | MySQL 5.7(utf8mb4) | Redis 3.0+ | Node.js 16+
```

**测试环境**:
- MySQL: `172.168.20.222:3306/im` (im/123456)
- Redis: `172.168.20.222:6379` (密码: 123456)

## 常用命令

```bash
# 后端
cd ruoyi-im-api && mvn clean package

# 前端
cd ruoyi-im-web && npm install && npm run dev

# 数据库
mysql -h 172.168.20.222 -u im -p123456 im
```

## 核心 Controller

| Controller | 路径 | 功能 |
|------------|------|------|
| ImAuthController | /api/im/auth | 登录/注册 |
| ImMessageController | /api/im/message | 消息 |
| ImConversationController | /api/im/conversation | 会话 |
| ImContactController | /api/im/contact | 联系人 |
| ImUserController | /api/im/user | 用户 |
| ImGroupController | /api/im/group | 群组 |
| ImVideoMeetingController | /api/im/meeting | 视频会议 |
| ImCloudDriveController | /api/im/cloud | 云盘 |
| ImTodoController | /api/im/todo | 待办 |
| ImApprovalController | /api/im/approval | 审批 |
| ImMailController | /api/im/mail | 邮件 |
| ImDocumentController | /api/im/document | 文档 |

**管理API** (ADMIN/SUPER_ADMIN): `/api/admin/users|groups|messages|stats`

## WebSocket

| 类型 | 方向 | 说明 |
|------|------|------|
| auth | C→S | 认证 |
| message | 双向 | 消息 |
| ping/pong | 双向 | 心跳30s |
| read | C→S | 已读 |
| typing | C→S | 输入中 |

**地址**: `ws://localhost:8080/ws/im?token=xxx`

## 已知问题

| 问题 | 解决方案 |
|------|---------|
| 会话重复 | 后端按 `peerUserId` 去重，前端按 `id`+`peerId` 去重 |
| 好友重复 | SQL去重 + service检查 + 前端去重 |
| 字段不一致 | 前端用 `sessionId`，API转 `conversationId` |

## 前端目录

```
ruoyi-im-web/src/
├── api/           # API接口
├── components/    # 组件(Common/Chat/Contacts)
├── views/         # 页面
├── store/         # Vuex
├── router/        # 路由
├── styles/        # 全局样式
├── utils/         # 工具
└── composables/   # 组合式函数
```

## Swagger 规范

```java
@Tag(name = "用户管理", description = "用户管理接口")
public class XxxController {
    @Operation(summary = "查询用户", description = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<UserVO> getUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        // ...
    }
}
```

## 全局异常处理

`com.ruoyi.im.handler.GlobalExceptionHandler`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)     // 业务异常
    @ExceptionHandler(MethodArgumentNotValidException.class)  // 校验异常
    @ExceptionHandler(Exception.class)            // 系统异常
}
```

---

# 文件操作约束

## 创建前 [P1]

1. Glob 搜索确认无重复
2. 确认命名符合规范
3. 确认位置正确

## 删除前 [P0]

1. Grep 搜索所有引用
2. 确认无引用后删除

## 负面清单 [P0]

**禁止修改**: `pom.xml` 版本 | `vite.config.js` 核心 | `.gitignore` 核心

**禁止创建**: `*-test.js` | `*-backup.*` | `*.old` | `temp-*` | `DEBUG_*`

---

# 检查清单 [P0]

提交前快速检查：

| # | 检查项 |
|---|--------|
| 1 | JDK 1.8 兼容（无新语法） |
| 2 | 分层架构（无跨层） |
| 3 | 安全规范（无硬编码密码） |
| 4 | 日志规范（用logger非System.out） |
| 5 | 命名规范（无data/info/temp） |
| 6 | 异常处理（不吞异常） |
| 7 | 魔法值（用常量/枚举） |
| 8 | Lambda查询（无Magic String） |
| 9 | 无冗余代码（无重复/死代码） |
| 10 | 功能验证（测试通过） |

---

# FAQ

**Q: 用户说"直接做"需要确认吗？**
A: 不需要，"直接做"/"不用确认"/"按你说的做"可跳过确认

**Q: 小改动需要完整流程吗？**
A: 不需要，搜索→确认→修改→验证即可

**Q: 必须用 DTO/VO 吗？**
A: 个人开发可用 Entity + VO，注意不暴露敏感字段

**Q: P2 规范可以不遵守吗？**
A: 可以，P0/P1 必须遵守，P2 可简化，P3 可跳过

---

*详细文档请查看 `docs/` 目录*
