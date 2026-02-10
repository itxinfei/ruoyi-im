<template>
  <div
    class="session-panel"
    :style="{ '--panel-width': panelWidth + 'px' }"
    @mouseleave="isResizing = false"
  >
    <!-- 拖拽调整宽度的手柄 -->
    <div
      class="resize-handle"
      :class="{ 'is-resizing': isResizing }"
      @mousedown="handleResizeStart"
      @dblclick="resetWidth"
    >
      <div class="resize-line" />
      <div
        v-show="isResizing"
        class="resize-hint"
      >
        {{ Math.round(panelWidth) }}px
      </div>
    </div>

    <!-- 头部 -->
    <div class="panel-header">
      <h1 class="panel-title">
        消息
      </h1>
      <el-dropdown
        trigger="click"
        placement="bottom-end"
        popper-class="add-menu-dropdown-popper"
        :show-timeout="0"
        :hide-timeout="200"
        @command="handleCommand"
      >
        <button
          class="add-btn"
          :class="{ 'is-active': isAddMenuOpen }"
        >
          <span class="material-icons-outlined">add</span>
        </button>
        <template #dropdown>
          <div class="add-menu-container">
            <!-- 快捷操作区 -->
            <div class="quick-actions-section">
              <div class="section-title">快捷操作</div>
              <div class="quick-actions-grid">
                <div class="quick-action-item" @click="handleCommand('group')">
                  <div class="action-icon success">
                    <span class="material-icons-outlined">group_add</span>
                  </div>
                  <span class="action-label">发起群聊</span>
                </div>
                <div class="quick-action-item" @click="handleCommand('join')">
                  <div class="action-icon warning">
                    <span class="material-icons-outlined">search</span>
                  </div>
                  <span class="action-label">加入群组</span>
                </div>
                <div class="quick-action-item" @click="handleCommand('contacts')">
                  <div class="action-icon info">
                    <span class="material-icons-outlined">person_add</span>
                  </div>
                  <span class="action-label">添加联系人</span>
                </div>
                <div class="quick-action-item" @click="handleCommand('scan')">
                  <div class="action-icon primary">
                    <span class="material-icons-outlined">qr_code_scanner</span>
                  </div>
                  <span class="action-label">扫一扫</span>
                </div>
              </div>
            </div>

            <div class="menu-divider" />

            <!-- 其他功能 -->
            <div class="common-actions-section">
              <div class="section-title">其他功能</div>
              <div class="menu-item-simple" @click="handleCommand('invite')">
                <span class="material-icons-outlined item-icon">share</span>
                <span class="item-title">邀请好友</span>
              </div>
              <div class="menu-item-simple" @click="handleCommand('archived')">
                <span class="material-icons-outlined item-icon">archive</span>
                <span class="item-title">归档会话</span>
              </div>
            </div>
          </div>
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
          placeholder="搜索"
          type="text"
          @focus="showGlobalSearch = true"
        >
        <span
          v-if="searchKeyword"
          class="clear-btn"
          @click="searchKeyword = ''"
        >
          <span class="material-icons-outlined">close</span>
        </span>
      </div>

      <div
        v-if="archivedCount > 0"
        class="archive-entry-mini"
        @click="handleShowArchived"
        title="归档会话"
      >
        <span class="material-icons-outlined">archive</span>
      </div>

      <!-- 全局搜索面板 -->
      <GlobalSearchDialog
        v-model="showGlobalSearch"
        @select-message="handleSearchSelectMessage"
        @select-contact="handleSearchSelectContact"
        @select-group="handleSearchSelectGroup"
      />
    </div>

    <!-- 会话列表 -->
    <div class="session-list">
      <!-- 骨架屏加载状态 -->
      <SkeletonLoader
        v-if="loading && sessions.length === 0"
        type="session"
        :count="5"
      />

      <!-- 分组会话列表 -->
      <template v-if="!loading">
        <div
          v-for="groupItem in groupedSessions"
          :key="groupItem.group.id"
          class="session-group"
        >
          <!-- 分组内的会话列表 -->
          <div class="group-sessions">
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
                  />
                  <!-- 未读角标 -->
                  <span
                    v-if="session.unreadCount > 0"
                    class="unread-badge"
                    :class="{ 'badge-dot': session.isMuted }"
                  >
                    {{ session.isMuted ? '' : (session.unreadCount > 99 ? '99+' : session.unreadCount) }}
                  </span>
                  <!-- @提及角标 -->
                  <span
                    v-if="getSessionMentionCount(session.id) > 0"
                    class="mention-badge"
                    title="有@我的消息"
                  >
                    @
                  </span>
                  <!-- 置顶标记 -->
                  <span
                    v-if="session.isPinned && !isActiveSession(session)"
                    class="pin-indicator"
                  >
                    置顶
                  </span>
                </div>

                <div class="session-info">
                  <div class="session-header">
                    <div class="session-name-group">
                      <span class="session-name">{{ session.name }}</span>
                      <span
                        v-if="session.isMuted"
                        class="mute-icon"
                      >
                        <span class="material-icons-outlined">notifications_off</span>
                      </span>
                    </div>
                    <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
                  </div>
                  <div class="session-preview">
                    <span
                      v-if="getSessionStatus(session) === 'typing'"
                      class="typing-indicator"
                    >
                      <span class="material-icons-outlined typing-icon">edit</span>
                      正在输入...
                    </span>
                    <span
                      v-else-if="getSessionStatus(session) === 'draft'"
                      class="draft-tag"
                    >[草稿]</span>
                    <span
                      v-else-if="getSessionStatus(session) === 'mention'"
                      class="mention-tag"
                    >@</span>
                    <span
                      v-if="session.lastSenderNickname && session.type === 'GROUP' && getSessionStatus(session) === 'normal'"
                      class="sender-name"
                    >
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
    <ContextMenu
      :show="contextMenu.show"
      :x="contextMenu.x"
      :y="contextMenu.y"
      :items="contextMenuItems"
      @select="handleContextMenuSelect"
      @update:show="contextMenu.show = $event"
    />

    <!-- 创建群组对话框 -->
    <CreateGroupDialog
      v-model="showCreateGroupDialog"
      @success="handleGroupCreated"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, reactive, nextTick, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import CreateGroupDialog from '@/components/CreateGroupDialog/index.vue'
