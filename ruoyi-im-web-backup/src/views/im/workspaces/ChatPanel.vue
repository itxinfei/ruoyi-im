<template>
  <div class="chat-panel" :class="{ collapsed: collapsed }">
    <!-- 聊天头部 -->
    <ChatHeader 
      v-if="currentSession"
      :session="currentSession"
      @search-in-chat="searchInChat"
      @voice-call="startVoiceCall"
      @video-call="startVideoCall"
      @show-profile="showSessionProfile"
    />

    <!-- 消息区域 -->
    <div ref="messageAreaRef" class="message-area" @contextmenu.prevent>
      <template v-for="(msg, index) in messagesWithTimeDivider" :key="msg.id || msg.clientMsgId || `divider-${index}`">
        <!-- 时间分割线 -->
        <div v-if="msg.isTimeDivider" class="time-divider">
          <span>{{ msg.timeText }}</span>
        </div>
        <!-- 消息气泡 -->
        <div
          v-else
          :data-message-id="msg.id"
          class="message-item"
          :class="{ isOwn: msg.isOwn || msg.senderId === currentUserId }"
        >
          <MessageBubble
            :message="msg"
            :is-own="msg.isOwn || msg.senderId === currentUserId"
            @right-click="showMessageMenu"
            @show-read-status="handleShowReadStatus"
          />
        </div>
      </template>
    </div>

    <!-- 输入区域 -->
    <ChatInput 
      v-if="currentSession"
      :disabled="uploading"
      @send-message="sendMessage"
      @upload-file="handleFileUpload"
      @upload-image="handleImageUpload"
    />

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-icon :size="64" color="#ddd">
        <ChatLineSquare />
      </el-icon>
      <p>选择一个会话开始聊天</p>
    </div>

    <!-- 已读回执弹窗 -->
    <ReadReceiptDialog
      v-model="readReceiptVisible"
      :message-id="selectedMessageId"
      @remind="handleRemindUnreadUsers"
    />
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { ChatLineSquare } from '@element-plus/icons-vue'
import ChatHeader from './ChatHeader.vue'
import ChatInput from './ChatInput.vue'
import MessageBubble from '@/components/Chat/MessageBubble.vue'
import ReadReceiptDialog from '@/components/Chat/ReadReceiptDialog.vue'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const store = useStore()

// 当前会话
const currentSession = computed(() => store.state.im.currentSession)

// 当前用户ID
const currentUserId = computed(() => store.state.user.userId)

// 消息列表
const messages = computed(() => {
  if (!currentSession.value) return []
  return store.state.im.messageList[currentSession.value.id] || []
})

// 带时间分割线的消息列表
const messagesWithTimeDivider = computed(() => {
  const msgs = messages.value
  if (msgs.length === 0) return []
  
  const result = []
  let lastHour = -1
  
  msgs.forEach((msg, index) => {
    // 添加时间分割线（每小时第一条消息前）
    const msgTime = new Date(msg.timestamp || msg.sendTime)
    const currentHour = msgTime.getHours()
    
    if (currentHour !== lastHour && index > 0) {
      lastHour = currentHour
      result.push({
        isTimeDivider: true,
        timeText: formatTimeDivider(msgTime)
      })
    }
    
    result.push(msg)
  })
  
  return result
})

// 格式化时间分割线显示
const formatTimeDivider = (date) => {
  const now = new Date()
  const diff = now - date
  
  // 小于1小时显示"XX分钟前"
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    if (minutes < 1) return '刚刚'
    return `${minutes}分钟前`
  }
  
  // 小于24小时显示"XX:XX"
  if (diff < 86400000) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 小于7天显示"星期X"
  if (diff < 604800000) {
    const weeks = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
    return weeks[date.getDay()]
  }
  
  // 超过7天显示"月日"
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 组件引用
const messageAreaRef = ref(null)
const uploading = ref(false)

// 已读回执弹窗
const readReceiptVisible = ref(false)
const selectedMessageId = ref(null)

// 搜索聊天记录
const searchInChat = () => {
  ElMessage.info('搜索功能开发中...')
}

// 开始语音通话
const startVoiceCall = () => {
  ElMessage.info('语音通话功能开发中...')
}

// 开始视频通话
const startVideoCall = () => {
  ElMessage.info('视频通话功能开发中...')
}

// 显示会话资料
const showSessionProfile = () => {
  ElMessage.info('会话资料功能开发中...')
}

// 显示消息右键菜单
const showMessageMenu = (event, message) => {
  // 显示消息操作菜单
  event.preventDefault()
}

// 显示已读状态详情
const handleShowReadStatus = (message) => {
  if (!message || !message.id) {
    ElMessage.warning('无法获取消息信息')
    return
  }
  // 排除临时消息（未发送成功的消息）
  if (String(message.id).startsWith('temp_')) {
    ElMessage.info('消息尚未发送完成，请稍后查看')
    return
  }
  selectedMessageId.value = message.id
  readReceiptVisible.value = true
}

// 处理提醒未读用户
const handleRemindUnreadUsers = (data) => {
  ElMessage.success('已提醒未读用户')
}

// 发送消息
const sendMessage = (content) => {
  if (!currentSession.value) return
  
  const message = {
    conversationId: currentSession.value.id,
    content: content,
    messageType: 'TEXT',
    senderId: currentUserId.value,
    isOwn: true,
    clientMsgId: `msg_${Date.now()}`,
    status: 'sending',
    sendTime: new Date().toISOString()
  }
  
  store.dispatch('im/sendMessage', message)
  
  nextTick(() => {
    scrollToBottom()
  })
}

// 处理文件上传
const handleFileUpload = (file) => {
  ElMessage.info('文件上传功能开发中...')
}

// 处理图片上传
const handleImageUpload = (file) => {
  ElMessage.info('图片上传功能开发中...')
}

// 滚动到底部
const scrollToBottom = () => {
  if (messageAreaRef.value) {
    messageAreaRef.value.scrollTop = messageAreaRef.value.scrollHeight
  }
}
</script>

<style scoped lang="scss">
.chat-panel {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #fff;
  transition: all 0.3s ease;

  &.collapsed {
    display: none;
  }
}

.message-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f5f5;

  .message-item {
    display: flex;
    margin-bottom: 16px;
    
    &.isOwn {
      flex-direction: row-reverse;
    }
  }
  
  // 时间分割线样式
  .time-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 16px 0;
    
    span {
      background: rgba(0, 0, 0, 0.04);
      padding: 4px 12px;
      border-radius: 4px;
      font-size: 12px;
      color: #8c8c8c;
    }
  }
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #b8b8b8;
  background: #f5f5f5;

  p {
    margin-top: 16px;
    font-size: 14px;
  }
}
</style>
