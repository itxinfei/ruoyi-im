---
title: 'RuoYi-IM 钉钉核心功能全面完善'
slug: 'ruoyi-im-dingtalk-core-enhancement'
created: '2026-01-27T04:45:33Z'
status: 'in-progress'
stepsCompleted: [1, 2, 3]
tech_stack:
  - Vue 3.3.11
  - Element Plus 2.4.4
  - Vite 5.0.7
  - Spring Boot 2.7.18
  - MyBatis-Plus 3.5.3
  - WebSocket
  - WebRTC (SimplePeer适配器)
  - v-viewer 3.0.11
files_to_modify:
  - ruoyi-im-web/src/components/Chat/MessageBubble.vue
  - ruoyi-im-web/src/components/Chat/MessageItem.vue
  - ruoyi-im-web/src/views/ChatPanel.vue
  - ruoyi-im-web/src/components/Chat/CallDialog.vue
  - ruoyi-im-web/src/store/modules/im/message.js
  - ruoyi-im-web/src/api/im/index.js
code_patterns:
  - 消息状态: sendStatus (PENDING/SENDING/DELIVERED/FAILED)
  - 消息类型: TEXT/IMAGE/FILE/VOICE/VIDEO/COMBINE/AUDIO
  - 乐观UI: 先显示消息，后通过WebSocket更新状态
  - 构造器注入: private final Service service
  - DTO隔离: @Valid @RequestBody XxxDTO
test_patterns:
  - 前端: Vitest + Vue Test Utils
  - 后端: JUnit 5 + Mockito
  - WebRTC: 需要两端联调测试
---

# Tech-Spec: RuoYi-IM 钉钉核心功能全面完善

**Created:** 2026-01-27

## Overview

### Problem Statement

当前 RuoYi-IM 系统完成度约 70%，与钉钉核心功能存在显著差距。主要问题包括：

1. **消息体验不完整**：缺少语音消息播放UI、链接卡片、发送状态反馈、失败重发机制
2. **交互功能缺失**：缺少图片全屏预览、拖拽上传、粘贴上传、长按多选
3. **群组功能不足**：缺少群文件共享、群投票、群二维码、群管理界面
4. **实时通讯缺失**：WebRTC语音/视频通话未集成、输入状态未同步
5. **协作功能空白**：快捷回复、消息置顶、聊天记录导出等功能缺失

### Solution

按照钉钉标准，分三个优先级阶段全面实现核心功能：

- **P0 阶段**：核心体验优化 - 消息状态管理、文件上传优化、预览功能
- **P1 阶段**：重要功能完善 - 多选模式、合并转发、群文件、实时通话
- **P2 阶段**：增强功能 - 链接卡片、表情包、协作工具

### Scope

**In Scope:**

**P0 - 核心体验优化**
1. 消息发送状态系统（sending/success/failed 状态）
2. 失败消息重发机制（一键重发）
3. 图片全屏预览组件（左右切换、缩放旋转）
4. 拖拽上传增强（支持文件拖拽到聊天区域）
5. 粘贴图片上传（Ctrl+V 粘贴板图片自动上传）

**P1 - 重要功能完善**
1. 消息多选模式优化（长按触发多选、批量操作工具栏）
2. 合并转发功能完善（合并转发消息展示、点击展开）
3. 群文件共享空间（文件库、分类管理、在线预览）
4. WebRTC 语音通话集成（1v1语音通话）
5. WebRTC 视频通话集成（1v1视频通话）
6. 输入状态同步（"对方正在输入..."提示）

**P2 - 增强功能**
1. 链接卡片自动识别（URL解析、标题摘要获取）
2. 自定义表情包管理（上传、收藏、使用）
3. 消息置顶功能（会话置顶、消息置顶）
4. 快捷回复功能（预设回复、管理快捷短语）
5. 聊天记录导出（PDF/HTML/Excel格式）

**Out of Scope:**
- 第三方应用集成
- 企业级定制功能
- AI 智能功能（已有独立 AI 助手）
- 审批流程（已有独立模块）
- 邮件功能（已有独立模块）

## Context for Development

### Codebase Patterns

**前端架构模式：**
- 使用 Vue 3 Composition API
- 组件化开发（Chat、Contacts、Workbench 等模块）
- Vuex 状态管理（im 模块：session、message、user、contact、group）
- WebSocket 实时通信（useImWebSocket composable）

**后端架构模式：**
- Spring Boot + MyBatis-Plus
- 分层架构：Controller → Service → Mapper
- 构造器注入（禁止 @Autowired）
- @Transactional 事务控制
- DTO/VO 数据隔离

**消息处理模式：**
- 消息类型：TEXT/IMAGE/FILE/VOICE/VIDEO/COMBINE/AUDIO
- content 字段：文本直接存储，媒体类型用 JSON 存储
- 乐观 UI：发送时先显示，后端返回后更新
- WebSocket 广播：消息通过 WebSocket 推送给其他会话成员

### Files to Reference

| 文件 | 说明 |
| --- | --- |
| `docs/钉钉聊天功能差距分析.md` | 差距分析文档 |
| `ruoyi-im-web/src/views/ChatPanel.vue` | 聊天面板主组件（已有拖拽和粘贴上传） |
| `ruoyi-im-web/src/components/Chat/MessageInput.vue` | 输入框组件（已有语音模式） |
| `ruoyi-im-web/src/components/Chat/MessageBubble.vue` | 消息气泡组件（已有v-viewer集成） |
| `ruoyi-im-web/src/components/Chat/MessageItem.vue` | 消息条目组件 |
| `ruoyi-im-web/src/components/Chat/CallDialog.vue` | WebRTC通话组件（已有1v1实现） |
| `ruoyi-im-web/src/components/Chat/VoiceRecorder.vue` | 语音录制组件（已实现） |
| `ruoyi-im-web/src/store/modules/im/message.js` | 消息状态管理 |
| `ruoyi-im-web/src/composables/useImWebSocket.js` | WebSocket通信（已有信令处理） |
| `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImMessageController.java` | 消息控制器 |
| `ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImMessage.java` | 消息实体（有sendStatus字段） |
| `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImGroupFileController.java` | 群文件控制器（后端已完成） |
| `ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImGroupFile.java` | 群文件实体 |
| `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImVideoCallController.java` | 视频通话控制器（信令） |

### Investigation Findings

**P0-1 消息状态系统 - 后端已完成，前端待UI**
- ✅ `ImMessage.java` 已有 `sendStatus` 字段（PENDING/SENDING/DELIVERED/FAILED）
- ✅ `ImMessageVO` 已有 `sendStatus` 映射（0=sending, 1=sent, 2=delivered, 3=read, 4=failed）
- ⚠️ 前端 `MessageBubble.vue` 未显示发送状态图标
- ⚠️ 失败消息无重发按钮

