<template>
  <div :class="['session-panel', { 'dark': isDark }]">
    <div class="panel-header">
      <h1 class="text-xl font-bold">消息</h1>
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
            @click="handleAvatarClick($event, session)"
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
import { useTheme } from '@/composables/useTheme'
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
const { isDark } = useTheme()

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
  } else {
    // 群组点击头像暂不处理或弹出群详情
    emit('select-session', session)
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
const handleGroupCreated = (groupData) => {
  ElMessage.success('群组创建成功')
  store.dispatch('im/session/loadSessions')
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

// 使用 getter 中的排序会话
const sortedSessions = computed(() => store.getters['im/sortedSessions'])

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
/* 滚动条 */
.session-list::-webkit-scrollbar { width: 4px; }
.session-list::-webkit-scrollbar-track { background: transparent; }
.session-list::-webkit-scrollbar-thumb { background: transparent; border-radius: 2px; }
.session-list:hover::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); }
:global(.dark) .session-list:hover::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); }

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
    padding: 18px 16px 14px;
    flex-shrink: 0;
    
    h1 { font-size: 18px; font-weight: 600; color: #171a1d; letter-spacing: 0.5px; }
    .plus-btn { 
      background: none; border: none; padding: 6px; color: #171a1d; cursor: pointer; border-radius: 8px;
      display: flex; align-items: center; transition: all 0.2s;
      &:hover { background: rgba(0,0,0,0.05); }
    }
  }

  .search-section {
    padding: 0 16px 16px;
    .search-container {
      position: relative; height: 36px; background: #edeff2; border-radius: 999px; padding: 0 12px;
      display: flex; align-items: center; gap: 8px; transition: all 0.2s;
      border: 1px solid transparent;

      &:focus-within {
        background: #fff;
        border-color: #0089ff;
        box-shadow: 0 0 0 2px rgba(0, 137, 255, 0.1);
      }

      .search-icon { font-size: 18px; color: #858b9c; }
      .search-input { 
        flex: 1; border: none; background: transparent; outline: none; font-size: 14px; color: #1f2329; 
        &::placeholder { color: #8f959e; }
      }
    }
  }

  .session-list {
    flex: 1; overflow-y: auto;
    padding: 0 0 8px 0;
    
    .session-item {
      display: flex; padding: 14px 16px; cursor: pointer; position: relative; gap: 12px;
      transition: background 0.15s;
      
      &:hover { background: rgba(0,0,0,0.02); }
      
      &.active { 
        background: var(--dt-bg-session-active); 
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 14px;
          bottom: 14px;
          width: 3px;
          background: var(--dt-brand-color);
          border-radius: 0 4px 4px 0;
        }

        .session-info .session-header .session-name { color: #1f2329; font-weight: 600; }
      }
      
      &.pinned { 
        background: #f7f8fa; 
        &.active { background: #e5f4ff; } 
      }

      .avatar-wrapper {
        position: relative; flex-shrink: 0;
        .session-avatar { width: 44px; height: 44px; border-radius: 14px; overflow: hidden; display: flex; align-items: center; justify-content: center; }
        .group-avatar { background: linear-gradient(135deg, #1677ff, #00d2ff); color: #fff; box-shadow: 0 4px 10px rgba(22, 119, 255, 0.2); }
        
        :deep(.session-avatar-item) {
          border-radius: 12px !important;
        }

        .status-dot {
          position: absolute; bottom: 0; right: 0; width: 12px; height: 12px; border: 2px solid #fff; border-radius: 50%;
          &.online { background: #52c41a; }
          &.offline { background: #d9d9d9; }
        }
        
        .unread-badge {
          position: absolute; top: -6px; right: -6px; padding: 0 4px; height: 18px; min-width: 18px;
          background: #ff4d4f; color: #fff; font-size: 10px; border-radius: 99px;
          display: flex; align-items: center; justify-content: center; font-weight: 600; border: 2px solid #fff;
          z-index: 5;
          box-shadow: 0 2px 6px rgba(255, 77, 79, 0.2);
          &.badge-dot { width: 10px; height: 10px; min-width: 10px; top: -2px; right: -2px; padding: 0; }
        }
      }

      .session-info {
        flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; gap: 4px;
        .session-header {
          display: flex; justify-content: space-between; align-items: center;
          .session-name { font-size: 15px; font-weight: 500; color: #1f2329; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; transition: color 0.2s; }
          .session-time { font-size: 11px; color: #babbc0; font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', sans-serif; }
        }
        .session-bottom {
          display: flex; align-items: center; gap: 4px;
          .session-preview { font-size: 13px; color: #8f959e; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; line-height: 1.4; flex: 1; }
          .mention-tag { color: #ff4d4f; font-size: 12px; flex-shrink: 0; font-weight: 500; }
          .pin-tag { 
            background: rgba(0, 137, 255, 0.1); color: #0089ff; font-size: 10px; padding: 0 4px; border-radius: 3px; flex-shrink: 0; margin-right: 4px; 
          }
          .mute-icon { font-size: 14px; color: #c4c6cc; flex-shrink: 0; }
        }
      }
    }
  }
}

.context-menu {
  position: fixed; 
  background: rgba(255, 255, 255, 0.95); 
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 12px; 
  box-shadow: 0 8px 24px rgba(0,0,0,0.12), 0 2px 8px rgba(0,0,0,0.04); 
  padding: 6px; 
  min-width: 160px; 
  z-index: 2000;
  border: 1px solid rgba(0,0,0,0.06);

  .menu-item {
    padding: 10px 12px; font-size: 13px; color: #1f2329; cursor: pointer; border-radius: 6px;
    transition: all 0.15s;
    font-weight: 500;
    
    &:hover { background: rgba(0,0,0,0.04); color: #0089ff; }
    &.danger { color: #ff4d4f; &:hover { background: #fff1f0; } }
  }
  .menu-divider { height: 1px; background: rgba(0,0,0,0.06); margin: 4px 6px; }
}

:global(.dark) {
  .session-panel {
    background: #111a2c; border-right-color: #2b3648;
    
    .panel-header {
      h1 { color: #f1f5f9; }
      .plus-btn { color: #94a3b8; &:hover { background: rgba(255,255,255,0.08); color: #f1f5f9; } }
    }

    .search-section .search-container { 
      background: #1e293b; 
      .search-icon { color: #64748b; }
      .search-input { color: #f1f5f9; &::placeholder { color: #475569; } }
      &:focus-within { background: #0f172a; border-color: #38bdf8; }
    }

    .session-list {
      .session-item {
        &:hover { background: rgba(255,255,255,0.03); }
        &.active { 
          background: #1e3a5f; 
          &::before { background: #38bdf8; }
          .session-info .session-header .session-name { color: #f1f5f9; }
        }
        &.pinned { background: rgba(255,255,255,0.02); }
        
        .avatar-wrapper .status-dot { background: #22c55e; border-color: #1e293b; &.offline { background: #64748b; } }
        .avatar-wrapper .unread-badge { border-color: #1e293b; }
        
        .session-info {
          .session-header {
            .session-name { color: #e2e8f0; }
          }
          .session-bottom {
            .pin-tag { background: rgba(56, 189, 248, 0.15); color: #38bdf8; }
            .mute-icon { color: #475569; }
          }
        }
      }
    }
  }
  
  .context-menu { 
    background: rgba(30, 41, 59, 0.95); 
    border-color: rgba(255,255,255,0.08);
    .menu-item { 
      color: #e2e8f0; 
      &:hover { background: rgba(255,255,255,0.08); color: #38bdf8; }
      &.danger:hover { background: rgba(239, 68, 68, 0.15); }
    }
    .menu-divider { background: rgba(255,255,255,0.08); }
  }
}
</style>
