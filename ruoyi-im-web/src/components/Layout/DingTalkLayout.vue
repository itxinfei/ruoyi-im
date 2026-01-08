<template>
  <div class="dt-layout">
    <!-- 左侧导航栏 -->
    <aside class="dt-sidebar" :class="{ expanded: sidebarExpanded }">
      <!-- Logo -->
      <div class="dt-sidebar__logo" @click="handleLogoClick">
        <span v-if="!sidebarExpanded">钉</span>
        <span v-else>钉钉IM</span>
      </div>

      <!-- 导航菜单 -->
      <nav class="dt-sidebar__nav">
        <div
          v-for="item in navItems"
          :key="item.key"
          class="dt-sidebar__item"
          :class="{ active: activeNav === item.key }"
          @click="handleNavClick(item)"
        >
          <el-icon :size="20">
            <component :is="item.icon" />
          </el-icon>
          <span v-if="sidebarExpanded" class="label">{{ item.label }}</span>
          <span v-if="item.badge && !sidebarExpanded" class="badge">{{ item.badge }}</span>
          <!-- 未读角标 -->
          <el-badge
            v-if="item.unread > 0 && sidebarExpanded"
            :value="item.unread"
            :max="99"
            class="nav-badge"
          />
        </div>
      </nav>

      <!-- 底部用户区 -->
      <div class="dt-sidebar__bottom">
        <div class="dt-sidebar__avatar" @click="handleProfileClick">
          <el-avatar :size="40" :src="currentUser.avatar">
            {{ currentUser.nickname?.charAt(0) || '?' }}
          </el-avatar>
        </div>
        <div v-if="sidebarExpanded" class="dt-sidebar__username">
          {{ currentUser.nickname || '未登录' }}
        </div>
      </div>
    </aside>

    <!-- 中间会话列表 -->
    <section class="dt-sessions">
      <!-- 搜索头 -->
      <header class="dt-sessions__header">
        <div class="dt-sessions__search">
          <el-icon><Search /></el-icon>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索"
            @input="handleSearch"
          />
        </div>
        <div class="dt-sessions__actions">
          <el-button @click="handleAddChat" title="新建聊天">
            <el-icon><Plus /></el-icon>
          </el-button>
          <el-button @click="toggleSidebarExpanded" title="展开/收起">
            <el-icon><Expand v-if="!sidebarExpanded" /><Fold v-else /></el-icon>
          </el-button>
        </div>
      </header>

      <!-- 会话标签 -->
      <div class="dt-sessions__tabs">
        <div
          v-for="tab in sessionTabs"
          :key="tab.key"
          class="dt-sessions__tab"
          :class="{ active: activeTab === tab.key }"
          @click="handleTabClick(tab)"
        >
          {{ tab.label }}
          <span v-if="tab.unread > 0" class="badge">{{ tab.unread }}</span>
        </div>
      </div>

      <!-- 会话列表 -->
      <div class="dt-sessions__list" ref="sessionListRef">
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
            <el-avatar :size="48" :src="session.avatar">
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
            <el-icon v-if="session.pinned" class="pin-icon" color="#ff6600">
              <Top />
            </el-icon>
            <!-- 未读数 -->
            <span v-if="session.unread > 0" class="dt-sessions__unread" :class="{ dot: session.unread === 1 }">
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

    <!-- 右侧聊天区域 -->
    <main class="dt-chat">
      <!-- 聊天头 -->
      <header v-if="activeSession" class="dt-chat__header">
        <div class="dt-chat__title">
          <span class="name">{{ activeSession.name }}</span>
          <span v-if="activeSession.isGroup" class="members" @click="showMembersDrawer = true">
            ({{ activeSession.memberCount || 0 }}人)
          </span>
        </div>
        <div class="dt-chat__actions">
          <el-button @click="handlePhoneCall" title="语音通话">
            <el-icon><Phone /></el-icon>
          </el-button>
          <el-button @click="handleVideoCall" title="视频会议">
            <el-icon><VideoCamera /></el-icon>
          </el-button>
          <el-button @click="handleMoreAction" title="更多">
            <el-icon><MoreFilled /></el-icon>
          </el-button>
        </div>
      </header>

      <!-- 空状态 -->
      <div v-else class="dt-chat__empty">
        <el-empty description="选择一个会话开始聊天" />
      </div>

      <!-- 消息列表 -->
      <div v-if="activeSession" class="dt-chat__messages" ref="messagesRef">
        <!-- 时间分隔符 -->
        <div v-for="(divider, index) in timeDividers" :key="'divider-' + index" class="dt-chat__time-divider">
          {{ divider }}
        </div>

        <!-- 消息列表 -->
        <MessageBubble
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
          <button
            class="dt-chat__send-btn"
            :disabled="!canSend"
            @click="handleSend"
          >
            发送
          </button>
        </div>
      </div>

      <!-- 表情选择器 -->
      <transition name="slide-up">
        <EmojiPicker
          v-if="showEmojiPicker"
          v-click-outside="() => showEmojiPicker = false"
          @select="handleEmojiSelect"
        />
      </transition>
    </main>

    <!-- 群组成员抽屉 -->
    <el-drawer
      v-model="showMembersDrawer"
      title="群组成员"
      direction="rtl"
      size="380px"
    >
      <GroupMembers :session="activeSession" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import {
  Search, Plus, Expand, Fold, Notification, Document, Picture,
  Microphone, Top, Bell, Phone, VideoCamera, MoreFilled,
  ChatDotRound, Folder, Crop, At
} from '@element-plus/icons-vue'
import MessageBubble from '@/components/Message/MessageBubble.vue'
import EmojiPicker from '@/components/Chat/EmojiPicker.vue'
import GroupMembers from '@/components/Chat/GroupMembers.vue'
import { formatTime } from '@/utils/format/time.js'