**P0-3 图片预览 - v-viewer已集成**
- ✅ `package.json` 已包含 `v-viewer@3.0.11`
- ✅ `main.js` 已注册 VueViewer 指令
- ⚠️ `MessageBubble.vue` 图片需要添加 `preview` 功能

**P0-4 拖拽上传 - ChatPanel已支持**
- ✅ `ChatPanel.vue:915-960` 已实现 dragover/drop 监听
- ✅ 区分图片/文件类型调用对应 API

**P0-5 粘贴上传 - ChatPanel已支持**
- ✅ `ChatPanel.vue:962-1000` 已实现 paste 监听
- ✅ 检测 clipboardData 中的图片并上传

**P1-4/P1-5 WebRTC通话 - CallDialog已实现**
- ✅ `CallDialog.vue` 已有完整1v1视频通话
- ✅ SimplePeer适配器，STUN服务器配置（google public STUN）
- ✅ `useImWebSocket.js` 已处理 `incoming_call` 和 `webrtc_signal` 事件
- ⚠️ 缺少多人会议UI
- ⚠️ 数据库迁移待执行（im_video_call_participant表）

**P1-3 群文件 - 后端已完成，前端待开发**
- ✅ `ImGroupFileController.java` 完整CRUD API
- ✅ `im_group_file` 和 `im_file_asset` 表存在
- ❌ 缺少 `GroupFilePanel.vue` 组件
- ❌ 缺少文件预览、批量操作UI

### Technical Decisions

1. **消息状态UI可视化**：复用现有 `sendStatus` 字段，在 `MessageBubble.vue` 添加状态图标（Element Plus Icon）
2. **图片预览增强**：使用已有 `v-viewer` 指令，在图片元素添加 `preview` 和 `preview-src-list` 属性
3. **WebRTC多人通话**：基于现有 `CallDialog.vue` 扩展，自适应网格布局（2人上下，3-4人2x2，5-9人3x3）
4. **群文件组件**：新建 `GroupFilePanel.vue`，复用现有 `FileUpload` 和 `FilePreviewDialog` 组件
5. **输入状态同步**：扩展现有 WebSocket 消息类型，添加 `typing` 事件广播

### User Interaction Decisions (高级引导确认)

#### 核心交互决策
| 决策点 | 选择方案 |
|-------|---------|
| **失败消息重发** | 气泡内重发按钮 - 失败消息显示感叹号图标，点击弹出重发/删除选项 |
| **视频会议布局** | 自适应网格 - 2人上下，3-4人2x2网格，5-9人3x3网格，自动调整 |
| **群文件权限** | 角色权限 - 群主/管理员可删除任意文件，普通成员只能删除自己上传的 |
| **输入状态显示** | 多人列表 - 多人输入时显示「A、B、C正在输入...」，最多显示3人 |
| **链接卡片获取** | 发送时自动获取 - 发送消息时自动检测URL并异步获取预览 |
| **表情包存储** | 混合模式 - 既有系统表情，又有用户自定义表情，支持分享给好友 |
| **快捷回复触发** | 上方快捷栏 - 在输入框上方显示横向滚动的快捷短语按钮，点击直接发送 |
| **导出格式支持** | 全格式 - PDF、HTML、Excel、TXT 四种格式 |
| **消息撤回策略** | 2分钟内可撤回，撤回后显示「XX撤回了一条消息」 |
| **合并转发查看** | 新页面展开 - 点击合并消息后在新页面/弹窗查看完整聊天记录 |
| **文件预览方式** | 媒体内联预览 - 图片/视频在聊天中预览，其他文件下载后本地打开 |

#### 消息与交互决策
| 决策点 | 选择方案 |
|-------|---------|
| **多选触发方式** | 工具栏按钮 - 多选模式切换按钮在工具栏，点击后所有消息显示勾选框 |
| **语音时长限制** | 120秒限制 - 最长2分钟，适合详细表达 |
| **消息置顶数量** | 最多3条 - 全局限制3条置顶消息，按置顶时间排序 |
| **搜索排序方式** | 混合排序 - 先按相关度分组，组内按时间排序 |
| **视频画质策略** | 自适应调整 - 根据网络状况自动调整，保证通话流畅性 |
| **屏幕共享显示** | 共享者可见 - 共享者可以看到自己的屏幕画面，其他人看到远程画面 |
| **快捷回复分组** | 固定分类 - 问候语、结束语、常用语、自定义分类，支持用户添加 |
| **文件上传限制** | 无限制提示 - 不限制大小，但提示大文件上传耗时长 |

#### 安全与边界决策
| 决策点 | 选择方案 |
|-------|---------|
| **敏感词过滤** | 发送后撤回 - 发送后服务端检测，发现后撤回消息并警告用户 |
| **离线消息接收** | 混合模式 - 少量离线消息推送，大量时主动拉取 |
| **多设备同步** | 30天自动同步 - 新设备登录时自动同步最近30天的聊天记录 |
| **群解散处理** | 保留记录 - 群解散后，所有成员保留聊天记录但无法继续发言 |

## Implementation Plan

### Tasks Summary

**状态说明：** ✅ 已完成 | ⚠️ 部分完成 | ❌ 待开发

| 优先级 | 任务 | 状态 | 说明 |
|-------|------|------|------|
| P0-1 | 消息发送状态UI | ⚠️ | 后端完成，前端需添加状态图标 |
| P0-2 | 失败消息重发 | ⚠️ | 需添加重发按钮和重试逻辑 |
| P0-3 | 图片全屏预览 | ⚠️ | v-viewer已集成，需添加preview功能 |
| P0-4 | 拖拽上传 | ✅ | ChatPanel已实现 |
| P0-5 | 粘贴上传 | ✅ | ChatPanel已实现 |
| P1-1 | 消息多选模式 | ⚠️ | 基础功能存在，需优化长按触发 |
| P1-2 | 合并转发展示 | ⚠️ | 后端完成，前端需COMBINE类型UI |
| P1-3 | 群文件组件 | ❌ | 后端完成，前端需新建GroupFilePanel |
| P1-4 | 语音通话 | ⚠️ | 已有视频通话，需语音模式UI |
| P1-5 | 视频通话 | ⚠️ | 1v1完成，需多人会议UI |
| P1-6 | 输入状态同步 | ❌ | 需添加typing事件处理 |
| P2-1 | 链接卡片 | ❌ | 需URL解析服务和LinkCard组件 |
| P2-2 | 自定义表情 | ❌ | 需表情包管理组件 |
| P2-3 | 消息置顶 | ⚠️ | 会话置顶已有，需消息级置顶 |
| P2-4 | 快捷回复 | ❌ | 需CRUD和UI组件 |
| P2-5 | 聊天记录导出 | ❌ | 需导出API和对话框 |

