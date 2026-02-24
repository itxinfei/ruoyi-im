# 消息气泡组件重构文档

## 概述

将原本 2100+ 行的单一 `MessageBubble.vue` 组件重构为模块化架构，提高可维护性和可测试性。

## 目录结构

```
message-bubble/
├── MessageBubbleRefactored.vue    # 主组件（重构版）
├── index.js                        # 组件导出索引
├── composables/                    # 组合式函数
│   ├── useMessageBubble.js        # 气泡通用逻辑
│   ├── useMessageStatus.js        # 状态管理
│   └── useMessageReaction.js      # 表情回应
├── bubbles/                        # 消息类型子组件
│   ├── TextBubble.vue             # 文本消息
│   ├── ImageBubble.vue            # 图片消息
│   ├── FileBubble.vue             # 文件消息
│   ├── VoiceBubble.vue            # 语音消息
│   ├── VideoBubble.vue            # 视频消息
│   ├── LocationBubble.vue         # 位置消息
│   ├── SystemBubble.vue           # 系统消息
│   └── RecalledBubble.vue         # 撤回消息
├── parts/                          # 零件组件
│   ├── MessageReplyRef.vue        # 引用回复块
│   ├── MessageStatus.vue          # 状态指示器
│   ├── MessageReactions.vue       # 表情聚合
│   ├── CodeBlock.vue              # 代码块
│   ├── MessageMarkers.vue         # 消息标记
│   └── LinkCardList.vue           # 链接卡片
└── styles/                         # 样式文件
    └── message-bubble.scss        # 统一样式
```

## 改进点

### 1. 代码组织

| 改进前 | 改进后 |
|--------|--------|
| 单文件 2100+ 行 | 主文件 ~300 行 |
| 样式与逻辑混杂 | 逻辑/样式/组件分离 |
| 难以定位问题 | 按消息类型快速定位 |

### 2. 样式优化

| 改进前 | 改进后 |
|--------|--------|
| 硬编码 `#D4EAFF` | 使用 `var(--dt-bubble-right-bg)` |
| 分散的动画定义 | 集中在 design-tokens.scss |
| 重复的样式代码 | 抽取为共享样式 |

### 3. 可维护性

- **单一职责**：每个子组件只负责一种消息类型
- **易于扩展**：新增消息类型只需添加新的子组件
- **易于测试**：每个组合式函数可独立测试

## 迁移指南

### 方式一：直接替换（推荐）

```vue
<!-- 旧版本 -->
<MessageBubble
  :message="message"
  :session-type="sessionType"
  @command="handleCommand"
  @preview="handlePreview"
/>

<!-- 新版本 -->
<MessageBubble
  :message="message"
  :session-type="sessionType"
  @command="handleCommand"
  @preview="handlePreview"
/>
```

API 保持完全兼容，无需修改调用代码。

### 方式二：渐进式迁移

1. 保留旧的 `MessageBubble.vue`
2. 新建 `MessageBubbleRefactored.vue`
3. 在 `MessageList.vue` 中切换导入：

```vue
<script setup>
// 旧版
// import MessageBubble from '@/components/Chat/MessageBubble.vue'

// 新版
import MessageBubble from '@/components/Chat/MessageBubbleRefactored.vue'
</script>
```

## 组件 API

### MessageBubble (主组件)

#### Props

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | Object | 是 | 消息对象 |
| sessionType | String | 否 | 会话类型 (默认: 'PRIVATE') |

#### Events

| 事件 | 参数 | 说明 |
|------|------|------|
| command | (cmd: string, message: Object) | 菜单命令 |
| preview | (url: string) | 预览图片 |
| download | (file: Object) | 下载文件 |
| at | (message: Object) | @提及 |
| scroll-to | (messageId: string) | 滚动到消息 |
| retry | (message: Object) | 重试发送 |
| re-edit | (content: string) | 重新编辑 |
| show-user | (userId: string) | 查看用户 |

### 子组件使用示例

```vue
<template>
  <!-- 单独使用文本气泡 -->
  <TextBubble
    :message="message"
    :has-markers="true"
    @scroll-to="handleScrollTo"
  />

  <!-- 单独使用图片气泡 -->
  <ImageBubble
    :message="message"
    @preview="handlePreview"
  />
</template>

<script setup>
import { TextBubble, ImageBubble } from '@/components/Chat/message-bubble'
</script>
```

### Composables 使用示例

```vue
<script setup>
import { useMessageBubble, useMessageStatus, useMessageReaction } from '@/components/Chat/message-bubble'

const props = defineProps({ message: Object })

const emit = defineEmits(['click', 'retry'])

// 使用气泡逻辑
const {
  isSelected,
  parsedContent,
  handleClick,
  handleCommand,
  handleRetry
} = useMessageBubble(props, emit)

// 使用状态管理
const {
  recallTimeDisplay,
  isSending,
  isFailed
} = useMessageStatus(props)

// 使用表情回应
const {
  hasReactions,
  toggleReaction,
  addReaction
} = useMessageReaction(props, emit)
</script>
```

## 样式变量

重构后的组件完全使用 design tokens，支持亮/暗主题切换：

```scss
// 对方消息气泡
--dt-bubble-left-bg: #FFFFFF;
--dt-bubble-left-border: #E4E7ED;

// 自己消息气泡
--dt-bubble-right-bg: #E8F4FF;
--dt-bubble-right-bg-hover: #DBEEFF;
```

暗色模式自动适配：

```scss
.dark {
  --dt-bubble-left-bg: #2d2d2d;
  --dt-bubble-right-bg: #0958d9;
}
```

## 未来扩展

添加新消息类型只需三步：

1. 创建子组件 `bubbles/NewTypeBubble.vue`
2. 在主组件中导入并使用
3. 添加对应样式（如需特殊样式）

```vue
<!-- MessageBubbleRefactored.vue -->
<NewTypeBubble
  v-else-if="message.type === 'NEW_TYPE'"
  :message="message"
  @custom-event="$emit('custom-event', $event)"
/>
```
