<template>
  <div class="chat-header">
    <div class="header-left" @click="handleShowDetail" role="button" tabindex="0">
      <div class="header-avatar-wrapper">
        <!-- 群组使用图标，单聊使用钉钉风格头像 -->
        <div v-if="session?.type === 'GROUP'" class="header-avatar group-avatar">
          <span class="material-icons-outlined">groups</span>
        </div>
        <DingtalkAvatar
          v-else
          :src="session?.avatar"
          :name="session?.name"
          :user-id="session?.targetId"
          :size="40"
          shape="square"
          custom-class="header-avatar"
        />
        <span v-if="session?.type !== 'GROUP' && isOnline" class="online-indicator"></span>
      </div>
      <div class="header-info">
        <h2 class="header-name">{{ session?.name }}</h2>
        <span v-if="session?.type === 'GROUP'" class="member-count">
          {{ session?.memberCount || 0 }}人
        </span>
        <span v-else class="status-text">
          {{ isOnline ? '在线' : '离线' }}
        </span>
      </div>
    </div>
    <div class="header-actions">
      <button class="action-btn" @click="handleVoiceCall" title="语音通话">
        <span class="material-icons-outlined">phone</span>
      </button>
      <button class="action-btn" @click="handleVideoCall" title="视频通话">
        <span class="material-icons-outlined">videocam</span>
      </button>
      <el-dropdown trigger="click" @command="handleMenuCommand">
        <button class="action-btn" title="更多">
          <span class="material-icons-outlined">more_horiz</span>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="search">
              <span class="material-icons-outlined">search</span>
              搜索聊天记录
            </el-dropdown-item>
            <el-dropdown-item command="files">
              <span class="material-icons-outlined">folder</span>
              查看文件
            </el-dropdown-item>
            <el-dropdown-item command="pin" :divided="true">
              {{ session?.isPinned ? '取消置顶' : '置顶会话' }}
            </el-dropdown-item>
            <el-dropdown-item command="mute">
              {{ session?.isMuted ? '取消免打扰' : '消息免打扰' }}
            </el-dropdown-item>
            <el-dropdown-item command="clear" :divided="true">
              清空聊天记录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <button class="action-btn" @click="$emit('toggle-sidebar')" title="详情">
        <span class="material-icons-outlined">info</span>
      </button>
    </div>
    
    <!-- 用户详情抽屉 -->
    <UserDetailDrawer
      v-model="showUserDetail"
      :session="session"
      @send-message="handleSendMessage"
      @voice-call="handleVoiceCall"
      @video-call="handleVideoCall"
    />
  </div>
</template>

<script setup>
import { computed, ref, inject } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import UserDetailDrawer from './UserDetailDrawer.vue'

const props = defineProps({
  session: {
    type: Object,
    default: null,
    validator: (value) => {
      // 如果有值，必须包含必要的字段
      if (value === null) return true
      return (
        typeof value.id === 'string' ||
        typeof value.id === 'number'
      )
    }
  }
})

const emit = defineEmits(['show-detail', 'voice-call', 'video-call', 'search', 'pin', 'mute', 'clear', 'toggle-sidebar'])

// 用户详情抽屉显示状态
const showUserDetail = ref(false)

// 获取在线状态
const store = useStore()
const isOnline = computed(() => {
  // 群组不显示在线状态
  if (props.session?.type === 'GROUP') return false
  
  // 优先使用store中的实时状态（WebSocket推送）
  const userId = props.session?.targetId
  if (userId && store.state.im.contact.userStatus[userId]) {
    return store.state.im.contact.userStatus[userId] === 'online'
  }
  
  // 回退到session中的peerOnline字段（后端初始值）
  return props.session?.peerOnline ?? false
})

// 显示详情
const handleShowDetail = () => {
  showUserDetail.value = true
  emit('show-detail', props.session)
}

// 发送消息(从抽屉触发)
const handleSendMessage = () => {
  // 抽屉会自动关闭,这里不需要额外处理
  ElMessage.success('已切换到聊天')
}

// 语音通话
const handleVoiceCall = () => {
  ElMessage.info('语音通话功能开发中')
  emit('voice-call', props.session)
}

// 视频通话
const handleVideoCall = () => {
  ElMessage.info('视频通话功能开发中')
  emit('video-call', props.session)
}

// 菜单命令处理
const handleMenuCommand = (command) => {
  switch (command) {
    case 'search':
      emit('search', props.session)
      break
    case 'files':
      ElMessage.info('查看文件功能开发中')
      break
    case 'pin':
      emit('pin', props.session)
      break
    case 'mute':
      emit('mute', props.session)
      break
    case 'clear':
      emit('clear', props.session)
      break
  }
}
</script>

<style scoped>
.chat-header {
  height: 60px;
  border-bottom: 1px solid #f0f1f2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
  flex-shrink: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar-wrapper {
  position: relative;
  cursor: pointer;
  transition: transform 0.2s;
}

.header-avatar-wrapper:hover {
  transform: scale(1.05);
}

.header-avatar {
  border-radius: 8px;
}

.group-avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #1677ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 在线状态指示器 */
.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: #52c41a;
  border: 2px solid #fff;
  border-radius: 50%;
  z-index: 1;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-name {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

.member-count,
.status-text {
  font-size: 11px;
  color: #8c8c8c;
  margin-top: 2px;
}

.status-text {
  color: #52c41a;  /* 在线状态绿色 */
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;  /* 可点击查看详情 */
  padding: 4px;
  margin: -4px;
  border-radius: 8px;
  transition: background 0.2s;
}

.header-left:hover {
  background: rgba(0, 0, 0, 0.03);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  background: transparent;
  border: none;
  padding: 4px;
  color: #8c8c8c;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: color 0.2s;
}

.action-btn:hover {
  color: #1677ff;
}

.action-btn .material-icons-outlined {
  font-size: 20px;
}

/* 暗色模式 */
:deep(.dark) .chat-header {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .header-name {
  color: #f1f5f9;
}

:deep(.dark) .member-count {
  color: #64748b;
}

:deep(.dark) .action-btn {
  color: #64748b;
}

:deep(.dark) .action-btn:hover {
  color: #60a5fa;
}
</style>
