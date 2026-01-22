<template>
  <div class="session-panel">
    <div class="panel-header">
      <h3>消息</h3>
      <el-dropdown trigger="click" @command="handleCommand">
        <el-tooltip content="新建会话">
          <el-button :icon="Plus" text circle size="small" />
        </el-tooltip>
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
            {{ session.lastMessage || '暂无消息' }}
          </div>
        </div>
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
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Search, Plus, UserFilled, ChatDotRound } from '@element-plus/icons-vue'
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
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return `${date.getMonth() + 1}/${date.getDate()}`
}

onMounted(() => {
  // 确保会话加载 (App.vue already does this, but safe to check or refresh)
  if (sessions.value.length === 0) {
    store.dispatch('im/loadSessions')
  }
})
</script>

<style scoped lang="scss">
.session-panel {
  display: flex;
  flex-direction: column;
  width: 320px;
  min-width: 320px;
  flex-shrink: 0;
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
