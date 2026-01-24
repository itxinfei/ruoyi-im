<template>
  <div class="session-panel">
    <div class="panel-header">
      <h1 class="text-xl font-bold dark:text-white">消息</h1>
      <el-dropdown trigger="click" @command="handleCommand">
        <button class="text-slate-500 hover:text-primary transition-colors">
          <span class="material-icons-outlined text-xl">add</span>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="group">
              <span class="material-icons-outlined text-sm mr-2">group</span>
              创建群组
            </el-dropdown-item>
            <el-dropdown-item command="chat">
              <span class="material-icons-outlined text-sm mr-2">chat</span>
              发起单聊
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="search-bar">
      <div class="relative">
        <span class="material-icons-outlined absolute left-3 top-2.5 text-slate-400 text-sm">search</span>
        <input
          v-model="searchKeyword"
          class="w-full bg-slate-100 dark:bg-slate-900 border-none rounded-md py-1.5 pl-9 pr-4 text-sm focus:ring-1 focus:ring-primary dark:text-slate-200 placeholder-slate-400"
          placeholder="搜索..."
          type="text"
        />
      </div>
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
          <div
            class="session-avatar"
            :class="getAvatarBgClass(session)"
          >
            {{ (session.name?.charAt(0) || '?').toUpperCase() }}
          </div>
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
              <span v-if="session.isMuted" class="material-icons-outlined text-xs text-slate-400 ml-1">notifications_off</span>
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
  max-width: 280px;
  flex-shrink: 0;
  border-right: 1px solid #e8e8e8;
  background: #fff;
  height: 100%;

  .dark & {
    background: #1e293b;
    border-right-color: #334155;
  }

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    border-bottom: 1px solid #f0f0f0;
    flex-shrink: 0;

    .dark & {
      border-bottom-color: #334155;
    }
  }

  .search-bar {
    padding: 8px 12px;
    border-bottom: 1px solid #f0f0f0;
    flex-shrink: 0;

    .dark & {
      border-bottom-color: #334155;
    }
  }

  .session-list {
    flex: 1;
    overflow-y: auto;

    .session-item {
      display: flex;
      align-items: center;
      padding: 10px 14px;
      cursor: pointer;
      transition: background-color 0.15s;
      position: relative;
      border-left: 3px solid transparent;

      &:hover {
        background: #f2f3f5;

        .dark & {
          background: rgba(255, 255, 255, 0.05);
        }
      }

      &.active {
        background: #ebf4ff;
        border-left-color: #1677ff;

        .dark & {
          background: rgba(22, 119, 255, 0.15);
        }
      }

      .avatar-wrapper {
        flex-shrink: 0;
      }

      .session-avatar {
        width: 44px;
        height: 44px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 500;
        font-size: 16px;
        color: #fff;
      }

      .unread-badge {
        position: absolute;
        top: 6px;
        left: 42px;
        background-color: #ef4444;
        color: white;
        font-size: 10px;
        min-width: 16px;
        height: 16px;
        line-height: 12px;
        border: 2px solid #fff;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 3px;
        z-index: 10;
      }

      .session-info {
        flex: 1;
        min-width: 0;
        margin-left: 10px;

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
              color: #0f172a;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;

              .dark & {
                color: #f1f5f9;
              }
            }
          }

          .session-time {
            font-size: 11px;
            color: #94a3b8;
            flex-shrink: 0;
            margin-left: 6px;
          }
        }

        .session-preview {
          font-size: 12px;
          color: #475569;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;

          .dark & {
            color: #94a3b8;
          }

          .sender-name {
            color: #94a3b8;
          }
        }
      }
    }

    .context-menu {
      position: fixed;
      background: #fff;
      border: 1px solid #e8e8e8;
      border-radius: 8px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      z-index: 2000;
      padding: 4px 0;
      min-width: 120px;

      .dark & {
        background: #1e293b;
        border-color: #334155;
      }

      .menu-item {
        padding: 8px 14px;
        font-size: 13px;
        color: #595959;
        cursor: pointer;
        transition: background-color 0.15s;

        .dark & {
          color: #cbd5e1;
        }

        &:hover {
          background: #f5f5f5;
          color: #1677ff;

          .dark & {
            background: #334155;
            color: #60a5fa;
          }
        }

        &.danger {
          color: #ef4444;

          .dark & {
            color: #f87171;
          }

          &:hover {
            background: #fef2f2;

            .dark & {
              background: rgba(239, 68, 68, 0.1);
            }
          }
        }
      }

      .menu-divider {
        height: 1px;
        background: #f0f0f0;
        margin: 4px 0;

        .dark & {
          background: #334155;
        }
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
