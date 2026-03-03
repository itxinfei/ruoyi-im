# 📚 组件使用指南

**版本**: 1.0.0  
**更新日期**: 2026-03-03  
**适用范围**: IM 即时通讯系统

---

## 目录

1. [快捷操作栏](#快捷操作栏)
2. [快捷表情栏](#快捷表情栏)
3. [错误边界](#错误边界)
4. [使用示例](#使用示例)

---

## 快捷操作栏

### 组件路径
```javascript
import QuickActionsBar from '@/components/Chat/QuickActionsBar.vue'
```

### Props

无

### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| create-group | - | 点击"发起群聊"按钮 |
| add-friend | - | 点击"添加好友"按钮 |
| join-group | - | 点击"加入群组"按钮 |
| search | - | 点击"搜索"按钮 |

### 使用示例

```vue
<template>
  <QuickActionsBar
    @create-group="handleCreateGroup"
    @add-friend="handleAddFriend"
    @join-group="handleJoinGroup"
    @search="handleSearch"
  />
</template>

<script setup>
import QuickActionsBar from '@/components/Chat/QuickActionsBar.vue'

const handleCreateGroup = () => {
  console.log('发起群聊')
  // 打开创建群组对话框
}

const handleAddFriend = () => {
  console.log('添加好友')
  // 打开添加好友对话框
}

const handleJoinGroup = () => {
  console.log('加入群组')
  // 打开加入群组对话框
}

const handleSearch = () => {
  console.log('搜索')
  // 打开全局搜索
}
</script>
```

### 样式定制

```scss
// 通过 CSS 变量定制
.quick-actions-bar {
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
}

.quick-action-btn {
  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-brand-color);
  }
}
```

---

## 快捷表情栏

### 组件路径
```javascript
import QuickEmojiBar from '@/components/Chat/QuickEmojiBar.vue'
```

### Props

无

### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| select | emoji: string | 选择表情 |
| toggle-picker | - | 切换表情选择器 |

### 使用示例

```vue
<template>
  <QuickEmojiBar
    @select="insertEmoji"
    @toggle-picker="toggleEmojiPicker"
  />
</template>

<script setup>
import QuickEmojiBar from '@/components/Chat/QuickEmojiBar.vue'
import { ref } from 'vue'

const messageContent = ref('')
const showEmojiPicker = ref(false)

const insertEmoji = (emoji) => {
  // 在光标位置插入表情
  const textarea = document.querySelector('textarea')
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  
  messageContent.value = 
    messageContent.value.slice(0, start) + 
    emoji + 
    messageContent.value.slice(end)
}

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}
</script>
```

### 自定义表情

```vue
<template>
  <QuickEmojiBar>
    <template #extra>
      <button @click="addCustomEmoji">
        自定义表情
      </button>
    </template>
  </QuickEmojiBar>
</template>
```

---

## 错误边界

### 组件路径
```javascript
import ErrorBoundary from '@/components/Base/ErrorBoundary.vue'
```

### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| fallback | String\|Object | 默认错误界面 | 错误时显示的内容 |
| onError | Function | null | 错误回调函数 |

### Slots

| 名称 | 说明 |
|------|------|
| default | 需要保护的内容 |
| fallback | 自定义错误界面（可选） |

### 使用示例

#### 基础用法

```vue
<template>
  <ErrorBoundary>
    <ComplexComponent />
  </ErrorBoundary>
</template>

<script setup>
import ErrorBoundary from '@/components/Base/ErrorBoundary.vue'
import ComplexComponent from './ComplexComponent.vue'
</script>
```

#### 自定义错误界面

```vue
<template>
  <ErrorBoundary>
    <template #fallback>
      <div class="custom-error">
        <span class="error-icon">⚠️</span>
        <p>组件加载失败，请刷新页面</p>
        <button @click="retry">重试</button>
      </div>
    </template>
    
    <ComplexComponent />
  </ErrorBoundary>
</template>

<script setup>
import { ref } from 'vue'
import ErrorBoundary from '@/components/Base/ErrorBoundary.vue'

const errorRef = ref(null)

const retry = () => {
  errorRef.value?.reset()
}
</script>
```

#### 错误回调

```vue
<template>
  <ErrorBoundary :on-error="handleError">
    <ComplexComponent />
  </ErrorBoundary>
</template>

<script setup>
import ErrorBoundary from '@/components/Base/ErrorBoundary.vue'
import { error } from '@/utils/logger'

const handleError = (err, vm, info) => {
  // 上报错误到监控服务
  error('ErrorBoundary', '组件错误', {
    error: err,
    component: vm,
    info
  })
}
</script>
```

---

## 使用示例

### 完整的聊天输入区域

```vue
<template>
  <div class="chat-input-area">
    <!-- 工具栏 -->
    <InputToolbar
      :show-emoji-picker="showEmojiPicker"
      @toggle-emoji="toggleEmojiPicker"
      @upload-image="handleUploadImage"
      @upload-file="handleUploadFile"
    />
    
    <!-- 输入框 -->
    <textarea
      v-model="message"
      placeholder="输入消息..."
      @keydown="handleKeydown"
    />
    
    <!-- 快捷表情栏 -->
    <QuickEmojiBar
      @select="insertEmoji"
      @toggle-picker="toggleEmojiPicker"
    />
    
    <!-- 发送按钮 -->
    <button
      :disabled="!canSend"
      @click="handleSend"
    >
      发送
    </button>
    
    <!-- 表情选择器 -->
    <EmojiPicker
      v-if="showEmojiPicker"
      @select="insertEmoji"
      @close="showEmojiPicker = false"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import InputToolbar from '@/components/Chat/InputToolbar.vue'
import QuickEmojiBar from '@/components/Chat/QuickEmojiBar.vue'
import EmojiPicker from '@/components/Chat/EmojiPicker.vue'

const message = ref('')
const showEmojiPicker = ref(false)

const canSend = computed(() => {
  return message.value.trim().length > 0
})

const insertEmoji = (emoji) => {
  message.value += emoji
}

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const handleSend = () => {
  if (!canSend.value) return
  // 发送消息
  console.log('发送:', message.value)
  message.value = ''
}
</script>

<style scoped lang="scss">
.chat-input-area {
  display: flex;
  flex-direction: column;
  gap: var(--dt-space-2);
  padding: var(--dt-space-4);
  background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-light);
}

textarea {
  min-height: 80px;
  padding: var(--dt-space-2);
  border: 1px solid var(--dt-border-input);
  border-radius: var(--dt-radius-sm);
  resize: vertical;
  
  &:focus {
    border-color: var(--dt-brand-color);
    outline: none;
  }
}
</style>
```

### 侧边栏布局示例

```vue
<template>
  <div class="sidebar-layout">
    <!-- 侧边导航 -->
    <ImSideNav
      :active-module="activeModule"
      @switch-module="handleSwitchModule"
    />
    
    <!-- 会话列表 -->
    <SessionPanel>
      <!-- 顶部快捷操作栏 -->
      <QuickActionsBar
        @create-group="handleCreateGroup"
        @add-friend="handleAddFriend"
      />
    </SessionPanel>
    
    <!-- 聊天面板 -->
    <ChatPanel :session="currentSession" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ImSideNav from '@/components/ImSideNav/index.vue'
import SessionPanel from '@/views/SessionPanel.vue'
import ChatPanel from '@/views/ChatPanel.vue'

const activeModule = ref('chat')
const currentSession = ref(null)

const handleSwitchModule = (module) => {
  activeModule.value = module
}

const handleCreateGroup = () => {
  // 打开创建群组对话框
  console.log('创建群组')
}

const handleAddFriend = () => {
  // 打开添加好友对话框
  console.log('添加好友')
}
</script>

<style scoped lang="scss">
.sidebar-layout {
  display: flex;
  height: 100vh;
  
  :deep(.im-side-nav) {
    width: 60px;
    flex-shrink: 0;
  }
  
  :deep(.session-panel) {
    width: 260px;
    flex-shrink: 0;
  }
  
  :deep(.chat-panel) {
    flex: 1;
    min-width: 0;
  }
}
</style>
```

---

## 最佳实践

### 1. 错误处理

```vue
<!-- ✅ 推荐：使用错误边界保护重要组件 -->
<ErrorBoundary>
  <CriticalComponent />
</ErrorBoundary>

<!-- ❌ 不推荐：没有错误处理 -->
<CriticalComponent />
```

### 2. 性能优化

```vue
<!-- ✅ 推荐：使用 v-memo 优化列表渲染 -->
<div
  v-for="item in list"
  :key="item.id"
  v-memo="[item.id, item.updatedAt]"
>
  {{ item.content }}
</div>

<!-- ❌ 不推荐：每次都重新渲染 -->
<div
  v-for="item in list"
  :key="item.id"
>
  {{ item.content }}
</div>
```

### 3. 可访问性

```vue
<!-- ✅ 推荐：添加 ARIA 标签 -->
<button
  @click="handleSearch"
  aria-label="全局搜索"
  :aria-expanded="isSearchOpen"
>
  <SearchIcon />
</button>

<!-- ❌ 不推荐：缺少无障碍支持 -->
<button @click="handleSearch">
  <SearchIcon />
</button>
```

### 4. 响应式设计

```scss
// ✅ 推荐：使用 CSS 变量
.container {
  padding: var(--dt-space-4);
  
  @media (max-width: 768px) {
    padding: var(--dt-space-2);
  }
}

// ❌ 不推荐：硬编码值
.container {
  padding: 16px;
  
  @media (max-width: 768px) {
    padding: 8px;
  }
}
```

---

## 常见问题

### Q1: 如何自定义快捷操作栏的按钮？

A: 目前不支持自定义按钮，但可以通过 CSS 隐藏不需要的按钮：

```scss
.quick-action-btn:nth-child(3) {
  display: none;
}
```

### Q2: 快捷表情栏可以添加自定义表情吗？

A: 可以，通过修改组件的 `quickEmojis` 数组：

```javascript
// 在 QuickEmojiBar.vue 中修改
const quickEmojis = ref(['👍', '👏', '❤️', '😂', '😊', '🎉', '✅', '🙏', '🚀', '✨'])
```

### Q3: 错误边界能捕获所有错误吗？

A: 错误边界只能捕获子组件树中的 JavaScript 错误，不能捕获：
- 事件处理器中的错误
- 异步代码（setTimeout、requestAnimationFrame）
- 服务端渲染错误
- 自身抛出的错误

---

## 更新日志

### v1.0.0 (2026-03-03)

- ✨ 新增 QuickActionsBar 组件
- ✨ 新增 QuickEmojiBar 组件
- ✨ 新增 ErrorBoundary 组件
- 📝 完善使用文档
- 🎨 统一样式规范

---

**文档维护**: AI Assistant  
**最后更新**: 2026-03-03  
**下次审查**: 2026-04-03
