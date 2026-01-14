<template>
  <div class="dt-layout">
    <!-- 左侧导航栏 (68px 图标导航) -->
    <aside class="dt-sidebar">
      <!-- Logo区 -->
      <div class="dt-sidebar__logo">
        <svg class="ding-logo" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="48" height="48" rx="8" fill="url(#logoGradient)" />
          <path
            d="M24 12L18 18H14V22L8 28V36H16V32H20V36H28V28L32 24H36V16H28L24 12Z"
            fill="white"
          />
          <defs>
            <linearGradient
              id="logoGradient"
              x1="0"
              y1="0"
              x2="48"
              y2="48"
              gradientUnits="userSpaceOnUse"
            >
              <stop offset="0" stop-color="#1890FF" />
              <stop offset="1" stop-color="#096DD9" />
            </linearGradient>
          </defs>
        </svg>
      </div>

      <!-- 主导航菜单 -->
      <nav class="dt-sidebar__nav">
        <div
          v-for="item in navItems"
          :key="item.key"
          class="dt-sidebar__item"
          :class="{ active: activeNav === item.key }"
          :title="item.label"
          @click="handleNavClick(item)"
        >
          <el-icon :size="24">
            <component :is="item.icon" />
          </el-icon>
          <!-- 未读角标 -->
          <span v-if="item.unread > 0" class="dt-sidebar__badge">
            {{ item.unread > 99 ? '99+' : item.unread }}
          </span>
        </div>
      </nav>

      <!-- 底部用户区 -->
      <div class="dt-sidebar__bottom">
        <div
          class="dt-sidebar__avatar"
          :title="currentUser.nickname || '用户'"
          @click="handleProfileClick"
        >
          <el-avatar :size="36" :src="currentUser.avatar">
            {{ currentUser.nickname?.charAt(0) || '?' }}
          </el-avatar>
          <span v-if="isOnline" class="online-indicator online"></span>
          <span v-else class="online-indicator offline"></span>
        </div>
      </div>
    </aside>

    <!-- 中间会话列表 -->
    <section class="dt-sessions">
      <!-- 搜索头 -->
      <header class="dt-sessions__header">
        <div class="dt-sessions__title">
          {{ currentNavTitle }}
        </div>
        <div class="dt-sessions__search-row">
          <div class="dt-sessions__search">
            <el-icon><Search /></el-icon>
            <input v-model="searchKeyword" type="text" placeholder="搜索" @input="handleSearch" />
          </div>
          <div class="dt-sessions__actions">
            <el-button title="新建聊天" @click="handleAddChat">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </div>
      </header>

      <!-- 会话标签（仅消息模块显示） -->
      <div v-if="activeNav === 'message'" class="dt-sessions__tabs">
        <div
          v-for="tab in sessionTabs"
          :key="tab.key"
          class="dt-sessions__tab"
          :class="{ active: activeTab === tab.key }"
          @click="handleTabClick(tab)"
        >
          {{ tab.label }}
          <span v-if="tab.unread > 0" class="dt-sessions__tab-badge">{{ tab.unread }}</span>
        </div>
      </div>

      <!-- 会话列表 -->
      <div ref="sessionListRef" class="dt-sessions__list">
        <div
          v-for="session in filteredSessions"
          :key="session.id"
          class="dt-sessions__item"
          :class="{ active: activeSessionId === session.id, pinned: session.pinned }"
          @click="handleSessionClick(session)"
          @contextmenu.prevent="handleSessionContextMenu($event, session)"
        >
          <!-- 头像 -->
          <div class="dt-sessions__item-avatar">
            <el-avatar :size="44" :src="session.avatar">
              {{ session.name?.charAt(0) || '?' }}
            </el-avatar>
            <!-- 在线状态 -->
            <span
              v-if="session.onlineStatus"
              class="online-status"
              :class="session.onlineStatus"
            ></span>
          </div>

          <!-- 内容 -->
          <div class="dt-sessions__item-content">
            <div class="dt-sessions__item-header">
              <span class="dt-sessions__item-name">{{ session.name }}</span>
              <span class="dt-sessions__item-time">{{ formatTime(session.lastMessageTime) }}</span>
            </div>
            <div class="dt-sessions__item-message">
              <span v-if="session.lastMessageSender" class="sender-name">
                {{ session.lastMessageSender }}:
              </span>
              <span v-if="session.lastMessage?.type === 'at'" class="icon-at">
                <el-icon><Notification /></el-icon>
              </span>
              <span v-else-if="session.lastMessage?.type === 'file'" class="icon-file">
                <el-icon><Document /></el-icon>
              </span>
              <span v-else-if="session.lastMessage?.type === 'image'" class="icon-image">
                <el-icon><Picture /></el-icon>
              </span>
              <span v-else-if="session.lastMessage?.type === 'voice'" class="icon-voice">
                <el-icon><Microphone /></el-icon>
              </span>
              <span class="message-text">{{ session.lastMessage?.text || '' }}</span>
            </div>
          </div>

          <!-- 元信息 -->
          <div class="dt-sessions__item-meta">
            <!-- 置顶标记 -->
            <el-icon v-if="session.pinned" class="pin-icon">
              <Top />
            </el-icon>
            <!-- 未读数 -->
            <span
              v-if="session.unread > 0"
              class="dt-sessions__unread"
              :class="{ dot: session.unread === 1 }"
            >
              {{ session.unread > 1 ? session.unread : '' }}
            </span>
            <!-- 免打扰 -->
            <el-icon v-if="session.muted" class="dt-sessions__muted">
              <Bell />
            </el-icon>
          </div>
        </div>
      </div>
    </section>

    <!-- 右侧内容区域 -->
    <main class="dt-content">
      <!-- 聊天区域 -->
      <div v-if="activeNav === 'message'" class="dt-chat">
        <!-- 聊天头 -->
        <header v-if="activeSession" class="dt-chat__header">
          <div class="dt-chat__title">
            <span class="name">{{ activeSession.name }}</span>
            <span v-if="activeSession.isGroup" class="members" @click="showMembersDrawer = true">
              ({{ activeSession.memberCount || 0 }}人)
            </span>
          </div>
          <div class="dt-chat__actions">
            <el-button title="语音通话" @click="handlePhoneCall">
              <el-icon><Phone /></el-icon>
            </el-button>
            <el-button title="视频会议" @click="handleVideoCall">
              <el-icon><VideoCamera /></el-icon>
            </el-button>
            <el-button title="更多" @click="handleMoreAction">
              <el-icon><MoreFilled /></el-icon>
            </el-button>
          </div>
        </header>

        <!-- 空状态 -->
        <div v-else class="dt-chat__empty">
          <el-empty description="选择一个会话开始聊天" />
        </div>

        <!-- 消息列表 -->
        <div v-if="activeSession" ref="messagesRef" class="dt-chat__messages">
          <!-- 时间分隔符 -->
          <div
            v-for="(divider, index) in timeDividers"
            :key="'divider-' + index"
            class="dt-chat__time-divider"
          >
            {{ divider }}
          </div>

          <!-- 消息列表 -->
          <DingMessageBubble
            v-for="message in displayMessages"
            :key="message.id"
            :message="message"
            :is-own="message.senderId === currentUserId"
            :is-group="activeSession.isGroup"
            :current-user="currentUser"
            :show-time="shouldShowTime(message)"
            :show-actions="hoveredMessageId === message.id"
            @mouseenter="hoveredMessageId = message.id"
            @mouseleave="hoveredMessageId = null"
            @click="handleMessageClick"
            @retry="handleMessageRetry"
            @copy="handleMessageCopy"
            @recall="handleMessageRecall"
            @edit="handleMessageEdit"
            @reply="handleMessageReply"
            @forward="handleMessageForward"
            @favorite="handleMessageFavorite"
            @select="handleMessageSelect"
            @more="handleMessageMore"
          />
        </div>

        <!-- 输入区域 -->
        <div v-if="activeSession" class="dt-chat__input">
          <!-- 工具栏 -->
          <div class="dt-chat__toolbar">
            <div class="dt-chat__tool-btn" @click="showEmojiPicker = !showEmojiPicker">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="dt-chat__tool-btn" @click="handleFileUpload">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="dt-chat__tool-btn" @click="handleScreenshot">
              <el-icon><Crop /></el-icon>
            </div>
            <div class="dt-chat__tool-btn" @click="handleAtMention">
              <el-icon><At /></el-icon>
            </div>
            <div class="dt-chat__tool-btn" @click="handleHistorySearch">
              <el-icon><Search /></el-icon>
            </div>
            <!-- 语音录制（内联组件） -->
            <VoiceRecorder @send="handleVoiceSend" />
          </div>

          <!-- 输入框 -->
          <textarea
            ref="textareaRef"
            v-model="messageInput"
            class="dt-chat__textarea"
            placeholder="输入消息，Enter 发送，Shift + Enter 换行"
            rows="1"
            @keydown="handleKeyDown"
            @input="handleInput"
          ></textarea>

          <!-- 底部操作栏 -->
          <div class="dt-chat__footer">
            <span class="dt-chat__tip">按 Enter 发送</span>
            <button class="dt-chat__send-btn" :disabled="!canSend" @click="handleSend">发送</button>
          </div>
        </div>

        <!-- 表情选择器 -->
        <transition name="slide-up">
          <EmojiPicker
            v-if="showEmojiPicker"
            v-click-outside="() => (showEmojiPicker = false)"
            @select="handleEmojiSelect"
          />
        </transition>
      </div>

      <!-- 其他导航内容区域 -->
      <div v-else class="dt-content-placeholder">
        <!-- 工作台子标签 -->
        <div v-if="activeNav === 'workbench'" class="workbench-container">
          <div class="workbench-tabs">
            <div
              v-for="tab in workbenchTabs"
              :key="tab.key"
              class="workbench-tab"
              :class="{ active: workbenchTab === tab.key }"
              @click="workbenchTab = tab.key"
            >
              <el-icon><component :is="tab.icon" /></el-icon>
              <span>{{ tab.label }}</span>
            </div>
          </div>
          <div class="workbench-content">
            <component :is="getCurrentNavComponent()" :user-id="currentUserId" />
          </div>
        </div>
        <!-- 其他导航内容 -->
        <component
          :is="getCurrentNavComponent()"
          v-else
          v-bind="getDocumentProps()"
          @open-document="handleOpenDocument"
          @document-created="handleDocumentCreated"
          @back="handleDocumentBack"
          @save="handleDocumentSave"
        />
      </div>
    </main>

    <!-- 群组成员抽屉 -->
    <el-drawer v-model="showMembersDrawer" title="群组成员" direction="rtl" size="380px">
      <GroupMembers :session="activeSession" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Plus,
  Notification,
  Document,
  Picture,
  Microphone,
  Top,
  Bell,
  Phone,
  VideoCamera,
  MoreFilled,
  ChatDotRound,
  Folder,
  Crop,
  At,
  ChatDotRound as MessageIcon,
  SetUp,
  MessageBox,
  Calendar,
  FolderOpen,
  User,
  Grid,
  OfficeBuilding,
  Check,
  Money,
} from '@element-plus/icons-vue'
import DingMessageBubble from '@/components/Message/DingMessageBubble.vue'
import EmojiPicker from '@/components/Chat/EmojiPicker.vue'
import GroupMembers from '@/components/Chat/GroupMembers.vue'
import VoiceRecorder from '@/components/Chat/VoiceRecorder.vue'
import { formatTime } from '@/utils/format/time.js'

