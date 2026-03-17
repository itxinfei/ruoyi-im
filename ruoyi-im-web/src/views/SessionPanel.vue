<template>
  <div class="session-panel">
    <!-- 1. 头部搜索与过滤 -->
    <div class="panel-header">
      <div class="header-action-row">
        <div class="search-box-unified">
          <el-icon class="search-icon"><Search /></el-icon>
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索"
            type="text"
            @focus="showGlobalSearch = true"
          />
        </div>
        <button class="icon-add-btn" @click="handleCreateGroup" title="发起群聊">
          <el-icon><Plus /></el-icon>
        </button>
      </div>

      <!-- 紧凑页签 (补齐样式) -->
      <div class="sub-tabs-compact">
        <div
          v-for="tab in subMenuTabs"
          :key="tab.key"
          class="tab-pill"
          :class="{ active: activeFilter === tab.key }"
          @click="handleFilterChange(tab.key)"
        >
          <span class="tab-label">{{ tab.label }}</span>
          <span v-if="tab.count > 0" class="tab-badge">{{ tab.count }}</span>
        </div>
      </div>
    </div>

    <!-- 2. 会话列表 (极致流畅滚动) -->
    <div v-loading="loading" class="session-list custom-scrollbar">
      <div
        v-for="session in sortedSessions"
        :key="session.id"
        class="session-item"
        :class="{ active: currentSession?.id === session.id, pinned: session.isPinned }"
        @click="$emit('select-session', session)"
        @contextmenu.prevent="handleContextMenu($event, session)"
      >
        <div class="item-avatar-wrapper">
          <DingtalkAvatar 
            :src="session.avatar" 
            :name="session.name" 
            :user-id="session.targetId" 
            :is-group="session.type === 'GROUP'"
            :members="session.members || []"
            :size="40" 
            shape="square" 
          />
          <span v-if="session.unreadCount > 0" class="unread-count-badge">
            {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
          </span>
        </div>

        <div class="item-content">
          <div class="content-top">
            <h3 class="session-name">{{ session.name }}</h3>
            <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
          </div>
          <div class="content-bottom">
            <div class="preview-wrapper">
              <span v-if="session.hasMention" class="mention-text">[@提及]</span>
              <span v-if="session.lastSenderNickname && session.type === 'GROUP'" class="sender-prefix">{{ session.lastSenderNickname }}: </span>
              <span class="preview-msg">{{ session.lastMessage || '暂无消息' }}</span>
            </div>
            <div class="status-icons">
              <el-icon v-if="session.isMuted" class="mute-icon"><BellFilled /></el-icon>
            </div>
          </div>
        </div>
        <div v-if="session.isPinned" class="pin-tag"></div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && sortedSessions.length === 0" class="empty-state">
        <span class="material-icons-outlined empty-icon">chat_bubble_outline</span>
        <p class="empty-text">暂无会话</p>
        <p class="empty-hint">开始对话或创建群聊吧</p>
      </div>
    </div>

    <!-- 全局搜索弹窗 -->
    <GlobalSearch v-model:visible="showGlobalSearch" :keyword="searchKeyword" @select="handleSearchSelect" />

    <!-- 创建群聊对话框 -->
    <CreateGroupDialog
      v-model="showCreateGroupDialog"
      @created="handleGroupCreated"
    />

    <!-- 右键菜单 (Element Plus 改版) -->
    <teleport to="body">
      <div
        v-if="contextMenu.show"
        class="context-menu-layer"
        :style="contextMenuStyle"
        @click.stop
        v-click-outside="closeContextMenu"
        @keydown.esc="closeContextMenu"
      >
        <div class="menu-item" @click="doAction('mark-read')">
          <el-icon><Check /></el-icon>
          <span>标记已读</span>
        </div>
        <div class="menu-item" @click="doAction('toggle-pin')">
          <el-icon><Top /></el-icon>
          <span>{{ contextMenu.session?.isPinned ? '取消置顶' : '置顶会话' }}</span>
        </div>
        <div class="menu-divider"></div>
        <div class="menu-item danger" @click="doAction('delete')">
          <el-icon><Delete /></el-icon>
          <span>删除会话</span>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, reactive } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElDialog } from 'element-plus'
