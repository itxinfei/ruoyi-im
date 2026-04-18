# LLM 任务模板

## 使用说明

每次只创建一个任务卡。  
任务卡用于模型执行，不等同于产品需求原文。  
任务卡中引用的文档必须是 **当前真实存在于 `docs/` 中的文件**，禁止沿用失效路径或历史目录。

## 标准模板

```yaml
task_id: IM-XXX-001
task_type: ui_rebuild
title: 一句话标题
goal: 一句话目标，必须可验证
priority: P0
status: todo
doc_refs:
  runtime:
    - docs/05-AI执行文档使用协议.md
    - docs/02-大模型研发规范.md
  product:
    - docs/10-产品需求总纲.md
  requirement_detail:
    - docs/13-需求明细.md#模块名
  collaboration_detail:
    - docs/18-平台模块与管理后台需求.md#模块名
  traceability:
    - docs/19-需求追溯矩阵.md#条目
  navigation:
    - docs/14-页面路由与菜单映射.md#条目
  entry_and_api:
    - docs/17-接口与入口清单.md#条目
  architecture:
    - docs/30-技术架构与约束.md
  domain:
    - docs/31-实体与状态模型.md#实体或状态
  schema:
    - docs/32-数据库设计.md#表或字段
  api_contract:
    - docs/33-API接口设计.md#接口名
  websocket:
    - docs/34-WebSocket协议.md#条目
  ui_blueprint:
    - docs/27-PC版钉钉UI设计蓝图.md#页面模板
  ui_rules:
    - docs/20-UI交互规范总纲.md#页面或组件
  ui_detail:
    - docs/21-IM桌面端页面规范.md#条目
  ui_behavior:
    - docs/26-IM交互深度行为规范.md#条目
  ui_acceptance:
    - docs/25-钉钉Windows复刻基线.md#条目
  design_tokens:
    - docs/35-前端全局样式与主题配置.md#条目
  navigation_target:
    - docs/15-产品形态与导航清单.md#模块名
  gap:
    - docs/16-设计复刻差异.md#条目
  task_plan:
    - docs/23-UI复刻任务计划.md#条目
  quality:
    - docs/52-测试与质量策略.md#条目
  acceptance_cases:
    - docs/54-验收用例清单.md#条目
scope_in:
  - 允许改动的文件或模块
scope_out:
  - 禁止改动的文件或模块
preconditions:
  - 执行前已满足的条件
dependencies:
  - 依赖的接口、组件、后端能力
route_or_entry:
  - 用户从哪里进入该功能
api_contract:
  - 已知接口或接口来源；未知则触发停机阻断 (HALT)
ui_tokens:
  - 必须采用的 CSS 变量标准，绝对禁止私自硬编码调色
anti_conflict_scan:
  - 承诺在写代码前执行全工作区 Grep 查重
state_changes:
  - 需要变更的前端或后端状态
doc_writeback:
  required:
    - docs/03-实现状态矩阵.md#对应条目
  conditional:
    - 路由/导航变化 -> docs/14-页面路由与菜单映射.md, docs/15-产品形态与导航清单.md, docs/17-接口与入口清单.md
    - UI规则变化 -> docs/16-设计复刻差异.md, docs/21-IM桌面端页面规范.md, docs/25-钉钉Windows复刻基线.md, docs/26-IM交互深度行为规范.md, docs/27-PC版钉钉UI设计蓝图.md, docs/35-前端全局样式与主题配置.md
    - 测试或验收变化 -> docs/52-测试与质量策略.md, docs/54-验收用例清单.md
acceptance:
  - AC1: ...
  - AC2: ...
  - AC3: ...
manual_test_steps:
  - Step1: ...
  - Step2: ...
verify_cmd:
  - npm run build
risk: low
rollback:
  - 回滚策略
notes:
  - 补充说明
```

## 编写要求

1. `goal` 必须是单目标
2. `task_type` 必须填写，并与任务实际类型一致（如 `ui_rebuild`、`ui_fix`、`backend_feature`、`fullstack_fix`、`docs_sync`）
3. `scope_in` 必须尽量小
4. `acceptance` 至少 3 条，必须可执行
5. `api_contract` 不知道就阻断停机，严禁编造接口路径
6. `manual_test_steps` 必须能让人重复验证
7. `doc_refs.runtime`、`product`、`architecture`、`entry_and_api` 必须填写，且引用路径必须真实存在
8. 如果任务属于 IM 主链路，`requirement_detail` 必须优先引用 [docs/13-需求明细.md](13-需求明细.md)
9. 如果任务属于左侧一级模块或协同模块，`collaboration_detail` / `navigation_target` 必须引用 [docs/18-平台模块与管理后台需求.md](18-平台模块与管理后台需求.md) 或 [docs/15-产品形态与导航清单.md](15-产品形态与导航清单.md)
10. 如果任务涉及路由、菜单、入口，必须填写 `navigation` 与 `doc_writeback`
11. 如果任务涉及数据库或接口，必须填写 `schema`、`api_contract`；若涉及 WebSocket，必须填写 `websocket`
12. 如果任务目标是“PC 版钉钉 UI 设计/复刻”，`ui_blueprint`、`ui_rules`、`ui_acceptance`、`design_tokens` 必须填写；若为 IM 细节复刻，额外填写 `ui_detail`、`ui_behavior`、`gap`、`task_plan`
13. 如果接口来源未完全确认，务必立刻阻断并抛出异常提示，绝不允许在代码中写下假想的请求路径
14. 任务卡只描述一个可交付闭环，不要把多个模块目标混成一个任务
15. 任何任务执行前必须强制声明遵守《docs/02-大模型研发规范.md》中的代码质量红线（如禁用 TypeScript 和特定 Java 9+ 语法）
16. 建类或写逻辑前，强制挂载前置搜查 (Pre-creation Scan) 约束点，绝不允许制造冗余工具类或覆写框架原有同名 Bean
17. 前端验收条件必须涵盖：内存回收已完成 (`onUnmounted`)，以及 100% UI 由 CSS Tokens 变量驱动
18. 所有任务完成后必须回写 `doc_writeback.required`，并按实际变化补齐 `doc_writeback.conditional`
