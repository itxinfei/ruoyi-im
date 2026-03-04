# RuoYi-IM 开发基线

## 核心法则 (The Constitution)
**所有 AI 行为必须强制遵守：**
1. `docs/大模型研发规范.md` (全站研发总纲)
2. `.agent/rules/ui-standards.md` (前端视觉红线)
3. `.agent/rules/backend-standards.md` (后端架构红线)

## 常用命令
- 前端：`cd ruoyi-im-web && npm run dev`
- 后端：`cd ruoyi-im-api && mvn spring-boot:run`
- 验证：`npm run build` (前端) | `mvn compile` (后端)

## 协作协议 (DoD)
- **拒绝占位**：禁止生成“开发中”提示代码。
- **清理垃圾**：重构必删旧代码。
- **自检模板**：每次任务必须输出 `[规范自检]` 结果。
