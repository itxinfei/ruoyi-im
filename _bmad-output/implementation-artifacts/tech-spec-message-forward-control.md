---
title: '消息转发管控系统实现'
slug: 'message-forward-control'
created: '2026-02-28'
status: 'ready-for-dev'
stepsCompleted: [1, 2, 3, 4]
tech_stack: ['Vue 3', 'Spring Boot', 'MyBatis Plus']
files_to_modify:
  - 'ruoyi-im-web/src/components/Chat/ForwardDialog.vue'
  - 'ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImForwardRule.java'
  - 'ruoyi-im-api/src/main/java/com/ruoyi/im/service/ImMessageService.java'
code_patterns:
  - '转发前权限校验'
  - '敏感会话禁止转发'
  - '转发审批流程'
  - '转发审计日志'
test_patterns:
  - '普通会话是否允许转发'
  - '敏感会话是否禁止转发'
  - '转发审批流程是否正常'
---

# Tech-Spec: 消息转发管控系统实现

**Created:** 2026-02-28
**Status:** Ready for Development

---

## 1. 需求概述

### 1.1 问题陈述

员工可能通过转发消息将敏感信息泄露给未授权人员，需要对消息转发进行管控和审计。

### 1.2 解决方案

实现消息转发管控系统，包括转发权限校验、敏感会话禁止转发、转发审批、转发审计。

### 1.3 范围

**包含范围 (In Scope):**
- 转发前权限校验
- 敏感会话禁止转发
- 转发审批流程（可选）
- 转发审计日志
- 转发水印（自动附加来源信息）

**不包含范围 (Out of Scope):**
- 消息导出管控（另外的需求）
- 批量转发限制

---

## 2. 开发上下文

### 2.1 管控策略

| 管控项 | 说明 | 配置 |
|-------|------|------|
| 禁止转发 | 敏感会话禁止转发消息 | 按会话/部门配置 |
| 转发审批 | 转发需审批后才能发送 | 可选开启 |
| 转发水印 | 转发内容自动添加水印 | 自动附加来源信息 |
| 转发限制 | 限制转发次数/范围 | 可配置 |

### 2.2 审计日志格式

```json
{
  "eventType": "MESSAGE_FORWARD",
  "userId": "10086",
  "username": "张三",
  "timestamp": "2026-02-28T15:30:00",
  "sourceConversationId": "conv_001",
  "targetConversationIds": ["conv_002", "conv_003"],
  "messageId": "msg_123456",
  "messageType": "TEXT",
  "action": "ALLOWED"
}
```

---

## 3. 实现计划

### 3.1 阶段一：转发规则实体（P0）- 1 小时

| 任务 ID | 任务描述 | 文件 | 预估工时 |
|--------|---------|------|---------|
| T01-1 | 创建 ForwardRule 实体 | ImForwardRule.java | 0.5h |
| T01-2 | 创建 ForwardRuleMapper | ImForwardRuleMapper.java | 0.5h |

### 3.2 阶段二：转发规则服务（P0）- 2 小时

| 任务 ID | 任务描述 | 文件 | 预估工时 |
|--------|---------|------|---------|
| T02-1 | 创建 ForwardRuleService | ImForwardRuleService.java | 1h |
| T02-2 | 创建转发规则管理接口 | ForwardRuleController.java | 1h |

### 3.3 阶段三：前端转发管控（P0）- 2 小时

| 任务 ID | 任务描述 | 文件 | 预估工时 |
|--------|---------|------|---------|
| T03-1 | 修改 ForwardDialog 组件 | ForwardDialog.vue | 1h |
| T03-2 | 添加转发权限校验 | useChatCommands.js | 0.5h |
| T03-3 | 添加转发审计上报 | ForwardDialog.vue | 0.5h |

**总预估工时：** 5 小时

---

## 4. 验收标准

### 4.1 功能验收

| ID | Given | When | Then |
|----|-------|------|------|
| F01 | 普通会话消息 | 尝试转发 | 允许转发 |
| F02 | 敏感会话消息 | 尝试转发 | 禁止转发并提示 |
| F03 | 开启审批 | 转发敏感消息 | 需要审批 |
| F04 | 转发成功 | 查看审计日志 | 有完整的转发记录 |

---

*本技术规格说明根据 BMAD 快速规格流程创建*
