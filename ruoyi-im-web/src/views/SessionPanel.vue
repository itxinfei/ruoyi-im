<template>
  <div class="session-panel">
    <!-- 头部 -->
    <div class="panel-header">
      <h1 class="panel-title">消息</h1>
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
            <el-dropdown-item divided command="manageGroups">
              <span class="material-icons-outlined item-icon">folder</span>
              管理分组
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
    <div class="session-list">
      <!-- 骨架屏加载状态 -->
      <SkeletonLoader v-if="loading && sessions.length === 0" type="session" :count="5" />

      <!-- 分组会话列表 -->
      <template v-if="!loading">
        <div
          v-for="groupItem in groupedSessions"
          :key="groupItem.group.id"
          class="session-group"
        >
          <!-- 分组标题 -->
          <div
            v-if="groupItem.group.name !== '全部消息' || customGroups.length > 0"
            class="group-header"
            @click="toggleGroupExpand(groupItem.group.id)"
          >
            <div class="group-title-row">
              <span class="material-icons-outlined group-arrow">
                {{ isGroupExpanded(groupItem.group.id) ? 'expand_more' : 'chevron_right' }}
              </span>
              <span class="group-name">{{ groupItem.group.name }}</span>
              <span class="group-count">({{ groupItem.sessions.length }})</span>
            </div>
            <!-- 分组操作按钮 -->
            <div v-if="!groupItem.group.isSystem" class="group-actions" @click.stop>
              <el-dropdown trigger="click" placement="bottom-end">
                <span class="material-icons-outlined more-icon">more_horiz</span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleRenameGroup(groupItem.group)">
                      <span class="material-icons-outlined item-icon">edit</span>
                      重命名
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleDeleteGroup(groupItem.group)">
                      <span class="material-icons-outlined item-icon danger">delete</span>
                      删除分组
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <!-- 分组内的会话列表 -->
          <Transition name="group-expand">
            <div v-show="isGroupExpanded(groupItem.group.id)" class="group-sessions">
              <TransitionGroup name="session-list">
                <div
                  v-for="session in groupItem.sessions"
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
                      :size="40"
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
                      置顶
                    </span>
                  </div>

                  <div class="session-info">
                    <div class="session-header">
                      <div class="session-name-group">
                        <span class="session-name">{{ session.name }}</span>
                        <span v-if="session.isMuted" class="mute-icon">
                          <span class="material-icons-outlined">notifications_off</span>
                        </span>
                        <!-- 草稿标识 -->
                        <span v-if="hasDraft(session.id)" class="draft-badge">
                          <span class="material-icons-outlined draft-icon">edit</span>
                          草稿
                        </span>
                      </div>
                      <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
                    </div>
                    <div class="session-preview">
                      <span v-if="getSessionStatus(session) === 'typing'" class="typing-indicator">
                        <span class="material-icons-outlined typing-icon">edit</span>
                        正在输入...
                      </span>
                      <span v-else-if="getSessionStatus(session) === 'draft'" class="draft-tag">[草稿]</span>
                      <span v-else-if="getSessionStatus(session) === 'mention'" class="mention-tag">@</span>
                      <span v-if="session.lastSenderNickname && session.type === 'GROUP' && getSessionStatus(session) === 'normal'" class="sender-name">
                        {{ session.lastSenderNickname }}:
                      </span>
                      <span class="preview-text">
                        {{ getSessionPreview(session) }}
                      </span>
                    </div>
                  </div>
                </div>
              </TransitionGroup>
            </div>
          </Transition>
        </div>
      </template>

      <!-- 空状态 -->
      <EmptyState
        v-if="!loading && sessions.length === 0"
        type="chat"
        title="暂无会话"
        description="点击上方 + 发起你的第一个聊天"
        :compact="true"
      />
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
      <!-- 移动到分组子菜单 -->
      <el-dropdown
        v-if="!contextMenu.session?.isPinned"
        trigger="hover"
        placement="right"
        @command="handleMoveSessionToGroup"
      >
        <div class="menu-item has-submenu">
          <span class="material-icons-outlined item-icon">folder</span>
          移动到分组
          <span class="material-icons-outlined submenu-arrow">chevron_right</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="group in customGroups"
              :key="group.id"
              :command="group.id"
            >
              <span class="material-icons-outlined item-icon">folder</span>
              {{ group.name }}
            </el-dropdown-item>
            <el-dropdown-item
              v-if="customGroups.length === 0"
              disabled
            >
              <span class="material-icons-outlined item-icon">info</span>
              暂无自定义分组
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <div class="menu-divider" v-if="!contextMenu.session?.isPinned"></div>
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
import { ref, computed, onMounted, onUnmounted, reactive, nextTick } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import CreateGroupDialog from '@/components/CreateGroupDialog/index.vue'
import GlobalSearch from '@/components/Chat/GlobalSearch.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import SkeletonLoader from '@/components/Common/SkeletonLoader.vue'
import EmptyState from '@/components/Common/EmptyState.vue'

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