import { Search, Plus, BellFilled, Check, Top, Delete } from '@element-plus/icons-vue'
import { ClickOutside as vClickOutside } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import GlobalSearch from '@/components/Chat/GlobalSearch.vue'
import CreateGroupDialog from '@/components/Chat/CreateGroupDialog.vue'

const props = defineProps({ currentSession: Object })
const emit = defineEmits(['select-session', 'show-user'])
const store = useStore()

const loading = ref(false)
const searchKeyword = ref('')
const showGlobalSearch = ref(false)
const activeFilter = ref('all')
const showCreateGroupDialog = ref(false)

const subMenuTabs = ref([
  { key: 'all', label: '全部', count: 0 },
  { key: 'unread', label: '未读', count: 0 },
  { key: 'pinned', label: '置顶', count: 0 }
])

const sessions = computed(() => store.state.im.session.sessions)

// 保存全部会话（用于统计各筛选类型的数量）
const allSessions = ref([])

// 各筛选类型的数量
const filterCounts = computed(() => {
  const all = allSessions.value
  return {
    all: all.length,
    unread: all.filter(s => s.unreadCount > 0).length,
    pinned: all.filter(s => s.isPinned).length
  }
})

// 更新筛选菜单的数量显示
const updateFilterCounts = () => {
  const counts = filterCounts.value
  subMenuTabs.value = subMenuTabs.value.map(tab => ({
    ...tab,
    count: counts[tab.key] || 0
  }))
}