### Detailed Tasks

---
## P0 - 核心体验优化
---

#### P0-1: 消息发送状态UI可视化
**状态**: ⚠️ 后端完成，前端待UI

**文件修改**:
- `ruoyi-im-web/src/components/Chat/MessageBubble.vue`
- `ruoyi-im-web/src/store/modules/im/message.js`

**实施步骤**:

1. **MessageBubble.vue 添加状态图标**
```javascript
// 在 message-footer 区域添加状态图标
<template #status>
  <el-icon v-if="message.sendStatus === 0" class="is-loading">
    <Loading />
  </el-icon>
  <el-icon v-else-if="message.sendStatus === 1" class="status-icon sent">
    <Check />
  </el-icon>
  <el-icon v-else-if="message.sendStatus === 2" class="status-icon delivered">
    <CircleCheck />
  </el-icon>
  <el-icon v-else-if="message.sendStatus === 3" class="status-icon read">
    <CircleCheckFilled />
  </el-icon>
  <el-dropdown v-else-if="message.sendStatus === 4" trigger="click">
    <el-icon class="status-icon failed">
      <WarningFilled />
    </el-icon>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item @click="$emit('retry', message)">重新发送</el-dropdown-item>
        <el-dropdown-item @click="$emit('delete', message)">删除</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>
```

2. **样式定义**
```scss
.status-icon {
  font-size: 14px;
  &.sent { color: var(--el-text-color-secondary); }
  &.delivered { color: var(--el-color-primary); }
  &.read { color: var(--el-color-success); }
  &.failed { color: var(--el-color-danger); }
}
```

3. **WebSocket状态更新监听**
```javascript
// useImWebSocket.js 扩展消息处理
onMessage((data) => {
  if (data.type === 'message_status') {
    commit('im/message/UPDATE_MESSAGE_STATUS', {
      messageId: data.messageId,
      sendStatus: data.sendStatus
    })
  }
})
```

**验收标准**:
- 发送中显示旋转Loading图标
- 发送成功显示单勾，送达显示双勾，已读显示实心双勾
- 失败显示感叹号，点击弹出重发/删除菜单

---

#### P0-2: 失败消息重发机制
**状态**: ⚠️ 待实现

**文件修改**:
- `ruoyi-im-web/src/views/ChatPanel.vue`

**实施步骤**:

1. **添加重发函数**
```javascript
// ChatPanel.vue
const handleRetryMessage = async (message) => {
  // 更新状态为发送中
  store.commit('im/message/UPDATE_MESSAGE_STATUS', {
    messageId: message.id,
    sendStatus: 0
  })

  try {
    // 重新调用发送API
    await sendMessage({
      conversationId: message.conversationId,
      messageType: message.messageType,
      content: message.content
    })
  } catch (error) {
    // 失败后更新状态
    store.commit('im/message/UPDATE_MESSAGE_STATUS', {
      messageId: message.id,
      sendStatus: 4
    })
    ElMessage.error('重发失败，请检查网络后重试')
  }
}
```

2. **MessageBubble.vue 事件绑定**
```vue
<MessageBubble
  :message="message"
  @retry="handleRetryMessage"
  @delete="handleDeleteMessage"
/>
```

**验收标准**:
- 点击失败消息的感叹号图标弹出操作菜单
- 点击"重新发送"后消息状态变为发送中
- 重试失败后可再次重试，无次数限制

---

#### P0-3: 图片全屏预览增强
**状态**: ⚠️ v-viewer已集成，需添加preview功能

**文件修改**:
- `ruoyi-im-web/src/components/Chat/MessageBubble.vue`

**实施步骤**:

1. **图片添加preview指令**
```vue
<template>
  <div v-if="isImage(message)" class="message-image">
    <el-image
      :src="message.content.url"
      :preview-src-list="conversationImageList"
      :initial-index="imageIndex"
      fit="cover"
      :preview-teleported="true"
    />
  </div>
</template>

<script setup>
// 计算同会话所有图片URL列表
const conversationImageList = computed(() => {
  const messages = store.state.im.message.messages[currentConversationId.value] || []
  return messages
    .filter(m => m.messageType === 'IMAGE')
    .map(m => m.content.url)
})

// 当前图片在列表中的索引
const imageIndex = computed(() => {
  return conversationImageList.value.indexOf(props.message.content.url)
})
</script>
```

2. **键盘导航增强**
```javascript
// v-viewer已内置键盘支持
// 左右箭头切换，ESC关闭
// 可通过选项自定义
import 'v-viewer/style.css'
```

**验收标准**:
- 点击图片进入全屏预览模式
- 左右箭头切换同会话图片
- ESC键关闭预览
- 支持鼠标滚轮缩放

---
## P1 - 重要功能完善
---

#### P1-1: 消息多选模式优化
**状态**: ⚠️ 基础功能存在，需优化为工具栏按钮触发

**文件修改**:
- `ruoyi-im-web/src/views/ChatPanel.vue`
- `ruoyi-im-web/src/components/Chat/MessageItem.vue`

**实施步骤**:

1. **添加多选模式切换按钮**
```vue
<el-button
  :type="isMultiSelectMode ? 'primary' : 'default'"
  @click="toggleMultiSelectMode"
>
  <el-icon><Select /></el-icon>
  多选
</el-button>
```

2. **多选状态管理**
```javascript
const isMultiSelectMode = ref(false)
const selectedMessages = ref(new Set())

const toggleMultiSelectMode = () => {
  isMultiSelectMode.value = !isMultiSelectMode.value
  selectedMessages.value.clear()
}

const toggleMessageSelection = (messageId) => {
  if (selectedMessages.value.has(messageId)) {
    selectedMessages.value.delete(messageId)
  } else {
    selectedMessages.value.add(messageId)
  }
}
```

3. **批量操作工具栏**
```vue
<div v-if="isMultiSelectMode" class="multi-select-toolbar">
  <span>已选 {{ selectedMessages.size }} 条</span>
  <el-button @click="handleBatchForward">转发</el-button>
  <el-button @click="handleBatchDelete">删除</el-button>
  <el-button @click="handleBatchFavorite">收藏</el-button>
  <el-button @click="toggleMultiSelectMode">取消</el-button>
</div>
```

**验收标准**:
- 点击工具栏按钮进入多选模式
- 消息显示勾选框
- 批量操作按钮可用
- 点击取消退出多选模式

