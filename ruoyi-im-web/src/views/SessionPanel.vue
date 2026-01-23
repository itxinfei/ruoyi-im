<template>
  <div class="session-panel">
    <div class="panel-header">
      <h1 class="text-xl font-bold dark:text-white">消息</h1>
      <el-dropdown trigger="click" @command="handleCommand">
        <button class="text-slate-500 hover:text-primary transition-colors">
          <el-icon class="text-xl"><Plus /></el-icon>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="group">
              <el-icon><UserFilled /></el-icon>
              创建群组
            </el-dropdown-item>
            <el-dropdown-item command="chat">
              <el-icon><ChatDotRound /></el-icon>
              发起单聊
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索会话..."
        :prefix-icon="Search"
        clearable
        size="small"
      />
    </div>

    <div v-loading="loading" class="session-list">
      <div
        v-for="session in sortedSessions"
        :key="session.id"
        class="session-item"
        :class="{ active: isActiveSession(session) }"
        @click="handleSessionClick(session)"
        @contextmenu.prevent="handleContextMenu($event, session)"
      >
        <div class="relative avatar-wrapper">
          <el-avatar 
            :size="48" 
            :src="session.avatar" 
            shape="square" 
            class="session-avatar"
            :class="getAvatarBgClass(session)"
          >
            {{ session.name?.charAt(0) }}
          </el-avatar>
          <span 
            v-if="session.unreadCount > 0" 
            class="unread-badge"
          >
            {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
          </span>
        </div>
        
        <div class="session-info">
          <div class="session-top">
            <div class="session-name-wrapper">
              <span class="session-name">{{ session.name }} {{ session.isPinned ? '⭐' : '' }}</span>
              <el-icon v-if="session.isMuted" class="muted-icon"><Mute /></el-icon>
            </div>
            <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
          </div>
          <div class="session-preview">
            <span v-if="session.lastSenderNickname" class="sender-name">{{ session.lastSenderNickname }}: </span>
            {{ session.lastMessage || '暂无消息' }}
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
import { Search, Plus, UserFilled, ChatDotRound, Mute } from '@element-plus/icons-vue'
import CreateGroupDialog from '@/components/CreateGroupDialog/index.vue'

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

const sessions = computed(() => store.state.im.sessions || [])
const loading = computed(() => store.state.im.loading.sessions)

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'group') {
    showCreateGroupDialog.value = true
  } else if (command === 'chat') {
    ElMessage.info('发起单聊请前往联系人页面选择好友')
  }
}

// 群组创建成功
const handleGroupCreated = (groupData) => {
  ElMessage.success('群组创建成功')
  store.dispatch('im/loadSessions')
}

// 过滤会话列表
const filteredSessions = computed(() => {
  const keyword = searchKeyword.value.toLowerCase()
  if (!keyword) return sessions.value
  
  return sessions.value.filter(session => 
    (session.name && session.name.toLowerCase().includes(keyword)) ||
    (session.lastMessage && session.lastMessage.toLowerCase().includes(keyword))
  )
})

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
  
  // 检查是否是今天
  if (date.toDateString() === now.toDateString()) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  const diff = now - date
  if (diff < 86400000 * 2) return '昨天'
  if (diff < 86400000 * 7) return `${Math.floor(diff / 86400000)}天前`
  
  return `${date.getMonth() + 1}/${date.getDate()}`
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

// 辅助方法：获取头像背景色
const getAvatarBgClass = (session) => {
  if (session.type === 'GROUP') return 'bg-primary';
  const colors = ['bg-blue-500', 'bg-orange-500', 'bg-emerald-500', 'bg-purple-500'];
  const id = session.id || 0;
  return colors[id % colors.length];
}

onMounted(() => {
  // 确保会话加载
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
  min-width: 280px;
  flex-shrink: 0;
  border-right: 1px solid #e8e8e8;
  background: #fff;
  height: 100%;
  
  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
    
    h3 {
      margin: 0;
      font-size: 15px;
      font-weight: 600;
      color: #262626;
    }
  }

  .search-bar {
    padding: 10px 12px;
    border-bottom: 1px solid #f0f0f0;
  }

  .session-list {
    flex: 1;
    overflow-y: auto;
    
    .session-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      cursor: pointer;
      transition: all 0.2s;
      position: relative;
      border-left: 4px solid transparent;
      
      &:hover {
        background: var(--dt-bg-session-hover);
      }
      
      &.active {
        background: var(--dt-bg-session-active);
        border-left-color: var(--dt-brand-color);
      }

      .avatar-wrapper {
        flex-shrink: 0;
      }

      .session-avatar {
        border-radius: 8px !important;
        font-weight: 500;
        color: #fff;
      }

      .unread-badge {
        position: absolute;
        top: -4px;
        right: -4px;
        background-color: var(--dt-badge-color);
        color: white;
        font-size: 10px;
        min-width: 18px;
        height: 18px;
        line-height: 14px;
        border: 2px solid #fff;
        border-radius: 9px;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 4px;
        z-index: 10;
      }
      
      .session-info {
        flex: 1;
        min-width: 0;
        margin-left: 12px;
        
        .session-top {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 2px;
          
          .session-name-wrapper {
            display: flex;
            align-items: center;
            flex: 1;
            min-width: 0;
            
            .session-name {
              font-size: 14px;
              font-weight: 500;
              color: var(--dt-text-primary);
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }

            .muted-icon {
              margin-left: 4px;
              font-size: 12px;
              color: #bfbfbf;
            }
          }
          
          .session-time {
            font-size: 11px;
            color: var(--dt-text-tertiary);
            flex-shrink: 0;
            margin-left: 8px;
          }
        }
        
        .session-preview {
          font-size: 12px;
          color: var(--dt-text-secondary);
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;

          .sender-name {
            color: var(--dt-text-tertiary);
          }
        }
      }
    }

    .context-menu {
      position: fixed;
      background: #fff;
      border: 1px solid #e8e8e8;
      border-radius: 4px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      z-index: 2000;
      padding: 4px 0;
      min-width: 120px;
      
      .menu-item {
        padding: 8px 16px;
        font-size: 13px;
        color: #595959;
        cursor: pointer;
        transition: all 0.2s;
        
        &:hover {
          background: #f5f5f5;
          color: #0089ff;
        }
        
        &.danger {
          color: #ff4d4f;
          &:hover {
            background: #fff1f0;
          }
        }
      }
      
      .menu-divider {
        height: 1px;
        background: #f0f0f0;
        margin: 4px 0;
      }
    }

    .empty-state {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
      padding: 40px 20px;
    }
  }
}
</style>