// 分组管理相关状态
const showGroupManageDialog = ref(false)
const showNewGroupInput = ref(false)
const newGroupName = ref('')
const newGroupInputRef = ref(null)
const expandedGroups = ref(new Set(['pinned', 'default']))

const sessions = computed(() => store.state.im.session?.sessions || [])
const loading = computed(() => store.state.im.session?.loading || false)
const userStatus = computed(() => store.state.im.contact?.userStatus || {})

// 分组会话列表
const groupedSessions = computed(() => store.getters['im/session/groupedSessions'] || [])

// 所有分组列表
const allGroups = computed(() => store.getters['im/session/sortedGroups'] || [])

// 非系统分组（可管理的分组）
const customGroups = computed(() => allGroups.value.filter(g => !g.isSystem))

// 草稿状态管理 - 使用 Vuex getters
const hasDraft = (conversationId) => {
  return store.getters['im/session/hasDraft'](conversationId)
}

const getDraftPreview = (conversationId) => {
  return store.getters['im/session/getDraftPreview'](conversationId)
}

// 输入状态 - 使用 Vuex getters
const isTyping = (conversationId) => {
  return store.getters['im/session/isTyping'](conversationId)
}

// 获取会话状态类型
const getSessionStatus = (session) => {
  if (isTyping(session.id)) {
    return 'typing'
  }
  
  if (hasDraft(session.id)) {
    return 'draft'
  }
  
  if (session.hasMention) {
    return 'mention'
  }
  
  return 'normal'
}

// 获取会话预览文本
const getSessionPreview = (session) => {
  // 优先级：输入状态 > 草稿 > 最新消息
  if (isTyping(session.id)) {
    return ''  // 输入状态不显示预览文本
  }
  
  if (hasDraft(session.id)) {
    return getDraftPreview(session.id)
  }
  
  return session.lastMessage || '暂无消息'
}

// 判断用户是否在线
const isUserOnline = (userId) => {
  return userStatus.value[userId] === 'online'
}

