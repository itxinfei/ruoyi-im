---
title: 'RuoYi-IM 消息 UI 优化对齐钉钉'
slug: 'ruoyi-im-message-ui-dingtalk-align'
created: '2026-02-28'
status: 'ready-for-dev'
stepsCompleted: [1, 2, 3, 4]
tech_stack: ['Vue 3', 'SCSS', 'Element Plus']
files_to_modify:
  - 'ruoyi-im-web/src/components/Chat/MessageBubble.vue'
  - 'ruoyi-im-web/src/components/Chat/MessageItem.vue'
  - 'ruoyi-im-web/src/components/Chat/MessageList.vue'
  - 'ruoyi-im-web/src/components/Chat/MessageInput.vue'
  - 'ruoyi-im-web/src/components/Chat/message-bubble/parts/MessageHoverActions.vue'
  - 'ruoyi-im-web/src/components/Chat/message-bubble/bubbles/TextBubble.vue'
  - 'ruoyi-im-web/src/components/Chat/message-bubble/bubbles/ImageBubble.vue'
  - 'ruoyi-im-web/src/components/Chat/message-bubble/bubbles/FileBubble.vue'
  - 'ruoyi-im-web/src/styles/design-tokens.scss'
  - 'ruoyi-im-web/src/components/Chat/ImSideNav.vue'
  - 'ruoyi-im-web/src/components/Chat/ChatSidebar.vue'
  - 'ruoyi-im-web/src/components/Common/DingtalkAvatar.vue'
code_patterns:
  - '使用设计令牌 (design-tokens.scss) 管理样式'
  - '组件采用 Composition API + <script setup> 语法'
  - 'SCSS 使用 @use 导入设计令牌'
  - '消息气泡使用 merge-single/merge-first/merge-middle/merge-last 类处理合并样式'
  - '过渡动画使用 --dt-transition-base 令牌'
  - '响应式断点使用 --dt-breakpoint-md 等令牌'
test_patterns:
  - '视觉回归测试：使用 Chrome DevTools Computed 样式验证'
  - '交互测试：使用 Chrome DevTools Performance 面板验证'
  - '响应式测试：在 375px、640px、768px、1024px、1280px、1920px 屏幕下验证'
  - '暗色模式测试：验证所有组件的暗色模式样式'
  - '性能测试：1000 条消息下的渲染性能'
---

# Tech-Spec: RuoYi-IM 消息 UI 优化对齐钉钉

**Created:** 2026-02-28
**Status:** Ready for Development

---

## 1. 需求概述

### 1.1 问题陈述

当前 RuoYi-IM 消息 UI 已实现基本功能，但需要进一步对齐钉钉 8.0 的视觉标准和交互体验，提升用户体验和视觉一致性。

### 1.2 解决方案

基于现有代码基础，微调样式细节（圆角、阴影、间距），优化交互动画过渡，确保响应式布局在各屏幕尺寸下表现一致。

### 1.3 范围

**包含范围 (In Scope):**
- 消息气泡样式微调（圆角、阴影、内边距）
- 消息项布局优化（头像、间距）
- 悬停操作栏样式对齐
- 时间分隔符样式优化（胶囊样式）
- 消息输入框工具栏优化
- 暗色模式适配
- 响应式布局调整

**不包含范围 (Out of Scope):**
- 新功能开发
- 后端 API 修改
- 数据库结构变更
- 移动端原生适配

---

## 2. 开发上下文

### 2.1 代码库模式 (Codebase Patterns)

根据深度代码调查，现有代码采用以下模式：

1. **设计令牌系统**: 所有样式通过 `design-tokens.scss` 管理
   - 气泡圆角：`--dt-radius-bubble-base: 6px`、`--dt-bubble-radius-merge: 2px`
   - 气泡阴影：`--dt-bubble-left-shadow`、`--dt-bubble-right-shadow`
   - 颜色令牌：`--dt-bubble-left-bg: #FFFFFF`、`--dt-bubble-right-bg: #165DFF`

2. **组件架构**: 采用 Vue 3 Composition API + `<script setup>` 语法

3. **消息合并逻辑**:
   - 使用 `canMergeWith()` 判断消息是否可合并（同一发送者、时间间隔<2 分钟）
   - 合并位置：`single`、`first`、`middle`、`last`
   - 合并圆角：首尾 6px，中间 2px

4. **SCSS 组织**: 使用 `@use '@/styles/design-tokens.scss' as *` 导入令牌

### 2.2 待修改文件

