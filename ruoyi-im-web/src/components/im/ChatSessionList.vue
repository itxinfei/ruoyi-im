<template>
  <div class="session-panel-v4" @click="handleGlobalClick">
    <!-- 1. 顶部搜索与类型 Tabs (Premium 紧凑版) -->
    <header class="panel-header-v4">
      <div class="search-box-v4" :class="{ 'is-focused': isSearchFocused }">
        <el-icon class="search-icon"><Search /></el-icon>
        <input 
          v-model="searchKeyword" 
          placeholder="搜索会话..." 
          @focus="isSearchFocused = true" 
          @blur="isSearchFocused = false" 
        />
        <el-icon class="add-icon" @click="handleOpenCreateChat"><Plus /></el-icon>
      </div>
      
      <div class="filter-tabs">
        <div 
          v-for="tab in tabs" :key="tab.value" 
          class="tab-item" :class="{ active: filterType === tab.value }"
          @click="filterType = tab.value"
        >
          {{ tab.label }}
        </div>
      </div>
    </header>

    <!-- 2. 会话列表区 -->
    <div class="list-container custom-scrollbar" v-loading="loading">
      <template v-for="group in groupedSessions" :key="group.type">
        <!-- 分组头 -->
        <div v-if="filterType === 'all' && group.sessions.length > 0" class="group-header" @click="toggleGroup(group.type)">
          <el-icon class="arrow" :class="{ collapsed: collapsedGroups[group.type] }"><ArrowDown /></el-icon>
          <span class="label">{{ group.label }}</span>
        </div>

        <div v-show="!collapsedGroups[group.type]" class="group-body">
          <div
            v-for="session in group.sessions"
            :key="session.id"
            class="session-item-v4"
            :class="{ active: currentSession?.id === session.id, pinned: session.isPinned }"
            @click="selectSession(session)"
            @contextmenu.prevent="handleContextMenu($event, session)"
          >
            <!-- 头像与角标 -->
            <div class="avatar-area">
              <img :src="session.avatar || '/avatars/default.png'" class="avatar-img">
              <div v-if="session.unreadCount > 0" class="unread-badge" :class="{ dot: session.isMuted }">
                {{ session.isMuted ? '' : (session.unreadCount > 99 ? '99+' : session.unreadCount) }}
              </div>
            </div>
            
            <!-- 信息区 -->
            <div class="info-area">
              <div class="row-top">
                <span class="name">{{ session.name }}</span>
                
                <!-- 核心：时间与操作的动态切换 (对齐钉钉 8.2) -->
                <div class="meta-side">
                  <span class="time">{{ formatTime(session.lastMessageTime) }}</span>
                  <div class="quick-actions">
                    <button class="action-btn" :title="session.isPinned ? '取消置顶' : '置顶'" @click.stop="togglePin(session)">
                      <el-icon><Top v-if="!session.isPinned" /><Bottom v-else /></el-icon>
                    </button>
                    <button class="action-btn" :title="session.isMuted ? '开启通知' : '免打扰'" @click.stop="toggleMute(session)">
                      <el-icon><BellFilled v-if="!session.isMuted" /><Mute v-else /></el-icon>
                    </button>
                    <button class="action-btn danger" title="删除" @click.stop="handleDelete(session)">
                      <el-icon><Delete /></el-icon>
                    </button>
                  </div>
                </div>
              </div>
              <div class="row-bottom">
                <span v-if="session.draftContent" class="draft-tag">[草稿]</span>
                <span class="msg-preview">{{ session.draftContent || session.lastMessage }}</span>
                <el-icon v-if="session.isMuted && !session.draftContent" class="mute-icon"><BellFilled /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="js">
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { Search, Plus, ArrowDown, BellFilled, Top, Bottom, Delete, Mute } from '@element-plus/icons-vue'

const store = useStore()
const isSearchFocused = ref(false)
const searchKeyword = ref('')
const filterType = ref(localStorage.getItem('dt_session_filter') || 'all')
const collapsedGroups = ref({ PINNED: false, PRIVATE: false, GROUP: false })

const tabs = [{ label: '全部', value: 'all' }, { label: '单聊', value: 'PRIVATE' }, { label: '群聊', value: 'GROUP' }]

const currentSession = computed(() => store.state.im.session.currentSession)
const sessions = computed(() => store.state.im.session.sessions)

const filteredSessions = computed(() => {
  let list = sessions.value
  if (filterType.value !== 'all') list = list.filter(s => s.type === filterType.value)
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(s => s.name?.toLowerCase().includes(kw))
  }
  return list
})

const groupedSessions = computed(() => {
  const list = filteredSessions.value
  if (filterType.value !== 'all') return [{ type: 'FLAT', sessions: list }]
  return [
    { type: 'PINNED', label: '置顶会话', sessions: list.filter(s => s.isPinned) },
    { type: 'PRIVATE', label: '单聊', sessions: list.filter(s => !s.isPinned && s.type === 'PRIVATE') },
    { type: 'GROUP', label: '群聊', sessions: list.filter(s => !s.isPinned && s.type === 'GROUP') }
  ]
})

