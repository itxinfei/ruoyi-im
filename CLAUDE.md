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

**开发模式**: **个人全栈开发** - 可适当简化部分规范（见下方说明）。

---

## ⚡ 快速参考（AI必读）

**遇到以下场景时，先查看对应规则：**

| 场景 | 查看章节 | 优先级 |
|------|---------|--------|
| 创建新文件 | [前端文件创建约束](#前端文件创建约束)、[后端类创建约束](#后端类创建约束) | P0 |
| 修改数据库 | [数据库修改约束](#数据库修改约束) | P0 |
| 写 SQL/查询 | [MyBatis-Plus 规范](#mybatis-plus-规范)、[数据库规范](#数据库规范) | P0 |
| 配置密码/密钥 | [配置安全规范](#配置安全规范-p0) | P0 |
| 打日志 | [日志规范](#日志规范-p0) | P0 |
| 命名变量/方法 | [命名规范](#命名规范-p0) | P0 |
| 创建 Vue 组件 | [Vue 3 开发规范](#vue-3-开发规范) | P1 |
| 写 API 接口 | [前端 API 封装规范](#前端-api-封装规范) | P1 |
| 提交代码前 | [检查清单](#检查清单) 核心10项 | P0 |

**个人开发捷径**：
- 小改动：快速搜索 → 确认位置 → 直接修改 → 运行验证
- 大改动：列出计划 → 等待确认 → 执行修改

---

## 约束优先级说明

为提高开发效率，规范按优先级分类：

| 优先级 | 含义 | 违规后果 | 适用场景 |
|--------|------|---------|---------|
| **[P0]** | 强制执行 | 视为失败 | 所有开发 |
| **[P1]** | 必须遵守 | 强烈建议修改 | 所有开发 |
| **[P2]** | 推荐遵守 | 建议优化 | 生产代码 |
| **[P3]** | 最佳实践 | 可选参考 | 时间充裕时 |

**个人开发模式**：P0、P1 必须遵守；P2 可根据项目规模简化；P3 可跳过。

---

## 个人全栈开发模式适配

作为个人开发者，以下规范可适当简化：

### ✅ 可简化（不降低质量）

| 规范 | 个人开发简化方案 |
|------|-----------------|
| **DTO/VO 分离** | 简化为 Entity + VO，或仅用 VO（注意字段安全） |
| **多环境配置** | 仅保留 dev + prod，使用环境变量区分 |
| **测试要求** | 核心功能测试即可，不需要 100% 覆盖 |
| **代码审查** | 使用自检清单（本文档末尾精简版） |
| **文档注释** | 公共方法必须注释，私有方法可简化 |
| **目录结构** | 小项目可合并目录，但保持分层 |

### ❌ 不能简化（确保质量）

| 规范 | 原因 |
|------|------|
| **分层架构** | 个人项目更需要清晰分层，长期维护必备 |
| **安全规范** | 个人项目同样面临安全风险 |
| **命名规范** | 好的命名节省大量回忆时间 |
| **Git 规范** | 清晰的提交历史帮助回溯问题 |
| **日志规范** | 出问题时日志是唯一线索 |
| **JDK 1.8 约束** | 部署环境限制，必须遵守 |

### 📋 个人开发精简流程

```
个人开发推荐流程（简化版）：
┌─────────────────────────────────────────────────────────────┐
│  1. 🔍 快速搜索 → Grep 搜索是否有类似实现                   │
│  2. 📖 确认位置 → Read 确认修改位置（不必全文阅读）         │
│  3. ✏️ 直接修改 → 按规范执行（P0/P1 必须遵守）             │
│  4. 🧪 快速验证 → 运行测试确保不报错                        │
└─────────────────────────────────────────────────────────────┘
```

**说明**：小型改动无需等待确认，涉及架构调整或删除代码时仍需确认。

---

# 目录索引

## 核心规范
- [约束优先级说明](#约束优先级说明) - P0-P3 分级说明
- [个人全栈开发模式适配](#个人全栈开发模式适配) - 可简化规范说明
- [大模型强制执行流程](#-大模型强制执行流程) - 最高优先级规则
- [绝对红线](#绝对红线) - P0 红线规则

## 编码规范 [P0-P2]
- [JDK 1.8 语法约束](#jdk-18-语法约束-p0) - [P0] Java 版本限制
- [分层架构](#分层架构-p0) - [P0] Controller/Service/Mapper 职责
- [命名规范](#命名规范-p0) - [P0] 类、方法、变量命名
- [前后端数据隔离规范](#前后端数据隔离规范-p1) - [P1] DTO/VO/Entity 分离（个人可简化）
- [MyBatis-Plus 规范](#mybatis-plus-规范) - Lambda 查询、禁止 Magic String
- [数据库规范](#数据库规范) - 表设计、SQL 规范

## 专项规范 [P0-P2]
- [日志规范](#日志规范-p0) - [P0] 日志级别、禁止行为
- [配置安全规范](#配置安全规范-p0) - [P0] 环境变量、敏感信息管理
- [事务规范](#事务规范-p1) - [P1] 超时设置、使用场景
- [常量管理规范](#常量管理规范-p1) - [P1] 魔法值禁止、常量定义
- [前端存储规范](#前端存储规范-p1) - [P1] localStorage 约束

## 前端规范 [P1-P2]
- [Vue 3 开发规范](#vue-3-开发规范-p1) - [P1] 组件命名、模板/脚本/样式规范、性能优化
- [前端 API 封装规范](#前端-api-封装规范-p1) - [P1] API 统一管理、请求/响应处理
- [前端状态管理规范](#前端状态管理规范-p2) - [P2] Vuex 使用规范
- [前端路由规范](#前端路由规范-p1) - [P1] 路由配置、导航守卫
- [前端文件创建约束](#前端文件创建约束-p1) - [P1] 组件创建规则
- [样式修改约束](#样式修改约束-p2) - [P2] 全局样式管理

## 代码质量 [P1-P3]
- [代码质量标准](#代码质量标准-p2) - [P2] 注释、异常、复杂度
- [大模型开发禁止清单](#大模型开发常见问题防范-p1) - [P1] 代码重复、工具类问题
- [检查清单](#检查清单) - **个人开发核心10项** + 详细清单
- [常见问题 FAQ](#常见问题-faq) - 开发流程、技术选择、规范优先级常见问题

## 约束类 [P0-P2]
- [后端类创建约束](#后端类创建约束-p1) - [P1] Controller/Service/Mapper 创建前检查
- [工具类创建约束](#工具类创建约束-p2) - [P2] 禁止创建重复工具
- [代码删除约束](#代码删除约束-p0) - [P0] 删除前必须执行
- [第三方库引入约束](#第三方库引入约束-p2) - [P2] 库引入评估
- [数据库修改约束](#数据库修改约束-p0) - [P0] 表结构修改流程
- [测试文件约束](#测试文件约束-p2) - [P2] 测试文件创建规则（个人开发可简化）
- [重构约束](#重构约束-p2) - [P2] 重构前必须
- [文件修改负面清单](#文件修改负面清单-p0) - [P0] 禁止修改的配置

## 项目文档
- [项目概述](#项目概述) - 内网 IM 系统介绍
- [项目结构](#项目结构) - 目录结构说明
- [常用命令](#常用命令) - 构建运行命令
- [开发环境](#开发环境) - JDK、Maven、MySQL 版本
- [测试环境配置](#测试环境配置) - 内网环境配置
- [系统架构](#系统架构) - 前后端通信、核心 Controller
- [技术栈总结](#技术栈总结-p3) - [P3] 后端/前端技术栈

## 技术参考
- [数据库设计](#数据库设计) - 核心表结构
- [MyBatis-Plus 规范](#mybatis-plus-规范-p0) - [P0] Lambda 查询、禁止 Magic String
- [数据库规范](#数据库规范-p0) - [P0] 表设计、SQL 规范
- [WebSocket 消息类型](#websocket-消息类型-p1) - [P1] 消息类型说明
- [已知问题与解决方案](#已知问题与解决方案-p2) - [P2] 历史问题记录
- [配置文件](#配置文件-p2) - [P2] 配置文件说明

---

## 🚨 大模型强制执行流程

**这是最重要的规则，必须始终遵循：**

```
┌─────────────────────────────────────────────────────────────────┐
│                     任何代码修改的强制流程                      │
├─────────────────────────────────────────────────────────────────┤
│  1. 🔍 搜索  → Grep/Glob 搜索现有实现                          │
│  2. 📖 读取  → Read 工具读取相关文件                           │
│  3. 💬 确认  → 向用户说明方案并等待确认                        │
│  4. ✏️ 修改  → 执行修改                                       │
│  5. ✓ 验证  → 搜索确认无遗留问题                              │
└─────────────────────────────────────────────────────────────────┘
```

### 禁止行为（绝对红线）

| 行为 | 后果 |
|------|------|
| ❌ 不搜索就创建新文件 | 违规 |
| ❌ 不读取就修改文件 | 违规 |
| ❌ 不确认就删除代码 | 违规 |
| ❌ 创建重复功能的组件/工具/API | 违规 |
| ❌ 在组件内重复定义全局样式 | 违规 |
| ❌ 创建临时/测试/备份文件 | 违规 |

### 用户明确跳过确认的情况

只有当用户说以下语句时可以跳过步骤 3：
- "直接做"
- "不用确认"
- "直接执行"
- "按你说的做"

**其他任何情况都必须先确认！**

---

# 绝对红线 [P0]

以下规则**必须严格遵守**，违规即为失败：

| 红线类型 | 规则 | 个人开发备注 |
|---------|------|-------------|
| **JDK 版本** | 强制使用 JDK 1.8 语法，禁止任何 Java 9+ 特性 | 部署环境限制，必须遵守 |
| **分层架构** | Controller → Service → Mapper 单向依赖，禁止跨层调用 | 个人项目同样需要 |
| **数据隔离** | 禁止 Entity 直接作为 API 入参或返回值，必须使用 DTO/VO | 可简化为 Entity + VO |
| **增量开发** | 编写代码前必须先搜索现有实现，确认无重复 | 个人开发重点避免重复造轮子 |
| **垃圾代码** | 禁止创建冗余工具类、重复功能、无用代码 | 个人开发尤其注意 |
| **文件操作** | 任何文件操作前必须先用 Read/Grep 确认，禁止盲写 | 防止破坏现有代码 |
| **创建约束** | 创建新文件前必须 Glob 检查目录，禁止创建重复组件 |
| **修改顺序** | 必须遵循 "搜索 → 读取 → 确认 → 修改 → 验证" 流程 |
| **日志规范** | 禁止使用 System.out.println（启动 Banner 除外） |
| **敏感配置** | 禁止在配置文件中硬编码密码和密钥 |
| **Magic String** | 禁止在 MyBatis-Plus 查询中使用字符串形式的字段名 |

---

# 大模型开发常见问题防范 [P1]

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

## 🔴 大模型开发禁止清单（2025-01-30 更新）

### 代码重复问题
- ❌ **创建本地格式化函数**：已有 `utils/message.js` 的 `formatFileSize`、`formatRelativeTime`，组件内重复定义 35+ 次
- ❌ **内联 JSON 解析**：已有 `utils/message.js` 的 `parseMessageContent`，组件内 `JSON.parse` 重复 25+ 次
- ❌ **本地动画定义**：已有 `styles/animations.scss` 的全局动画，组件内 `@keyframes` 重复 30+ 次
- ✅ **必须先搜索**：Grep/Glob 搜索已有实现，引用现有工具函数

### 组件管理问题
- ❌ **创建重复组件**：如 `EmojiPicker` 和 `GroupDetailDialog` 重复
- ❌ **创建未使用组件**：如 `MessageStatisticsDialog`、`UserTagsDialog` 等创建后从未引用
- ❌ **错误导入路径**：如 `@/utils/format` 不存在却导入
- ✅ **删除前确认**：用 Grep 搜索所有引用，确认无引用后再删除

### 工具函数问题
- ❌ **不使用现有工具**：`utils/message.js` 有完整格式化函数，组件全部重写
- ❌ **创建临时测试文件**：`debug-draft.js`、`test-draft.js` 等遗留
- ❌ **创建备份文件**：`global.scss.backup` 等
- ✅ **先查后用**：Read 工具文件确认函数存在，直接引用

### 导入路径问题
- ❌ **错误导入路径**：`@/components/EditProfileDialog/index.vue` 路径错误
- ❌ **不存在的文件夹**：Glob 先确认文件夹存在
- ✅ **验证路径**：创建文件前检查目录结构

### 前端开发规范
- ❌ **内联样式重复**：相同样式在 40+ 组件重复定义（如滚动条、暗色模式）
- ❌ **动画名称冲突**：本地动画覆盖全局同名动画
- ✅ **使用全局样式**：引用 `design-tokens.scss` 和 `animations.scss`

### 日志相关问题
- ❌ **使用 System.out.println**：应用 slf4j logger
- ❌ **使用 e.printStackTrace()**：应使用 log.error()
- ❌ **日志中输出敏感信息**：禁止输出密码、token 等

### 配置相关问题
- ❌ **硬编码敏感信息**：密码、密钥必须使用环境变量
- ❌ **生产环境使用 debug 级别**：生产环境应使用 info 或 warn

### 常量相关问题
- ❌ **使用魔法值**：状态值、配置值必须提取为常量
- ❌ **Magic String 查询**：MyBatis-Plus 必须使用 Lambda
- ❌ **重复定义常量**：优先使用已有常量类

## 幻觉问题防范

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

# JDK 1.8 语法约束 [P0]

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

# 分层架构 [P0]

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

# 命名规范 [P0]

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

# 前后端数据隔离规范 [P1]

## 三层隔离原则

```
数据库字段 ≠ API 接口字段 ≠ 前端页面字段
```

### 企业开发（完整隔离）
必须使用三层隔离：
- **Entity**: 数据库实体映射
- **DTO**: 接收前端请求参数
- **VO**: 返回前端响应数据

### 个人开发（可简化）
```
数据库 ← Entity → VO → 前端
              ↑
            DTO（复杂接口时使用）
```

| 场景 | 推荐方案 |
|------|---------|
| 简单 CRUD | Entity + VO 即可 |
| 复杂查询/表单 | 使用 DTO |
| 敏感数据 | 必须使用 VO 隐藏字段 |

## Entity 规范 [P0]
- 添加 `@TableName` 指定表名
- 数据库字段使用 `@TableField` 映射
- 非数据库字段标注 `@TableField(exist = false)`

## VO 规范 [P1]
- 只包含返回给前端的字段
- **必须排除敏感字段**（密码、token等）
- 可包含关联查询的字段

## DTO 规范 [P2]（个人开发可选）
- 只包含前端传递的字段
- 不包含后端生成的字段（如 id、createTime）
- 添加 `@NotNull`、`@NotBlank` 等校验注解

---

# 后端类创建约束 [P1]

## 创建 Controller 前必须执行
1. Grep 搜索 `*Controller.java` 确认无重复
2. 确认路由路径不冲突（如 `/api/im/xxx` 已有）
3. 创建对应的 Service 和 Mapper

## 创建 Service 前必须执行
1. 确认业务逻辑不属于现有 Service
2. 创建对应的 Mapper（如需数据库操作）

## 禁止
- ❌ 创建只有一个方法的 Controller（合并到现有 Controller）
- ❌ 创建没有数据库操作的 Mapper
- ❌ Service 直接调用其他 Service 的 Mapper（通过 Service 调用）

---

# Vue 3 开发规范 [P1]

## 一、组件命名规范

### 文件命名
- **组件文件**: 使用 PascalCase（大驼峰）
  - ✅ `UserProfile.vue`、`ChatHeader.vue`、`MessageList.vue`
  - ❌ `userProfile.vue`、`chat_header.vue`、`message-list.vue`
- **视图文件**: 使用 PascalCase
  - ✅ `LoginPage.vue`、`Dashboard.vue`、`UserManagement.vue`
- **工具文件**: 使用 camelCase（小驼峰）
  - ✅ `formatDate.js`、`request.js`、`storage.js`

### 组件名称注册
```javascript
// ✅ 正确：组件名与文件名一致
export default {
  name: 'UserProfile'  // 与 UserProfile.vue 一致
}

// ✅ 单文件组件使用 script setup 时，工具会自动推断
```

### 组件引用
```vue
<script setup>
// ✅ 正确：使用 PascalCase 导入
import UserProfile from '@/components/UserProfile.vue'
import ChatHeader from '@/components/Chat/ChatHeader.vue'

// ❌ 错误：使用别名的缩写
import UP from '@/components/UserProfile.vue'
</script>
```

## 二、文件和目录结构

### 目录组织
```
src/
├── api/               # API 接口
├── assets/            # 静态资源（图片、字体）
├── components/        # 通用组件
│   ├── Common/        # 通用组件
│   ├── Chat/          # 聊天相关组件
│   └── Contacts/      # 联系人相关组件
├── composables/       # 组合式函数
├── constants/         # 常量定义
├── directives/        # 自定义指令
├── router/            # 路由配置
├── store/             # 状态管理
├── styles/            # 全局样式
├── utils/             # 工具函数
└── views/             # 页面视图
```

### 组件文件顺序
```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup>
// 1. 导入
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'

// 2. Props 定义
const props = defineProps({
  // ...
})

// 3. Emits 定义
const emit = defineEmits(['update', 'change'])

// 4. 响应式数据
const count = ref(0)

// 5. 计算属性
const doubleCount = computed(() => count.value * 2)

// 6. 方法
const increment = () => {
  count.value++
}

// 7. 生命周期
onMounted(() => {
  // ...
})

// 8. 监听
watch(() => props.value, (newVal) => {
  // ...
})
</script>

<style lang="scss" scoped>
/* 样式内容 */
</style>
```

## 三、模板（Template）规范

### 指令使用
```vue
<template>
  <!-- ✅ v-if 用于条件渲染 -->
  <div v-if="isLoading">加载中...</div>
  <div v-else-if="hasError">加载失败</div>
  <div v-else>内容区域</div>

  <!-- ✅ v-show 用于频繁切换 -->
  <div v-show="isVisible">可见内容</div>

  <!-- ✅ v-for 必须指定 key -->
  <div v-for="item in list" :key="item.id">{{ item.name }}</div>

  <!-- ❌ 禁止：v-for 不使用 key -->
  <div v-for="item in list">{{ item.name }}</div>

  <!-- ✅ 事件修饰符按顺序 -->
  <button @click.stop.prevent="handleSubmit">提交</button>
</template>
```

### 模板表达式
```vue
<template>
  <!-- ✅ 简单表达式 -->
  <div>{{ fullName }}</div>
  <div :class="{ active: isActive }"></div>

  <!-- ❌ 复杂逻辑应移到 computed -->
  <div>{{ user.firstName + ' ' + user.lastName }}</div>

  <!-- ✅ 正确：使用 computed -->
  <div>{{ fullName }}</div>
</template>
```

### 插值和绑定
```vue
<template>
  <!-- ✅ 文本插值 -->
  <div>{{ message }}</div>

  <!-- ✅ 属性绑定 -->
  <div :id="dynamicId"></div>
  <img :src="imageSrc" :alt="imageAlt" />

  <!-- ✅ class 绑定 -->
  <div :class="{ active: isActive, disabled: isDisabled }"></div>
  <div :class="[classA, classB]"></div>

  <!-- ✅ style 绑定 -->
  <div :style="{ color: activeColor, fontSize: fontSize + 'px' }"></div>

  <!-- ❌ 禁止：拼接 HTML（安全风险） -->
  <div>{{ rawHtml }}</div>
</template>
```

## 四、脚本（Script）规范

### 必须使用 `<script setup>`
```vue
<!-- ✅ 正确：使用 setup 语法 -->
<script setup>
import { ref } from 'vue'

const count = ref(0)
</script>

<!-- ❌ 错误：使用 Options API -->
<script>
export default {
  data() {
    return { count: 0 }
  }
}
</script>
```

### 响应式数据定义
```javascript
// ✅ 简单类型使用 ref
const count = ref(0)
const message = ref('Hello')
const isActive = ref(false)

// ✅ 对象类型使用 reactive
const user = reactive({
  id: 1,
  name: 'Alice',
  profile: { age: 25 }
})

// ✅ 数组使用 ref
const list = ref([])

// ❌ 错误：解构 reactive 会失去响应性
const { name } = user  // name 不是响应式的

// ✅ 正确：使用 toRefs
import { toRefs } from 'vue'
const { name } = toRefs(user)
```

### 计算属性
```javascript
// ✅ 优先使用 computed
const fullName = computed(() => {
  return user.firstName + ' ' + user.lastName
})

// ✅ 可写计算属性
const fullName = computed({
  get() {
    return user.firstName + ' ' + user.lastName
  },
  set(value) {
    const parts = value.split(' ')
    user.firstName = parts[0]
    user.lastName = parts[1]
  }
})
```

### Props 定义
```javascript
// ✅ 完整定义：类型、默认值、验证
const props = defineProps({
  // 基础类型检查
  title: String,
  count: Number,
  isActive: Boolean,

  // 带默认值
  message: {
    type: String,
    default: 'Hello'
  },

  // 对象/数组默认值使用工厂函数
  user: {
    type: Object,
    default: () => ({ id: 0, name: '' })
  },
  list: {
    type: Array,
    default: () => []
  },

  // 自定义验证
  age: {
    type: Number,
    validator: (value) => {
      return value >= 0 && value <= 150
    }
  },

  // 必填项
  requiredId: {
    type: Number,
    required: true
  }
})

// ✅ 使用 TypeScript 更佳（如项目支持）
interface Props {
  title: string
  count?: number
  user?: User
}

const props = withDefaults(defineProps<Props>(), {
  count: 0,
  user: () => ({ id: 0, name: '' })
})
```

### Emits 定义
```javascript
// ✅ 明确定义事件
const emit = defineEmits(['update:modelValue', 'change', 'submit'])

// ✅ 带验证的事件
const emit = defineEmits({
  // 无验证
  change: null,

  // 带验证
  submit: (payload) => {
    if (payload.email && payload.name) {
      return true
    } else {
      console.warn('Invalid submit event payload')
      return false
    }
  }
})

// ✅ 使用事件
emit('change', newValue)
emit('submit', { email, name })
```

### 生命周期使用
```javascript
import {
  onMounted,
  onBeforeMount,
  onUpdated,
  onBeforeUnmount,
  onUnmounted
} from 'vue'

// ✅ 按需引入生命周期
onMounted(() => {
  console.log('组件已挂载')
})

onBeforeUnmount(() => {
  console.log('组件即将卸载')
})

// ❌ 禁止：使用 Options API 生命周期
export default {
  mounted() { }  // 禁止
}
```

### Watch 使用
```javascript
import { ref, watch } from 'vue'

const count = ref(0)
const user = reactive({ name: 'Alice' })

// ✅ 监听 ref
watch(count, (newVal, oldVal) => {
  console.log(`count changed: ${oldVal} -> ${newVal}`)
})

// ✅ 监听多个源
watch([count, () => user.name], ([newCount, newName]) => {
  console.log(`count: ${newCount}, name: ${newName}`)
})

// ✅ 深度监听
watch(user, (newVal) => {
  console.log('user changed', newVal)
}, { deep: true })

// ✅ 立即执行
watch(count, (val) => {
  console.log(val)
}, { immediate: true })

// ✅ 监听对象属性（推荐：使用 getter 函数）
watch(() => user.name, (newName) => {
  console.log('name changed to', newName)
})
```

### 组件暴露
```javascript
// ✅ 定义暴露给父组件的方法/属性
const resetForm = () => {
  // ...
}

const validate = () => {
  // ...
}

defineExpose({
  resetForm,
  validate,
  internalData: ref('internal')
})
```

## 五、样式（Style）规范

### Scoped 要求
```vue
<!-- ✅ 组件样式必须使用 scoped -->
<style lang="scss" scoped>
.message-item {
  padding: 10px;
}
</style>

<!-- ✅ 或使用 module -->
<style module>
.messageItem {
  padding: 10px;
}
</style>

<!-- ❌ 禁止：全局样式（除非必要） -->
<style>
.message-item {
  padding: 10px;
}
</style>
```

### 样式选择器
```vue
<style scoped>
/* ✅ 使用类选择器 */
.message-container { }
.message-item { }

/* ❌ 避免直接使用元素选择器 */
div { }
span { }

/* ✅ 属性选择器用于特定元素 */
input[type="text"] { }
button[disabled] { }

/* ✅ 伪类 */
.item:hover { }
.item:focus { }
.item:nth-child(2n) { }
</style>
```

### CSS 变量使用
```vue
<template>
  <div class="card" :style="cardStyle">卡片</div>
</template>

<style scoped>
/* ✅ 优先使用设计令牌变量 */
.card {
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  padding: var(--spacing-md);
}

/* ❌ 避免硬编码颜色值 */
.card {
  background-color: #ffffff;
  border: 1px solid #dcdfe6;
}
</style>
```

## 六、组件通信规范

### 父子组件通信
```vue
<!-- 父组件 -->
<template>
  <!-- ✅ v-model 双向绑定 -->
  <ChildComponent v-model:value="inputValue" />

  <!-- ✅ 明确的 props 和 events -->
  <ChildComponent
    :title="title"
    :items="list"
    @update="handleUpdate"
    @delete="handleDelete"
  />
</template>

<!-- 子组件 -->
<script setup>
const props = defineProps({
  modelValue: String,
  title: String,
  items: Array
})

const emit = defineEmits(['update:modelValue', 'update', 'delete'])

// 更新 v-model
const updateValue = (val) => {
  emit('update:modelValue', val)
}
</script>
```

### 依赖注入
```vue
<!-- 祖先组件 -->
<script setup>
import { provide } from 'vue'
import { symbolKey } from './keys'

provide(symbolKey, {
  theme: ref('light'),
  updateTheme: (newTheme) => { }
})
</script>

<!-- 后代组件 -->
<script setup>
import { inject } from 'vue'
import { symbolKey } from './keys'

const { theme, updateTheme } = inject(symbolKey)
</script>
```

## 七、性能优化规范

### v-for 和 key
```vue
<template>
  <!-- ✅ 唯一、稳定的 key -->
  <div v-for="item in list" :key="item.id">{{ item.name }}</div>

  <!-- ❌ 禁止：使用 index 作为 key（列表会变化时） -->
  <div v-for="(item, index) in list" :key="index">{{ item.name }}</div>

  <!-- ✅ 静态列表可以使用 index -->
  <div v-for="(item, index) in staticList" :key="index">{{ item }}</div>
</template>
```

### v-if vs v-show
```vue
<template>
  <!-- ✅ 很少切换：使用 v-if -->
  <div v-if="showDetails">详情内容（很多）</div>

  <!-- ✅ 频繁切换：使用 v-show -->
  <div v-show="isVisible">切换内容</div>
</template>
```

### 计算属性缓存
```javascript
// ✅ 使用 computed（有缓存）
const filteredList = computed(() => {
  return list.value.filter(item => item.active)
})

// ❌ 避免使用方法（每次重新计算）
const filteredList = () => {
  return list.value.filter(item => item.active)
}
```

### 懒加载组件
```javascript
// ✅ 路由级懒加载
const Dashboard = () => import('@/views/Dashboard.vue')
const UserManagement = () => import('@/views/UserManagement.vue')

// ✅ 组件级懒加载
const HeavyComponent = defineAsyncComponent(() =>
  import('@/components/HeavyComponent.vue')
)
```

### 列表虚拟化
```vue
<template>
  <!-- ✅ 大列表使用虚拟滚动 -->
  <VirtualList
    :items="largeList"
    :item-height="50"
    :visible-count="20"
  />
</template>
```

## 八、类型安全（推荐）

### 使用 TypeScript
```vue
<script setup lang="ts">
import { ref, computed } from 'vue'

// ✅ 定义接口
interface User {
  id: number
  name: string
  email: string
}

// ✅ 类型化 props
interface Props {
  user: User
  title?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: '默认标题'
})

// ✅ 类型化 emits
const emit = defineEmits<{
  update: [value: string]
  change: [id: number, value: string]
}>()

// ✅ 类型化 ref
const userList = ref<User[]>([])
</script>
```

## 九、可访问性（A11Y）

### 语义化 HTML
```vue
<template>
  <!-- ✅ 使用语义化标签 -->
  <nav>导航</nav>
  <main>主要内容</main>
  <aside>侧边栏</aside>
  <footer>页脚</footer>

  <!-- ✅ 按钮使用 button 元素 -->
  <button @click="submit">提交</button>

  <!-- ❌ 避免非语义化 -->
  <div @click="submit">提交</div>
</template>
```

### ARIA 属性
```vue
<template>
  <!-- ✅ 添加必要的 ARIA 属性 -->
  <button
    aria-label="关闭对话框"
    @click="close"
  >
    <CloseIcon />
  </button>

  <!-- ✅ 表单关联 -->
  <label for="username">用户名</label>
  <input
    id="username"
    v-model="username"
    aria-required="true"
    aria-invalid="hasError"
  />

  <!-- ✅ 加载状态 -->
  <div
    v-if="isLoading"
    role="status"
    aria-live="polite"
  >
    加载中...
  </div>
</template>
```

## 十、禁止清单

| 行为 | 说明 |
|------|------|
| ❌ 禁止使用 Options API | 必须使用 Composition API + setup |
| ❌ 禁止全局样式（除非必要） | 组件样式必须 scoped |
| ❌ 禁止 v-for 无 key | 必须指定唯一稳定的 key |
| ❌ 禁止在模板中写复杂逻辑 | 移至 computed 或方法 |
| ❌ 禁止直接修改 props | 必须通过 emit 通知父组件 |
| ❌ 禁止使用 this | setup 语法中没有 this |
| ❌ 禁止使用 v-html 处理用户输入 | XSS 安全风险 |
| ❌ 禁止在 created 中操作 DOM | 应在 onMounted 中 |
| ❌ 禁止深层次 watch（除非必要） | 使用 getter 函数替代 deep |
| ❌ 禁止在 template 中使用 || 运算符设置默认值 | 使用 ?? 或默认值处理 |

---

# 前端 API 封装规范 [P1]

## 一、API 目录组织
```
src/api/
├── request.js          # axios 实例配置
├── auth.js             # 认证相关 API
├── user.js             # 用户相关 API
├── message.js          # 消息相关 API
├── conversation.js     # 会话相关 API
├── contact.js          # 联系人相关 API
├── group.js            # 群组相关 API
└── admin.js            # 管理后台 API
```

## 二、API 文件命名规范
- 使用小写字母 + 连字符：`user-profile.js`、`message-list.js`
- 文件名与功能模块对应：一个业务模块一个文件
- 禁止使用驼峰：`userProfile.js` ❌

## 三、API 函数定义规范

### 基本结构
```javascript
// src/api/user.js

import request from './request'

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise} 用户列表数据
 */
export function getUserList(params) {
  return request({
    url: '/api/im/user/list',
    method: 'get',
    params
  })
}

/**
 * 获取用户详情
 * @param {number} userId - 用户ID
 * @returns {Promise} 用户详情数据
 */
export function getUserById(userId) {
  return request({
    url: `/api/im/user/${userId}`,
    method: 'get'
  })
}

/**
 * 创建用户
 * @param {Object} data - 用户数据
 * @returns {Promise} 创建结果
 */
export function createUser(data) {
  return request({
    url: '/api/im/user',
    method: 'post',
    data
  })
}
```

### 函数命名规范
```javascript
// ✅ 正确命名
getUserList()       // 查询列表
getUserById()       // 查询单个
createUser()        // 创建
updateUser()        // 更新
deleteUser()        // 删除
sendMessage()       // 发送消息
uploadFile()        // 上传文件

// ❌ 错误命名
getUsers()          // 应为 getUserList
user()              // 语义不明确
handleUser()        // handle 语义模糊
doCreate()          // do 前缀多余
```

## 四、请求/响应处理规范

### 统一 Request 配置
```javascript
// src/api/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearToken } from '@/utils/storage'

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 添加 token
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 根据后端约定处理响应
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // 401: 未登录或 token 过期
      if (res.code === 401) {
        clearToken()
        window.location.href = '/login'
      }

      return Promise.reject(new Error(res.message || 'Error'))
    }

    return res.data
  },
  (error) => {
    console.error('Response error:', error)

    // 处理网络错误
    if (error.response) {
      switch (error.response.status) {
        case 400:
          ElMessage.error('请求参数错误')
          break
        case 401:
          ElMessage.error('未授权，请重新登录')
          clearToken()
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络连接失败')
    }

    return Promise.reject(error)
  }
)

export default service
```

### 错误处理规范
```javascript
// ✅ 统一错误处理
export async function loadUserData(userId) {
  try {
    const data = await getUserById(userId)
    return data
  } catch (error) {
    console.error('加载用户数据失败:', error)
    throw error
  }
}

// ✅ 组件中使用
import { getUserById } from '@/api/user'

const loadUser = async () => {
  loading.value = true
  try {
    const data = await getUserById(userId.value)
    user.value = data
  } catch (error) {
    errorMessage.value = '加载用户失败'
  } finally {
    loading.value = false
  }
}
```

## 五、禁止清单

| 行为 | 说明 |
|------|------|
| ❌ 组件内直接使用 axios | 必须使用 api/ 目录封装 |
| ❌ 硬编码 API 路径 | 必须在 request.js 配置 baseURL |
| ❌ 创建重复功能的 API | 复用现有 API 或组合调用 |
| ❌ 不添加 JSDoc 注释 | 必须说明参数和返回值 |
| ❌ 不处理错误 | 必须统一处理请求失败 |
| ❌ 在 API 中处理 UI 逻辑 | API 只负责数据请求 |
| ❌ 绕过拦截器直接请求 | 必须使用统一的 request 实例 |

---

# 前端状态管理规范 [P2]

## 一、Vuex Store 组织
```
src/store/
├── index.js            # store 入口
├── modules/
│   ├── user.js         # 用户状态
│   ├── chat.js         # 聊天状态
│   ├── contact.js      # 联系人状态
│   └── app.js          # 应用状态
└── getters.js          # 全局 getters
```

## 二、模块定义规范
```javascript
// src/store/modules/user.js
import { getToken, setToken, removeToken } from '@/utils/storage'

const state = () => ({
  token: getToken(),
  userInfo: null,
  permissions: []
})

const getters = {
  isLoggedIn: (state) => !!state.token,
  userId: (state) => state.userInfo?.id,
  username: (state) => state.userInfo?.username,
  hasPermission: (state) => (permission) => {
    return state.permissions.includes(permission)
  }
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    setToken(token)
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
  },
  SET_PERMISSIONS(state, permissions) {
    state.permissions = permissions
  },
  CLEAR_USER(state) {
    state.token = ''
    state.userInfo = null
    state.permissions = []
    removeToken()
  }
}

const actions = {
  // 登录
  async login({ commit }, { username, password }) {
    const { data } = await login({ username, password })
    commit('SET_TOKEN', data.token)
    return data
  },

  // 获取用户信息
  async getUserInfo({ commit }) {
    const { data } = await getUserInfo()
    commit('SET_USER_INFO', data)
    commit('SET_PERMISSIONS', data.permissions || [])
    return data
  },

  // 登出
  async logout({ commit }) {
    await logout()
    commit('CLEAR_USER')
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
}
```

## 三、状态使用规范
```vue
<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()

// ✅ 使用 getters（推荐）
const isLoggedIn = computed(() => store.getters['user/isLoggedIn'])
const userId = computed(() => store.getters['user/userId'])

// ✅ 使用 state（必要时）
const userInfo = computed(() => store.state.user.userInfo)

// ✅ 调用 actions
const handleLogin = async () => {
  await store.dispatch('user/login', { username, password })
}

// ✅ 调用 mutations（慎用，优先使用 actions）
const updateToken = (token) => {
  store.commit('user/SET_TOKEN', token)
}

// ❌ 避免：直接修改 state
store.state.user.token = 'xxx'  // 禁止！
</script>
```

## 四、状态划分原则

| 状态类型 | 存储位置 | 示例 |
|---------|---------|-----|
| **全局共享状态** | Vuex | 用户信息、权限、全局配置 |
| **模块共享状态** | Vuex Module | 聊天消息、会话列表、联系人列表 |
| **组件私有状态** | 组件内 ref | 表单输入、本地加载状态 |
| **跨组件共享但简单** | provide/inject | 主题、语言设置 |

## 五、禁止清单
- ❌ 禁止在组件内直接修改 state（必须通过 mutation）
- ❌ 禁止在 mutation 中执行异步操作
- ❌ 禁止在 action 中直接修改 state（必须通过 mutation）
- ❌ 禁止将所有状态都放 Vuex（组件私有状态放在组件内）

---

# 前端路由规范 [P1]

## 一、路由配置组织
```javascript
// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/storage'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginPage.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/MainPage.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/chat'
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/ChatPanel.vue'),
        meta: { title: '聊天', keepAlive: true }
      },
      {
        path: 'contacts',
        name: 'Contacts',
        component: () => import('@/views/ContactsPanel.vue'),
        meta: { title: '联系人', keepAlive: true }
      },
      {
        path: 'workbench',
        name: 'Workbench',
        component: () => import('@/views/WorkbenchPanel.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/admin/AdminLayout.vue'),
        meta: { title: '管理后台', roles: ['ADMIN', 'SUPER_ADMIN'] },
        children: [
          {
            path: 'dashboard',
            name: 'AdminDashboard',
            component: () => import('@/views/admin/Dashboard.vue'),
            meta: { title: '数据概览' }
          },
          {
            path: 'users',
            name: 'UserManagement',
            component: () => import('@/views/admin/UserManagement.vue'),
            meta: { title: '用户管理' }
          }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundPage.vue')
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  }
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - IM系统` : 'IM系统'

  // 登录验证
  const token = getToken()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)

  if (requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 权限验证
  if (to.meta.roles) {
    const userInfo = store.state.user.userInfo
    if (!to.meta.roles.includes(userInfo?.role)) {
      next({ name: 'Forbidden' })
      return
    }
  }

  next()
})

// 全局后置钩子
router.afterEach((to, from) => {
  // 页面访问统计等
})

export default router
```

## 二、路由命名规范
```javascript
// ✅ 正确：使用大驼峰
{
  path: '/user-profile',
  name: 'UserProfile',  // 大驼峰
  component: UserProfile
}

// ❌ 错误
{
  path: '/user-profile',
  name: 'user-profile',  // 小写 + 连字符
  name: 'user_profile',  // 下划线
  name: 'USERPROFILE'    // 全大写
}
```

## 三、路由元信息规范
```javascript
// ✅ 完整的 meta 定义
{
  path: '/admin/users',
  name: 'UserManagement',
  component: UserManagement,
  meta: {
    title: '用户管理',           // 页面标题
    requiresAuth: true,          // 是否需要登录
    roles: ['ADMIN'],            // 允许的角色
    permissions: ['user:read'],  // 需要的权限
    keepAlive: true,             // 是否缓存
    icon: 'User',                // 菜单图标
    order: 1,                    // 排序
    hidden: false,               // 是否隐藏（不在菜单显示）
    affix: false                 // 是否固定（不可关闭）
  }
}
```

## 四、导航规范
```vue
<template>
  <!-- ✅ 声明式导航 -->
  <router-link :to="{ name: 'UserProfile', params: { id: userId } }">
    查看用户
  </router-link>

  <!-- ✅ 编程式导航 -->
  <script setup>
  import { useRouter } from 'vue-router'

  const router = useRouter()

  // 导航到命名路由
  const goToProfile = () => {
    router.push({ name: 'UserProfile', params: { id: userId } })
  }

  // 带查询参数
  const searchUsers = () => {
    router.push({
      path: '/users',
      query: { keyword: searchKeyword.value }
    })
  }

  // 替换当前路由
  const replaceRoute = () => {
    router.replace({ name: 'Home' })
  }

  // 后退/前进
  const goBack = () => {
    router.back()
  }
  </script>
</template>
```

## 五、路由懒加载
```javascript
// ✅ 所有路由组件使用懒加载
const Dashboard = () => import('@/views/Dashboard.vue')
const UserManagement = () => import('@/views/admin/UserManagement.vue')

// ✅ 分组懒加载（将相关路由打包在一起）
const Dashboard = () => import(/* webpackChunkName: "admin" */ '@/views/admin/Dashboard.vue')
const UserManagement = () => import(/* webpackChunkName: "admin" */ '@/views/admin/UserManagement.vue')
```

## 六、禁止清单
| 行为 | 说明 |
|------|------|
| ❌ 硬编码路由跳转 | 使用路由名称而非路径 |
| ❌ 在组件内使用 $router | 应使用 useRouter 组合式函数 |
| ❌ 不处理路由跳转错误 | 使用 try-catch 捕获导航错误 |
| ❌ 滥用 router.go(-1) | 用户可能直接进入页面，无法返回 |
| ❌ 路径参数使用连字符 | /user-profile/:user-id ❌ |

---

# 前端文件创建约束 [P1]

## 创建新组件前必须执行
1. Glob 搜索 `**/components/**/*.vue` 确认无重复
2. Glob 搜索 `**/views/**/*/*.vue` 确认位置正确
3. 确认组件命名符合 PascalCase 规范

## 禁止创建的文件
- ❌ 临时测试文件（test-*.js、debug-*.js、*-draft.js）
- ❌ 备份文件（*.backup、*.bak、*.old）
- ❌ 重复功能的组件

## 组件内 `<style>` 约束
- ❌ 禁止定义全局动画（@keyframes）
- ❌ 禁止重复定义已存在的滚动条样式
- ❌ 禁止创建与 global.scss 重复的变量
- ✅ 组件样式仅限组件特有样式

---

# 代码质量标准 [P2]

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

# 工具类创建约束 [P2]

## 已有工具类清单（禁止创建重复）
- `cn.hutool.core.*` - Hutool 工具库（优先使用）
- `org.apache.commons.lang3.*` - Apache Commons Lang
- `org.springframework.util.*` - Spring 工具类
- `com.ruoyi.common.utils.*` - 若依框架工具类
- `ruoyi-im-web/src/utils/` - 前端工具函数目录

## 创建工具类前必须执行
1. 搜索 Hutool、Apache Commons 是否已有实现
2. 搜索项目中是否已有类似工具类
3. 确认无法通过组合现有工具类实现

## 绝对禁止创建的工具方法
- ❌ 日期格式化（用 Hutool 的 DateUtil）
- ❌ 字符串判空（用 StringUtils）
- ❌ JSON 序列化（用 Jackson 或 JSONUtil）
- ❌ 集合操作（用 CollUtil、Stream）
- ❌ 文件大小格式化（前端已有 formatFileSize）
- ❌ 时间相对格式化（前端已有 formatRelativeTime）

---

# 样式修改约束 [P2]

## 修改全局样式前必须执行
1. Read `ruoyi-im-web/src/styles/global.scss` 确认不存在
2. Read `ruoyi-im-web/src/styles/design-tokens.scss` 确认无变量
3. 确认修改不会影响其他组件

## 组件内 `<style scoped>` 约束
- ✅ 仅限组件特有样式（如特定布局、组件状态）
- ❌ 禁止定义全局动画（@keyframes）
- ❌ 禁止定义滚动条样式（全局已有）
- ❌ 禁止定义暗色模式变量（全局已有）
- ❌ 禁止覆盖 Element Plus 默认样式（应在 global.scss 统一处理）

## 样式变量使用
- 优先使用 `design-tokens.scss` 中定义的变量
- 禁止硬编码颜色值

---

# 代码删除约束 [P0]

## 删除前必须执行
1. **后端**：Grep 搜索所有 `.java` 文件的引用
2. **前端**：Grep 搜索所有 `.vue`、`.js` 文件的引用
3. 确认无引用后方可删除

## 禁止删除
- ❌ 被 `.gitignore` 忽略但仍被引用的文件
- ❌ 注释掉的代码（直接删除，不要注释）
- ❌ 可能被外部系统调用的公开 API

## 删除验证
删除后立即搜索确认无遗留引用

---

# 第三方库引入约束 [P2]

## 引入前必须评估
1. 功能是否已有库可实现（Hutool、Apache Commons、Element Plus）
2. 包大小是否合理（避免引入巨型库）
3. 维护状态是否活跃

## 禁止引入
- ❌ 功能重复的库（如已有 Lombok 不引入 MapStruct）
- ❌ 未经验证的新兴库
- ❌ 许可证不明确的库

## 前端禁止引入
- ❌ UI 组件库（已有 Element Plus）
- ❌ 状态管理库（已有 Vuex）
- ❌ 日期处理库（已有 Day.js）
- ❌ HTTP 客户端（已有 Axios）

---

# 数据库修改约束 [P0]

## 禁止直接操作
- ❌ 禁止直接修改生产数据库
- ❌ 禁止修改表结构而不创建迁移脚本

## 表结构修改流程
1. 在 `sql/migrations/` 创建迁移脚本（命名：YYYYMMDD_描述.sql）
2. 脚本必须可重复执行（使用 IF EXISTS）
3. 更新 Entity 和对应的 Mapper

## 迁移脚本规范
- 必须有回滚步骤（注释形式）
- 必须有变更说明

---

# 测试文件约束 [P2]

### 企业开发要求
## 创建测试文件前
1. 确认测试框架已配置（JUnit、pytest 等）
2. 确认被测试类的功能稳定

## 禁止
- ❌ 创建空的测试文件
- ❌ 创建只有通过/失败断言的测试
- ❌ 创建测试但不运行验证

## 测试覆盖要求
- 核心业务逻辑必须有测试
- 测试必须覆盖正常和异常场景

### 个人开发简化
```
个人开发测试策略：
┌─────────────────────────────────────────────────────────────┐
│  核心 API（用户相关、支付等）    → 必须测试                  │
│  复杂业务逻辑                  → 建议测试                      │
│  简单 CRUD                    → 可跳过，手动验证即可           │
│  工具类（纯函数）              → 建议测试                      │
└─────────────────────────────────────────────────────────────┘
```

**最低要求**：核心功能手动测试通过，确保不出现空指针等基础错误。

---

# 重构约束 [P2]

## 重构定义
重构是**不改变功能**的代码结构调整，如：
- 提取方法
- 重命名变量/方法
- 移动代码位置
- 消除重复

## 重构前必须
1. 明确说明重构目的
2. 列出涉及的所有文件
3. 确认有测试覆盖（或手动验证方案）

## 禁止
- ❌ 以"重构"名义修改业务逻辑
- ❌ 一次重构超过 5 个文件（分批进行）
- ❌ 重构不熟悉的代码

---

# 文件修改负面清单 [P0]

## 禁止修改的配置文件
- ❌ `pom.xml` 的版本号（除非升级）
- ❌ `vite.config.js` 的核心配置
- ❌ `.gitignore` 的核心规则

## 修改前需要通知的文件
- ⚠️ `package.json` 的依赖版本
- ⚠️ `application.yml` 的数据库连接
- ⚠️ 路由配置文件

## 禁止创建的文件模式
- ❌ `*-test.js`、`*-spec.js`（测试用对应目录）
- ❌ `*-backup.*`、`*.old`、`*.bak`
- ❌ `temp-*`、`tmp-*`、`draft-*`
- ❌ `DEBUG_*`、`debug_*`

---

# 强制执行顺序 [P0]

## 任何代码修改必须遵循

```
1️⃣ 搜索 → 用 Grep/Glob 搜索现有实现
2️⃣ 读取 → 用 Read 工具读取相关文件
3️⃣ 确认 → 向用户确认修改方案
4️⃣ 修改 → 用 Edit 工具执行修改
5️⃣ 验证 → 搜索确认无遗留问题
```

## 跳步条件
- 只有用户明确说"直接做"时，可跳过步骤 3
- 其他步骤**禁止跳过**

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

### 强制执行流程

```
用户请求 → Grep/Glob 搜索 → Read 确认 → 向用户汇报 → 等待确认 → 执行修改
```

**禁止跳过**：
- ❌ 不搜索就创建新文件
- ❌ 不读取就修改文件
- ❌ 不确认就删除代码

### 特殊情况

仅当用户明确说以下语句时可跳过确认：
- "直接做"
- "不用确认"
- "直接执行"
- 其他明确表示跳过确认的语句

## 开发闭环

```
上下文检索 → 方案规划 → 代码生成 → 自检验证
```

---

# 检查清单

## 个人开发核心检查（10项）[P0]

提交代码前快速检查：

| # | 检查项 | 说明 |
|---|--------|------|
| 1 | **JDK 1.8 兼容** | 无 var、record、sealed、→、""" 等新语法 |
| 2 | **分层架构** | Controller → Service → Mapper，无跨层调用 |
| 3 | **安全规范** | 无硬编码密码，敏感字段不返回前端 |
| 4 | **日志规范** | 使用 logger 而非 System.out，无敏感信息泄露 |
| 5 | **命名规范** | 类/方法/变量名语义明确，无 data/info/temp |
| 6 | **异常处理** | 不吞异常，关键操作有日志 |
| 7 | **魔法值** | 状态值、配置值使用常量或枚举 |
| 8 | **Lambda 查询** | MyBatis-Plus 使用 Lambda 风格，无 Magic String |
| 9 | **无冗余代码** | 无重复造轮子，无死代码（注释掉的代码） |
| 10 | **功能验证** | 核心功能测试通过，编译无报错 |

**详细检查清单**（生产环境使用，个人开发可选）：

### JDK 1.8 兼容性
- [ ] 无 `var` 关键字
- [ ] 无 `record` 类型
- [ ] 无 `sealed` 关键字
- [ ] 无 `switch →` 语法
- [ ] 无 `"""` 文本块
- [ ] 无 `Optional.isEmpty()`
- [ ] 无 `Collection.toList()`

### 代码质量 [P2]
- [ ] 类有 JavaDoc 类注释
- [ ] public 方法有 JavaDoc 方法注释
- [ ] 业务逻辑有中文注释说明业务背景
- [ ] NPE 防护到位
- [ ] 异常处理完整（不吞异常、有日志）

### 分层架构 [P0]
- [ ] DTO/VO 与 Entity 分离（个人开发可简化）
- [ ] Controller 不直接调用 Mapper
- [ ] Service 有 `@Transactional` 注解
- [ ] 无跨层调用

### 垃圾代码检查 [P1]
- [ ] 无冗余工具类或方法
- [ ] 无重复造轮子
- [ ] 无死代码（注释掉的代码、预留字段）
- [ ] 无过度设计（不必要的抽象）
- [ ] 无魔法值，已提取常量
- [ ] 无无意义的命名（data、info、temp等）
- [ ] 方法长度不超过 50 行
- [ ] 嵌套层级不超过 3 层
- [ ] 无重复的格式化函数（应引用 utils/message.js）
- [ ] 无内联的 JSON.parse（应引用 parseMessageContent）
- [ ] 无组件内重复的动画定义（应引用 animations.scss）
- [ ] 无临时测试文件（test-*.js、debug-*.js、*-draft.js）
- [ ] 无备份文件（*.backup、*.old、*.bak）
- [ ] 无未使用的导入和变量

### 重复代码检查 [P1]
- [ ] 不与现有组件功能重复
- [ ] 不与现有 API 功能重复
- [ ] 不与现有工具类功能重复
- [ ] 不与现有样式定义重复

### 文件操作检查 [P0]
- [ ] 创建新文件前已 Glob 检查目录
- [ ] 修改文件前已 Read 读取内容
- [ ] 删除文件前已 Grep 搜索引用
- [ ] 修改公共代码前已搜索所有引用

### 日志规范检查 [P0]
- [ ] 无 System.out.println（启动 Banner 除外）
- [ ] 无 e.printStackTrace()
- [ ] 日志中无敏感信息泄露
- [ ] 使用 slf4j logger 而非控制台输出

### 配置安全检查 [P0]
- [ ] 配置文件无硬编码密码
- [ ] 敏感配置使用环境变量
- [ ] 生产环境日志级别不为 debug

### 常量管理检查 [P1]
- [ ] 无魔法数字、魔法字符串
- [ ] 状态值使用常量或枚举
- [ ] MyBatis-Plus 使用 Lambda 风格
- [ ] 无 `wrapper.eq("field", value)` 形式的 Magic String

---

# 常见问题 FAQ

## 开发流程相关

**Q: 用户说"直接做"，需要确认吗？**
A: 不需要。用户明确说"直接做"、"不用确认"、"按你说的做"时，可跳过确认步骤直接执行。

**Q: 小改动需要完整流程吗？**
A: 不需要。个人开发模式下，小改动（如修改一个方法、调整样式）可使用简化流程：搜索 → 确认 → 修改 → 验证。

**Q: 什么时候必须先确认？**
A: 以下情况必须确认：
- 删除代码或文件
- 修改 3 个及以上文件
- 修改公共模块（如 utils、api、store）
- 架构性调整

## 技术选择相关

**Q: 个人开发必须用 DTO/VO 吗？**
A: 不必须。可使用 Entity + VO，注意不暴露敏感字段即可。

**Q: 必须写单元测试吗？**
A: 个人开发不强制。核心 API 手动测试通过即可，复杂逻辑建议写测试。

**Q: 可以在组件内直接写 axios 请求吗？**
A: 不可以。必须封装在 `src/api/` 目录，复用统一的 request 实例。

## 规范优先级相关

**Q: P2 规范可以不遵守吗？**
A: 个人开发模式下可以。P0、P1 必须遵守；P2 推荐遵守；P3 可跳过。

**Q: 时间紧时可以跳过哪些规范？**
A: 可跳过：完整注释、单元测试、详细的 JSDoc。不可跳过：分层架构、安全规范、命名规范。

## 常见错误相关

**Q: 出现 "违反绝对红线" 怎么办？**
A: 立即停止当前操作，查看具体违反的红线，修改后重新执行。

**Q: 代码已经写完了才发现违反规范怎么办？**
A: 及时修改，不要抱有侥幸心理。规范是为了长期维护考虑。

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
| `ImVideoMeetingController` | `/api/im/meeting` | 视频会议管理 |
| `ImCloudDriveController` | `/api/im/cloud` | 企业云盘 |
| `ImAttendanceGroupController` | `/api/im/attendance/group` | 考勤组管理 |
| `ImDocumentCollaborationController` | `/api/im/document/collaboration` | 文档协作 |
| `ImMessageMarkerController` | `/api/im/message/marker` | 消息标记/待办 |
| `ImVoiceTranscriptController` | `/api/im/voice/transcript` | 语音转文字 |

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
| **即时通讯** | 单聊、群聊、消息撤回、消息编辑、消息转发、引用回复、消息已读回执 | 支持文本、图片、文件、语音、视频 |
| **联系人** | 好友管理、分组、组织架构树、外部联系人 | 支持按部门/职位浏览 |
| **工作台** | 待办事项、审批流程、邮件、AI 助理、文档、日程 | 企业办公一体化 |
| **视频会议** | 多人视频会议、屏幕共享、会议管理、参会者管理 | 基于 WebRTC 实现 |
| **企业云盘** | 文件存储、文件夹管理、文件分享、回收站、版本管理 | 支持个人/部门/公司三种权限 |
| **考勤管理** | 考勤组、班次管理、排班管理、打卡记录、节假日管理 | 支持多种打卡方式 |
| **文档协作** | 在线文档编辑、协作者管理、权限控制、操作日志 | 实时光标同步 |
| **消息标记** | 消息标记（旗标/重要）、待办提醒、待办完成 | 重要消息不错过 |
| **语音转文字** | 语音消息转文字、多语言支持、转写状态管理 | 支持阿里云/讯飞/腾讯 |
| **全局搜索** | 消息、联系人、群组、文件全文搜索 | 统一搜索入口 |
| **管理后台** | 用户管理、群组管理、消息管理、数据统计 | 基于 @PreAuthorize 权限控制 |

---

# 新增功能模块详解

## P0: 核心增强功能

### 1. 消息已读回执
- `im_message_read_receipt` 表记录已读状态
- 支持单聊和群聊的已读回执
- 自动更新会话成员的 `last_read_message_id`

### 2. 消息撤回配置
- `im_system_config` 表配置撤回时限
- 支持按消息类型设置不同的撤回时限
- 默认 2 分钟内可撤回

### 3. 多人视频通话
- 基于 WebRTC 的 P2P 连接
- 支持 9 人同时视频通话
- 集成屏幕共享功能

### 4. 全局搜索
- 搜索范围: 消息、联系人、群组、文件
- 使用 Elasticsearch 实现全文检索
- 支持关键词高亮显示

## P1: 扩展功能

### 1. 视频会议
- 会议预定和管理
- 参会者邀请和移除
- 主持人权限转移
- 静音/解除静音控制
- 屏幕共享控制
- 会议状态: SCHEDULED、IN_PROGRESS、COMPLETED、CANCELLED

### 2. 企业云盘
- 文件夹层级管理
- 文件上传/下载/预览
- 文件分享(内链/外链)
- 访问密码保护
- 回收站和永久删除
- 文件版本历史

### 3. 考勤组管理
- 考勤组创建和管理
- 考勤类型: FIXED(固定班次)、FLEXIBLE(弹性班次)、FREE(自由打卡)
- 打卡方式: FACE(人脸)、LOCATION(地理位置)、WIFI(Wi-Fi)、MIXED(混合)
- 班次管理和排班
- 节假日管理

### 4. 文档协作
- 在线协作者管理
- 权限: EDIT(编辑)、COMMENT(评论)、VIEW(查看)
- 实时在线状态
- 光标位置同步
- 操作日志记录

## P2: 增强体验功能

### 1. 消息转发
- 单条消息转发
- 多条消息批量转发
- 转发到多个会话

### 2. 引用回复
- 引用原消息进行回复
- 显示引用内容预览
- 支持嵌套引用

### 3. 消息标记/待办
- 标记类型: FLAG(标记)、IMPORTANT(重要)、TODO(待办)
- 待办提醒时间设置
- 待办完成/重启状态
- 标记颜色自定义

### 4. 语音转文字
- 语音消息自动转文字
- 支持中文(zh-CN)、英文(en-US)
- 第三方服务商: ALIYUN、XUNFEI、TENCENT
- 转写状态: PENDING、PROCESSING、SUCCESS、FAILED
- 置信度记录

---

# 数据库迁移脚本

新增功能的数据库迁移脚本位于 `sql/migrations/` 目录:

```
sql/migrations/
├── 20250125_p0_feature_enhancement.sql      # P0 核心增强功能
├── 20250125_p1_video_meeting.sql           # P1-1 视频会议
├── 20250125_p1_cloud_drive.sql             # P1-2 企业云盘
├── 20250125_p1_attendance_group.sql        # P1-3 考勤管理
├── 20250125_p1_document_collaboration.sql  # P1-4 文档协作
├── 20250125_p2_message_marker.sql          # P2-3 消息标记
├── 20250125_p2_voice_transcript.sql        # P2-4 语音转文字
├── 20250125_performance_indexes.sql        # 性能优化索引
└── ...
```

---

# 代码质量优化记录

## 2025-01-25 代码质量改进

### 分层架构修复
- **ImMessageController**: 移除直接注入的 Mapper，改为通过 Service 层调用
- **ImGroupAdminController**: 修复直接使用 Entity 作为 API 参数的违规，改用 `ImGroupUpdateRequest` DTO
- **ImUserController**: 修复直接使用 Entity 作为 API 参数的违规，新增 `ImUserStatusUpdateRequest` DTO

### N+1 查询优化
- **ImConversationServiceImpl.getUserConversations**: 使用批量查询替代循环查询
  - 新增 `ImConversationMapper.selectUserConversationsWithMembers()` - 一次性获取会话和成员信息
  - 新增 `ImMessageMapper.selectLastMessagesByConversationIds()` - 批量获取会话最后消息
  - 新增 `ImGroupMapper.selectGroupsByIds()` - 批量获取群组信息
  - 使用现有的 `ImUserMapper.selectImUserListByIds()` - 批量获取用户信息
- 查询数量从 O(n) 降低到 O(1)（3个批量查询替代 n 个单独查询）

### 新增 DTO 类
- `ImUserStatusUpdateRequest`: 用户状态更新请求 DTO，支持状态值校验

### 数据库索引优化
迁移脚本 `20250125_performance_indexes.sql` 添加以下索引：
- **im_conversation**: 会话类型+目标ID组合索引、最后消息时间索引
- **im_conversation_member**: 用户ID+删除状态索引、用户ID+置顶状态索引
- **im_message**: 会话ID+创建时间索引、会话ID+ID索引、发送者ID+创建时间索引
- **im_friend**: 用户ID+好友ID组合索引（双向）、用户ID+分组索引
- **im_group**: 群主ID索引、群组名称前缀索引
- **im_user**: 用户状态索引
- **im_message_read**: 消息ID+用户ID唯一索引、会话ID+用户ID索引

---

# 全局异常处理

全局异常处理器位于 `com.ruoyi.im.handler.GlobalExceptionHandler`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(...)

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(...)

    // 权限异常
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(...)

    // 系统异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(...)
}
```

---

# Swagger 中文注解规范

所有 Controller 接口必须使用中文 Swagger 注解:

```java
@Tag(name = "视频会议", description = "视频会议管理接口")
public class ImVideoMeetingController {

    @Operation(summary = "创建会议", description = "创建新的视频会议")
    @PostMapping("/create")
    public Result<Long> createMeeting(
            @Parameter(description = "会议创建请求")
            @Valid @RequestBody ImVideoMeetingCreateRequest request) {
        ...
    }
}
```

**注解规范**:
- `@Tag`: 控制器级别的中文描述
- `@Operation`: 接口的中文摘要和详细描述
- `@Parameter`: 参数的中文描述，包括取值范围说明

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

**im_video_meeting** - 视频会议表
- 字段: `title`、`description`、`host_id`、`meeting_type`、`status`、`scheduled_start_time`、`room_id`
- 状态: SCHEDULED、IN_PROGRESS、COMPLETED、CANCELLED

**im_video_meeting_participant** - 会议参与者表
- 字段: `meeting_id`、`user_id`、`join_time`、`role`、`is_muted`、`video_enabled`

**im_cloud_folder** - 云盘文件夹表
- 字段: `folder_name`、`parent_id`、`owner_type`、`owner_id`、`access_permission`、`is_deleted`

**im_cloud_file** - 云盘文件表
- 字段: `folder_id`、`file_name`、`file_size`、`file_type`、`file_url`、`uploader_id`、`is_deleted`

**im_cloud_file_share** - 文件分享表
- 字段: `share_type`、`resource_id`、`share_code`、`access_password`、`expire_days`、`share_by`

**im_cloud_file_version** - 文件版本表
- 字段: `file_id`、`version_number`、`file_url`、`file_size`、`created_by`

**im_attendance_group** - 考勤组表
- 字段: `group_name`、`description`、`attendance_type`、`check_method`、`work_start_time`、`work_end_time`

**im_attendance_group_member** - 考勤组成员表
- 字段: `group_id`、`user_id`、`join_time`

**im_attendance_shift** - 考勤班次表
- 字段: `group_id`、`shift_name`、`work_start_time`、`work_end_time`

**im_attendance_schedule** - 考勤排班表
- 字段: `user_id`、`group_id`、`shift_id`、`schedule_date`

**im_attendance_holiday** - 节假日表
- 字段: `holiday_name`、`holiday_date`、`is_workday`

**im_document_collaborator** - 文档协作者表
- 字段: `document_id`、`user_id`、`permission`、`online_status`、`cursor_position`

**im_document_operation_log** - 文档操作日志表
- 字段: `document_id`、`user_id`、`operation_type`、`operation_detail`

**im_message_marker** - 消息标记表
- 字段: `message_id`、`user_id`、`marker_type`、`color`、`is_completed`、`remind_time`

**im_voice_transcript** - 语音转文字表
- 字段: `message_id`、`voice_url`、`duration`、`status`、`transcript_text`、`language`、`provider`

### 字段命名规范

- 数据库: `snake_case` (如: `conversation_id`)
- Java 实体类: `camelCase` (如: `conversationId`)
- 前端: `camelCase` (如: `conversationId`)

⚠️ **数据库是唯一事实来源** - 修改实体类或 API 前必须先检查表结构。

---

# MyBatis-Plus 规范 [P0]

## 查询要求（强制）
- ✅ 必须使用 Lambda 风格：`.eq(ImUser::getId, userId)`
- ❌ 禁止 Magic String（非 Lambda）：`.eq("id", userId)`
- ❌ 禁止 Magic String（字符串魔法值）：`.eq("is_deleted", 0)`
- ✅ 大表查询必须分页，单页不超过 1000 条
- ❌ 禁止 `SELECT *`，明确指定字段

## Entity 注解
- 类添加 `@TableName("表名")`
- 数据库字段映射 `@TableField("db_field")`
- 非数据库字段 `@TableField(exist = false)`

## Magic String 禁止
```java
// ❌ 错误：Magic String（严重违规）
wrapper.eq("is_deleted", 0)
wrapper.eq("status", "ACTIVE")

// ✅ 正确：Lambda + 常量
wrapper.eq(ImFileAsset::getIsDeleted, false)
wrapper.eq(ImFileAsset::getStatus, StatusConstants.ACTIVE)
```

## 状态值常量化
```java
// ✅ 定义状态常量
public class StatusConstants {
    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    public static final String DELETED = "DELETED";
}

// ✅ 使用常量
wrapper.eq(ImFileAsset::getStatus, StatusConstants.ACTIVE)
```

## 明确查询字段
```java
// ❌ 错误：SELECT *
mapper.selectList(null);

// ✅ 正确：明确字段
mapper.selectList(new LambdaQueryWrapper<ImUser>()
    .select(ImUser::getId, ImUser::getUsername, ImUser::getNickname));
```

---

# 数据库规范 [P0]

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

# WebSocket 消息类型 [P1]

| 类型 | 方向 | 描述 |
|------|------|------|
| `auth` | 客户端→服务端 | 身份认证 |
| `message` | 双向 | 聊天消息 |
| `ping/pong` | 双向 | 心跳检测 (30秒间隔) |
| `read` | 客户端→服务端 | 已读回执 |
| `typing` | 客户端→服务端 | 输入状态提示 |

---

# 已知问题与解决方案 [P2]

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

# 配置文件 [P2]

| 文件 | 用途 |
|------|------|
| `ruoyi-im-api/src/main/resources/application.yml` | API 配置 (数据库、Redis、WebSocket) |
| `ruoyi-im-web/vite.config.js` | 前端构建/开发配置 |

---

# 默认访问地址 [P3]

- API 服务器: http://localhost:8080
- 管理后台: http://localhost:8080/api/admin (需 ADMIN/SUPER_ADMIN 角色)
- 前端开发: http://localhost:5173

---

# 配置安全规范 [P0]

## 敏感信息管理原则
- ❌ 禁止在 application.yml 中硬编码密码、密钥、token
- ✅ 敏感配置必须使用环境变量或配置中心
- ✅ 开发/测试/生产环境配置分离

## 环境变量使用规范
```yaml
# ✅ 正确：使用环境变量 + 默认值
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:im}
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:}
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}

# ❌ 错误：硬编码敏感信息
spring:
  datasource:
    password: 123456  # 禁止！
```

## 敏感配置清单
以下配置必须使用环境变量：
- 数据库密码 (spring.datasource.password)
- Redis 密码 (spring.redis.password)
- JWT 密钥 (im.jwt.secret)
- 消息加密密钥 (im.message.encryption.key)
- 第三方 API 密钥（阿里云、腾讯云等）
- 邮件服务器密码
- 短信服务密钥

## 配置文件加密（可选进阶）
如需在配置文件中存储敏感信息，使用 Jasypt 加密：
```yaml
jasypt:
  encryptor:
    password: ${JASYPT_PASSWORD}

spring:
  datasource:
    password: ENC(encrypted_password_here)
```

---

# 日志规范 [P0]

## 禁止的日志方式
- ❌ 禁止使用 System.out.println（启动 Banner 除外）
- ❌ 禁止使用 System.err.println
- ❌ 禁止使用 e.printStackTrace()
- ❌ 禁止在日志中输出密码、token、敏感信息
- ❌ 禁止在生产环境使用 DEBUG 级别

## 日志使用规范
```java
// ✅ 正确：使用 slf4j logger
private static final Logger log = LoggerFactory.getLogger(XXX.class);

// 信息日志
log.info("用户登录成功, userId={}", userId);
log.info("消息发送成功, messageId={}, conversationId={}", messageId, conversationId);

// 错误日志（带异常堆栈）
log.error("消息发送失败, messageId={}, error={}", messageId, e.getMessage(), e);

// 调试日志（仅开发环境）
log.debug("查询参数: {}", params);

// ❌ 错误
System.out.println("处理完成");  // 禁止
log.info("用户登录, password={}", password);  // 泄露密码
e.printStackTrace();  // 禁止
```

## 日志级别规范
| 级别 | 使用场景 | 示例 |
|-----|---------|-----|
| ERROR | 系统错误、异常 | 数据库连接失败、业务异常 |
| WARN | 警告信息 | 配置项使用默认值、降级处理 |
| INFO | 关键业务操作 | 用户登录、消息发送、审批流转 |
| DEBUG | 调试信息 | 方法参数、中间状态 |

## 环境日志级别配置
```yaml
logging:
  level:
    com.ruoyi.im: ${LOG_LEVEL:info}  # 生产使用 info，开发使用 debug
    org.springframework.security: warn
```

## 特殊情况说明
- **启动 Banner**：ImApplication.java 的启动图案可保留 System.out
- **一次性工具**：PasswordUtil.main() 方法可临时使用 System.out
- **数据库初始化**：DatabasePasswordFixer 应改用 logger

---

# 事务规范 [P1]

## 事务使用要求
- Service 层方法必须添加 @Transactional
- 必须指定 rollbackFor = Exception.class
- 查询方法使用 @Transactional(readOnly = true)

## 事务超时设置
```java
// ✅ 普通操作（默认30秒超时）
@Transactional(rollbackFor = Exception.class)

// ✅ 只读操作
@Transactional(readOnly = true, rollbackFor = Exception.class)

// ✅ 长事务（必须显式设置超时）
@Transactional(timeout = 60, rollbackFor = Exception.class)

// ✅ 批量操作（根据数据量设置）
@Transactional(timeout = 300, rollbackFor = Exception.class)
```

## 超时时间参考
| 操作类型 | 建议超时 | 说明 |
|---------|---------|-----|
| 普通 CRUD | 30秒（默认） | 单表操作 |
| 复杂查询 | 60秒 | 多表关联 |
| 批量插入/更新 | 300秒 | 大量数据处理 |
| 导入导出 | 600秒 | 文件处理 |

## 禁止
- ❌ 在 Controller 层使用 @Transactional
- ❌ 在事务中调用外部 HTTP API
- ❌ 大批量操作不设置超时（>1000条）
- ❌ 事务中执行耗时计算（考虑异步）

## 最佳实践
```java
@Service
public class ImMessageServiceImpl {

    // ✅ 简单操作
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(SendMessageRequest request) {
        // ...
    }

    // ✅ 只读查询
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<ImMessage> getMessages(Long conversationId) {
        // ...
    }

    // ✅ 批量操作（设置超时）
    @Transactional(timeout = 120, rollbackFor = Exception.class)
    public void batchDeleteMessages(List<Long> messageIds) {
        // ...
    }
}
```

---

# 常量管理规范 [P1]

## 魔法值禁止
- ❌ 禁止在代码中直接使用魔法数字、魔法字符串
- ✅ 必须提取为常量或枚举

## 常量定义位置

### 后端常量
```java
// ✅ 方式1：常量类（推荐通用常量）
package com.ruoyi.im.constants;

public class MessageConstants {
    /** 最大消息内容长度 */
    public static final int MAX_CONTENT_LENGTH = 2000;

    /** 默认字符集 */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /** 消息撤回时间窗口（秒） */
    public static final int RECALL_WINDOW_SECONDS = 120;
}

// ✅ 方式2：枚举（推荐状态值）
package com.ruoyi.im.enums;

public enum MessageStatus {
    SENDING("SENDING", "发送中"),
    SENT("SENT", "已发送"),
    FAILED("FAILED", "发送失败"),
    READ("READ", "已读");

    private final String code;
    private final String desc;
    // ...
}
```

### 前端常量
```javascript
// ✅ 统一管理在 constants/ 目录
// src/constants/message.js
export const MESSAGE_LIMITS = {
  MAX_CONTENT_LENGTH: 2000,
  RECALL_WINDOW_SECONDS: 120
}

export const MessageStatus = {
  SENDING: 'SENDING',
  SENT: 'SENT',
  FAILED: 'FAILED',
  READ: 'READ'
}

// ✅ 文件大小限制
export const FILE_SIZE_LIMITS = {
  IMAGE: 10 * 1024 * 1024,   // 10MB
  VIDEO: 100 * 1024 * 1024,  // 100MB
  DOCUMENT: 20 * 1024 * 1024 // 20MB
}
```

## 使用示例
```java
// ❌ 错误：魔法值
if (message.getStatus().equals("SENDING")) {
    if (content.length() > 2000) {
        throw new RuntimeException("消息过长");
    }
}

// ✅ 正确：使用常量
if (MessageStatus.SENDING.getCode().equals(message.getStatus())) {
    if (content.length() > MessageConstants.MAX_CONTENT_LENGTH) {
        throw new BusinessException("消息过长");
    }
}
```

## 常量命名规范
- 全大写 + 下划线：`MAX_CONTENT_LENGTH`
- 枚举类名：`XxxStatus`、`XxxType`
- 枚举值：大驼峰：`SENDING`、`ACTIVE`

---

# 前端存储规范 [P1]

## localStorage 使用约束
- ❌ 禁止直接使用 localStorage 存储敏感信息（token、密码）
- ❌ 禁止硬编码 localStorage 的 key 名称
- ✅ 必须使用统一的 storage 工具类

## 统一存储 API
项目已有 `src/utils/storage.js`，必须使用其提供的 API：

```javascript
// ✅ 正确：使用统一封装
import { getToken, setToken, getUserInfo, setUserInfo } from '@/utils/storage'

// Token 操作
setToken(token)
const token = getToken()

// 用户信息
setUserInfo(userInfo)
const userInfo = getUserInfo()

// ❌ 错误：禁止直接使用
localStorage.setItem('im_token', token)  // 禁止
localStorage.setItem('my_key', value)     // 禁止（key未定义）
```

## 存储管理
所有存储 key 必须在 `storage.js` 的 `StorageKeys` 中定义：

```javascript
// ✅ 新增 key 必须在此定义
export const StorageKeys = {
  // 认证相关
  TOKEN: 'im_token',

  // 新增：XXX功能
  XXX_CONFIG: 'im_xxx_config'  // 新增时添加
}
```

## 敏感信息存储建议
- Token：考虑使用 httpOnly cookie（后端设置）
- 用户信息：优先使用 Vuex + sessionStorage 持久化
- 密码：禁止存储在任何地方

---

# 技术栈总结 [P3]

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