| 文件路径 | 用途 | 当前状态 |
|---------|------|---------|
| `MessageBubble.vue` | 消息气泡组件 | ✅ 已实现钉钉 8.0 基础样式 |
| `MessageItem.vue` | 消息项组件 | ✅ 已实现基础布局 |
| `MessageList.vue` | 消息列表组件 | ✅ 已实现虚拟滚动、时间分隔符 |
| `MessageHoverActions.vue` | 悬停操作栏 | ✅ 已实现钉钉风格操作栏 |
| `MessageInput.vue` | 消息输入框 | ✅ 已实现工具栏 |
| `TextBubble.vue` | 文本气泡 | ✅ 需检查样式对齐 |
| `ImageBubble.vue` | 图片气泡 | ✅ 需检查圆角对齐 |
| `FileBubble.vue` | 文件气泡 | ✅ 需检查样式对齐 |
| `design-tokens.scss` | 设计令牌 | ✅ 已定义完整令牌系统 |
| `DingtalkAvatar.vue` | 头像组件 | ✅ 需检查圆角对齐 |

### 2.3 技术决策

1. **圆角标准**: 采用钉钉 8.0 标准
   - 单条消息：6px
   - 合并消息：首尾 6px，中间 2px
   - 媒体消息（图片/视频）：2px

2. **阴影标准**: 微阴影设计
   - 左侧气泡：`0 1px 2px rgba(0, 0, 0, 0.06)`
   - 右侧气泡：`0 1px 2px rgba(22, 93, 255, 0.2)`

3. **间距标准**: 4px 基础单位
   - 气泡内边距：8px 12px
   - 头像与气泡间距：8px
   - 消息项间距：4px

---

## 3. 实现计划

### 3.1 阶段一：UI 布局对齐 (优先级：P0) - 核心对齐

**目标：** 确保消息气泡、头像、间距等核心布局元素与钉钉 8.0 精确对齐

| 任务 ID | 任务描述 | 文件 | CSS 属性/修改 | 预估工时 |
|--------|---------|------|--------------|---------|
| T01-1 | 消息气泡圆角标准化 | MessageBubble.vue | `.bubble-other`/`.bubble-own` 的 `border-radius: var(--dt-radius-bubble-base, 6px)` | 0.5h |
| T01-2 | 合并消息圆角标准化 | MessageBubble.vue | `.merge-first`: 6px 6px 2px 2px, `.merge-middle`: 2px, `.merge-last`: 2px 2px 6px 6px | 0.5h |
| T01-3 | 媒体消息圆角标准化 | MessageBubble.vue | `:has(.image-bubble)`/`:has(.video-bubble)` 的 `border-radius: var(--dt-bubble-radius-merge, 2px)` | 0.5h |
| T02-1 | 左侧气泡阴影标准化 | MessageBubble.vue | `.bubble-other` 的 `box-shadow: var(--dt-bubble-left-shadow, 0 1px 2px rgba(0,0,0,0.06))` | 0.5h |
| T02-2 | 右侧气泡阴影标准化 | MessageBubble.vue | `.bubble-own` 的 `box-shadow: var(--dt-bubble-right-shadow, 0 1px 2px rgba(22,93,255,0.2))` | 0.5h |
| T03-1 | 气泡内边距标准化 | MessageBubble.vue | `.bubble-content` 的 `padding: 8px 12px` | 0.5h |
| T03-2 | 消息项间距标准化 | MessageItem.vue | `.message-item` 的 `margin-bottom: 4px` | 0.5h |
| T03-3 | 头像与气泡间距标准化 | MessageItem.vue | `.avatar-box` 的 `margin-top: 2px`, `.content-box` 的 `margin: 0 8px` | 0.5h |
| T03-4 | 头像尺寸标准化 | MessageItem.vue | `DingtalkAvatar` 的 `size="36"`, `.avatar-box` 的 `width/height: 36px` | 0.5h |
| T03-5 | 头像圆角标准化 | MessageItem.vue/DingtalkAvatar.vue | `border-radius: 4px` (钉钉 8.0 方圆角) | 0.5h |
| T04-1 | 时间分隔符胶囊样式 | MessageList.vue | `background: var(--dt-brand-lighter)`, `border-radius: 100px`, `padding: 4px 12px` | 1h |
| T05-1 | 悬停操作栏位置标准化 | MessageHoverActions.vue | `transform: translateY(calc(-100% - 4px))`, `gap: 2px` | 0.5h |
| T05-2 | 悬停操作栏按钮尺寸 | MessageHoverActions.vue | `.hover-action-btn` 的 `width/height: 24px`, `border-radius: 4px` | 0.5h |

**阶段一小计：** 7.5 小时

### 3.2 阶段二：交互优化 (优先级：P1)