const selectSession = (s) => store.dispatch('im/session/selectSession', s)
const toggleGroup = (t) => collapsedGroups.value[t] = !collapsedGroups.value[t]
const togglePin = (s) => store.dispatch('im/session/pinSession', { sessionId: s.id, pinned: !s.isPinned })
const toggleMute = (s) => store.dispatch('im/session/muteSession', { sessionId: s.id, muted: !s.isMuted })
const handleDelete = (s) => store.dispatch('im/session/deleteSession', s.id)
const formatTime = (t) => t ? new Date(t).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''
const handleGlobalClick = () => { /* 预留全局点击处理 */ }
const handleOpenCreateChat = () => { /* 预留创建会话 */ }
</script>

<style scoped lang="scss">
.session-panel-v4 {
  width: 280px; height: 100%; background: var(--dt-bg-card); border-right: 1px solid var(--dt-border-light);
  display: flex; flex-direction: column;
}

.panel-header-v4 {
  padding: var(--dt-spacing-md) var(--dt-spacing-lg) var(--dt-spacing-sm);
  .search-box-v4 {
    height: 32px; background: var(--dt-bg-input); border-radius: var(--dt-radius-sm); display: flex; align-items: center; padding: 0 var(--dt-spacing-sm); gap: var(--dt-spacing-sm);
    transition: all 0.2s; border: 1px solid transparent;
    &.is-focused { background: var(--dt-bg-card); border-color: var(--dt-brand-color); box-shadow: 0 0 0 2px var(--dt-brand-lighter); }
    input { flex: 1; border: none; background: transparent; outline: none; font-size: var(--dt-font-size-base); }
    .search-icon { color: var(--dt-text-tertiary); font-size: 16px; }
    .add-icon { cursor: pointer; color: var(--dt-text-secondary); font-size: 16px; &:hover { color: var(--dt-brand-color); } }
  }
}

.filter-tabs {
  display: flex; gap: var(--dt-spacing-lg); margin-top: 14px; padding: 0 var(--dt-spacing-xs);
  .tab-item {
    font-size: var(--dt-font-size-sm); color: var(--dt-text-secondary); cursor: pointer; position: relative; padding-bottom: 6px;
    &.active { color: var(--dt-text-primary); font-weight: 600; &::after { content: ''; position: absolute; bottom: 0; left: 0; right: 0; height: 2px; background: var(--dt-brand-color); } }
  }
}

.list-container { flex: 1; overflow-y: auto; }

.group-header {
  height: 32px; display: flex; align-items: center; padding: 0 var(--dt-spacing-md); gap: var(--dt-spacing-sm); cursor: pointer;
  background: var(--dt-bg-body); color: var(--dt-text-tertiary);
  .arrow { font-size: 10px; transition: var(--dt-transition-fast); &.collapsed { transform: rotate(-90deg); } }
  .label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; }
}

.session-item-v4 {
  height: 56px; display: flex; align-items: center; padding: 0 var(--dt-spacing-md); gap: var(--dt-spacing-md); cursor: pointer;
  transition: background-color 0.1s; position: relative;
  &:hover { background-color: var(--dt-bg-session-hover); .time { display: none; } .quick-actions { opacity: 1; } }
  &.active { background-color: var(--dt-bg-session-active) !important; }
  &.pinned { background-color: rgba(23, 26, 29, 0.02); }

  .avatar-area {
    position: relative; width: 40px; height: 40px;
    .avatar-img { width: 100%; height: 100%; border-radius: var(--dt-radius-sm); object-fit: cover; }
    .unread-badge {
      position: absolute; top: -5px; right: -5px; height: 16px; min-width: 16px; padding: 0 4px;
      background: var(--dt-error-color); color: var(--dt-text-white); font-size: 10px; font-weight: 700; border-radius: 9px;
      @include flex-center; border: 1.5px solid var(--dt-bg-card);
      &.dot { width: 8px; height: 8px; min-width: 8px; top: -1px; right: -1px; padding: 0; border-radius: 50%; }
    }
  }

  .info-area {
    flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px;
    .row-top { display: flex; justify-content: space-between; align-items: center;
      .name { font-size: var(--dt-font-size-base); font-weight: 500; color: var(--dt-text-primary); @include text-ellipsis; }

      .meta-side {
        position: relative; height: 20px; display: flex; align-items: center;
        .time { font-size: 11px; color: var(--dt-text-tertiary); }

        /* 快捷操作组 */
        .quick-actions {
          position: absolute; right: 0; display: flex; gap: 2px; opacity: 0;
          transition: opacity 0.2s;
          .action-btn {
            width: 22px; height: 22px; @include flex-center; border: none; background: transparent;
            border-radius: var(--dt-radius-sm); color: var(--dt-text-tertiary); cursor: pointer; font-size: 14px;
            &:hover { background: var(--dt-bg-hover); color: var(--dt-brand-color); }
            &.danger:hover { color: var(--dt-error-color); }
          }
        }
      }
    }
    .row-bottom { display: flex; align-items: center; gap: 4px;
      .draft-tag { font-size: var(--dt-font-size-sm); color: var(--dt-error-color); flex-shrink: 0; }
      .msg-preview { font-size: var(--dt-font-size-sm); color: var(--dt-text-tertiary); @include text-ellipsis; flex: 1; }
      .mute-icon { font-size: 12px; color: var(--dt-border-color); }
    }
  }
}
</style>