import GlobalSearchDialog from '@/components/Common/GlobalSearchDialog.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import SkeletonLoader from '@/components/Common/SkeletonLoader.vue'
import EmptyState from '@/components/Common/EmptyState.vue'
import ContextMenu from '@/components/Common/ContextMenu.vue'
import { sendFriendRequest } from '@/api/im/contact'
import { joinGroup } from '@/api/im/group'
import { getArchivedSessions, archiveSession, unarchiveSession } from '@/api/im/conversation'
import { scanQRCode } from '@/api/im/user'
import { formatChatTime } from '@/utils/format'
import { useMentions } from '@/composables/useMentions.js'

const props = defineProps({
  currentSession: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['select-session', 'show-user'])
const store = useStore()

// ==================== 侧边栏宽度调整 ====================
const STORAGE_KEY = 'session-panel-width'
const MIN_WIDTH = 220
const MAX_WIDTH = 420
const DEFAULT_WIDTH = 280

const panelWidth = ref(DEFAULT_WIDTH)
const isResizing = ref(false)

// 初始化宽度
// 会话数据加载
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
  // 加载未读@提及
  loadMentions()
  // 加载归档数量
  loadArchivedCount()

  // 初始化面板宽度
  const savedWidth = localStorage.getItem(STORAGE_KEY)
  if (savedWidth) {
    const width = parseInt(savedWidth, 10)
    if (!isNaN(width) && width >= MIN_WIDTH && width <= MAX_WIDTH) {
      panelWidth.value = width
    }
  }

  // 监听全局鼠标移动和释放
  window.addEventListener('mousemove', handleResizeMove)
  window.addEventListener('mouseup', handleResizeEnd)
})

onUnmounted(() => {
  // 清理工作
  window.removeEventListener('mousemove', handleResizeMove)
  window.removeEventListener('mouseup', handleResizeEnd)
})

