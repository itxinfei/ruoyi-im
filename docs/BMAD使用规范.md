# BMAD 使用规范（RuoYi-IM 项目）

> 目标：统一团队在本项目中使用 BMAD-METHOD 的方式，避免“会用命令但产物不落地”。

## 1. 你问的 `/BMAD` 怎么用

1. 在 Claude Code/Cursor 里，BMAD 命令通常是小写前缀：`/bmad-...`
2. 入口命令优先用：`/bmad-help`
3. 可以先输入 `/bmad`，用自动补全查看可用命令

说明：

1. 常见命令示例：`/bmad-help`、`/bmad-bmm-create-prd`、`/bmad-bmm-create-architecture`
2. Agent 命令示例：`/bmad-agent-bmm-dev`、`/bmad-agent-bmm-pm`

## 2. 本项目固定基线（必须带入 BMAD 会话）

1. JDK：`1.8`
2. 前端：`Vue 3`
3. 数据库：`MySQL 5.7`
4. 开发环境：`Windows 11`
5. 生产环境：`CentOS 7`

## 3. BMAD + 本项目推荐流程

1. `/bmad-help`：确认当前阶段该做什么
2. `/bmad-bmm-create-prd`：生成或更新需求（参考 `docs/需求文档.md`）
3. `/bmad-bmm-create-architecture`：补齐架构（对齐 `ruoyi-im-api`/`ruoyi-im-web`）
4. `/bmad-bmm-create-epics-and-stories`：拆分可执行任务
5. `/bmad-bmm-dev-story`：进入实现
6. `/bmad-bmm-code-review`：代码审查后再合并

## 4. 与现有规范联动（必须执行）

1. 需求基线：`docs/需求文档.md`
2. UI基线：`docs/UI对标设计规范.md`
3. LLM执行规范：`docs/大模型研发规范.md`
4. 任务模板：`docs/LLM任务模板.md`
5. 任务池：`docs/llm-tasks.yaml`

## 5. Java 代码红线（BMAD 会话里要重复声明）

1. 仅允许 JDK8 语法
2. 禁止魔法值（常量或枚举替代）
3. 禁止多层三元和复杂表达式链
4. 优先 `if/for` 显式写法
5. 方法过长必须拆分（建议 <= 60 行）
6. 异常禁止吞掉，必须可诊断

## 6. 对齐钉钉的任务输入格式（建议直接复制）

```text
你现在使用 BMAD 方法执行本任务。
项目固定技术栈：JDK1.8、Vue3、MySQL5.7、开发Win11、生产CentOS7。
严格遵守：禁止魔法值、禁止复杂表达式、优先可维护性与可靠性。
本次任务所属：一级导航=消息，二级菜单=最近会话，三级弹窗=发起通话弹窗。
先输出实施计划，再落地代码，再执行验证命令，再给AC对照。
```