// 工作台组件
import Attendance from '@/views/im/workbench/Attendance.vue'
import Schedule from '@/views/im/workbench/Schedule.vue'
import Report from '@/views/im/workbench/Report.vue'
import Approval from '@/views/im/workbench/Approval.vue'

// 文档模块组件
import DocumentList from '@/views/im/document/DocumentList.vue'
import DocumentEditor from '@/views/im/document/DocumentEditor.vue'

// Props
const props = defineProps({
  currentUser: {
    type: Object,
    default: () => ({}),
  },
  sessions: {
    type: Array,
    default: () => [],
  },
  messages: {
    type: Array,
    default: () => [],
  },
})

// Emits
const emit = defineEmits(['session-click', 'send-message', 'message-action', 'nav-change'])

// State
const activeNav = ref('message')
const searchKeyword = ref('')
const activeTab = ref('all')
const activeSessionId = ref(null)
const currentUserId = ref(props.currentUser?.id || null)
const messageInput = ref('')
const hoveredMessageId = ref(null)
const showEmojiPicker = ref(false)
const showMembersDrawer = ref(false)
const isOnline = ref(true)
const showVoiceRecorder = ref(true) // 显示语音录制组件（内联模式）

// 工作台子标签
const workbenchTab = ref('attendance')

// 工作台标签列表
const workbenchTabs = ref([
  { key: 'attendance', label: '考勤', icon: OfficeBuilding },
  { key: 'schedule', label: '日程', icon: Calendar },
  { key: 'report', label: '报告', icon: Document },
  { key: 'approval', label: '审批', icon: Check },
])