// 开始拖拽
const handleResizeStart = e => {
  isResizing.value = true
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}

// 拖拽中
const handleResizeMove = e => {
  if (!isResizing.value) {return}

  const newWidth = e.clientX
  if (newWidth >= MIN_WIDTH && newWidth <= MAX_WIDTH) {
    panelWidth.value = newWidth
  }
}

// 结束拖拽
const handleResizeEnd = () => {
  if (isResizing.value) {
    isResizing.value = false
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
    // 保存到 localStorage
    localStorage.setItem(STORAGE_KEY, String(panelWidth.value))
  }
}

// 重置宽度
const resetWidth = () => {
  panelWidth.value = DEFAULT_WIDTH
  localStorage.setItem(STORAGE_KEY, String(DEFAULT_WIDTH))
}

// 暴露宽度供父组件使用
defineExpose({
  panelWidth
})
// ====================================================

const searchKeyword = ref('')
const showCreateGroupDialog = ref(false)
const showGlobalSearch = ref(false)
const showContactSelector = ref(false)
const isAddMenuOpen = ref(false)

// ==================== 会话归档 ====================
const archivedCount = ref(0)
const showArchivedSessions = ref(false)
const archivedSessions = ref([])

// 加载归档数量
const loadArchivedCount = async () => {
  try {
    const count = await store.dispatch('im/session/getArchivedCount')
    archivedCount.value = count || 0
  } catch (error) {
    console.error('获取归档数量失败', error)
  }
}

// 显示归档会话列表
const handleShowArchived = async () => {
  showArchivedSessions.value = !showArchivedSessions.value
  if (showArchivedSessions.value) {
    try {
      const res = await getArchivedSessions()
      if (res.code === 200) {
        archivedSessions.value = res.data || []
        if (archivedSessions.value.length === 0) {
          ElMessage.info('暂无归档会话')
        }
      } else if (res.code === 404) {
        ElMessage.warning('归档会话功能开发中，敬请期待')
      }
    } catch (error) {
      ElMessage.error('加载归档会话失败')
    }
  }
}
// ====================================================

// 分组管理相关状态
const showGroupManageDialog = ref(false)
const showNewGroupInput = ref(false)
const newGroupName = ref('')
const newGroupInputRef = ref(null)
const expandedGroups = ref(new Set(['pinned', 'default']))

const sessions = computed(() => store.state.im.session?.sessions || [])
const loading = computed(() => store.state.im.session?.loading || false)
const userStatus = computed(() => store.state.im.contact?.userStatus || {})

// @提及管理
const {
  unreadMentions,
  unreadCount: mentionCount,
  loadAll: loadMentions,
  getUnreadCountByConversation,
  batchMarkAsRead
} = useMentions()

// 获取会话的未读@提及数量
const getSessionMentionCount = sessionId => {
  return getUnreadCountByConversation(sessionId)
}

// 分组会话列表
const groupedSessions = computed(() => store.getters['im/session/groupedSessions'] || [])

// 所有分组列表
const allGroups = computed(() => store.getters['im/session/sortedGroups'] || [])

// 非系统分组（可管理的分组）
const customGroups = computed(() => allGroups.value.filter(g => !g.isSystem))

// 草稿状态管理 - 使用 Vuex getters
const hasDraft = conversationId => {
  return store.getters['im/session/hasDraft'](conversationId)
}

const getDraftPreview = conversationId => {
  return store.getters['im/session/getDraftPreview'](conversationId)
}

// 输入状态 - 使用 Vuex getters
const isTyping = conversationId => {
  return store.getters['im/session/isTyping'](conversationId)
}