// 获取会话索引用于动画延迟
const getSessionIndex = (session) => {
  return sortedSessions.value.findIndex(s => s.id === session.id)
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
  } else if (command === 'manageGroups') {
    showGroupManageDialog.value = true
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

const contextMenuStyle = computed(() => {
  if (!contextMenu.show) return {}
  
  // 获取菜单尺寸
  const menuWidth = 200
  const menuHeight = contextMenu.session?.isPinned ? 150 : 200
  
  // 获取会话列表容器
  const sessionList = document.querySelector('.session-list')
  if (!sessionList) return {}
  
  const rect = sessionList.getBoundingClientRect()
  
  // 计算居中位置
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 2
  
  // 计算菜单位置（居中）
  let x = centerX - menuWidth / 2
  let y = centerY - menuHeight / 2
  
  // 边界检测
  const padding = 10
  const maxX = window.innerWidth - menuWidth - padding
  const maxY = window.innerHeight - menuHeight - padding
  
  x = Math.max(padding, Math.min(x, maxX))
  y = Math.max(padding, Math.min(y, maxY))
  
  return {
    left: `${x}px`,
    top: `${y}px`
  }
})

const handleContextMenu = (e, session) => {
  e.preventDefault()
  e.stopPropagation()
  
  contextMenu.show = true
  contextMenu.session = session
  
  // 不使用鼠标位置，使用计算后的居中位置
  contextMenu.x = 0
  contextMenu.y = 0
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

// ========== 分组管理函数 ==========

// 切换分组展开/收起
const toggleGroupExpand = (groupId) => {
  store.dispatch('im/session/toggleGroupExpand', groupId)
  if (expandedGroups.value.has(groupId)) {
    expandedGroups.value.delete(groupId)
  } else {
    expandedGroups.value.add(groupId)
  }
}

// 判断分组是否展开
const isGroupExpanded = (groupId) => {
  const group = allGroups.value.find(g => g.id === groupId)
  return group?.isExpanded ?? true
}

// 显示新建分组输入框
const showCreateGroupInput = () => {
  showNewGroupInput.value = true
  nextTick(() => {
    newGroupInputRef.value?.focus()
  })
}

// 创建新分组
const handleCreateGroup = () => {
  const name = newGroupName.value.trim()
  if (!name) {
    ElMessage.warning('请输入分组名称')
    return
  }

  // 检查重名
  if (customGroups.value.some(g => g.name === name)) {
    ElMessage.warning('分组名称已存在')
    return
  }

  store.dispatch('im/session/createGroup', {
    name,
    order: allGroups.value.length
  })

  newGroupName.value = ''
  showNewGroupInput.value = false
  ElMessage.success('分组创建成功')
}

// 取消创建分组
const cancelCreateGroup = () => {
  newGroupName.value = ''
  showNewGroupInput.value = false
}

// 重命名分组
const handleRenameGroup = (group) => {
  ElMessageBox.prompt('请输入新的分组名称', '重命名分组', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: group.name,
    inputPattern: /^.+$/,
    inputErrorMessage: '分组名称不能为空'
  }).then(({ value }) => {
    store.dispatch('im/session/renameGroup', { groupId: group.id, name: value })
    ElMessage.success('重命名成功')
  }).catch(() => {})
}

// 删除分组
const handleDeleteGroup = (group) => {
  const sessionCount = groupedSessions.value
    .find(item => item.group.id === group.id)
    ?.sessions.length || 0

  const message = sessionCount > 0
    ? `确定要删除分组"${group.name}"吗？该分组下的 ${sessionCount} 个会话将移至"全部消息"。`
    : `确定要删除分组"${group.name}"吗？`

  ElMessageBox.confirm(message, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    store.dispatch('im/session/deleteGroup', group.id)
    ElMessage.success('分组已删除')
  }).catch(() => {})
}

// 移动会话到分组（右键菜单）
const handleMoveSessionToGroup = (groupId) => {
  if (!contextMenu.session) return
  store.dispatch('im/session/moveConversationToGroup', {
    conversationId: contextMenu.session.id,
    groupId
  })
  ElMessage.success('已移动会话')
  hideContextMenu()
}

// 排序后的会话列表
const sortedSessions = computed(() => store.getters['im/session/sortedSessions'])

onMounted(() => {
  // 初始化分组数据
  store.dispatch('im/session/initGroups')
  if (sessions.value.length === 0) {
    store.dispatch('im/session/loadSessions')
  }
  // 加载草稿
  store.dispatch('im/session/loadDrafts')
  // 启动输入状态清理
  store.dispatch('im/session/startTypingCleanup')
  window.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  window.removeEventListener('click', hideContextMenu)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 容器 - 使用设计令牌变量
// ============================================================================
.session-panel {
  display: flex;
  flex-direction: column;
  width: var(--dt-session-panel-width);
  flex-shrink: 0;
  border-right: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  height: 100%;
  animation: fadeIn 0.3s var(--dt-ease-out);
  position: relative;
  z-index: 1; // 确保在正常的 flex 流中
}

// ============================================================================
// 头部
// ============================================================================
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 16px 14px;
  flex-shrink: 0;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
  animation: fadeInDown 0.4s var(--dt-ease-out);

  .title-icon {
    font-size: 19px;
    color: var(--dt-brand-color);
    animation: breathe 3s ease-in-out infinite;
  }
}

.add-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  color: #3b4252;
  transition: all var(--dt-transition-fast);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    background: var(--dt-brand-color);
    opacity: 0;
    transition: opacity var(--dt-transition-fast);
  }

  &:hover {
    color: var(--dt-brand-color);
    transform: translateY(-2px) rotate(90deg);

    &::before {
      opacity: 0.1;
    }
  }

  &:active {
    transform: translateY(0) rotate(90deg);
  }

  .material-icons-outlined {
    position: relative;
    z-index: 1;
    transition: transform var(--dt-transition-base);
  }
}

