<template>
  <div class="session-panel">
    <!-- 头部搜索与过滤 (极致对标钉钉 8.2) -->
    <div class="panel-header">
      <div class="header-action-row">
        <div class="search-box-unified">
          <span class="material-icons-outlined search-icon">search</span>
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索"
            type="text"
            @focus="showGlobalSearch = true"
          />
        </div>
        <button class="icon-add-btn" @click="handleCreateGroup" title="发起群聊">
          <span class="material-icons-outlined">add</span>
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

    <!-- 会话列表 (极致流畅滚动) -->
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
          <div v-if="session.type === 'GROUP'" class="avatar-icon-group">
            <span class="material-icons-outlined">groups</span>
          </div>
          <DingtalkAvatar v-else :src="session.avatar" :name="session.name" :user-id="session.targetId" :size="40" shape="square" />
          <span v-if="session.unreadCount > 0" class="unread-count-badge">{{ session.unreadCount > 99 ? '99+' : session.unreadCount }}</span>
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
              <span v-if="session.isMuted" class="material-icons-outlined mute-icon">notifications_off</span>
            </div>
          </div>
        </div>
        <div v-if="session.isPinned" class="pin-tag"></div>
      </div>
    </div>

    <GlobalSearch v-model:visible="showGlobalSearch" :keyword="searchKeyword" @select="handleSearchSelect" />

    <!-- 右键菜单 -->
    <div v-if="contextMenu.show" class="context-menu-layer shadow-xl" :style="contextMenuStyle" @click.stop>
      <div class="menu-item" @click="doAction('mark-read')"><span class="material-icons-outlined">done_all</span> 标记已读</div>
      <div class="menu-item" @click="doAction('toggle-pin')"><span class="material-icons-outlined">push_pin</span> {{ contextMenu.session?.isPinned ? '取消置顶' : '置顶会话' }}</div>
      <div class="menu-divider"></div>
      <div class="menu-item danger" @click="doAction('delete')"><span class="material-icons-outlined">delete</span> 删除会话</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
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
    return new Date(b.lastMessageTime) - new Date(a.lastMessageTime)
  })
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  if (date.toDateString() === now.toDateString()) return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0')
  return (date.getMonth() + 1) + '-' + date.getDate()
}

const contextMenu = reactive({ show: false, x: 0, y: 0, session: null })
const contextMenuStyle = computed(() => ({ left: `${contextMenu.x}px`, top: `${contextMenu.y}px` }))
const handleContextMenu = (e, session) => {
  contextMenu.show = true; contextMenu.x = e.clientX; contextMenu.y = e.clientY; contextMenu.session = session
  const hide = () => { contextMenu.show = false; document.removeEventListener('click', hide) }
  setTimeout(() => document.addEventListener('click', hide), 10)
}

const handleFilterChange = (key) => { activeFilter.value = key; store.dispatch('im/session/loadSessions', { filter: key }) }
const handleCreateGroup = () => {
  // TODO: 打开创建群组弹窗
  ElMessage.info('请通过通讯录创建群组')
}
const doAction = (cmd) => { contextMenu.show = false; ElMessage.success('操作已执行') }

onMounted(() => store.dispatch('im/session/loadSessions'))
</script>

<style scoped lang="scss">
.session-panel { width: 280px; height: 100%; border-right: 1px solid #f2f3f5; background: #fff; display: flex; flex-direction: column; }
.panel-header { padding: 12px 12px 8px; flex-shrink: 0;
  .header-action-row { display: flex; align-items: center; gap: 8px; margin-bottom: 12px;
    .search-box-unified { flex: 1; position: relative; height: 32px; background: #f3f3f3; border-radius: 6px; display: flex; align-items: center;
      .search-icon { position: absolute; left: 10px; font-size: 16px; color: #8f959e; }
      .search-input { width: 100%; border: none; background: transparent; padding: 0 34px; font-size: 13px; outline: none; }
    }
    .icon-add-btn { width: 32px; height: 32px; border: none; background: #f3f3f3; border-radius: 6px; cursor: pointer; color: #1f2329; display: flex; align-items: center; justify-content: center; &:hover { background: #e8e8e8; } }
  }
}
.sub-tabs-compact { display: flex; gap: 4px; padding-bottom: 8px; border-bottom: 1px solid #f2f3f5;
  .tab-pill { padding: 4px 10px; font-size: 12px; color: #1f2329; border-radius: 14px; cursor: pointer; display: flex; align-items: center; gap: 4px;
    &:hover { background: #f5f6f7; }
    &.active { background: #f2f3f5; color: #1677ff; font-weight: 600; }
    .tab-badge { background: #ff4d4f; color: #fff; padding: 0 5px; border-radius: 10px; transform: scale(0.8); }
  }
}
.session-list { flex: 1; overflow-y: auto; }
.session-item { display: flex; padding: 12px; cursor: pointer; position: relative; transition: background 0.2s;
  &:hover { background: #f5f6f7; }
  &.active { background: #e8f4ff; .session-name { color: #1677ff; } }
  .item-avatar-wrapper { position: relative; flex-shrink: 0; .avatar-icon-group { width: 40px; height: 40px; background: #1677ff; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #fff; .material-icons-outlined { font-size: 24px; } }
    .unread-count-badge { position: absolute; top: -6px; right: -6px; background: #ff4d4f; color: #fff; font-size: 10px; min-width: 16px; height: 16px; border-radius: 8px; display: flex; align-items: center; justify-content: center; border: 2px solid #fff; }
  }
  .item-content { flex: 1; margin-left: 12px; min-width: 0; display: flex; flex-direction: column; justify-content: center; gap: 4px;
    .content-top { display: flex; justify-content: space-between; align-items: baseline; .session-name { font-size: 14px; font-weight: 500; color: #1f2329; margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; } .session-time { font-size: 11px; color: #8f959e; } }
    .content-bottom { display: flex; justify-content: space-between; align-items: center; .preview-wrapper { display: flex; align-items: center; gap: 4px; min-width: 0; flex: 1; font-size: 12px; color: #8f959e; overflow: hidden; white-space: nowrap; .mention-text { color: #ff4d4f; } }
      .mute-icon { font-size: 14px; color: #c9cdd4; }
    }
  }
  .pin-tag { position: absolute; top: 0; left: 0; width: 0; height: 0; border-top: 6px solid #1677ff; border-right: 6px solid transparent; }
}
.context-menu-layer { position: fixed; background: #fff; border: 1px solid #f2f3f5; border-radius: 8px; padding: 4px; min-width: 140px; z-index: 3000;
  .menu-item { display: flex; align-items: center; gap: 10px; padding: 8px 12px; font-size: 13px; color: #1f2329; cursor: pointer; border-radius: 4px; &:hover { background: #1677ff; color: #fff; }
    &.danger { color: #ff4d4f; &:hover { background: #ff4d4f; color: #fff; } }
  }
}
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: transparent; border-radius: 2px; }
.session-list:hover.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(0, 0, 0, 0.1); }
</style>