// 获取会话状态类型
const getSessionStatus = session => {
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
const getSessionPreview = session => {
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
const isUserOnline = userId => {
  return userStatus.value[userId] === 'online'
}

// 获取会话索引用于动画延迟
const getSessionIndex = session => {
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
const handleCommand = async command => {
  if (command === 'group') {
    showCreateGroupDialog.value = true
  } else if (command === 'chat') {
    await handleStartChat()
  } else if (command === 'join') {
    await handleJoinGroup()
  } else if (command === 'contacts') {
    await handleAddFriend()
  } else if (command === 'manageGroups') {
    showGroupManageDialog.value = true
  } else if (command === 'scan') {
    await handleScanQR()
  } else if (command === 'invite') {
    await handleInviteFriend()
  } else if (command === 'archived') {
    await handleShowArchived()
  }
}

// 发起单聊 - 打开联系人选择器
const handleStartChat = async () => {
  try {
    // 获取好友列表
    const res = await store.dispatch('im/contact/loadContacts')
    if (!res || res.length === 0) {
      ElMessage.warning('暂无好友，请先添加好友')
      return
    }
    // 打开联系人选择对话框
    showContactSelector.value = true
  } catch (error) {
    ElMessage.error('加载好友列表失败')
  }
}

// 处理选择的联系人
const handleContactSelected = contact => {
  showContactSelector.value = false
  if (contact) {
    // 创建或获取会话
    store.dispatch('im/session/createSession', {
      targetId: contact.id,
      type: 'SINGLE'
    }).then(session => {
      emit('select-session', session)
      ElMessage.success(`已开始与 ${contact.name || contact.userName} 的聊天`)
    }).catch(() => {
      ElMessage.error('创建会话失败')
    })
  }
}

// 扫一扫
const handleScanQR = async () => {
  try {
    // 检查浏览器是否支持相机访问
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      ElMessage.warning('您的浏览器不支持相机访问，请使用最新版 Chrome 或 Safari')
      return
    }

    // 尝试访问相机
    const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })

    // 成功获取相机权限后，可以打开扫码对话框
    // 这里简化处理，显示提示
    stream.getTracks().forEach(track => track.stop())

    ElMessage.info('扫码功能开发中，敬请期待')
  } catch (error) {
    if (error.name === 'NotAllowedError') {
      ElMessage.warning('请允许相机访问权限以使用扫码功能')
    } else if (error.name === 'NotFoundError') {
      ElMessage.warning('未检测到相机设备')
    } else {
      ElMessage.warning('扫码功能开发中，敬请期待')
    }
  }
}

// 邀请好友
const handleInviteFriend = async () => {
  try {
    // 生成邀请链接
    const inviteLink = `${window.location.origin}/invite?ref=${store.state.user.userInfo.userId}`

    // 复制到剪贴板
    await navigator.clipboard.writeText(inviteLink)

    ElMessage.success({
      message: '邀请链接已复制到剪贴板',
      duration: 3000
    })
  } catch (err) {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 群组创建成功
const handleGroupCreated = () => {
  ElMessage.success('群组创建成功')
  store.dispatch('im/session/loadSessions')
}

// 添加好友
const handleAddFriend = async () => {
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入要添加的好友用户ID或手机号',
      '添加好友',
      {
        confirmButtonText: '发送申请',
        cancelButtonText: '取消',
        inputPlaceholder: '用户ID或手机号',
        inputPattern: /^[a-zA-Z0-9\u4e00-\u9fa5]+$/,
        inputErrorMessage: '请输入有效的用户ID或手机号'
      }
    )

    if (!value) {return}

    // 发送好友申请
    await sendFriendRequest({
      userId: value.trim(),
      remark: ''
    })

    ElMessage.success('好友申请已发送，等待对方确认')
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 加入群组
const handleJoinGroup = async () => {
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入群组ID或群组码',
      '加入群组',
      {
        confirmButtonText: '申请加入',
        cancelButtonText: '取消',
        inputPlaceholder: '群组ID或群组码',
        inputErrorMessage: '请输入有效的群组ID或群组码'
      }
    )

    if (!value) {return}

    // 调用加入群组 API
    const res = await joinGroup(value.trim())
    if (res.code === 200) {
      ElMessage.success('成功加入群组')
      // 刷新会话列表
      store.dispatch('im/session/loadSessions')
    } else {
      ElMessage.warning(res.msg || '加入失败，请检查群组码是否正确')
    }
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.msg || '操作失败，请重试')
    }
  }
}

