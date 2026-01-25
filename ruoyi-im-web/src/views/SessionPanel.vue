<template>
  <div class="session-panel">
    <!-- 头部 -->
    <div class="panel-header">
      <h1 class="panel-title">
        <span class="material-icons-outlined title-icon">chat</span>
        消息
      </h1>
      <el-dropdown trigger="click" @command="handleCommand" placement="bottom-start">
        <button class="add-btn">
          <span class="material-icons-outlined">add</span>
        </button>
        <template #dropdown>
          <el-dropdown-menu class="action-dropdown">
            <el-dropdown-item command="chat">
              <span class="material-icons-outlined item-icon">chat</span>
              发起单聊
            </el-dropdown-item>
            <el-dropdown-item command="group">
              <span class="material-icons-outlined item-icon">group_add</span>
              创建群组
            </el-dropdown-item>
            <el-dropdown-item command="join">
              <span class="material-icons-outlined item-icon">search</span>
              加入群组
            </el-dropdown-item>
            <el-dropdown-item divided command="contacts">
              <span class="material-icons-outlined item-icon">person_add</span>
              添加好友
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 搜索 -->
    <div class="search-section">
      <div class="search-container">
        <span class="material-icons-outlined search-icon">search</span>
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索联系人、群组、消息..."
          type="text"
          @focus="showGlobalSearch = true"
        />
        <span v-if="searchKeyword" class="clear-btn" @click="searchKeyword = ''">
          <span class="material-icons-outlined">close</span>
        </span>
      </div>
      <!-- 全局搜索面板 -->
      <GlobalSearch
        v-model:visible="showGlobalSearch"
        :keyword="searchKeyword"
        @select="handleSearchSelect"
      />
    </div>

    <!-- 会话列表 -->
    <div v-loading="loading" class="session-list">
      <div
        v-for="session in sortedSessions"
        :key="session.id"
        class="session-item"
        :class="{
          active: isActiveSession(session),
          pinned: session.isPinned,
          unread: session.unreadCount > 0
        }"
        @click="handleSessionClick(session)"
        @contextmenu.prevent="handleContextMenu($event, session)"
      >
        <!-- 活跃指示条 -->
        <div class="active-indicator"></div>

        <div class="avatar-wrapper">
          <!-- 群组头像 -->
          <template v-if="session.type === 'GROUP'">
            <div class="session-avatar group-avatar">
              <span class="material-icons-outlined">groups</span>
            </div>
          </template>
          <!-- 单聊头像 -->
          <DingtalkAvatar
            v-else
            :name="session.name"
            :user-id="session.targetId"
            :size="46"
            shape="square"
            custom-class="session-avatar"
            @click="handleAvatarClick($event, session)"
          />
          <!-- 在线状态点 -->
          <span
            v-if="session.type === 'PRIVATE'"
            class="status-dot"
            :class="{ online: isUserOnline(session.targetId) }"
          ></span>
          <!-- 未读角标 -->
          <span
            v-if="session.unreadCount > 0"
            class="unread-badge"
            :class="{ 'badge-dot': session.isMuted }"
          >
            {{ session.isMuted ? '' : (session.unreadCount > 99 ? '99+' : session.unreadCount) }}
          </span>
          <!-- 置顶标记 -->
          <span v-if="session.isPinned && !isActiveSession(session)" class="pin-indicator">
            <span class="material-icons-outlined">push_pin</span>
          </span>
        </div>

        <div class="session-info">
          <div class="session-header">
            <div class="session-name-group">
              <span class="session-name">{{ session.name }}</span>
              <span v-if="session.isMuted" class="mute-icon">
                <span class="material-icons-outlined">notifications_off</span>
              </span>
            </div>
            <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
          </div>
          <div class="session-preview">
            <span v-if="session.hasMention" class="mention-tag">@</span>
            <span v-if="session.lastSenderNickname && session.type === 'GROUP'" class="sender-name">
              {{ session.lastSenderNickname }}:
            </span>
            <span class="preview-text">{{ session.lastMessage || '暂无消息' }}</span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && sessions.length === 0" class="empty-state">
        <div class="empty-illustration">
          <span class="material-icons-outlined empty-icon">forum</span>
          <div class="empty-decoration"></div>
        </div>
        <h3 class="empty-title">暂无会话</h3>
        <p class="empty-text">点击上方 + 发起你的第一个聊天</p>
      </div>
    </div>

    <!-- 右键菜单 -->
    <div v-if="contextMenu.show" class="context-menu" :style="contextMenuStyle">
      <div class="menu-item" @click="handleMarkAsRead">
        <span class="material-icons-outlined item-icon">done_all</span>
        标记已读
      </div>
      <div class="menu-item" @click="handleTogglePin">
        <span class="material-icons-outlined item-icon">
          {{ contextMenu.session?.isPinned ? 'push_pin' : 'push_pin' }}
        </span>
        {{ contextMenu.session?.isPinned ? '取消置顶' : '置顶会话' }}
      </div>
      <div class="menu-item" @click="handleToggleMute">
        <span class="material-icons-outlined item-icon">
          {{ contextMenu.session?.isMuted ? 'notifications' : 'notifications_off' }}
        </span>
        {{ contextMenu.session?.isMuted ? '取消免打扰' : '消息免打扰' }}
      </div>
      <div class="menu-divider"></div>
      <div class="menu-item danger" @click="handleDeleteSession">
        <span class="material-icons-outlined item-icon">delete</span>
        删除会话
      </div>
    </div>

    <!-- 创建群组对话框 -->
    <CreateGroupDialog
      v-model="showCreateGroupDialog"
      @success="handleGroupCreated"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, reactive } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import CreateGroupDialog from '@/components/CreateGroupDialog/index.vue'