const sortedSessions = computed(() => {
  return [...sessions.value].sort((a, b) => {
    if (a.isPinned !== b.isPinned) return a.isPinned ? -1 : 1
    const timeA = new Date(a.lastMessageTime || 0).getTime()
    const timeB = new Date(b.lastMessageTime || 0).getTime()
    return timeB - timeA
  })
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  if (date.toDateString() === now.toDateString()) {
    return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0')
  }
  return (date.getMonth() + 1) + '-' + date.getDate()
}

const contextMenu = reactive({ show: false, x: 0, y: 0, session: null })
const contextMenuStyle = computed(() => ({ left: `${contextMenu.x}px`, top: `${contextMenu.y}px` }))

const handleContextMenu = (e, session) => {
  // 计算菜单位置，确保不超出视窗
  const menuWidth = 180
  const menuHeight = 140
  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight

  let x = e.clientX
  let y = e.clientY

  // 右边界检测
  if (x + menuWidth > windowWidth) {
    x = windowWidth - menuWidth - 8
  }
  // 下边界检测
  if (y + menuHeight > windowHeight) {
    y = windowHeight - menuHeight - 8
  }

  contextMenu.show = true
  contextMenu.x = x
  contextMenu.y = y
  contextMenu.session = session

  // 添加键盘监听
  document.addEventListener('keydown', handleEscKey)
  // 点击外部关闭
  setTimeout(() => {
    document.addEventListener('click', closeContextMenu, { once: true })
  }, 10)
}

const closeContextMenu = () => {
  contextMenu.show = false
  document.removeEventListener('keydown', handleEscKey)
}

const handleEscKey = (e) => {
  if (e.key === 'Escape') {
    closeContextMenu()
  }
}

const handleFilterChange = (key) => { 
  activeFilter.value = key
  // 始终保持 allSessions 为完整的会话列表用于计数
  if (key === 'all') {
    // 切换到"全部"时重新加载以确保最新
    store.dispatch('im/session/loadSessions', 'all').then(() => {
      allSessions.value = store.state.im.session.sessions
      updateFilterCounts()
    })
  } else {
    // 切换到其他筛选时，也保持 allSessions 不变用于计数显示
    // 这样用户切换筛选再切回"全部"时，计数不会丢失
    if (allSessions.value.length === 0) {
      allSessions.value = store.state.im.session.sessions
    }
    // 前端筛选显示
    store.dispatch('im/session/loadSessions', 'all').then(() => {
      // 前端筛选逻辑
      const allSess = store.state.im.session.sessions
      let filteredSess = allSess
      if (key === 'unread') {
        filteredSess = allSess.filter(s => s.unreadCount > 0)
      } else if (key === 'pinned') {
        filteredSess = allSess.filter(s => s.isPinned)
      }
      // 临时更新sessions用于显示
      store.commit('im/session/SET_SESSIONS', filteredSess)
      updateFilterCounts()
    })
  }
}

const handleCreateGroup = () => {
  showCreateGroupDialog.value = true
}

const handleGroupCreated = (group) => {
  showCreateGroupDialog.value = false
  // 刷新会话列表
  store.dispatch('im/session/loadSessions')
  // 选择新创建的群聊
  emit('select-session', group)
  ElMessage.success('群聊创建成功')
}

const handleSearchSelect = (result) => {
  showGlobalSearch.value = false
  if (result.sessionId) {
    const session = sessions.value.find(s => s.id === result.sessionId)
    if (session) emit('select-session', session)
  }
}

const doAction = (cmd) => { 
  contextMenu.show = false
  if (!contextMenu.session) return
  
  switch(cmd) {
    case 'mark-read':
      store.dispatch('im/session/markSessionAsRead', contextMenu.session.id)
      break
    case 'toggle-pin':
      store.dispatch('im/session/pinSession', { 
        sessionId: contextMenu.session.id, 
        pinned: !contextMenu.session.isPinned 
      })
      break
    case 'delete':
      ElMessageBox.confirm('确定删除该会话吗？', '提示', { type: 'warning' })
        .then(() => {
          store.dispatch('im/session/deleteSession', contextMenu.session.id)
        })
        .catch(() => {})
      break
  }
}

onMounted(async () => {
  if (store.state.user?.token) {
    // 先加载全部会话（用于统计数量）
    await store.dispatch('im/session/loadSessions', 'all')
    // 保存全部会话用于统计
    allSessions.value = store.state.im.session.sessions
    // 更新筛选计数
    updateFilterCounts()
  }
})
</script>

<style scoped lang="scss">
// ============================================================================
// 会话面板容器 - 锁定 280px 黄金中轴
// ============================================================================
@mixin flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

@mixin text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-panel {
  width: var(--dt-session-panel-width);
  height: 100%;
  border-right: 1px solid var(--dt-border-light);
  background: var(--dt-bg-session-list);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  min-width: 0;
}

// ============================================================================
// 头部搜索区
// ============================================================================
.panel-header {
  padding: var(--dt-spacing-md) var(--dt-spacing-md) var(--dt-spacing-sm);
  flex-shrink: 0;

  .header-action-row {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-sm);
    margin-bottom: var(--dt-spacing-md);
  }

  .search-box-unified {
    flex: 1;
    position: relative;
    height: var(--dt-btn-height-sm);
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-sm);
    display: flex;
    align-items: center;
    transition: all var(--dt-transition-base);

    &:focus-within {
      background: var(--dt-bg-input);
      box-shadow: 0 0 0 1px var(--dt-brand-color);
    }

    .search-icon {
      position: absolute;
      left: var(--dt-spacing-sm);
      font-size: 14px;
      color: var(--dt-text-tertiary);
    }

    .search-input {
      width: 100%;
      border: none;
      background: transparent;
      padding: 0 32px;
      font-size: var(--dt-font-size-sm);
      outline: none;
      color: var(--dt-text-primary);
      &::placeholder { color: var(--dt-text-quaternary); }
    }
  }

  .icon-add-btn {
    width: var(--dt-btn-height-sm);
    height: var(--dt-btn-height-sm);
    border: none;
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-sm);
    cursor: pointer;
    color: var(--dt-text-primary);
    @include flex-center;
    transition: all var(--dt-transition-fast);

    &:hover { background: var(--dt-border-light); color: var(--dt-brand-color); }
    &:active { transform: scale(0.95); }
  }
}

// ============================================================================
// 导航页签
// ============================================================================
.sub-tabs-compact {
  display: flex;
  gap: var(--dt-spacing-xs);
  padding-bottom: var(--dt-spacing-xs);
  border-bottom: 1px solid var(--dt-border-lighter);

  .tab-pill {
    padding: 4px var(--dt-spacing-sm);
    font-size: var(--dt-font-size-xs);
    color: var(--dt-text-secondary);
    border-radius: var(--dt-radius-full);
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
    transition: all var(--dt-transition-fast);

    &:hover { background: var(--dt-bg-session-hover); }
    &.active { 
      background: var(--dt-brand-lighter); 
      color: var(--dt-brand-color); 
      font-weight: var(--dt-font-weight-medium); 
    }

    .tab-badge {
      background: var(--dt-error-color);
      color: var(--dt-bg-card);
      padding: 0 4px;
      border-radius: var(--dt-radius-full);
      font-size: 10px;
      transform: scale(0.85);
    }
  }
}

