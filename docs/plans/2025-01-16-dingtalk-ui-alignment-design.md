# 钉钉 UI 完整对齐设计文档

> **设计日期**：2025-01-16
> **目标版本**：钉钉PC版 6.5.x
> **设计范围**：P0 尺寸对齐 + P1 功能增强

---

## 一、设计目标

将 RuoYi-IM 前端界面与钉钉PC版 6.5.x 进行完整对齐，包括：
- P0：尺寸精确对齐（导航栏、会话列表、图标）
- P1：核心功能增强（时间分割线、已读状态、输入区优化）

---

## 二、P0 尺寸对齐

### 2.1 修改汇总

| 组件 | 文件路径 | 当前值 | 目标值 | 修改内容 |
|------|----------|--------|--------|----------|
| 导航栏宽度 | `ImSideNav/index.vue` | 60px | **64px** | `navWidth` prop、CSS width |
| 导航图标 | `ImSideNav/index.vue` | 22px | **24px** | `.nav-icon` font-size |
| 会话列表宽度 | `SessionPanel.vue` | 320px | **280px** | 面板宽度 |
| 搜索框背景 | `ImHeader/index.vue` | 白色 | **#f5f5f5** | 输入框背景色 |

### 2.2 ImSideNav 组件修改

```vue
<!-- ImSideNav/index.vue -->
<template>
  <aside class="nav-sidebar" :style="{ width: navWidth + 'px' }">
    <!-- ... -->
  </aside>
</template>

<script setup>
const props = defineProps({
  navWidth: {
    type: Number,
    default: 64  // 从 60 改为 64
  }
})
</script>

<style scoped>
.nav-sidebar {
  width: 64px;  /* 从 60px 改为 64px */
}

.nav-icon {
  font-size: 24px;  /* 从 22px 改为 24px */
}

.nav-item {
  width: 48px;
  height: 48px;
}
</style>
```

### 2.3 SessionPanel 组件修改

```vue
<!-- SessionPanel.vue -->
<style scoped>
.session-panel {
  width: 280px;  /* 从 320px 改为 280px */
}

.session-item {
  height: 64px;
  padding: 12px 16px;
}

.session-avatar {
  width: 40px;
  height: 40px;
}
</style>
```

### 2.4 ImHeader 搜索框修改

```vue
<!-- ImHeader/index.vue -->
<style scoped>
.global-search-input {
  :deep(.el-input__wrapper) {
    background-color: #f5f5f5;  /* 改为灰色背景 */
    border-radius: 8px;
    box-shadow: none;
    border: 1px solid transparent;

    &:hover {
      background-color: #f0f0f0;
      border-color: #d9d9d9;
    }

    &.is-focus {
      background-color: #ffffff;
      border-color: #1677ff;
      box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
    }
  }
}
</style>
```

### 2.5 主题变量更新

```scss
// design-tokens.scss
:root {
  --dt-nav-sidebar-width: 64px;      // 从 68px 改为 64px
  --dt-nav-icon-size: 24px;          // 保持 24px
  --dt-session-panel-width: 280px;   // 从 320px 改为 280px
}

// dingtalk-theme.scss
$nav-sidebar-width: 64px;            // 从 68px 改为 64px
$sidebar-width: 280px;               // 从 320px 改为 280px
```

---

## 三、P1 功能增强

### 3.1 时间分割线

#### 功能说明
- 每隔约30分钟或5-10条消息显示一条时间线
- 时间线居中显示，浅灰背景，圆角4px

#### 组件结构

```vue
<!-- ChatPanel.vue - 新增时间分割线组件 -->
<template>
  <div class="chat-messages">
    <div v-for="message in messages" :key="message.id">
      <!-- 时间分割线（条件渲染） -->
      <div v-if="shouldShowTimeDivider(message, index)" class="time-divider">
        <span class="time-text">{{ formatTimeDivider(message.timestamp) }}</span>
      </div>

      <!-- 消息气泡 -->
      <MessageBubble :message="message" />
    </div>
  </div>
</template>

<script setup>
// 判断是否显示时间分割线
const shouldShowTimeDivider = (message, index) => {
  if (index === 0) return true

  const prevMessage = messages.value[index - 1]
  const timeDiff = message.timestamp - prevMessage.timestamp

  // 30分钟或5条消息间隔
  return timeDiff > 30 * 60 * 1000 || index % 5 === 0
}

// 格式化时间显示
const formatTimeDivider = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  const diffDays = Math.floor((today - msgDate) / (1000 * 60 * 60 * 24))

  const timeStr = date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })

  if (diffDays === 0) return `今天 ${timeStr}`
  if (diffDays === 1) return `昨天 ${timeStr}`
  if (diffDays < 7) return `${diffDays}天前 ${timeStr}`

  return `${date.getMonth() + 1}月${date.getDate()}日 ${timeStr}`
}
</script>

<style scoped>
.time-divider {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 16px 0;
}

.time-text {
  background: rgba(0, 0, 0, 0.04);
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #8c8c8c;
}
</style>
```