---

#### P1-2: 合并转发消息展示
**状态**: ⚠️ 后端完成，前端需COMBINE类型UI

**文件修改**:
- `ruoyi-im-web/src/components/Chat/MessageBubble.vue`
- `ruoyi-im-web/src/components/Chat/CombineMessagePreview.vue` (新建)

**实施步骤**:

1. **创建合并消息预览组件**
```vue
<!-- CombineMessagePreview.vue -->
<template>
  <div class="combine-message-preview" @click="showDetail">
    <div class="combine-header">
      <el-icon><ChatDotRound /></el-icon>
      <span>{{ content.title || '聊天记录' }}</span>
    </div>
    <div class="combine-summary">
      <div v-for="msg in content.messages.slice(0, 3)" :key="msg.id" class="summary-item">
        <span class="sender">{{ msg.senderName }}:</span>
        <span class="text">{{ getPreviewText(msg) }}</span>
      </div>
      <div v-if="content.messages.length > 3" class="more-hint">
        共 {{ content.messages.length }} 条聊天记录
      </div>
    </div>
  </div>
</template>
```

2. **详情查看对话框**
```vue
<el-dialog v-model="detailVisible" title="聊天记录" width="600px">
  <div class="combine-detail">
    <div v-for="msg in content.messages" :key="msg.id" class="detail-item">
      <div class="detail-header">
        <DingtalkAvatar :src="msg.senderAvatar" :size="32" />
        <span class="sender-name">{{ msg.senderName }}</span>
        <span class="send-time">{{ formatTime(msg.sendTime) }}</span>
      </div>
      <div class="detail-content">{{ msg.content }}</div>
    </div>
  </div>
</el-dialog>
```

**验收标准**:
- 合并消息显示为卡片样式
- 显示标题和前3条消息摘要
- 点击卡片在新弹窗查看完整记录
- 支持转发/收藏合并消息

---

#### P1-3: 群文件组件开发
**状态**: ❌ 后端完成，前端待开发

**新建文件**:
- `ruoyi-im-web/src/components/Chat/GroupFilePanel.vue`
- `ruoyi-im-web/src/api/im/groupFile.js`

**修改文件**:
- `ruoyi-im-web/src/views/ChatPanel.vue`

**实施步骤**:

1. **创建群文件API**
```javascript
// api/im/groupFile.js
export function getGroupFiles(groupId, params) {
  return request({
    url: `/api/im/group/${groupId}/files`,
    method: 'get',
    params
  })
}

export function uploadGroupFile(groupId, data) {
  return request({
    url: `/api/im/group/${groupId}/files`,
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteGroupFile(groupId, fileId) {
  return request({
    url: `/api/im/group/${groupId}/files/${fileId}`,
    method: 'delete'
  })
}
```

2. **创建群文件面板组件**
```vue
<template>
  <div class="group-file-panel">
    <div class="file-header">
      <el-input v-model="searchKeyword" placeholder="搜索文件" prefix-icon="Search" />
      <el-button type="primary" @click="handleUpload">
        <el-icon><Upload /></el-icon>
        上传
      </el-button>
    </div>

    <el-tabs v-model="activeCategory">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="图片" name="image" />
      <el-tab-pane label="文档" name="document" />
      <el-tab-pane label="视频" name="video" />
      <el-tab-pane label="其他" name="other" />
    </el-tabs>

    <el-table :data="filteredFiles" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="fileName" label="文件名">
        <template #default="{ row }">
          <div class="file-name">
            <el-icon><Document /></el-icon>
            <span>{{ row.fileName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="fileSize" label="大小" :formatter="formatSize" />
      <el-table-column prop="uploaderName" label="上传者" />
      <el-table-column prop="createTime" label="上传时间" :formatter="formatTime" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link @click="handleDownload(row)">下载</el-button>
          <el-button
            link
            type="danger"
            :disabled="!canDelete(row)"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="selectedFiles.length > 0" class="batch-actions">
      <span>已选 {{ selectedFiles.length }} 项</span>
      <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  groupId: String,
  currentUserRole: String // OWNER, ADMIN, MEMBER
})

// 角色权限判断
const canDelete = (file) => {
  return props.currentUserRole !== 'MEMBER' ||
         file.uploaderId === currentUserId.value
}
</script>
```

3. **集成到ChatPanel**
```vue
<el-drawer v-model="groupFileVisible" title="群文件" size="600px">
  <GroupFilePanel
    :group-id="currentConversation.targetId"
    :current-user-role="currentUserRoleInGroup"
  />
</el-drawer>
```

**验收标准**:
- 群组会话显示文件入口
- 文件按类型分类展示
- 支持搜索、上传、下载
- 群主/管理员可删除任意文件
- 普通成员只能删除自己上传的文件
- 支持批量选择和删除

---

#### P1-4 & P1-5: WebRTC多人通话扩展
**状态**: ⚠️ 1v1完成，需多人会议UI和语音模式

**文件修改**:
- `ruoyi-im-web/src/components/Chat/CallDialog.vue`
- `ruoyi-im-web/src/views/ChatPanel.vue`

**实施步骤**:

1. **自适应网格布局**
```vue
<template>
  <div class="video-call-grid" :class="`grid-${participantCount}`">
    <div v-for="participant in participants" :key="participant.userId" class="video-slot">
      <video
        :ref="el => setVideoRef(participant.userId, el)"
        autoplay
        playsinline
        :muted="participant.isLocal"
      />
      <div class="participant-info">
        <span>{{ participant.userName }}</span>
        <el-icon v-if="participant.isMuted"><MuteNotification /></el-icon>
      </div>
    </div>
  </div>
</template>

<style scoped>
.video-call-grid {
  display: grid;
  gap: 8px;
  height: 100%;
  padding: 16px;

  &.grid-1 { grid-template-columns: 1fr; }
  &.grid-2 { grid-template-columns: 1fr; grid-template-rows: 1fr 1fr; }
  &.grid-3, &.grid-4 { grid-template-columns: 1fr 1fr; }
  &.grid-5, &.grid-6, &.grid-7, &.grid-8, &.grid-9 {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
```

2. **语音模式UI**
```vue
<template>
  <div class="voice-call-ui">
    <div class="voice-avatars">
      <div v-for="participant in participants" :key="participant.userId" class="voice-avatar">
        <DingtalkAvatar
          :src="participant.avatar"
          :size="80"
          :class="{ speaking: participant.isSpeaking }"
        />
        <span>{{ participant.userName }}</span>
      </div>
    </div>
    <div class="call-controls">
      <el-button circle @click="toggleMute">
        <el-icon><MuteNotification v-if="isMuted" /><Microphone v-else /></el-icon>
      </el-button>
      <el-button type="danger" circle @click="hangup">
        <el-icon><PhoneFilled /></el-icon>
      </el-button>
    </div>
  </div>
</template>
```

