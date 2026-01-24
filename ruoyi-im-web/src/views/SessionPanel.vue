<template>
  <div class="session-panel">
    <div class="panel-header">
      <h1 class="text-xl font-bold dark:text-white">消息</h1>
      <el-dropdown trigger="click" @command="handleCommand">
        <button class="plus-btn transition-colors">
          <span class="material-icons-outlined">add</span>
        </button>
        <template #dropdown>
          <el-dropdown-menu class="plus-dropdown-menu">
            <el-dropdown-item command="chat">
              <span class="material-icons-outlined mr-2">chat</span>
              发起单聊
            </el-dropdown-item>
            <el-dropdown-item command="group">
              <span class="material-icons-outlined mr-2">group_add</span>
              创建群组
            </el-dropdown-item>
            <el-dropdown-item command="join">
              <span class="material-icons-outlined mr-2">search</span>
              加入群组
            </el-dropdown-item>
            <div class="menu-divider"></div>
            <el-dropdown-item command="contacts">
              <span class="material-icons-outlined mr-2">person_search</span>
              添加好友
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="search-section">
      <div class="search-container">
        <span class="material-icons-outlined search-icon">search</span>
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索"
          type="text"
          @focus="showGlobalSearch = true"
        />
      </div>
      <!-- 全局搜索面板 -->
      <GlobalSearch
        v-model:visible="showGlobalSearch"
        :keyword="searchKeyword"
        @select="handleSearchSelect"
      />
    </div>

    <div v-loading="loading" class="session-list">
      <div
        v-for="session in sortedSessions"
        :key="session.id"
        class="session-item"
        :class="{
          active: isActiveSession(session),
          pinned: session.isPinned
        }"
        @click="handleSessionClick(session)"
        @contextmenu.prevent="handleContextMenu($event, session)"
      >
        <div class="avatar-wrapper">
          <!-- 群组显示图标，单聊使用钉钉风格头像 -->
          <template v-if="session.type === 'GROUP'">
            <div
              class="session-avatar group-avatar"
            >
              <span class="material-icons-outlined text-xl">groups</span>
            </div>
          </template>
          <DingtalkAvatar
            v-else
            :name="session.name"
            :user-id="session.targetId"
            :size="44"
            shape="square"
            custom-class="session-avatar-item"
          />
          <!-- 在线状态点 (仅单聊显示) -->
          <span
            v-if="session.type === 'PRIVATE'"
            :class="['status-dot', isUserOnline(session.targetId) ? 'online' : 'offline']"
          ></span>
          <span
            v-if="session.unreadCount > 0"
            class="unread-badge"
            :class="{ 'badge-dot': session.isMuted }"
          >
            {{ session.isMuted ? '' : (session.unreadCount > 99 ? '99+' : session.unreadCount) }}
          </span>
        </div>

        <div class="session-info">
          <div class="session-header">
            <div class="session-name-group">
              <span class="session-name">{{ session.name }}</span>
              <span v-if="session.isMuted" class="material-icons-outlined mute-icon">notifications_off</span>
            </div>
            <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
          </div>
          <div class="session-bottom">
            <div class="session-preview">
              <span v-if="session.hasMention" class="mention-tag">[有人@我]</span>
              <span v-if="session.isPinned && !isActiveSession(session)" class="pin-tag">[置顶]</span>
              <span v-if="session.lastSenderNickname && session.type === 'GROUP'" class="sender-name">{{ session.lastSenderNickname }}: </span>
              {{ session.lastMessage || '暂无消息' }}
            </div>
          </div>
        </div>
      </div>

      <!-- 右键菜单 -->
      <div v-if="contextMenu.show" class="context-menu" :style="contextMenuStyle">
        <div class="menu-item" @click="handleMarkAsRead">标记已读</div>
        <div class="menu-item" @click="handleTogglePin">
          {{ contextMenu.session?.isPinned ? '取消置顶' : '置顶会话' }}
        </div>
        <div class="menu-item" @click="handleToggleMute">
          {{ contextMenu.session?.isMuted ? '取消静音' : '消息免打扰' }}
        </div>
        <div class="menu-divider"></div>
        <div class="menu-item danger" @click="handleDeleteSession">删除会话</div>
      </div>

      <div v-if="!loading && sessions.length === 0" class="empty-state">
        <el-empty description="暂无会话" />
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
import { ref, computed, onMounted, reactive, onUnmounted } from 'vue'
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

const emit = defineEmits(['select-session'])
const store = useStore()

const searchKeyword = ref('')
const showCreateGroupDialog = ref(false)
const showGlobalSearch = ref(false)

const sessions = computed(() => store.state.im.sessions || [])
const loading = computed(() => store.state.im.loading.sessions)
const userStatus = computed(() => store.state.im.userStatus || {})

// 判断用户是否在线
const isUserOnline = (userId) => {
  return userStatus.value[userId] === 'online'
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
const handleGroupCreated = (groupData) => {
  ElMessage.success('群组创建成功')
  store.dispatch('im/loadSessions')
}

// 处理搜索选择
const handleSearchSelect = (item) => {
  if (typeof item === 'string') {
    searchKeyword.value = item
  } else if (item.id) {
    if (item.conversationId) {
      const session = sessions.value.find(s => s.id === item.conversationId)
      if (session) emit('select-session', session)
    }
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
  await store.dispatch('im/markSessionAsRead', contextMenu.session.id)
  hideContextMenu()
}

const handleTogglePin = async () => {
  if (!contextMenu.session) return
  const pinned = !contextMenu.session.isPinned
  await store.dispatch('im/pinSession', { sessionId: contextMenu.session.id, pinned })
  ElMessage.success(pinned ? '已置顶' : '已取消置顶')
  hideContextMenu()
}

const handleToggleMute = async () => {
  if (!contextMenu.session) return
  const muted = !contextMenu.session.isMuted
  await store.dispatch('im/muteSession', { sessionId: contextMenu.session.id, muted })
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
    await store.dispatch('im/deleteSession', contextMenu.session.id)
    ElMessage.success('已删除')
    hideContextMenu()
  }).catch(() => {})
}

