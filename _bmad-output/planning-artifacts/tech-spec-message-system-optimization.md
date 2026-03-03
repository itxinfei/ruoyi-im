---
title: 'RuoYi-IM 消息功能系统性优化'
slug: 'ruoyi-im-message-system-optimization'
created: '2026-02-28'
status: 'draft'
stepsCompleted: []
tech_stack: ['Vue 3', 'SCSS', 'Vuex']
files_to_modify:
  - 'ruoyi-im-web/src/components/Chat/MessageBubble.vue'
  - 'ruoyi-im-web/src/components/Chat/MessageItem.vue'
  - 'ruoyi-im-web/src/components/Chat/MessageList.vue'
  - 'ruoyi-im-web/src/components/Chat/MessageInput.vue'
  - 'ruoyi-im-web/src/components/Chat/message-bubble/bubbles/TextBubble.vue'
  - 'ruoyi-im-web/src/components/Chat/message-bubble/parts/MessageHoverActions.vue'
  - 'ruoyi-im-web/src/components/Common/DingtalkAvatar.vue'
  - 'ruoyi-im-web/src/styles/design-tokens.scss'
  - 'ruoyi-im-web/src/views/ChatPanel.vue'
  - 'ruoyi-im-web/src/store/modules/im-message.js'
  - 'ruoyi-im-web/src/store/modules/im-session.js'
code_patterns: []
test_patterns: []
---

# Tech-Spec: RuoYi-IM 消息功能系统性优化

**Created:** 2026-02-28
**Status:** Draft

---

## 1. 问题概述

### 1.1 核心问题

经过分析，当前消息功能存在以下系统性问题：

| 问题类别 | 具体问题 | 影响 | 优先级 |
|---------|---------|------|--------|
| **气泡样式** | 发送消息后气泡从直角变圆角 | 视觉不一致，用户体验差 | P0 |
| **Vuex 架构** | `im/session/UPDATE_SESSION` action 未定义 | 控制台报错，状态管理混乱 | P0 |
| **虚拟滚动** | `vue-virtual-scroller` Key 为 undefined | 组件崩溃，消息无法显示 | P0 |
| **滚动逻辑** | 打开窗口不滚动到最新消息 | 用户需要手动滚动 | P1 |
| **代码重复** | 多个组件有重复的样式和逻辑 | 维护成本高 | P2 |

### 1.2 问题根源分析

**气泡样式问题根源：**
1. `MessageBubble.vue` 定义了气泡样式
2. `TextBubble.vue` 等子组件可能有覆盖样式
3. 消息发送后状态变化（如从 pending 到 sent）可能导致 class 变化
4. `groupPosition` 属性可能未正确计算或传递

**Vuex 架构问题根源：**
1. `im/session/UPDATE_SESSION` action 在 `im-message.js` 中被调用但未在 `im-session.js` 中定义
2. 模块之间职责不清晰
3. 缺少类型定义和接口规范

**虚拟滚动问题根源：**
1. `messagesWithDividers` 包含时间分隔符对象（`{ id: 'time-xxx', isTimeDivider: true }`）
2. 时间分隔符的 `id` 格式与消息 ID 冲突
3. `vue-virtual-scroller` 要求每个 item 有唯一的 `keyField`

---

## 2. 解决方案

### 2.1 气泡样式统一（P0）

**目标：** 确保所有消息气泡样式一致，发送前后保持统一

**修复步骤：**

1. **统一气泡圆角处理**
   ```scss
   // MessageBubble.vue
   .bubble-content {
     // 所有消息统一使用 6px 圆角
     border-radius: var(--dt-radius-bubble-base, 6px);
   }
   
   // 移除合并消息的复杂圆角逻辑（简化）
   &.merge-single,
   &.merge-first,
   &.merge-middle,
   &.merge-last {
     border-radius: var(--dt-radius-bubble-base, 6px);
   }
   ```

2. **检查 TextBubble 组件样式**
   - 确保 `.text-bubble` 没有覆盖 `.bubble-content` 的样式
   - 移除可能的 `border-radius` 硬编码

