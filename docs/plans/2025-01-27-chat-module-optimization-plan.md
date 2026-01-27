# 聊天模块优化实施计划

**文档版本**：v1.0
**创建日期**：2026年1月27日
**目标**：快速完善 P0 核心功能，对齐钉钉基础体验
**开发资源**：1人开发
**预计完成时间**：1.5-2.5 天

---

## 📋 目录

1. [项目背景](#项目背景)
2. [优先级矩阵](#优先级矩阵)
3. [功能实施计划](#功能实施计划)
4. [技术方案详解](#技术方案详解)
5. [开发时间估算](#开发时间估算)
6. [实施步骤](#实施步骤)
7. [验收标准](#验收标准)
8. [风险与应对](#风险与应对)

---

## 项目背景

### 当前状态

RuoYi-IM 项目整体完成度约 94%，但聊天模块仍有关键细节需要完善，用户从钉钉切换到该系统时会感到体验缺失。

### 核心问题

用户最不能接受的 3 个体验缺失：
1. **聊天模块** - 消息发送状态、图片预览、多选转发等
2. **群组模块** - 群文件、群公告等
3. **OA 工作台** - 待办事项、审批流程等

### 优化目标

聚焦聊天模块，快速完善核心功能，提升用户体验至钉钉基础水平。

---

## 优先级矩阵

### 评估维度

- **用户抱怨程度**（5分 = 最频繁抱怨）
- **实现难度**（1分 = 最简单，5分 = 最复杂）
- **开发时间**（1分 = 最快，5分 = 最慢）
- **对上线的影响**（5分 = 必须有才能上线）

### 功能优先级排序

| 排名 | 功能 | 得分 | 批次 | 开发时间 |
|------|------|------|------|---------|
| 🥇 1 | 图片全屏预览 + 左右切换 | 16/20 | 第一批 | 3-4 小时 |
| 🥈 2 | 消息多选 + 合并转发 | 15/20 | 第一批 | 4-6 小时 |
| 🥉 3 | 消息发送状态 | 14/20 | 第二批 | 1-2 小时 |
| 4 | 失败消息重发 | 14/20 | 第二批 | 2-3 小时 |
| 5 | 图片拖拽上传 | 11/20 | 第三批 | 1-2 小时 |
| 6 | 粘贴图片上传 | 10/20 | 第三批 | 1 小时 |
| 7 | 语音消息 | 10/20 | 第四批（延后） | 6-8 小时 |

---

## 功能实施计划

### 第一批（必须完成）

#### 功能 1：图片全屏预览 + 左右切换

**需求描述：**
用户点击聊天中的图片，能够全屏查看，并支持左右切换查看其他图片。

**技术方案：**
- 使用第三方库 `v-viewer`
- 支持预览、缩放、旋转、左右切换

**实施步骤：**
1. 安装依赖：`npm install v-viewer`
2. 在 `main.js` 中引入并注册
3. 修改聊天组件，集成 `v-viewer`
4. 测试预览、缩放、切换功能

**涉及文件：**
- `ruoyi-im-web/src/main.js`
- `ruoyi-im-web/src/views/ChatPanel.vue`
- `ruoyi-im-web/src/components/Chat/MessageBubble.vue`

**预计时间：** 3-4 小时

---

#### 功能 2：消息多选 + 合并转发

**需求描述：**
用户可以选择多条消息，合并转发到其他会话。

**技术方案：**
- 键盘快捷键多选（Ctrl + 点击 / Shift + 点击）
- 选中消息高亮显示
- 工具栏显示"转发"按钮和已选数量
- 右键菜单添加"转发"选项

**实施步骤：**
1. 在消息组件中添加点击事件监听
2. 检测 Ctrl/Shift 按键状态
3. 维护选中消息列表（Set 数据结构）
4. 添加选中消息高亮样式
5. 工具栏动态显示"转发"按钮
6. 右键菜单添加"转发"选项
7. 实现会话选择器
8. 调用后端合并转发 API

**涉及文件：**
- `ruoyi-im-web/src/views/ChatPanel.vue`
- `ruoyi-im-web/src/components/Chat/MessageBubble.vue`
- `ruoyi-im-web/src/components/ForwardDialog.vue`（新建）
- `ruoyi-im-web/src/api/im/message.js`

**预计时间：** 4-6 小时

---

### 第二批（重要功能）

#### 功能 3：消息发送状态

**需求描述：**
实时显示消息发送状态（发送中/已发送/发送失败）。

**技术方案：**
- 后端已有状态字段（`status`）
- 前端根据状态显示不同图标
- 失败消息显示红色感叹号和重发选项

**实施步骤：**
1. 检查后端消息状态字段
2. 在消息组件中添加状态图标
3. 定义不同状态的图标样式
4. 添加失败消息的重发按钮

**涉及文件：**
- `ruoyi-im-web/src/components/Chat/MessageBubble.vue`
- `ruoyi-im-web/src/api/im/message.js`

**预计时间：** 1-2 小时

---

#### 功能 4：失败消息重发

**需求描述：**
发送失败的消息可以重新发送。

**技术方案：**
- 点击失败消息 → 弹出"重发"选项
- 调用原发送接口
- 更新消息状态

**实施步骤：**
1. 在失败消息上添加重发按钮
2. 实现重发逻辑
3. 调用发送 API
4. 更新消息状态

**涉及文件：**
- `ruoyi-im-web/src/components/Chat/MessageBubble.vue`
- `ruoyi-im-web/src/api/im/message.js`

**预计时间：** 2-3 小时

---

### 第三批（锦上添花）

#### 功能 5：图片拖拽上传

**需求描述：**
用户可以拖拽图片到聊天区域上传。

**技术方案：**
- 监听 `dragover` / `drop` 事件
- 拖拽区域高亮提示
- 调用现有上传接口

**实施步骤：**
1. 在聊天输入区域添加拖拽事件监听
2. 实现拖拽区域高亮样式
3. 处理 `drop` 事件，获取文件
4. 调用上传 API
5. 发送图片消息

**涉及文件：**
- `ruoyi-im-web/src/views/ChatPanel.vue`
- `ruoyi-im-web/src/components/Chat/ChatInput.vue`

**预计时间：** 1-2 小时

---

#### 功能 6：粘贴图片上传

**需求描述：**
用户可以粘贴剪贴板中的图片到聊天区域。

**技术方案：**
- 监听 `paste` 事件
- 检测剪贴板中的图片
- 调用现有上传接口

**实施步骤：**
1. 在聊天输入区域添加粘贴事件监听
2. 检测剪贴板内容类型
3. 提取图片数据
4. 调用上传 API
5. 发送图片消息

**涉及文件：**
- `ruoyi-im-web/src/views/ChatPanel.vue`
- `ruoyi-im-web/src/components/Chat/ChatInput.vue`

**预计时间：** 1 小时

---

### 第四批（可延后）

#### 功能 7：语音消息

**需求描述：**
用户可以录制语音消息并播放。

**技术方案：**
- 使用 Web Audio API 录音
- 显示音频波形
- 支持播放、暂停、进度拖拽

**实施步骤：**
1. 实现录音功能
2. 实现波形显示
3. 实现播放控制
4. 集成到聊天组件

**涉及文件：**
- `ruoyi-im-web/src/components/Chat/VoiceRecorder.vue`（新建）
- `ruoyi-im-web/src/components/Chat/VoicePlayer.vue`（新建）
- `ruoyi-im-web/src/views/ChatPanel.vue`

**预计时间：** 6-8 小时

**建议：** 上线后再加

---

## 技术方案详解

### 功能 1：图片全屏预览（v-viewer）

#### 1.1 安装依赖

```bash
npm install v-viewer
```

#### 1.2 在 main.js 中引入

```javascript
// ruoyi-im-web/src/main.js
import { createApp } from 'vue'
import App from './App.vue'
import 'viewerjs/dist/viewer.css'
import VueViewer from 'v-viewer'

const app = createApp(App)
app.use(VueViewer)
app.mount('#app')
```

#### 1.3 在聊天组件中使用

```vue
<!-- ruoyi-im-web/src/components/Chat/MessageBubble.vue -->
<template>
  <div class="message-bubble">
    <viewer :images="imageList" @inited="inited">
      <template #default="viewer">
        <img
          v-for="src in imageList"
          :key="src"
          :src="src"
          class="message-image"
          @click="viewer.view(index)"
        />
      </template>
    </viewer>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const imageList = ref([])
const viewer = ref(null)

const inited = (viewerInstance) => {
  viewer.value = viewerInstance
}
</script>

<style scoped>
.message-image {
  max-width: 200px;
  max-height: 200px;
  cursor: pointer;
  border-radius: 4px;
}
</style>
```

---

### 功能 2：消息多选 + 合并转发

#### 2.1 消息多选逻辑

```vue
<!-- ruoyi-im-web/src/components/Chat/MessageBubble.vue -->
<template>
  <div
    class="message-bubble"
    :class="{ selected: isSelected }"
    @click="handleClick"
  >
    <!-- 消息内容 -->
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'

const props = defineProps({
  message: Object
})

const store = useStore()
const selectedMessages = computed(() => store.state.chat.selectedMessages)

const isSelected = computed(() => selectedMessages.value.has(props.message.id))

const handleClick = (event) => {
  if (event.ctrlKey || event.metaKey) {
    // Ctrl + 点击：不连续多选
    toggleSelection()
  } else if (event.shiftKey) {
    // Shift + 点击：连续多选
    rangeSelection()
  } else {
    // 普通点击：清空选择
    clearSelection()
  }
}

const toggleSelection = () => {
  store.commit('chat/toggleMessageSelection', props.message.id)
}

const rangeSelection = () => {
  store.commit('chat/rangeMessageSelection', props.message.id)
}

const clearSelection = () => {
  store.commit('chat/clearMessageSelection')
}
</script>

<style scoped>
.message-bubble.selected {
  border: 2px solid #1890ff;
  background-color: #e6f7ff;
}
</style>
```

#### 2.2 Vuex Store 状态管理

```javascript
// ruoyi-im-web/src/store/modules/chat.js
export default {
  namespaced: true,
  state: {
    selectedMessages: new Set()
  },
  mutations: {
    toggleMessageSelection(state, messageId) {
      if (state.selectedMessages.has(messageId)) {
        state.selectedMessages.delete(messageId)
      } else {
        state.selectedMessages.add(messageId)
      }
      state.selectedMessages = new Set(state.selectedMessages)
    },
    rangeMessageSelection(state, messageId) {
      // 实现连续选择逻辑
      // ...
    },
    clearMessageSelection(state) {
      state.selectedMessages = new Set()
    }
  },
  getters: {
    selectedMessageCount: (state) => state.selectedMessages.size,
    selectedMessageList: (state, getters, rootState) => {
      return Array.from(state.selectedMessages).map(id => {
        return rootState.chat.messages.find(msg => msg.id === id)
      })
    }
  }
}
```

#### 2.3 工具栏转发按钮

```vue
<!-- ruoyi-im-web/src/views/ChatPanel.vue -->
<template>
  <div class="chat-panel">
    <!-- 消息列表 -->
    <div class="message-list">
      <!-- ... -->
    </div>

    <!-- 工具栏 -->
    <div v-if="selectedMessageCount > 0" class="selection-toolbar">
      <span>已选 {{ selectedMessageCount }} 条消息</span>
      <el-button type="primary" @click="showForwardDialog">
        转发
      </el-button>
      <el-button @click="clearSelection">
        取消
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()

const selectedMessageCount = computed(() => store.getters['chat/selectedMessageCount'])

const clearSelection = () => {
  store.commit('chat/clearMessageSelection')
}

const showForwardDialog = () => {
  // 打开转发对话框
}
</script>

<style scoped>
.selection-toolbar {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  padding: 10px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 1000;
}
</style>
```

#### 2.4 转发对话框

```vue
<!-- ruoyi-im-web/src/components/ForwardDialog.vue -->
<template>
  <el-dialog
    v-model="visible"
    title="选择转发到..."
    width="500px"
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="联系人" name="contacts">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索联系人"
          prefix-icon="Search"
        />
        <div class="contact-list">
          <div
            v-for="contact in filteredContacts"
            :key="contact.id"
            class="contact-item"
            @click="selectContact(contact)"
          >
            <el-avatar :size="40" :src="contact.avatar" />
            <span>{{ contact.nickname }}</span>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="群组" name="groups">
        <div class="group-list">
          <div
            v-for="group in groups"
            :key="group.id"
            class="group-item"
            @click="selectGroup(group)"
          >
            <el-avatar :size="40" :src="group.avatar" />
            <span>{{ group.name }}</span>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="confirmForward" :disabled="!selectedTarget">
        确认转发
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { forwardMessages } from '@/api/im/message'

const store = useStore()
const visible = ref(false)
const activeTab = ref('contacts')
const searchKeyword = ref('')
const selectedTarget = ref(null)

const contacts = computed(() => store.state.contact.contacts)
const groups = computed(() => store.state.group.groups)

const filteredContacts = computed(() => {
  if (!searchKeyword.value) return contacts.value
  return contacts.value.filter(contact =>
    contact.nickname.includes(searchKeyword.value)
  )
})

const selectContact = (contact) => {
  selectedTarget.value = { type: 'user', ...contact }
}

const selectGroup = (group) => {
  selectedTarget.value = { type: 'group', ...group }
}

const confirmForward = async () => {
  const selectedMessages = store.getters['chat/selectedMessageList']
  const messageIds = selectedMessages.map(msg => msg.id)

  await forwardMessages({
    targetType: selectedTarget.value.type,
    targetId: selectedTarget.value.id,
    messageIds
  })

  visible.value = false
  store.commit('chat/clearMessageSelection')
  ElMessage.success('转发成功')
}

const open = () => {
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped>
.contact-list,
.group-list {
  max-height: 400px;
  overflow-y: auto;
  margin-top: 10px;
}

.contact-item,
.group-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  cursor: pointer;
  border-radius: 4px;
}

.contact-item:hover,
.group-item:hover {
  background-color: #f5f5f5;
}
</style>
```

---

### 功能 3：消息发送状态

#### 3.1 消息状态图标

```vue
<!-- ruoyi-im-web/src/components/Chat/MessageBubble.vue -->
<template>
  <div class="message-bubble">
    <div class="message-content">
      <!-- 消息内容 -->
    </div>
    <div class="message-status">
      <el-icon v-if="message.status === 'sending'" class="is-loading">
        <Loading />
      </el-icon>
      <el-icon v-else-if="message.status === 'sent'" color="#909399">
        <Check />
      </el-icon>
      <el-icon v-else-if="message.status === 'read'" color="#909399">
        <Check />
        <Check />
      </el-icon>
      <el-icon v-else-if="message.status === 'failed'" color="#f56c6c">
        <WarningFilled />
      </el-icon>
    </div>
  </div>
</template>

<script setup>
import { Loading, Check, WarningFilled } from '@element-plus/icons-vue'

const props = defineProps({
  message: Object
})
</script>

<style scoped>
.message-status {
  display: flex;
  align-items: center;
  margin-left: 5px;
  font-size: 14px;
}
</style>
```

---

### 功能 4：失败消息重发

#### 4.1 重发按钮

```vue
<!-- ruoyi-im-web/src/components/Chat/MessageBubble.vue -->
<template>
  <div class="message-bubble">
    <div class="message-content">
      <!-- 消息内容 -->
    </div>
    <div class="message-status">
      <!-- 状态图标 -->
    </div>
    <el-button
      v-if="message.status === 'failed'"
      type="danger"
      size="small"
      @click="handleResend"
    >
      重发
    </el-button>
  </div>
</template>

<script setup>
import { resendMessage } from '@/api/im/message'

const props = defineProps({
  message: Object
})

const handleResend = async () => {
  try {
    await resendMessage(props.message.id)
    ElMessage.success('重发成功')
  } catch (error) {
    ElMessage.error('重发失败')
  }
}
</script>
```

---

### 功能 5：图片拖拽上传

#### 5.1 拖拽事件监听

```vue
<!-- ruoyi-im-web/src/views/ChatPanel.vue -->
<template>
  <div
    class="chat-panel"
    @dragover.prevent="handleDragOver"
    @dragleave.prevent="handleDragLeave"
    @drop.prevent="handleDrop"
    :class="{ 'drag-over': isDragging }"
  >
    <!-- 聊天内容 -->
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { uploadFile } from '@/api/file'
import { sendMessage } from '@/api/im/message'

const isDragging = ref(false)

const handleDragOver = (event) => {
  isDragging.value = true
}

const handleDragLeave = (event) => {
  isDragging.value = false
}

const handleDrop = async (event) => {
  isDragging.value = false

  const files = event.dataTransfer.files
  if (files.length === 0) return

  for (const file of files) {
    if (file.type.startsWith('image/')) {
      await uploadImage(file)
    }
  }
}

const uploadImage = async (file) => {
  try {
    const { url } = await uploadFile(file)
    await sendMessage({
      type: 'image',
      content: url
    })
  } catch (error) {
    ElMessage.error('上传失败')
  }
}
</script>

<style scoped>
.chat-panel.drag-over {
  background-color: #e6f7ff;
  border: 2px dashed #1890ff;
}
</style>
```

---

### 功能 6：粘贴图片上传

#### 6.1 粘贴事件监听

```vue
<!-- ruoyi-im-web/src/views/ChatPanel.vue -->
<template>
  <div class="chat-panel" @paste="handlePaste">
    <!-- 聊天内容 -->
  </div>
</template>

<script setup>
const handlePaste = async (event) => {
  const items = event.clipboardData.items

  for (const item of items) {
    if (item.type.startsWith('image/')) {
      const file = item.getAsFile()
      await uploadImage(file)
    }
  }
}
</script>
```

---

## 开发时间估算

### 总体时间表

| 功能 | 开发时间 | 测试时间 | 总计 |
|------|---------|---------|------|
| 图片全屏预览 | 3-4 小时 | 1 小时 | 4-5 小时 |
| 消息多选转发 | 4-6 小时 | 2 小时 | 6-8 小时 |
| 消息发送状态 | 1-2 小时 | 0.5 小时 | 1.5-2.5 小时 |
| 失败消息重发 | 2-3 小时 | 1 小时 | 3-4 小时 |
| 图片拖拽上传 | 1-2 小时 | 0.5 小时 | 1.5-2.5 小时 |
| 粘贴图片上传 | 1 小时 | 0.5 小时 | 1.5 小时 |
| **总计** | **12-18 小时** | **5.5 小时** | **17.5-23.5 小时** |

### 按批次估算

| 批次 | 功能 | 开发时间 | 测试时间 | 总计 |
|------|------|---------|---------|------|
| 第一批 | 图片预览 + 多选转发 | 7-10 小时 | 3 小时 | 10-13 小时 |
| 第二批 | 发送状态 + 重发 | 3-5 小时 | 1.5 小时 | 4.5-6.5 小时 |
| 第三批 | 拖拽上传 + 粘贴上传 | 2-3 小时 | 1 小时 | 3-4 小时 |
| **总计** | | **12-18 小时** | **5.5 小时** | **17.5-23.5 小时** |

### 建议开发节奏

**Day 1（8 小时）：**
- 上午（4 小时）：图片全屏预览
- 下午（4 小时）：消息多选转发（完成 80%）

**Day 2（8 小时）：**
- 上午（2 小时）：完成消息多选转发
- 上午（2 小时）：消息发送状态
- 下午（2 小时）：失败消息重发
- 下午（2 小时）：拖拽上传 + 粘贴上传

**Day 3（可选，4 小时）：**
- 全面测试
- Bug 修复
- 优化调整

---

## 实施步骤

### 第一步：环境准备（0.5 小时）

1. 确保开发环境正常运行
2. 拉取最新代码
3. 创建功能分支：`feature/chat-module-optimization`

```bash
git checkout -b feature/chat-module-optimization
```

---

### 第二步：第一批功能（10-13 小时）

#### 2.1 图片全屏预览（4-5 小时）

1. **安装依赖**（0.5 小时）
   ```bash
   npm install v-viewer
   ```

2. **集成到 main.js**（0.5 小时）
   - 引入 `v-viewer`
   - 注册全局组件

3. **修改聊天组件**（2 小时）
   - 在 `MessageBubble.vue` 中集成
   - 测试预览功能

4. **测试与优化**（1 小时）
   - 测试预览、缩放、旋转
   - 测试左右切换
   - 优化样式

---

#### 2.2 消息多选转发（6-8 小时）

1. **实现多选逻辑**（2 小时）
   - 修改 `MessageBubble.vue`
   - 添加点击事件监听
   - 实现 Ctrl/Shift 选择

2. **状态管理**（1.5 小时）
   - 修改 Vuex Store
   - 添加选中消息状态
   - 实现选择/取消/清空逻辑

3. **工具栏**（1 小时）
   - 添加选择工具栏
   - 显示已选数量
   - 添加转发/取消按钮

4. **转发对话框**（2 小时）
   - 创建 `ForwardDialog.vue`
   - 实现联系人/群组选择
   - 调用转发 API

5. **测试**（1.5 小时）
   - 测试多选功能
   - 测试转发功能
   - 测试边界情况

---

### 第三步：第二批功能（4.5-6.5 小时）

#### 3.1 消息发送状态（1.5-2.5 小时）

1. **检查后端接口**（0.5 小时）
   - 确认消息状态字段
   - 确认状态值含义

2. **添加状态图标**（1 小时）
   - 修改 `MessageBubble.vue`
   - 添加不同状态的图标
   - 定义样式

3. **测试**（0.5 小时）
   - 测试不同状态的显示
   - 测试状态更新

---

#### 3.2 失败消息重发（3-4 小时）

1. **实现重发按钮**（1 小时）
   - 在失败消息上添加按钮
   - 添加点击事件

2. **实现重发逻辑**（1.5 小时）
   - 调用重发 API
   - 更新消息状态
   - 处理错误

3. **测试**（0.5-1 小时）
   - 测试重发功能
   - 测试网络异常情况

---

### 第四步：第三批功能（3-4 小时）

#### 4.1 图片拖拽上传（1.5-2.5 小时）

1. **添加拖拽事件**（1 小时）
   - 监听 `dragover` / `drop`
   - 实现拖拽区域高亮

2. **处理文件上传**（0.5 小时）
   - 获取拖拽文件
   - 调用上传 API
   - 发送消息

3. **测试**（0.5 小时）
   - 测试拖拽上传
   - 测试多文件上传

---

#### 4.2 粘贴图片上传（1.5 小时）

1. **添加粘贴事件**（0.5 小时）
   - 监听 `paste` 事件
   - 检测剪贴板图片

2. **处理上传**（0.5 小时）
   - 提取图片数据
   - 调用上传 API
   - 发送消息

3. **测试**（0.5 小时）
   - 测试粘贴上传
   - 测试截图粘贴

---

### 第五步：全面测试（4 小时）

1. **功能测试**（2 小时）
   - 测试所有新功能
   - 测试边界情况
   - 测试异常情况

2. **兼容性测试**（1 小时）
   - 测试不同浏览器
   - 测试不同屏幕尺寸

3. **性能测试**（0.5 小时）
   - 测试大量消息时的性能
   - 测试大图片上传

4. **Bug 修复**（0.5 小时）
   - 修复发现的问题
   - 优化用户体验

---

### 第六步：代码提交（0.5 小时）

1. **代码审查**
   - 检查代码质量
   - 检查注释完整性

2. **提交代码**
   ```bash
   git add .
   git commit -m "feat: 优化聊天模块功能

   - 添加图片全屏预览功能（支持左右切换）
   - 添加消息多选转发功能（支持 Ctrl/Shift 选择）
   - 添加消息发送状态显示
   - 添加失败消息重发功能
   - 添加图片拖拽上传功能
   - 添加粘贴图片上传功能"
   ```

3. **合并到主分支**
   ```bash
   git checkout main
   git merge feature/chat-module-optimization
   ```

---

## 验收标准

### 功能验收

#### 图片全屏预览
- ✅ 点击图片能够全屏预览
- ✅ 支持缩放（滚轮/按钮）
- ✅ 支持旋转
- ✅ 支持左右切换查看其他图片
- ✅ 支持关闭预览（ESC/点击背景）

#### 消息多选转发
- ✅ Ctrl + 点击消息能够不连续多选
- ✅ Shift + 点击消息能够连续多选
- ✅ 选中消息有高亮显示
- ✅ 工具栏显示已选数量
- ✅ 点击转发能够选择目标会话
- ✅ 转发成功后清空选择

#### 消息发送状态
- ✅ 发送中显示加载图标
- ✅ 发送成功显示对勾图标
- ✅ 已读显示双对勾图标
- ✅ 发送失败显示警告图标

#### 失败消息重发
- ✅ 失败消息显示重发按钮
- ✅ 点击重发能够重新发送
- ✅ 重发成功后更新状态
- ✅ 重发失败后显示错误提示

#### 图片拖拽上传
- ✅ 拖拽图片到聊天区域能够上传
- ✅ 拖拽区域有高亮提示
- ✅ 支持多文件拖拽
- ✅ 上传成功后自动发送消息

#### 粘贴图片上传
- ✅ 粘贴剪贴板图片能够上传
- ✅ 支持截图粘贴
- ✅ 上传成功后自动发送消息

---

### 性能验收

- ✅ 图片预览加载时间 < 1 秒
- ✅ 多选 100 条消息无卡顿
- ✅ 拖拽上传响应时间 < 0.5 秒
- ✅ 粘贴上传响应时间 < 0.5 秒

---

### 兼容性验收

- ✅ Chrome 浏览器正常
- ✅ Edge 浏览器正常
- ✅ Firefox 浏览器正常
- ✅ Safari 浏览器正常

---

### 代码质量验收

- ✅ 代码符合项目规范
- ✅ 没有代码警告
- ✅ 没有代码错误
- ✅ 注释完整清晰

---

## 风险与应对

### 技术风险

#### 风险 1：v-viewer 与 Element Plus 冲突

**描述：**
`v-viewer` 可能与 Element Plus 的样式或组件冲突。

**应对：**
- 提前测试兼容性
- 如有冲突，考虑使用其他库（如 `vue-image-viewer`）
- 必要时自己实现预览功能

---

#### 风险 2：消息多选性能问题

**描述：**
选择大量消息时可能出现性能问题。

**应对：**
- 使用 Set 数据结构优化性能
- 限制最多选择数量（如 100 条）
- 使用虚拟滚动优化渲染

---

#### 风险 3：后端 API 不支持

**描述：**
部分功能可能需要后端支持，但后端 API 不完善。

**应对：**
- 提前确认后端 API
- 如不支持，先使用模拟数据
- 协调后端开发支持

---

### 进度风险

#### 风险 4：开发时间超预期

**描述：**
实际开发时间可能超过预估时间。

**应对：**
- 严格按照优先级实施
- 第一批功能必须完成
- 后续功能可根据时间调整
- 必要时延后第四批功能

---

#### 风险 5：测试时间不足

**描述：**
开发时间超预期导致测试时间不足。

**应对：**
- 边开发边测试
- 重点测试核心功能
- 上线后快速迭代修复 bug

---

### 质量风险

#### 风险 6：用户体验不达标

**描述：**
实现的功能用户体验不如预期。

**应对：**
- 参考主流 IM 的交互设计
- 收集用户反馈
- 快速迭代优化

---

## 总结

本计划聚焦聊天模块的核心功能优化，通过 1.5-2.5 天的开发时间，快速完善图片预览、消息多选转发、发送状态、重发、拖拽上传、粘贴上传等 6 个关键功能，显著提升用户体验，对齐钉钉基础水平。

### 关键成功因素

1. **严格按优先级实施**：第一批功能必须完成，后续功能可根据时间调整
2. **边开发边测试**：确保每个功能完成后立即测试，避免累积问题
3. **参考主流设计**：借鉴微信、钉钉等成熟 IM 的交互设计
4. **快速迭代**：上线后根据用户反馈快速优化

### 后续优化方向

1. **语音消息**：实现录音、播放功能
2. **消息搜索**：优化搜索体验
3. **消息撤回**：优化撤回功能的交互
4. **消息编辑**：优化编辑功能的交互
5. **表情包**：添加自定义表情包功能

---

**文档结束**