| 任务 ID | 任务描述 | 文件 | CSS 属性/修改 | 预估工时 |
|--------|---------|------|--------------|---------|
| T06-1 | 悬停显示动画 | MessageBubble.vue | `transition: opacity 0.2s ease` | 1h |
| T06-2 | 悬停隐藏延迟 | MessageBubble.vue | `hoverCloseTimer = setTimeout(..., 100)` | 0.5h |
| T07-1 | 操作栏按钮悬停效果 | MessageHoverActions.vue | `:hover` 的 `background: var(--dt-bg-hover)`, `color: var(--dt-brand-color)` | 1h |
| T07-2 | 操作栏阴影过渡 | MessageHoverActions.vue | `transition: box-shadow 0.2s ease` | 0.5h |
| T08-1 | 响应式消息最大宽度 | MessageItem.vue | `@media (max-width: 768px)` 的 `max-width: 85%` | 1h |
| T08-2 | 响应式头像尺寸 | MessageItem.vue | `@media (max-width: 768px)` 的头像尺寸调整 | 0.5h |
| T08-3 | 响应式操作栏隐藏 | MessageHoverActions.vue | `@media (max-width: 768px)` 隐藏 PC 悬停栏，显示移动端菜单 | 1h |

**阶段二小计：** 6.5 小时

### 3.3 阶段三：细节完善 (优先级：P2)

| 任务 ID | 任务描述 | 文件 | CSS 属性/修改 | 预估工时 |
|--------|---------|------|--------------|---------|
| T09-1 | 暗色模式气泡背景 | MessageBubble.vue | `[data-theme='dark'] .bubble-other` 的 `background: var(--dt-bubble-left-bg-dark, #1e1e1e)` | 1.5h |
| T09-2 | 暗色模式气泡阴影 | MessageBubble.vue | `[data-theme='dark']` 的 `box-shadow: var(--dt-bubble-left-shadow-dark, 0 1px 2px rgba(0,0,0,0.2))` | 1.5h |
| T09-3 | 暗色模式操作栏 | MessageHoverActions.vue | `[data-theme='dark']` 的 `background: var(--dt-bg-card-dark)`, `border-color: var(--dt-border-dark)` | 1.5h |
| T10-1 | 移动端触摸反馈 | MessageHoverActions.vue | `:active` 的 `transform: scale(0.95)` | 1h |
| T10-2 | 移动端安全区域 | MessageInput.vue | `padding-bottom: env(safe-area-inset-bottom, 20px)` | 1h |
| T10-3 | 移动端虚拟键盘适配 | MessageInput.vue | 焦点时滚动确保输入框可见 | 1h |
| T11-1 | 虚拟滚动性能优化 | MessageList.vue | 优化 `v-memo` 缓存键 | 1.5h |
| T11-2 | 合并缓存优化 | MessageList.vue | `mergeCache` 大小限制和清理 | 1.5h |

**阶段三小计：** 10 小时

### 3.4 阶段四：代码规范化 (优先级：P2)

| 任务 ID | 任务描述 | 文件 | 修改内容 | 预估工时 |
|--------|---------|------|---------|---------|
| T12-1 | 圆角令牌审查 | 所有 SCSS 文件 | 确保所有 `border-radius` 使用设计令牌 | 1h |
| T12-2 | 硬编码圆角替换 | 所有 SCSS 文件 | 替换 `6px`/`8px`/`2px` 为令牌 | 1h |
| T13-1 | 阴影令牌审查 | 所有 SCSS 文件 | 确保所有 `box-shadow` 使用设计令牌 | 1h |
| T13-2 | 硬编码阴影替换 | 所有 SCSS 文件 | 替换硬编码阴影为令牌 | 1h |
| T14-1 | 过渡令牌审查 | 所有 SCSS 文件 | 确保所有 `transition` 使用设计令牌 | 1h |
| T15-1 | 钉钉 8.0 标准注释 | 所有组件 | 添加 `// 钉钉 8.0 标准：8px 12px 内边距` 等注释 | 2h |

**阶段四小计：** 7 小时

---

**总预估工时：** 31 小时

---

## 4. 验收标准

### 4.1 视觉验收 (UI 布局对齐)

**验收工具：** Chrome DevTools - Elements 面板 - Computed 样式