3. **画质自适应**
```javascript
// 根据网络状况调整画质
const adaptQuality = (stats) => {
  const bandwidth = stats.bandwidth || 1000

  if (bandwidth < 500) {
    sender.setParameters({ encodings: [{ maxBitrate: 300000 }] })
  } else if (bandwidth < 1000) {
    sender.setParameters({ encodings: [{ maxBitrate: 700000 }] })
  } else {
    sender.setParameters({ encodings: [{ maxBitrate: 2000000 }] })
  }
}
```

**验收标准**:
- 2人视频上下排列
- 3-4人2x2网格
- 5-9人3x3网格
- 支持语音模式（头像+声波动画）
- 画质根据网络自适应调整

---

#### P1-6: 输入状态同步
**状态**: ❌ 待开发

**后端修改**:
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImUserController.java`
- `ruoyi-im-api/src/main/java/com/ruoyi/im/websocket/ImWebSocketHandler.java`

**前端修改**:
- `ruoyi-im-web/src/components/Chat/MessageInput.vue`
- `ruoyi-im-web/src/views/ChatPanel.vue`

**实施步骤**:

1. **后端：WebSocket扩展**
```java
// ImWebSocketHandler.java
public void handleTyping(WebSocketSession session, TypingMessage msg) {
    String conversationId = msg.getConversationId();
    Long userId = getUserIdFromSession(session);

    // 存储到Redis，3秒过期
    String key = "typing:" + conversationId + ":" + userId;
    redisTemplate.opsForValue().set(key, "1", 3, TimeUnit.SECONDS);

    // 广播给会话其他成员
    TypingNotification notification = new TypingNotification();
    notification.setConversationId(conversationId);
    notification.setUserId(userId);
    notification.setUserName(getUserName(userId));

    broadcastToConversation(conversationId, notification, userId);
}
```

2. **前端：发送输入状态**
```javascript
// MessageInput.vue
import { debounce } from 'lodash-es'

const emitTyping = debounce(() => {
  if (inputValue.value.trim() && currentConversationId.value) {
    ws.send({
      type: 'typing',
      conversationId: currentConversationId.value
    })
  }
}, 1000)

watch(inputValue, () => {
  if (inputValue.value.trim()) {
    emitTyping()
  }
})
```

3. **前端：显示输入提示**
```vue
<template>
  <div v-if="typingUsers.length > 0" class="typing-indicator">
    <span>{{ getTypingText() }}</span>
    <span class="typing-dots">
      <span class="dot"></span>
      <span class="dot"></span>
      <span class="dot"></span>
    </span>
  </div>
</template>

<script setup>
const getTypingText = () => {
  const names = typingUsers.value.map(u => u.userName).slice(0, 3)
  if (typingUsers.value.length <= 3) {
    return names.join('、') + '正在输入...'
  }
  return names.join('、') + `等${typingUsers.value.length}人正在输入...`
}
</script>
```

**验收标准**:
- 输入时发送typing事件（防抖1秒）
- 接收后显示"XX正在输入..."
- 多人输入时显示"A、B、C正在输入..."（最多3人）
- 停止输入3秒后提示消失

---
## P2 - 增强功能
---

#### P2-1: 链接卡片自动识别
**状态**: ❌ 待开发

**后端修改**:
- `ruoyi-im-api/src/main/java/com/ruoyi/im/service/LinkMetadataService.java`
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImMessageController.java`

**前端修改**:
- `ruoyi-im-web/src/components/Chat/LinkCard.vue` (新建)
- `ruoyi-im-web/src/components/Chat/MessageBubble.vue`

**实施步骤**:

1. **后端：链接元数据服务**
```java
@Service
public class LinkMetadataService {

    public LinkMetadataDTO fetchMetadata(String url) {
        try {
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(5000)
                .get();

            LinkMetadataDTO metadata = new LinkMetadataDTO();
            metadata.setUrl(url);
            metadata.setTitle(doc.title());

            // 提取描述
            Element description = doc.select("meta[name=description]").first();
            if (description != null) {
                metadata.setDescription(description.attr("content"));
            }

            // 提取缩略图
            Element ogImage = doc.select("meta[property=og:image]").first();
            if (ogImage != null) {
                metadata.setThumbnail(ogImage.attr("content"));
            }

            return metadata;
        } catch (Exception e) {
            log.error("获取链接元数据失败: {}", url, e);
            return null;
        }
    }
}
```

2. **前端：链接卡片组件**
```vue
<template>
  <div class="link-card" @click="openLink">
    <div v-if="metadata.thumbnail" class="link-thumbnail">
      <img :src="metadata.thumbnail" />
    </div>
    <div class="link-content">
      <div class="link-title">{{ metadata.title || metadata.url }}</div>
      <div class="link-description">{{ metadata.description }}</div>
      <div class="link-url">{{ getDomain(metadata.url) }}</div>
    </div>
  </div>
</template>

<style scoped>
.link-card {
  display: flex;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  }
}

.link-thumbnail {
  width: 120px;
  height: 90px;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.link-content {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
```

3. **URL检测和渲染**
```javascript
// MessageBubble.vue
const urlRegex = /(https?:\/\/[^\s]+)/g
const urls = computed(() => {
  if (props.message.messageType !== 'TEXT') return []
  return props.message.content.match(urlRegex) || []
})

const linkMetadata = ref(null)

onMounted(async () => {
  if (urls.value.length > 0) {
    const { data } = await getLinkMetadata(urls.value[0])
    linkMetadata.value = data
  }
})
```

**验收标准**:
- 发送消息时自动检测URL
- 异步获取链接元数据
- 显示缩略图、标题、描述
- 点击卡片在新窗口打开链接

---

#### P2-2: 自定义表情包管理
**状态**: ❌ 待开发