3. **验证消息状态变化不影响样式**
   - 检查 `message.status` 从 `pending` 到 `sent` 的变化
   - 确保状态变化不触发 class 变化

### 2.2 Vuex 架构修复（P0）

**目标：** 修复未定义 action，清晰模块职责

**修复步骤：**

1. **在 `im-session.js` 中添加 `UPDATE_SESSION` action**
   ```javascript
   // im-session.js
   const actions = {
     // ... existing actions
     UPDATE_SESSION({ commit }, payload) {
       commit('UPDATE_SESSION', payload)
     }
   }
   ```

2. **或在 `im-message.js` 中移除对 `im/session/UPDATE_SESSION` 的调用**
   ```javascript
   // 移除或替换这行
   // await dispatch('im/session/UPDATE_SESSION', ...)
   ```

3. **添加 JSDoc 类型定义**
   ```javascript
   /**
    * @typedef {Object} Session
    * @property {string|number} id
    * @property {string} type
    * @property {string} targetId
    */
   ```

### 2.3 虚拟滚动修复（P0）

**目标：** 修复虚拟滚动器 Key 为 undefined 的问题

**修复步骤：**

1. **确保 `messagesWithDividers` 中每个项目都有唯一 ID**
   ```javascript
   const messagesWithDividers = computed(() => {
     const res = []
     const msgs = props.messages
     
     for (let i = 0; i < msgs.length; i++) {
       const current = { ...msgs[i] }
       const prev = i > 0 ? msgs[i - 1] : null
       
       // 时间分隔符
       if (i === 0 || shouldAddTimeDivider(current, prev)) {
         res.push({
           id: `time-${current.id || i}`, // 确保 ID 唯一
           isTimeDivider: true,
           timeText: formatTimeDivider(getMessageTimestamp(current))
         })
       }
       
       // 消息
       if (!current.id) {
         current.id = `temp-${Date.now()}-${i}` // 临时 ID
       }
       res.push(current)
     }
     
     return res
   })
   ```

2. **虚拟滚动器配置**
   ```vue
   <RecycleScroller
     :items="messagesWithDividers"
     :item-size="80"
     key-field="id"
     :buffer="300"
   />
   ```

3. **移除 `scrollToItem` 调用，改用简单滚动**
   ```javascript
   const scrollToBottomForCurrentMode = () => {
     if (!listRef.value) return
     const scrollHeight = listRef.value.scrollHeight
     listRef.value.scrollTo({
       top: scrollHeight,
       behavior: 'auto'
     })
   }
   ```

### 2.4 滚动逻辑优化（P1）

**目标：** 打开窗口时自动滚动到最新消息

**修复步骤：**

1. **在 `ChatPanel.vue` 中优化会话切换逻辑**
   ```javascript
   watch(() => props.session?.id, async (newId, oldId) => {
     if (newId !== oldId) {
       resetTypingState()
       await loadHistory()
       // 等待 DOM更新
       await nextTick()
       await nextTick()
       msgListRef.value?.scrollToBottomForCurrentMode?.()
     }
   })
   ```

2. **在 `MessageList.vue` 中添加 loading 状态监听**
   ```javascript
   watch(() => props.loading, (newLoading, oldLoading) => {
     if (oldLoading && !newLoading && props.messages.length > 0) {
       nextTick(() => {
         scrollToBottomForCurrentMode()
       })
     }
   }, { flush: 'post' })
   ```

### 2.5 代码重构（P2）

**目标：** 减少代码重复，提高可维护性

**重构步骤：**

1. **提取公共样式到 design-tokens.scss**
   ```scss
   // design-tokens.scss
   --dt-bubble-padding: 8px 12px;
   --dt-bubble-radius: 6px;
   --dt-bubble-shadow-left: 0 1px 2px rgba(0, 0, 0, 0.06);
   --dt-bubble-shadow-right: 0 1px 2px rgba(22, 93, 255, 0.2);
   ```