// 处理搜索选择
const handleSearchSelect = item => {
  if (typeof item === 'string') {
    searchKeyword.value = item
  } else if (item.id && item.conversationId) {
    const session = sessions.value.find(s => s.id === item.conversationId)
    if (session) {emit('select-session', session)}
  }
}

// 处理消息搜索选择
const handleSearchSelectMessage = message => {
  if (message && message.conversationId) {
    const session = sessions.value.find(s => s.id === message.conversationId)
    if (session) {
      emit('select-session', session)
      ElMessage.success('正在定位消息...')
    }
  }
}

// 处理联系人搜索选择
const handleSearchSelectContact = contact => {
  if (contact && contact.userId) {
    const existingContact = sessions.value.find(s => 
      s.type === 'PRIVATE' && s.targetId === contact.userId
    )
    if (existingContact) {
      emit('select-session', existingContact)
    } else {
      // 创建新会话
      ElMessage.info(`正在发起与 ${contact.nickname || contact.userName} 的对话...`)
    }
  }
}

// 处理群组搜索选择
const handleSearchSelectGroup = group => {
  if (group && group.groupId) {
    const existingGroup = sessions.value.find(s => 
      s.type === 'GROUP' && s.targetId === group.groupId
    )
    if (existingGroup) {
      emit('select-session', existingGroup)
    } else {
      // 创建新会话
      ElMessage.info(`正在加入 ${group.groupName}...`)
    }
  }
}

// 判断是否为当前会话
const isActiveSession = session => {
  return props.currentSession?.id === session.id
}

// 处理会话点击
const handleSessionClick = session => {
  // 标记该会话的@提及为已读
  const mentionCount = getSessionMentionCount(session.id)
  if (mentionCount > 0) {
    // 找出该会话的所有未读提及消息ID
    const messageIds = unreadMentions.value
      .filter(m => m.conversationId === session.id)
      .map(m => m.messageId)

    if (messageIds.length > 0) {
      // 异步标记为已读，不阻塞用户操作
      markMentionsAsRead(messageIds)
    }
  }

  emit('select-session', session)
}

// 标记提及为已读
const markMentionsAsRead = async messageIds => {
  try {
    await batchMarkAsRead(messageIds)
  } catch (error) {
    console.error('标记@提及已读失败:', error)
  }
}

// 格式化时间
const formatTime = formatChatTime

// 右键菜单状态
const contextMenu = ref({
  show: false,
  x: 0,
  y: 0,
  session: null
})

// 右键菜单项配置
const contextMenuItems = computed(() => {
  const session = contextMenu.value.session
  if (!session) {return []}

  const items = [
    {
      label: '标记已读',
      icon: 'done_all',
      value: 'markRead',
      disabled: !session.unreadCount
    },
    {
      label: session.isArchived ? '取消归档' : '归档会话',
      icon: 'archive',
      value: 'archive'
    },
    {
      label: session.isPinned ? '取消置顶' : '置顶会话',
      icon: session.isPinned ? 'push_pin' : 'push_pin',
      value: 'pin'
    },
    {
      label: session.isMuted ? '取消免打扰' : '消息免打扰',
      icon: session.isMuted ? 'notifications' : 'notifications_off',
      value: 'mute'
    }
  ]

  // 添加分组选项（仅当会话未置顶时）
  if (!session.isPinned) {
    items.push({ divider: true })

    // 添加分组子菜单项
    if (customGroups.value.length > 0) {
      customGroups.value.forEach(group => {
        items.push({
          label: group.name,
          icon: 'folder',
          value: `moveToGroup:${group.id}`
        })
      })
    } else {
      items.push({
        label: '暂无自定义分组',
        icon: 'info',
        value: 'noGroup',
        disabled: true
      })
    }

    items.push({ divider: true })
  }

  // 删除选项
  items.push({
    label: '删除会话',
    icon: 'delete',
    value: 'delete',
    danger: true
  })

  return items
})

const handleContextMenu = (e, session) => {
  e.preventDefault()
  e.stopPropagation()

  contextMenu.value = {
    show: true,
    x: e.clientX,
    y: e.clientY,
    session
  }
}