// 文档模块状态
const documentView = ref('list') // 'list' | 'editor'
const currentDocument = ref(null)

// Refs
const sessionListRef = ref(null)
const messagesRef = ref(null)
const textareaRef = ref(null)

// 导航项（钉钉6.5完整导航）
const navItems = ref([
  { key: 'message', label: '消息', icon: MessageIcon, unread: 5 },
  { key: 'ding', label: 'DING', icon: MessageBox, unread: 2 },
  { key: 'workbench', label: '工作台', icon: Grid, unread: 0 },
  { key: 'document', label: '文档', icon: Document, unread: 0 },
  { key: 'contacts', label: '通讯录', icon: User, unread: 0 },
  { key: 'mail', label: '邮箱', icon: FolderOpen, unread: 0 },
  { key: 'apps', label: '应用中心', icon: Grid, unread: 0 },
  { key: 'settings', label: '设置', icon: SetUp, unread: 0 },
])

// 会话标签
const sessionTabs = ref([
  { key: 'all', label: '全部', unread: 0 },
  { key: 'unread', label: '未读', unread: 5 },
  { key: 'pinned', label: '置顶', unread: 0 },
  { key: 'group', label: '群聊', unread: 0 },
])

// 计算属性
const currentNavTitle = computed(() => {
  const item = navItems.value.find(n => n.key === activeNav.value)
  return item ? item.label : ''
})

