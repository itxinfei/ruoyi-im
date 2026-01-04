<template>
  <transition-group name="notification" tag="div" class="notification-container">
    <div
      v-for="notification in notifications"
      :key="notification.id"
      class="notification-toast"
      :class="notification.type"
      @click="handleNotificationClick(notification)"
    >
      <div class="notification-avatar">
        <el-avatar :size="40" :src="notification.avatar">
          {{ notification.title?.charAt(0) }}
        </el-avatar>
      </div>
      <div class="notification-content">
        <div class="notification-header">
          <span class="notification-title">{{ notification.title }}</span>
          <span class="notification-time">{{ formatTime(notification.time) }}</span>
        </div>
        <div class="notification-body">{{ notification.content }}</div>
      </div>
      <el-icon class="notification-close" @click.stop="removeNotification(notification.id)">
        <Close />
      </el-icon>
    </div>
  </transition-group>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { Close } from '@element-plus/icons-vue'

const router = useRouter()
const store = useStore()

const notifications = ref([])
let notificationId = 0

// 添加通知
const addNotification = (notification) => {
  const id = ++notificationId
  notifications.value.push({
    id,
    ...notification,
    time: new Date(),
  })

  // 5秒后自动移除
  setTimeout(() => {
    removeNotification(id)
  }, 5000)

  // 播放提示音
  playNotificationSound()
}

// 移除通知
const removeNotification = (id) => {
  const index = notifications.value.findIndex(n => n.id === id)
  if (index > -1) {
    notifications.value.splice(index, 1)
  }
}

// 点击通知
const handleNotificationClick = (notification) => {
  if (notification.sessionId) {
    router.push('/im/chat')
    store.dispatch('im/switchSession', { id: notification.sessionId })
  }
  removeNotification(notification.id)
}

// 格式化时间
const formatTime = (time) => {
  const now = new Date()
  const diff = now - time
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  return time.toLocaleTimeString()
}

// 播放提示音
const playNotificationSound = () => {
  try {
    const audio = new Audio('/notification.mp3')
    audio.volume = 0.5
    audio.play().catch(() => {})
  } catch (e) {
    // 忽略音频播放错误
  }
}

// 监听新消息事件
const handleNewMessage = (event) => {
  const message = event.detail
  if (message && document.hidden) {
    addNotification({
      type: 'message',
      title: message.senderName || '新消息',
      content: getMessagePreview(message),
      avatar: message.senderAvatar,
      sessionId: message.sessionId,
    })
  }
}

// 获取消息预览
const getMessagePreview = (message) => {
  switch (message.type) {
    case 'text': return message.content
    case 'image': return '[图片]'
    case 'file': return '[文件]'
    case 'voice': return '[语音]'
    case 'video': return '[视频]'
    case 'location': return '[位置]'
    default: return '[消息]'
  }
}

onMounted(() => {
  window.addEventListener('im-new-message', handleNewMessage)
})

onUnmounted(() => {
  window.removeEventListener('im-new-message', handleNewMessage)
})

// 暴露方法供外部调用
defineExpose({
  addNotification,
})
</script>

<style lang="scss" scoped>
.notification-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  pointer-events: none;
}

.notification-toast {
  width: 320px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: flex-start;
  gap: 12px;
  cursor: pointer;
  pointer-events: auto;
  transition: all 0.3s ease;

  &:hover {
    transform: translateX(-4px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);

    .notification-close {
      opacity: 1;
    }
  }

  &.message {
    border-left: 4px solid #409eff;
  }

  &.system {
    border-left: 4px solid #67c23a;
  }

  &.warning {
    border-left: 4px solid #e6a23c;
  }
}

.notification-avatar {
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;

  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4px;

    .notification-title {
      font-weight: 500;
      color: #303133;
      font-size: 14px;
    }

    .notification-time {
      font-size: 12px;
      color: #909399;
    }
  }

  .notification-body {
    font-size: 13px;
    color: #606266;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.notification-close {
  flex-shrink: 0;
  color: #909399;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;

  &:hover {
    color: #606266;
  }
}

// 动画
.notification-enter-active {
  animation: slideIn 0.3s ease;
}

.notification-leave-active {
  animation: slideOut 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(100%);
  }
}
</style>