// Props
const props = defineProps({
  currentUser: {
    type: Object,
    default: () => ({})
  },
  sessions: {
    type: Array,
    default: () => []
  },
  messages: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits([
  'session-click', 'send-message', 'message-action', 'nav-change'
])

// State
const sidebarExpanded = ref(false)
const activeNav = ref('chat')
const searchKeyword = ref('')
const activeTab = ref('all')
const activeSessionId = ref(null)
const currentUserId = ref(props.currentUser?.id || null)
const messageInput = ref('')
const hoveredMessageId = ref(null)
const showEmojiPicker = ref(false)
const showMembersDrawer = ref(false)

// Refs
const sessionListRef = ref(null)
const messagesRef = ref(null)
const textareaRef = ref(null)

// 导航项
const navItems = ref([
  { key: 'chat', label: '消息', icon: 'ChatDotRound', unread: 5 },
  { key: 'contacts', label: '通讯录', icon: 'User', unread: 0 },
  { key: 'apps', label: '应用', icon: 'Grid', unread: 0 },
  { key: 'settings', label: '设置', icon: 'Setting', unread: 0 }
])

// 会话标签
const sessionTabs = ref([
  { key: 'all', label: '全部', unread: 0 },
  { key: 'unread', label: '未读', unread: 5 },
  { key: 'pinned', label: '置顶', unread: 0 },
  { key: 'group', label: '群聊', unread: 0 }
])

// 计算属性
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
    result = result.filter(s =>
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
const handleLogoClick = () => {
  // 跳转到工作台
}

const handleNavClick = (item) => {
  activeNav.value = item.key
  emit('nav-change', item.key)
}

const handleProfileClick = () => {
  // 显示用户信息
}

const handleSearch = () => {
  // 搜索会话
}

const handleAddChat = () => {
  // 新建聊天
}

const toggleSidebarExpanded = () => {
  sidebarExpanded.value = !sidebarExpanded.value
}

const handleTabClick = (tab) => {
  activeTab.value = tab.key
}

const handleSessionClick = (session) => {
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

const shouldShowTime = (message) => {
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

const handleKeyDown = (e) => {
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
    type: 'text'
  })

  messageInput.value = ''
  nextTick(() => {
    autoResizeTextarea()
    scrollToBottom()
  })
}

const handleEmojiSelect = (emoji) => {
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

const handleMessageClick = () => {}

const handleMessageRetry = (message) => {
  emit('message-action', { type: 'retry', message })
}

const handleMessageCopy = (message) => {
  emit('message-action', { type: 'copy', message })
}

const handleMessageRecall = (message) => {
  emit('message-action', { type: 'recall', message })
}

const handleMessageEdit = (message) => {
  emit('message-action', { type: 'edit', message })
}

const handleMessageReply = (message) => {
  emit('message-action', { type: 'reply', message })
}

const handleMessageForward = (message) => {
  emit('message-action', { type: 'forward', message })
}

const handleMessageFavorite = (message) => {
  emit('message-action', { type: 'favorite', message })
}

const handleMessageSelect = (message) => {
  emit('message-action', { type: 'select', message })
}

const handleMessageMore = (data) => {
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

const formatDateDivider = (date) => {
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
</style>