const filteredSessions = computed(() => {
  let result = props.sessions

  // 标签过滤
  if (activeTab.value === 'unread') {
    result = result.filter(s => s.unread > 0)
  } else if (activeTab.value === 'pinned') {
    result = result.filter(s => s.pinned)
  } else if (activeTab.value === 'group') {
    result = result.filter(s => s.isGroup)
  }

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      s =>
        s.name?.toLowerCase().includes(keyword) ||
        s.lastMessage?.text?.toLowerCase().includes(keyword)
    )
  }

  return result
})

const activeSession = computed(() => {
  return props.sessions.find(s => s.id === activeSessionId.value) || null
})

const displayMessages = computed(() => {
  if (!activeSessionId.value) return []
  return props.messages.filter(m => m.sessionId === activeSessionId.value)
})

const timeDividers = computed(() => {
  const dividers = []
  let lastDate = null

  displayMessages.value.forEach((msg, index) => {
    const msgDate = new Date(msg.createTime)
    if (!lastDate || isDifferentDay(msgDate, lastDate)) {
      dividers.push(formatDateDivider(msgDate))
      lastDate = msgDate
    }
  })

  return dividers
})

const canSend = computed(() => {
  return messageInput.value.trim().length > 0
})

// 方法
const handleNavClick = item => {
  activeNav.value = item.key
  emit('nav-change', item.key)
}

const handleProfileClick = () => {
  activeNav.value = 'settings'
  emit('nav-change', 'settings')
}

const handleSearch = () => {
  // 搜索会话
}

const handleAddChat = () => {
  // 新建聊天
}

const handleTabClick = tab => {
  activeTab.value = tab.key
}

const handleSessionClick = session => {
  activeSessionId.value = session.id
  emit('session-click', session)
  nextTick(() => {
    scrollToBottom()
  })
}

const handleSessionContextMenu = (e, session) => {
  // 显示右键菜单
}

const handlePhoneCall = () => {
  // 语音通话
}

const handleVideoCall = () => {
  // 视频会议
}

const handleMoreAction = () => {
  // 更多操作
}

// 文档模块事件处理
const handleOpenDocument = doc => {
  currentDocument.value = doc
  documentView.value = 'editor'
}

const handleDocumentBack = () => {
  documentView.value = 'list'
}

const handleDocumentCreated = doc => {
  currentDocument.value = doc
  documentView.value = 'editor'
}

const handleDocumentSave = data => {
  // TODO: 保存文档到服务器
  console.log('Document saved:', data)
}

