<template>
  <div class="chat-main-v4">
    <!-- 1. 核心会话区 (Header + List + Input) -->
    <div class="chat-container">
      <ChatWindowHeader
        :session="currentSession"
        :is-sidebar-open="isDetailOpen"
        @toggle-sidebar="isDetailOpen = !isDetailOpen"
        @toggle-selection="toggleSelectionMode"
        @open-search="showGlobalSearch = true"
        @voice-call="handleVoiceCall"
        @video-call="handleVideoCall"
      />

      <!-- 消息列表区 -->
      <div class="message-area-scroller custom-scrollbar" ref="messageListRef">
        <ChatMessageList
          ref="chatMessageListRef"
          :messages="messages"
          :is-selection-mode="isSelectionMode"
          :selected-messages="selectedMessages"
          @select-message="handleMessageClick"
          @load-more="handleLoadMore"
        />
      </div>

      <!-- 底部输入区 (对齐钉钉：通铺纯白) -->
      <ChatInputArea
        v-model="currentDraft"
        @send="processSendMessage"
      />

      <!-- 多选操作条 (悬浮覆盖) -->
      <SelectionActionBar
        v-if="isSelectionMode"
        :selected-count="selectedMessages.size"
        @cancel="isSelectionMode = false"
        @delete="handleBatchDelete"
        @forward="handleOpenForwardDialog"
        @favorite="handleBatchFavorite"
        @select-all="handleSelectAll"
      />

      <!-- 转发对话框 -->
      <ForwardDialog ref="forwardDialogRef" />
    </div>

    <!-- 2. 侧边详情区 (去虚构：Push 布局而非覆盖) -->
    <transition name="push-sidebar">
      <aside v-if="isDetailOpen" class="chat-right-sidebar">
        <ChatDetailDrawer :session="currentSession" @close="isDetailOpen = false" />
      </aside>
    </transition>
  </div>
</template>

<script setup lang="js">
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import ChatWindowHeader from './ChatWindow/ChatWindowHeader.vue'
import ChatMessageList from './ChatWindow/ChatMessageList.vue'
import ChatInputArea from './ChatInputArea.vue'
import SelectionActionBar from './ChatWindow/SelectionActionBar.vue'
import ChatDetailDrawer from '@/components/Chat/ChatDetailDrawer.vue'
import ForwardDialog from '@/components/ForwardDialog/index.vue'

const store = useStore()
const isDetailOpen = ref(false)
const isSelectionMode = ref(false)
const selectedMessages = ref(new Set())
const currentDraft = ref('')
const messageListRef = ref(null)
const chatMessageListRef = ref(null)
const isLoadingHistory = ref(false)
const forwardDialogRef = ref(null)

const currentSession = computed(() => store.state.im.session.currentSession)
const messages = computed(() => store.state.im.message.messages)

// 发送消息
const processSendMessage = (payload) => {
  store.dispatch('im/message/sendMessage', { ...payload, sessionId: currentSession.value.id })
}

// 加载更多历史消息
const handleLoadMore = async () => {
  if (isLoadingHistory.value || !currentSession.value) return

  const messages_data = messages.value
  if (!messages_data || messages_data.length === 0) return

  isLoadingHistory.value = true

  try {
    // 获取最早一条消息的 ID 作为分页起点
    const oldestMessage = messages_data[0]
    const lastMessageId = oldestMessage?.messageId || oldestMessage?.id

    await store.dispatch('im/message/loadMessages', {
      sessionId: currentSession.value.id,
      lastMessageId: lastMessageId,
      pageSize: 20,
      isLoadMore: true
    })
  } catch (error) {
    console.error('加载历史消息失败:', error)
  } finally {
    isLoadingHistory.value = false
  }
}

// 处理消息点击（多选模式）
const handleMessageClick = (message) => {
  if (isSelectionMode.value) {
    // 多选模式：切换选中状态
    if (selectedMessages.value.has(message.messageId)) {
      selectedMessages.value.delete(message.messageId)
    } else {
      selectedMessages.value.add(message.messageId)
    }
  }
}

// 切换多选模式
const toggleSelectionMode = () => {
  isSelectionMode.value = !isSelectionMode.value
  if (!isSelectionMode.value) {
    selectedMessages.value.clear()
  }
}

// 批量删除消息
const handleBatchDelete = async () => {
  const messageIds = Array.from(selectedMessages.value)
  if (messageIds.length === 0) return

  try {
    await store.dispatch('im/message/batchDeleteMessagesAction', messageIds)
    ElMessage.success(`已删除 ${messageIds.length} 条消息`)
    isSelectionMode.value = false
    selectedMessages.value.clear()
  } catch (error) {
    console.error('批量删除失败:', error)
    ElMessage.error('删除失败，请重试')
  }
}

// 批量收藏消息
const handleBatchFavorite = async () => {
  const messageIds = Array.from(selectedMessages.value)
  if (messageIds.length === 0) return

  try {
    for (const messageId of messageIds) {
      await store.dispatch('im/message/addFavorite', {
        messageId,
        conversationId: currentSession.value.id
      })
    }
    ElMessage.success(`已收藏 ${messageIds.length} 条消息`)
    isSelectionMode.value = false
    selectedMessages.value.clear()
  } catch (error) {
    console.error('批量收藏失败:', error)
    ElMessage.error('收藏失败，请重试')
  }
}

// 全选消息
const handleSelectAll = () => {
  messages.value.forEach(msg => {
    selectedMessages.value.add(msg.messageId)
  })
}

// 打开转发对话框
const handleOpenForwardDialog = () => {
  const selectedMsgs = messages.value.filter(msg => selectedMessages.value.has(msg.messageId))
  if (forwardDialogRef.value) {
    forwardDialogRef.value.open(selectedMsgs)
  }
}

// 发起语音通话
const handleVoiceCall = () => {
  ElMessage.info('语音通话功能开发中')
}

// 发起视频通话
const handleVideoCall = () => {
  ElMessage.info('视频通话功能开发中')
}
</script>

<style scoped lang="scss">
.chat-main-v4 {
  flex: 1;
  display: flex;
  height: 100%;
  overflow: hidden;
  background-color: var(--dt-bg-card);
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 400px;
  height: 100%;
  position: relative;
}

.message-area-scroller {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* 右侧侧边栏 (钉钉 Push 布局) */
.chat-right-sidebar {
  width: 320px;
  background-color: var(--dt-bg-card);
  border-left: 1px solid var(--dt-border-light);
  height: 100%;
  flex-shrink: 0;
  z-index: 50;
}

// 侧边栏滑出动效 (挤压感)
.push-sidebar-enter-active, .push-sidebar-leave-active {
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}
.push-sidebar-enter-from, .push-sidebar-leave-to {
  width: 0;
}
</style>
