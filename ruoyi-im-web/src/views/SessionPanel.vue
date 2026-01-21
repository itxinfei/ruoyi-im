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
import { ElMessage } from 'element-plus'
import { Search, Plus, UserFilled, ChatDotRound } from '@element-plus/icons-vue'
import { getConversations } from '@/api/im/conversation'
import { useImWebSocket } from '@/composables/useImWebSocket'
import CreateGroupDialog from '@/components/CreateGroupDialog/index.vue'

const props = defineProps({
  currentSession: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['select-session'])

const searchKeyword = ref('')
const sessions = ref([])
const loading = ref(false)
const showCreateGroupDialog = ref(false)

// WebSocket 连接
const { isConnected, connect, onMessage } = useImWebSocket()

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'group') {
    showCreateGroupDialog.value = true
  } else if (command === 'chat') {
    ElMessage.info('发起单聊功能开发中...')
  }
}

// 群组创建成功
const handleGroupCreated = (groupData) => {
  ElMessage.success('群组创建成功')
  loadSessions() // 重新加载会话列表
}

// 加载会话列表
const loadSessions = async () => {
  loading.value = true
  try {
    const response = await getConversations()
    if (response && response.data) {
      sessions.value = response.data.map(item => ({
        id: item.conversationId || item.id,
        name: item.conversationName || item.name || '未命名',
        avatar: item.avatar || '',
        lastMessage: item.lastMessage || '',
        lastMessageTime: item.lastMessageTime || item.updateTime,
        unreadCount: item.unreadCount || 0,
        isGroup: item.type === 'GROUP'
      }))
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载会话列表失败')
  } finally {
    loading.value = false
  }
}

// 过滤会话列表
const filteredSessions = computed(() => {
  if (!searchKeyword.value) return sessions.value
  
  return sessions.value.filter(session => 
    session.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
    (session.lastMessage && session.lastMessage.toLowerCase().includes(searchKeyword.value.toLowerCase()))
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

// 处理新建会话
const handleNewConversation = () => {
  console.log('新建会话')
  // TODO: 打开新建会话对话框
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

// 更新会话最后一条消息
const updateSessionMessage = (message) => {
  const session = sessions.value.find(s => s.id === message.conversationId)
  if (session) {
    session.lastMessage = message.content
    session.lastMessageTime = message.timestamp || Date.now()
    
    // 如果不是当前会话，增加未读数
    if (props.currentSession?.id !== session.id) {
      session.unreadCount = (session.unreadCount || 0) + 1
    }
    
    // 将会话移到列表顶部
    const index = sessions.value.indexOf(session)
    if (index > 0) {
      sessions.value.splice(index, 1)
      sessions.value.unshift(session)
    }
  }
}

// 监听 WebSocket 消息
onMessage((message) => {
  console.log('SessionPanel 收到消息:', message)
  updateSessionMessage(message)
})

// 组件挂载时加载数据
onMounted(async () => {
  await loadSessions()
  
  // 连接 WebSocket
  if (!isConnected.value) {
    const token = localStorage.getItem('access_token')
    if (token) {
      connect(token)
    }
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