const getDocumentProps = () => {
  if (activeNav.value === 'document') {
    if (documentView.value === 'editor' && currentDocument.value) {
      return {
        documentId: currentDocument.value.id,
        document: currentDocument.value,
        userId: currentUserId.value,
      }
    }
    return {
      userId: currentUserId.value,
    }
  }
  return {}
}

const getCurrentNavComponent = () => {
  // 返回当前导航对应的组件
  if (activeNav.value === 'workbench') {
    switch (workbenchTab.value) {
      case 'attendance':
        return Attendance
      case 'schedule':
        return Schedule
      case 'report':
        return Report
      case 'approval':
        return Approval
      default:
        return Attendance
    }
  }

  // DING 模块占位
  if (activeNav.value === 'ding') {
    return {
      template: `
        <div class="ding-placeholder">
          <el-empty description="DING 消息中心开发中">
            <template #image>
              <el-icon :size="80"><MessageBox /></el-icon>
            </template>
          </el-empty>
        </div>
      `,
    }
  }

  // 文档模块
  if (activeNav.value === 'document') {
    if (documentView.value === 'editor' && currentDocument.value) {
      return DocumentEditor
    }
    return DocumentList
  }

  // 通讯录模块占位
  if (activeNav.value === 'contacts') {
    return {
      template: `
        <div class="contacts-placeholder">
          <el-empty description="通讯录功能开发中">
            <template #image>
              <el-icon :size="80"><User /></el-icon>
            </template>
          </el-empty>
        </div>
      `,
    }
  }

  // 邮箱模块占位
  if (activeNav.value === 'mail') {
    return {
      template: `
        <div class="mail-placeholder">
          <el-empty description="邮箱功能开发中">
            <template #image>
              <el-icon :size="80"><FolderOpen /></el-icon>
            </template>
          </el-empty>
        </div>
      `,
    }
  }

  // 应用中心占位
  if (activeNav.value === 'apps') {
    return {
      template: `
        <div class="apps-placeholder">
          <el-empty description="应用中心开发中">
            <template #image>
              <el-icon :size="80"><Grid /></el-icon>
            </template>
          </el-empty>
        </div>
      `,
    }
  }

  // 设置模块占位
  if (activeNav.value === 'settings') {
    return {
      template: `
        <div class="settings-placeholder">
          <el-empty description="设置功能开发中">
            <template #image>
              <el-icon :size="80"><SetUp /></el-icon>
            </template>
          </el-empty>
        </div>
      `,
    }
  }

  return null
}

const shouldShowTime = message => {
  const index = displayMessages.value.findIndex(m => m.id === message.id)
  if (index === 0) return true
  const prevMessage = displayMessages.value[index - 1]
  if (!prevMessage) return true
  const currentTime = new Date(message.createTime).getTime()
  const prevTime = new Date(prevMessage.createTime).getTime()
  return currentTime - prevTime > 5 * 60 * 1000 // 5分钟
}

const handleInput = () => {
  autoResizeTextarea()
}

const handleKeyDown = e => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

const handleSend = () => {
  if (!canSend.value) return

  emit('send-message', {
    sessionId: activeSessionId.value,
    content: messageInput.value.trim(),
    type: 'text',
  })

  messageInput.value = ''
  nextTick(() => {
    autoResizeTextarea()
    scrollToBottom()
  })
}

const handleEmojiSelect = emoji => {
  messageInput.value += emoji
  textareaRef.value?.focus()
}

const handleFileUpload = () => {
  // 文件上传
}

const handleScreenshot = () => {
  // 截图
}

const handleAtMention = () => {
  // @提及
}

const handleHistorySearch = () => {
  // 历史搜索
}

// 处理语音发送
const handleVoiceSend = async voiceData => {
  // 上传语音文件
  try {
    const formData = new FormData()
    formData.append(
      'file',
      voiceData.blob,
      `voice_${Date.now()}.${voiceData.type.split('/')[1] || 'webm'}`
    )
    formData.append('type', 'voice')
    formData.append('duration', voiceData.duration)

    // 调用文件上传API
    // const response = await uploadFile(formData)
    // const fileUrl = response.data.url

    // 发送语音消息
    emit('send-message', {
      sessionId: activeSessionId.value,
      content: voiceData.url, // 临时使用本地URL
      type: 'voice',
      duration: voiceData.duration,
      fileUrl: voiceData.url,
    })
  } catch (error) {
    console.error('语音发送失败:', error)
    ElMessage.error('语音发送失败')
  }
}