### 3.2 已读状态显示

#### 功能说明
- **单聊**：消息气泡下方显示"已读"（灰色小字）
- **群聊**：消息气泡下方显示已读人数，hover显示详情

#### 组件结构

```vue
<!-- MessageBubble.vue - 新增已读状态 -->
<template>
  <div class="message-wrapper" :class="{ 'is-own': isOwn }">
    <!-- 头像 -->
    <img v-if="!isOwn" :src="senderAvatar" class="message-avatar" />

    <!-- 消息内容区 -->
    <div class="message-content-wrapper">
      <!-- 发送者名称（群聊） -->
      <div v-if="!isOwn && isGroup" class="sender-name">
        {{ senderName }}
      </div>

      <!-- 消息气泡 -->
      <div class="message-bubble">
        {{ message.content }}
      </div>

      <!-- 已读状态 -->
      <div v-if="showReadStatus" class="read-status">
        <!-- 单聊：已读 -->
        <template v-if="isPrivate && message.isRead">
          <span class="read-text">已读</span>
        </template>

        <!-- 群聊：已读人数 -->
        <template v-if="isGroup && message.readCount > 0">
          <el-popover placement="top" :width="200" trigger="hover">
            <template #reference>
              <span class="read-count">{{ message.readCount }}人已读</span>
            </template>
            <div class="read-list">
              <div v-for="user in message.readBy" :key="user.id" class="read-user">
                <el-avatar :size="24" :src="user.avatar" />
                <span>{{ user.name }}</span>
              </div>
            </div>
          </el-popover>
        </template>
      </div>
    </div>

    <!-- 自己的头像 -->
    <img v-if="isOwn" :src="myAvatar" class="message-avatar" />
  </div>
</template>

<script setup>
const props = defineProps({
  message: Object,
  conversationType: String  // 'PRIVATE' | 'GROUP'
})

const isOwn = computed(() => props.message.senderId === currentUserId.value)
const isPrivate = computed(() => props.conversationType === 'PRIVATE')
const isGroup = computed(() => props.conversationType === 'GROUP')

// 是否显示已读状态（自己发送的消息）
const showReadStatus = computed(() => {
  return isOwn.value && props.message.status === 'sent'
})
</script>

<style scoped>
.read-status {
  margin-top: 4px;
  font-size: 12px;
  color: #8c8c8c;
}

.read-text {
  color: #8c8c8c;
}

.read-count {
  cursor: pointer;
  color: #1677ff;

  &:hover {
    text-decoration: underline;
  }
}

.read-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.read-user {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
```

#### 后端接口调整

```java
// ImMessageService.java - 新增已读状态查询

/**
 * 获取消息已读状态
 * @param messageId 消息ID
 * @return 已读用户列表
 */
List<MessageReadUser> getMessageReadUsers(Long messageId);

/**
 * 获取消息已读人数
 * @param messageId 消息ID
 * @return 已读人数
 */
int getMessageReadCount(Long messageId);
```

### 3.3 输入区优化

#### 功能说明
- 工具栏增加 `@` 符号按钮（群聊@成员）
- 工具栏增加视频通话图标
- 发送按钮位置移到输入框右侧
- 输入框支持自动增高（最大5行）

#### 组件结构

