<template>
  <div class="chat-header">
    <div class="header-left" @click="handleShowDetail" role="button" tabindex="0">
      <div class="header-avatar" :class="getAvatarBgClass(session)">
        {{ (session?.name?.charAt(0) || '?').toUpperCase() }}
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
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import UserDetailDrawer from './UserDetailDrawer.vue'

const props = defineProps({
  session: Object
})

const emit = defineEmits(['show-detail', 'voice-call', 'video-call', 'search', 'pin', 'mute', 'clear'])

// 用户详情抽屉显示状态
const showUserDetail = ref(false)

// 模拟在线状态（实际应从后端获取）
const isOnline = computed(() => {
  if (props.session?.type === 'GROUP') return false
  return Math.random() > 0.3  // 70%概率在线
})

const getAvatarBgClass = (session) => {
  if (!session) return 'bg-primary'
  if (session.type === 'GROUP') return 'bg-primary'
  const colors = ['bg-blue-500', 'bg-orange-500', 'bg-emerald-500', 'bg-purple-500']
  return colors[(session.id || 0) % colors.length]
}

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
  height: 64px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #1677ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  position: relative;  /* 为在线指示器定位 */
  cursor: pointer;  /* 可点击 */
  transition: transform 0.2s;
}

.header-avatar:hover {
  transform: scale(1.05);
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
}

.bg-primary { background-color: #1677ff; }
.bg-blue-500 { background-color: #3b82f6; }
.bg-orange-500 { background-color: #f97316; }
.bg-emerald-500 { background-color: #10b981; }
.bg-purple-500 { background-color: #a855f7; }

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