// ============================================================================
// 搜索
// ============================================================================
.search-section {
  padding: 0 12px 14px;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
  height: 34px;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-full);
  padding: 0 12px;
  transition: all var(--dt-transition-fast);

  &:focus-within {
    background: var(--dt-bg-card);
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 3px var(--dt-brand-lighter);
  }
}

.search-icon {
  font-size: 18px;
  color: #8f959e;
  pointer-events: none;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: var(--dt-text-primary);
  padding: 0 10px;
  font-family: var(--dt-font-family);

  &::placeholder {
    color: var(--dt-text-quaternary);
  }
}

.clear-btn {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: var(--dt-radius-full);
  color: #8f959e;
  transition: all var(--dt-transition-fast);
  flex-shrink: 0;

  &:hover {
    background: var(--dt-border-color);
    color: #3b4252;
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

// 会话列表过渡动画
.session-list-enter-active {
  transition: all 0.3s var(--dt-ease-out);
}

.session-list-leave-active {
  transition: all 0.2s var(--dt-ease-in);
}

.session-list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.session-list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.session-list-move {
  transition: transform 0.3s var(--dt-ease-out);
}

.session-item {
  position: relative;
  display: flex;
  padding: 12px; // 钉钉风格：统一间距
  cursor: pointer;
  gap: 10px;
  transition: all var(--dt-transition-fast);
  animation: fadeInLeft 0.3s var(--dt-ease-out) both;
  border-radius: 0; // 钉钉风格：无圆角，方形设计

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 0;
    background: var(--dt-brand-color);
    border-radius: 0;
    transition: height var(--dt-transition-fast);
  }

  &:hover {
    background: rgba(0, 0, 0, 0.04); // 钉钉风格：淡淡的hover背景

    &::before {
      height: 24px;
    }
  }

  &.active {
    background: rgba(22, 119, 255, 0.08); // 钉钉风格：品牌色的淡背景

    &::before {
      height: 32px;
    }

    .session-name {
      color: var(--dt-brand-color);
      font-weight: 600;
    }

    .session-avatar {
      box-shadow: 0 0 0 2px var(--dt-brand-color);
    }

    .preview-text {
      color: var(--dt-text-secondary);
    }
  }

  &.pinned {
    background: rgba(0, 0, 0, 0.02);
  }

  &.pinned.active {
    background: rgba(22, 119, 255, 0.08);
  }

  &.unread {
    .session-name {
      font-weight: 600;
    }

    .preview-text {
      color: var(--dt-text-primary);
    }
  }

  .dark &:hover {
    background: rgba(255, 255, 255, 0.06);
  }

  .dark &.active {
    background: rgba(22, 119, 255, 0.15);
  }

  .dark &.pinned {
    background: rgba(255, 255, 255, 0.03);
  }
}

// 头像区域
.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.session-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-lg);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s var(--dt-ease-out), box-shadow 0.2s var(--dt-ease-out);

  &.group-avatar {
    background: linear-gradient(135deg, #1677ff 0%, #0e5fd9 100%);
    color: #fff;
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
  }

  :deep(.dingtalk-avatar) {
    border-radius: var(--dt-radius-lg) !important;
    transition: transform 0.2s var(--dt-ease-out);
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
    animation: pulse 2s ease-in-out infinite;
  }
}

.unread-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #ff4d4f; // 钉钉风格：更醒目的红色
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  border-radius: var(--dt-radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--dt-bg-card);
  box-shadow: 0 2px 6px rgba(255, 77, 79, 0.3);
  animation: scaleIn 0.3s var(--dt-ease-bounce);
  z-index: 2;

  &.badge-dot {
    min-width: 10px;
    width: 10px;
    height: 10px;
    padding: 0;
    top: 0;
    right: 0;
    border-radius: 50%;
    animation: pulse 1.5s ease-in-out infinite;
  }
}

.pin-indicator {
  position: absolute;
  bottom: -2px;
  left: -2px;
  padding: 2px 6px;
  background: var(--dt-warning-color);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--dt-bg-card);
  animation: fadeInUp 0.3s var(--dt-ease-out);
  font-size: 10px;
  color: #fff;
  font-weight: 500;
  white-space: nowrap;
}

