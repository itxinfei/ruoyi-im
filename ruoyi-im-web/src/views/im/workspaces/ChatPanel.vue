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
      <div
        v-for="msg in messages"
        :key="msg.id || msg.clientMsgId"
        :data-message-id="msg.id"
        class="message-item"
        :class="{ isOwn: msg.isOwn || msg.senderId === currentUserId }"
      >
        <MessageBubble 
          :message="msg"
          :is-own="msg.isOwn || msg.senderId === currentUserId"
          @right-click="showMessageMenu"
        />
      </div>
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

// 组件引用
const messageAreaRef = ref(null)
const uploading = ref(false)

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
