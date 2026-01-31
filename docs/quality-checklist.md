# 质量检查清单

本文档整理自 `CLAUDE.md` 的规范，用于开发过程中的质量检查。

---

## JDK 1.8 语法检查

### 禁止使用的语法

在代码审查时，确保代码不包含以下 Java 9+ 特性：

- [ ] **禁止使用 `var` 关键字**（类型推断，Java 10）
- [ ] **禁止使用 `record` 类型**（Java 14）
- [ ] **禁止使用 `sealed` 关键字**（Java 15）
- [ ] **禁止使用 `switch` 表达式语法 `case A ->`**（Java 12）
- [ ] **禁止使用文本块 `"""..."""`**（Java 15）
- [ ] **禁止使用 `Optional.isEmpty()`**（Java 11）
- [ ] **禁止使用 `Optional.orElseThrow()` 无参版本**（Java 10）
- [ ] **禁止使用 `Collection.toList()`**（Java 16）
- [ ] **禁止使用 Pattern matching instanceof**（Java 14）

**记忆口诀**: 不用 var、record、sealed、→、"""、isEmpty、toList

---

## 分层架构检查

### Controller 层

- [ ] **职责**: 接收请求、参数校验、调用 Service、返回响应
- [ ] **禁止**: 直接调用 Mapper、编写业务逻辑
- [ ] **必须**: 使用 DTO 接收参数、使用 VO 返回数据
- [ ] **必须**: 添加 Swagger 中文注解（`@Tag`、`@Operation`、`@Parameter`）

### Service 层

- [ ] **职责**: 业务逻辑、事务控制、调用 Mapper
- [ ] **禁止**: 跨层调用 Controller
- [ ] **必须**: 添加 `@Transactional(rollbackFor = Exception.class)`
- [ ] **必须**: 业务逻辑分支添加中文注释说明业务背景

### Mapper 层

- [ ] **职责**: 数据库访问、SQL 执行
- [ ] **禁止**: 包含业务逻辑
- [ ] **必须**: 使用 MyBatis-Plus Lambda 风格查询
- [ ] **禁止**: 使用 Magic String（如 `.eq("id", userId)`）

### 数据隔离检查

- [ ] **Entity**: 不能作为 API 入参或返回值，必须使用 DTO/VO
- [ ] **Entity**: 添加 `@TableName` 指定表名
- [ ] **Entity**: 数据库字段使用 `@TableField` 映射
- [ ] **Entity**: 非数据库字段标注 `@TableField(exist = false)`

---

## 命名规范检查

### 类命名

- [ ] Controller: `XxxController`
- [ ] Service 接口: `XxxService`
- [ ] Service 实现: `XxxServiceImpl`
- [ ] Mapper: `XxxMapper`
- [ ] Entity: 表名（如 `ImUser`）
- [ ] DTO: `XxxDTO` 或 `XxxRequest` / `XxxResponse`
- [ ] VO: `XxxVO`

### 禁止使用的命名

- [ ] ❌ `data` → 改用 `userVO`、`messageDTO`
- [ ] ❌ `info` → 改用 `userInfo`、`messageInfo`
- [ ] ❌ `temp` → 改用 `cachedUser`、`buffer`
- [ ] ❌ `obj` → 改用 `requestParam`、`response`
- [ ] ❌ `handle` → 改用 `processMessage`、`validateUser`
- [ ] ❌ `doXxx` → 直接用动词：`sendMessage`

### 方法命名

- [ ] 查询：`get`、`list`、`query`、`search`
- [ ] 新增：`add`、`create`、`insert`
- [ ] 修改：`update`、`modify`
- [ ] 删除：`delete`、`remove`
- [ ] 判断：`is`、`has`、`can`、`validate`
- [ ] 计算：`calculate`、`compute`
- [ ] 转换：`convert`、`transform`、`toXxx`

---

## Vue 3 组件规范

### 组件结构

- [ ] 必须使用 `<script setup lang="js">` 语法
- [ ] 必须使用 Composition API
- [ ] CSS 必须添加 `scoped` 属性
- [ ] Props 必须定义类型和默认值
- [ ] Emits 必须明确定义
- [ ] 暴露方法使用 `defineExpose`

### 响应式数据

- [ ] 简单类型使用 `ref`
- [ ] 对象类型使用 `reactive`
- [ ] 优先使用 `computed` 而非方法

### API 封装

- [ ] 必须封装在 `src/api/` 目录
- [ ] 使用统一的 request 实例
- [ ] 添加 JSDoc 注释说明参数

---

## 代码质量检查

### 注释要求

- [ ] 所有类必须有 JavaDoc 类注释
- [ ] 所有 public 方法必须有 JavaDoc 方法注释
- [ ] 业务逻辑分支必须添加中文注释说明**业务背景**
- [ ] 注释要说明"为什么"而非"是什么"

### 异常处理

- [ ] 禁止吞异常（空 catch 块）
- [ ] 禁止只打印不抛出（e.printStackTrace()）
- [ ] 业务异常：记录日志后重新抛出
- [ ] 系统异常：记录日志后包装为 BusinessException
- [ ] 日志必须包含参数信息

### NPE 防护

- [ ] 查询结果判空：`if (user == null) { throw ... }`
- [ ] 使用 `Optional`、`StringUtils`、`Objects` 工具类
- [ ] 不信任外部输入，必须校验

### 代码复杂度控制

- [ ] 单个方法不超过 50 行
- [ ] 嵌套层级不超过 3 层
- [ ] 单个类不超过 500 行
- [ ] 魔法值必须提取常量

---

## 垃圾代码检查

- [ ] 无冗余工具类或方法
- [ ] 无重复造轮子（优先使用 Apache Commons、Hutool、JDK 原生方法）
- [ ] 无死代码（注释掉的代码、预留字段、预留方法）
- [ ] 无过度设计（不必要的抽象）
- [ ] 无魔法值，已提取常量
- [ ] 无无意义的命名（data、info、temp等）

---

## 测试要求

### 单元测试

- [ ] 后端核心业务逻辑必须有单元测试（JUnit + Mockito）
- [ ] 前端核心组件必须有组件测试（Vitest + Vue Test Utils）
- [ ] 测试覆盖率：核心功能 >= 80%

### 集成测试

- [ ] API 接口测试（Postman/Apifox）
- [ ] WebSocket 事件测试
- [ ] 数据库操作测试

### 性能测试

- [ ] 建立性能基线（如 1000 条消息发送时间）
- [ ] N+1 查询优化验证
- [ ] 内存泄漏测试（发送 1000 条消息后，Map 自动清理旧记录）

---

## 前端代码规范

### 避免重复代码

- [ ] 使用 `utils/message.js` 的格式化函数，不重复定义
- [ ] 使用 `design-tokens.scss` 的全局样式，不内联样式重复定义
- [ ] 使用 `animations.scss` 的全局动画，不重复定义 @keyframes

### 组件导入

- [ ] 确认导入路径正确（使用 Glob 先验证）
- [ ] 不导入不存在的组件或模块
- [ ] 组件删除前搜索所有引用

---

## 数据库规范

### 表设计

- [ ] 表名：小写下划线 `im_message`
- [ ] 字段名：小写下划线 `conversation_id`
- [ ] 必须有主键 `id` (BIGINT)
- [ ] 必须有 `create_time`、`update_time`
- [ ] 逻辑删除字段 `del_flag` (0=正常 1=删除)
- [ ] 字符集：utf8mb4，排序规则：utf8mb4_general_ci

### SQL 规范

- [ ] 禁止 `SELECT *` - 明确指定字段
- [ ] 禁止在 WHERE 子句中对字段进行函数操作
- [ ] 模糊查询使用前缀匹配 `LIKE 'keyword%'`
- [ ] 避免 COUNT(*) - 使用 COUNT(id)
- [ ] 大表操作需分页，单页不超过 1000 条

### MyBatis-Plus 规范

- [ ] 必须使用 Lambda 风格：`.eq(ImUser::getId, userId)`
- [ ] 禁止 Magic String：`.eq("id", userId)`
- [ ] 大表查询必须分页

---

## WebSocket 通信规范

- [ ] 消息推送: `message` 事件
- [ ] 输入状态: `typing` / `stop-typing` 事件
- [ ] 消息状态: `message_delivered` / `message_failed` 事件
- [ ] 心跳检测: `ping` / `pong`（30 秒间隔）
- [ ] 实现幂等性处理（使用 `clientMsgId` 去重）

---

## 安全检查

- [ ] 文件上传类型检查（防止恶意文件上传）
- [ ] 消息输入 XSS 过滤
- [ ] WebSocket 消息权限控制
- [ ] SQL 注入防护（使用参数化查询）
- [ ] 敏感数据加密存储

---

## 使用方法

### 开发前

1. 阅读本检查清单
2. 了解即将编写的代码涉及的检查项

### 开发中

1. 对照检查项编写代码
2. 完成一项，勾选一项

### 代码审查

1. 使用本清单作为审查依据
2. 确保所有检查项都通过

### 持续改进

1. 发现新的问题，更新到清单中
2. 定期（每月）审查和更新清单内容