**后端修改**:
- `ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImEmoji.java`
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImEmojiController.java`

**前端修改**:
- `ruoyi-im-web/src/components/Chat/EmojiManager.vue` (新建)
- `ruoyi-im-web/src/components/Chat/EmojiPicker.vue` (修改)

**实施步骤**:

1. **数据库表**
```sql
CREATE TABLE im_emoji (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    emoji_code VARCHAR(50) NOT NULL COMMENT '表情编码',
    emoji_url VARCHAR(500) NOT NULL COMMENT '表情URL',
    category VARCHAR(50) DEFAULT 'custom' COMMENT '分类：system/custom',
    uploader_id BIGINT COMMENT '上传者ID',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开：0私有 1公开',
    width INT DEFAULT 100 COMMENT '宽度',
    height INT DEFAULT 100 COMMENT '高度',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

2. **表情管理对话框**
```vue
<template>
  <el-dialog v-model="visible" title="表情管理" width="700px">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="系统表情" name="system">
        <div class="emoji-grid">
          <div v-for="emoji in systemEmojis" :key="emoji.id" class="emoji-item">
            <img :src="emoji.emojiUrl" @click="insertEmoji(emoji)" />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的表情" name="custom">
        <div class="emoji-actions">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            accept="image/*"
            @success="handleUploadSuccess"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              添加表情
            </el-button>
          </el-upload>
        </div>
        <div class="emoji-grid">
          <div v-for="emoji in customEmojis" :key="emoji.id" class="emoji-item custom">
            <img :src="emoji.emojiUrl" @click="insertEmoji(emoji)" />
            <el-dropdown trigger="click" @click.stop>
              <el-icon class="more-btn"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleShare(emoji)">分享</el-dropdown-item>
                  <el-dropdown-item @click="handleDelete(emoji)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>
```

3. **表情选择器扩展**
```vue
<template>
  <div class="emoji-picker">
    <div class="emoji-tabs">
      <div
        v-for="category in categories"
        :key="category.key"
        :class="{ active: activeCategory === category.key }"
        @click="activeCategory = category.key"
      >
        {{ category.icon }}
      </div>
    </div>
    <div class="emoji-list">
      <span
        v-for="emoji in currentEmojis"
        :key="emoji.code"
        @click="$emit('select', emoji)"
      >
        {{ emoji.code }}
      </span>
    </div>
    <div class="emoji-footer">
      <el-button link @click="$emit('openManager')">
        <el-icon><Setting /></el-icon>
        表情管理
      </el-button>
    </div>
  </div>
</template>
```

**验收标准**:
- 系统表情和自定义表情分类展示
- 支持上传自定义表情
- 支持分享表情给好友
- 点击表情直接插入输入框

---

#### P2-3: 消息置顶功能
**状态**: ⚠️ 会话置顶已有，需消息级置顶

**后端修改**:
- `ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImMessageMarker.java` (已有)
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImMessageMarkerController.java`

**前端修改**:
- `ruoyi-im-web/src/views/ChatPanel.vue`
- `ruoyi-im-web/src/components/Chat/MessageItem.vue`

**实施步骤**:

1. **置顶入口**
```vue
<!-- MessageItem.vue 菜单 -->
<el-dropdown-item command="pin" :disabled="isPinned">
  <el-icon><Top /></el-icon>
  {{ isPinned ? '取消置顶' : '置顶消息' }}
</el-dropdown-item>
```

2. **置顶消息展示区**
```vue
<template>
  <div v-if="pinnedMessages.length > 0" class="pinned-messages-bar">
    <el-scrollbar>
      <div class="pinned-list">
        <div
          v-for="msg in pinnedMessages"
          :key="msg.id"
          class="pinned-item"
          @click="scrollToMessage(msg.id)"
        >
          <div class="pinned-content">{{ getPreviewText(msg) }}</div>
          <el-icon
            class="unpin-btn"
            @click.stop="handleUnpin(msg.id)"
          >
            <Close />
          </el-icon>
        </div>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup>
// 全局最多3条置顶消息
const pinnedMessages = computed(() => {
  return store.state.im.message.pinnedMessages
    .sort((a, b) => new Date(b.pinTime) - new Date(a.pinTime))
    .slice(0, 3)
})

const handlePin = async (message) => {
  if (pinnedMessages.value.length >= 3) {
    ElMessage.warning('最多只能置顶3条消息')
    return
  }
  await markMessage({
    messageId: message.id,
    markerType: 'PIN',
    color: '#FF6B6B'
  })
}
</script>
```

**验收标准**:
- 长按消息显示置顶选项
- 全局限制最多3条置顶消息
- 置顶消息在聊天区域顶部展示
- 点击置顶消息滚动到原位置
- 支持取消置顶

---

#### P2-4: 快捷回复功能
**状态**: ❌ 待开发

**后端修改**:
- `ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImQuickReply.java`
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImQuickReplyController.java`

**前端修改**:
- `ruoyi-im-web/src/components/Chat/QuickReplyDialog.vue` (新建)
- `ruoyi-im-web/src/components/Chat/MessageInput.vue`

**实施步骤**:

1. **数据库表**
```sql
CREATE TABLE im_quick_reply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content VARCHAR(500) NOT NULL COMMENT '回复内容',
    category VARCHAR(50) DEFAULT 'custom' COMMENT '分类：greeting/ending/common/custom',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

2. **快捷回复管理对话框**
```vue
<template>
  <el-dialog v-model="visible" title="快捷回复管理" width="600px">
    <el-tabs v-model="activeCategory">
      <el-tab-pane label="问候语" name="greeting" />
      <el-tab-pane label="结束语" name="ending" />
      <el-tab-pane label="常用语" name="common" />
      <el-tab-pane label="自定义" name="custom" />
    </el-tabs>

    <div class="quick-reply-list">
      <div v-for="(item, index) in currentCategoryReplies" :key="item.id" class="reply-item">
        <span>{{ item.content }}</span>
        <div class="reply-actions">
          <el-icon @click="handleEdit(item)"><Edit /></el-icon>
          <el-icon @click="handleDelete(item.id)"><Delete /></el-icon>
          <el-icon @click="handleMoveUp(index)"><ArrowUp /></el-icon>
        </div>
      </div>
    </div>

    <div class="add-reply">
      <el-input
        v-model="newReply"
        placeholder="输入快捷回复内容"
        @keyup.enter="handleAdd"
      />
      <el-button type="primary" @click="handleAdd">添加</el-button>
    </div>
  </el-dialog>
</template>
```

3. **输入框上方快捷栏**
```vue
<template>
  <div v-if="quickReplies.length > 0" class="quick-reply-bar">
    <el-scrollbar>
      <div class="quick-reply-list">
        <el-button
          v-for="reply in quickReplies"
          :key="reply.id"
          size="small"
          @click="insertQuickReply(reply.content)"
        >
          {{ reply.content }}
        </el-button>
      </div>
    </el-scrollbar>
    <el-icon class="close-btn" @click="showQuickBar = false">
      <Close />
    </el-icon>
  </div>
</template>

<style scoped>
.quick-reply-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  margin-bottom: 8px;
}