// ============================================================================
// 列表项渲染
// ============================================================================
.session-list {
  flex: 1;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--dt-spacing-xl);
  color: var(--dt-text-tertiary);

  .empty-icon {
    font-size: 48px;
    margin-bottom: var(--dt-spacing-md);
    opacity: 0.5;
  }

  .empty-text {
    font-size: var(--dt-font-size-md);
    margin: 0;
  }

  .empty-hint {
    font-size: var(--dt-font-size-sm);
    margin: var(--dt-spacing-xs) 0 0;
    opacity: 0.7;
  }
}

.session-item {
  display: flex;
  padding: var(--dt-spacing-md);
  cursor: pointer;
  position: relative;
  transition: all var(--dt-transition-base);
  border-bottom: 1px solid transparent;

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  &.active {
    background: var(--dt-bg-session-active);
    .session-name { color: var(--dt-brand-color); }
  }

  &.pinned {
    background: var(--dt-bg-body);
    border-left: 3px solid var(--dt-brand-color);

    &::before {
      content: '\e6a1'; // Element Plus 置顶图标
      font-family: 'element-icons';
      position: absolute;
      top: 4px;
      right: 4px;
      font-size: 10px;
      color: var(--dt-brand-color);
      opacity: 0.7;
    }

    .session-name::before {
      content: '[置顶] ';
      color: var(--dt-brand-color);
      font-weight: 600;
      font-size: 12px;
    }
  }

  .item-avatar-wrapper {
    position: relative;
    flex-shrink: 0;

    .unread-count-badge {
      position: absolute;
      top: -2px;
      right: -2px;
      background: var(--dt-error-color);
      color: var(--dt-bg-card);
      font-size: 10px;
      min-width: 16px;
      height: 16px;
      padding: 0 4px;
      border-radius: var(--dt-radius-full);
      @include flex-center;
      border: 2px solid var(--dt-bg-session-list);
      box-shadow: 0 2px 4px rgba(245, 74, 69, 0.2);
      font-weight: 600;
      z-index: 1;
    }
  }

  .item-content {
    flex: 1;
    margin-left: var(--dt-spacing-md);
    min-width: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 2px;

    .content-top {
      display: flex;
      justify-content: space-between;
      align-items: baseline;

      .session-name {
        font-size: var(--dt-font-size-md);
        font-weight: var(--dt-font-weight-medium);
        color: var(--dt-text-primary);
        margin: 0;
        @include text-ellipsis;
      }

      .session-time {
        font-size: var(--dt-font-size-xs);
        color: var(--dt-text-tertiary);
        flex-shrink: 0;
        margin-left: 8px;
      }
    }

    .content-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 2px;

      .preview-wrapper {
        display: flex;
        align-items: center;
        gap: 4px;
        min-width: 0;
        flex: 1;
        font-size: var(--dt-font-size-sm);
        color: var(--dt-text-tertiary);
        line-height: 1.4;
        @include text-ellipsis;

        .mention-text { color: var(--dt-error-color); font-weight: 500; font-size: 12px; }
        .sender-prefix { color: var(--dt-text-secondary); }
      }

      .mute-icon {
        font-size: 12px;
        color: var(--dt-text-quaternary);
        margin-left: 4px;
      }
    }
  }
}

// ============================================================================
// 右键菜单
// ============================================================================
.context-menu-layer {
  position: fixed;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  padding: 4px;
  min-width: 160px;
  z-index: var(--dt-z-max);
  box-shadow: var(--dt-shadow-modal);

  .menu-item {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-sm);
    padding: 8px var(--dt-spacing-md);
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-primary);
    cursor: pointer;
    border-radius: var(--dt-radius-sm);
    transition: all var(--dt-transition-fast);

    &:hover {
      background: var(--dt-brand-lighter);
      color: var(--dt-brand-color);
    }

    &.danger {
      color: var(--dt-error-color);
      &:hover { background: var(--dt-error-bg); color: var(--dt-error-color); }
    }

    .el-icon { font-size: 16px; }

    span {
      flex: 1;
    }

    .shortcut {
      font-size: 11px;
      color: var(--dt-text-quaternary);
      margin-left: auto;
      padding-left: var(--dt-spacing-md);
    }
  }

  .menu-divider {
    height: 1px;
    background: var(--dt-border-lighter);
    margin: 4px 0;
  }
}
</style>
