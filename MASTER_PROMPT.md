# RuoYi-IM 开发全案引导词 (Master Prompt)

> **使用说明**：将本段内容直接粘贴给任何大模型（Claude/GPT/DeepSeek），即可让其进入“高精度开发状态”。

---

## 1. 项目背景与技术栈
我是 RuoYi-IM 项目的开发者。本项目要求完美复刻 **Windows 版钉钉 8.2.0** 的 UI/UX。
- **后端**：Java 1.8 (Spring Boot + MyBatis-Plus + MySQL 5.7)
- **前端**：Vue 3 (Composition API + `<script setup lang="js">`)
- **实时**：原生 WebSocket (带 Seq 序列号机制)

---

## 2. 核心红线 (必须严格遵守)
1. **语法红线**：
   - **Java**：禁止使用 `var`、`record`、`switch ->` 等 Java 9+ 语法。强制使用 JDK 1.8。
   - **Vue**：禁止使用 TypeScript，禁止 `lang="ts"`。必须使用纯 JavaScript。
2. **架构红线**：
   - 必须遵循 `Controller -> Service -> Mapper` 单向依赖。
   - 禁止 Entity 直接作为接口入参/出参，必须使用 `DTO`/`VO`。
3. **命名红线**：
   - 禁止使用 `data`、`info`、`temp` 等模糊变量名。
   - 禁止方法前缀滥用 `handle`，应使用 `process`、`execute` 等业务动词。

---

## 3. 视觉契约 (钉钉基因)
在编写 CSS/SCSS 时，必须引用以下全局变量：
- **品牌蓝**：`#007FFF` (var(--dt-brand-color))
- **边框/分割线**：`1px solid #E5E7EB` (var(--dt-border-color))
- **黄金间距**：遵循 8px 步进 (4/8/16/24/32px)。
- **头像**：固定 `36x36px`，圆角 `4px` (严禁圆形)。

---

## 4. 协作指令
接下来，我会告诉你具体要开发的模块（如：消息撤回、审批列表）。
**当你准备好开始时，请回复：“RuoYi-IM 架构协议已加载，请下达具体开发任务。”**
