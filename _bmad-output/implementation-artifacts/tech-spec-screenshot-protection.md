---
title: '截图/录屏防护系统实现'
slug: 'screenshot-screen-recording-protection'
created: '2026-02-28'
status: 'ready-for-dev'
stepsCompleted: [1, 2, 3, 4]
tech_stack: ['Vue 3', 'JavaScript', 'Clipboard API']
files_to_modify:
  - 'ruoyi-im-web/src/components/Security/ScreenshotDetector.vue'
  - 'ruoyi-im-web/src/views/MainPage.vue'
  - 'ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImSecurityLog.java'
  - 'ruoyi-im-api/src/main/java/com/ruoyi/im/service/ImSecurityLogService.java'
code_patterns:
  - '监听键盘事件拦截截图快捷键'
  - '监控剪贴板内容变化'
  - '检测开发者工具打开'
  - '截图行为审计日志'
test_patterns:
  - '截图快捷键是否被拦截'
  - '剪贴板监控是否正常工作'
  - '审计日志是否正确记录'
---

# Tech-Spec: 截图/录屏防护系统实现

**Created:** 2026-02-28
**Status:** Ready for Development

---

## 1. 需求概述

### 1.1 问题陈述

员工可能通过截图、录屏等方式泄露敏感信息，需要检测并记录这些行为，以便追溯责任人。

### 1.2 解决方案

实现截图/录屏检测系统，包括快捷键拦截、剪贴板监控、开发者工具检测，并记录审计日志。

### 1.3 范围

**包含范围 (In Scope):**
- 截图快捷键拦截（PrintScreen 等）
- 剪贴板内容监控
- 开发者工具检测
- 截图行为审计日志
- 控制台清空防护

**不包含范围 (Out of Scope):**
- 真正的截图阻止（浏览器限制无法完全阻止）
- 第三方截图工具检测
- 手机拍照检测（物理方式无法阻止）

---

## 2. 开发上下文

### 2.1 检测机制

| 检测方式 | 说明 | 响应动作 |
|---------|------|---------|
| 截图快捷键拦截 | 拦截 PrintScreen、Ctrl+P 等 | 阻止并记录日志 |
| 剪贴板监控 | 检测剪贴板内容变化 | 记录复制内容 |
| DevTools 检测 | 检测开发者工具打开 | 警告并记录日志 |
| 控制台清空 | 页面加载时清空控制台 | 防止敏感信息泄露 |

### 2.2 审计日志格式

```json
{
  "eventType": "SCREENSHOT_ATTEMPT",
  "userId": "10086",
  "username": "张三",
  "timestamp": "2026-02-28T15:30:00",
  "clientIp": "192.168.1.100",
  "conversationId": "conv_001",
  "action": "BLOCKED",
  "method": "KEYBOARD_SHORTCUT"
}
```

---

## 3. 实现计划

### 3.1 阶段一：前端检测组件（P0）- 3 小时

| 任务 ID | 任务描述 | 文件 | 预估工时 |
|--------|---------|------|---------|
| T01-1 | 创建 ScreenshotDetector 组件 | ScreenshotDetector.vue | 1.5h |
| T01-2 | 实现快捷键拦截逻辑 | ScreenshotDetector.vue | 0.5h |
| T01-3 | 实现剪贴板监控逻辑 | ScreenshotDetector.vue | 0.5h |
| T01-4 | 实现 DevTools 检测逻辑 | ScreenshotDetector.vue | 0.5h |

### 3.2 阶段二：后端审计日志（P0）- 3 小时

| 任务 ID | 任务描述 | 文件 | 预估工时 |
|--------|---------|------|---------|
| T02-1 | 创建 SecurityLog 实体 | ImSecurityLog.java | 0.5h |
| T02-2 | 创建 SecurityLogService | ImSecurityLogService.java | 1h |
| T02-3 | 创建审计日志接口 | SecurityLogController.java | 1h |
| T02-4 | 创建数据库表 | im_security_log.sql | 0.5h |

### 3.3 阶段三：集成测试（P1）- 1 小时

| 任务 ID | 任务描述 | 文件 | 预估工时 |
|--------|---------|------|---------|
| T03-1 | 集成到 MainPage.vue | MainPage.vue | 0.5h |
| T03-2 | 测试验证 | - | 0.5h |

**总预估工时：** 7 小时

---

## 4. 验收标准

### 4.1 功能验收

| ID | Given | When | Then |
|----|-------|------|------|
| F01 | 用户按下 PrintScreen | 尝试截图 | 触发警告并记录日志 |
| F02 | 用户按下 Ctrl+P | 尝试打印/截图 | 触发警告并记录日志 |
| F03 | 用户复制内容 | 剪贴板变化 | 记录复制内容审计日志 |
| F04 | 用户打开 DevTools | F12/Ctrl+Shift+I | 触发警告并记录日志 |
| F05 | 页面加载 | 控制台检查 | 控制台内容为空 |

### 4.2 审计验收

| ID | Given | When | Then |
|----|-------|------|------|
| A01 | 截图行为发生 | 查看审计日志 | 有完整的审计记录 |
| A02 | 管理员查询 | 访问审计界面 | 可查看截图行为记录 |

---

## 5. 依赖关系

### 5.1 技术依赖

| 依赖 | 版本要求 | 用途 |
|------|---------|------|
| Clipboard API | 浏览器原生 | 剪贴板监控 |
| KeyboardEvent | 浏览器原生 | 快捷键拦截 |
| Vue 3 | ≥3.3.0 | 前端框架 |

### 5.2 数据依赖

| 数据 | 来源 | 用途 |
|------|------|------|
| 用户 ID | Vuex store | 审计日志 |
| 会话 ID | 当前路由 | 审计日志上下文 |
| IP 地址 | 后端获取 | 审计日志 |

---

*本技术规格说明根据 BMAD 快速规格流程创建*