// 会话信息
.session-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px; // 钉钉风格：更紧凑的行间距
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  line-height: 1.2;
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
  font-size: 12px;
  color: var(--dt-text-quaternary);
}

.session-time {
  font-size: 11px;
  color: var(--dt-text-quaternary);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', sans-serif;
  flex-shrink: 0;
  margin-left: 6px;
}

.session-preview {
  display: flex;
  align-items: center;
  gap: 2px;
  line-height: 1.3;
}

.mention-tag {
  color: var(--dt-error-color);
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.sender-name {
  font-size: 12px;
  color: var(--dt-text-secondary);
  flex-shrink: 0;
}

.preview-text {
  font-size: 12px; // 钉钉风格：稍小的预览文字
  color: var(--dt-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
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
  animation: menuFadeIn 0.2s var(--dt-ease-out);

  .menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 9px 10px;
    font-size: 13px;
    color: #1f2329;
    cursor: pointer;
    border-radius: 6px;
    transition: all var(--dt-transition-base);
    font-weight: 500;

    .item-icon {
      font-size: 16px;
      color: #646a73;
    }

    &:hover {
      background: #f5f6f7;
      color: #1677ff;

      .item-icon {
        color: #1677ff;
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
  color: #bdc3c9;

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
    color: #f1f5f9;

    .item-icon {
      color: #bdc3c9;
    }

    &:hover {
      background: var(--dt-bg-hover-dark);
      color: #1677ff;

      .item-icon {
        color: #1677ff;
      }
    }
  }

  .menu-divider {
    background: var(--dt-border-dark);
  }
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

// ============================================================================
// 分组样式
// ============================================================================
.session-group {
  margin-bottom: 4px;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  cursor: pointer;
  user-select: none;
  transition: all var(--dt-transition-fast);
  border-radius: var(--dt-radius-md);

  &:hover {
    background: rgba(0, 0, 0, 0.04);

    .group-name {
      color: var(--dt-text-primary);
    }
  }

  .dark &:hover {
    background: rgba(255, 255, 255, 0.06);
  }
}

.group-title-row {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.group-arrow {
  font-size: 18px;
  color: var(--dt-text-tertiary);
  transition: transform var(--dt-transition-fast);
}

.group-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--dt-text-secondary);
  transition: color var(--dt-transition-fast);
}

.group-count {
  font-size: 11px;
  color: var(--dt-text-quaternary);
  margin-left: 2px;
}

.group-actions {
  opacity: 0;
  transition: opacity var(--dt-transition-fast);
}

.group-header:hover .group-actions {
  opacity: 1;
}

.more-icon {
  font-size: 18px;
  color: var(--dt-text-quaternary);
  padding: 2px;
  border-radius: var(--dt-radius-sm);
  transition: all var(--dt-transition-fast);

  &:hover {
    background: rgba(0, 0, 0, 0.06);
    color: var(--dt-text-secondary);
  }
}

.group-sessions {
  overflow: hidden;
}

// 分组展开/收起过渡动画
.group-expand-enter-active,
.group-expand-leave-active {
  transition: all 0.25s var(--dt-ease-out);
}

.group-expand-enter-from,
.group-expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.group-expand-enter-to,
.group-expand-leave-from {
  opacity: 1;
  max-height: 1000px;
}

// 右键菜单子菜单样式
.context-menu {
  .has-submenu {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .submenu-arrow {
      font-size: 16px;
      color: var(--dt-text-quaternary);
      margin-left: auto;
    }
  }

  :deep(.el-dropdown) {
    width: 100%;
  }
}

// ============================================================================
// 草稿标识样式
// ============================================================================
.draft-badge {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 6px;
  background: rgba(103, 194, 58, 0.1);
  color: var(--dt-success-color);
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  margin-left: 4px;
}

.draft-icon {
  font-size: 11px;
}

.draft-tag {
  color: var(--dt-success-color);
  font-size: 11px;
  font-weight: 500;
  flex-shrink: 0;
}

// ============================================================================
// 输入状态样式
// ============================================================================
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--dt-brand-color);
  font-weight: 500;
  animation: typingPulse 1.5s ease-in-out infinite;
  
  .typing-icon {
    font-size: 14px;
  }
}

@keyframes typingPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

// ============================================================================
// 动画定义
// ============================================================================
@keyframes menuFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