| ID | Given | When | Then | 测量方法 |
|----|-------|------|------|---------|
| V01 | 单条消息气泡已渲染 | 使用 DevTools 检查 `.bubble-other` | `border-radius: 6px`, `padding: 8px 12px` | DevTools Computed 样式 |
| V02 | 合并消息首条已渲染 | 使用 DevTools 检查 `.merge-first` | `border-radius: 6px 6px 2px 2px` | DevTools Computed 样式 |
| V03 | 合并消息中间已渲染 | 使用 DevTools 检查 `.merge-middle` | `border-radius: 2px` | DevTools Computed 样式 |
| V04 | 合并消息末尾已渲染 | 使用 DevTools 检查 `.merge-last` | `border-radius: 2px 2px 6px 6px` | DevTools Computed 样式 |
| V05 | 图片消息已渲染 | 使用 DevTools 检查 `:has(.image-bubble)` | `border-radius: 2px` | DevTools Computed 样式 |
| V06 | 左侧消息气泡已渲染 | 使用 DevTools 检查 `.bubble-other` | `box-shadow: 0 1px 2px rgba(0,0,0,0.06)` | DevTools Computed 样式 |
| V07 | 右侧消息气泡已渲染 | 使用 DevTools 检查 `.bubble-own` | `box-shadow: 0 1px 2px rgba(22,93,255,0.2)` | DevTools Computed 样式 |
| V08 | 消息项已渲染 | 使用 DevTools 检查 `.message-item` | `margin-bottom: 4px` | DevTools Computed 样式 |
| V09 | 头像已渲染 | 使用 DevTools 检查 `.avatar-box` | `width: 36px`, `height: 36px`, `margin-top: 2px` | DevTools Computed 样式 |
| V10 | 头像已渲染 | 使用 DevTools 检查 `.dingtalk-avatar` | `border-radius: 4px` | DevTools Computed 样式 |
| V11 | 头像与气泡间距 | 使用 DevTools 检查 `.content-box` | `margin-left: 8px` 或 `margin-right: 8px` | DevTools Computed 样式 |
| V12 | 时间分隔符已渲染 | 使用 DevTools 检查 `.time-divider` | `background: rgba(50,150,250,0.05)`, `border-radius: 100px`, `padding: 4px 12px` | DevTools Computed 样式 |
| V13 | 悬停操作栏已渲染 | 使用 DevTools 检查 `.message-hover-actions` | `gap: 2px`, `transform: translateY(-104%)` | DevTools Computed 样式 |
| V14 | 悬停操作栏按钮已渲染 | 使用 DevTools 检查 `.hover-action-btn` | `width: 24px`, `height: 24px`, `border-radius: 4px` | DevTools Computed 样式 |
| V15 | 消息最大宽度 | 使用 DevTools 检查 `.content-box` | `max-width: 70%` | DevTools Computed 样式 |
| V16 | 移动端消息最大宽度 | 屏幕宽度<768px 时检查 `.message-bubble` | `max-width: 85%` | DevTools + 响应式模式 |

### 4.2 交互验收

**验收工具：** Chrome DevTools - Performance 面板

| ID | Given | When | Then | 测量方法 |
|----|-------|------|------|---------|
| I01 | 鼠标移入消息气泡 | 悬停操作栏显示 | 显示延迟 <100ms | Performance 面板测量 |
| I02 | 鼠标移出消息气泡 | 悬停操作栏隐藏 | 延迟 100ms 后隐藏 | 代码逻辑验证 |
| I03 | 悬停操作栏显示 | 按钮悬停 | `background` 变为 `var(--dt-bg-hover)`, `color` 变为 `var(--dt-brand-color)` | DevTools Computed 样式 |
| I04 | 消息列表滚动 | 新消息进入视口 | 已读状态自动上报 | 网络请求验证 |
| I05 | 移动端长按消息 | 操作菜单显示 | 500ms 后显示底部菜单 | 手动计时验证 |
| I06 | 移动端触摸按钮 | 触摸反馈 | `transform: scale(0.95)` | DevTools Computed 样式 |

### 4.3 兼容性验收

| ID | Given | When | Then |
|----|-------|------|------|
| C01 | Chrome 90+ 浏览器 | 访问页面 | 所有样式和交互正常 |
| C02 | Firefox 88+ 浏览器 | 访问页面 | 所有样式和交互正常 |
| C03 | Safari 14+ 浏览器 | 访问页面 | 所有样式和交互正常 |
| C04 | Edge 90+ 浏览器 | 访问页面 | 所有样式和交互正常 |
| C05 | 屏幕宽度 375px | 查看消息列表 | 响应式布局正常，最大宽度 85% |
| C06 | 屏幕宽度 768px | 查看消息列表 | 响应式布局正常 |
| C07 | 屏幕宽度 1024px | 查看消息列表 | 响应式布局正常 |
| C08 | 屏幕宽度 1920px | 查看消息列表 | 响应式布局正常 |
| C09 | 暗色模式已启用 | 查看消息气泡 | 左侧背景为#1e1e1e，右侧保持#165DFF |
| C10 | 暗色模式已启用 | 查看悬停操作栏 | 背景为 `var(--dt-bg-card-dark)`, 边框为 `var(--dt-border-dark)` |

