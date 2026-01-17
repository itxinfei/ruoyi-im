<template>
  <div class="session-panel">
    <div class="panel-header">
      <h3>消息</h3>
      <el-tooltip content="新建会话">
        <el-button :icon="Plus" text circle size="small" />
      </el-tooltip>
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

    <div class="session-list">
      <div
        v-for="session in filteredSessions"
        :key="session.id"
        class="session-item"
        :class="{ active: isActiveSession(session) }"
        @click="handleSessionClick(session)"
      >
        <el-badge :value="session.unreadCount" :hidden="session.unreadCount === 0" :max="99">
          <el-avatar :size="40" :src="session.avatar">
            {{ session.name?.charAt(0) }}
          </el-avatar>
        </el-badge>
        
        <div class="session-info">
          <div class="session-top">
            <span class="session-name">{{ session.name }}</span>
            <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
          </div>
          <div class="session-preview">
            {{ session.lastMessage }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'

const props = defineProps({
  currentSession: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['select-session'])

const searchKeyword = ref('')

const sessions = ref([
  {
    id: 1,
    name: '张三',
    avatar: 'https://via.placeholder.com/40',
    lastMessage: '你好，在吗？',
    lastMessageTime: Date.now() - 3600000,
    unreadCount: 2
  },
  {
    id: 2,
    name: '李四',
    avatar: 'https://via.placeholder.com/40',
    lastMessage: '明天开会',
    lastMessageTime: Date.now() - 7200000,
    unreadCount: 0
  },
  {
    id: 3,
    name: '项目群',
    avatar: 'https://via.placeholder.com/40',
    lastMessage: '王五: 收到',
    lastMessageTime: Date.now() - 86400000,
    unreadCount: 5,
    isGroup: true
  }
])

const filteredSessions = computed(() => {
  if (!searchKeyword.value) return sessions.value
  
  return sessions.value.filter(session => 
    session.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
    session.lastMessage.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

const isActiveSession = (session) => {
  return props.currentSession?.id === session.id
}

const handleSessionClick = (session) => {
  emit('select-session', session)
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return `${date.getMonth() + 1}/${date.getDate()}`
}
</script>

<style scoped lang="scss">
.session-panel {
  display: flex;
  flex-direction: column;
  width: 280px;
  border-right: 1px solid #e8e8e8;
  background: #fff;
  height: 100%;
  
  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #262626;
    }
  }

  .search-bar {
    padding: 12px 16px;
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
      transition: all 0.2s ease;
      
      &:hover {
        background: rgba(0, 0, 0, 0.04);
      }
      
      &.active {
        background: rgba(0, 137, 255, 0.1);
      }
      
      .session-info {
        flex: 1;
        min-width: 0;
        margin-left: 12px;
        
        .session-top {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 4px;
          
          .session-name {
            font-size: 14px;
            font-weight: 500;
            color: #262626;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          
          .session-time {
            font-size: 12px;
            color: #8c8c8c;
            flex-shrink: 0;
          }
        }
        
        .session-preview {
          font-size: 12px;
          color: #8c8c8c;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }
  }
}
</style>