// 右键菜单选择处理
const handleContextMenuSelect = async item => {
  const session = contextMenu.value.session
  if (!session) {return}

  switch (item.value) {
    case 'markRead':
      await handleMarkAsRead(session)
      break
    case 'archive':
      await handleArchiveSession(session)
      break
    case 'pin':
      await handleTogglePin(session)
      break
    case 'mute':
      await handleToggleMute(session)
      break
    case 'delete':
      await handleDeleteSession(session)
      break
    case 'noGroup':
      // 无操作，只是提示
      break
    default:
      // 处理移动到分组
      if (item.value?.startsWith('moveToGroup:')) {
        const groupId = item.value.split(':')[1]
        await handleMoveSessionToGroup(groupId)
      }
  }

  contextMenu.value.show = false
}

// 会话操作
const handleMarkAsRead = async session => {
  await store.dispatch('im/session/markSessionAsRead', session.id)
}

const handleTogglePin = async session => {
  const pinned = !session.isPinned
  await store.dispatch('im/session/pinSession', { sessionId: session.id, pinned })
  ElMessage.success(pinned ? '已置顶' : '已取消置顶')
}

const handleToggleMute = async session => {
  const muted = !session.isMuted
  await store.dispatch('im/session/muteSession', { sessionId: session.id, muted })
  ElMessage.success(muted ? '已开启免打扰' : '已关闭免打扰')
}

const handleDeleteSession = async session => {
  ElMessageBox.confirm('确定要删除该会话吗？历史消息将保留。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await store.dispatch('im/session/deleteSession', session.id)
    ElMessage.success('已删除')
  }).catch(() => {})
}