### 4.4 性能验收

**验收工具：** Chrome DevTools - Performance 面板

| ID | 指标 | 目标值 | 测试条件 | 测量方法 |
|----|------|--------|---------|---------|
| P01 | 1000 条消息首屏渲染 | <500ms | 空缓存，CPU 4x 降速 | Performance 面板 |
| P02 | 滚动帧率 | ≥60fps | 1000 条消息列表 | Performance 面板 Frame 计数 |
| P03 | 悬停响应延迟 | <100ms | 鼠标移入消息 | Performance 面板 Event 时间戳 |
| P04 | 动画过渡时间 | 200ms±10ms | CSS `transition` 属性 | Performance 面板 |
| P05 | 窗口 resize 响应 | <50ms | 窗口尺寸变化 | Performance 面板 |

---

## 5. 风险评估

| 风险 | 影响 | 概率 | 缓解措施 |
|-----|------|------|---------|
| 样式冲突 | 中 | 低 | 使用设计令牌，避免硬编码 |
| 性能下降 | 低 | 低 | 保持现有虚拟滚动机制 |
| 响应式问题 | 中 | 中 | 充分测试各屏幕尺寸 |
| 暗色模式遗漏 | 低 | 中 | 建立暗色模式检查清单 |

---

## 6. 依赖关系

### 6.1 技术依赖

| 依赖 | 版本要求 | 用途 |
|------|---------|------|
| Element Plus | ≥2.3.0 | UI 组件库 |
| Vue | ≥3.3.0 | 前端框架 |
| vue-virtual-scroller | ≥2.0.0-beta.8 | 虚拟滚动 |

### 6.2 浏览器支持

| 浏览器 | 最低版本 | 说明 |
|--------|---------|------|
| Chrome | 90+ | 主要支持浏览器 |
| Firefox | 88+ | 完整支持 |
| Safari | 14+ | 完整支持 |
| Edge | 90+ | 基于 Chromium，完整支持 |

### 6.3 设计资源

| 资源 | 状态 | 说明 |
|------|------|------|
| 钉钉 8.0 设计规范 | ✅ 已有 | 参考 `design-tokens.scss` |
| 视觉对比截图 | ⚠️ 待提供 | 建议创建视觉对比文档 |

---

## 7. 兼容性约束

### 7.1 消息类型兼容

必须保持与以下现有消息类型的兼容：

- ✅ 文本消息 (TextBubble)
- ✅ 图片消息 (ImageBubble)
- ✅ 文件消息 (FileBubble)
- ✅ 视频消息 (VideoBubble)
- ✅ 系统消息 (SystemBubble)
- ✅ 撤回消息 (RecalledBubble)

### 7.2 协议兼容

- ✅ 保持与现有 WebSocket 消息协议兼容
- ✅ 保持与现有后端 API 兼容
- ✅ 无需数据库结构变更

### 7.3 功能兼容

- ✅ 保持现有虚拟滚动机制
- ✅ 保持现有消息合并逻辑
- ✅ 保持现有已读上报机制

---

## 8. 性能指标

### 8.1 渲染性能

| 指标 | 目标值 | 测量方法 |
|------|--------|---------|
| 1000 条消息首屏渲染 | <500ms | Chrome DevTools Performance |
| 滚动帧率 | ≥60fps | Chrome DevTools Performance |
| 虚拟滚动触发阈值 | 100 条消息 | 代码逻辑验证 |

### 8.2 交互性能

| 指标 | 目标值 | 测量方法 |
|------|--------|---------|
| 悬停操作栏显示延迟 | <100ms | Chrome DevTools Performance |
| 悬停操作栏隐藏延迟 | 100ms (intentional) | 代码逻辑验证 |
| 动画过渡时间 | 200ms | CSS `transition` 属性 |

### 8.3 响应式性能

| 指标 | 目标值 | 测量方法 |
|------|--------|---------|
| 窗口 resize 响应 | <50ms | Chrome DevTools Performance |
| 防抖更新频率 | 200ms | 代码逻辑验证 |

---

## 9. 参考资料

- 钉钉 8.0 UI 设计规范
- 项目设计令牌文档 (`design-tokens.scss`)
- 现有实现代码

---

*本技术规格说明根据 BMAD 快速规格流程创建*