const handleVoiceRecord = () => {
  // 语音录制已通过 VoiceRecorder 组件处理
  console.log('语音录制功能已集成')
}

const handleMessageClick = () => {}

const handleMessageRetry = message => {
  emit('message-action', { type: 'retry', message })
}

const handleMessageCopy = message => {
  emit('message-action', { type: 'copy', message })
}

const handleMessageRecall = message => {
  emit('message-action', { type: 'recall', message })
}

const handleMessageEdit = message => {
  emit('message-action', { type: 'edit', message })
}

const handleMessageReply = message => {
  emit('message-action', { type: 'reply', message })
}

const handleMessageForward = message => {
  emit('message-action', { type: 'forward', message })
}

const handleMessageFavorite = message => {
  emit('message-action', { type: 'favorite', message })
}

const handleMessageSelect = message => {
  emit('message-action', { type: 'select', message })
}

const handleMessageMore = data => {
  emit('message-action', data)
}

const autoResizeTextarea = () => {
  const textarea = textareaRef.value
  if (!textarea) return
  textarea.style.height = 'auto'
  textarea.style.height = Math.min(textarea.scrollHeight, 200) + 'px'
}

const scrollToBottom = () => {
  nextTick(() => {
    const container = messagesRef.value
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })
}

const isDifferentDay = (date1, date2) => {
  return (
    date1.getFullYear() !== date2.getFullYear() ||
    date1.getMonth() !== date2.getMonth() ||
    date1.getDate() !== date2.getDate()
  )
}

const formatDateDivider = date => {
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (isDifferentDay(date, today)) {
    if (isDifferentDay(date, yesterday)) {
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return weekdays[date.getDay()]
    } else {
      return '昨天'
    }
  }
  return ''
}

// 监听会话变化
watch(activeSessionId, () => {
  nextTick(() => {
    scrollToBottom()
  })
})

onMounted(() => {
  // 初始化
})
</script>

<style lang="scss" scoped>
@import '@/styles/dingtalk-6.5/index.scss';

// 68px导航栏样式覆盖
.dt-sidebar {
  width: 68px !important;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0;

  &__logo {
    width: 40px;
    height: 40px;
    margin-bottom: 12px;
    cursor: pointer;

    .ding-logo {
      width: 100%;
      height: 100%;
    }
  }

  &__nav {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    width: 100%;
  }

  &__item {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    color: var(--dt-text-secondary);

    &:hover {
      background: rgba(0, 0, 0, 0.04);
      color: var(--dt-text-primary);
    }

    &.active {
      background: rgba(22, 119, 255, 0.1);
      color: var(--dt-primary);

      &::after {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 24px;
        background: var(--dt-primary);
        border-radius: 0 2px 2px 0;
      }
    }
  }

  &__badge {
    position: absolute;
    top: 4px;
    right: 4px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    background: #ff4d4f;
    color: white;
    font-size: 10px;
    font-weight: 500;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
  }

  &__bottom {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 8px;
    border-top: 1px solid var(--dt-border-lighter);
  }

  &__avatar {
    position: relative;
    width: 36px;
    height: 36px;
    cursor: pointer;

    .el-avatar {
      width: 100%;
      height: 100%;
    }

    .online-indicator {
      position: absolute;
      bottom: -1px;
      right: -1px;
      width: 10px;
      height: 10px;
      border-radius: 50%;
      border: 2px solid white;

      &.online {
        background: #52c41a;
      }

      &.offline {
        background: #999999;
      }
    }
  }
}

// 320px会话列表
.dt-sessions {
  width: 320px;
}

// 在线状态
.online-status {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid white;

  &.online {
    background: #52c41a;
  }

  &.offline {
    background: #999999;
  }

  &.busy {
    background: #ff4d4f;
  }
}

// 内容占位区
.dt-content-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--dt-text-secondary);
}

// 工作台容器
.workbench-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
}

.workbench-tabs {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid var(--dt-border-light);
}

.workbench-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--dt-text-secondary);
  font-size: 14px;

  &:hover {
    background: var(--dt-fill-light);
  }

  &.active {
    background: var(--dt-primary-light-9);
    color: var(--dt-primary);
    font-weight: 500;
  }
}

.workbench-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

// 过渡动画
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.2s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