.quick-reply-list {
  display: flex;
  gap: 8px;
  flex: 1;
}
</style>
```

**验收标准**:
- 输入框上方显示横向滚动的快捷回复按钮
- 点击快捷回复直接插入到输入框
- 支持分类：问候语、结束语、常用语、自定义
- 支持新增、编辑、删除、排序
- 快捷回复可收起

---

#### P2-5: 聊天记录导出
**状态**: ❌ 待开发

**后端修改**:
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImMessageController.java`

**前端修改**:
- `ruoyi-im-web/src/components/Chat/ExportDialog.vue` (新建)
- `ruoyi-im-web/src/views/ChatPanel.vue`

**实施步骤**:

1. **后端导出API**
```java
@RestController
@RequestMapping("/api/im/message")
public class ImMessageController {

    @GetMapping("/export")
    public void exportMessages(
            @RequestParam Long conversationId,
            @RequestParam String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            HttpServletResponse response) throws IOException {

        List<ImMessage> messages = messageService.getMessagesByTimeRange(
            conversationId, startTime, endTime);

        switch (format.toLowerCase()) {
            case "pdf":
                exportToPdf(messages, response);
                break;
            case "html":
                exportToHtml(messages, response);
                break;
            case "excel":
                exportToExcel(messages, response);
                break;
            case "txt":
                exportToTxt(messages, response);
                break;
            default:
                throw new BusinessException("不支持的导出格式");
        }
    }

    private void exportToPdf(List<ImMessage> messages, HttpServletResponse response) {
        // 使用 iText 生成 PDF
    }

    private void exportToHtml(List<ImMessage> messages, HttpServletResponse response) {
        // 使用 Thymeleaf 模板生成 HTML
    }

    private void exportToExcel(List<ImMessage> messages, HttpServletResponse response) {
        // 使用 Apache POI 生成 Excel
    }

    private void exportToTxt(List<ImMessage> messages, HttpServletResponse response) {
        // 生成纯文本
    }
}
```

2. **导出对话框**
```vue
<template>
  <el-dialog v-model="visible" title="导出聊天记录" width="500px">
    <el-form :model="exportForm" label-width="100px">
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
        />
      </el-form-item>

      <el-form-item label="导出格式">
        <el-checkbox-group v-model="exportForm.formats">
          <el-checkbox label="pdf">PDF（打印存档）</el-checkbox>
          <el-checkbox label="html">HTML（样式保留）</el-checkbox>
          <el-checkbox label="excel">Excel（数据格式）</el-checkbox>
          <el-checkbox label="txt">TXT（轻量）</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="包含内容">
        <el-checkbox v-model="exportForm.includeImages">包含图片</el-checkbox>
        <el-checkbox v-model="exportForm.includeFiles">包含文件信息</el-checkbox>
        <el-checkbox v-model="exportForm.includeVoice">包含语音转文字</el-checkbox>
      </el-form-item>
    </el-form>

    <div v-if="exporting" class="export-progress">
      <el-progress :percentage="progress" />
      <p>{{ exportStatus }}</p>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="exporting" @click="handleExport">
        开始导出
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
const handleExport = async () => {
  exporting.value = true
  progress.value = 0

  try {
    for (const format of exportForm.value.formats) {
      exportStatus.value = `正在导出 ${format.toUpperCase()}...`

      const response = await exportMessages({
        conversationId: currentConversationId.value,
        format,
        startTime: dateRange.value[0],
        endTime: dateRange.value[1],
        ...exportForm.value
      })

      // 下载文件
      downloadFile(response, `chat_export_${format}.${format}`)

      progress.value += (100 / exportForm.value.formats.length)
    }

    ElMessage.success('导出完成')
    visible.value = false
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  } finally {
    exporting.value = false
  }
}
</script>
```

3. **导出入口**
```vue
<!-- ChatPanel.vue 工具栏 -->
<el-dropdown @command="handleToolbarCommand">
  <el-icon><MoreFilled /></el-icon>
  <template #dropdown>
    <el-dropdown-menu>
      <el-dropdown-item command="export">导出聊天记录</el-dropdown-item>
      <!-- 其他选项 -->
    </el-dropdown-menu>
  </template>
</el-dropdown>
```

**验收标准**:
- 支持选择时间范围导出
- 支持 PDF、HTML、Excel、TXT 四种格式
- 可选择是否包含图片、文件、语音转文字
- 显示导出进度
- 导出完成后自动下载文件


### Acceptance Criteria

---
## P0 验收标准
---

#### AC-P0-1: 消息发送状态可视化
**Scenario**: 用户发送消息并观察状态变化

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 用户输入消息并点击发送 | 消息立即显示在列表，状态图标为旋转Loading |
| 2 | 等待消息发送成功 | Loading消失，显示单勾图标（灰色） |
| 3 | 等待消息被对方接收 | 单勾变为双勾（蓝色） |
| 4 | 等待对方已读 | 双勾变为实心双勾（绿色） |
| 5 | 模拟网络断开，发送消息 | 显示感叹号图标（红色） |
| 6 | 点击失败消息的感叹号 | 弹出操作菜单：重新发送、删除 |
| 7 | 点击"重新发送" | 消息状态变为Loading，重新尝试发送 |

#### AC-P0-2: 失败消息重发
**Scenario**: 消息发送失败后重试

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 发送消息时网络断开 | 消息显示失败状态（红色感叹号） |
| 2 | 点击感叹号，选择"重新发送" | 消息状态变为Loading |
| 3 | 网络恢复后 | 消息发送成功，状态正常 |
| 4 | 重发仍然失败 | 可再次重试，无次数限制 |

#### AC-P0-3: 图片全屏预览
**Scenario**: 用户查看会话中的多张图片

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 会话中有多张图片消息 | 图片正常显示在消息列表 |
| 2 | 点击任意一张图片 | 进入全屏预览模式 |
| 3 | 按键盘右箭头 | 切换到下一张图片 |
| 4 | 按键盘左箭头 | 切换到上一张图片 |
| 5 | 鼠标滚轮向上滚动 | 图片放大 |
| 6 | 鼠标滚轮向下滚动 | 图片缩小 |
| 7 | 按ESC键或点击背景 | 退出预览模式 |

---
## P1 验收标准
---

#### AC-P1-1: 消息多选模式
**Scenario**: 用户批量操作消息

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 点击工具栏"多选"按钮 | 进入多选模式，所有消息显示勾选框 |
| 2 | 勾选3条消息 | 勾选框显示选中状态 |
| 3 | 批量操作工具栏显示"已选 3 条" | 状态正确显示 |
| 4 | 点击"转发"按钮 | 打开转发对话框，已选3条消息 |
| 5 | 点击"取消"按钮 | 退出多选模式，勾选框消失 |

