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
          <div
            class="session-avatar"
            :class="getAvatarBgClass(session)"
          >
            <template v-if="session.type === 'GROUP'">
              <span class="material-icons-outlined text-xl">groups</span>
            </template>
            <template v-else>
              {{ (session.name?.charAt(0) || '?').toUpperCase() }}
            </template>
          </div>
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

const props = defineProps({
  currentSession: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['select-session'])
const store = useStore()

const term = ref('')
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
    // 如果是消息，可能需要滚动到消息位置
    // 目前简单处理，如果是会话直接打开
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

  // 今天
  if (date >= today) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  // 昨天
  if (date >= yesterday) {
    return '昨天'
  }
  // 今年
  if (date.getFullYear() === now.getFullYear()) {
    return `${date.getMonth() + 1}/${date.getDate()}`
  }
  // 更早
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
    padding: 16px 16px 8px;
    flex-shrink: 0;

    h1 {
      font-size: 18px;
      font-weight: 600;
      color: #1f2329;

      .dark & {
        color: #f1f5f9;
      }
    }
  }

  .search-section {
    padding: 8px 16px 12px;
    flex-shrink: 0;

    .search-container {
      position: relative;
      display: flex;
      align-items: center;
      background: #f2f3f5;
      border-radius: 18px;
      padding: 0 12px;
      height: 32px;
      transition: all 0.2s;

      .dark & {
        background: #0f172a;
      }

      &:focus-within {
        background: #fff;
        box-shadow: 0 0 0 1px #0089ff;
        
        .dark & {
          background: #1e293b;
        }
      }

      .search-icon {
        font-size: 16px;
        color: #8f959e;
        margin-right: 8px;
      }

      .search-input {
        flex: 1;
        background: transparent;
        border: none;
        outline: none;
        font-size: 13px;
        color: #1f2329;
        
        &::placeholder {
          color: #8f959e;
        }

        .dark & {
          color: #f1f5f9;
        }
      }
    }
  }

  .session-list {
    flex: 1;
    overflow-y: auto;
    padding-bottom: 20px;

    .session-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      cursor: pointer;
      transition: background-color 0.15s;
      position: relative;

      &:hover {
        background: #f5f6f7;

        .dark & {
          background: rgba(255, 255, 255, 0.05);
        }
      }

      &.active {
        background: #e1f0ff;

        .dark & {
          background: rgba(0, 137, 255, 0.15);
        }
      }

      &.pinned {
        background: #f8f9fa;

        .dark & {
          background: rgba(255, 255, 255, 0.02);
        }

        &.active {
          background: #e1f0ff;
        }
      }

      .avatar-wrapper {
        position: relative;
        flex-shrink: 0;

        .status-dot {
          position: absolute;
          bottom: 0;
          right: 0;
          width: 10px;
          height: 10px;
          border-radius: 50%;
          border: 2px solid #fff;
          z-index: 2;

          &.online {
            background-color: #52c41a;
          }

          &.offline {
            background-color: #8f959e;
          }

          .dark & {
            border-color: #1e293b;
          }
        }
      }

      .session-avatar {
        width: 44px;
        height: 44px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 500;
        font-size: 16px;
        color: #fff;
        overflow: hidden;
      }

      .unread-badge {
        position: absolute;
        top: -4px;
        right: -6px;
        background-color: #f54a45;
        color: white;
        font-size: 11px;
        font-weight: 600;
        min-width: 18px;
        height: 18px;
        line-height: 14px;
        border: 2px solid #fff;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 4px;
        z-index: 10;
        box-shadow: 0 1px 2px rgba(0,0,0,0.1);

        &.badge-dot {
          min-width: 10px;
          height: 10px;
          padding: 0;
          top: -2px;
          right: -2px;
        }

        .dark & {
          border-color: #1e293b;
        }
      }

      .session-info {
        flex: 1;
        min-width: 0;
        margin-left: 12px;

        .session-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 4px;

          .session-name-group {
            display: flex;
            align-items: center;
            flex: 1;
            min-width: 0;

            .session-name {
              font-size: 14px;
              font-weight: 500;
              color: #1f2329;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;

              .dark & {
                color: #f1f5f9;
              }
            }

            .mute-icon {
              font-size: 14px;
              color: #8f959e;
              margin-left: 4px;
              flex-shrink: 0;
            }
          }

          .session-time {
            font-size: 11px;
            color: #8f959e;
            flex-shrink: 0;
            margin-left: 8px;
          }
        }

        .session-bottom {
          display: flex;
          align-items: center;
          
          .session-preview {
            font-size: 12px;
            color: #8f959e;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            line-height: 1.4;

            .pin-tag, .mention-tag {
              margin-right: 4px;
            }

            .mention-tag {
              color: #f54a45;
              font-weight: 500;
            }

            .pin-tag {
              color: #0089ff;
            }

            .sender-name {
              color: #646a73;
              
              .dark & {
                color: #94a3b8;
              }
            }
          }
        }
      }
    }

    .context-menu {
      position: fixed;
      background: #fff;
      border: 1px solid #e8e8e8;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
      z-index: 2000;
      padding: 4px 0;
      min-width: 160px;

      .dark & {
        background: #1e293b;
        border-color: #334155;
      }

      .menu-item {
        padding: 10px 16px;
        font-size: 13px;
        color: #1f2329;
        cursor: pointer;
        transition: background-color 0.15s;

        .dark & {
          color: #cbd5e1;
        }

        &:hover {
          background: #f5f6f7;
          color: #0089ff;

          .dark & {
            background: #334155;
            color: #60a5fa;
          }
        }

        &.danger {
          color: #f54a45;

          &:hover {
            background: #fff0f0;

            .dark & {
              background: rgba(245, 74, 69, 0.1);
            }
          }
        }
      }

      .menu-divider {
        height: 1px;
        background: #f2f3f5;
        margin: 4px 0;

        .dark & {
          background: #334155;
        }
      }
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      padding: 40px 20px;
    }
  }
}

/* 滚动条样式优化 */
.session-list::-webkit-scrollbar {
  width: 5px;
}

.session-list::-webkit-scrollbar-track {
  background: transparent;
}

.session-list::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 10px;
}

.session-list:hover::-webkit-scrollbar-thumb {
  background: rgba(31, 35, 41, 0.15);
  
  .dark & {
    background: rgba(255, 255, 255, 0.15);
  }
}
</style>
