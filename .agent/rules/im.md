---
trigger: always_on
---

# RuoYi-IM 全栈开发 AI 约束规范

## 1. 核心上下文与技术栈 (Strict Stack)

* **后端 (ruoyi-im-api):** JDK 8 (禁止 Java 9+ 语法), Spring Boot 2.7.18, MyBatis-Plus, Redis, MySQL 5.7.
* **后台 (ruoyi-im-admin):** RuoYi v4.8 (Apache Shiro, Thymeleaf).
* **前端 (ruoyi-im-web):** Vue 3.3+ (Composition API, `<script setup>`), Vite, Element Plus, Vuex 4.
* **通信:** WebSocket (核心消息) + REST API (管理逻辑).

## 2. 防止“逻辑震荡”与“代码重写”的强制流程

**在执行任何修改前，AI 必须遵循以下“三思而后行”审计步骤：**

### 第一步：代码考古 (Archaeology)

* 修改前必须先执行 `Grep` 或 `Glob` 搜索。
* **禁止**在未确认是否存在同名类、同名方法的情况下创建新代码。
* **禁止**删除已有功能代码。如果代码冗余，必须先向用户说明原因。

### 第二步：真理来源 (Source of Truth)

* **数据库是唯一真理：** 修改任何 Java Entity 或 前端 API 传参前，必须先 `cat sql/im.sql`。
* **字段对齐：** 必须确保数据库 `snake_case`、Java `camelCase` 和前端 `camelCase` 的三方映射完全一致。

### 第三步：方案公示 (Plan Disclosure)

* 在生成大段代码前，必须简述：
1. `Root Cause`: 报错的根本原因。
2. `Impact`: 本次修改会影响哪些现有模块。
3. `Prevention`: 如何防止修改后出现“参数不对齐”的新报错。



## 3. 后端隔离规范 (Standard Layering)

为了防止字段名混乱，**严禁**将 Entity 直接暴露给 Controller。

1. **Entity (`domain.entity`)**: 仅映射数据库。
2. **DTO (`domain.dto`)**: 接收前端参数。必须使用 `@Validated` 校验。
3. **VO (`domain.vo`)**: 返回给前端的数据。
4. **转换逻辑**: 必须在 Service 层或 Controller 层使用 `BeanUtil` 完成转换，禁止前端直接感知数据库结构。

## 4. 前端开发禁令 (Frontend Constraints)

* **禁止使用 Options API**: 必须使用 `<script setup>`。
* **禁止组件膨胀**: 单个 `.vue` 文件超过 400 行必须拆分。
* **状态同步**: IM 消息状态必须走 Vuex (`store/modules/im.js`)，禁止在页面组件里私自定义 `messages` 数组。
* **API 统一**: 所有请求必须封装在 `src/api/im/` 下，严禁在组件内直接写 `axios.post`。

## 5. 错误修复与调试规范

* **禁止盲目重试**: 如果一个修复方案失败，禁止再次尝试相同的代码。
* **日志先行**: 修复报错前，必须在相关位置添加 `log.info` (Java) 或 `console.log` (Vue) 来定位数据真实流向。
* **清理脏代码**: 修复完成后，必须删除为了调试而添加的临时注释和测试代码。

## 6. 搜索参考指令

* `Grep -r "target_id" .` (检查数据库字段使用情况)
* `find ruoyi-im-api -name "*DTO.java"` (确认参数定义)