```vue
<!-- ChatInput.vue - 优化后的输入区 -->
<template>
  <div class="chat-input-container">
    <!-- 工具栏 -->
    <div class="input-toolbar">
      <el-tooltip content="表情" placement="top">
        <el-button :icon="Emoji" text class="toolbar-btn" @click="showEmojiPicker" />
      </el-tooltip>

      <el-tooltip content="上传文件" placement="top">
        <el-button :icon="FolderOpened" text class="toolbar-btn" @click="uploadFile" />
      </el-tooltip>

      <el-tooltip content="上传图片" placement="top">
        <el-button :icon="Picture" text class="toolbar-btn" @click="uploadImage" />
      </el-tooltip>

      <!-- 新增：@成员功能（仅群聊） -->
      <el-tooltip v-if="isGroupChat" content="@成员" placement="top">
        <el-button :icon="AtSign" text class="toolbar-btn" @click="showAtMemberPicker" />
      </el-tooltip>

      <!-- 新增：语音通话 -->
      <el-tooltip content="语音通话" placement="top">
        <el-button :icon="Phone" text class="toolbar-btn" @click="startVoiceCall" />
      </el-tooltip>

      <!-- 新增：视频通话 -->
      <el-tooltip content="视频通话" placement="top">
        <el-button :icon="VideoCamera" text class="toolbar-btn" @click="startVideoCall" />
      </el-tooltip>

      <el-tooltip content="截图" placement="top">
        <el-button :icon="Scissor" text class="toolbar-btn" @click="takeScreenshot" />
      </el-tooltip>

      <el-tooltip content="历史记录" placement="top">
        <el-button :icon="Clock" text class="toolbar-btn" @click="showHistory" />
      </el-tooltip>
    </div>

    <!-- 输入区域（包含发送按钮） -->
    <div class="input-area-wrapper">
      <el-input
        ref="inputRef"
        v-model="inputContent"
        type="textarea"
        :autosize="{ minRows: 1, maxRows: 5 }"
        placeholder="输入消息..."
        class="message-input"
        @keydown="handleKeydown"
      />

      <!-- 发送按钮（移到右侧） -->
      <div class="send-button-wrapper">
        <el-button
          type="primary"
          :disabled="!inputContent.trim()"
          @click="sendMessage"
        >
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  Emoji,
  FolderOpened,
  Picture,
  Phone,
  VideoCamera,
  Scissor,
  Clock,
  AtSign
} from '@element-plus/icons-vue'

const isGroupChat = computed(() => {
  return currentConversation.value?.type === 'GROUP'
})

// 显示@成员选择器
const showAtMemberPicker = () => {
  // 打开成员选择弹窗
  emit('showAtMembers')
}

// 语音通话
const startVoiceCall = () => {
  emit('startVoiceCall')
}

// 视频通话
const startVideoCall = () => {
  emit('startVideoCall')
}
</script>

<style scoped>
.chat-input-container {
  border-top: 1px solid #e8e8e8;
  background: #fff;
}

.input-toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.toolbar-btn {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  color: #666;

  &:hover {
    background: rgba(0, 0, 0, 0.04);
    color: #333;
  }
}

.input-area-wrapper {
  display: flex;
  align-items: flex-end;
  padding: 8px 16px 12px;
  gap: 12px;
}

.message-input {
  flex: 1;

  :deep(.el-textarea__inner) {
    border: none;
    box-shadow: none;
    padding: 8px 0;
    resize: none;
    font-size: 14px;
    line-height: 1.5;
  }
}

.send-button-wrapper {
  flex-shrink: 0;
  margin-bottom: 2px;

  .el-button {
    min-width: 80px;
  }
}
</style>
```

---

## 四、实施计划

### 第一阶段：P0 尺寸对齐（立即执行）

| 任务 | 文件 | 预计工作量 |
|------|------|-----------|
| 修改导航栏宽度 | `ImSideNav/index.vue` | 5分钟 |
| 修改导航图标大小 | `ImSideNav/index.vue` | 5分钟 |
| 修改会话列表宽度 | `SessionPanel.vue` | 5分钟 |
| 修改搜索框背景 | `ImHeader/index.vue` | 5分钟 |
| 更新主题变量 | `design-tokens.scss` | 5分钟 |
| **总计** | | **25分钟** |

### 第二阶段：P1 功能增强

| 任务 | 文件 | 预计工作量 |
|------|------|-----------|
| 时间分割线组件 | `ChatPanel.vue` | 30分钟 |
| 已读状态显示（前端） | `MessageBubble.vue` | 30分钟 |
| 已读状态接口（后端） | `ImMessageService.java` | 45分钟 |
| 输入区工具栏优化 | `ChatInput.vue` | 30分钟 |
| @成员选择弹窗 | `AtMemberPicker.vue` (新建) | 45分钟 |
| **总计** | | **3小时** |

### 第三阶段：验证测试

