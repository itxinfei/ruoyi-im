<template>
  <div 
    class="session-panel" 
    :style="{ width: width + 'px' }"
    :class="{ collapsed: collapsed }"
  >
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
          <DtAvatar
            :name="session.name"
            :avatar="session.avatar"
            :size="40"
          />
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
import { useStore } from 'vuex'
import SmartAvatar from '@/components/SmartAvatar/index.vue'

const props = defineProps({
  width: {
    type: Number,
    default: 320
  },
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:width', 'select-session'])

const store = useStore()
const searchKeyword = ref('')

// 会话列表
const sessions = computed(() => store.state.im.sessions)

// 当前会话
const currentSession = computed(() => store.state.im.currentSession)

// 过滤后的会话
const filteredSessions = computed(() => {
  if (!searchKeyword.value) return sessions.value
  
  return sessions.value.filter(session => 
    session.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
    session.lastMessage.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 判断是否为当前激活会话
const isActiveSession = (session) => {
  return currentSession.value?.id === session.id
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
  border-right: 1px solid #e8e8e8;
  background: #fff;
  transition: all 0.3s ease;
  
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
        background: rgba(22, 119, 255, 0.1);
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
  
  &.collapsed {
    display: none;
  }
}
</style>