// 归档/取消归档会话
const handleArchiveSession = async session => {
  const isArchived = session.isArchived || false

  try {
    await store.dispatch('im/session/toggleArchive', {
      conversationId: session.id,
      archived: !isArchived
    })
    ElMessage.success(isArchived ? '已取消归档' : '会话已归档')
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// ========== 分组管理函数 ==========

// 切换分组展开/收起
const toggleGroupExpand = groupId => {
  store.dispatch('im/session/toggleGroupExpand', groupId)
  if (expandedGroups.value.has(groupId)) {
    expandedGroups.value.delete(groupId)
  } else {
    expandedGroups.value.add(groupId)
  }
}

// 判断分组是否展开
const isGroupExpanded = groupId => {
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
const handleRenameGroup = group => {
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
const handleDeleteGroup = group => {
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
const handleMoveSessionToGroup = groupId => {
  if (!contextMenu.value.session) {return}
  store.dispatch('im/session/moveConversationToGroup', {
    conversationId: contextMenu.value.session.id,
    groupId
  })
  ElMessage.success('已移动会话')
  contextMenu.value.show = false
}

// 排序后的会话列表
const sortedSessions = computed(() => store.getters['im/session/sortedSessions'])

// 已合并到前面的 onMounted 中
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 容器 - 使用设计令牌变量
// ============================================================================
.session-panel {
  display: flex;
  flex-direction: column;
  width: var(--panel-width, var(--dt-session-panel-width, 280px));
  min-width: var(--dt-session-panel-min-width, 200px);
  max-width: var(--dt-session-panel-max-width, 400px);
  flex-shrink: 0; // 防止收缩，关键修复
  border-right: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  height: 100%;
  animation: fadeIn 0.3s var(--dt-ease-out);
  position: relative;
  z-index: 10; // 确保在正常的 flex 流中
  transition: width 0.05s linear; // 拖拽时无延迟
  overflow: hidden; // 防止内部内容溢出
}

// ============================================================================
// 拖拽调整宽度的手柄
// ============================================================================
.resize-handle {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 6px;
  cursor: col-resize;
  z-index: 20;
  transition: background-color 0.2s;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 3px;
    height: 40px;
    background: transparent;
    border-radius: var(--dt-radius-sm);
    transition: all 0.2s;
  }

  &:hover::before {
    background: var(--dt-brand-color);
    opacity: 0.5;
  }

  &.is-resizing::before {
    background: var(--dt-brand-color);
    height: 60px;
    opacity: 0.8;
  }

  &:hover .resize-line {
    opacity: 1;
  }

  &.is-resizing .resize-line {
    opacity: 1;
  }
}

.resize-line {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 2px;
  height: 30px;
  background: linear-gradient(
    to bottom,
    transparent,
    var(--dt-border-color) 20%,
    var(--dt-border-color) 80%,
    transparent
  );
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
}

.resize-hint {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 4px 8px;
  background: var(--dt-brand-color);
  color: var(--dt-text-inverse);
  font-size: 11px;
  font-weight: 600;
  border-radius: var(--dt-radius-sm);
  white-space: nowrap;
  pointer-events: none;
  animation: fadeIn 0.2s;
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
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  color: var(--dt-text-secondary);
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
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

// ============================================================================
// 归档入口
// ============================================================================
.archive-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 12px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  .archive-icon {
    font-size: 18px;
    color: var(--dt-text-secondary);
  }

  .archive-text {
    flex: 1;
    font-size: 13px;
    color: var(--dt-text-primary);
  }

  .archive-chevron {
    font-size: 18px;
    color: var(--dt-text-quaternary);
  }
}

.search-container {
  flex: 1;
  height: 32px;
  background: var(--dt-bg-subtle);
  border: 1px solid var(--dt-border-light);
  border-radius: 6px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  transition: all 0.2s ease;

  &:focus-within {
    background: #fff;
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 2px var(--dt-brand-light);

    .search-icon {
      color: var(--dt-brand-color);
    }
  }
}

.search-icon {
  font-size: 18px;
  color: var(--dt-text-quaternary);
  margin-right: 6px;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: var(--dt-text-primary);
  padding: 0;

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
  color: var(--dt-text-tertiary);
  transition: all var(--dt-transition-fast);
  flex-shrink: 0;

  &:hover {
    background: var(--dt-border-color);
    color: var(--dt-text-secondary);
  }
}

.archive-entry-mini {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: var(--dt-bg-subtle);
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

// ============================================================================
// 会话列表
// ============================================================================
.session-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 0;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0,0,0,0.05);
    border-radius: 3px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &:hover::-webkit-scrollbar-thumb {
    background: var(--dt-scrollbar-thumb);
  }

  .dark &:hover::-webkit-scrollbar-thumb {
    background: var(--dt-scrollbar-thumb-dark);
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
  height: 72px;
  padding: 0 16px;
  cursor: pointer;
  gap: 12px;
  transition: background var(--dt-transition-fast);
  border-radius: 0;
  align-items: center;

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
    background: var(--dt-bg-session-hover);

    &::before {
      height: 24px;
    }
  }

  &.active {
    background: var(--dt-bg-session-active);

    &::before {
      height: 40px;
    }

    .session-name {
      color: var(--dt-brand-color);
      font-weight: 600;
    }

    .session-avatar {
      border: 2px solid var(--dt-brand-color);
    }

    .preview-text {
      color: var(--dt-text-secondary);
    }
  }

  &.pinned:not(.active) {
    background: var(--dt-bg-pinned);
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
    background: var(--dt-bg-session-hover-dark);
  }

  .dark &.active {
    background: var(--dt-bg-session-active-dark);
  }

  .dark &.pinned {
    background: var(--dt-bg-pinned-dark);
  }
}

// 头像区域
.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.session-avatar {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform var(--dt-transition-base);

  &.group-avatar {
    background: var(--dt-brand-color);
    color: var(--dt-text-inverse);
  }

  :deep(.dingtalk-avatar) {
    border-radius: 6px !important;
    transition: transform var(--dt-transition-base);
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
    box-shadow: var(--dt-shadow-success-glow);
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
  background: var(--dt-error-color); // 使用设计令牌
  color: var(--dt-text-inverse);
  font-size: 10px;
  font-weight: 600;
  border-radius: var(--dt-radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--dt-bg-card);
  box-shadow: var(--dt-shadow-error);
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

.mention-badge {
  position: absolute;
  bottom: -2px;
  left: -2px;
  width: 18px;
  height: 18px;
  background: var(--dt-error-gradient);
  color: var(--dt-text-inverse);
  font-size: 12px;
  font-weight: 700;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--dt-bg-card);
  box-shadow: var(--dt-shadow-error);
  animation: mentionPulse 2s ease-in-out infinite;
  z-index: 3;
  cursor: pointer;
}

.pin-indicator {
  position: absolute;
  bottom: -2px;
  left: -2px;
  padding: 2px 6px;
  background: var(--dt-warning-color);
  border-radius: var(--dt-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--dt-bg-card);
  animation: fadeInUp 0.3s var(--dt-ease-out);
  font-size: 10px;
  color: var(--dt-text-inverse);
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
    background: var(--dt-bg-card-hover-dark);
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

// ============================================================================
// 添加按钮菜单样式
// ============================================================================
:deep(.add-menu-dropdown) {
  .el-dropdown__popper {
    padding: 0;
    border-radius: var(--dt-radius-xl);
    box-shadow: var(--dt-shadow-dialog);
    border: none;
    overflow: hidden;
  }

  .el-dropdown__list {
    padding: 0;
  }
}

.add-menu-container {
  width: 320px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  overflow: hidden;

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

// 快捷操作区
.quick-actions-section {
  padding: 16px;

  .section-title {
    font-size: 12px;
    font-weight: 600;
    color: var(--dt-text-tertiary);
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 12px;
  }
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 4px;
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    background: var(--dt-bg-session-hover);
    transform: translateY(-2px);

    .action-icon {
      transform: scale(1.1);
    }
  }

  &:active {
    transform: translateY(0);
  }

  .action-icon {
    width: 48px;
    height: 48px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.25s ease;

    .material-icons-outlined {
      font-size: 24px;
      color: var(--dt-text-inverse);
    }

    &.primary {
      background: var(--dt-brand-gradient);
      box-shadow: var(--dt-shadow-brand);
    }

    &.success {
      background: var(--dt-success-gradient);
      box-shadow: var(--dt-shadow-success);
    }

    &.warning {
      background: var(--dt-warning-gradient);
      box-shadow: var(--dt-shadow-warning);
    }

    &.info {
      background: var(--dt-info-gradient);
      box-shadow: var(--dt-shadow-info);
    }
  }

  .action-label {
    font-size: 12px;
    font-weight: 500;
    color: var(--dt-text-primary);
    text-align: center;
  }
}

// 常用功能
.common-actions-section {
  padding: 12px 16px;

  .section-title {
    font-size: 12px;
    font-weight: 600;
    color: var(--dt-text-tertiary);
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 8px;
  }
}

.menu-item-with-icon {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px !important;
  border-radius: var(--dt-radius-lg) !important;
  margin: 2px 0;
  transition: all 0.2s ease;

  &:hover {
    background: var(--dt-bg-session-hover) !important;
  }

  .item-icon {
    font-size: 20px;
    color: var(--dt-brand-color);
    margin-top: 2px;
  }

  .item-content {
    display: flex;
    flex-direction: column;
    gap: 2px;
    flex: 1;
  }

  .item-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
  }

  .item-desc {
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }
}

.menu-item-simple {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px !important;
  border-radius: var(--dt-radius-lg) !important;
  margin: 2px 0;
  transition: all 0.2s ease;

  &:hover {
    background: var(--dt-bg-session-hover) !important;
  }

  .item-icon {
    font-size: 18px;
    color: var(--dt-text-secondary);
  }

  .item-title {
    font-size: 14px;
    color: var(--dt-text-primary);
  }
}

.menu-divider {
  height: 1px;
  background: var(--dt-border-lighter);
  margin: 0 16px;
}

// ============================================================================
// Element Plus 下拉菜单样式（旧版兼容）
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



.group-sessions {
  overflow: hidden;
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

// ============================================================================
// @提及动画
// ============================================================================
@keyframes mentionPulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: var(--dt-shadow-error);
  }
  50% {
    transform: scale(1.1);
    box-shadow: var(--dt-shadow-error-strong);
  }
}

// ============================================================================
</style>