import GlobalSearch from '@/components/Chat/GlobalSearch.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  currentSession: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['select-session', 'show-user'])
const store = useStore()

const searchKeyword = ref('')
const showCreateGroupDialog = ref(false)
const showGlobalSearch = ref(false)

const sessions = computed(() => store.state.im.session?.sessions || [])
const loading = computed(() => store.state.im.session?.loading || false)
const userStatus = computed(() => store.state.im.contact?.userStatus || {})

// 判断用户是否在线
const isUserOnline = (userId) => {
  return userStatus.value[userId] === 'online'
}

// 处理头像点击
const handleAvatarClick = (e, session) => {
  e.stopPropagation()
  if (session.type === 'PRIVATE') {
    emit('show-user', session.targetId)
  }
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'group') {
    showCreateGroupDialog.value = true
  } else if (command === 'chat') {
    ElMessage.info('发起单聊请前往联系人页面选择好友')
  } else if (command === 'join') {
    ElMessage.info('加入群组功能开发中')
  } else if (command === 'contacts') {
    ElMessage.info('添加好友功能开发中')
  }
}

// 群组创建成功
const handleGroupCreated = () => {
  ElMessage.success('群组创建成功')
  store.dispatch('im/session/loadSessions')
}

// 处理搜索选择
const handleSearchSelect = (item) => {
  if (typeof item === 'string') {
    searchKeyword.value = item
  } else if (item.id && item.conversationId) {
    const session = sessions.value.find(s => s.id === item.conversationId)
    if (session) emit('select-session', session)
  }
}

// 判断是否为当前会话
const isActiveSession = (session) => {
  return props.currentSession?.id === session.id
}

