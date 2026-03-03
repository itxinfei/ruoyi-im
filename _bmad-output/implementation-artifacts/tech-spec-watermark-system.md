---
title: '屏幕水印系统实现'
slug: 'screen-watermark-system'
created: '2026-02-28'
status: 'ready-for-dev'
stepsCompleted: [1, 2, 3, 4]
tech_stack: ['Vue 3', 'Canvas', 'SCSS']
files_to_modify:
  - 'ruoyi-im-web/src/components/Security/WatermarkOverlay.vue'
  - 'ruoyi-im-web/src/views/MainPage.vue'
  - 'ruoyi-im-web/src/store/modules/im.js'
  - 'ruoyi-im-api/src/main/java/com/ruoyi/im/domain/WatermarkConfig.java'
  - 'ruoyi-im-api/src/main/java/com/ruoyi/im/service/ImSystemConfigService.java'
code_patterns:
  - '使用 Canvas 生成半透明水印'
  - '使用 MutationObserver 防止水印被移除'
  - '水印内容包含：用户名 + 时间 + 工号/IP'
test_patterns:
  - '水印是否正确显示用户信息'
  - '水印是否全屏平铺'
  - '水印透明度是否正确'
  - '尝试移除水印是否被阻止'
---

# Tech-Spec: 屏幕水印系统实现

**Created:** 2026-02-28
**Status:** Ready for Development

---

## 1. 需求概述

### 1.1 问题陈述

企业内网环境中，员工可能通过截图、拍照等方式泄露敏感信息，需要添加屏幕水印以便追溯责任人。

### 1.2 解决方案

实现全屏半透明水印系统，水印内容包含用户身份信息，即使用户截图也能追溯到泄露源头。

### 1.3 范围

**包含范围 (In Scope):**
- 水印组件开发（Canvas 生成）
- 水印配置管理（管理员可配置）
- 水印强制显示（用户无法关闭）
- 水印内容：用户名 + 时间 + 工号/IP

**不包含范围 (Out of Scope):**
- 截图检测功能（另外的需求）
- 拍照检测功能（另外的需求）
- 水印识别系统（后端需求）

---

## 2. 开发上下文

### 2.1 水印设计规格

```
水印效果：
┌─────────────────────────────────────────────────────┐
│ ╱ 张三 ╱ 2026-02-28 15:30 ╱ 工号:10086 ╱          │
│ ╱ 张三 ╱ 2026-02-28 15:30 ╱ 工号:10086 ╱          │
│ ╱ 张三 ╱ 2026-02-28 15:30 ╱ 工号:10086 ╱          │
│ ╱ 张三 ╱ 2026-02-28 15:30 ╱ 工号:10086 ╱          │
└─────────────────────────────────────────────────────┘
```

**水印配置参数：**

| 参数 | 默认值 | 说明 |
|------|--------|------|
| 内容模板 | `${username} \| ${datetime} \| ${employeeId}` | 可自定义 |
| 字体大小 | 14px | 水印文字大小 |
| 透明度 | 0.15 (15%) | 水印透明度 |
| 旋转角度 | -25 度 | 水印倾斜角度 |
| 间距 | [200, 150] | [横向间距，纵向间距] |
| 颜色 | #000000 | 水印颜色 |

### 2.2 技术决策

1. **Canvas 生成水印**：使用 Canvas API 生成水印图片，性能好，可定制
2. **Base64 背景**：将 Canvas 转为 Base64，作为背景图平铺
3. **MutationObserver**：监听 DOM 变化，防止水印被移除
4. **全局覆盖**：水印层覆盖整个应用，pointer-events: none 不影响交互

---

## 3. 实现计划

### 3.1 阶段一：水印组件开发（P0）- 2 小时

| 任务 ID | 任务描述 | 文件 | 预估工时 |
|--------|---------|------|---------|
| T01-1 | 创建 WatermarkOverlay 组件 | WatermarkOverlay.vue | 1h |
| T01-2 | 实现 Canvas 水印生成逻辑 | WatermarkOverlay.vue | 0.5h |
| T01-3 | 实现水印防移除保护 | WatermarkOverlay.vue | 0.5h |

### 3.2 阶段二：集成到应用（P0）- 1 小时

| 任务 ID | 任务描述 | 文件 | 预估工时 |
|--------|---------|------|---------|
| T02-1 | 在 MainPage.vue 中添加水印组件 | MainPage.vue | 0.5h |
| T02-2 | 在 store 中添加用户信息 state | im.js | 0.5h |

### 3.3 阶段三：后端配置接口（P1）- 2 小时

| 任务 ID | 任务描述 | 文件 | 预估工时 |
|--------|---------|------|---------|
| T03-1 | 创建 WatermarkConfig 实体 | WatermarkConfig.java | 0.5h |
| T03-2 | 创建水印配置查询接口 | ImSystemConfigService.java | 1h |
| T03-3 | 创建水印配置更新接口 | ImSystemConfigService.java | 0.5h |

**总预估工时：** 5 小时

---

## 4. 验收标准

### 4.1 功能验收

| ID | Given | When | Then |
|----|-------|------|------|
| F01 | 用户登录系统 | 查看任何页面 | 屏幕显示半透明水印 |
| F02 | 水印内容 | 查看水印 | 包含用户名、时间、工号/IP |
| F03 | 水印样式 | 查看水印 | 斜向平铺、透明度 15% |
| F04 | 尝试用开发者工具移除水印 | 删除水印 DOM 元素 | 水印自动恢复 |
| F05 | 水印层 | 尝试点击水印 | 不影响页面正常交互 |

### 4.2 配置验收

| ID | Given | When | Then |
|----|-------|------|------|
| C01 | 管理员修改水印配置 | 用户刷新页面 | 水印样式按新配置显示 |
| C02 | 水印关闭配置 | 用户刷新页面 | 水印不显示（如允许关闭） |

### 4.3 兼容性验收

| ID | Given | When | Then |
|----|-------|------|------|
| B01 | Chrome 90+ 浏览器 | 访问页面 | 水印正常显示 |
| B02 | Firefox 88+ 浏览器 | 访问页面 | 水印正常显示 |
| B03 | Edge 90+ 浏览器 | 访问页面 | 水印正常显示 |

---

## 5. 依赖关系

### 5.1 技术依赖

| 依赖 | 版本要求 | 用途 |
|------|---------|------|
| Canvas API | HTML5 原生 | 水印生成 |
| MutationObserver | HTML5 原生 | DOM 监听 |
| Vue 3 | ≥3.3.0 | 前端框架 |

### 5.2 数据依赖

| 数据 | 来源 | 用途 |
|------|------|------|
| 用户名 | Vuex store (user.currentUser) | 水印内容 |
| 工号 | Vuex store (user.currentUser.employeeId) | 水印内容 |
| IP 地址 | 浏览器获取 | 水印内容（工号缺失时） |

---

## 6. 风险评估

| 风险 | 影响 | 概率 | 缓解措施 |
|-----|------|------|---------|
| 水印影响性能 | 中 | 低 | 使用 Canvas 缓存，减少重绘 |
| 水印被移除 | 高 | 中 | MutationObserver 监听恢复 |
| 用户反感 | 中 | 中 | 透明度调低，不影响使用 |

---

## 7. 参考资料

- 钉钉水印实现
- 企业安全合规要求
- Canvas API 文档

---

*本技术规格说明根据 BMAD 快速规格流程创建*