#### AC-P1-2: 合并转发消息
**Scenario**: 用户收到合并转发的聊天记录

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 收到合并转发消息 | 显示卡片样式，标题"5条聊天记录" |
| 2 | 查看卡片内容 | 显示前3条消息摘要（发送者+内容） |
| 3 | 点击卡片 | 打开新对话框，显示完整聊天记录 |
| 4 | 完整记录包含 | 发送者头像、名称、时间、完整内容 |

#### AC-P1-3: 群文件管理
**Scenario**: 群组用户管理群文件

| 步骤 | 操作 | 角色 | 预期结果 |
|-----|------|-----|---------|
| 1 | 打开群文件面板 | 任意 | 显示文件列表，按类型分类 |
| 2 | 上传文件 | 任意 | 文件上传成功，列表更新 |
| 3 | 点击"图片"标签 | 任意 | 只显示图片类型文件 |
| 4 | 搜索文件名 | 任意 | 显示匹配的文件 |
| 5 | 删除他人上传的文件 | 普通成员 | 删除按钮禁用 |
| 6 | 删除他人上传的文件 | 管理员 | 删除成功 |
| 7 | 勾选2个文件，批量删除 | 任意 | 两个文件都被删除 |

#### AC-P1-4: 多人视频通话
**Scenario**: 3人进行视频会议

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 3人加入视频通话 | 显示2x2网格布局 |
| 2 | 第4人加入 | 仍保持2x2布局 |
| 3 | 第5人加入 | 切换到3x3网格布局 |
| 4 | 点击"语音模式" | 切换到头像+声波动画模式 |
| 5 | 网络变差 | 画质自动降低，保持流畅 |
| 6 | 网络恢复 | 画质自动提升 |

#### AC-P1-5: 输入状态同步
**Scenario**: 多用户同时输入

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | A开始输入文字 | B和C的界面显示"A正在输入..." |
| 2 | B也开始输入 | A和C的界面显示"A、B正在输入..." |
| 3 | C也开始输入 | 所有人显示"A、B、C正在输入..." |
| 4 | D也开始输入 | 所有人显示"A、B、C等4人正在输入..." |
| 5 | A停止输入3秒 | "A"从列表中消失 |
| 6 | 所有人停止输入 | 提示完全消失 |

#### AC-P1-6: 屏幕共享
**Scenario**: 会议中共享屏幕

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 共享者点击"共享屏幕" | 共享者可以看到自己的屏幕画面 |
| 2 | 其他参与者看到 | 远程屏幕画面 |
| 3 | 共享者停止共享 | 返回视频画面 |

---
## P2 验收标准
---

#### AC-P2-1: 链接卡片
**Scenario**: 发送包含URL的消息

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 输入包含URL的文本 | 检测到URL |
| 2 | 发送消息 | 自动异步获取链接元数据 |
| 3 | 元数据获取成功 | 消息下方显示链接卡片（缩略图、标题、描述） |
| 4 | 点击链接卡片 | 在新窗口打开原始URL |

#### AC-P2-2: 自定义表情包
**Scenario**: 用户管理自定义表情

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 打开表情管理对话框 | 显示系统表情和我的表情两个标签 |
| 2 | 点击"添加表情" | 打开文件选择对话框 |
| 3 | 选择图片上传 | 上传成功，添加到"我的表情" |
| 4 | 点击表情 | 插入到输入框 |
| 5 | 点击"分享" | 生成表情分享链接 |

#### AC-P2-3: 消息置顶
**Scenario**: 用户置顶重要消息

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 长按消息，选择"置顶" | 消息被置顶 |
| 2 | 置顶消息显示在聊天区域顶部 | 显示消息内容预览和删除按钮 |
| 3 | 置顶第4条消息 | 提示"最多只能置顶3条消息" |
| 4 | 点击置顶消息 | 聊天记录滚动到原消息位置 |
| 5 | 点击置顶消息的删除按钮 | 取消置顶 |

#### AC-P2-4: 快捷回复
**Scenario**: 用户使用快捷回复

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 输入框上方显示快捷回复栏 | 横向滚动显示快捷短语按钮 |
| 2 | 点击"收到，谢谢"按钮 | 短语直接插入到输入框 |
| 3 | 点击"快捷回复管理" | 打开管理对话框 |
| 4 | 切换到"问候语"标签 | 显示问候语分类的快捷短语 |
| 5 | 添加新短语 | 输入内容，按回车，添加成功 |
| 6 | 拖拽短语调整顺序 | 顺序更新 |

#### AC-P2-5: 聊天记录导出
**Scenario**: 用户导出聊天记录

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 点击工具栏"导出"按钮 | 打开导出对话框 |
| 2 | 选择时间范围（最近7天） | 日期选择器显示范围 |
| 3 | 勾选PDF和Excel格式 | 两个复选框被选中 |
| 4 | 勾选"包含图片" | 复选框被选中 |
| 5 | 点击"开始导出" | 显示进度条 |
| 6 | 导出完成 | 自动下载PDF和Excel两个文件 |

---
## 边界场景验收
---

#### AC-EDGE-1: 网络异常处理
| 场景 | 操作 | 预期结果 |
|-----|------|---------|
| 离线发送消息 | 消息进入本地队列 | 网络恢复后自动发送 |
| WebSocket断开 | 显示连接状态提示 | 自动重连，重连后同步离线消息 |
| 大量离线消息 | 混合模式处理 | 少量推送，大量拉取 |

#### AC-EDGE-2: 敏感内容处理
| 场景 | 操作 | 预期结果 |
|-----|------|---------|
| 发送包含敏感词的消息 | 服务端检测 | 消息被撤回，用户收到警告 |
| 上传违规文件 | 服务端检测 | 上传失败，显示错误提示 |

#### AC-EDGE-3: 权限控制
| 场景 | 操作 | 预期结果 |
|-----|------|---------|
| 普通成员尝试解散群 | 无权限 | 操作按钮禁用或隐藏 |
| 非管理员尝试删除他人文件 | 无权限 | 删除按钮禁用 |

## Additional Context

### Dependencies

- 现有 WebSocket 基础设施
- 现有文件上传 API
- 现有消息存储结构
- 钉钉 UI 设计参考

### Testing Strategy

- 每个功能需要单元测试（Vitest）和集成测试
- 消息状态需要模拟网络延迟场景测试
- WebRTC 功能需要两端联调测试
- 拖拽上传需要测试各种文件类型

### Notes

- WebRTC 功能可能需要额外的 STUN/TURN 服务器配置
- 链接卡片解析需要考虑网络请求超时和失败处理
- 语音消息录制需要浏览器权限支持
- 全屏预览组件需要考虑大量图片的性能问题