// 使用 getter 中的排序会话
const sortedSessions = computed(() => store.getters['im/sortedSessions'])

onMounted(() => {
  if (sessions.value.length === 0) {
    store.dispatch('im/loadSessions')
  }
  window.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  window.removeEventListener('click', hideContextMenu)
})
</script>

<style scoped lang="scss">
.session-panel {
  display: flex;
  flex-direction: column;
  width: 280px;
  flex-shrink: 0;
  border-right: 1px solid #f0f0f0;
  background: #fff;
  height: 100%;

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 16px 10px;
    flex-shrink: 0;
    
    h1 { font-size: 18px; font-weight: 600; color: #1f2329; }
    .plus-btn { 
      background: none; border: none; padding: 4px; color: #646a73; cursor: pointer; border-radius: 4px;
      display: flex; align-items: center;
      &:hover { background: #f2f3f5; color: #1677ff; }
    }
  }

  .search-section {
    padding: 8px 16px 12px;
    .search-container {
      position: relative; height: 32px; background: #f2f3f5; border-radius: 4px; padding: 0 10px;
      display: flex; align-items: center; gap: 8px;
      .search-icon { font-size: 16px; color: #8f959e; }
      .search-input { flex: 1; border: none; background: transparent; outline: none; font-size: 13px; color: #1f2329; }
    }
  }

  .session-list {
    flex: 1; overflow-y: auto;
    .session-item {
      display: flex; padding: 12px 16px; cursor: pointer; position: relative; gap: 12px;
      transition: background 0.15s;
      
      &:hover { background: #f5f6f7; }
      &.active { background: #e8f3ff; .session-name { color: #1677ff; } }
      &.pinned { background: #f8fafc; &.active { background: #e8f3ff; } }

      .avatar-wrapper {
        position: relative; flex-shrink: 0;
        .session-avatar { width: 44px; height: 44px; border-radius: 6px; overflow: hidden; display: flex; align-items: center; justify-content: center; }
        .group-avatar { background: linear-gradient(135deg, #1677ff, #00d2ff); color: #fff; }
        
        .status-dot {
          position: absolute; bottom: -1px; right: -1px; width: 12px; height: 12px; border: 2px solid #fff; border-radius: 50%;
          &.online { background: #52c41a; }
          &.offline { background: #8f959e; }
        }
        
        .unread-badge {
          position: absolute; top: -4px; right: -6px; padding: 0 5px; height: 18px; min-width: 18px;
          background: #f54a45; color: #fff; font-size: 11px; border-radius: 9px;
          display: flex; align-items: center; justify-content: center; font-weight: 600; border: 2px solid #fff;
          z-index: 5;
          &.badge-dot { width: 10px; height: 10px; min-width: 10px; top: -2px; right: -2px; padding: 0; }
        }
      }

      .session-info {
        flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; gap: 4px;
        .session-header {
          display: flex; justify-content: space-between; align-items: center;
          .session-name { font-size: 14px; font-weight: 500; color: #1f2329; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
          .session-time { font-size: 11px; color: #8f959e; }
        }
        .session-bottom {
          display: flex; align-items: center; gap: 4px;
          .session-preview { font-size: 12px; color: #8f959e; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; line-height: 1.2; }
          .mention-tag { color: #f54a45; font-size: 12px; flex-shrink: 0; }
          .pin-tag { color: #1677ff; font-size: 12px; flex-shrink: 0; }
          .mute-icon { font-size: 13px; color: #8f959e; flex-shrink: 0; }
        }
      }
    }
  }
}

.context-menu {
  position: fixed; background: #fff; border-radius: 8px; box-shadow: 0 4px 16px rgba(0,0,0,0.12); padding: 5px 0; min-width: 140px; z-index: 2000;
  .menu-item {
    padding: 8px 16px; font-size: 13px; color: #1f2329; cursor: pointer;
    &:hover { background: #f2f3f5; color: #1677ff; }
    &.danger { color: #f54a45; &:hover { background: #fff1f0; } }
  }
  .menu-divider { height: 1px; background: #f0f0f0; margin: 4px 0; }
}

:global(.dark) {
  .session-panel {
    background: #1e293b; border-color: #334155;
    .session-item {
      &:hover { background: #334155; }
      &.active { background: #1e3a5f; }
      &.pinned { background: rgba(255,255,255,0.02); &.active { background: #1e3a5f; } }
      .avatar-wrapper .status-dot, .unread-badge { border-color: #1e293b; }
    }
    .panel-header h1, .session-name { color: #f1f5f9; }
    .search-section .search-container { background: #0f172a; .search-input { color: #f1f5f9; } }
  }
  .context-menu { background: #2d3748; border: 1px solid #4a5568; .menu-item { color: #e2e8f0; &:hover { background: #4a5568; } } }
}

/* 滚动条 */
.session-list::-webkit-scrollbar { width: 4px; }
.session-list::-webkit-scrollbar-track { background: transparent; }
.session-list::-webkit-scrollbar-thumb { background: transparent; border-radius: 2px; }
.session-list:hover::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); }
:global(.dark) .session-list:hover::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); }
</style>