// 处理会话点击
const handleSessionClick = (session) => {
  emit('select-session', session)
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''

  const date = new Date(timestamp)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 86400000)

  if (date >= today) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  if (date >= yesterday) {
    return '昨天'
  }
  if (date.getFullYear() === now.getFullYear()) {
    return `${date.getMonth() + 1}/${date.getDate()}`
  }
  return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`
}

// 右键菜单状态
const contextMenu = reactive({
  show: false,
  x: 0,
  y: 0,
  session: null
})

const contextMenuStyle = computed(() => ({
  left: `${contextMenu.x}px`,
  top: `${contextMenu.y}px`
}))

const handleContextMenu = (e, session) => {
  contextMenu.show = true
  contextMenu.x = e.clientX
  contextMenu.y = e.clientY
  contextMenu.session = session
}

const hideContextMenu = () => {
  contextMenu.show = false
}

// 会话操作
const handleMarkAsRead = async () => {
  if (!contextMenu.session) return
  await store.dispatch('im/session/markSessionAsRead', contextMenu.session.id)
  hideContextMenu()
}

const handleTogglePin = async () => {
  if (!contextMenu.session) return
  const pinned = !contextMenu.session.isPinned
  await store.dispatch('im/session/pinSession', { sessionId: contextMenu.session.id, pinned })
  ElMessage.success(pinned ? '已置顶' : '已取消置顶')
  hideContextMenu()
}

const handleToggleMute = async () => {
  if (!contextMenu.session) return
  const muted = !contextMenu.session.isMuted
  await store.dispatch('im/session/muteSession', { sessionId: contextMenu.session.id, muted })
  ElMessage.success(muted ? '已开启免打扰' : '已关闭免打扰')
  hideContextMenu()
}

const handleDeleteSession = () => {
  if (!contextMenu.session) return
  ElMessageBox.confirm('确定要删除该会话吗？历史消息将保留。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await store.dispatch('im/session/deleteSession', contextMenu.session.id)
    ElMessage.success('已删除')
    hideContextMenu()
  }).catch(() => {})
}

// 排序后的会话列表
const sortedSessions = computed(() => store.getters['im/session/sortedSessions'])

onMounted(() => {
  if (sessions.value.length === 0) {
    store.dispatch('im/session/loadSessions')
  }
  window.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  window.removeEventListener('click', hideContextMenu)
})
</script>

<style scoped lang="scss">
// ============================================================================
// 容器
// ============================================================================
.session-panel {
  display: flex;
  flex-direction: column;
  width: 280px;
  flex-shrink: 0;
  border-right: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  height: 100%;
}

// ============================================================================
// 头部
// ============================================================================
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
  flex-shrink: 0;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;

  .title-icon {
    font-size: 20px;
    color: var(--dt-brand-color);
  }
}

.add-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  color: var(--dt-text-secondary);
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-body);
    color: var(--dt-brand-color);
  }

  .material-icons-outlined {
    font-size: 20px;
  }
}

// ============================================================================
// 搜索
// ============================================================================
.search-section {
  padding: 0 12px 12px;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
  height: 36px;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-full);
  padding: 0 12px;
  transition: all var(--dt-transition-base);

  &:focus-within {
    background: var(--dt-bg-card);
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 2px var(--dt-brand-lighter);
  }
}

.search-icon {
  font-size: 18px;
  color: var(--dt-text-quaternary);
  pointer-events: none;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: var(--dt-text-primary);
  padding: 0 8px;

  &::placeholder {
    color: var(--dt-text-quaternary);
  }
}

.clear-btn {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  color: var(--dt-text-quaternary);
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-border-color);
    color: var(--dt-text-secondary);
  }

  .material-icons-outlined {
    font-size: 14px;
  }
}

// ============================================================================
// 会话列表
// ============================================================================
.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: transparent;
    border-radius: 2px;
  }

  &:hover::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
  }

  .dark &:hover::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.1);
  }
}

.session-item {
  position: relative;
  display: flex;
  padding: 10px 16px;
  cursor: pointer;
  gap: 12px;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  &.active {
    background: var(--dt-bg-session-active);

    .active-indicator {
      opacity: 1;
    }

    .session-name {
      color: var(--dt-text-primary);
      font-weight: 600;
    }
  }

  &.pinned {
    background: var(--dt-bg-body);
  }

  &.pinned.active {
    background: var(--dt-bg-session-active);
  }
}

.active-indicator {
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 3px;
  background: var(--dt-brand-color);
  border-radius: 0 4px 4px 0;
  opacity: 0;
  transition: opacity var(--dt-transition-base);
}

// 头像区域
.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.session-avatar {
  width: 46px;
  height: 46px;
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;

  &.group-avatar {
    background: linear-gradient(135deg, #1677ff 0%, #0e5fd9 100%);
    color: #fff;
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);

    .material-icons-outlined {
      font-size: 22px;
    }
  }

  :deep(.dingtalk-avatar) {
    border-radius: var(--dt-radius-lg) !important;
  }
}

.status-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 10px;
  height: 10px;
  background: var(--dt-text-quaternary);
  border: 2px solid var(--dt-bg-card);
  border-radius: 50%;
  transition: all var(--dt-transition-base);

  &.online {
    background: var(--dt-success-color);
    box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.2);
  }
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: var(--dt-error-color);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  border-radius: var(--dt-radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--dt-bg-card);
  box-shadow: 0 2px 6px rgba(255, 77, 79, 0.3);

  &.badge-dot {
    min-width: 8px;
    width: 8px;
    height: 8px;
    padding: 0;
    top: 0;
    right: 0;
  }
}

.pin-indicator {
  position: absolute;
  bottom: -2px;
  left: -2px;
  width: 14px;
  height: 14px;
  background: var(--dt-warning-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--dt-bg-card);

  .material-icons-outlined {
    font-size: 10px;
    color: #fff;
  }
}

// 会话信息
.session-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 3px;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-name-group {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.session-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all var(--dt-transition-base);
}

.mute-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;

  .material-icons-outlined {
    font-size: 14px;
    color: var(--dt-text-quaternary);
  }
}

.session-time {
  font-size: 11px;
  color: var(--dt-text-quaternary);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', sans-serif;
  flex-shrink: 0;
  margin-left: 8px;
}

.session-preview {
  display: flex;
  align-items: center;
  gap: 2px;
}

.mention-tag {
  color: var(--dt-error-color);
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.sender-name {
  font-size: 12px;
  color: var(--dt-text-secondary);
  flex-shrink: 0;
}

.preview-text {
  font-size: 13px;
  color: var(--dt-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

// ============================================================================
// 空状态
// ============================================================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-illustration {
  position: relative;
  margin-bottom: 20px;
}

.empty-icon {
  font-size: 64px;
  color: var(--dt-border-color);
}

.empty-decoration {
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 48px;
  height: 4px;
  background: var(--dt-border-color);
  border-radius: var(--dt-radius-full);
  opacity: 0.5;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 6px 0;
}

.empty-text {
  font-size: 13px;
  color: var(--dt-text-tertiary);
  margin: 0;
}

// ============================================================================
// 右键菜单
// ============================================================================
.context-menu {
  position: fixed;
  background: var(--dt-bg-dropdown);
  backdrop-filter: blur(12px);
  border-radius: var(--dt-radius-lg);
  box-shadow: var(--dt-shadow-float);
  padding: 6px;
  min-width: 160px;
  z-index: 2000;
  border: 1px solid var(--dt-border-light);

  .menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 9px 10px;
    font-size: 13px;
    color: var(--dt-text-primary);
    cursor: pointer;
    border-radius: var(--dt-radius-md);
    transition: all var(--dt-transition-base);
    font-weight: 500;

    .item-icon {
      font-size: 16px;
      color: var(--dt-text-secondary);
    }

    &:hover {
      background: var(--dt-bg-body);
      color: var(--dt-brand-color);

      .item-icon {
        color: var(--dt-brand-color);
      }
    }

    &.danger {
      color: var(--dt-error-color);

      .item-icon {
        color: var(--dt-error-color);
      }

      &:hover {
        background: var(--dt-error-bg);
        color: var(--dt-error-color);
      }
    }
  }

  .menu-divider {
    height: 1px;
    background: var(--dt-border-light);
    margin: 4px 6px;
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark .session-panel {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .panel-title {
  color: var(--dt-text-primary-dark);
}

.dark .add-btn {
  color: var(--dt-text-secondary-dark);

  &:hover {
    background: var(--dt-bg-hover-dark);
  }
}

.dark .search-container {
  background: var(--dt-bg-input-dark);
  border-color: var(--dt-border-dark);

  &:focus-within {
    background: var(--dt-bg-hover-dark);
    border-color: var(--dt-brand-color);
  }
}

.dark .search-input {
  color: var(--dt-text-primary-dark);

  &::placeholder {
    color: var(--dt-text-quaternary-dark);
  }
}

.dark .session-item {
  &:hover {
    background: var(--dt-bg-hover-dark);
  }

  &.active {
    background: var(--dt-bg-active-dark);

    .session-name {
      color: var(--dt-text-primary-dark);
    }
  }

  &.pinned {
    background: transparent;
  }
}

.dark .session-name {
  color: var(--dt-text-secondary-dark);
}

.dark .session-time {
  color: var(--dt-text-quaternary-dark);
}

.dark .preview-text {
  color: var(--dt-text-tertiary-dark);
}

.dark .status-dot {
  border-color: var(--dt-bg-card-dark);
}

.dark .unread-badge {
  border-color: var(--dt-bg-card-dark);
}

.dark .context-menu {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);

  .menu-item {
    color: var(--dt-text-primary-dark);

    .item-icon {
      color: var(--dt-text-secondary-dark);
    }

    &:hover {
      background: var(--dt-bg-hover-dark);
    }
  }

  .menu-divider {
    background: var(--dt-border-dark);
  }
}

.dark .empty-icon {
  color: var(--dt-border-dark);
}

.dark .empty-decoration {
  background: var(--dt-border-dark);
}

// ============================================================================
// Element Plus 下拉菜单样式
// ============================================================================
:deep(.action-dropdown) {
  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    font-size: 13px;

    .item-icon {
      font-size: 16px;
      color: var(--dt-text-secondary);
    }
  }
}
</style>