| 任务 | 说明 |
|------|------|
| 视觉对比 | 与钉钉PC版进行逐像素对比 |
| 交互测试 | 验证所有新增功能的交互 |
| 响应式测试 | 不同分辨率下的显示效果 |
| 兼容性测试 | Chrome、Edge、Firefox 浏览器 |

---

## 五、设计规范速查表

### 5.1 尺寸规范

| 组件 | 宽度 | 高度 | 其他 |
|------|------|------|------|
| 顶部导航栏 | 100% | 48px | - |
| 左侧导航栏 | 64px | 100% | - |
| 导航项 | 48px | 48px | 图标24×24px |
| 会话列表 | 280px | 100% | - |
| 会话项 | 280px | 64px | 头像40×40px |
| 聊天头部 | 100% | 56px | - |
| 输入区 | 100% | auto | 最小120px |
| 发送按钮 | 80px | 32px | - |

### 5.2 颜色规范

| 用途 | 颜色值 | 备注 |
|------|--------|------|
| 品牌色 | #1677ff | 钉钉蓝 |
| 成功色 | #52c41a | - |
| 警告色 | #faad14 | - |
| 错误色 | #ff4d4f | - |
| 标题色 | #262626 | - |
| 正文色 | #595959 | - |
| 辅助色 | #8c8c8c | - |
| 边框色 | #e8e8e8 | - |
| 背景色 | #f5f5f5 | - |
| 激活背景 | rgba(0, 137, 255, 0.1) | - |

### 5.3 圆角规范

| 用途 | 值 |
|------|-----|
| 按钮 | 4px |
| 输入框 | 4px / 8px |
| 消息气泡 | 8px |
| 卡片 | 8px |
| 头像 | 50% |

### 5.4 间距规范

| 级别 | 值 | 用途 |
|------|-----|------|
| xs | 4px | 紧凑间距 |
| sm | 8px | 小间距 |
| md | 12px | 中间距 |
| lg | 16px | 大间距 |
| xl | 24px | 超大间距 |

---

## 六、参考文件清单

### 需要修改的文件

**前端 (Vue)**
```
ruoyi-im-web/src/
├── views/im/components/
│   ├── ImSideNav/index.vue          # 导航栏尺寸
│   ├── ImHeader/index.vue           # 搜索框样式
│   └── shared/
│       └── MessageBubble.vue        # 已读状态
├── views/im/workspaces/
│   ├── SessionPanel.vue             # 会话列表宽度
│   ├── ChatPanel.vue                # 时间分割线
│   └── ChatInput.vue                # 输入区优化
├── styles/
│   ├── design-tokens.scss           # 设计变量
│   └── dingtalk-theme.scss          # 主题变量
└── components/
    └── AtMemberPicker.vue           # 新建：@成员选择器
```

**后端 (Java)**
```
ruoyi-im-api/src/main/java/com/ruoyi/im/
├── controller/
│   └── ImMessageController.java     # 新增已读状态接口
├── service/
│   ├── ImMessageService.java        # 新增方法
│   └── impl/
│       └── ImMessageServiceImpl.java # 实现
└── domain/
    └── MessageReadUser.java         # 新建：已读用户实体
```

---

## 七、验收标准

### P0 尺寸对齐验收

- [ ] 导航栏宽度 = 64px（误差±0px）
- [ ] 导航图标大小 = 24px
- [ ] 会话列表宽度 = 280px（误差±0px）
- [ ] 搜索框背景色 = #f5f5f5
- [ ] 所有间距符合规范

### P1 功能增强验收

- [ ] 时间分割线正确显示（30分钟间隔或5条消息）
- [ ] 时间格式正确（今天/昨天/日期）
- [ ] 单聊显示"已读"状态
- [ ] 群聊显示已读人数和详情
- [ ] 输入工具栏包含@、视频按钮
- [ ] 发送按钮在输入框右侧
- [ ] 输入框自动增高（最大5行）

---

## 八、风险与注意事项

1. **已有组件覆盖**：修改前确认是否有其他组件使用了被修改的变量
2. **浏览器兼容**：CSS Grid/Flexbox 在旧浏览器中的兼容性
3. **后端接口**：已读状态功能需要后端支持
4. **性能影响**：时间分割线计算可能影响大量消息的渲染性能

---

## 九、后续优化方向

本设计文档聚焦于 P0 和 P1，未来可考虑的优化：

1. **消息引用样式优化**
2. **消息撤回动画**
3. **表情包面板优化**
4. **文件上传进度显示**
5. **语音消息录制动画**
