# LLM 任务模板

## 使用说明

每次只创建一个任务卡。  
任务卡用于模型执行，不等同于产品需求原文。

## 标准模板

```yaml
task_id: IM-XXX-001
title: 一句话标题
goal: 一句话目标，必须可验证
priority: P0
status: todo
doc_refs:
  agent_protocol:
    - 02-大模型研发规范.md
  product:
    - 10-产品需求总纲.md
  requirement_detail:
    - 13-需求明细.md#模块名
  architecture:
    - 30-技术架构与约束.md
    - backend/01-消息模块后端代码重构与规范.md
    - backend/02-全局错误码与异常字典规范.md
    - backend/03-日志掩码与可观测性规约.md
    - frontend/01-消息模块Vue组件化与性能优化.md
    - ui-ux/01-消息核心模块交互与视觉规范.md
  domain:
    - 31-实体与状态模型.md#实体或状态
  ui:
    - 20-UI交互规范总纲.md#页面或组件
  navigation:
    - 15-产品形态与导航清单.md#模块名
  module_detail:
    - 18-平台模块与管理后台需求.md#模块名
  entry_and_api:
    - 17-接口与入口清单.md#条目
  ui_detail:
    - 21-IM桌面端页面规范.md#条目
  gap:
    - 16-设计复刻差异.md#条目
  plan:
    - 23-UI复刻任务计划.md#条目
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
2. `scope_in` 必须尽量小
3. `acceptance` 至少 3 条，必须可执行
4. `api_contract` 不知道就阻断停机，严禁编造接口路径
5. `manual_test_steps` 必须能让人重复验证
6. `doc_refs` 必须填写，至少包含 `product`、`requirement_detail`、`ui`、`entry_and_api`
7. 如果任务属于 IM 主链路，`requirement_detail` 必须优先引用 [13-需求明细.md](13-需求明细.md)
8. 如果任务属于左侧一级模块，`module_detail` 必须引用 [18-平台模块与管理后台需求.md](18-平台模块与管理后台需求.md)
9. 如果接口来源未完全确认，务必立刻阻断并抛出异常提示，绝不允许在代码中写下假想的请求路径。
10. 任务卡只描述一个可交付闭环，不要把多个模块目标混成一个任务
11. 如果任务目标是"钉钉 UI 复刻"，`ui_detail`、`gap`、`plan` 必须填写，且 `acceptance` 要包含视觉和交互对齐条目
12. 任何任务执行前必须强制声明遵守《02-大模型研发规范.md》中的代码质量红线（如禁用TypeScript和特定Java9+语法）。
13. 建类或写逻辑前，强制挂载前置搜查 (Pre-creation Scan) 约束点，绝不允许制造冗余工具类或覆写框架原有同名 Bean。
14. 前端验收条件必须涵盖：内存回收已完成 (onUnmounted)，以及 100% UI 由 CSS Tokens 变量代跑。