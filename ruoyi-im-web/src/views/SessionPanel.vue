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
    </div>

    <!-- 全局搜索弹窗 -->
    <GlobalSearch v-model:visible="showGlobalSearch" :keyword="searchKeyword" @select="handleSearchSelect" />

    <!-- 右键菜单 (Element Plus 改版) -->
    <div v-if="contextMenu.show" class="context-menu-layer" :style="contextMenuStyle" @click.stop>
      <div class="menu-item" @click="doAction('mark-read')"><el-icon><Check /></el-icon> 标记已读</div>
      <div class="menu-item" @click="doAction('toggle-pin')"><el-icon><Top /></el-icon> {{ contextMenu.session?.isPinned ? '取消置顶' : '置顶会话' }}</div>
      <div class="menu-divider"></div>
      <div class="menu-item danger" @click="doAction('delete')"><el-icon><Delete /></el-icon> 删除会话</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Search, Plus, BellFilled, Check, Top, Delete } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import GlobalSearch from '@/components/Chat/GlobalSearch.vue'

const props = defineProps({ currentSession: Object })
const emit = defineEmits(['select-session', 'show-user'])
const store = useStore()

const loading = ref(false)
const searchKeyword = ref('')
const showGlobalSearch = ref(false)
const activeFilter = ref('all')

const subMenuTabs = ref([
  { key: 'all', label: '全部', count: 0 },
  { key: 'unread', label: '未读', count: 0 },
  { key: 'mentions', label: '@我', count: 0 }
])

const sessions = computed(() => store.state.im.session.sessions)
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
  contextMenu.show = true
  contextMenu.x = e.clientX
  contextMenu.y = e.clientY
  contextMenu.session = session
  const hide = () => { contextMenu.show = false; document.removeEventListener('click', hide) }
  setTimeout(() => document.addEventListener('click', hide), 10)
}

const handleFilterChange = (key) => { 
  activeFilter.value = key
  store.dispatch('im/session/loadSessions', key) 
}

const handleCreateGroup = () => {
  ElMessage.info('功能开发中，请从通讯录发起')
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
      store.dispatch('im/session/deleteSession', contextMenu.session.id)
      break
  }
}

onMounted(() => {
  if (store.state.user?.token) {
    store.dispatch('im/session/loadSessions')
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
      color: #ffffff;
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
    &::after {
      content: "";
      position: absolute;
      top: 0;
      left: 0;
      border-top: 6px solid var(--dt-brand-color);
      border-right: 6px solid transparent;
    }
  }

  .item-avatar-wrapper {
    position: relative;
    flex-shrink: 0;

    .unread-count-badge {
      position: absolute;
      top: -4px;
      right: -4px;
      background: var(--dt-error-color);
      color: #ffffff;
      font-size: 10px;
      min-width: 16px;
      height: 16px;
      padding: 0 2px;
      border-radius: var(--dt-radius-full);
      @include flex-center;
      border: 1.5px solid #ffffff;
      box-shadow: var(--dt-shadow-1);
      font-weight: 600;
    }
  }

  .item-content {
    flex: 1;
    margin-left: var(--dt-spacing-sm);
    min-width: 0; // 防崩核心
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 4px;

    .content-top {
      display: flex;
      justify-content: space-between;
      align-items: baseline;

      .session-name {
        font-size: var(--dt-font-size-base);
        font-weight: var(--dt-font-weight-medium);
        color: var(--dt-text-primary);
        margin: 0;
        @include text-ellipsis;
      }

      .session-time {
        font-size: var(--dt-font-size-xs);
        color: var(--dt-text-tertiary);
        flex-shrink: 0;
      }
    }

    .content-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .preview-wrapper {
        display: flex;
        align-items: center;
        gap: 4px;
        min-width: 0;
        flex: 1;
        font-size: var(--dt-font-size-sm);
        color: var(--dt-text-tertiary);
        @include text-ellipsis;

        .mention-text { color: var(--dt-error-color); font-weight: 500; }
        .sender-prefix { color: var(--dt-text-secondary); }
      }

      .mute-icon {
        font-size: 14px;
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
  }

  .menu-divider {
    height: 1px;
    background: var(--dt-border-lighter);
    margin: 4px 0;
  }
}
</style>