2. **统一消息状态管理**
   ```javascript
   // 创建 useMessageStatus composable
   export function useMessageStatus(message) {
     const status = computed(() => {
       return message.value?.status || 'pending'
     })
     
     const isSending = computed(() => status.value === 'sending')
     const isSent = computed(() => status.value === 'sent')
     const isFailed = computed(() => status.value === 'failed')
     
     return { status, isSending, isSent, isFailed }
   }
   ```

---

## 3. 实现计划

### 3.1 阶段一：紧急修复（P0）- 4 小时

| 任务 ID | 任务描述 | 文件 | 预估工时 |
|--------|---------|------|---------|
| T01-1 | 统一气泡圆角样式 | MessageBubble.vue | 1h |
| T01-2 | 检查并修复 TextBubble 样式覆盖 | TextBubble.vue | 0.5h |
| T01-3 | 修复 Vuex UPDATE_SESSION action | im-session.js | 0.5h |
| T01-4 | 修复虚拟滚动 Key 为 undefined | MessageList.vue | 1h |
| T01-5 | 验证消息发送后样式一致性 | 测试 | 1h |

### 3.2 阶段二：功能优化（P1）- 3 小时

| 任务 ID | 任务描述 | 文件 | 预估工时 |
|--------|---------|------|---------|
| T02-1 | 优化会话切换滚动逻辑 | ChatPanel.vue | 1h |
| T02-2 | 添加 loading 状态监听 | MessageList.vue | 1h |
| T02-3 | 测试各种场景下的滚动行为 | 测试 | 1h |

### 3.3 阶段三：代码重构（P2）- 4 小时

| 任务 ID | 任务描述 | 文件 | 预估工时 |
|--------|---------|------|---------|
| T03-1 | 提取公共样式到 design-tokens | design-tokens.scss | 1h |
| T03-2 | 创建 useMessageStatus composable | composables/useMessageStatus.js | 1.5h |
| T03-3 | 重构消息状态管理逻辑 | 多个组件 | 1.5h |

**总预估工时：** 11 小时

---

## 4. 验收标准

### 4.1 气泡样式验收

| ID | Given | When | Then |
|----|-------|------|------|
| V01 | 用户发送消息 | 消息发送前后 | 气泡圆角保持 6px 不变 |
| V02 | 用户发送消息 | 消息状态从 pending 到 sent | 气泡样式不变化 |
| V03 | 查看消息列表 | 所有消息气泡 | 圆角、内边距、阴影一致 |

### 4.2 Vuex 架构验收

| ID | Given | When | Then |
|----|-------|------|------|
| V04 | 控制台打开 | 发送消息 | 无 `unknown action type` 错误 |
| V05 | 会话切换 | 查看 Vuex store | 状态更新正确 |

### 4.3 虚拟滚动验收

| ID | Given | When | Then |
|----|-------|------|------|
| V06 | 打开有 100+ 条消息的会话 | 滚动消息列表 | 无 `Key is undefined` 错误 |
| V07 | 发送新消息 | 消息列表更新 | 正常渲染，无崩溃 |

### 4.4 滚动逻辑验收

| ID | Given | When | Then |
|----|-------|------|------|
| V08 | 打开聊天窗口 | 有历史消息 | 自动滚动到最新消息 |
| V09 | 切换会话 | 新会话有消息 | 自动滚动到最新消息 |
| V10 | 发送消息 | 消息发送成功 | 自动滚动到底部 |

---

## 5. 风险评估

| 风险 | 影响 | 概率 | 缓解措施 |
|-----|------|------|---------|
| 样式修复影响其他组件 | 中 | 中 | 充分测试各种消息类型 |
| Vuex 修复影响其他模块 | 中 | 低 | 回归测试相关功能 |
| 虚拟滚动性能下降 | 低 | 低 | 监控性能指标 |

---

## 6. 参考资料

- 钉钉 8.0 UI 设计规范
- Vue 3 最佳实践
- Vuex 模块化架构指南

---

*本技术规格说明根据 BMAD 快速规格流程创建*
